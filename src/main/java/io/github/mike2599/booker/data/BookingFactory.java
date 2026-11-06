package io.github.mike2599.booker.data;

import io.github.mike2599.booker.models.Booking;
import io.github.mike2599.booker.models.BookingDates;
import java.time.LocalDate;
import java.util.Locale;
import net.datafaker.Faker;

/**
 * Genera reservas válidas y distintas en cada ejecución con Datafaker (sucesor mantenido de JavaFaker).
 * Ningún valor está escrito a mano: nombres, precio, depósito, fechas y necesidades salen del generador.
 * Cada prueba crea su propia instancia (Faker no es seguro entre hilos).
 */
public class BookingFactory {

    private static final int MIN_PRICE = 50;
    private static final int MAX_PRICE = 3000;

    private final Faker faker = new Faker(Locale.of("es"));

    /** Una reserva completa con fechas futuras coherentes (checkout posterior a checkin). */
    public Booking aBooking() {
        LocalDate checkin = LocalDate.now().plusDays(faker.number().numberBetween(1, 120));
        return Booking.builder()
                .firstname(faker.name().firstName())
                .lastname(faker.name().lastName())
                .totalprice(faker.number().numberBetween(MIN_PRICE, MAX_PRICE))
                .depositpaid(faker.bool().bool())
                .bookingdates(BookingDates.builder()
                        .checkin(checkin)
                        .checkout(checkin.plusDays(faker.number().numberBetween(1, 21)))
                        .build())
                .additionalneeds(faker.options().option("Desayuno", "Late checkout", "Cuna para bebé",
                        "Traslado al aeropuerto", "Parqueadero", "Habitación con vista"))
                .build();
    }

    /** Un apellido único por ejecución, para aislar los datos de una prueba en una API pública compartida. */
    public String aUniqueLastname() {
        return faker.name().lastName() + "-" + faker.regexify("[a-z0-9]{8}");
    }

    /** Un umbral de precio aleatorio, dejando margen para generar precios por encima y por debajo. */
    public int aPriceThreshold() {
        return faker.number().numberBetween(500, 2000);
    }

    public int aPriceAbove(int threshold) {
        return faker.number().numberBetween(threshold + 1, threshold + 1000);
    }

    public int aPriceAtMost(int threshold) {
        return faker.number().numberBetween(MIN_PRICE, threshold + 1);
    }

    /** Un token con formato plausible pero que la API no emitió. */
    public String anInvalidToken() {
        return faker.regexify("[a-f0-9]{15}");
    }
}
