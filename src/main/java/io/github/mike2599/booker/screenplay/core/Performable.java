package io.github.mike2599.booker.screenplay.core;

/**
 * Una tarea o interacción que un actor puede ejecutar.
 * {@link #toString()} describe la acción en lenguaje de negocio y se usa como nombre del paso en Allure.
 */
@FunctionalInterface
public interface Performable {
    void performAs(Actor actor);
}
