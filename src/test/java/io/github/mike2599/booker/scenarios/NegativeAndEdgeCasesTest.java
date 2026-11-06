package io.github.mike2599.booker.scenarios;

import static io.github.mike2599.booker.assertions.ResponseExpectations.forbidden;
import static io.github.mike2599.booker.assertions.ResponseExpectations.notFound;
import static io.github.mike2599.booker.assertions.ResponseExpectations.okJson;
import static org.hamcrest.Matchers.equalTo;

import io.github.mike2599.booker.assertions.Ensure;
import io.github.mike2599.booker.models.Booking;
import io.github.mike2599.booker.screenplay.core.Actor;
import io.github.mike2599.booker.screenplay.questions.TheLastResponse;
import io.github.mike2599.booker.screenplay.tasks.ConsultBooking;
import io.github.mike2599.booker.screenplay.tasks.CreateBooking;
import io.github.mike2599.booker.screenplay.tasks.Memory;
import io.github.mike2599.booker.screenplay.tasks.UpdateBooking;
import io.github.mike2599.booker.screenplay.tasks.UseToken;
import io.github.mike2599.booker.support.BookerScenario;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@Epic("API Restful-Booker")
@Feature("Escenario 5 · Manejo negativo y edge cases")
@DisplayName("Escenario 5 · Manejo negativo y edge cases")
class NegativeAndEdgeCasesTest extends BookerScenario {

    enum Credential {
        SIN_TOKEN("sin token"),
        TOKEN_INVALIDO("con un token inválido");

        private final String description;

        Credential(String description) {
            this.description = description;
        }

        @Override
        public String toString() {
            return description;
        }
    }

    @ParameterizedTest(name = "PUT /booking/:id {0} responde 403 y la reserva no cambia")
    @EnumSource(Credential.class)
    @Story("Actualizar sin autorización")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Actualizar una reserva sin un token válido es rechazado con 403 Forbidden")
    void updatingWithoutAValidTokenIsForbidden(Credential credential) {
        Actor intruder = actorNamed("Pablo (sin permisos)");
        Booking original = data.aBooking();
        intruder.attemptsTo(CreateBooking.with(original));
        int id = intruder.recall(Memory.LAST_BOOKING_ID);

        if (credential == Credential.TOKEN_INVALIDO) {
            intruder.attemptsTo(UseToken.withValue(data.anInvalidToken()));
        }
        intruder.attemptsTo(UpdateBooking.withId(id).using(data.aBooking()));
        intruder.should(Ensure.thatTheLastResponse("PUT %s responde 403 Forbidden".formatted(credential),
                response -> response.spec(forbidden())));

        intruder.attemptsTo(ConsultBooking.withId(id));
        intruder.should(
                Ensure.thatTheLastResponse("la reserva sigue disponible", response -> response.spec(okJson())),
                Ensure.that(TheLastResponse.bodyAs(Booking.class), equalTo(original)));
    }

    @Test
    @Story("Consultar una reserva inexistente")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Consultar un ID inexistente responde 404 Not Found")
    void consultingANonexistentBookingReturnsNotFound() {
        Actor guest = actorNamed("Sofía (huésped)");
        int nonexistentId = environment.nonexistentBookingId();

        guest.attemptsTo(ConsultBooking.withId(nonexistentId));
        guest.should(Ensure.thatTheLastResponse("GET /booking/%d responde 404 Not Found".formatted(nonexistentId),
                response -> response.spec(notFound())));
    }
}
