package io.github.mike2599.booker.screenplay.core;

/** Un resultado esperado que el actor verifica con {@link Actor#should(Consequence...)}. */
@FunctionalInterface
public interface Consequence {
    void evaluateFor(Actor actor);
}
