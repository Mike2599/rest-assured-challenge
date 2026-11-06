package io.github.mike2599.booker.screenplay.tasks;

import io.github.mike2599.booker.models.Booking;
import io.github.mike2599.booker.models.BookingResponse;
import io.github.mike2599.booker.screenplay.abilities.CallTheBookerApi;
import io.github.mike2599.booker.screenplay.core.Actor;
import io.github.mike2599.booker.screenplay.core.Performable;
import io.restassured.response.Response;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** Crea una reserva con POST /booking y recuerda su ID (también para limpiarla al final de la prueba). */
public class CreateBooking implements Performable {

    private final Booking booking;

    private CreateBooking(Booking booking) {
        this.booking = booking;
    }

    public static CreateBooking with(Booking booking) {
        return new CreateBooking(booking);
    }

    @Override
    public void performAs(Actor actor) {
        CallTheBookerApi api = CallTheBookerApi.as(actor);
        Response response = api.record(api.bookings().create(booking));
        if (response.statusCode() == 200) {
            int id = response.as(BookingResponse.class).getBookingid();
            actor.remember(Memory.LAST_BOOKING_ID, id);
            List<Integer> created = actor.recallOrDefault(Memory.CREATED_BOOKING_IDS, new CopyOnWriteArrayList<>());
            created.add(id);
            actor.remember(Memory.CREATED_BOOKING_IDS, created);
        }
    }

    @Override
    public String toString() {
        return "crea una reserva para %s %s por %d".formatted(booking.getFirstname(), booking.getLastname(), booking.getTotalprice());
    }
}
