package io.github.mike2599.booker.screenplay.tasks;

import io.github.mike2599.booker.models.Booking;
import io.github.mike2599.booker.screenplay.abilities.CallTheBookerApi;
import io.github.mike2599.booker.screenplay.core.Actor;
import io.github.mike2599.booker.screenplay.core.Performable;

/** Reemplaza una reserva completa con PUT /booking/{id} (requiere token). */
public class UpdateBooking implements Performable {

    private final int id;
    private final Booking booking;

    private UpdateBooking(int id, Booking booking) {
        this.id = id;
        this.booking = booking;
    }

    public static Builder withId(int id) {
        return booking -> new UpdateBooking(id, booking);
    }

    @Override
    public void performAs(Actor actor) {
        CallTheBookerApi api = CallTheBookerApi.as(actor);
        api.record(api.bookings().update(id, booking));
    }

    @Override
    public String toString() {
        return "actualiza la reserva %d a nombre de %s %s".formatted(id, booking.getFirstname(), booking.getLastname());
    }

    @FunctionalInterface
    public interface Builder {
        UpdateBooking using(Booking booking);
    }
}
