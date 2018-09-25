/****************************************************************************
 * Copyright (C) 2018 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file is part of the Open eCard App.
 *
 * GNU General Public License Usage
 * This file may be used under the terms of the GNU General Public
 * License version 3.0 as published by the Free Software Foundation
 * and appearing in the file LICENSE.GPL included in the packaging of
 * this file. Please review the following information to ensure the
 * GNU General Public License version 3.0 requirements will be met:
 * http://www.gnu.org/copyleft/gpl.html.
 *
 * Other Usage
 * Alternatively, this file may be used in accordance with the terms
 * and conditions contained in a signed written agreement between
 * you and ecsec GmbH.
 *
 ***************************************************************************/

package org.openecard.demo.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

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

        ActivationImpl() {
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

            // show error
            String errorMsg = buildErrorMsg(result);
            showMessageFragment(errorMsg);
        }

        @Override
        public void onAuthenticationInterrupted(ActivationResult result) {
            LOG.info("Authentication interrupted: {}", result);

            // show error message
            String errorMsg = buildInterruptedMsg(result);
            showMessageFragment(errorMsg);
        }

        @Nullable
        @Override
        public Dialog showCardRemoveDialog() {
            return new AlertDialog.Builder(PINManagementActivity.this)
                    .setTitle("Remove the Card")
                    .setMessage("Please remove the identity card.")
                    .setNeutralButton("Proceed", (dialog, which) -> dialog.dismiss())
                    .create();
        }
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
        cancelBtn.setOnClickListener(view -> {
			LOG.info("Cancel pressed");
			cancelBtn.setEnabled(false);
			cancelBtn.setClickable(false);

			new Thread(() -> {
				if (pinMngGui != null) {
					pinMngGui.cancel();
				}
				activationImpl.cancelAuthentication();
			}).start();
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

        if (pinMngGui == null || findViewById(R.id.fragment) != null) {
            // show WaitFragment
            Fragment fragment = new WaitFragment();
            cancelBtn.setVisibility(View.VISIBLE);
            fragment.setArguments(getIntent().getExtras());
            getFragmentManager().beginTransaction()
                    .replace(R.id.fragment, fragment).addToBackStack(null).commit();
        }
    }

    public void onPINIsRequired(PinStatus status) {
		PINChangeFragment fragment = new PINChangeFragment();
		fragment.setStatus(status);

		// show PINChangeFragment
		getFragmentManager().beginTransaction()
				.replace(R.id.fragment, fragment).addToBackStack(null).commitAllowingStateLoss();
	}

	public void onCANIsRequired(boolean triedBefore) {
        GenericInputFragment fragment = new CANInputFragment();

        if (triedBefore) {
            fragment.setMessage("The entered CAN was wrong, please try again.");
        }

        // show CANInput
        getFragmentManager().beginTransaction()
                .replace(R.id.fragment, fragment).addToBackStack(null).commitAllowingStateLoss();
    }

    public void onPUKIsRequired(boolean triedBefore) {
        GenericInputFragment fragment = new PUKInputFragment();

        if (triedBefore) {
            fragment.setMessage("The entered PUK was wrong, please try again.");
        }

        // show PUKInput
        getFragmentManager().beginTransaction()
                .replace(R.id.fragment, fragment).addToBackStack(null).commitAllowingStateLoss();
    }

    public void enterCan(String can) {
        try {
            boolean canCorrect = pinMngGui.enterCan(can);
            LOG.info("CAN correct: {}", canCorrect);

            if (canCorrect) {
				try {
					onPINIsRequired(pinMngGui.getPinStatus());
				} catch (InterruptedException ex) {
					LOG.error(ex.getMessage(), ex);
				}
            } else {
				onCANIsRequired(true);
            }
        } catch (InterruptedException ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }

    public void enterPUK(String puk) {
        try {
            boolean pukCorrect = pinMngGui.unblockPin(puk);
            LOG.info("PUK correct: {}", pukCorrect);

            if (! pukCorrect){
				onPUKIsRequired(true);
            } else {
				showMessageFragment("PIN was successful unblocked.");
            }
        } catch (InterruptedException ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }

    public void changePin(String oldPin, String newPin) {
        try {
            LOG.info("Perform PIN change...");
            boolean changeSuccessful = pinMngGui.changePin(oldPin, newPin);
            LOG.info("PINChange was successful: {}", changeSuccessful);

            if (! changeSuccessful) {
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

        runOnUiThread(() -> cancelBtn.setEnabled(false));

        // show ServerDataFragment
        getFragmentManager().beginTransaction()
                .replace(R.id.fragment, fragment).addToBackStack(null).commitAllowingStateLoss();
    }

    private void initPinChangeGui() {
        try {
            final PinStatus pinStatus = pinMngGui.getPinStatus();
            LOG.info("PIN status: {}", pinStatus);

            if (pinStatus.isNormalPinEntry()) {
				onPINIsRequired(pinStatus);
            } else if (pinStatus.needsCan()) {
				onCANIsRequired(false);
            } else if (pinStatus.needsPuk()) {
				onPUKIsRequired(false);
            } else if (pinStatus.isDead()) {
                String msg = String.format("PIN Status is '%s'.", pinStatus);
				showMessageFragment(msg);
                LOG.error(msg);
                pinMngGui.cancel();
            }
        } catch (InterruptedException ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }


    ///
    /// methods for building error messages
    ///

    private String buildErrorMsg(ActivationResult result) {
        String msg;
        if (result.getErrorMessage() != null) {
            String errorType = result.getResultCode().name();
            String errorMsg = result.getErrorMessage();
            msg = String.format("During PIN Management an error occurred (%s): %s.", errorType, errorMsg);
        } else if (result.getResultCode() != null) {
            String errorType = result.getResultCode().name();
            msg = String.format("During PIN Management an unknown error occurred (%s).", errorType);
        } else {
            msg = "During PIN Management an unknown error occurred.";
        }
        return msg;
    }

    private String buildInterruptedMsg(ActivationResult result) {
        String msg;
        if (result.getErrorMessage() != null) {
            String errorType = result.getResultCode().name();
            String errorMsg = result.getErrorMessage();
            msg = String.format("PIN Management was interrupted (%s): %s.", errorType, errorMsg);
        } else if (result.getResultCode() != null) {
            String errorType = result.getResultCode().name();
            msg = String.format("PIN Management was interrupted by the user or implicitly by a shutdown of a " +
                    "subsystem or the whole system (%s).", errorType);
        } else {
            msg = "PIN Management was interrupted by the user or implicitly by a shutdown of a subsystem or the whole system.";
        }
        return msg;
    }

}
