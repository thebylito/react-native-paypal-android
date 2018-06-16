package com.thebylito.rnpaypal;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.BaseActivityEventListener;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import org.json.JSONException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class RnPaypalModule extends ReactContextBaseJavaModule  implements LifecycleEventListener {
    public static final String REACT_CLASS = "RnPaypal";
    private static final String ERROR_USER_CANCELLED = "USER_CANCELLED";
    private static final String ERROR_INVALID_CONFIG = "INVALID_CONFIG";
    private static final String ERROR_INTERNAL_ERROR = "INTERNAL_ERROR";
    private static final String ERROR_NO_CLIENTID = "the 'clientId' parameter is required";
    private static final String ERROR_NO_ENVIRONMENT = "the 'environment' parameter is required";
    private static final String ERROR_INVALID_ENVIRONMENT = "the 'environment' parameter is invalid";
    private static final String ERROR_NO_PRODUCTNAME = "the 'productName' parameter is required";
    private static final String ERROR_NO_CURRENCY = "the 'currency' parameter is required";
    private static final String ERROR_NO_VALUE = "the 'value' parameter is required";
    private static final String ERROR_NO_MODE = "the 'mode' parameter is required";
    private static final String ERROR_INVALID_MODE = "the 'mode' parameter is invalid";


    private static final String PAYMENT_INTENT_SALE= PayPalPayment.PAYMENT_INTENT_SALE;
    private static final String PAYMENT_INTENT_ORDER= PayPalPayment.PAYMENT_INTENT_ORDER;
    private static final String PAYMENT_INTENT_AUTHORIZE= PayPalPayment.PAYMENT_INTENT_AUTHORIZE;
    private static final String SANDBOX = PayPalConfiguration.ENVIRONMENT_SANDBOX;
    private static final String PRODUCTION = PayPalConfiguration.ENVIRONMENT_PRODUCTION;


    private static ReactApplicationContext reactContext = null;
    private static PayPalConfiguration config;// = new PayPalConfiguration().environment(PayPalConfiguration.ENVIRONMENT_SANDBOX).clientId(PaypalKey);
    private Promise promise;

    private final ActivityEventListener mActivityEventListener = new BaseActivityEventListener() {
        @Override
        public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
            if (resultCode == Activity.RESULT_OK) {
                PaymentConfirmation confirm = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if (confirm != null) {
                    try {
                        WritableMap map;
                        map = JsonConvert.jsonToReact(confirm.toJSONObject());
                        promise.resolve(map);
                    } catch (JSONException e) {
                        promise.reject(ERROR_INTERNAL_ERROR, "an extremely unlikely failure occurred: ", e);
                    }
                }
            }
            else if (resultCode == Activity.RESULT_CANCELED) {
                promise.reject(ERROR_USER_CANCELLED,"The user canceled.");
            }
            else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
                promise.reject( ERROR_INVALID_CONFIG,"An invalid Payment or PayPalConfiguration was submitted. Please see the docs.");
            }
            config = null;
        }
    };

    public RnPaypalModule(ReactApplicationContext context) {
        super(context);
        reactContext = context;
        reactContext.addActivityEventListener(mActivityEventListener);
        reactContext.addLifecycleEventListener(this);
    }

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    public Map<String, Object> getConstants() {
        final Map<String, Object> constants = new HashMap<>();
        constants.put("SANDBOX", SANDBOX);
        constants.put("PRODUCTION", PRODUCTION);
        constants.put("PAYMENT_INTENT_SALE", PAYMENT_INTENT_SALE);
        constants.put("PAYMENT_INTENT_ORDER", PAYMENT_INTENT_ORDER);
        constants.put("PAYMENT_INTENT_AUTHORIZE", PAYMENT_INTENT_AUTHORIZE);
        return constants;
    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @ReactMethod
    public void setUserAccount(ReadableMap account, Promise promise){
        if(!account.hasKey("clientId")){
            promise.reject(ERROR_NO_CLIENTID, ERROR_NO_CLIENTID);
            return;
        }else if(!account.hasKey("environment")){
            promise.reject(ERROR_NO_ENVIRONMENT, ERROR_NO_ENVIRONMENT);
            return;
        }

        String clientId = account.getString("clientId");
        String environment = account.getString("environment");
        if(!(environment.equals(SANDBOX) || environment.equals(PRODUCTION))){
            promise.reject(ERROR_INVALID_ENVIRONMENT, ERROR_INVALID_ENVIRONMENT);
            return;
        }
        config =  new PayPalConfiguration().environment(environment).clientId(clientId);
        Intent intent = new Intent(reactContext, PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        reactContext.startService(intent);
        promise.resolve(null);

    }


    @ReactMethod
    public void buy (ReadableMap params, Promise promise) {
        this.promise = promise;
        if(!params.hasKey("productName")){
            this.promise.reject(ERROR_NO_PRODUCTNAME, ERROR_NO_PRODUCTNAME);
            return;
        }else if(!params.hasKey("currency")){
            this.promise.reject(ERROR_NO_CURRENCY, ERROR_NO_CURRENCY);
            return;
        }else if(!params.hasKey("value")) {
            this.promise.reject(ERROR_NO_VALUE, ERROR_NO_VALUE);
            return;
        }else if(!params.hasKey("mode")) {
            this.promise.reject(ERROR_NO_MODE, ERROR_NO_MODE);
            return;
        }
        String productName = params.getString("productName");
        String currency = params.getString("currency");
        Double productValue = params.getDouble("value");
        String mode = params.getString("mode");

        if(!(mode.equals(PAYMENT_INTENT_SALE) || mode.equals(PAYMENT_INTENT_ORDER)|| mode.equals(PAYMENT_INTENT_AUTHORIZE))){
            this.promise.reject(ERROR_INVALID_MODE, ERROR_INVALID_MODE);
            return;
        }


        PayPalPayment payment = new PayPalPayment(new BigDecimal(productValue), currency, productName, mode);
        Intent intent = new Intent(reactContext, PaymentActivity.class);
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payment);
        getCurrentActivity().startActivityForResult(intent, 0);

    }

    @Override
    public void onHostDestroy() {
        reactContext.stopService(new Intent(reactContext, PayPalService.class));
    }

    @Override
    public void onHostResume() {
        // Do nothing
    }

    @Override
    public void onHostPause() {
        // Do nothing
    }
}
