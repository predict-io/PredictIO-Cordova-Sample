# PredictIO Cordova Plugins
predict.io offers mobile developers a battery-optimized SDK to get normalised sensor results. Available for Cordova as iOS and Android plugins. It gives you real-time updates when a user starts or ends a journey. With this trigger come contextual details for the mode of transportation (car vs. non-car).

## Features
#### Arrival
Detects that a user just arrived at the destination.

#### Departure
Detects that user just started their journey.

#### Transport Mode
Distinguish car and non-car trips.

#### Use Cases
Look through the [Use Cases](https://github.com/predict-io/PredictIO-Cordova/wiki/Use-Cases) where this SDK can be used.

## Remote Notification Support
predict.io sdk also provides a mechanism which can be used to send remote notifications to the users' devices when a departure or arrival event is detected. To make this happen, the sdk provides the following two methods,

- setCustomParameter
- setWebhookURL

To send a remote notification to an iOS device, the device token of that device is needed. Once the device token is fetched by using the appropriate iOS APIs, it needs to be sent to predict.io servers. To do this, use setCustomParameter method. The key should be /* device_token */ and the value should be the actual device token.
```javascript
cordova.exec( function successCallback() { },
              function errorCallback(error) { },
              'PredictIOPlugin',
              'setCustomParameter',
              ['device_token', token]);
```
The second important part in successfully delivering a remote notification is the server which actually originates the remote notification. In technical terms, this server is called the provider. Whenever a departure or arrival event is detected, predict.io sdk can communicate with the provider using a HTTP callback which should be implemented by the provider. The provider HTTP callback url can be set using the method setWebhookURL.
```javascript
cordova.exec( function successCallback() { },
              function errorCallback(error) { },
              'PredictIOPlugin',
              'setWebhookURL',
              ['https://api.parktag.mobi/demo/notifications/send_notification']);
```
So as soon as the departure or arrival event is detected, the predict.io sdk would send the appropriate event to the webhook url along with the device token and then the provider at the webhook url would be able to send remote notification to that specific device about that event.

## Requirements
#### iOS
* [Sign up](http://www.predict.io/sdk-sign-up/) for API key
* iOS 7.0+
* ARC

#### Android
* [Sign up](http://www.predict.io/sdk-sign-up/) for API key
* Android 2.3.3 (API 10) or above
* Google Play services 9.4.0

## Installation
#### iOS Integration Guide
To add predict.io iOS Plugin to your project, check the [predict.io cordova iOS Integration Guide](https://github.com/predict-io/PredictIO-Cordova/wiki/Integrating-predict.io-Cordova-iOS-Plugin) for more details.

#### Android Integration Guide
To add predict.io Android Plugin to your project, check the [predict.io cordova Android Integration Guide](https://github.com/predict-io/PredictIO-Cordova/wiki/Integrating-predict.io-Cordova-Android-Plugin) for more details.

## API Documentation
#### iOS API Documentation
For a complete reference of the API for iOS, please check out our [API documentation and usage guide iOS](https://github.com/predict-io/PredictIO-Cordova/wiki/API-Documentation-&-Usage-Guide-iOS).

#### Android API Documentation
For a complete reference of the API for Android, please check out our [API documentation and usage guide Android](https://github.com/predict-io/PredictIO-Cordova/wiki/API-Documentation-&-Usage-Guide-Android).

## FAQ
Look through the [FAQ](https://github.com/predict-io/PredictIO-Cordova/wiki/FAQs) for answers to the most commonly-asked questions about predict.io.

## Communication
If you need help, visit our [Help Center] (https://support.predict.io)

## Author
predict.io, support@predict.io

## Credits
### About predict.io
Our mobile SDK gives you battery friendly background location so you always know where and when a user arrives or departs. No fiddling with geofences. No beacons or NFC needed. It uses the sensors embedded in any modern smartphone. You can embed it in minutes. Rather than spending months fiddling with the Activity APIs yourself, our SDK powers many industry leading Android and iOS apps in mobility, retail, hospitality, lifestyle and banking.
### License
#### Terms of Service
Terms of service can be found [here](http://www.predict.io/terms-of-service/).
#### Privacy Policy
Privacy Policy can be found [here](http://www.predict.io/privacy-policy/).
#### License
predict.io is available under the Apache License Version 2.0. See the LICENSE file for more info.
