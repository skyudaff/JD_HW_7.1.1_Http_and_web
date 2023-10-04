package org.example;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        final var server = new Server(9999, 64);

        server.addHandler("GET", "/messages", (request, responseStream) -> {
            try {
                String paramValue = request.getQueryParam("last");
                String responseBody = "GET param: " + paramValue;
                server.sendResponse(responseStream, 200, "OK", responseBody);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        server.addHandler("POST", "/messages", (request, responseStream) -> {
            try {
                String paramValue = request.getQueryParam("last");
                String responseBody = "POST param: " + paramValue;
                server.sendResponse(responseStream, 200, "OK", responseBody);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        server.listen();
    }
}
