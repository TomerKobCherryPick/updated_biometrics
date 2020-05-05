package com.bioauth.Authenticator;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.hardware.biometrics.BiometricPrompt;
import android.os.Build;
import android.os.CancellationSignal;

import com.facebook.react.bridge.ReactApplicationContext;

import java.util.concurrent.Executor;

public class BiometricPromptAuthenticator {
    private static ReactApplicationContext reactContext;
    private BiometricPrompt.Builder bioPromptBuilder;
    private BiometricPrompt bioPrompt;
    private CancellationSignal cancelSignal;
    private Executor executor;
    private BiometricPrompt.AuthenticationCallback callback;
    private BioAuth.errorTypes error;

    BiometricPromptAuthenticator(ReactApplicationContext context) {
        reactContext = context;
    }

    /***
     * api 28 implementation of Biometric authentication
     */
    private void setBioPrompt() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            executor = new Executor() {
                @Override
                public void execute(Runnable command) {
                    command.run();
                }
            };
            bioPromptBuilder = new BiometricPrompt.Builder(reactContext);
            bioPromptBuilder.setTitle(BioAuth.title);
            bioPromptBuilder.setSubtitle(BioAuth.description);
            // cancel button
            bioPromptBuilder.setNegativeButton("Cancel", executor, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // only if the user tried to previously authenticate
                    // and failed (in authenticationFailed function),
                    // then on pressing cancel, we treat it like the authentication has failed
                    if (error == BioAuth.errorTypes.authenticationFailed) {
                        BioAuth.runOnFailure(error);
                    }
                    cleanMemory();
                }
            });

            bioPrompt = bioPromptBuilder.build();
        }
    }

    @TargetApi(28)
    protected void authenticate() {
        setBioPrompt();
        cancelSignal = new CancellationSignal();
        if (bioPrompt != null) {
            callback = new BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationError(int errorCode, CharSequence errString) {
                    super.onAuthenticationError(errorCode, errString);
                    if ((errorCode == BiometricPrompt.BIOMETRIC_ERROR_NO_BIOMETRICS) ||
                            (errorCode == BiometricPrompt.BIOMETRIC_ERROR_HW_NOT_PRESENT) ||
                            (errorCode == BiometricPrompt.BIOMETRIC_ERROR_HW_UNAVAILABLE) ||
                            (errorCode == BiometricPrompt.BIOMETRIC_ERROR_LOCKOUT_PERMANENT) ||
                            (errorCode == BiometricPrompt.BIOMETRIC_ERROR_LOCKOUT)) {
                        BioAuth.passcodeAuthenticaton();
                    } else {
                        if (error == BioAuth.errorTypes.authenticationFailed) {
                            BioAuth.runOnFailure(error);
                        }
                        cleanMemory();
                    }
                }

                @Override
                public void onAuthenticationFailed() {
                    super.onAuthenticationFailed();
                    error = BioAuth.errorTypes.authenticationFailed;
                }
                @Override
                public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
                    super.onAuthenticationHelp(helpCode, helpString);
                }
                @Override
                public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                    super.onAuthenticationSucceeded(result);
                    BioAuth.runOnSuccess();
                    cleanMemory();
                }



            };
            bioPrompt.authenticate(cancelSignal, executor, callback);
        }
    }

    protected void cleanMemory() {
        bioPrompt = null;
        bioPromptBuilder = null;
        error = null;
    }

}
