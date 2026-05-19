package roomescape.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class UserReservationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String login() {
        jdbcTemplate.update("INSERT INTO member (name, email, password) VALUES (?, ?, ?)", "브라운", "brown@example.com", "password1");

        Map<String, String> loginBody = new HashMap<>();
        loginBody.put("email", "brown@example.com");
        loginBody.put("password", "password1");

        Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(loginBody)
                .when().post("/login");

        return response.getCookie("JSESSIONID");
    }

    @Test
    void 예약_정상_흐름_테스트() {
        String sessionId = login();
        createTheme();

        Map<String, String> times1 = new HashMap<>();
        times1.put("startAt", "10:00");
        RestAssured.given().contentType(ContentType.JSON).body(times1)
                .when().post("/admin/times").then().statusCode(201);

        Map<String, String> times2 = new HashMap<>();
        times2.put("startAt", "11:00");
        RestAssured.given().contentType(ContentType.JSON).body(times2)
                .when().post("/admin/times").then().statusCode(201);

        RestAssured.given().log().all()
                .when().get("/times/available?themeId=1&date=2026-06-04")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(2));

        Map<String, Object> reservation = new HashMap<>();
        reservation.put("date", "2026-06-04");
        reservation.put("timeId", 1);
        reservation.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("JSESSIONID", sessionId)
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .when().get("/times/available?themeId=1&date=2026-06-04")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));
    }

    @Test
    void 예약_날짜_시간_변경() {
        String sessionId = login();
        createTheme();
        createTime("10:00");
        createTime("11:00");

        Map<String, Object> reservation = new HashMap<>();
        reservation.put("date", "2026-08-05");
        reservation.put("timeId", 1);
        reservation.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("JSESSIONID", sessionId)
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        Map<String, Object> update = new HashMap<>();
        update.put("date", "2026-08-06");
        update.put("timeId", 2);
        update.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(update)
                .when().patch("/reservations/1")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    void 중복_예약시_409를_반환한다() {
        String sessionId = login();
        createTheme();
        createTime("10:00");

        Map<String, Object> reservation = createReservationBody("2026-08-05", 1, 1);
        RestAssured.given().contentType(ContentType.JSON)
                .cookie("JSESSIONID", sessionId)
                .body(reservation)
                .when().post("/reservations").then().statusCode(201);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("JSESSIONID", sessionId)
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(409)
                .body("errorCode", is("DUPLICATE_RESERVATION"))
                .body("message", is("이미 예약된 시간입니다."));
    }

    @Test
    void 이미_예약된_시간으로_변경시_409를_반환한다() {
        String sessionId = login();
        createTheme();
        createTime("10:00");
        createTime("11:00");

        Map<String, Object> reservation1 = createReservationBody("2026-08-05", 1, 1);
        RestAssured.given().contentType(ContentType.JSON)
                .cookie("JSESSIONID", sessionId)
                .body(reservation1)
                .when().post("/reservations").then().statusCode(201);

        Map<String, Object> reservation2 = createReservationBody("2026-08-05", 2, 1);
        RestAssured.given().contentType(ContentType.JSON)
                .cookie("JSESSIONID", sessionId)
                .body(reservation2)
                .when().post("/reservations").then().statusCode(201);

        Map<String, Object> update = createReservationBody("2026-08-05", 2, 1);
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(update)
                .when().patch("/reservations/1")
                .then().log().all()
                .statusCode(409)
                .body("errorCode", is("DUPLICATE_RESERVATION"))
                .body("message", is("이미 예약된 시간입니다."));
    }

    @Test
    void 존재하지_않는_예약_변경시_404를_반환한다() {
        createTheme();
        createTime("10:00");

        Map<String, Object> update = createReservationBody("2026-08-05", 1, 1);
        RestAssured.given().log().all()
                .contentType(ContentType.JSON).body(update)
                .when().patch("/reservations/999")
                .then().log().all()
                .statusCode(404)
                .body("errorCode", is("RESERVATION_NOT_FOUND"))
                .body("message", is("999번 예약을 찾을 수 없습니다."));
    }

    @Test
    void 존재하지_않는_시간으로_예약시_404를_반환한다() {
        String sessionId = login();
        createTheme();

        Map<String, Object> reservation = createReservationBody("2026-08-05", 999, 1);
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("JSESSIONID", sessionId)
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(404)
                .body("errorCode", is("TIME_NOT_FOUND"))
                .body("message", is("999번 예약 시간을 찾을 수 없습니다."));
    }

    @Test
    void 존재하지_않는_테마로_예약시_404를_반환한다() {
        String sessionId = login();
        createTime("10:00");

        Map<String, Object> reservation = createReservationBody("2026-08-05", 1, 999);
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("JSESSIONID", sessionId)
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(404)
                .body("errorCode", is("THEME_NOT_FOUND"))
                .body("message", is("999번 테마를 찾을 수 없습니다."));
    }

    @Test
    void 과거_날짜로_예약시_400을_반환한다() {
        String sessionId = login();
        createTheme();
        createTime("10:00");

        Map<String, Object> reservation = createReservationBody("2020-01-01", 1, 1);
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("JSESSIONID", sessionId)
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400)
                .body("errorCode", is("INVALID_DATE_OR_TIME"))
                .body("message", is("이미 지난 날짜이거나 시간입니다."));
    }

    @Test
    void 과거_날짜로_변경시_400을_반환한다() {
        String sessionId = login();
        createTheme();
        createTime("10:00");

        Map<String, Object> reservation = createReservationBody("2026-08-05", 1, 1);
        RestAssured.given().contentType(ContentType.JSON)
                .cookie("JSESSIONID", sessionId)
                .body(reservation)
                .when().post("/reservations").then().statusCode(201);

        Map<String, Object> update = createReservationBody("2020-01-01", 1, 1);
        RestAssured.given().log().all()
                .contentType(ContentType.JSON).body(update)
                .when().patch("/reservations/1")
                .then().log().all()
                .statusCode(400)
                .body("errorCode", is("INVALID_DATE_OR_TIME"))
                .body("message", is("이미 지난 날짜이거나 시간입니다."));
    }

    @Test
    void 비로그인_예약시_401을_반환한다() {
        createTheme();
        createTime("10:00");

        Map<String, Object> reservation = createReservationBody("2026-08-05", 1, 1);
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(401);
    }

    private void createTheme() {
        Map<String, String> theme = new HashMap<>();
        theme.put("name", "무서운 이야기");
        theme.put("description", "공포");
        theme.put("url", "http://example.com");
        RestAssured.given().contentType(ContentType.JSON).body(theme)
                .when().post("/admin/themes").then().statusCode(201);
    }

    private void createTime(String startAt) {
        Map<String, String> time = new HashMap<>();
        time.put("startAt", startAt);
        RestAssured.given().contentType(ContentType.JSON).body(time)
                .when().post("/admin/times").then().statusCode(201);
    }

    private Map<String, Object> createReservationBody(String date, int timeId, int themeId) {
        Map<String, Object> body = new HashMap<>();
        body.put("date", date);
        body.put("timeId", timeId);
        body.put("themeId", themeId);
        return body;
    }
}
