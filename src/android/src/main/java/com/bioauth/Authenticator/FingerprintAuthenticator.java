package com.bioauth.Authenticator;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.fingerprint.FingerprintManager;
import android.os.CancellationSignal;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import com.bioauth.R;

import com.facebook.react.bridge.ReactApplicationContext;


import java.lang.ref.WeakReference;

public class FingerprintAuthenticator {
    private static ReactApplicationContext reactContext;
    private FingerprintManager fingerprintManager;
    private AlertDialog fingerPrintDialog;
    private AlertDialog.Builder fingerPrintDialogBuilder;
    private CancellationSignal cancelSignal;
    private BioAuth.errorTypes error;
    private static  WeakReference<Activity> weakActivity;

    FingerprintAuthenticator(ReactApplicationContext context) {
        reactContext = context;
    }

    public static void setActivity(Activity activity) {
        weakActivity = new WeakReference<>(activity);
    }

    @TargetApi(23)
    private void setFingerprintManager() {
        fingerprintManager = (FingerprintManager) getCurrentActivity().getSystemService(reactContext.FINGERPRINT_SERVICE);
    }

    @TargetApi(23)
    private void setFingerPrintAlertDialog() {
        Context ctx = getCurrentActivity();

        fingerPrintDialogBuilder = new AlertDialog.Builder(ctx);
        fingerPrintDialogBuilder.setTitle(BioAuth.title);
        fingerPrintDialogBuilder.setCancelable(false);
        // cancel button
        fingerPrintDialogBuilder.setPositiveButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                getCurrentActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        fingerPrintDialog.hide();
                        cancelSignal.cancel();
                        // only if the user tried to previously authenticate
                        // and failed (in authenticationFailed function),
                        // then on pressing cancel, we treat it like the authentication has failed
                        if (error != null) {
                            BioAuth.runOnFailure(error);
                        }
                        cleanMemory();
                    }
                });
            }
        });

        fingerPrintDialogBuilder.setNegativeButton("USE PASSWORD", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                getCurrentActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        fingerPrintDialog.hide();
                        cancelSignal.cancel();
                        BioAuth.passcodeAuthenticaton();
                    }
                });

            }
        });

        LayoutInflater inflater = (LayoutInflater) reactContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customLayout = inflater.inflate(R.layout.fingerprint_dialog, null);
        TextView description = (TextView) customLayout.findViewById(R.id.fingerprint_description);
        description.setText(BioAuth.description);
        fingerPrintDialogBuilder.setView(customLayout);


    }

    @TargetApi(23)
    protected void authenticate() {
        setFingerprintManager();
        if (fingerprintManager != null) {
            setFingerPrintAlertDialog();
            if (fingerprintManager.isHardwareDetected() && fingerprintManager.hasEnrolledFingerprints()) {
                this.showFingerPrintDialog();
                cancelSignal = new CancellationSignal();
                fingerprintManager.authenticate(null, cancelSignal, 0, new FingerprintManager.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationError(int errorCode, CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                    }

                    @Override
                    public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
                        super.onAuthenticationHelp(helpCode, helpString);
                    }

                    @Override
                    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        getCurrentActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                fingerPrintDialog.hide();
                                BioAuth.runOnSuccess();
                                cleanMemory();
                            }
                        });
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        error = BioAuth.errorTypes.authenticationFailed;
                        getCurrentActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Window fingerprintDialogWindow = fingerPrintDialog.getWindow();
                                ImageView fingerprintImage = fingerprintDialogWindow.findViewById(R.id.fingerprint_image);
                                TextView fingerprintCta = fingerprintDialogWindow.findViewById(R.id.fingerprint_cta);
                                fingerprintCta.setText("Try Again.");
                                fingerprintImage.setImageResource(R.drawable.fingerprint_fail);
                            }
                        });

                    }

                }, null);
            } else {
                BioAuth.passcodeAuthenticaton();
            }
        }
    }

    protected void cleanMemory() {
        fingerprintManager = null;
        fingerPrintDialog = null;
        fingerPrintDialogBuilder = null;
        error = null;
    }

    private void showFingerPrintDialog() {
        getCurrentActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                fingerPrintDialog = fingerPrintDialogBuilder.create();
                fingerPrintDialog.show();
            }
        });
    }

    private Activity getCurrentActivity() {
        if (weakActivity != null && weakActivity.get() != null) {
            return weakActivity.get();
        }
        else {
            return reactContext.getCurrentActivity();
        }
    }
}