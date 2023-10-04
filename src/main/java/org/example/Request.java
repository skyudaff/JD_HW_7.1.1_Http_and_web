package org.example;

import java.io.BufferedReader;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class Request {
    private final String method;
    private final String path;
    private final Map<String, String> headers = new HashMap<>();
    private final InputStream bodyStream;

    public Request(String method, String path, BufferedReader reader, InputStream bodyStream) {
        this.method = method;
        this.path = path;
        this.bodyStream = bodyStream;
        parseHeaders(reader);
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getHeader(String name) {
        return headers.get(name);
    }

    public InputStream getBodyStream() {
        return bodyStream;
    }

    private void parseHeaders(BufferedReader reader) {
        try {
            String line;
            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                String[] parts = line.split(": ", 2);
                if (parts.length == 2) {
                    headers.put(parts[0], parts[1]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}