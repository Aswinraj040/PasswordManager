Password Manager App using Jetpack compose

Here's a detailed documentation guide on how to build, run, and use my password manager application developed using Jetpack Compose in Android Studio.

Building the Application

Prerequisites

1.	Android Studio: Ensure you have Android Studio installed and updated to version Koala | 2024.1.1 or a version which supports gradle-8.7 and agp version 8.5.0

2.	Java Development Kit (JDK): Make sure JDK is installed on your development machine.

3. Device or Emulator : Prepare a physical Android device or set up an Android emulator for testing out the application Minimum sdk requirement is 24 (Android 7 Nougat) and Maximum sdk requirement is 34 (Android 14 UpsideDownCake).

Steps to Build

1.	Clone the Repository : I have attached my repo link below , open the link and clone the project to your local machine. Also I have attached the compiled apk in the GitHub repository , you can download it and install the apk for quick usage of the app.

GitHub Repository link : https://github.com/Aswinraj040/PasswordManager

2. Open Project in Android Studio : Launch Android Studio and open the password manager project.

3. Build Project :
   - Wait for Android Studio to sync and build the project automatically.
   - Resolve any dependency issues that might arise during the build process.

Running the Application

Running on Emulator or Physical Device

1. Select Target Device:
   - Connect a physical Android device via USB or launch an Android emulator from Android Studio's AVD Manager.
   - Ensure the device/emulator is detected and visible in Android Studio.

2. Run Configuration:
   - Choose the app module and select the target device/emulator.

3. Run the App:
   - Click on the green "Run" button or use the shortcut `Shift + F10` to deploy and launch the application on the selected device/emulator.
   - Android Studio installs the APK and starts the app automatically.

4. Verify Installation:
   - Once the app is installed and launched successfully, verify that it functions as expected on the device/emulator.

Using the Application

Initial Setup and Authentication

1. First Launch:
   - On the first launch of the password manager app, the biometric authentication prompt will appear, depending on your authentication setup if you have biometric support you can use your fingerprint. If you don’t have biometric support you can use your password or pattern.

2. Home Screen:
   - Upon successful authentication, the home screen of the app will display a list of all saved passwords.

Managing Passwords

1. Adding a New Password:
   - To add a new password, press the floating action button.
   - A bottom sheet dialog will appear and ask for the Account name , Username/ Email and        for password
   - Enter the account type (e.g., Gmail, Facebook), username/email, and password.
   - Ensure all mandatory fields are filled, and press on “Add New Account”
   - After adding the new password It will be displayed immediately on the screen

2. View/Edit Existing Passwords:
   - Tap on an existing password entry from the home screen list to view its details.
   - Press the edit button , a new bottom dialog sheet appears where you can edit the Username/ Email and password. Note : You cannot edit the Account type!

3. Deleting Passwords:
   - To delete a password, click on the Account type a bottom sheet dialog appears.
   - Use the delete button to remove the password from the database securely.

4. Biometric/PIN Authentication:
   - Each time the app is launched, it should prompt for biometric authentication (or PIN) to ensure secure access to password data.

Error Handling and Security Measures

- Encryption: I have used strong encryption algorithm called AES to protect user data. And Sqllite3 Database for storing the details in the device.
- Input Validation: The data’s are validated precisely, if a field is empty the app will not allow to add new account or save the edited changes.

Conclusion

By following this comprehensive guide, you can build, run, and effectively use the password manager application developed with Jetpack Compose. Thank you for giving me this wonderful opportunity to explore jetpack compose and also other new features.

Developer details

Aswin R
9042046471
aswinraj040@gmail.com
