import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class HMAC_Serialization_WebServiceCaller {
    public static void main(String[] args) {
        String filePath = "C:\\Users\\chris\\IdeaProjects\\lab4Carlos\\ships.txt"; // Update the file path

        // Read ship data from file
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                processShipData(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void processShipData(String line) {
        // Split the line to extract ship name and health
        String[] parts = line.split("\\s+");
        if (parts.length == 2) {
            String name = parts[0];
            int health = Integer.parseInt(parts[1]);

            // Create a Ship object
            Ships_Flatfile_Creator.Ship ship = new Ships_Flatfile_Creator.Ship(name, health);

            // Process the ship object
            processShip(ship);
        } else {
            System.out.println("Invalid ship data: " + line);
        }
    }

    private static void processShip(Ships_Flatfile_Creator.Ship ship) {
        // Serialize ship object to JSON
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String shipJson = objectMapper.writeValueAsString(ship);
            System.out.println("Ship object serialized to JSON:");
            System.out.println(shipJson);

            // Calculate HMAC for the JSON string
            String key = "secret"; // Replace with your secret key
            String hmac = calculateHMAC(shipJson, key);
            System.out.println("HMAC: " + hmac);

            // Send JSON and HMAC to the server
            sendToServer(shipJson, hmac);
        } catch (JsonProcessingException | NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
        }
    }

    public static void sendToServer(String json, String hmac) {
        try {
            // Specify the URL of the web service
            String url = "http://localhost:8000/receive";

            // Create a URL object
            URL obj = new URL(url);

            // Open a connection to the URL
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            // Set the request method
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "text/plain; charset=UTF-8");

            // Send POST data
            con.setDoOutput(true);
            OutputStream os = con.getOutputStream();
            os.write((json + "\n" + hmac).getBytes(StandardCharsets.UTF_8));
            os.flush();
            os.close();

            // Get the response code
            int responseCode = con.getResponseCode();
            System.out.println("Response Code : " + responseCode);

            // Read the response from the web service
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // Print the response
            System.out.println("Response: " + response.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String calculateHMAC(String data, String key)
            throws NoSuchAlgorithmException, InvalidKeyException {
        String algorithm = "HmacSHA256";
        Mac mac = Mac.getInstance(algorithm);
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), algorithm);
        mac.init(secretKeySpec);
        byte[] hmacBytes = mac.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(hmacBytes);
    }
}
