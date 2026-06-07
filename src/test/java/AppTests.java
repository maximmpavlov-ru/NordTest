import Util.Actions;
import Util.Tokens;
import com.github.tomakehurst.wiremock.WireMockServer;
import io.restassured.response.ValidatableResponse;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.*;
import restAPI.RestAPIRequests;

import java.util.ArrayList;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

public class AppTests {
    private static WireMockServer wireMockServer;
    static List<String> tokens = new ArrayList<>();

    @BeforeAll
    @DisplayName("Запуск WireMock сервера для обработки запросов")
    public static void startWireMock() {
        WireMockServer wireMockServer = new WireMockServer(8888);
        wireMockServer.start();

        com.github.tomakehurst.wiremock.client.WireMock.configureFor("localhost", 8888);

        stubFor(post(urlEqualTo("/auth"))
                .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded"))
                .withRequestBody(containing("token"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/x-www-form-urlencoded")));


        stubFor(post(urlEqualTo("/doAction"))
                .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded"))
                .withRequestBody(containing("token"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/x-www-form-urlencoded")));
    }

    @AfterAll
    @DisplayName("Остановка WireMock сервера после прохождения тестов, если он запущен, и очистка тестовых данных")
    public static void stopWireMockAndClearData() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
        for (String token : tokens) {
            RestAPIRequests.sendPostRequest(token, Actions.LOGOUT);
        }
    }

    @Test
    @DisplayName("Попытка повторного использования токена, который уже зарегистрирован")
    public void doubleAuthenticationAttempt() {
        String token = Tokens.generateCorrectToken();
        RestAPIRequests.sendPostRequest(token, Actions.LOGIN);
        ValidatableResponse requestResponse = RestAPIRequests.sendPostRequest(token, Actions.LOGIN);
        int statusCode = requestResponse.extract().statusCode();
        tokens.add(token);

        Assertions.assertEquals(HttpStatus.SC_CONFLICT, statusCode, "Неправильный код ответа. Ожидается код 409");
    }

    @Test
    @DisplayName("Успешная аутентификация по токену")
    public void successfulAuthentication() {
        String token = Tokens.generateCorrectToken();
        ValidatableResponse requestResponse = RestAPIRequests.sendPostRequest(token, Actions.LOGIN);
        int statusCode = requestResponse.extract().statusCode();
        tokens.add(token);

        Assertions.assertEquals(HttpStatus.SC_OK, statusCode, "Неправильный код ответа. Ожидается код 200");
    }

    @Test
    @DisplayName("Попытка аутентификации с токеном некорректного формата")
    public void attemptToAuthenticateWithIncorrectToken() {
        ValidatableResponse requestResponse = RestAPIRequests.sendPostRequest(Tokens.generateShortToken(), Actions.LOGIN);
        int statusCode = requestResponse.extract().statusCode();

        Assertions.assertEquals(HttpStatus.SC_BAD_REQUEST, statusCode, "Неправильный код ответа. Ожидается код 400");
    }

    @Test
    @DisplayName("Попытка аутентификации без указания токена")
    public void attemptToAuthenticateWithoutToken() {
        ValidatableResponse requestResponse = RestAPIRequests.sendPostRequest("", Actions.LOGIN);
        int statusCode = requestResponse.extract().statusCode();

        Assertions.assertEquals(HttpStatus.SC_BAD_REQUEST, statusCode, "Неправильный код ответа. Ожидается код 400");
    }

    @Test
    @DisplayName("Успешное завершение сессии пользователя")
    public void successfulLogout() {
        String token = Tokens.generateCorrectToken();
        RestAPIRequests.sendPostRequest(token, Actions.LOGIN);
        ValidatableResponse requestResponse = RestAPIRequests.sendPostRequest(token, Actions.LOGOUT);
        int statusCode = requestResponse.extract().statusCode();

        Assertions.assertEquals(HttpStatus.SC_OK, statusCode, "Неправильный код ответа. Ожидается код 200");
    }

    @Test
    @DisplayName("Попытка завершения сессии с незарегистрированным токеном")
    public void attemptToLogoutWithUnknownToken() {
        ValidatableResponse requestResponse = RestAPIRequests.sendPostRequest(Tokens.generateCorrectToken(), Actions.LOGOUT);
        int statusCode = requestResponse.extract().statusCode();

        Assertions.assertEquals(HttpStatus.SC_FORBIDDEN, statusCode, "Неправильный код ответа. Ожидается код 403");
    }

    @Test
    @DisplayName("Попытка завершения сессии без указания токена")
    public void attemptToLogoutWithoutToken() {
        ValidatableResponse requestResponse = RestAPIRequests.sendPostRequest("", Actions.LOGOUT);
        int statusCode = requestResponse.extract().statusCode();

        Assertions.assertEquals(HttpStatus.SC_BAD_REQUEST, statusCode, "Неправильный код ответа. Ожидается код 400");
    }

    @Test
    @DisplayName("Попытка завершения сессии с токеном некорректной длины")
    public void attemptToLogoutWithShortToken() {
        ValidatableResponse requestResponse = RestAPIRequests.sendPostRequest(Tokens.generateShortToken(), Actions.LOGOUT);
        int statusCode = requestResponse.extract().statusCode();

        Assertions.assertEquals(HttpStatus.SC_BAD_REQUEST, statusCode, "Неправильный код ответа. Ожидается код 400");
    }

    @Test
    @DisplayName("Попытка завершения сессии с токеном некорректного формата")
    public void attemptToLogoutWithIncorrectToken() {
        ValidatableResponse requestResponse = RestAPIRequests.sendPostRequest(Tokens.generateIncorrectToken(), Actions.LOGOUT);
        int statusCode = requestResponse.extract().statusCode();

        Assertions.assertEquals(HttpStatus.SC_BAD_REQUEST, statusCode, "Неправильный код ответа. Ожидается код 400");
    }

    @Test
    @DisplayName("Попытка запроса без указания параметров token и action")
    public void requestWithoutTokenAndWithoutAction() {
        ValidatableResponse requestResponse = RestAPIRequests.sendPostRequest("", "");
        int statusCode = requestResponse.extract().statusCode();

        Assertions.assertEquals(HttpStatus.SC_BAD_REQUEST, statusCode, "Неправильный код ответа. Ожидается код 400");
    }

    @Test
    @DisplayName("Успешное обращение к ручке /action")
    public void successfulActionUsage() {
        String token = Tokens.generateCorrectToken();
        RestAPIRequests.sendPostRequest(token, Actions.LOGIN);
        ValidatableResponse requestResponse = RestAPIRequests.sendPostRequest(token, Actions.ACTION);
        int statusCode = requestResponse.extract().statusCode();
        tokens.add(token);

        Assertions.assertEquals(HttpStatus.SC_OK, statusCode, "Неправильный код ответа. Ожидается код 200");
    }

    @Test
    @DisplayName("Попытка обращения к ручке /action с незарегистрированным токеном")
    public void ActionUsageWithUnknowToken() {
        ValidatableResponse requestResponse = RestAPIRequests.sendPostRequest(Tokens.generateCorrectToken(), Actions.ACTION);
        int statusCode = requestResponse.extract().statusCode();

        Assertions.assertEquals(HttpStatus.SC_FORBIDDEN, statusCode, "Неправильный код ответа. Ожидается код 403");
    }

    @Test
    @DisplayName("Попытка обращения к ручке /action с токеном неправильного формата")
    public void ActionUsageWithIncorrectToken() {
        ValidatableResponse requestResponse = RestAPIRequests.sendPostRequest(Tokens.generateShortToken(), Actions.ACTION);
        int statusCode = requestResponse.extract().statusCode();

        Assertions.assertEquals(HttpStatus.SC_BAD_REQUEST, statusCode, "Неправильный код ответа. Ожидается код 400");
    }

    @Test
    @DisplayName("Попытка отправки запроса с некорректным параметром 'action'")
    public void requestAttemptWithUnknowActionType() {
        ValidatableResponse requestResponse = RestAPIRequests.sendPostRequest(Tokens.generateCorrectToken(), "BadValue");
        int statusCode = requestResponse.extract().statusCode();
        Assertions.assertEquals(HttpStatus.SC_BAD_REQUEST, statusCode, "Неправильный код ответа. Ожидается код 400");
    }

}
