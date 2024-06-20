import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class HMAC_Serialization_Server {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/receive", new MyHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
    }

    static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                // Read request body
                InputStream is = exchange.getRequestBody();
                String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);

                // Parse JSON and HMAC from the body
                String[] parts = body.split("\n");
                String receivedShipJson = parts[0];
                String receivedHmac = parts[1];

                // Verify HMAC
                String key = "secret"; // Same secret key as GameA
                String response;
                try {
                    if (verifyHMAC(receivedShipJson, receivedHmac, key)) {
                        response = "HMAC verification successful.\n";

                        // Deserialize JSON to Ship object
                        ObjectMapper objectMapper = new ObjectMapper();
                        Ships_Flatfile_Creator.Ship ship = objectMapper.readValue(receivedShipJson, Ships_Flatfile_Creator.Ship.class);
                        response += "Ship object deserialized from JSON:\n";
                        response += "Name: " + ship.getName() + "\n";
                        response += "Health: " + ship.getHealth() + "\n";
                    } else {
                        response = "HMAC verification failed. Data integrity compromised.";
                    }
                } catch (Exception e) {
                    response = "Error processing request: " + e.getMessage();
                    e.printStackTrace();
                }

                // Send response
                exchange.sendResponseHeaders(200, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else {
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            }
        }

        public static boolean verifyHMAC(String data, String receivedHmac, String key)
                throws NoSuchAlgorithmException, InvalidKeyException {
            String algorithm = "HmacSHA256";
            Mac mac = Mac.getInstance(algorithm);
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), algorithm);
            mac.init(secretKeySpec);
            byte[] hmacBytes = mac.doFinal(data.getBytes());
            String calculatedHmac = Base64.getEncoder().encodeToString(hmacBytes);
            return calculatedHmac.equals(receivedHmac);
        }
    }
}
