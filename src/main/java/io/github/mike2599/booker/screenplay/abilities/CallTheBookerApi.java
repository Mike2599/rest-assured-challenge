package io.github.mike2599.booker.screenplay.abilities;

import io.github.mike2599.booker.api.AuthApi;
import io.github.mike2599.booker.api.BookingApi;
import io.github.mike2599.booker.api.filters.TokenCookieFilter;
import io.github.mike2599.booker.api.specs.BookerSpecs;
import io.github.mike2599.booker.config.Environment;
import io.github.mike2599.booker.screenplay.core.Ability;
import io.github.mike2599.booker.screenplay.core.Actor;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

/**
 * Habilidad de llamar a Restful-Booker. Reúne los API Objects, el filtro que reutiliza el token
 * de la sesión del actor y la última respuesta recibida (que luego consultan las preguntas).
 */
public class CallTheBookerApi implements Ability {

    private final Environment environment;
    private final TokenCookieFilter session = new TokenCookieFilter();
    private final AuthApi auth;
    private final BookingApi bookings;
    private volatile Response lastResponse;

    private CallTheBookerApi(Environment environment) {
        this.environment = environment;
        RequestSpecification spec = BookerSpecs.request(environment, session);
        this.auth = new AuthApi(spec);
        this.bookings = new BookingApi(spec);
    }

    public static CallTheBookerApi at(Environment environment) {
        return new CallTheBookerApi(environment);
    }

    public static CallTheBookerApi as(Actor actor) {
        return actor.abilityTo(CallTheBookerApi.class);
    }

    public Environment environment() {
        return environment;
    }

    public TokenCookieFilter session() {
        return session;
    }

    public AuthApi auth() {
        return auth;
    }

    public BookingApi bookings() {
        return bookings;
    }

    /** Guarda la respuesta como la última recibida y la devuelve. */
    public Response record(Response response) {
        this.lastResponse = response;
        return response;
    }

    public Response lastResponse() {
        if (lastResponse == null) {
            throw new IllegalStateException("Todavía no se ha hecho ninguna petición");
        }
        return lastResponse;
    }
}
