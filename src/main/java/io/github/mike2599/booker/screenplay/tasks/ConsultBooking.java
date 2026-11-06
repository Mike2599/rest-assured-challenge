package io.github.mike2599.booker.screenplay.tasks;

import io.github.mike2599.booker.screenplay.abilities.CallTheBookerApi;
import io.github.mike2599.booker.screenplay.core.Actor;
import io.github.mike2599.booker.screenplay.core.Performable;

/** Consulta una reserva con GET /booking/{id}. */
public class ConsultBooking implements Performable {

    private final int id;

    private ConsultBooking(int id) {
        this.id = id;
    }

    public static ConsultBooking withId(int id) {
        return new ConsultBooking(id);
    }

    @Override
    public void performAs(Actor actor) {
        CallTheBookerApi api = CallTheBookerApi.as(actor);
        api.record(api.bookings().get(id));
    }

    @Override
    public String toString() {
        return "consulta la reserva " + id;
    }
}
