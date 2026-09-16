package za.ac.rosebank.vanguard;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LoginTest {

    private Login login;

    @BeforeEach
    public void setUp() {
        login = new Login();
        login.registerUser("kyl_1", "Ch&&sec@ke99!", "+27838968976", "Kyle", "Smith");
    }

    @Test
    public void testUsernameCorrectlyFormatted() {
        boolean result = login.checkUserName("kyl_1");
        assertTrue(result, "Username should be recognized as valid.");
    }

    @Test
    public void testUsernameIncorrectlyFormatted() {
        boolean result = login.checkUserName("kyle!!!!!!");
        assertFalse(result, "Username should be recognized as invalid.");
    }

    @Test
    public void testPasswordComplexitySuccess() {
        boolean result = login.checkPasswordComplexity("Ch&&sec@ke99!");
        assertTrue(result, "Password should meet complexity requirements.");
    }

    @Test
    public void testPasswordComplexityFailure() {
        boolean result = login.checkPasswordComplexity("password");
        assertFalse(result, "Password should fail complexity requirements.");
    }

    @Test
    public void testCellPhoneNumberCorrectlyFormatted() {
        boolean result = login.checkCellPhoneNumber("+27838968976");
        assertTrue(result, "Cell phone number format should be valid.");
    }

    @Test
    public void testCellPhoneNumberIncorrectlyFormatted() {
        boolean result = login.checkCellPhoneNumber("08966553");
        assertFalse(result, "Cell phone number without international code should fail.");
    }

    @Test
    public void testLoginSuccessfulMatch() {
        boolean success = login.loginUser("kyl_1", "Ch&&sec@ke99!");
        assertTrue(success, "Login should succeed with correct credentials.");
    }

    @Test
    public void testLoginFailedMatch() {
        boolean success = login.loginUser("kyl_1", "WrongPassword1!");
        assertFalse(success, "Login should fail with incorrect credentials.");
    }

    @Test
    public void testReturnLoginStatusSuccess() {
        boolean success = login.loginUser("kyl_1", "Ch&&sec@ke99!");
        String status = login.returnLoginStatus(success);
        assertEquals("Welcome Kyle, Smith it is great to see you again.", status);
    }

    @Test
    public void testReturnLoginStatusFailure() {
        boolean success = login.loginUser("kyl_1", "WrongPassword1!");
        String status = login.returnLoginStatus(success);
        assertEquals("Username or password incorrect, please try again.", status);
    }
}