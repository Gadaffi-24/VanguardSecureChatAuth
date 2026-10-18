package za.ac.rosebank.vanguard;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

//Unit test suite for the Message class (Part 2) using the PoE test data
public class MessageTest {

    private static final String TEXT_1 = "Hi Mike, can you join us for dinner tonight?";
    private static final String TEXT_2 = "Hi Keegan, did you receive the payment?";

    private Path tempDir;
    private Path storageFile;
    private Message message1;
    private Message message2;

    @BeforeEach
    public void setUp() throws IOException {
        Message.clearSentMessages();
        tempDir = Files.createTempDirectory("quickchat-test");
        storageFile = tempDir.resolve("messages.json");
        Message.setStoragePath(storageFile.toString());

        // Test data for Message 1 and Message 2 (fixed IDs so the hashes can be predicted)
        message1 = new Message("0012345678", 0, "+27718693002", TEXT_1);
        message2 = new Message("0112345678", 1, "08575975889", TEXT_2);
    }

    @AfterEach
    public void tearDown() throws IOException {
        Message.resetStoragePath();
        Message.clearSentMessages();
        Files.deleteIfExists(storageFile);
        Files.deleteIfExists(tempDir);
    }

    // ---------- Message length ----------
    @Test
    public void testMessageLengthSuccess() {
        assertEquals("Message ready to send.", Message.checkMessageLength(TEXT_1));
    }

    @Test
    public void testMessageLengthExactly250IsAccepted() {
        assertEquals("Message ready to send.", Message.checkMessageLength("a".repeat(250)));
    }

    @Test
    public void testMessageLengthFailure() {
        assertEquals("Message exceeds 250 characters by 10; please reduce the size.",
                Message.checkMessageLength("a".repeat(260)));
    }

    // ---------- Recipient number ----------
    @Test
    public void testRecipientCellSuccess() {
        assertEquals("Cell phone number successfully captured.",
                Message.checkRecipientCell("+27718693002"));
    }

    @Test
    public void testRecipientCellFailure() {
        assertEquals("Cell phone number is incorrectly formatted or does not contain an international code. "
                + "Please correct the number and try again.",
                Message.checkRecipientCell("08575975889"));
    }

    // ---------- Message hash ----------
    @Test
    public void testMessageHashTestCase1() {
        assertEquals("00:0:HITONIGHT", message1.createMessageHash());
    }

    @Test
    public void testMessageHashesInLoop() {
        String[] ids = {"0012345678", "0112345678", "9987654321"};
        String[] texts = {TEXT_1, TEXT_2, "Where are you? You are late!"};
        String[] expected = {"00:0:HITONIGHT", "01:1:HIPAYMENT", "99:2:WHERELATE"};

        for (int i = 0; i < expected.length; i++) {
            Message message = new Message(ids[i], i, "+27718693002", texts[i]);
            assertEquals(expected[i], message.createMessageHash());
        }
    }

    // ---------- Message ID ----------
    @Test
    public void testMessageIDCreated() {
        assertTrue(message1.checkMessageID(), "ID should be no more than ten characters.");
        assertEquals("Message ID generated: 0012345678", message1.generatedIDMessage());
    }

    @Test
    public void testGeneratedMessageIDsAreTenDigits() {
        for (int i = 0; i < 100; i++) {
            String id = Message.generateMessageID();
            assertEquals(10, id.length());
            assertTrue(id.matches("\\d{10}"));
        }
    }

    @Test
    public void testMessageIDTooLongFails() {
        Message longId = new Message("12345678901", 0, "+27718693002", TEXT_1);
        assertFalse(longId.checkMessageID());
    }

    // ---------- Send / disregard / store ----------
    @Test
    public void testSentMessageSend() {
        assertEquals("Message successfully sent.", message1.sentMessage(1));
    }

    @Test
    public void testSentMessageDisregard() {
        assertEquals("Press 0 to delete the message.", message2.sentMessage(2));
    }

    @Test
    public void testSentMessageStore() {
        assertEquals("Message successfully stored.", message1.sentMessage(3));
    }

    @Test
    public void testSentMessageInvalidOption() {
        assertEquals("Invalid option. Please choose 1, 2 or 3.", message1.sentMessage(9));
    }

    // ---------- Totals and printing ----------
    @Test
    public void testReturnTotalMessages() {
        message1.sentMessage(1);   // Test Data 1: Send
        message2.sentMessage(2);   // Test Data 2: Discard
        assertEquals(1, Message.returnTotalMessages());
    }

    @Test
    public void testSendingSameMessageTwiceCountsOnce() {
        message1.sentMessage(1);
        message1.sentMessage(1);
        assertEquals(1, Message.returnTotalMessages());
    }

    @Test
    public void testPrintMessagesOnlyShowsSentMessages() {
        message1.sentMessage(1);
        message2.sentMessage(2);
        String output = Message.printMessages();
        assertTrue(output.contains("Message ID: 0012345678"));
        assertTrue(output.contains("Message Hash: 00:0:HITONIGHT"));
        assertTrue(output.contains("Recipient: +27718693002"));
        assertTrue(output.contains("Message: " + TEXT_1));
        assertFalse(output.contains("Keegan"));
    }

    @Test
    public void testPrintMessagesWhenNothingSent() {
        assertEquals("No messages have been sent.", Message.printMessages());
    }

    @Test
    public void testMessageDetailsOrder() {
        String expected = "Message ID: 0012345678\n"
                + "Message Hash: 00:0:HITONIGHT\n"
                + "Recipient: +27718693002\n"
                + "Message: " + TEXT_1;
        assertEquals(expected, message1.getDetails());
    }

    @Test
    public void testDeleteMessage() {
        assertEquals("Message deleted.", message2.deleteMessage());
    }

    // ---------- JSON storage ----------
    @Test
    public void testStoreMessageWritesJsonFile() throws IOException {
        assertTrue(message1.storeMessage());
        String json = Files.readString(storageFile, StandardCharsets.UTF_8);
        assertTrue(json.startsWith("["));
        assertTrue(json.contains("\"messageID\": \"0012345678\""));
        assertTrue(json.contains("\"messageHash\": \"00:0:HITONIGHT\""));
        assertTrue(json.contains("\"recipient\": \"+27718693002\""));
    }

    @Test
    public void testStoreMessageAppendsToExistingFile() throws IOException {
        message1.storeMessage();
        message2.storeMessage();
        String json = Files.readString(storageFile, StandardCharsets.UTF_8);
        assertTrue(json.contains("0012345678"));
        assertTrue(json.contains("0112345678"));
        assertEquals(1, json.chars().filter(c -> c == '[').count());
    }

    @Test
    public void testStoreMessageEscapesQuotes() throws IOException {
        Message quoted = new Message("0212345678", 2, "+27718693002", "He said \"hi\" today");
        quoted.storeMessage();
        String json = Files.readString(storageFile, StandardCharsets.UTF_8);
        assertTrue(json.contains("He said \\\"hi\\\" today"));
    }
}