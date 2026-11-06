package io.github.mike2599.booker.screenplay.core;

import java.util.function.Function;

/** Algo que un actor puede consultar sobre el estado del sistema (la respuesta, la sesión, los datos). */
@FunctionalInterface
public interface Question<T> {

    T answeredBy(Actor actor);

    /** Crea una pregunta con una descripción legible para el reporte. */
    static <T> Question<T> about(String subject, Function<Actor, T> answer) {
        return new Question<>() {
            @Override
            public T answeredBy(Actor actor) {
                return answer.apply(actor);
            }

            @Override
            public String toString() {
                return subject;
            }
        };
    }
}
