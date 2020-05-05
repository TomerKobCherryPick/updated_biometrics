package com.bioauth.Authenticator;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Intent;
import android.os.Build;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.BaseActivityEventListener;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import static android.content.Context.KEYGUARD_SERVICE;

/**
 * This class represents biometric authentication.
 * there are 2 implementations here, one for 23 <= api < 28 , using Fingerprint manager
 * and one for 28 <= api, using Biometric prompt. this is needed since, Fingerprint manager was deprecated
 * in api 28, and Biometric Prompt was added instead.
 * in order for this module to work you need to add the following line to your AndroidManifest.xml:
 *  <uses-permission android:name="android.permission.USE_BIOMETRIC" />
 *  <uses-permission android:name="android.permission.USE_FINGERPRINT" />
 */
public class BioAuth extends ReactContextBaseJavaModule {
    private static ReactApplicationContext reactContext;
    private static FingerprintAuthenticator fingerprintAuthenticator;
    private static BiometricPromptAuthenticator biometricPromptAuthenticator;
    private static Callback onSuccess;
    private static Callback onFailure;
    protected static String title = "";
    protected static String description = "";

    protected enum errorTypes {
        authenticationFailed,
        noAuthenticationOnDevice
    }

    BioAuth(ReactApplicationContext context) {
        super(context);
        reactContext = context;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            biometricPromptAuthenticator = new BiometricPromptAuthenticator(context);
        } else {
            fingerprintAuthenticator = new FingerprintAuthenticator(context);
        }
    }

    public static void runOnSuccess() {
        if (onSuccess != null) {
            onSuccess.invoke();
        }
    }

    public static void runOnFailure(errorTypes error) {
        if (onFailure != null) {
            onFailure.invoke(error.toString());
        }
    }

    @TargetApi(21)
    /**
     * this method responsible when there is no biometrics available on the device,
     * it tries to ask for a passcode instead
     */
    protected static void passcodeAuthenticaton() {
        KeyguardManager km = (KeyguardManager) reactContext.getSystemService(KEYGUARD_SERVICE);

        if (km.isKeyguardSecure()) {
            Intent authIntent = km.createConfirmDeviceCredentialIntent(BioAuth.title, BioAuth.description);
            final int AUTH_REQUEST_CODE = 1;
            ActivityEventListener authEventListener = new BaseActivityEventListener(){
                @Override
                public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
                    super.onActivityResult(activity, requestCode, resultCode, data);
                    if (requestCode == AUTH_REQUEST_CODE) {
                        if (resultCode == Activity.RESULT_OK) {
                            // passcode succeeded
                            runOnSuccess();
                        } else {
                            // passcode did not succeed
                            runOnFailure(errorTypes.authenticationFailed);
                        }
                        reactContext.removeActivityEventListener(this);
                    }
                }

            };
            reactContext.addActivityEventListener(authEventListener);

            reactContext.startActivityForResult(authIntent, AUTH_REQUEST_CODE, new android.os.Bundle());

        } else {
            // no authentication on this device
            runOnFailure(errorTypes.noAuthenticationOnDevice);
        }
        cleanMemory();
    }

    private static void cleanMemory() {
        if (biometricPromptAuthenticator != null) {
            biometricPromptAuthenticator.cleanMemory();
        }
        if (fingerprintAuthenticator != null) {
            fingerprintAuthenticator.cleanMemory();
        }
    }

    @Override
    public String getName() {
        return "BioAuth";
    }



    @ReactMethod
    public void authenticate(String authenticationTitle, String authenticationDescription, Callback onSuccess, Callback onFailure) {
        BioAuth.onFailure = onFailure;
        BioAuth.onSuccess = onSuccess;
        BioAuth.title = authenticationTitle;
        BioAuth.description = authenticationDescription;
        String brand = Build.BRAND;


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            /*
             * this is because of a known issue with how OnePlus implemented biometric prompt
             * https://forums.oneplus.com/threads/problems-about-oneplus-6t-fingerprint-api.944959/
             */
            if (brand.equals("OnePlus")) {
                passcodeAuthenticaton();
            } else {
                biometricPromptAuthenticator.authenticate();
            }

        } else {
            fingerprintAuthenticator.authenticate();
        }
    }
}
