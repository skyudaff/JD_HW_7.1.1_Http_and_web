package org.example;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private final int port;
    private final ExecutorService threadPool;
    private final Map<String, Map<String, Handler>> handlers = new ConcurrentHashMap<>();

    public Server(int port, int threadCount) {
        this.port = port;
        this.threadPool = Executors.newFixedThreadPool(threadCount);
    }

    public void addHandler(String method, String path, Handler handler) {
        handlers.computeIfAbsent(method, k -> new HashMap<>()).put(path, handler);
    }

    public void listen() {
        try (var serverSocket = new ServerSocket(port)) {
            System.out.println("Started on port: " + port);
            while (true) {
                var socket = serverSocket.accept();
                threadPool.submit(() -> handleConnection(socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleConnection(Socket socket) {
        try (var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             var out = new BufferedOutputStream(socket.getOutputStream())
        ) {
            String requestLine = in.readLine();
            if (requestLine != null) {
                String[] requestParts = requestLine.split(" ");
                if (requestParts.length == 3) {
                    String method = requestParts[0];
                    String path = requestParts[1];
                    Handler handler = findHandler(method, path);

                    if (handler != null) {
                        if (method.equals(Main.GET)) {
                            handleGetRequest(path, in, out, handler);
                        } else if (method.equals(Main.POST)) {
                            handlePostRequest(path, in, out, handler);
                        }
                    } else {
                        sendResponse(out, Main.NOT_FOUND, "Not Found", "404 Not Found");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleGetRequest(String path, BufferedReader in, BufferedOutputStream out, Handler handler) throws IOException {
        Request request = new Request(Main.GET, path, in);
        handler.handle(request, out);
    }

    private void handlePostRequest(String path, BufferedReader in, BufferedOutputStream out, Handler handler) throws IOException {
        int contentLength = getContentLength(in);
        if (contentLength > 0) {
            char[] requestBody = new char[contentLength];
            in.read(requestBody);
            String requestBodyString = new String(requestBody);
            Request request = new Request(Main.POST, path, new BufferedReader(new StringReader("")),
                    new ByteArrayInputStream(requestBodyString.getBytes()));
            handler.handle(request, out);
        } else {
            String responseBody = "No POST params found";
            sendResponse(out, Main.BAD_REQUEST, "Bad Request", responseBody);
        }
    }

    private int getContentLength(BufferedReader in) throws IOException {
        String contentLengthHeader = null;
        String line;
        while ((line = in.readLine()) != null && !line.isEmpty()) {
            if (line.startsWith("Content-Length: ")) {
                contentLengthHeader = line;
            }
        }
        if (contentLengthHeader != null) {
            return Integer.parseInt(contentLengthHeader.substring("Content-Length: ".length()));
        }
        return 0;
    }

    private Handler findHandler(String method, String path) {
        int index = path.indexOf('?');
        if (index != -1) {
            path = path.substring(0, index);
        }
        Map<String, Handler> methodHandlers = handlers.get(method);
        if (methodHandlers != null) {
            return methodHandlers.get(path);
        }
        return null;
    }

    void sendResponse(OutputStream output, int statusCode, String statusMessage, String body) throws IOException {
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(output));
        writer.println("HTTP/1.1 " + statusCode + " " + statusMessage);
        writer.println("Content-Length: " + body.length());
        writer.println();
        writer.print(body);
        writer.flush();
        writer.close();
    }
}