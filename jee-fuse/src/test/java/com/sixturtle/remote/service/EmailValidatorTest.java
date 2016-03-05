package com.sixturtle.remote.service;

import static org.junit.Assert.*;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import org.junit.Rule;
import org.junit.Test;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

/**
 * jUnit tests for {@link EmailValidator} API.
 *
 * @author Anurag Sharma
 */
public class EmailValidatorTest {
    protected WireMockRule wireMockRule = new WireMockRule(8089);

    @Rule
    public WireMockRule getWireMock() {
        return wireMockRule;
    }

    @Test
    public void testEmailSuccess() throws Exception {
        EmailValidatorImpl client = new EmailValidatorImpl();
        String email = "john.doe@domain.com";

        stubFor(get(urlPathEqualTo(client.getContext().getApiPath()))
            .withQueryParam("email", equalTo(email))
            .willReturn(
                aResponse().withStatus(Response.Status.OK.getStatusCode())
                           .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                           .withBodyFile("/valid-response.json")));

        assertTrue("invalid response", client.isValidEmail(email));
    }

    @Test
    public void testEmailFail() throws Exception {
        EmailValidatorImpl client = new EmailValidatorImpl();
        String email = "invalid@domain.com";

        stubFor(get(urlPathEqualTo(client.getContext().getApiPath()))
            .withQueryParam("email", equalTo(email))
            .willReturn(
                aResponse().withStatus(Response.Status.OK.getStatusCode())
                           .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                           .withBodyFile("/invalid-response.json")));

        assertFalse("invalid response", client.isValidEmail(email));
    }
}
