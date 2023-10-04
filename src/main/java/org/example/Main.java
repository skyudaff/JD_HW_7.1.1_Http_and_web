package org.example;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        final var server = new Server(9999, 64);

        server.addHandler("GET", "/message", (request, responseStream) -> {
            try {
                String responseBody = "Hello from GET";
                server.sendResponse(responseStream, 200, "OK", responseBody);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        server.addHandler("POST", "/message", (request, responseStream) -> {
            try {
                String responseBody = "Hello from POST";
                server.sendResponse(responseStream, 200, "OK", responseBody);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        server.listen();
    }
}
