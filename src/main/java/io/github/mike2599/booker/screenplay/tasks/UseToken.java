package io.github.mike2599.booker.screenplay.tasks;

import io.github.mike2599.booker.screenplay.abilities.CallTheBookerApi;
import io.github.mike2599.booker.screenplay.core.Actor;
import io.github.mike2599.booker.screenplay.core.Performable;

/** Fuerza el token de la sesión (por ejemplo, uno inválido para los casos negativos). */
public class UseToken implements Performable {

    private final String token;

    private UseToken(String token) {
        this.token = token;
    }

    public static UseToken withValue(String token) {
        return new UseToken(token);
    }

    @Override
    public void performAs(Actor actor) {
        CallTheBookerApi.as(actor).session().useToken(token);
    }

    @Override
    public String toString() {
        return "usa un token de sesión forzado";
    }
}
