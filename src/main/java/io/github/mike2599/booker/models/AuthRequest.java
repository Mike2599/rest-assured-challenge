package io.github.mike2599.booker.models;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/** Cuerpo de POST /auth. */
@Value
@Builder
@Jacksonized
public class AuthRequest {
    String username;
    String password;

    /** La contraseña no se imprime en logs ni en los pasos del reporte. */
    @Override
    public String toString() {
        return "AuthRequest(username=" + username + ", password=****)";
    }
}
