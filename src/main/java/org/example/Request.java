package org.example;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Request {
    private final String method;
    private final String path;
    private final Map<String, String> headers = new HashMap<>();
    private final InputStream bodyStream;
    private final List<NameValuePair> queryParams;
    private final List<NameValuePair> formParams;

    public Request(String method, String path, BufferedReader reader, InputStream bodyStream) {
        this.method = method;
        this.path = path;
        this.bodyStream = bodyStream;
        this.queryParams = parseQueryParams(path);
        this.formParams = parseFormParams(bodyStream);
        parseHeaders(reader);
    }

    public Request(String method, String path, BufferedReader reader) {
        this(method, path, reader, null);
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

    public List<NameValuePair> getQueryParams() {
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

    public String getFormParam(String name) {
        for (NameValuePair param : formParams) {
            if (param.getName().equals(name)) {
                return param.getValue();
            }
        }
        return null;
    }

    public List<NameValuePair> getFormParams() {
        return formParams;
    }

    private List<NameValuePair> parseQueryParams(String path) {
        int index = path.indexOf('?');
        if (index != -1 && index < path.length() - 1) {
            String queryString = path.substring(index + 1);
            return URLEncodedUtils.parse(queryString, StandardCharsets.UTF_8);
        }
        return Collections.emptyList();
    }

    private List<NameValuePair> parseFormParams(InputStream bodyStream) {
        List<NameValuePair> params = new ArrayList<>();
        if (bodyStream != null) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(bodyStream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] paramPairs = line.split("&");
                    for (String paramPair : paramPairs) {
                        String[] parts = paramPair.split("=");
                        if (parts.length == 2) {
                            String key = parts[0];
                            String value = parts[1];
                            params.add(new BasicNameValuePair(key, value));
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return params;
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