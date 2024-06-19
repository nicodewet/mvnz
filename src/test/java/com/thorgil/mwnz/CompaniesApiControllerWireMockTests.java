package com.thorgil.mwnz;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.status;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.Fault;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Here we are replacing the XML API with a test double, specifically an HTTP stub (despite the term Mock in WireMock).
 * I've borrowed from this relatively authoritative resource: https://rieckpil.de/spring-boot-integration-tests-with-wiremock-and-junit-5/
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = {WireMockInitializer.class})
@AutoConfigureWebTestClient
public class CompaniesApiControllerWireMockTests {

    @Autowired
    private WireMockServer wireMockServer;

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void givenAValidCompanyAsXML_WhenWeFetchIt_ThenWeGetTheExpectedCompanyJSON() {

        // GIVEN
        wireMockServer.stubFor(WireMock.get("/xml-api/1.xml").willReturn(aResponse()
                .withHeader("Content-Type", MediaType.TEXT_PLAIN_VALUE)
                .withBody("""
                    <?xml version="1.0" encoding="UTF-8"?>
                     <Data>
                        <id>1</id>
                        <name>MWNZ</name>
                        <description>..is awesome</description>
                      </Data>   
                    """))

        );

        // WHEN & THEN
        this.webTestClient
                .get()
                .uri("/v1/companies/1")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody().json(
                        """
                        {
                            "id": 1,
                            "name": "MWNZ",
                            "description": "..is awesome"
                        }
                        """);

    }

    @Test
    void givenAMissingCompany_WhenWeFetchIt_ThenWeGetTheExpectedErrorJSON() {

        // GIVEN
        wireMockServer.stubFor(WireMock.get("/xml-api/0.xml").willReturn(status(404)));

        // WHEN & THEN
        this.webTestClient
                .get()
                .uri("/v1/companies/0")
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBody().json(
                        """
                        {
                            "error": "NOT_FOUND",
                            "error_description": "The company with id 0 was not found"
                        }
                        """);

    }

    /**
     * MALFORMED_RESPONSE_CHUNK: Send an OK status header, then garbage, then close the connection.
     * See: https://wiremock.org/docs/simulating-faults/#bad-responses
     */
    @Test
    void givenMalformedResponse_WhenWeFetchACompany_ThenWeGetTheExpectedErrorJSON() {

        // GIVEN
        wireMockServer.stubFor(WireMock.get("/xml-api/1.xml")
                .willReturn(aResponse().withFault(Fault.MALFORMED_RESPONSE_CHUNK)));

        // WHEN & THEN
        this.webTestClient
                .get()
                .uri("/v1/companies/1")
                .exchange()
                .expectStatus()
                .is5xxServerError()
                .expectBody().json(
                        """
                        {
                            "error": "INTERNAL_SERVER_ERROR",
                            "error_description": "An unexpected error occurred when fetching the company with id 1"
                        }
                        """);

    }

}
