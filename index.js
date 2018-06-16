//  Created by react-native-create-bridge

import { NativeModules } from 'react-native';

const { RnPaypal } = NativeModules;

export default {
  buy(params) {
    return RnPaypal.buy(params);
  },
  config(user) {
    return RnPaypal.setUserAccount(user);
  },
  constants: {
    env: {
      SANDBOX: RnPaypal.SANDBOX,
      PRODUCTION: RnPaypal.PRODUCTION,
    },
    mode: {
      PAYMENT_INTENT_SALE: RnPaypal.PAYMENT_INTENT_SALE,
      PAYMENT_INTENT_ORDER: RnPaypal.PAYMENT_INTENT_ORDER,
      PAYMENT_INTENT_AUTHORIZE: RnPaypal.PAYMENT_INTENT_AUTHORIZE,
    }
  },
};
