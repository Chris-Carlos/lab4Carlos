import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;


/**
 * Project: Space Game
 * Purpose Details: Receiving encrypted game object data via RabbitMQ.
 * Course: IST 242 Section 611 Inter App Dev
 * Author: Christopher Carlos
 * Date Developed: 06/18/24
 * Last Date Changed: 06/19/24
 * Revision: 1
 */

public class ReceiveFlatfile {
    private final static String QUEUE_NAME = "game_queue";
    private final static int SHIFT_VALUE = 7; // Caesar Cipher shift value

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        System.out.println(" [*] Waiting for data...Press CTRL+C to cancel");

        DeliverCallback deliverCallback = (_, delivery) -> {
            String encryptedData = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [*] Received data from RabbitMQ...");

            // Decrypt the received encrypted data
            String decryptedData = caesarCipherDecrypt(encryptedData);

            // Print the received encrypted and decrypted data
            System.out.println("\nEncrypted Text: " + encryptedData);
            System.out.println("Decrypted Text: " + decryptedData);
        };

        channel.basicConsume(QUEUE_NAME, true, deliverCallback, _ -> {
        });
    }

    private static String caesarCipherDecrypt(String encryptedData) {
        StringBuilder decryptedText = new StringBuilder();

        for (char character : encryptedData.toCharArray()) {
            if (Character.isLetter(character)) {
                char base = Character.isLowerCase(character) ? 'a' : 'A';
                int originalAlphabetPosition = character - base;
                int newAlphabetPosition = (originalAlphabetPosition - ReceiveFlatfile.SHIFT_VALUE + 26) % 26; // Ensure positive result
                char newCharacter = (char) (base + newAlphabetPosition);
                decryptedText.append(newCharacter);
            } else {
                decryptedText.append(character);
            }
        }

        return decryptedText.toString();
    }
}
