import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Project: Space Game
 * Purpose Details: Sending encrypted game object data via RabbitMQ.
 * Course: IST 242 Section 611 Inter App Dev
 * Author: Christopher Carlos
 * Date Developed: 06/18/24
 * Last Date Changed: 06/19/24
 * Revision: 1
 */

public class SendFlatfile {
    private final static String QUEUE_NAME = "game_queue";
    private final static int SHIFT_VALUE = 7; // Caesar Cipher shift value

    public static void main(String[] argv) throws Exception {
        // Read contents from flatfile.txt
        String serializedData = readFromFile("ships.txt");

        // Print plaintext and encrypted text
        System.out.println("Plaintext: " + serializedData);

        // Encrypt the serialized data using Caesar Cipher
        String encryptedData = caesarCipherEncrypt(serializedData.getBytes(), SHIFT_VALUE);

        // Print encrypted data
        System.out.println("Encrypted text: " + encryptedData);

        // Set up RabbitMQ connection and channel
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            channel.basicPublish("", QUEUE_NAME, null, encryptedData.getBytes());
            System.out.println(" [x] Sent encrypted ship data via RabbitMQ");
        }
    }

    private static String readFromFile(String fileName) throws IOException {
        StringBuilder fileContent = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                fileContent.append(line).append("\n");
            }
        }
        return fileContent.toString();
    }

    private static String caesarCipherEncrypt(byte[] data, int shift) {
        StringBuilder encryptedData = new StringBuilder();
        for (byte b : data) {
            char character = (char) b;
            if (Character.isLetter(character)) {
                char base = Character.isLowerCase(character) ? 'a' : 'A';
                int originalAlphabetPosition = character - base;
                int newAlphabetPosition = (originalAlphabetPosition + shift) % 26;
                char newCharacter = (char) (base + newAlphabetPosition);
                encryptedData.append(newCharacter);
            } else {
                encryptedData.append(character);
            }
        }
        return encryptedData.toString();
    }
}
