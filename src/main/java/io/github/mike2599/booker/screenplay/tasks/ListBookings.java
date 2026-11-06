package io.github.mike2599.booker.screenplay.tasks;

import io.github.mike2599.booker.screenplay.abilities.CallTheBookerApi;
import io.github.mike2599.booker.screenplay.core.Actor;
import io.github.mike2599.booker.screenplay.core.Performable;
import java.util.Map;

/** Lista reservas con GET /booking, opcionalmente filtradas por query params (firstname, lastname, checkin, checkout). */
public class ListBookings implements Performable {

    private final Map<String, ?> filters;

    private ListBookings(Map<String, ?> filters) {
        this.filters = filters;
    }

    public static ListBookings all() {
        return new ListBookings(Map.of());
    }

    public static ListBookings filteredBy(Map<String, ?> filters) {
        return new ListBookings(Map.copyOf(filters));
    }

    @Override
    public void performAs(Actor actor) {
        CallTheBookerApi api = CallTheBookerApi.as(actor);
        api.record(api.bookings().list(filters));
    }

    @Override
    public String toString() {
        return filters.isEmpty() ? "consulta la lista general de reservas" : "consulta las reservas filtradas por " + filters;
    }
}
