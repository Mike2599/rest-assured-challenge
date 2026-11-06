package io.github.mike2599.booker.screenplay.core;

import io.qameta.allure.Allure;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Protagonista de cada escenario Screenplay: tiene habilidades, ejecuta tareas, hace preguntas,
 * verifica consecuencias y recuerda datos entre pasos. Cada acción queda como un paso en Allure.
 */
public final class Actor {

    private final String name;
    private final Map<Class<? extends Ability>, Ability> abilities = new ConcurrentHashMap<>();
    private final Map<String, Object> memory = new ConcurrentHashMap<>();

    private Actor(String name) {
        this.name = name;
    }

    public static Actor named(String name) {
        return new Actor(name);
    }

    public String name() {
        return name;
    }

    public Actor whoCan(Ability... newAbilities) {
        Arrays.stream(newAbilities).forEach(ability -> abilities.put(ability.getClass(), ability));
        return this;
    }

    public <A extends Ability> A abilityTo(Class<A> type) {
        Ability ability = abilities.get(type);
        if (ability == null) {
            throw new IllegalStateException("%s no tiene la habilidad %s".formatted(name, type.getSimpleName()));
        }
        return type.cast(ability);
    }

    public void attemptsTo(Performable... tasks) {
        Arrays.stream(tasks).forEach(task -> Allure.step(name + " " + task, () -> task.performAs(this)));
    }

    public <T> T asksFor(Question<T> question) {
        return Allure.step(name + " consulta " + question, () -> question.answeredBy(this));
    }

    public void should(Consequence... consequences) {
        Arrays.stream(consequences)
                .forEach(consequence -> Allure.step(name + " verifica que " + consequence, () -> consequence.evaluateFor(this)));
    }

    public <T> void remember(String key, T value) {
        memory.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T recall(String key) {
        Object value = memory.get(key);
        if (value == null) {
            throw new IllegalStateException("%s no recuerda '%s' (¿falló un paso anterior?)".formatted(name, key));
        }
        return (T) value;
    }

    @SuppressWarnings("unchecked")
    public <T> T recallOrDefault(String key, T defaultValue) {
        return (T) memory.getOrDefault(key, defaultValue);
    }

    @Override
    public String toString() {
        return name;
    }
}
