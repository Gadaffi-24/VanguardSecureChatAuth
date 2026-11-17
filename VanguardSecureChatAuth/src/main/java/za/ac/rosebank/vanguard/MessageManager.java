package za.ac.rosebank.vanguard;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class MessageManager {

    public static final String FLAG_SENT = "Sent";
    public static final String FLAG_STORED = "Stored";
    public static final String FLAG_DISREGARD = "Disregard";

    //Messages that the user disregarded
    private final List<Message> disregardedMessages = new ArrayList<>();
    //Stored messages, always filled by reading the JSON file
    private List<Message> storedMessages = new ArrayList<>();

    //Parallel arrays: index i holds the message, its ID and its hash
    private final List<Message> allMessages = new ArrayList<>();
    private final List<String> messageIDs = new ArrayList<>();
    private final List<String> messageHashes = new ArrayList<>();

    //Adds a message to the arrays that match its flag (Sent, Stored or Disregard) and returns the result text
    public String addMessage(Message message, String flag) {
        if (FLAG_SENT.equals(flag)) {
            registerMessage(message);
            return message.sentMessage(1);
        }
        if (FLAG_STORED.equals(flag)) {
            registerMessage(message);
            String result = message.sentMessage(3);
            loadStoredMessages();
            return result;
        }
        if (FLAG_DISREGARD.equals(flag)) {
            registerMessage(message);
            if (!disregardedMessages.contains(message)) {
                disregardedMessages.add(message);
            }
            return message.deleteMessage();
        }
        return "Invalid message flag. Please use Sent, Stored or Disregard.";
    }

    //Puts the message, its ID and its hash into the parallel arrays (a message is only added once)
    private void registerMessage(Message message) {
        if (!messageIDs.contains(message.getMessageID())) {
            allMessages.add(message);
            messageIDs.add(message.getMessageID());
            messageHashes.add(message.getMessageHash());
        }
    }

    //Reads the JSON file into the Stored Messages array; returns false if the file cannot be read
    public boolean loadStoredMessages() {
        try {
            storedMessages = MessageJsonStorage.read(Paths.get(Message.getStoragePath()));
        } catch (IOException e) {
            return false;
        }
        for (Message message : storedMessages) {
            registerMessage(message);
        }
        return true;
    }

    // ---------- Stored Messages menu features ----------
    //Sender and Recipient of every stored message
    public String displaySendersAndRecipients() {
        if (storedMessages.isEmpty()) {
            return "No stored messages found.";
        }
        StringBuilder output = new StringBuilder();
        for (Message message : storedMessages) {
            output.append("Sender: ").append(message.getSender())
                    .append(" | Recipient: ").append(message.getRecipient()).append("\n");
        }
        return output.toString().trim();
    }

    //Finds the stored message with the most characters (the first one wins a tie); null if none
    public Message findLongestStoredMessage() {
        Message longest = null;
        for (Message message : storedMessages) {
            if (longest == null || message.getMessageText().length() > longest.getMessageText().length()) {
                longest = message;
            }
        }
        return longest;
    }

    public String displayLongestStoredMessage() {
        Message longest = findLongestStoredMessage();
        if (longest == null) {
            return "No stored messages found.";
        }
        return "Longest stored message (" + longest.getMessageText().length() + " characters):\n"
                + longest.getMessageText();
    }

    //Looks the ID up in the ID array and uses the same index in the message array
    public Message findMessageByID(String messageID) {
        int index = messageIDs.indexOf(messageID);
        return index >= 0 ? allMessages.get(index) : null;
    }

    public String searchByMessageID(String messageID) {
        Message found = findMessageByID(messageID);
        if (found == null) {
            return "No message found with ID " + messageID + ".";
        }
        return "Recipient: " + found.getRecipient() + "\nMessage: " + found.getMessageText();
    }

    //Every sent or stored message for one recipient
    public List<Message> findMessagesByRecipient(String recipient) {
        List<Message> matches = new ArrayList<>();
        for (Message message : Message.getSentMessages()) {
            if (message.getRecipient().equals(recipient)) {
                matches.add(message);
            }
        }
        for (Message message : storedMessages) {
            if (message.getRecipient().equals(recipient)) {
                matches.add(message);
            }
        }
        return matches;
    }

    public String displayMessagesForRecipient(String recipient) {
        List<Message> matches = findMessagesByRecipient(recipient);
        if (matches.isEmpty()) {
            return "No sent or stored messages found for " + recipient + ".";
        }
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < matches.size(); i++) {
            output.append(i + 1).append(") ").append(matches.get(i).getMessageText()).append("\n");
        }
        return output.toString().trim();
    }

    //Finds the stored message with this hash, removes it from every array and from the JSON file
    public String deleteMessageByHash(String messageHash) {
        int index = indexOfStoredHash(messageHash);
        if (index < 0) {
            return "No stored message found with hash " + messageHash + ".";
        }
        String messageID = messageIDs.get(index);
        Message target = allMessages.get(index);

        List<Message> remaining = new ArrayList<>(storedMessages);
        remaining.removeIf(message -> message.getMessageID().equals(messageID));
        try {
            MessageJsonStorage.write(Paths.get(Message.getStoragePath()), remaining);
        } catch (IOException e) {
            return "Message could not be deleted because the storage file could not be updated.";
        }

        storedMessages = remaining;
        allMessages.remove(index);
        messageIDs.remove(index);
        messageHashes.remove(index);
        return "Message: \"" + target.getMessageText() + "\" successfully deleted.";
    }

    //Searches the hash array for a hash that belongs to a stored message (capitals do not matter)
    private int indexOfStoredHash(String messageHash) {
        for (int i = 0; i < messageHashes.size(); i++) {
            if (messageHashes.get(i).equalsIgnoreCase(messageHash.trim()) && isStored(messageIDs.get(i))) {
                return i;
            }
        }
        return -1;
    }

    private boolean isStored(String messageID) {
        for (Message message : storedMessages) {
            if (message.getMessageID().equals(messageID)) {
                return true;
            }
        }
        return false;
    }

    //Full details of every stored message
    public String displayStoredReport() {
        return buildReport("STORED MESSAGES REPORT", storedMessages, "No stored messages found.");
    }

    //Report of the messages sent while the program has been running (main menu option 2)
    public String displaySentReport() {
        return buildReport("SENT MESSAGES REPORT", Message.getSentMessages(), "No messages have been sent.");
    }

    private String buildReport(String title, List<Message> messages, String emptyText) {
        if (messages.isEmpty()) {
            return emptyText;
        }
        StringBuilder report = new StringBuilder("=== " + title + " ===\n");
        for (int i = 0; i < messages.size(); i++) {
            Message message = messages.get(i);
            report.append("\nMessage ").append(i + 1).append(" of ").append(messages.size()).append("\n")
                    .append("Message ID: ").append(message.getMessageID()).append("\n")
                    .append("Message Hash: ").append(message.getMessageHash()).append("\n")
                    .append("Sender: ").append(message.getSender()).append("\n")
                    .append("Recipient: ").append(message.getRecipient()).append("\n")
                    .append("Message: ").append(message.getMessageText()).append("\n");
        }
        return report.toString().trim();
    }

    //Array getters (copies, so the arrays cannot be changed from outside)
    public List<Message> getSentMessages() {
        return Message.getSentMessages();
    }

    public List<Message> getDisregardedMessages() {
        return new ArrayList<>(disregardedMessages);
    }

    public List<Message> getStoredMessages() {
        return new ArrayList<>(storedMessages);
    }

    public List<String> getMessageHashes() {
        return new ArrayList<>(messageHashes);
    }

    public List<String> getMessageIDs() {
        return new ArrayList<>(messageIDs);
    }
}
