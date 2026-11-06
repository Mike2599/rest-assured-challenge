package io.github.mike2599.booker.scenarios;

import static io.github.mike2599.booker.assertions.ResponseExpectations.okJson;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;

import io.github.mike2599.booker.assertions.Ensure;
import io.github.mike2599.booker.models.Booking;
import io.github.mike2599.booker.screenplay.core.Actor;
import io.github.mike2599.booker.screenplay.core.Question;
import io.github.mike2599.booker.screenplay.questions.TheBookingIds;
import io.github.mike2599.booker.screenplay.questions.TheLastResponse;
import io.github.mike2599.booker.screenplay.tasks.ConsultBooking;
import io.github.mike2599.booker.screenplay.tasks.ConsultBookingDetails;
import io.github.mike2599.booker.screenplay.tasks.CreateBooking;
import io.github.mike2599.booker.screenplay.tasks.ListBookings;
import io.github.mike2599.booker.screenplay.tasks.Memory;
import io.github.mike2599.booker.support.BookerScenario;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * GET /booking solo devuelve {@code [{"bookingid": n}]}: el precio no viene en la lista.
 * Para filtrar por {@code totalprice} sin depender de datos ajenos (la API es pública y se reinicia),
 * la prueba crea sus reservas con un apellido único y precios a ambos lados de un umbral aleatorio,
 * las encuentra con query params, reúne su detalle y filtra con GPath.
 */
@Epic("API Restful-Booker")
@Feature("Escenario 3 · Serialización, deserialización y expresiones GPath")
@DisplayName("Escenario 3 · Serialización, deserialización y expresiones GPath")
class BookingSearchWithGPathTest extends BookerScenario {

    @Test
    @Story("Filtrar reservas por totalprice con GPath y mapear una al tipo Booking")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("GPath extrae solo los IDs de reservas con totalprice mayor al umbral y la consulta se mapea a Booking")
    void gpathFiltersBookingsAboveAPriceThreshold() {
        Actor analyst = actorNamed("Laura (analista)");
        int threshold = data.aPriceThreshold();
        String lastname = data.aUniqueLastname();

        analyst.attemptsTo(
                CreateBooking.with(data.aBooking().toBuilder().lastname(lastname).totalprice(data.aPriceAbove(threshold)).build()),
                CreateBooking.with(data.aBooking().toBuilder().lastname(lastname).totalprice(data.aPriceAbove(threshold)).build()),
                CreateBooking.with(data.aBooking().toBuilder().lastname(lastname).totalprice(data.aPriceAtMost(threshold)).build()),
                CreateBooking.with(data.aBooking().toBuilder().lastname(lastname).totalprice(data.aPriceAtMost(threshold)).build()));
        List<Integer> created = analyst.recall(Memory.CREATED_BOOKING_IDS);
        List<Integer> expectedAboveThreshold = created.subList(0, 2);

        analyst.attemptsTo(ListBookings.all());
        analyst.should(Ensure.thatTheLastResponse("la lista general responde 200 e incluye las reservas creadas (GPath: bookingid)",
                response -> response
                        .spec(okJson())
                        .body("bookingid", hasItems(created.toArray(Integer[]::new)))
                        .body("findAll { it.bookingid > 0 }.size()", greaterThan(0))));

        analyst.attemptsTo(ListBookings.filteredBy(Map.of("lastname", lastname)));
        List<Integer> ids = analyst.asksFor(TheBookingIds.inTheLastResponse());
        analyst.should(Ensure.that(TheBookingIds.inTheLastResponse(), containsInAnyOrder(created.toArray(Integer[]::new))));

        analyst.attemptsTo(ConsultBookingDetails.of(ids));
        List<Integer> aboveThreshold = analyst.asksFor(TheBookingIds.withTotalPriceAbove(threshold));
        analyst.should(Ensure.that(TheBookingIds.withTotalPriceAbove(threshold),
                containsInAnyOrder(expectedAboveThreshold.toArray(Integer[]::new))));

        analyst.attemptsTo(ConsultBooking.withId(aboveThreshold.getFirst()));
        Booking booking = analyst.asksFor(TheLastResponse.bodyAs(Booking.class));
        analyst.should(
                Ensure.thatTheLastResponse("GET /booking/{id} responde 200", response -> response.spec(okJson())),
                Ensure.that(TheLastResponse.bodyAs(Booking.class), allOf(
                        hasProperty("lastname", equalTo(lastname)),
                        hasProperty("totalprice", greaterThan(threshold)))),
                Ensure.that(Question.about("si en las fechas anidadas (LocalDate) el checkout es posterior al checkin",
                                actor -> booking.getBookingdates().getCheckout().isAfter(booking.getBookingdates().getCheckin())),
                        is(true)));
    }
}
