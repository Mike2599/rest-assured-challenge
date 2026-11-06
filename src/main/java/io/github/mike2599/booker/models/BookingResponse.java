package io.github.mike2599.booker.models;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/** Respuesta de POST /booking: el ID asignado y la reserva tal como quedó guardada. */
@Value
@Builder
@Jacksonized
public class BookingResponse {
    Integer bookingid;
    Booking booking;
}
