# Rest Assured · Automatización de la API Restful-Booker

Solución a la **Prueba técnica: automatización de APIs REST** con **Rest Assured + Java 21 + Maven**, patrón **Screenplay** sobre **API Objects** y **Builder Pattern (Lombok)**.

El framework automatiza los cinco escenarios del enunciado contra [Restful-Booker](https://restful-booker.herokuapp.com/apidoc/index.html): autenticación con reutilización del token, payloads dinámicos, filtros con GPath, validación de contrato, SLA y headers, y casos negativos. Las pruebas corren en paralelo y cada petición y respuesta queda adjunta en Allure.

**Reporte de la última ejecución en CI:** [Allure en GitHub Pages](https://mike2599.github.io/rest-assured-challenge/)

---

## Contenido

- [Stack](#stack)
- [Requisitos previos](#requisitos-previos)
- [Instalación](#instalación)
- [Ejecución por ambiente](#ejecución-por-ambiente)
- [Reporte de pruebas](#reporte-de-pruebas)
- [Arquitectura](#arquitectura)
- [Escenarios automatizados](#escenarios-automatizados)
- [Criterios de calidad](#criterios-de-calidad)
- [Gestión de ambientes y seguridad](#gestión-de-ambientes-y-seguridad)
- [Estructura del proyecto](#estructura-del-proyecto)
- [Decisiones sobre el enunciado](#decisiones-sobre-el-enunciado)
- [Resultados de la ejecución](#resultados-de-la-ejecución)

---

## Stack

| Componente | Herramienta | Versión |
|---|---|---|
| Lenguaje | Java | 21 |
| Core test library | Rest Assured (+ json-schema-validator) | 5.5.7 |
| Test runner | JUnit 5 (Jupiter) | 5.14.4 |
| Gestor de dependencias | Maven (con Maven Wrapper incluido) | 3.9.16 |
| Mapeo de datos | Jackson Databind (+ jsr310 para `LocalDate`) | 2.22.4 |
| DTOs / Builder | Lombok (`@Builder`, `@Value`, `@Jacksonized`) | 1.18.48 |
| Datos dinámicos | Datafaker (sucesor de JavaFaker) | 2.7.0 |
| Configuración por ambiente | Typesafe Config (`application.conf`) | 1.4.10 |
| Reporte | Allure Framework (`allure-junit5`, `allure-rest-assured`, `allure-maven`) | 2.35.5 / reporte 2.46.1 |

## Requisitos previos

- **Java 21** (JDK). Verifica con `java -version`. El build falla con un mensaje claro si la versión es menor (`maven-enforcer-plugin`).
- **Maven 3.9 o superior**. Verifica con `mvn -v`.
  - Si no tienes Maven instalado, usa el **Maven Wrapper** incluido: `./mvnw` en Linux/macOS o `mvnw.cmd` en Windows. Descarga Maven 3.9.16 la primera vez.
- **Conexión a internet**: las pruebas llaman a la API pública `https://restful-booker.herokuapp.com`, y `allure:serve` descarga el generador de Allure la primera vez.

No se necesita instalar Allure aparte: el plugin `allure-maven` lo descarga.

## Instalación

```bash
git clone https://github.com/Mike2599/rest-assured-challenge.git
cd rest-assured-challenge
mvn -v            # o ./mvnw -v
```

## Ejecución por ambiente

El ambiente se elige con `-Denv` (por defecto `qa`). Cada ambiente está definido en [`application.conf`](src/main/resources/application.conf).

| Qué ejecutar | Maven | Maven Wrapper |
|---|---|---|
| Todas las pruebas en **QA** (por defecto) | `mvn clean test -Denv=qa` | `./mvnw clean test -Denv=qa` |
| Todas las pruebas en **staging** | `mvn clean test -Denv=staging` | `./mvnw clean test -Denv=staging` |
| Un escenario | `mvn clean test -Denv=qa -Dtest=BookingSearchWithGPathTest` | `./mvnw clean test -Dtest=BookingSearchWithGPathTest` |
| Cambiar los hilos paralelos (por defecto 5) | `mvn clean test -Denv=qa -Dthreads=2` | `./mvnw clean test -Dthreads=2` |

En Windows con PowerShell, pon las propiedades entre comillas: `mvn clean test "-Denv=qa"`, o usa `mvnw.cmd clean test "-Denv=qa"`.

Cualquier valor del ambiente se puede sobrescribir con variables de entorno, sin tocar archivos:

| Variable | Qué sobrescribe |
|---|---|
| `BOOKER_BASE_URL` | URL base de la API |
| `BOOKER_USERNAME` / `BOOKER_PASSWORD` | Credenciales de `POST /auth` |
| `BOOKER_SLA_MS` | SLA del tiempo de respuesta (por defecto 2000 ms) |

Si se pide un ambiente que no existe, la ejecución se detiene con un mensaje que lista los disponibles.

## Reporte de pruebas

| Acción | Comando |
|---|---|
| **Generar y abrir el reporte localmente** | `mvn allure:serve` (o `./mvnw allure:serve`) |
| Generar el reporte en `target/site/allure-maven-plugin` | `mvn allure:report` |

Primero se ejecutan las pruebas (`mvn clean test`) y luego el comando del reporte, que lee `target/allure-results`.

El reporte incluye:

- **Cada petición y respuesta adjunta** (`allure-rest-assured`): método, URL, headers, cookies, cuerpo, código de estado, tiempo y el comando `curl` equivalente.
- **Pasos Screenplay**: cada tarea, pregunta y verificación del actor aparece como un paso legible, por ejemplo *"Ana (administradora) actualiza la reserva 2149 a nombre de…"*.
- **Ambiente** usado (nombre, URL, SLA, Java, hilos, sistema operativo) y **categorías de fallo propias**: SLA incumplido, contrato roto, código de estado inesperado, datos incoherentes y API no disponible.
- **Limpieza**: la eliminación de las reservas que creó cada prueba, como paso de *tear down*.

En CI, cada push a `main` ejecuta la suite y publica el reporte en [GitHub Pages](https://mike2599.github.io/rest-assured-challenge/). También queda como artefacto del workflow.

## Arquitectura

El enunciado pide **Screenplay** en el encabezado y **API Object Pattern + Builder** en la sección de arquitectura. El framework usa los dos, cada uno en su capa:

```
 Prueba (escenario JUnit 5)        "qué" se prueba, en lenguaje de negocio
        │  actor.attemptsTo(...) · actor.asksFor(...) · actor.should(...)
        ▼
 Screenplay                        Actor · Tasks · Questions · Consequences (Ensure)
        │  usa la habilidad CallTheBookerApi
        ▼
 API Objects                       AuthApi · BookingApi: una operación por endpoint, sin aserciones
        │  given().spec(...)
        ▼
 Especificaciones y filtros        BookerSpecs (RequestSpecBuilder) · TokenCookieFilter · AllureRestAssured
        ▼
 Rest Assured → Restful-Booker
```

| Capa | Responsabilidad | Clases |
|---|---|---|
| **Contratos de petición** | Forma de los datos que viajan | `models/` (`Booking`, `BookingDates`, `BookingResponse`, `AuthRequest`, `AuthResponse`) con `@Builder` + `@Jacksonized` · `schemas/booking.json` |
| **Datos** | Valores dinámicos, nunca escritos a mano | `data/BookingFactory` (Datafaker) |
| **API Objects** | Cómo se llama a cada endpoint | `api/AuthApi`, `api/BookingApi`, `api/Endpoints` |
| **Especificaciones** | Lo común a todas las peticiones (DRY) | `api/specs/BookerSpecs` |
| **Sesión** | Reutilizar el token | `api/filters/TokenCookieFilter` |
| **Screenplay** | Quién hace qué | `screenplay/core`, `abilities`, `tasks`, `questions` |
| **Aserciones** | Qué se espera de una respuesta | `assertions/ResponseExpectations` (ResponseSpecBuilder) · `assertions/Ensure` |
| **Configuración** | Ambiente activo | `config/Environment` + `application.conf` |

La capa Screenplay es propia y liviana (Actor, Ability, Performable, Question, Consequence). Serenity BDD trae su propio reporte, que compite con Allure, y el enunciado pide Allure.

## Escenarios automatizados

Cinco casos de prueba independientes, uno por escenario, cada uno en su propia clase. Cada una crea sus propios datos y los elimina al terminar.

| Escenario | Clase | Cómo se cumple |
|---|---|---|
| **1. Autenticación dinámica y reutilización de sesión** | [`AuthenticationAndSessionReuseTest`](src/test/java/io/github/mike2599/booker/scenarios/AuthenticationAndSessionReuseTest.java) | `POST /auth` con las credenciales del ambiente → respuesta deserializada a `AuthResponse` → el token se entrega a `TokenCookieFilter`, un **Filter de Rest Assured** que inyecta `Cookie: token=<valor>` en cada PUT/PATCH/DELETE. La prueba actualiza (200) y elimina (201) una reserva y comprueba que el **mismo token** viajó en las **2** peticiones protegidas |
| **2. Payload dinámico con Builder Pattern** | [`DynamicBookingPayloadTest`](src/test/java/io/github/mike2599/booker/scenarios/DynamicBookingPayloadTest.java) | `Booking.builder()` (Lombok) con nombres, precio, depósito, fechas coherentes y necesidades generados por Datafaker. Sin JSON estáticos ni strings armados a mano. Valida **200 OK**, deserializa a `BookingResponse` y compara lo enviado contra lo devuelto y contra lo guardado (`GET /booking/{id}`) |
| **3. Serialización, deserialización y GPath** | [`BookingSearchWithGPathTest`](src/test/java/io/github/mike2599/booker/scenarios/BookingSearchWithGPathTest.java) | `GET /booking` (lista general) validada con GPath (`bookingid`, `findAll { it.bookingid > 0 }`). Filtra por **`totalprice` mayor a un umbral aleatorio** con `findAll { it.totalprice > threshold }.bookingid` y extrae los IDs **sin bucles `for`**. Consulta uno y lo mapea a `Booking.class` con sus fechas anidadas como `LocalDate`. Ver la [decisión sobre la lista sin precios](#decisiones-sobre-el-enunciado) |
| **4. Contrato, SLA y headers** | [`BookingContractSlaAndHeadersTest`](src/test/java/io/github/mike2599/booker/scenarios/BookingContractSlaAndHeadersTest.java) | `GET /booking/{id}` sobre una reserva existente: `matchesJsonSchemaInClasspath("schemas/booking.json")` con esquema **estricto** (campos obligatorios, tipos, formato de fechas, `additionalProperties: false`), `time(lessThan(2000L), MILLISECONDS)`, `Content-Type: application/json; charset=utf-8` exacto y presencia del header `Server` |
| **5. Manejo negativo y edge cases** | [`NegativeAndEdgeCasesTest`](src/test/java/io/github/mike2599/booker/scenarios/NegativeAndEdgeCasesTest.java) Una sola prueba con los pasos del enunciado: `PUT /booking/{id}` **sin token** y luego **con un token inválido**, ambos **403 Forbidden**. Verifica que la reserva no cambió y que `GET /booking/9999999` responde **404 Not Found** |

## Criterios de calidad

| Criterio | Cómo se cumple |
|---|---|
| **Clean Code y SOLID** | Una responsabilidad por clase: los API Objects solo llaman, las tareas solo actúan, las preguntas solo consultan y las aserciones viven en su propia capa. Las pruebas se leen como el escenario de negocio (`admin.attemptsTo(UpdateBooking.withId(id).using(changes))`) |
| **DRY** | `RequestSpecBuilder` en `BookerSpecs` (URL, JSON, mapper, timeouts, logs, Allure) y `ResponseSpecBuilder` en `ResponseExpectations` (`okJson()`, `deleted()`, `forbidden()`, `notFound()`, `jsonHeaders()`) |
| **Ejecución paralela** | JUnit 5 Parallel Execution (`junit-platform.properties`): clases y métodos concurrentes con 5 hilos, configurables con `-Dthreads`. No se usa la configuración estática de RestAssured: cada actor tiene su especificación, su filtro de sesión y su generador de datos |
| **Logs y debugging** | `log().ifValidationFails()` en las peticiones y en las validaciones, con `LogConfig.enableLoggingOfRequestAndResponseIfValidationFails(LogDetail.ALL)` y pretty print. Si una validación falla, la consola muestra la petición y la respuesta completas. Los headers sensibles (`Cookie`, `Authorization`) no se imprimen |

## Gestión de ambientes y seguridad

- **Ambientes** en [`application.conf`](src/main/resources/application.conf) (HOCON): un bloque `defaults` y un bloque por ambiente (`qa`, `staging`) que redefine solo lo que cambia. Se elige con `-Denv`, que el `pom.xml` pasa a las pruebas.
- **Sin tokens en el código**: el token se genera en tiempo de ejecución con `POST /auth` y vive solo en memoria, dentro del filtro de sesión de cada actor.
- **Credenciales**: son las de demostración que publica Restful-Booker (`admin` / `password123`), que el enunciado indica usar. Están en la configuración y no en el código, y se pueden reemplazar con `BOOKER_USERNAME` / `BOOKER_PASSWORD`. `Environment`, `AuthRequest` y `AuthResponse` ocultan la contraseña y el token en su `toString()`.
- **Solo la API pública provista**: no se usan mocks ni instancias locales.

## Estructura del proyecto

```
rest-assured-challenge
├── pom.xml                                  # Dependencias, Java 21 (enforcer), paralelismo, -Denv, allure-maven
├── mvnw · mvnw.cmd · .mvn/wrapper/          # Maven Wrapper (Maven 3.9.16)
├── .github/workflows/api-tests.yml          # CI: pruebas en cada push y reporte en GitHub Pages
├── src/main/resources/application.conf      # Ambientes: URL, credenciales, SLA, timeouts
├── src/main/java/io/github/mike2599/booker
│   ├── config/Environment.java              # Carga el ambiente elegido con -Denv
│   ├── models/                              # Contratos con Lombok @Builder
│   ├── data/BookingFactory.java             # Datos dinámicos con Datafaker
│   ├── api/
│   │   ├── AuthApi.java · BookingApi.java   # API Objects
│   │   ├── Endpoints.java
│   │   ├── specs/BookerSpecs.java           # RequestSpecBuilder común
│   │   └── filters/TokenCookieFilter.java   # Filter que reutiliza el token
│   ├── screenplay/
│   │   ├── core/                            # Actor, Ability, Performable, Question, Consequence
│   │   ├── abilities/CallTheBookerApi.java
│   │   ├── tasks/                           # Authenticate, CreateBooking, UpdateBooking, DeleteBooking, ...
│   │   └── questions/                       # TheLastResponse, TheBookingIds (GPath), TheSession
│   └── assertions/                          # ResponseExpectations (ResponseSpecBuilder) y Ensure
└── src/test
    ├── java/io/github/mike2599/booker
    │   ├── scenarios/                       # Los 5 escenarios
    │   └── support/                         # Base con limpieza de datos y ambiente para Allure
    └── resources
        ├── schemas/booking.json             # Contrato JSON Schema
        ├── junit-platform.properties        # Ejecución paralela
        ├── allure.properties · allure/categories.json
        └── META-INF/services/...            # Registro automático de la extensión de Allure
```

## Decisiones sobre el enunciado

| El enunciado dice | Se resolvió así | Por qué |
|---|---|---|
| "Patrón de diseño Screenplay" (encabezado) y "API Object Pattern + Builder" (arquitectura) | **Ambos**: Screenplay para expresar el escenario y API Objects para hablar con la API | Cada patrón resuelve un problema distinto y se complementan. Se implementó un Screenplay propio para no depender de Serenity, cuyo reporte compite con Allure |
| Escenario 3: filtrar la lista de `GET /booking` por `totalprice` | La prueba **crea sus reservas** con un apellido único y precios a ambos lados de un umbral aleatorio, las encuentra con el query param `lastname`, reúne su detalle y filtra con GPath | `GET /booking` solo devuelve `[{"bookingid": n}]`, sin precio. Además la API es pública, otros usuarios cambian sus datos y se reinicia periódicamente, así que filtrar datos ajenos no sería repetible |
| "Puedes apoyarte en JavaFaker" | **Datafaker** | Es el sucesor mantenido de JavaFaker, que no se actualiza desde 2020. La API es casi idéntica |
| Ambientes (`-Denv=qa`) | Ambientes `qa` y `staging` | Restful-Booker solo publica una instancia, así que los dos apuntan a la misma URL. `staging` demuestra cómo un ambiente sobrescribe solo lo que cambia (los timeouts), y agregar uno real es agregar un bloque |
| "Rest Assured 5.x+", "JUnit 5" | Rest Assured **5.5.7** y JUnit **5.14.4** | Las últimas versiones de las líneas que pide el enunciado |
| `Content-Type: application/json; charset=utf-8` como header "de seguridad o servidor" | Se valida ese header exacto y la presencia de `Server` | Restful-Booker no envía headers de seguridad como `Strict-Transport-Security` o `X-Content-Type-Options`, así que exigirlos haría fallar siempre la prueba. Expone `X-Powered-By: Express`, que en un sistema real se reportaría como hallazgo |
| Token en peticiones subsecuentes | El reporte de Allure muestra la cookie del token | Así se ve la inyección del filtro. El token es temporal y sale de credenciales públicas. En un sistema real se ocultaría como se hace en los logs |

Durante el desarrollo apareció un detalle de la API: Restful-Booker responde **418 I'm a teapot** a `POST /booking` si el header `Accept` no es exactamente `application/json`. `ContentType.JSON` de Rest Assured envía una lista de cuatro tipos, por eso `BookerSpecs` fija el valor exacto.

## Resultados de la ejecución

Ejecución local de referencia (Windows 11, JDK 21.0.13, Maven 3.9.16, 5 hilos):

| Métrica | Resultado |
|---|---|
| Pruebas | **5 / 5 exitosas**: un caso de prueba por escenario |
| Duración | ~6,5 s de reloj frente a ~23 s de tiempo acumulado, gracias a la ejecución en paralelo |
| Estabilidad | 3 ejecuciones seguidas sin fallos |
| Ambientes | `qa` y `staging` en verde. Un ambiente inexistente detiene la ejecución con un mensaje claro |
| Limpieza | Las reservas creadas responden 404 al terminar |

**Verificación de que las pruebas detectan fallos**: se rompió el contrato a propósito, agregando un campo obligatorio inexistente, y luego el SLA (`BOOKER_SLA_MS=1`). En ambos casos el escenario 4 falló con un mensaje claro, y `log().ifValidationFails()` imprimió la petición y la respuesta completas.

## Licencia

[MIT](LICENSE)
