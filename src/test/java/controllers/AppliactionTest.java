package controllers;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@QuarkusTest
@TestHTTPEndpoint(Application.class)
public class AppliactionTest {

    @Test
    public void getKnownShortcut() {
        given()
                .redirects().follow(false)
                .get("/{tag}", "cVsWU")
                .then()
                .statusCode(303);
    }

    @Test
    public void getKnownShortcutInfo() {
        given()
                .redirects().follow(false)
                .get("/{tag}?info=true", "cVsWU")
                .then()
                .statusCode(200)
                .contentType("application/json");
    }

    @Test
    public void getByIdNotFound() {
        given()
                .redirects().follow(false)
                .get("/{tag}", 987654321)
                .then()
                .statusCode(404);
    }

    @Test
    public void getQRCodeAsPNG() {
        given()
                .redirects().follow(false)
                .get("/{scheme}/{target}-qr.png", "lot-id", "4D48C8650442")
                .then()
                .statusCode(200)
                .contentType("image/png");
    }   

    @Test
    public void getQRCodeAsSVG() {
        given()
                .redirects().follow(false)
                .get("/{scheme}/{target}-qr.svg", "lot-id", "4D48C8650442")
                .then()
                .statusCode(200)
                .contentType("image/svg+xml");
    }   
}