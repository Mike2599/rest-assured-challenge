package io.github.mike2599.booker.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/** Contrato de una reserva: cuerpo de POST/PUT /booking y respuesta de GET /booking/{id}. */
@Value
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Booking {
    String firstname;
    String lastname;
    Integer totalprice;
    Boolean depositpaid;
    BookingDates bookingdates;
    String additionalneeds;
}
