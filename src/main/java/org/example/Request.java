package org.example;

import java.io.BufferedReader;
import java.io.InputStream;
import java.util.*;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

public class Request {
    private final String method;
    private final String path;
    private final Map<String, String> headers = new HashMap<>();
    private final InputStream bodyStream;
    private final List<NameValuePair> queryParams;

    public Request(String method, String path, BufferedReader reader, InputStream bodyStream) {
        this.method = method;
        this.path = path;
        this.bodyStream = bodyStream;
        this.queryParams = parseQueryParams(path);
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

    private List<NameValuePair> getQueryParams() {
        return queryParams;
    }

    public String getQueryParam(String name) {
        for (NameValuePair param : queryParams) {
            if (param.getName().equals(name)) {
                return param.getValue();
            }
        }
        return null;
    }

    private List<NameValuePair> parseQueryParams(String path) {
        int index = path.indexOf('?');
        if (index != -1 && index < path.length() - 1) {
            String queryString = path.substring(index + 1);
            return URLEncodedUtils.parse(queryString, java.nio.charset.StandardCharsets.UTF_8);
        }
        return Collections.emptyList();
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