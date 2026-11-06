package io.github.mike2599.booker.screenplay.questions;

import io.github.mike2599.booker.screenplay.abilities.CallTheBookerApi;
import io.github.mike2599.booker.screenplay.core.Question;

/** Preguntas sobre la última respuesta que recibió el actor. */
public final class TheLastResponse {

    private TheLastResponse() {
    }

    public static Question<Integer> statusCode() {
        return Question.about("el código de estado de la última respuesta",
                actor -> CallTheBookerApi.as(actor).lastResponse().statusCode());
    }

    /** Deserializa el cuerpo a una clase Java tipada (por ejemplo, {@code Booking.class}). */
    public static <T> Question<T> bodyAs(Class<T> type) {
        return Question.about("el cuerpo de la última respuesta como " + type.getSimpleName(),
                actor -> CallTheBookerApi.as(actor).lastResponse().as(type));
    }

    /** Evalúa una expresión GPath sobre el cuerpo JSON de la última respuesta. */
    public static <T> Question<T> path(String gpath) {
        return Question.about("'" + gpath + "' en la última respuesta",
                actor -> CallTheBookerApi.as(actor).lastResponse().path(gpath));
    }
}
