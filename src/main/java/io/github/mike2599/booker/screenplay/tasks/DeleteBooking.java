package io.github.mike2599.booker.screenplay.tasks;

import io.github.mike2599.booker.screenplay.abilities.CallTheBookerApi;
import io.github.mike2599.booker.screenplay.core.Actor;
import io.github.mike2599.booker.screenplay.core.Performable;

/** Elimina una reserva con DELETE /booking/{id} (requiere token). */
public class DeleteBooking implements Performable {

    private final int id;

    private DeleteBooking(int id) {
        this.id = id;
    }

    public static DeleteBooking withId(int id) {
        return new DeleteBooking(id);
    }

    @Override
    public void performAs(Actor actor) {
        CallTheBookerApi api = CallTheBookerApi.as(actor);
        api.record(api.bookings().delete(id));
    }

    @Override
    public String toString() {
        return "elimina la reserva " + id;
    }
}
