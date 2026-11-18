package za.ac.rosebank.vanguard;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

//Unit test suite for the MessageManager class (Part 3) using the PoE Part 3 test data
public class MessageManagerTest {

    private static final String TEXT_1 = "Did you get the cake?";
    private static final String TEXT_2 = "Where are you? You are late! I have asked you to be on time.";
    private static final String TEXT_3 = "Yohoooo, I am at your gate.";
    private static final String TEXT_4 = "It is dinner time!";
    private static final String TEXT_5 = "Ok, I am leaving without you.";

    private Path tempDir;
    private Path storageFile;
    private MessageManager manager;
    private Message message1;
    private Message message2;
    private Message message3;
    private Message message4;
    private Message message5;

    @BeforeEach
    public void setUp() throws IOException {
        Message.clearSentMessages();
        tempDir = Files.createTempDirectory("quickchat-part3-test");
        storageFile = tempDir.resolve("messages.json");
        Message.setStoragePath(storageFile.toString());
        manager = new MessageManager();

        // Test data messages 1 to 5 (fixed IDs so hashes can be predicted; message 4 uses the developer ID 0838884567)
        // The PoE gives no recipient for message 4, so a number that is not used by the other messages is used.
        message1 = new Message("1134567890", 0, "+27834557896", TEXT_1);
        message2 = new Message("2234567890", 1, "+27838884567", TEXT_2);
        message3 = new Message("3334567890", 2, "+27834484567", TEXT_3);
        message4 = new Message("0838884567", 3, "+27831234567", TEXT_4);
        message5 = new Message("5534567890", 4, "+27838884567", TEXT_5);
        for (Message message : new Message[]{message1, message2, message3, message4, message5}) {
            message.setSender("Kyle Smith");
        }
    }

    @AfterEach
    public void tearDown() throws IOException {
        Message.resetStoragePath();
        Message.clearSentMessages();
        Files.deleteIfExists(storageFile);
        Files.deleteIfExists(tempDir);
    }

    //Adds the five test messages with their flags: Sent, Stored, Disregard, Sent, Stored
    private void loadTestData() {
        manager.addMessage(message1, MessageManager.FLAG_SENT);
        manager.addMessage(message2, MessageManager.FLAG_STORED);
        manager.addMessage(message3, MessageManager.FLAG_DISREGARD);
        manager.addMessage(message4, MessageManager.FLAG_SENT);
        manager.addMessage(message5, MessageManager.FLAG_STORED);
    }

    private List<String> texts(List<Message> messages) {
        List<String> texts = new ArrayList<>();
        for (Message message : messages) {
            texts.add(message.getMessageText());
        }
        return texts;
    }

    // ---------- Arrays correctly populated ----------
    @Test
    public void testSentMessagesArrayPopulated() {
        loadTestData();
        assertEquals(List.of("Did you get the cake?", "It is dinner time!"), texts(manager.getSentMessages()));
    }

    @Test
    public void testDisregardedMessagesArrayPopulated() {
        loadTestData();
        assertEquals(List.of("Yohoooo, I am at your gate."), texts(manager.getDisregardedMessages()));
    }

    @Test
    public void testStoredMessagesArrayReadFromJsonFile() {
        loadTestData();
        assertEquals(List.of(TEXT_2, TEXT_5), texts(manager.getStoredMessages()));
    }

    @Test
    public void testMessageHashArrayPopulated() {
        loadTestData();
        assertEquals(List.of("11:0:DIDCAKE", "22:1:WHERETIME", "33:2:YOHOOOOGATE", "08:3:ITTIME", "55:4:OKYOU"),
                manager.getMessageHashes());
    }

    @Test
    public void testMessageIDArrayPopulated() {
        loadTestData();
        assertEquals(List.of("1134567890", "2234567890", "3334567890", "0838884567", "5534567890"),
                manager.getMessageIDs());
    }

    @Test
    public void testAddMessageReturnsResultText() {
        assertEquals("Message successfully sent.", manager.addMessage(message1, MessageManager.FLAG_SENT));
        assertEquals("Message successfully stored.", manager.addMessage(message2, MessageManager.FLAG_STORED));
        assertEquals("Message deleted.", manager.addMessage(message3, MessageManager.FLAG_DISREGARD));
    }

    @Test
    public void testInvalidFlagAddsNothing() {
        assertEquals("Invalid message flag. Please use Sent, Stored or Disregard.",
                manager.addMessage(message1, "Archive"));
        assertTrue(manager.getMessageIDs().isEmpty());
        assertTrue(manager.getSentMessages().isEmpty());
    }

    @Test
    public void testSameMessageIsOnlyRegisteredOnce() {
        manager.addMessage(message2, MessageManager.FLAG_STORED);
        manager.loadStoredMessages();
        assertEquals(1, manager.getMessageIDs().size());
        assertEquals(1, manager.getMessageHashes().size());
    }

    // ---------- Sender and recipient of stored messages ----------
    @Test
    public void testDisplaySendersAndRecipients() {
        loadTestData();
        assertEquals("Sender: Kyle Smith | Recipient: +27838884567\n"
                + "Sender: Kyle Smith | Recipient: +27838884567", manager.displaySendersAndRecipients());
    }

    @Test
    public void testDisplaySendersAndRecipientsWhenNothingStored() {
        assertEquals("No stored messages found.", manager.displaySendersAndRecipients());
    }

    // ---------- Longest message ----------
    @Test
    public void testLongestStoredMessage() {
        loadTestData();
        assertEquals(TEXT_2, manager.findLongestStoredMessage().getMessageText());
    }

    @Test
    public void testDisplayLongestStoredMessage() {
        loadTestData();
        assertEquals("Longest stored message (60 characters):\n" + TEXT_2, manager.displayLongestStoredMessage());
    }

    @Test
    public void testLongestStoredMessageWhenNothingStored() {
        assertNull(manager.findLongestStoredMessage());
        assertEquals("No stored messages found.", manager.displayLongestStoredMessage());
    }

    // ---------- Search for a message ID ----------
    @Test
    public void testSearchMessageIDFindsMessage4() {
        loadTestData();
        assertEquals("It is dinner time!", manager.findMessageByID("0838884567").getMessageText());
        assertEquals("Recipient: +27831234567\nMessage: It is dinner time!", manager.searchByMessageID("0838884567"));
    }

    @Test
    public void testSearchMessageIDNotFound() {
        loadTestData();
        assertNull(manager.findMessageByID("9999999999"));
        assertEquals("No message found with ID 9999999999.", manager.searchByMessageID("9999999999"));
    }

    // ---------- Search for a recipient ----------
    @Test
    public void testSearchByRecipient() {
        loadTestData();
        assertEquals(List.of(TEXT_2, TEXT_5), texts(manager.findMessagesByRecipient("+27838884567")));
    }

    @Test
    public void testSearchByRecipientFindsSentMessages() {
        loadTestData();
        assertEquals(List.of(TEXT_1), texts(manager.findMessagesByRecipient("+27834557896")));
    }

    @Test
    public void testSearchByRecipientIgnoresDisregardedMessages() {
        loadTestData();
        assertTrue(manager.findMessagesByRecipient("+27834484567").isEmpty());
    }

    @Test
    public void testDisplayMessagesForRecipient() {
        loadTestData();
        assertEquals("1) " + TEXT_2 + "\n2) " + TEXT_5, manager.displayMessagesForRecipient("+27838884567"));
        assertEquals("No sent or stored messages found for +27000000000.",
                manager.displayMessagesForRecipient("+27000000000"));
    }

    // ---------- Delete using the message hash ----------
    @Test
    public void testDeleteMessageByHash() {
        loadTestData();
        assertEquals("Message: \"Where are you? You are late! I have asked you to be on time.\" successfully deleted.",
                manager.deleteMessageByHash(message2.getMessageHash()));
    }

    @Test
    public void testDeleteRemovesMessageFromArraysAndFile() throws IOException {
        loadTestData();
        manager.deleteMessageByHash("22:1:WHERETIME");
        assertEquals(List.of(TEXT_5), texts(manager.getStoredMessages()));
        assertFalse(manager.getMessageHashes().contains("22:1:WHERETIME"));
        assertFalse(manager.getMessageIDs().contains("2234567890"));
        assertNull(manager.findMessageByID("2234567890"));
        String json = Files.readString(storageFile, StandardCharsets.UTF_8);
        assertFalse(json.contains("2234567890"));
        assertTrue(json.contains("5534567890"));
    }

    @Test
    public void testDeleteIgnoresCapitalLetters() {
        loadTestData();
        assertTrue(manager.deleteMessageByHash("22:1:wheretime").endsWith("successfully deleted."));
    }

    @Test
    public void testDeleteUnknownHash() {
        loadTestData();
        assertEquals("No stored message found with hash 99:9:NOPE.", manager.deleteMessageByHash("99:9:NOPE"));
        assertEquals(2, manager.getStoredMessages().size());
        assertEquals(5, manager.getMessageHashes().size());
    }

    @Test
    public void testDeleteOnlyWorksForStoredMessages() {
        loadTestData();
        assertEquals("No stored message found with hash 11:0:DIDCAKE.",
                manager.deleteMessageByHash(message1.getMessageHash()));
        assertEquals(2, manager.getSentMessages().size());
    }

    @Test
    public void testDeleteLastStoredMessageLeavesEmptyFile() throws IOException {
        manager.addMessage(message2, MessageManager.FLAG_STORED);
        manager.deleteMessageByHash(message2.getMessageHash());
        assertTrue(manager.getStoredMessages().isEmpty());
        assertEquals("[]", Files.readString(storageFile, StandardCharsets.UTF_8).trim());
    }

    // ---------- Reports ----------
    @Test
    public void testDisplayStoredReport() {
        loadTestData();
        String expected = "=== STORED MESSAGES REPORT ===\n"
                + "\nMessage 1 of 2\n"
                + "Message ID: 2234567890\n"
                + "Message Hash: 22:1:WHERETIME\n"
                + "Sender: Kyle Smith\n"
                + "Recipient: +27838884567\n"
                + "Message: " + TEXT_2 + "\n"
                + "\nMessage 2 of 2\n"
                + "Message ID: 5534567890\n"
                + "Message Hash: 55:4:OKYOU\n"
                + "Sender: Kyle Smith\n"
                + "Recipient: +27838884567\n"
                + "Message: " + TEXT_5;
        assertEquals(expected, manager.displayStoredReport());
    }

    @Test
    public void testDisplaySentReportShowsHashRecipientAndMessage() {
        loadTestData();
        String report = manager.displaySentReport();
        assertTrue(report.contains("Message Hash: 11:0:DIDCAKE"));
        assertTrue(report.contains("Recipient: +27834557896"));
        assertTrue(report.contains("Message: " + TEXT_1));
        assertTrue(report.contains("Message Hash: 08:3:ITTIME"));
        assertFalse(report.contains("Where are you?"));
        assertFalse(report.contains("Yohoooo"));
    }

    @Test
    public void testReportsWhenNothingToShow() {
        assertEquals("No stored messages found.", manager.displayStoredReport());
        assertEquals("No messages have been sent.", manager.displaySentReport());
    }

    // ---------- Reading the JSON file ----------
    @Test
    public void testLoadStoredMessagesWithNoFile() {
        assertTrue(manager.loadStoredMessages());
        assertTrue(manager.getStoredMessages().isEmpty());
    }

    @Test
    public void testLoadStoredMessagesKeepsSenderAndNumber() {
        manager.addMessage(message5, MessageManager.FLAG_STORED);
        Message loaded = manager.getStoredMessages().get(0);
        assertEquals("Kyle Smith", loaded.getSender());
        assertEquals(4, loaded.getMessageNumber());
        assertEquals("55:4:OKYOU", loaded.getMessageHash());
    }

    @Test
    public void testLoadStoredMessagesFromPreviousRun() throws IOException {
        message2.storeMessage();   // stored by an earlier run of the program
        MessageManager newRun = new MessageManager();
        assertTrue(newRun.loadStoredMessages());
        assertEquals(List.of(TEXT_2), texts(newRun.getStoredMessages()));
        assertEquals("Recipient: +27838884567\nMessage: " + TEXT_2, newRun.searchByMessageID("2234567890"));
    }

    @Test
    public void testLoadStoredMessagesWithBrokenFileKeepsArray() throws IOException {
        manager.addMessage(message2, MessageManager.FLAG_STORED);
        Files.writeString(storageFile, "[ {\"messageID\": ", StandardCharsets.UTF_8);
        assertFalse(manager.loadStoredMessages());
        assertEquals(1, manager.getStoredMessages().size());
        assertNotNull(manager.findMessageByID("2234567890"));
    }
}