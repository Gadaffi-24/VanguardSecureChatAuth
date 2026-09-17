package za.ac.rosebank.vanguard;
import java.util.Scanner;

public class MainApplication {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Login authSystem = new Login();

        System.out.println("=== VANGUARD SECURE CHAT AUTHENTICATION SYSTEM ===");
        
        // Prompt the user for their personal details
        System.out.print("Enter your First Name: ");
        String firstName = scanner.nextLine();

        System.out.print("Enter your Last Name: ");
        String lastName = scanner.nextLine();

        boolean registrationComplete = false;

        // Loop registration until valid credentials are provided
        while (!registrationComplete) {
            System.out.print("\nEnter Username (max 5 chars, must contain '_'): ");
            String username = scanner.nextLine();

            System.out.print("Enter Password (min 8 chars, 1 capital, 1 number, 1 special): ");
            String password = scanner.nextLine();

            System.out.print("Enter Cell Phone Number (e.g., +27838968976): ");
            String cellNumber = scanner.nextLine();

            // Register user and evaluate feedback
            String regResponse = authSystem.registerUser(username, password, cellNumber, firstName, lastName);
            System.out.println("\n[Registration Feedback]:\n" + regResponse);

            if (regResponse.contains("successfully")) {
                registrationComplete = true;
            } else {
                System.out.println("Please try registering again with correct formatting.\n");
            }
        }

        System.out.println("\n=== LOGIN PORTAL ===");
        boolean loginSuccess = false;
        
        // Loop login until credentials match successfully
        while (!loginSuccess) {
            System.out.print("Enter Username: ");
            String loginUser = scanner.nextLine();

            System.out.print("Enter Password: ");
            String loginPass = scanner.nextLine();

            loginSuccess = authSystem.loginUser(loginUser, loginPass);
            String loginStatusMsg = authSystem.returnLoginStatus(loginSuccess);
            
            System.out.println("[Login Feedback]: " + loginStatusMsg);

            // If login fails this prompts the user to try again
            if (!loginSuccess) {
                System.out.println("Authentication failed. Please check your credentials.\n");
            }
        }

        scanner.close();
    }
}