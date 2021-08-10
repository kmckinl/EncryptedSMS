# EncryptedSMS

A mobile SMS application which utilizes AES encryption and Google Firestore for secure key storage to protect messages contents. 
SMS messages are encrypted before being sent to the target address, causing the recipient to only see an encrypted message within
their devices sms inbox, unless in the app as well. When the message is encrypted, a unique ID is generated to go along with the message, 
and a key is generated and sent alongside this ID to a Firestore database where the recipient, upon receiving the message, can use the 
address the message came from and the message ID to locate the encryption key to decrypt the message. 

## Installation
No APK was generated for this, in order to run this app, you must have Android Studio as well as two emulators within Android Studio
using API 30 or above. The gradle will build all necessary dependencies, the only information you will need are the port numbers
of the emulators.

To get the emulator port number, the simplest method is to start the emulator, then hover your mouse over the icon on your taskbar
until the full name of the application (emulator) appears, formatted similarly to "Android Emulator - Pixel_XL_API_30:5554" where
5554 is the port for this emulator. It is probable that the ports will be 5554 and 5556, but it is always best to check.

Once you have both emulators running and know their ports, build and run the app within Android Studio for both emulators. To choose
a different emulator, simply select it from the drop down shown:
