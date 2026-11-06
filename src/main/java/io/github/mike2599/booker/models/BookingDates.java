package io.github.mike2599.booker.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/** Fechas de la reserva, anidadas dentro de {@link Booking}. */
@Value
@Builder(toBuilder = true)
@Jacksonized
public class BookingDates {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    LocalDate checkin;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    LocalDate checkout;
}
