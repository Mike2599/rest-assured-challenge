package io.github.mike2599.booker.scenarios;

import static io.github.mike2599.booker.assertions.ResponseExpectations.deleted;
import static io.github.mike2599.booker.assertions.ResponseExpectations.notFound;
import static io.github.mike2599.booker.assertions.ResponseExpectations.okJson;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import io.github.mike2599.booker.assertions.Ensure;
import io.github.mike2599.booker.models.AuthResponse;
import io.github.mike2599.booker.models.Booking;
import io.github.mike2599.booker.screenplay.core.Actor;
import io.github.mike2599.booker.screenplay.questions.TheLastResponse;
import io.github.mike2599.booker.screenplay.questions.TheSession;
import io.github.mike2599.booker.screenplay.tasks.Authenticate;
import io.github.mike2599.booker.screenplay.tasks.ConsultBooking;
import io.github.mike2599.booker.screenplay.tasks.CreateBooking;
import io.github.mike2599.booker.screenplay.tasks.DeleteBooking;
import io.github.mike2599.booker.screenplay.tasks.Memory;
import io.github.mike2599.booker.screenplay.tasks.UpdateBooking;
import io.github.mike2599.booker.support.BookerScenario;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@Epic("API Restful-Booker")
@Feature("Escenario 1 · Autenticación dinámica y reutilización de sesión")
@DisplayName("Escenario 1 · Autenticación dinámica y reutilización de sesión")
class AuthenticationAndSessionReuseTest extends BookerScenario {

    @Test
    @Story("El token de POST /auth se reutiliza en PUT y DELETE mediante un Filter de Rest Assured")
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("Un solo token obtenido en /auth autoriza la actualización y la eliminación de una reserva")
    void theTokenFromAuthIsReusedForEveryModification() {
        Actor admin = actorNamed("Ana (administradora)");

        admin.attemptsTo(Authenticate.withTheEnvironmentCredentials());
        admin.should(
                Ensure.thatTheLastResponse("POST /auth responde 200 con un token", response -> response
                        .spec(okJson())
                        .body("token", not(emptyOrNullString()))),
                Ensure.that(TheLastResponse.bodyAs(AuthResponse.class), hasProperty("token", not(emptyOrNullString()))));

        Booking original = data.aBooking();
        admin.attemptsTo(CreateBooking.with(original));
        int id = admin.recall(Memory.LAST_BOOKING_ID);

        Booking changes = data.aBooking();
        admin.attemptsTo(UpdateBooking.withId(id).using(changes));
        admin.should(
                Ensure.thatTheLastResponse("PUT con la cookie del token responde 200", response -> response.spec(okJson())),
                Ensure.that(TheLastResponse.bodyAs(Booking.class), equalTo(changes)));

        admin.attemptsTo(DeleteBooking.withId(id));
        admin.should(Ensure.thatTheLastResponse("DELETE con la cookie del token responde 201", response -> response.spec(deleted())));

        admin.attemptsTo(ConsultBooking.withId(id));
        admin.should(
                Ensure.thatTheLastResponse("la reserva eliminada ya no existe", response -> response.spec(notFound())),
                Ensure.that(TheSession.tokenInjections(), is(2)));
    }
}
