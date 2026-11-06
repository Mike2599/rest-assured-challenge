package io.github.mike2599.booker.models;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * Respuesta de POST /auth. Con credenciales válidas trae {@code token};
 * con credenciales inválidas la API responde 200 con {@code reason: "Bad credentials"}.
 */
@Value
@Builder
@Jacksonized
public class AuthResponse {
    String token;
    String reason;

    @Override
    public String toString() {
        return "AuthResponse(token=" + (token == null ? null : "****") + ", reason=" + reason + ")";
    }
}
