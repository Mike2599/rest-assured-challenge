package io.github.mike2599.booker.screenplay.tasks;

import io.github.mike2599.booker.config.Environment;
import io.github.mike2599.booker.models.AuthRequest;
import io.github.mike2599.booker.models.AuthResponse;
import io.github.mike2599.booker.screenplay.abilities.CallTheBookerApi;
import io.github.mike2599.booker.screenplay.core.Actor;
import io.github.mike2599.booker.screenplay.core.Performable;
import io.restassured.response.Response;

/**
 * Obtiene un token con POST /auth, lo deserializa a {@link AuthResponse} y se lo entrega al filtro de sesión
 * del actor, que lo inyecta como cookie en cada PUT y DELETE posterior.
 */
public class Authenticate implements Performable {

    private final AuthRequest credentials;

    private Authenticate(AuthRequest credentials) {
        this.credentials = credentials;
    }

    /** Credenciales del ambiente activo (application.conf o variables de entorno). */
    public static Authenticate withTheEnvironmentCredentials() {
        Environment environment = Environment.current();
        return new Authenticate(AuthRequest.builder()
                .username(environment.username())
                .password(environment.password())
                .build());
    }

    @Override
    public void performAs(Actor actor) {
        CallTheBookerApi api = CallTheBookerApi.as(actor);
        Response response = api.record(api.auth().createToken(credentials));
        AuthResponse auth = response.as(AuthResponse.class);
        if (auth.getToken() == null || auth.getToken().isBlank()) {
            throw new IllegalStateException("POST /auth no devolvió un token (HTTP %d, reason=%s)"
                    .formatted(response.statusCode(), auth.getReason()));
        }
        api.session().useToken(auth.getToken());
        actor.remember(Memory.AUTH_TOKEN, auth.getToken());
    }

    @Override
    public String toString() {
        return "se autentica como '" + credentials.getUsername() + "' y guarda el token de sesión";
    }
}
