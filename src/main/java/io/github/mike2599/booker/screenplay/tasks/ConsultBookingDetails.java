package io.github.mike2599.booker.screenplay.tasks;

import io.github.mike2599.booker.api.specs.BookerSpecs;
import io.github.mike2599.booker.screenplay.abilities.CallTheBookerApi;
import io.github.mike2599.booker.screenplay.core.Actor;
import io.github.mike2599.booker.screenplay.core.Performable;
import io.restassured.common.mapper.TypeRef;
import io.restassured.path.json.JsonPath;
import java.io.UncheckedIOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Consulta el detalle de varias reservas y lo reúne en un solo documento JSON
 * ({@code [{bookingid, firstname, totalprice, ...}]}) sobre el que luego se aplican expresiones GPath.
 * Es necesario porque GET /booking solo devuelve IDs, sin el precio de cada reserva.
 */
public class ConsultBookingDetails implements Performable {

    private final List<Integer> ids;

    private ConsultBookingDetails(List<Integer> ids) {
        this.ids = List.copyOf(ids);
    }

    public static ConsultBookingDetails of(List<Integer> ids) {
        return new ConsultBookingDetails(ids);
    }

    @Override
    public void performAs(Actor actor) {
        CallTheBookerApi api = CallTheBookerApi.as(actor);
        List<Map<String, Object>> details = ids.stream()
                .map(id -> withId(id, api.record(api.bookings().get(id)).as(new TypeRef<Map<String, Object>>() {})))
                .toList();
        actor.remember(Memory.BOOKING_DETAILS, new JsonPath(toJson(details)));
    }

    private static Map<String, Object> withId(int id, Map<String, Object> booking) {
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("bookingid", id);
        detail.putAll(booking);
        return detail;
    }

    private static String toJson(Object value) {
        try {
            return BookerSpecs.jsonMapper().writeValueAsString(value);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public String toString() {
        return "consulta el detalle de las reservas " + ids;
    }
}
