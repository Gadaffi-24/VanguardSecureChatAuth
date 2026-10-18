# VanguardSecureChatAuth

Console-based registration, login and QuickChat messaging system developed in Java for PROG5121.

Links:
GitHub Repository: https://github.com/Gadaffi-24/VanguardSecureChatAuth.git
YouTube Presentation (Part 1): https://youtu.be/hGN37Pw2YXY?si=7_9hqabkIpFIm9qe
YouTube Presentation (Part 2): https://youtu.be/PIpFs-L-zwY

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