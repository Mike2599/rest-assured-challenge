package io.github.mike2599.booker.scenarios;

import static io.github.mike2599.booker.assertions.ResponseExpectations.okJson;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasProperty;

import io.github.mike2599.booker.assertions.Ensure;
import io.github.mike2599.booker.models.Booking;
import io.github.mike2599.booker.models.BookingResponse;
import io.github.mike2599.booker.screenplay.core.Actor;
import io.github.mike2599.booker.screenplay.questions.TheLastResponse;
import io.github.mike2599.booker.screenplay.tasks.ConsultBooking;
import io.github.mike2599.booker.screenplay.tasks.CreateBooking;
import io.github.mike2599.booker.screenplay.tasks.Memory;
import io.github.mike2599.booker.support.BookerScenario;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@Epic("API Restful-Booker")
@Feature("Escenario 2 · Payload dinámico con Builder Pattern")
@DisplayName("Escenario 2 · Payload dinámico con Builder Pattern")
class DynamicBookingPayloadTest extends BookerScenario {

    @Test
    @Story("POST /booking con un POJO de Lombok (@Builder) y datos generados con Datafaker")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("La reserva creada con datos dinámicos se devuelve y se guarda tal como se envió")
    void theCreatedBookingMatchesTheGeneratedPayload() {
        Actor guest = actorNamed("Carlos (huésped)");
        Booking booking = data.aBooking();

        guest.attemptsTo(CreateBooking.with(booking));
        guest.should(
                Ensure.thatTheLastResponse("POST /booking responde 200 OK con JSON", response -> response.spec(okJson())),
                Ensure.that(TheLastResponse.bodyAs(BookingResponse.class), allOf(
                        hasProperty("bookingid", greaterThan(0)),
                        hasProperty("booking", equalTo(booking)))));

        int id = guest.recall(Memory.LAST_BOOKING_ID);
        guest.attemptsTo(ConsultBooking.withId(id));
        guest.should(
                Ensure.thatTheLastResponse("GET /booking/{id} responde 200 OK", response -> response.spec(okJson())),
                Ensure.that(TheLastResponse.bodyAs(Booking.class), equalTo(booking)));
    }
}
