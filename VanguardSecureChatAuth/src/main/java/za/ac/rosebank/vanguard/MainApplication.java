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

        // Messages can only be sent once the user has logged in successfully
        if (loginSuccess) {
            runQuickChat(scanner);
        }

        scanner.close();
    }

    // Main QuickChat loop: runs until the user selects Quit
    private static void runQuickChat(Scanner scanner) {
        System.out.println("\nWelcome to QuickChat.");

        int numMessages = promptForNumMessages(scanner);
        boolean messagesEntered = false;
        boolean running = true;

        while (running) {
            printMenu();
            int choice = readNumber(scanner);

            switch (choice) {
                case 1:
                    if (messagesEntered) {
                        System.out.println("You have already entered all " + numMessages + " messages.");
                    } else {
                        enterMessages(scanner, numMessages);
                        messagesEntered = true;
                    }
                    break;
                case 2:
                    System.out.println("Coming Soon.");
                    break;
                case 3:
                    System.out.println("Thank you for using QuickChat. Goodbye!");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option. Please enter 1, 2 or 3.");
            }
        }
    }

    private static void printMenu() {
        System.out.println("\n=== QUICKCHAT MENU ===");
        System.out.println("1) Send Messages");
        System.out.println("2) Show recently sent messages");
        System.out.println("3) Quit");
        System.out.print("Choose an option: ");
    }

    // Asks how many messages the user wants to enter; only that many are allowed
    private static int promptForNumMessages(Scanner scanner) {
        int numMessages = 0;
        while (numMessages < 1) {
            System.out.print("How many messages do you wish to enter? ");
            numMessages = readNumber(scanner);
            if (numMessages < 1) {
                System.out.println("Please enter a whole number of 1 or more.");
            }
        }
        return numMessages;
    }

    // The for loop counter doubles as the message number and 0 for the first message
    private static void enterMessages(Scanner scanner, int numMessages) {
        for (int messageNumber = 0; messageNumber < numMessages; messageNumber++) {
            System.out.println("\n--- Message " + (messageNumber + 1) + " of " + numMessages + " ---");

            String recipient = promptForRecipient(scanner);
            String text = promptForMessageText(scanner);

            Message message = new Message(messageNumber, recipient, text);
            System.out.println(message.generatedIDMessage());
            System.out.println("Message Hash: " + message.getMessageHash());

            handleSendOption(scanner, message);
        }
        System.out.println("\nTotal number of messages sent: " + Message.returnTotalMessages());
    }

    // Keeps asking until the recipient number is accepted
    private static String promptForRecipient(Scanner scanner) {
        while (true) {
            System.out.print("Enter recipient cell number (e.g., +27718693002): ");
            String recipient = scanner.nextLine().trim();
            String result = Message.checkRecipientCell(recipient);
            System.out.println(result);
            if (result.contains("successfully")) {
                return recipient;
            }
        }
    }

    // Keeps asking until the message is not empty and is within 250 characters
    private static String promptForMessageText(Scanner scanner) {
        while (true) {
            System.out.print("Enter your message (max 250 characters): ");
            String text = scanner.nextLine();
            if (text.trim().isEmpty()) {
                System.out.println("A message cannot be empty.");
            } else if (text.length() > Message.MAX_MESSAGE_LENGTH) {
                System.out.println("Please enter a message of less than 250 characters.");
                System.out.println(Message.checkMessageLength(text));
            } else {
                System.out.println("Message sent");
                return text;
            }
        }
    }

    // Lets the user send, disregard or store the message
    private static void handleSendOption(Scanner scanner, Message message) {
        boolean decided = false;
        while (!decided) {
            System.out.println("\nWhat would you like to do with this message?");
            System.out.println("1) Send Message");
            System.out.println("2) Disregard Message");
            System.out.println("3) Store Message to send later");
            System.out.print("Choose an option: ");
            int choice = readNumber(scanner);

            System.out.println(message.sentMessage(choice));

            if (choice == 1) {
                System.out.println("\n" + message.getDetails());
                decided = true;
            } else if (choice == 2) {
                decided = confirmDelete(scanner, message);
            } else if (choice == 3) {
                decided = true;
            }
        }
    }

    // The message is only deleted when the user presses 0
    private static boolean confirmDelete(Scanner scanner, Message message) {
        if (readNumber(scanner) == 0) {
            System.out.println(message.deleteMessage());
            return true;
        }
        System.out.println("The message was not deleted. Please choose an option again.");
        return false;
    }

    // Reads a whole number from the console; returns -1 if the input is not a number
    private static int readNumber(Scanner scanner) {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}