package io.github.mike2599.booker.assertions;

import static org.hamcrest.MatcherAssert.assertThat;

import io.github.mike2599.booker.screenplay.abilities.CallTheBookerApi;
import io.github.mike2599.booker.screenplay.core.Consequence;
import io.github.mike2599.booker.screenplay.core.Question;
import io.restassured.response.ValidatableResponse;
import java.util.function.Consumer;
import org.hamcrest.Matcher;

/**
 * Consecuencias que el actor verifica con {@code actor.should(...)}.
 * Las validaciones sobre la respuesta usan {@code then().log().ifValidationFails()}: si fallan,
 * la petición y la respuesta completas quedan en el log para diagnosticar.
 */
public final class Ensure {

    private Ensure() {
    }

    /** Valida la última respuesta con el DSL de Rest Assured (specs, GPath, schema, tiempos, headers). */
    public static Consequence thatTheLastResponse(String description, Consumer<ValidatableResponse> validation) {
        return describedAs(description, actor -> validation.accept(
                CallTheBookerApi.as(actor).lastResponse().then().log().ifValidationFails()));
    }

    /** Compara la respuesta a una pregunta contra un matcher de Hamcrest. */
    public static <T> Consequence that(Question<T> question, Matcher<? super T> matcher) {
        return describedAs(question + " " + matcher, actor -> assertThat(question.toString(), question.answeredBy(actor), matcher));
    }

    private static Consequence describedAs(String description, Consequence consequence) {
        return new Consequence() {
            @Override
            public void evaluateFor(io.github.mike2599.booker.screenplay.core.Actor actor) {
                consequence.evaluateFor(actor);
            }

            @Override
            public String toString() {
                return description;
            }
        };
    }
}
