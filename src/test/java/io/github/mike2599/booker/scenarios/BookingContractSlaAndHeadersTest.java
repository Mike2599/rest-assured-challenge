package io.github.mike2599.booker.scenarios;

import static io.github.mike2599.booker.assertions.ResponseExpectations.jsonHeaders;
import static io.github.mike2599.booker.assertions.ResponseExpectations.okJson;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.lessThan;

import io.github.mike2599.booker.assertions.Ensure;
import io.github.mike2599.booker.screenplay.core.Actor;
import io.github.mike2599.booker.screenplay.tasks.ConsultBooking;
import io.github.mike2599.booker.screenplay.tasks.CreateBooking;
import io.github.mike2599.booker.screenplay.tasks.Memory;
import io.github.mike2599.booker.support.BookerScenario;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@Epic("API Restful-Booker")
@Feature("Escenario 4 · Validaciones avanzadas: contrato, SLA y headers")
@DisplayName("Escenario 4 · Validaciones avanzadas: contrato, SLA y headers")
class BookingContractSlaAndHeadersTest extends BookerScenario {

    @Test
    @Story("GET /booking/{id} cumple el JSON Schema, el SLA y los headers esperados")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("La consulta de una reserva existente cumple el contrato, responde en menos de 2000 ms y trae los headers esperados")
    void anExistingBookingHonoursContractSlaAndHeaders() {
        Actor auditor = actorNamed("Diego (auditor)");
        auditor.attemptsTo(CreateBooking.with(data.aBooking()));
        int id = auditor.recall(Memory.LAST_BOOKING_ID);

        auditor.attemptsTo(ConsultBooking.withId(id));
        auditor.should(
                Ensure.thatTheLastResponse("responde 200 con JSON", response -> response.spec(okJson())),
                Ensure.thatTheLastResponse("cumple estrictamente el contrato schemas/booking.json",
                        response -> response.body(matchesJsonSchemaInClasspath("schemas/booking.json"))),
                Ensure.thatTheLastResponse("responde en menos de %d ms".formatted(environment.slaResponseTimeMs()),
                        response -> response.time(lessThan(environment.slaResponseTimeMs()), TimeUnit.MILLISECONDS)),
                Ensure.thatTheLastResponse("trae Content-Type: application/json; charset=utf-8 y el header Server",
                        response -> response.spec(jsonHeaders())));
    }
}
