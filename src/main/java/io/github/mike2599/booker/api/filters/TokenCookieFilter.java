package io.github.mike2599.booker.api.filters;

import io.restassured.filter.FilterContext;
import io.restassured.filter.OrderedFilter;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Reutiliza dinámicamente el token de sesión: inyecta {@code Cookie: token=<valor>} en las peticiones
 * de modificación (PUT, PATCH y DELETE), que son las que Restful-Booker protege.
 * El token no está en el código ni en la configuración: lo entrega la tarea {@code Authenticate} en tiempo de ejecución.
 * Corre antes que el filtro de Allure, así el reporte muestra la petición tal como salió.
 */
public class TokenCookieFilter implements OrderedFilter {

    private static final Set<String> PROTECTED_METHODS = Set.of("PUT", "PATCH", "DELETE");

    private final AtomicReference<String> token = new AtomicReference<>();
    private final AtomicInteger injections = new AtomicInteger();

    public void useToken(String value) {
        token.set(value);
    }

    public void forgetToken() {
        token.set(null);
    }

    public boolean hasToken() {
        return token.get() != null;
    }

    /** Cuántas peticiones salieron con el token inyectado (sirve para verificar la reutilización de la sesión). */
    public int injections() {
        return injections.get();
    }

    @Override
    public Response filter(FilterableRequestSpecification request,
                           FilterableResponseSpecification response,
                           FilterContext context) {
        String current = token.get();
        if (current != null && PROTECTED_METHODS.contains(request.getMethod())) {
            request.removeCookies().cookie("token", current);
            injections.incrementAndGet();
        }
        return context.next(request, response);
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }
}
