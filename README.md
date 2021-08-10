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

![emulatorchoice](https://user-images.githubusercontent.com/18041942/128904833-fcd1fc8a-9341-423c-aefd-5734ecab1756.png)

Then simply run the emulators individually. 

With both running, within the app, you can enter the port number of the opposite emulator into the Phone Number field and click add:

![addcontact](https://user-images.githubusercontent.com/18041942/128905198-52181bfe-7d5f-4c3a-8f64-51970da4430b.png)

By clicking add, you ensure that the address you are sending to exists within the database. If you forget to press Add, a Toast 
will display reminding you. With the contact added, you can enter a message and press send. The receiving emulator will receive
a notification from the built in messenging app that shows the encrypted message. Due to the fact that we are utilizing SMS, any 
app which reads from the devices sms/inbox will see the message, however they will only see the encrypted version. When in this sms
application, however, the message will be decrypted. To showcase this, an Encrypt/Decrypt button was added allowing the user to 
toggle between showing the encrypted and unencrypted message within the app. Due to time constraints, no receiver was implemented, 
meaning the app will not automatically update when an sms is received, so toggling the Encrypt/Decrypt button will refresh the inbox.

![encryptdecrypt](https://user-images.githubusercontent.com/18041942/128906340-f3471850-ca42-486d-b1aa-737fc4eeee5c.png)

A note about the inbox: the application is designed to only show data which follows a particular format, so messages within the 
sms/inbox that do not follow this (whether encrypted or not) will be ignored in the display. In the above image, the right 
shot shows many more messages than the three in the left because there are messages saved within my emulators storage that were
encrypted with the same format but do not have a key, and thus can't be decrypted. Those extra messages won't show up on your 
screen. Additionally and not ideally, due to time constraints the ordering of the inbox contents was ignored as the purpose of
this application was the encryption.
