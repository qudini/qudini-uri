# Qudini URIs

Utilities for URIs. Currently only covers URI creation and subpath creation, both with proper escaping and validation.

```java
class Application {
    public static void main(String[] args) {
        new com.qudini.uri.Builder().host("google.com").toString();

        // -> https://google.com


        new com.qudini.uri.Builder()
                .scheme(UriBuilder.Scheme.HTTPS)
                .host("qudini.com")
                .fragment("section-2")
                .user("qudini")
                .port(8080)
                .path("api", "queues", "add")
                .param("queu&#eId", 1)
                .param("activate", true)
                .toString();

        // -> https://qudini@qudini.com:8080/api/queues/add?activate=true&queu%26%23eId=1#section-2


        com.qudini.uri.Builder.createPath("api", "v1", "queue");

        // -> "api/v1/queue"
        // Don't worry if user input has a URI character; `#createPath`, like the rest of the Builder, will escape it.
    }
}
 ```