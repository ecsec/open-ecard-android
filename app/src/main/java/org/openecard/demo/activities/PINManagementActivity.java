package org.openecard.demo.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import org.openecard.android.activation.ActivationImplementationInterface;
import org.openecard.android.activation.ActivationResult;
import org.openecard.android.activation.PinMgmtActivationHandler;
import org.openecard.demo.R;
import org.openecard.demo.fragments.CANInputFragment;
import org.openecard.demo.fragments.FailureFragment;
import org.openecard.demo.fragments.GenericInputFragment;
import org.openecard.demo.fragments.InitFragment;
import org.openecard.demo.fragments.PINChangeFragment;
import org.openecard.demo.fragments.PUKInputFragment;
import org.openecard.demo.fragments.WaitFragment;
import org.openecard.gui.android.pinmanagement.PINManagementGui;
import org.openecard.gui.android.pinmanagement.PinStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PINManagementActivity extends AppCompatActivity {

    private static final Logger LOG = LoggerFactory.getLogger(PINManagementActivity.class);


    private final PinMgmtActivationHandler<PINManagementActivity> activationImpl;
    private PINManagementGui pinMngGui;
    private Button cancelBtn;

    public PINManagementActivity() {
        this.activationImpl = new ActivationImpl();
    }

    private class ActivationImpl extends PinMgmtActivationHandler<PINManagementActivity> {

        public ActivationImpl() {
            super(PINManagementActivity.this);
        }

        @Override
        public void onGuiIfaceSet(PINManagementGui gui) {
            PINManagementActivity.this.pinMngGui = gui;
            initPinChangeGui();
        }

        @Override
        public void onAuthenticationFailure(ActivationResult result) {
            LOG.info("Authentication failure: {}", result);
        }

        @Override
        public void onAuthenticationInterrupted(ActivationResult result) {
            LOG.info("Authentication interrupted: {}", result);
            //back to Main menu
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setClass(PINManagementActivity.this, IdsActivity.class);
            startActivity(i);
        }

        @Nullable
        @Override
        public Dialog showCardRemoveDialog() {
            AlertDialog dialog = new AlertDialog.Builder(PINManagementActivity.this)
                    .setTitle("Remove the Card")
                    .setMessage("Please remove the identity card.")
                    .setNeutralButton("Proceed", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create();
            dialog.show();
            return dialog;
        }
    }

    public void onPINIsRequired(PinStatus status) {
        PINChangeFragment fragment = new PINChangeFragment();
        fragment.setStatus(status);

        // show PINChangeFragment
        getFragmentManager().beginTransaction()
                .replace(R.id.fragment, fragment).addToBackStack(null).commitAllowingStateLoss();
    }


    @Override
    public void onBackPressed() {
        activationImpl.cancelAuthentication();
    }



    ///
    /// Callback handlers from Activity which have to be forwarded to the Activation implementation
    ///

    @Override
    protected void onStart() {
        super.onStart();
        activationImpl.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        activationImpl.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        activationImpl.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom);

        cancelBtn = findViewById(R.id.cancelBtn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                LOG.info("Cancel pressed");
                cancelBtn.setEnabled(false);
                cancelBtn.setClickable(false);

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (pinMngGui != null) {
                            pinMngGui.cancel();
                        }
                        activationImpl.cancelAuthentication();
                    }
                }, 100);
            }
        });

        if (findViewById(R.id.fragment) != null) {
            // show InitFragment
            Fragment fragment = new InitFragment();
            cancelBtn.setVisibility(View.VISIBLE);
            fragment.setArguments(getIntent().getExtras());
            getFragmentManager().beginTransaction()
                    .replace(R.id.fragment, fragment).addToBackStack(null).commit();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        activationImpl.onResume();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        activationImpl.onNewIntent(intent);
        // if you receive a nfc tag, disable the cancel button until the next fragment comes in
        //disableCancel();

        if (findViewById(R.id.fragment) != null) {
            // show InitFragment
            Fragment fragment = new WaitFragment();
            cancelBtn.setVisibility(View.VISIBLE);
            fragment.setArguments(getIntent().getExtras());
            getFragmentManager().beginTransaction()
                    .replace(R.id.fragment, fragment).addToBackStack(null).commit();
        }
    }

    public void onCANIsRequired(boolean triedBefore){
        GenericInputFragment fragment = new CANInputFragment();

        if(triedBefore){
            fragment.setMessage("The entered CAN was wrong, please try again.");
        }

        // show CANInput
        getFragmentManager().beginTransaction()
                .replace(R.id.fragment, fragment).addToBackStack(null).commitAllowingStateLoss();
    }

    public void onPUKIsRequired(boolean triedBefore){
        GenericInputFragment fragment = new PUKInputFragment();

        if(triedBefore){
            fragment.setMessage("The entered PUK was wrong, please try again.");
        }

        // show PUKInput
        getFragmentManager().beginTransaction()
                .replace(R.id.fragment, fragment).addToBackStack(null).commitAllowingStateLoss();
    }

    public void enterCan(String can){
        try {
            boolean canCorrect = pinMngGui.enterCan(can);
            LOG.info("CAN correct: {}", canCorrect);

            if(canCorrect){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            onPINIsRequired(pinMngGui.getPinStatus());
                        } catch (InterruptedException ex) {
                            LOG.error(ex.getMessage(), ex);
                        }
                    }
                });
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onCANIsRequired(true);
                    }
                });
            }

        } catch (InterruptedException ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }

    public void enterPUK(String puk){
        try {
            boolean pukCorrect = pinMngGui.unblockPin(puk);
            LOG.info("PUK correct: {}", pukCorrect);

            if(!pukCorrect){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onPUKIsRequired(true);
                    }
                });
            } else {
                showMessageFragment("PIN was successful unblocked.");
            }

        } catch (InterruptedException ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }

    public void changePin(String oldPin, String newPin){
        try {
            LOG.info("Perform PIN change...");
            boolean changeSuccessful = pinMngGui.changePin(oldPin, newPin);
            LOG.info("PINChange was successful: {}", changeSuccessful);

            if(!changeSuccessful) {
                initPinChangeGui();
            } else {
                showMessageFragment("Your PIN was changed successfully.");
                pinMngGui.cancel();
            }


        } catch (InterruptedException ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }

    private void showMessageFragment(String msg) {
        FailureFragment fragment = new FailureFragment();
        fragment.setErrorMessage(msg);

        // show ServerDataFragment
        getFragmentManager().beginTransaction()
                .replace(R.id.fragment, fragment).addToBackStack(null).commitAllowingStateLoss();
    }

    private void initPinChangeGui(){

        try {
            final PinStatus pinStatus = pinMngGui.getPinStatus();
            LOG.info("PIN status: {}", pinStatus);

            if(pinStatus.isNormalPinEntry()){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onPINIsRequired(pinStatus);
                    }
                });
            }
            else if(pinStatus.needsCan()){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onCANIsRequired(false);
                    }
                });
            }

            else if (pinStatus.needsPuk()){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onPUKIsRequired(false);
                    }
                });
            } else if (pinStatus.isDead()){
                String msg = String.format("PIN Status is '%s'.", pinStatus);
                showMessageFragment(msg);
                LOG.error(msg);
                pinMngGui.cancel();
            }

        } catch (InterruptedException ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }
}
