# react-native-paypal-android

## Getting started

`$ npm install react-native-paypal-android --save`

### Mostly automatic installation

`$ react-native link react-native-paypal-android`

### Manual installation

#### Android

1. Open up `android/app/src/main/java/[...]/MainApplication.java`
  - Add `import com.thebylito.reactnativepagseguro.RNPaypalPackage;` to the imports at the top of the file
  - Add `new RNPaypalPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-paypal-android'
  	project(':react-native-paypal-android').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-paypal-android/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-paypal-android')
  	```

## Usage
```javascript
import React, { Component } from 'react';
import RNPaypal from 'react-native-paypal-android';
import { StyleSheet, View, Button } from 'react-native';

const client = {
  sandbox: 'YOUR_SANDBOX_KEY',
  production: 'YOUR_PRODUCTION_KEY',
}

export default class App extends Component {
  render() {
    return (
      <View style={styles.container}>
        <Button
          title="Comprar"
          onPress={async () => {
            try {
                await RNPaypal.config({
                  clientId: client.sandbox,
                  environment: RNPaypal.constants.env.SANDBOX
                })
              const pay = await RNPaypal.buy({
                value: 1.99,
                productName: 'Testanto 100',
                currency: 'BRL',
                mode: RNPaypal.constants.mode.PAYMENT_INTENT_SALE
              });
              console.log(pay);// SUCESSS
            } catch (e) {
              console.log(e);// NO MONEY :()
            }
          }}
        />
      </View>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#F5FCFF',
  }
});
```
## API
```javascript
import RNPaypal from 'react-native-paypal-android';

// TODO: What to do with the module?
RNPaypal;
```