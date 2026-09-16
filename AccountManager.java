package za.ac.rosebank.vanguard;

public class AccountManager {
    private String firstName;
    private String lastName;

    public AccountManager(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }
}