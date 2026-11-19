# VanguardSecureChatAuth

Console-based registration, login and QuickChat messaging system developed in Java for PROG5121.

Links:
GitHub Repository: https://github.com/Gadaffi-24/VanguardSecureChatAuth.git
YouTube Presentation (Part 1): https://youtu.be/hGN37Pw2YXY?si=7_9hqabkIpFIm9qe
YouTube Presentation (Part 2): https://youtu.be/PIpFs-L-zwY
YouTube Presentation (Part 3 / Final PoE): https://youtu.be/YADRzOCyBFg

# Part 1
Features:
- Username, complex password and SA cell phone number validation (+27...).
- User registration and secure login portal.
- JUnit 5 unit testing suite (LoginTest).

# Part 2
Features:
- Messages can only be sent after a successful login.
- "Welcome to QuickChat." and a numeric menu: 1) Send Messages, 2) Show recently sent messages ("Coming Soon.") and 3) Quit.
- The user chooses how many messages to enter and a for loop allows only that many.
- Each message has a random ten-digit ID, a message number (loop counter), a validated recipient (reuses the Part 1 cell checker), a message of at most 250 characters and an auto-generated hash (e.g. 00:0:HITONIGHT).
- The user can send, disregard (press 0 to delete) or store each message and stored messages are written to messages.json.
- Total number of sent messages is displayed at the end.
- JUnit 5 tests (MessageTest) run automatically through GitHub Actions (.github/workflows/test-java.yml).

# Part 3
Features:
- Arrays (MessageManager) filled while the user works, nothing is hard-coded: Sent Messages, Disregarded Messages, Stored Messages (read from messages.json), Message Hashes and Message IDs. The IDs, hashes and messages are parallel arrays.
- Main menu option 2 now shows a report of the sent messages and a fourth option, "4) Stored Messages", opens the Stored Messages menu:
1. Display the sender and recipient of all stored messages
2. Display the longest stored message
3. Search for a message ID and display the recipient and message
4. Search for all sent or stored messages for a recipient
5. Delete a stored message using its message hash (also removed from messages.json)
6. Display a report with the full details of all stored messages
- MessageJsonStorage reads messages.json back into an array and writes it again (follows the JSON grammar in RFC 8259).
- JUnit 5 tests (MessageManagerTest and MessageJsonStorageTest) use the Part 3 test data and run through GitHub Actions.

# References
Bray, T. (ed.) 2017. The JavaScript Object Notation (JSON) Data Interchange Format. RFC 8259. Internet Engineering Task Force. Available at: https://www.rfc-editor.org/rfc/rfc8259 [Accessed 17 November 2026].