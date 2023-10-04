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
            System.out.println("Сервер запущен. Порт " + port);
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
             var out = new BufferedOutputStream(socket.getOutputStream());
             var bodyStream = socket.getInputStream()
        ) {
            String requestLine = in.readLine();
            if (requestLine != null) {
                String[] requestParts = requestLine.split(" ");
                if (requestParts.length == 3) {
                    String method = requestParts[0];
                    String path = requestParts[1];
                    Handler handler = findHandler(method, path);
                    if (handler != null) {
                        Request request = new Request(method, path, in, bodyStream);
                        handler.handle(request, out);
                    } else {
                        sendResponse(out, 404, "Not Found", "404 Not Found");
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

    private Handler findHandler(String method, String path) {
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
    }
}