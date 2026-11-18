package za.ac.rosebank.vanguard;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

//Unit test suite for reading and writing the messages.json file (Part 3)
public class MessageJsonStorageTest {

    private Path tempDir;
    private Path file;

    @BeforeEach
    public void setUp() throws IOException {
        tempDir = Files.createTempDirectory("quickchat-json-test");
        file = tempDir.resolve("messages.json");
    }

    @AfterEach
    public void tearDown() throws IOException {
        Files.deleteIfExists(file);
        Files.deleteIfExists(tempDir);
    }

    @Test
    public void testReadMissingFileGivesEmptyArray() throws IOException {
        assertTrue(MessageJsonStorage.read(file).isEmpty());
    }

    @Test
    public void testReadEmptyFileGivesEmptyArray() throws IOException {
        Files.writeString(file, "  \n", StandardCharsets.UTF_8);
        assertTrue(MessageJsonStorage.read(file).isEmpty());
    }

    @Test
    public void testWriteThenReadRoundTrip() throws IOException {
        Message first = new Message("1134567890", 0, "+27834557896", "Did you get the cake?");
        first.setSender("Kyle Smith");
        Message second = new Message("0838884567", 3, "+27831234567", "It is dinner time!");
        MessageJsonStorage.write(file, List.of(first, second));

        List<Message> messages = MessageJsonStorage.read(file);
        assertEquals(2, messages.size());
        assertEquals("1134567890", messages.get(0).getMessageID());
        assertEquals(0, messages.get(0).getMessageNumber());
        assertEquals("Kyle Smith", messages.get(0).getSender());
        assertEquals("+27834557896", messages.get(0).getRecipient());
        assertEquals("Did you get the cake?", messages.get(0).getMessageText());
        assertEquals("08:3:ITTIME", messages.get(1).getMessageHash());
        assertEquals("Unknown", messages.get(1).getSender());
    }

    @Test
    public void testSpecialCharactersSurviveRoundTrip() throws IOException {
        String text = "He said \"hi\"\\ today\tand\nleft \u00e9";
        MessageJsonStorage.write(file, List.of(new Message("0212345678", 2, "+27718693002", text)));
        assertEquals(text, MessageJsonStorage.read(file).get(0).getMessageText());
    }

    @Test
    public void testWriteEmptyArray() throws IOException {
        MessageJsonStorage.write(file, List.of());
        assertEquals("[]", Files.readString(file, StandardCharsets.UTF_8).trim());
        assertTrue(MessageJsonStorage.read(file).isEmpty());
    }

    @Test
    public void testReadFileWrittenByHand() throws IOException {
        Files.writeString(file, "[{\"messageID\":\"1234567890\",\"messageNumber\":7,\"recipient\":\"+27718693002\","
                + "\"message\":\"Hi \\u0041\\/B\"}]", StandardCharsets.UTF_8);
        Message message = MessageJsonStorage.read(file).get(0);
        assertEquals(7, message.getMessageNumber());
        assertEquals("Hi A/B", message.getMessageText());
    }

    @Test
    public void testBrokenJsonIsRejected() throws IOException {
        String[] brokenFiles = {
            "{\"messageID\": \"1\"}",                       // not an array
            "[{\"messageID\": \"1\"",                       // never closed
            "[{\"messageID\": \"unfinished}]",              // unterminated string
            "[{\"messageID\": \"1\"}] extra",               // content after the array
            "[{\"messageID\": \"\\q\"}]",                   // bad escape
            "[{\"messageID\": \"\\u12\"}]",                 // short unicode escape
            "[{\"messageNumber\": abc}]",                   // number that is not a number
            "[{\"messageID\": }]"                           // missing value
        };
        for (String broken : brokenFiles) {
            Files.writeString(file, broken, StandardCharsets.UTF_8);
            assertThrows(IOException.class, () -> MessageJsonStorage.read(file), broken);
        }
    }
}