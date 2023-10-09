package org.example;

import org.apache.http.NameValuePair;

import java.io.IOException;
import java.util.List;

public class Main {
    public static final String GET = "GET";
    public static final String POST = "POST";
    public static final int OK = 200;
    public static final int BAD_REQUEST = 400;
    public static final int NOT_FOUND = 404;

    public static void main(String[] args) {
        final var server = new Server(9999, 64);

        server.addHandler(GET, "/messages", (request, responseStream) -> {
            try {
                String paramName = "last";
                String paramValue = request.getQueryParam(paramName);
                String responseBody = "GET param: " + paramName + "=" + paramValue;
                System.out.println(responseBody);
                server.sendResponse(responseStream, OK, "OK", responseBody);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        server.addHandler(POST, "/messages", (request, responseStream) -> {
            try {
                List<NameValuePair> formParams = request.getFormParams();
                int paramIndex = 1;
                StringBuilder responseBodyBuilder = new StringBuilder();
                for (NameValuePair param : formParams) {
                    String paramName = param.getName();
                    String paramValue = param.getValue();
                    responseBodyBuilder.append("POST ").append("param ").append(paramIndex).append(": ")
                            .append(paramName).append(": ").append(paramValue).append("\n");
                    paramIndex++;
                }
                if (formParams.isEmpty()) {
                    server.sendResponse(
                            responseStream, BAD_REQUEST, "Bad Request", "No POST params found");
                } else {
                    String responseBody = responseBodyBuilder.toString();
                    System.out.println(responseBody);
                    server.sendResponse(responseStream, OK, "OK", responseBody);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        server.listen();
    }
}