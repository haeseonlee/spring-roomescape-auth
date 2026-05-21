package roomescape.admin;

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
public class MissionStepTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String login() {
        jdbcTemplate.update("INSERT INTO member (name, email, password, role) VALUES (?, ?, ?, ?)", "브라운", "brown@example.com", "password1", "ADMIN");

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
    void 예약_조회() {
        RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(0));
    }

    @Test
    void 예약_추가_및_삭제() {
        String sessionId = login();
        jdbcTemplate.update("INSERT INTO theme (name, description, url) VALUES (?, ?, ?)", "무서워", "akdk", "https://hello.com");
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", "15:40");

        Map<String, Object> params = new HashMap<>();
        params.put("date", "2026-08-05");
        params.put("timeId", 1);
        params.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("JSESSIONID", sessionId)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .body("id", is(1));

        RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));

        RestAssured.given().log().all()
                .cookie("JSESSIONID", sessionId)
                .when().delete("/reservations/1")
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(0));
    }
}
