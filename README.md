[En français](https://github.com/fensminger/Urgence/blob/master/README-fr.md)

Emergency call
=========

## Program using ##

This application is designed to allow you to quickly call emergencies.

A Widget (size 4x2) allows to call emergency via the main button and quickly call 4 others contacts.

![](https://github.com/fensminger/Urgence/blob/master/doc/img/UrgenceWidget.png?raw=true)

The main application is used to configure and to call for emergency and other contacts. A long press on a contact can remove this contact from the emergency number. No action is obviously done on the contact itself.
The application is based entirely on the list of phone numbers existing in the phone.

![](https://github.com/fensminger/Urgence/blob/master/doc/img/UrgenceParam1.png?raw=true)


### Call the main emergency contact ###

This button allows to call the emergency (for example, 112 or a privileged person). If in the settings of other contacts you have checked the SMS box, an SMS will be sent with your approximate location. And this SMS will mention to the person that you have called. SMS is sent 2 minutes later only if you press the button of the phone.

If "Sending an SMS on try calling emergency" is activated, one or more SMS are sent from the moment you press the main button. Nevertheless, the message would be less alarmist indicating that this is perhaps a mistake.

![](https://github.com/fensminger/Urgence/blob/master/doc/img/UrgenceParam2.png?raw=true)

### Calling other contacts ###

The four smaller buttons are actually shortcuts to your preferred people to call them directly.
No SMS is send on this 4 buttons.

## Future developments ##

This application is free and intended to remain so. Sources are available on github by searching on "Urgence".

The application was only tested on the emulator and on a Samsung Galaxy S2 on Android 4.0.3. Nevertheless, it should not be any problem to run on other phones. Feel free to send me a feedback on github.

Next planned changes:

- Improve "look" of the Widget.
- Put the contact picture in the button widget's call.
- Make the widget available on the screen lock phone.
- Add option for directly calling the emergency without validation.

