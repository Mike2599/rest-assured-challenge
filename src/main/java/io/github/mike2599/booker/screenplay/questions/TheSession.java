package io.github.mike2599.booker.screenplay.questions;

import io.github.mike2599.booker.screenplay.abilities.CallTheBookerApi;
import io.github.mike2599.booker.screenplay.core.Question;

/** Preguntas sobre la sesión del actor (el token que reutiliza el filtro). */
public final class TheSession {

    private TheSession() {
    }

    public static Question<Boolean> hasAToken() {
        return Question.about("si la sesión tiene un token",
                actor -> CallTheBookerApi.as(actor).session().hasToken());
    }

    public static Question<Integer> tokenInjections() {
        return Question.about("cuántas peticiones llevaron el token en la cookie",
                actor -> CallTheBookerApi.as(actor).session().injections());
    }
}
