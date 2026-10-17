package za.ac.rosebank.vanguard;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Message {
    public static final int MAX_MESSAGE_LENGTH = 250;
    private static final int ID_LENGTH = 10;
    private static final String DEFAULT_STORAGE_PATH = "messages.json";

    //Shared across the program run so printMessages() and returnTotalMessages() see every sent message
    private static final List<Message> sentMessages = new ArrayList<>();
    private static String storagePath = DEFAULT_STORAGE_PATH;

    //Reuses the Part 1 cell phone validation instead of duplicating it
    private static final Login CELL_VALIDATOR = new Login();

    private final String messageID;
    private final int messageNumber;
    private final String recipient;
    private final String messageText;
    private final String messageHash;

    //Creates a message with an auto-generated ten-digit ID
    public Message(int messageNumber, String recipient, String messageText) {
        this(generateMessageID(), messageNumber, recipient, messageText);
    }

    //Creates a message with a supplied ID used by the unit tests so hashes can be predicted
    public Message(String messageID, int messageNumber, String recipient, String messageText) {
        this.messageID = messageID;
        this.messageNumber = messageNumber;
        this.recipient = recipient;
        this.messageText = messageText;
        this.messageHash = createMessageHash();
    }

    //Builds a random ten-digit ID one digit at a time and leading zeros are allowed
    public static String generateMessageID() {
        StringBuilder id = new StringBuilder();
        for (int i = 0; i < ID_LENGTH; i++) {
            id.append(ThreadLocalRandom.current().nextInt(10));
        }
        return id.toString();
    }

    //Ensures the message ID is made of digits and is not more than ten characters long
    public boolean checkMessageID() {
        return messageID != null
                && !messageID.isEmpty()
                && messageID.length() <= ID_LENGTH
                && messageID.matches("\\d+");
    }

    //Returns the ID confirmation text expected by the unit tests
    public String generatedIDMessage() {
        return "Message ID generated: " + messageID;
    }

    //Reuses checkCellPhoneNumber() from Login
    public static String checkRecipientCell(String cellNumber) {
        if (CELL_VALIDATOR.checkCellPhoneNumber(cellNumber)) {
            return "Cell phone number successfully captured.";
        }
        return "Cell phone number is incorrectly formatted or does not contain an international code. "
                + "Please correct the number and try again.";
    }

    //Checks the 250 character limit and reports by how many characters the message is too long
    public static String checkMessageLength(String text) {
        int excess = text.length() - MAX_MESSAGE_LENGTH;
        if (excess > 0) {
            return "Message exceeds " + MAX_MESSAGE_LENGTH + " characters by " + excess
                    + "; please reduce the size.";
        }
        return "Message ready to send.";
    }

    //Hash format: first two ID digits : message number : first word + last word, all in capitals
    public String createMessageHash() {
        String firstTwo = messageID.substring(0, Math.min(2, messageID.length()));
        String[] words = messageText.trim().split("\\s+");
        String firstWord = stripPunctuation(words[0]);
        String lastWord = stripPunctuation(words[words.length - 1]);
        return (firstTwo + ":" + messageNumber + ":" + firstWord + lastWord).toUpperCase();
    }

    //Removes punctuation such as "?" or "," so "tonight?" becomes "tonight"
    private static String stripPunctuation(String word) {
        return word.replaceAll("[^A-Za-z0-9]", "");
    }

    //Handles the user's choice: 1 = send, 2 = disregard, 3 = store to send later
    public String sentMessage(int choice) {
        switch (choice) {
            case 1:
                if (!sentMessages.contains(this)) {
                    sentMessages.add(this);
                }
                return "Message successfully sent.";
            case 2:
                return "Press 0 to delete the message.";
            case 3:
                if (storeMessage()) {
                    return "Message successfully stored.";
                }
                return "Message could not be stored.";
            default:
                return "Invalid option. Please choose 1, 2 or 3.";
        }
    }

    //Called once the user presses 0 to confirm that the message must be deleted
    public String deleteMessage() {
        return "Message deleted.";
    }

    //Full details in the required order: Message ID, Message Hash, Recipient and Message
    public String getDetails() {
        return "Message ID: " + messageID + "\n"
                + "Message Hash: " + messageHash + "\n"
                + "Recipient: " + recipient + "\n"
                + "Message: " + messageText;
    }

    //Returns the details of every message sent while the program has been running
    public static String printMessages() {
        if (sentMessages.isEmpty()) {
            return "No messages have been sent.";
        }
        StringBuilder output = new StringBuilder();
        for (Message message : sentMessages) {
            output.append(message.getDetails()).append("\n\n");
        }
        return output.toString().trim();
    }

    //Returns how many messages that have already been sent
    public static int returnTotalMessages() {
        return sentMessages.size();
    }

    //Adds this message to the JSON array in the storage file and creates the file if needed
    public boolean storeMessage() {
        Path path = Paths.get(storagePath);
        String entry = toJson();
        try {
            String content = "[\n" + entry + "\n]\n";
            if (Files.exists(path)) {
                String existing = Files.readString(path, StandardCharsets.UTF_8).trim();
                if (existing.startsWith("[") && existing.endsWith("]")) {
                    String body = existing.substring(1, existing.length() - 1).trim();
                    if (!body.isEmpty()) {
                        content = "[\n" + body + ",\n" + entry + "\n]\n";
                    }
                }
            }
            Files.writeString(path, content, StandardCharsets.UTF_8);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    //Formats this message as one JSON object
    private String toJson() {
        return "  {\n"
                + "    \"messageID\": \"" + escapeJson(messageID) + "\",\n"
                + "    \"messageNumber\": " + messageNumber + ",\n"
                + "    \"messageHash\": \"" + escapeJson(messageHash) + "\",\n"
                + "    \"recipient\": \"" + escapeJson(recipient) + "\",\n"
                + "    \"message\": \"" + escapeJson(messageText) + "\"\n"
                + "  }";
    }

    //Escapes quotes backslashes and control characters as required for JSON strings (Bray, 2017)
    private static String escapeJson(String text) {
        StringBuilder escaped = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '"':
                    escaped.append("\\\"");
                    break;
                case '\\':
                    escaped.append("\\\\");
                    break;
                case '\n':
                    escaped.append("\\n");
                    break;
                case '\r':
                    escaped.append("\\r");
                    break;
                case '\t':
                    escaped.append("\\t");
                    break;
                default:
                    if (c < 0x20) {
                        escaped.append(String.format("\\u%04x", (int) c));
                    } else {
                        escaped.append(c);
                    }
            }
        }
        return escaped.toString();
    }

    //Allows the unit tests to write to a temporary file instead of the real messages.json
    public static void setStoragePath(String path) {
        storagePath = path;
    }
    public static void resetStoragePath() {
        storagePath = DEFAULT_STORAGE_PATH;
    }

    //Empties the list of sent messages
    public static void clearSentMessages() {
        sentMessages.clear();
    }
    public String getMessageID() {
        return messageID;
    }
    public int getMessageNumber() {
        return messageNumber;
    }
    public String getRecipient() {
        return recipient;
    }
    public String getMessageText() {
        return messageText;
    }
    public String getMessageHash() {
        return messageHash;
    }
}