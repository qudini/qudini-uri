package com.qudini.uri;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * Create URI strings with proper escaping.
 * <p>
 * Simple example:
 * <pre>{@code
 * new UriBuilder().host("google.com").toString()
 *
 * Returns: https://google.com
 * }</pre>
 * <p>
 * Complex example:
 * <pre>{@code
 * new UriBuilder()
 *         .scheme(UriBuilder.Scheme.HTTPS)
 *         .host("qudini.com")
 *         .fragment("section-2")
 *         .user("qudini")
 *         .port(8080)
 *         .path("api", "queues", "add")
 *         .param("queu&#eId", 1)
 *         .param("activate", true)
 *         .toString()
 *
 * Returns: https://qudini@qudini.com:8080/api/queues/add?activate=true&queu%26%23eId=1#section-2
 * }</pre>
 */
public final class Builder {

    private static final Map<Character, String> queryStringEscapes;

    static {
        Map<Character, String> e = new HashMap<>();
        e.put('!', "21");
        e.put('#', "23");
        e.put('$', "24");
        e.put('&', "26");
        e.put('\'', "27");
        e.put('(', "28");
        e.put(')', "29");
        e.put('*', "2A");
        e.put('+', "2B");
        e.put(',', "2C");
        e.put('/', "2F");
        e.put(':', "3A");
        e.put(';', "3B");
        e.put('=', "3D");
        e.put('?', "3F");
        e.put('@', "40");
        e.put('[', "5B");
        e.put(']', "5D");
        queryStringEscapes = Collections.unmodifiableMap(e);
    }

    private String host;
    private Map<String, Object> queryParameters = new HashMap<>();
    private Optional<List<String>> pathComponents = Optional.empty();
    private Optional<Long> port = Optional.empty();
    private Optional<Scheme> scheme = Optional.empty();
    private Optional<String> user = Optional.empty();
    private Optional<String> fragment = Optional.empty();

    private static void checkPath(String... components) {
        for (String component : components) {
            reject('/', new SlashInPathComponentException(), component);
            reject('#', new HashInPathComponentException(), component);
            reject('?', new QuestionMarkInPathComponentException(), component);
        }
    }

    public static String createPath(String... components) {
        checkPath(components);
        return String.join("/", components);
    }

    private static void reject(char rejected, InvalidValueException rejectionException, String subject) {
        if (subject.contains(String.valueOf(rejected))) {
            throw rejectionException;
        }
    }

    public Builder host(String host) {
        reject('/', new SlashInHostnameException(), host);
        reject(':', new ColonInHostnameException(), host);

        this.host = host;
        return this;
    }

    /**
     * Note that parameter order in the resulting URI is left undefined and could change.
     */
    public Builder param(String name, Object value) {
        if (queryParameters.containsKey(name)) {
            throw new ParameterAlreadySetException();
        }
        queryParameters.put(name, value);
        return this;
    }

    public Builder path(String... pathComponents) {
        if (this.pathComponents.isPresent()) {
            throw new PathAlreadySetException();
        }

        checkPath(pathComponents);
        this.pathComponents = Optional.of(Arrays.asList(pathComponents));
        return this;
    }

    public Builder port(long port) {
        this.port = Optional.of(port);
        return this;
    }

    public Builder scheme(Scheme scheme) {
        this.scheme = Optional.of(scheme);
        return this;
    }

    public Builder user(String user) {
        reject('@', new AtInUserNameException(), user);

        this.user = Optional.of(user);
        return this;
    }

    public Builder fragment(String fragment) {
        this.fragment = Optional.of(fragment);
        return this;
    }

    @Override
    public String toString() {
        return String.format(
                "%s://%s%s%s%s%s%s",
                scheme.orElse(Scheme.HTTPS).name().toLowerCase(),
                user.map(x -> x + "@").orElse(""),
                requireNonNull(host),
                port.map(x -> ":" + x.toString()).orElse(""),
                pathComponents.map(xs -> "/" + String.join("/", xs)).orElse(""),
                ((queryParameters.size() <= 0) ? "" : ("?" + queryParameters
                        .entrySet()
                        .stream()
                        .map(pair -> escapeQueryStringComponent(pair.getKey())
                                + "="
                                + pair.getValue()
                        )
                        .collect(Collectors.joining("&"))
                )),
                fragment.map(x -> "#" + x).orElse("")
        );
    }

    private String escapeQueryStringComponent(String component) {
        StringBuilder escaped = new StringBuilder();
        for (char x : component.toCharArray()) {
            if (queryStringEscapes.containsKey(x)) {
                escaped.append("%").append(queryStringEscapes.get(x));
            } else {
                escaped.append(x);
            }
        }
        return escaped.toString();
    }

    public enum Scheme {HTTP, HTTPS}

    private static abstract class InvalidValueException extends RuntimeException {
    }

    public static final class AtInUserNameException extends InvalidValueException {
    }

    public static final class SlashInHostnameException extends InvalidValueException {
    }

    public static final class ColonInHostnameException extends InvalidValueException {
    }

    public static final class SlashInPathComponentException extends InvalidValueException {
    }

    public static final class HashInPathComponentException extends InvalidValueException {
    }

    public static final class QuestionMarkInPathComponentException extends InvalidValueException {
    }

    public static final class PathAlreadySetException extends RuntimeException {
    }

    public static final class ParameterAlreadySetException extends RuntimeException {
    }
}

