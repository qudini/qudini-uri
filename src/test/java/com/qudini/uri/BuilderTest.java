package com.qudini.uri;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class BuilderTest {

    private static final String HOST = "qudini.com";
    private static final String USER = "qudini";
    private static final int PORT = 80;

    @Test
    public void justHost() {
        assertEquals("https://qudini.com", new Builder().host(HOST).toString());
    }

    @Test
    public void userAndHost() {
        assertEquals("https://qudini@qudini.com", new Builder().host(HOST).user(USER).toString());
    }

    @Test
    public void hostAndPort() {
        assertEquals("https://qudini.com:80", new Builder().host(HOST).port(PORT).toString());
    }

    @Test
    public void schemeAndHost() {
        String uri = new Builder()
                .scheme(Builder.Scheme.HTTP)
                .host(HOST)
                .toString();
        assertEquals(uri, "http://qudini.com");
    }

    @Test
    public void hostPortAndPath() {
        String uri = new Builder().host(HOST).port(PORT).path("api", "v2", "resource").toString();
        assertEquals("https://qudini.com:80/api/v2/resource", uri);
    }

    @Test
    public void hostPathAndParameters() {
        String uri = new Builder()
                .host(HOST)
                .path("api", "v2", "resource")
                .param("abc", "def")
                .param("123", "456")
                .toString();

        // The API does not guarantee parameter order, so be prepared to update this test according to implementation
        // changes.
        assertEquals("https://qudini.com/api/v2/resource?123=456&abc=def", uri);
    }

    @Test
    public void failingHostPathAndParameters() {
        try {
            new Builder().host(HOST).path("api", "v2", "abc/def?ghi#123").toString();
        } catch (
                Builder.HashInPathComponentException
                        | Builder.QuestionMarkInPathComponentException
                        | Builder.SlashInPathComponentException e
                ) {
            return;
        }
        fail("an invalid URI should cause an exception");
    }

    @Test
    public void createPath() {
        assertEquals(Builder.createPath("123", "abc", "42", "foo"), "/123/abc/42/foo");
    }
}
