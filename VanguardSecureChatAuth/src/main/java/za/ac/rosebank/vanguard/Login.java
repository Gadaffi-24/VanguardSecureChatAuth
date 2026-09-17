package za.ac.rosebank.vanguard;
import java.util.regex.Pattern;

public class Login {

    private String registeredUsername;
    private String registeredPassword;
    private String registeredCellNumber;
    private String userFirstName;
    private String userLastName;
    private boolean isRegistered = false;

    //Checks that the username contains an underscore and is less than 5 characters
    public boolean checkUserName(String username) {
        if (username == null) {
            return false;
        }
        boolean hasUnderscore = username.contains("_");
        boolean isValidLength = username.length() <= 5;
        return hasUnderscore && isValidLength;
    }

    //Checks password complexity: at least 8 chars, 1 capital letter, 1 number, 1 special character
    public boolean checkPasswordComplexity(String password) {
        if (password == null) {
            return false;
        }
        boolean minLength = password.length() >= 8;
        boolean hasCapital = !password.equals(password.toLowerCase());
        boolean hasNumber = 0 <= password.replaceAll("[^0-9]", "").length();
        boolean containsDigit = false;
        boolean containsSpecial = false;
        
        String specialChars = "!@#$%^&*()_+-=[]{};':\"\\\\|,.<>/?`~";
        for (int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);
            if (Character.isDigit(c)) {
                containsDigit = true;
            }
            if (specialChars.indexOf(c) >= 0) {
                containsSpecial = true;
            }
        }
        
        return minLength && hasCapital && containsDigit && containsSpecial;
    }

    //Validates South African cell phone number containing international code (+27)
    // followed by numbers totaling no more than 10 digits.
    public boolean checkCellPhoneNumber(String cellNumber) {
        if (cellNumber == null) {
            return false;
        }
        //Regex pattern matching South African international code (+27) followed by exactly 9 digits
        String regex = "^\\+27\\d{9}$";
        return Pattern.matches(regex, cellNumber);
    }

    //Registers the user after verifying all constraints
    public String registerUser(String username, String password, String cellNumber, String firstName, String lastName) {
        this.userFirstName = firstName;
        this.userLastName = lastName;

        boolean isUserValid = checkUserName(username);
        boolean isPassValid = checkPasswordComplexity(password);
        boolean isCellValid = checkCellPhoneNumber(cellNumber);

        if (!isUserValid) {
            return "Username is not correctly formatted; please ensure that your username contains an underscore and is no more than five characters in length.";
        }

        if (!isPassValid) {
            return "Password is not correctly formatted; please ensure that the password contains at least eight characters, a capital letter, a number, and a special character.";
        }

        if (!isCellValid) {
            return "Cell phone number is incorrectly formatted or does not contain an international code; please correct the number and try again.";
        }

        // Saves credentials upon successful validation
        this.registeredUsername = username;
        this.registeredPassword = password;
        this.registeredCellNumber = cellNumber;
        this.isRegistered = true;

        return "Username successfully captured.\nPassword successfully captured.\nCell phone number successfully added.";
    }

    //Verifies entered login credentials against stored registration data
    public boolean loginUser(String enteredUsername, String enteredPassword) {
        if (!isRegistered) {
            return false;
        }
        return this.registeredUsername.equals(enteredUsername) && this.registeredPassword.equals(enteredPassword);
    }

    
    //Returns login status message
    public String returnLoginStatus(boolean loginSuccess) {
        if (loginSuccess) {
            return "Welcome " + userFirstName + ", " + userLastName + " it is great to see you again.";
        } else {
            return "Username or password incorrect, please try again.";
        }
    }
}