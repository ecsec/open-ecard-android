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

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import org.openecard.android.activation.OpeneCard;
import org.openecard.android.utils.NfcUtils;
import org.openecard.demo.R;
import org.openecard.demo.fragments.CANInputFragment;
import org.openecard.demo.fragments.FailureFragment;
import org.openecard.demo.fragments.PINChangeFragment;
import org.openecard.demo.fragments.PINInputFragment;
import org.openecard.demo.fragments.PUKInputFragment;
import org.openecard.demo.fragments.UserInfoFragment;
import org.openecard.mobile.activation.ActivationController;
import org.openecard.mobile.activation.ActivationResult;
import org.openecard.mobile.activation.ActivationResultCode;
import org.openecard.mobile.activation.ActivationSource;
import org.openecard.mobile.activation.ConfirmAttributeSelectionOperation;
import org.openecard.mobile.activation.ConfirmOldSetNewPasswordOperation;
import org.openecard.mobile.activation.ConfirmPasswordOperation;
import org.openecard.mobile.activation.ConfirmTwoPasswordsOperation;
import org.openecard.mobile.activation.ContextManager;
import org.openecard.mobile.activation.ControllerCallback;
import org.openecard.mobile.activation.EacControllerFactory;
import org.openecard.mobile.activation.EacInteraction;
import org.openecard.mobile.activation.NFCOverlayMessageHandler;
import org.openecard.mobile.activation.PinManagementControllerFactory;
import org.openecard.mobile.activation.PinManagementInteraction;
import org.openecard.mobile.activation.ServerData;
import org.openecard.mobile.activation.ServiceErrorResponse;
import org.openecard.mobile.activation.StartServiceHandler;
import org.openecard.mobile.ex.ApduExtLengthNotSupported;
import org.openecard.mobile.ex.NfcDisabled;
import org.openecard.mobile.ex.NfcUnavailable;
import org.openecard.mobile.ex.UnableToInitialize;
import org.openecard.mobile.ui.PINManagementNavigator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;


public class PINManagementActivity extends AppCompatActivity {

    private static final Logger LOG = LoggerFactory.getLogger(PINManagementActivity.class);
	private View cancelBtn;

	private ActivationController actController;
	private OpeneCard oe;
	private ContextManager context;

	private PinManagementControllerFactory pinMgmtFactory;
	private boolean shouldTriggerNfc;
	private boolean hasTriggeredNfcDispatch;


    @Override
    public void onBackPressed() {
    	finish();
    }
	@Override
	protected void onPause() {
		LOG.info("Pausing.");
		if (hasTriggeredNfcDispatch) {
			NfcUtils.getInstance().disableNFCDispatch(this);
			hasTriggeredNfcDispatch = false;
		}
		super.onPause();
	}

	@Override
	protected void onResume() {
		if (shouldTriggerNfc) {
			NfcUtils.getInstance().enableNFCDispatch(PINManagementActivity.this);
			hasTriggeredNfcDispatch = true;
		}
		super.onResume();
	}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	LOG.info("Creating");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom);

        cancelBtn = findViewById(R.id.cancelBtn);
        cancelBtn.setOnClickListener(view -> {
			LOG.info("Cancel pressed");
			cancelBtn.setEnabled(false);
			cancelBtn.setClickable(false);

			showUserInfoFragmentWithMessage("Cancelling authentication...", false, true);
			showFailureFragment("The User cancelled the authentication procedure, please wait for the process to end.");
			if(actController != null) {
				actController.cancelAuthentication();
			}

        });
		showUserInfoFragmentWithMessage("Please wait...", false, true);
    }
	@Override
	protected void onStart() {
		LOG.info("Starting.");
		this.oe = OpeneCard.createInstance();
		this.context = oe.context(this);
		try {
			this.context.start(new StartServiceHandler() {
				@Override
				public void onSuccess(ActivationSource activationSource) {
					LOG.debug("onSuccess");
					pinMgmtFactory = activationSource.pinManagementFactory();
					actController = pinMgmtFactory.create(new PINManagementActivity.PINMgmtControllerCallback(), new PINManagementActivity.PINMgmtInteractionImp());
				}

				@Override
				public void onFailure(ServiceErrorResponse serviceErrorResponse) {
					LOG.error("Could not start OeC-Framework: {}", serviceErrorResponse);
				}
			});
		} catch (UnableToInitialize unableToInitialize) {
			LOG.error("Exception during start: {}", unableToInitialize);
		} catch (NfcUnavailable nfcUnavailable) {
			LOG.error("Exception during start: {}", nfcUnavailable);
		} catch (NfcDisabled nfcDisabled) {
			LOG.error("Exception during start: {}", nfcDisabled);
		} catch (ApduExtLengthNotSupported apduExtLengthNotSupported) {
			LOG.error("Exception during start: {}", apduExtLengthNotSupported);
		}

		super.onStart();
	}

	private void showFailureFragment(String errorMessage) {
		FailureFragment fragment = new FailureFragment();
		fragment.setErrorMessage(errorMessage);

		runOnUiThread(() -> {
			cancelBtn.setVisibility(View.INVISIBLE);
		});

		// show ServerDataFragment
		LOG.debug("Replace fragment with FailureFragment.");
		getFragmentManager().beginTransaction()
				.replace(R.id.fragment, fragment).addToBackStack(null).commitAllowingStateLoss();

	}

    private void showUserInfoFragmentWithMessage(String msg, boolean showConfirmBtn, boolean showSpinner){
		if (findViewById(R.id.fragment) != null) {
			UserInfoFragment fragment = new UserInfoFragment();
			fragment.setWaitMessage(msg);
			fragment.setConfirmBtn(showConfirmBtn);
			fragment.setSpinner(showSpinner);
			fragment.setArguments(getIntent().getExtras());
			getFragmentManager().beginTransaction()
					.replace(R.id.fragment, fragment).addToBackStack(null).commit();
		}
	}
	@Override
	protected void onNewIntent(Intent intent) {
		LOG.info("On new intent.");
		super.onNewIntent(intent);
		try {
			oe.onNewIntent(this, intent);
		} catch (ApduExtLengthNotSupported apduExtLengthNotSupported) {
			LOG.error("Exception during start: {}", apduExtLengthNotSupported);
		} catch (IOException e) {
			LOG.error("exception during start: {}", e);
		}

		showUserInfoFragmentWithMessage("Please wait...", false, true);

	}
	private class PINMgmtControllerCallback implements ControllerCallback {
		@Override
		public void onStarted() {
			LOG.debug("onStarted");

		}

		@Override
		public void onAuthenticationCompletion(ActivationResult activationResult) {
			PINManagementNavigator d;
			LOG.debug("onAuthenticationCompletion");
			actController = null;
			if(activationResult != null) {
				LOG.debug("onAuthenticationSuccess Result={}", activationResult.getResultCode());
				LOG.debug("onAuthenticationSuccess ResultMinor={}", activationResult.getProcessResultMinor());

				if(activationResult.getResultCode()== ActivationResultCode.OK) {
					showUserInfoFragmentWithMessage("Success", true, false);
				}else{
					showUserInfoFragmentWithMessage("Fail - " + activationResult.getResultCode().toString(), true, false);
				}
			}
		}
	}

	private class PINMgmtInteractionImp implements PinManagementInteraction {


		@Override
		public void onPinChangeable(int i, ConfirmOldSetNewPasswordOperation confirmOldSetNewPasswordOperation) {
			LOG.debug("onPinChangeable");

			PINChangeFragment fragment = new PINChangeFragment();
			fragment.setConfirmPasswordOperation(confirmOldSetNewPasswordOperation);
			fragment.setAttempt(i);
			// show PINInputFragment
			getFragmentManager().beginTransaction().replace(R.id.fragment, fragment).addToBackStack(null).commitAllowingStateLoss();
		}

		@Override
		public void onCanRequired(ConfirmPasswordOperation confirmPasswordOperation) {
			LOG.debug("onCanRequired");
			CANInputFragment fragment = new CANInputFragment();
			fragment.setConfirmPasswordOperation(confirmPasswordOperation);
			// show PINInputFragment
			getFragmentManager().beginTransaction().replace(R.id.fragment, fragment).addToBackStack(null).commitAllowingStateLoss();
		}

		@Override
		public void onPinBlocked(ConfirmPasswordOperation confirmPasswordOperation) {
			LOG.debug("onCanRequired");
			PUKInputFragment fragment = new PUKInputFragment();
			fragment.setConfirmPasswordOperation(confirmPasswordOperation);
			// show PINInputFragment
			getFragmentManager().beginTransaction().replace(R.id.fragment, fragment).addToBackStack(null).commitAllowingStateLoss();
		}

		@Override
		public void requestCardInsertion() {
			LOG.debug("requestCardInsertion");
			LOG.debug("eacInteractionHandler::requestCardInsertion");
			runOnUiThread(() -> {
				showUserInfoFragmentWithMessage("Please provide card",false, false);
			});
			shouldTriggerNfc = true;
			NfcUtils.getInstance().enableNFCDispatch(PINManagementActivity.this);
			hasTriggeredNfcDispatch = true;
		}

		@Override
		public void requestCardInsertion(NFCOverlayMessageHandler nfcOverlayMessageHandler) {
			//this is for ios and should not be called
			LOG.debug("requestCardInsertion");

		}

		@Override
		public void onCardInteractionComplete() {
			LOG.debug("onCardInteractionComplete");

		}

		@Override
		public void onCardRecognized() {
			LOG.info("Card inserted.");
			runOnUiThread(() -> {
				showUserInfoFragmentWithMessage("Please don't move device or card!",false, true);
			});
		}

		@Override
		public void onCardRemoved() {
			LOG.debug("onCardRemoved");

		}
	}




}
