package io.github.mike2599.booker.support;

import io.github.mike2599.booker.config.Environment;
import io.github.mike2599.booker.data.BookingFactory;
import io.github.mike2599.booker.screenplay.abilities.CallTheBookerApi;
import io.github.mike2599.booker.screenplay.core.Actor;
import io.github.mike2599.booker.screenplay.tasks.Authenticate;
import io.github.mike2599.booker.screenplay.tasks.DeleteBooking;
import io.github.mike2599.booker.screenplay.tasks.Memory;
import io.qameta.allure.Allure;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.AfterEach;

/**
 * Base de los escenarios: entrega actores con la habilidad de llamar a la API del ambiente activo,
 * un generador de datos propio de cada prueba y, al terminar, elimina las reservas que la prueba creó
 * (la API es pública y compartida, así que cada prueba deja el entorno como lo encontró).
 */
public abstract class BookerScenario {

    protected final Environment environment = Environment.current();
    protected final BookingFactory data = new BookingFactory();
    private final List<Actor> cast = new CopyOnWriteArrayList<>();

    protected Actor actorNamed(String name) {
        Actor actor = Actor.named(name).whoCan(CallTheBookerApi.at(environment));
        cast.add(actor);
        return actor;
    }

    @AfterEach
    void deleteTheBookingsCreatedByTheTest() {
        List<Integer> created = cast.stream()
                .flatMap(actor -> actor.<List<Integer>>recallOrDefault(Memory.CREATED_BOOKING_IDS, List.of()).stream())
                .toList();
        if (created.isEmpty()) {
            return;
        }
        Allure.step("Limpieza: elimina las reservas creadas por la prueba " + created, () -> {
            Actor cleaner = Actor.named("Limpieza").whoCan(CallTheBookerApi.at(environment));
            cleaner.attemptsTo(Authenticate.withTheEnvironmentCredentials());
            created.forEach(id -> cleaner.attemptsTo(DeleteBooking.withId(id)));
        });
    }
}
