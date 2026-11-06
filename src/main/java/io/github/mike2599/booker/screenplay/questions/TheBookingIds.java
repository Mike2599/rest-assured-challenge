package io.github.mike2599.booker.screenplay.questions;

import io.github.mike2599.booker.screenplay.abilities.CallTheBookerApi;
import io.github.mike2599.booker.screenplay.core.Question;
import io.github.mike2599.booker.screenplay.tasks.Memory;
import io.restassured.path.json.JsonPath;
import java.util.List;

/** Extrae IDs de reserva con expresiones GPath de Rest Assured, sin recorrer listas con bucles en Java. */
public final class TheBookingIds {

    private TheBookingIds() {
    }

    /** Todos los IDs de la última respuesta de GET /booking. */
    public static Question<List<Integer>> inTheLastResponse() {
        return Question.about("los IDs de reserva de la última respuesta (GPath: bookingid)",
                actor -> CallTheBookerApi.as(actor).lastResponse().jsonPath().getList("bookingid", Integer.class));
    }

    /** IDs de las reservas consultadas cuyo {@code totalprice} supera el umbral. */
    public static Question<List<Integer>> withTotalPriceAbove(int threshold) {
        return Question.about("los IDs de reserva con totalprice mayor a " + threshold
                        + " (GPath: findAll { it.totalprice > umbral }.bookingid)",
                actor -> actor.<JsonPath>recall(Memory.BOOKING_DETAILS)
                        .param("threshold", threshold)
                        .getList("findAll { it.totalprice > threshold }.bookingid", Integer.class));
    }
}
