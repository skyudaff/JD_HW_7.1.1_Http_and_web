import org.example.Request;
import org.junit.jupiter.api.Test;
import org.apache.http.NameValuePair;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class ServerTest {

    @Test
    public void testParseFormParams() {
        String method = "POST";
        String path = "/messages";
        String requestBody = "name=Vasya&age=10";
        InputStream bodyStream = new ByteArrayInputStream(requestBody.getBytes());

        BufferedReader reader = new BufferedReader(new StringReader(""));

        Request request = new Request(method, path, reader, bodyStream);

        List<NameValuePair> formParams = request.getFormParams();
        assertNotNull(formParams);

        Map<String, String> formParamsMap = formParams.stream()
                .collect(Collectors.toMap(NameValuePair::getName, NameValuePair::getValue));

        assertEquals("Vasya", formParamsMap.get("name"));
        assertEquals("10", formParamsMap.get("age"));
    }

    @Test
    public void testServerHandling() {
        org.example.Server server = new org.example.Server(9999, 1);
        server.addHandler("POST", "/message", (request, responseStream) -> {
            String paramValue = request.getFormParam("name");
            assertNotNull(paramValue);
            assertEquals("Vasya", paramValue);
        });
    }
}