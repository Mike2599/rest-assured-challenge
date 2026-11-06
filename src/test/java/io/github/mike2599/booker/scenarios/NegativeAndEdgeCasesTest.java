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

@Epic("API Restful-Booker")
@Feature("Escenario 5 · Manejo negativo y edge cases")
@DisplayName("Escenario 5 · Manejo negativo y edge cases")
class NegativeAndEdgeCasesTest extends BookerScenario {

    @Test
    @Story("Actualizar sin autorización y consultar una reserva inexistente")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("PUT sin token o con un token inválido responde 403 Forbidden y un ID inexistente responde 404 Not Found")
    void unauthorizedUpdatesAreForbiddenAndUnknownBookingsAreNotFound() {
        Actor intruder = actorNamed("Pablo (sin permisos)");
        Booking original = data.aBooking();
        intruder.attemptsTo(CreateBooking.with(original));
        int id = intruder.recall(Memory.LAST_BOOKING_ID);

        intruder.attemptsTo(UpdateBooking.withId(id).using(data.aBooking()));
        intruder.should(Ensure.thatTheLastResponse("PUT sin token responde 403 Forbidden",
                response -> response.spec(forbidden())));

        intruder.attemptsTo(
                UseToken.withValue(data.anInvalidToken()),
                UpdateBooking.withId(id).using(data.aBooking()));
        intruder.should(Ensure.thatTheLastResponse("PUT con un token inválido responde 403 Forbidden",
                response -> response.spec(forbidden())));

        intruder.attemptsTo(ConsultBooking.withId(id));
        intruder.should(
                Ensure.thatTheLastResponse("la reserva sigue disponible", response -> response.spec(okJson())),
                Ensure.that(TheLastResponse.bodyAs(Booking.class), equalTo(original)));

        int nonexistentId = environment.nonexistentBookingId();
        intruder.attemptsTo(ConsultBooking.withId(nonexistentId));
        intruder.should(Ensure.thatTheLastResponse("GET /booking/%d responde 404 Not Found".formatted(nonexistentId),
                response -> response.spec(notFound())));
    }
}
