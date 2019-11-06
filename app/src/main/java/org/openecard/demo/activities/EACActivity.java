/****************************************************************************
 * Copyright (C) 2017-2018 ecsec GmbH.
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
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import org.openecard.android.activation.OpeneCard;
import org.openecard.android.utils.NfcUtils;
import org.openecard.common.util.UrlEncoder;
import org.openecard.demo.R;
import org.openecard.demo.fragments.FailureFragment;
import org.openecard.demo.fragments.PINInputFragment;
import org.openecard.demo.fragments.ServerDataFragment;
import org.openecard.demo.fragments.UserInfoFragment;
import org.openecard.mobile.activation.ActivationController;
import org.openecard.mobile.activation.ActivationResult;
import org.openecard.mobile.activation.ActivationResultCode;
import org.openecard.mobile.activation.ActivationSource;
import org.openecard.mobile.activation.ConfirmAttributeSelectionOperation;
import org.openecard.mobile.activation.ConfirmPasswordOperation;
import org.openecard.mobile.activation.ConfirmTwoPasswordsOperation;
import org.openecard.mobile.activation.ContextManager;
import org.openecard.mobile.activation.ControllerCallback;
import org.openecard.mobile.activation.EacControllerFactory;
import org.openecard.mobile.activation.EacInteraction;
import org.openecard.mobile.activation.NFCOverlayMessageHandler;
import org.openecard.mobile.activation.ServerData;
import org.openecard.mobile.activation.ServiceErrorResponse;
import org.openecard.mobile.activation.StartServiceHandler;
import org.openecard.mobile.activation.StopServiceHandler;
import org.openecard.mobile.ex.ApduExtLengthNotSupported;
import org.openecard.mobile.ex.NfcDisabled;
import org.openecard.mobile.ex.NfcUnavailable;
import org.openecard.mobile.ex.UnableToInitialize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;


/**
 * Implementation of the Activation Activity.
 * The abstract base class takes care of handling the Intents and the Open eCard Stack initialization.
 *
 *
 * @author Mike Prechtl
 * @author Tobias Wich
 * @author Florian Otto
 */
public class EACActivity extends AppCompatActivity {

	private static final Logger LOG = LoggerFactory.getLogger(EACActivity.class);

	public static final String BUNDLE_SERVER_DATA = "ServerData";
	public static final String BUNDLE_TRANSACTION_INFO = "TransactionInfo";


	private Button cancelBtn;
	private OpeneCard oe;
	private ActivationController actController;
	private ContextManager context;
	private EacControllerFactory eacFactory;


	private ControllerCallback ccb = new ControllerCallback() {
		@Override
		public void onStarted() {
			LOG.info("ccb::onStarted was called");
		}

		@Override
		public void onAuthenticationCompletion(ActivationResult activationResult) {
			context.stop(new StopServiceHandler() {
				@Override
				public void onSuccess() {
					LOG.debug("OpenECard framework stopped succesfully");
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

				@Override
				public void onFailure(ServiceErrorResponse serviceErrorResponse) {
					LOG.debug("OpenECard framework stopped with error: {}", serviceErrorResponse);
					actController.cancelAuthentication();
					showUserInfoFragmentWithMessage(serviceErrorResponse.getMessage(), true, false);
				}
			});
		}
	};

	private EacInteraction eacInteraction = new EacInteraction() {
		@Override
		public void onCanRequest(ConfirmPasswordOperation confirmPasswordOperation) {
			LOG.debug("eacInteractionHandler::onCanRequest");
		}

		@Override
		public void onPinRequest(int i, ConfirmPasswordOperation confirmPasswordOperation) {
			LOG.debug("eacInteractionHandler::onPinRequest");
//			confirmPasswordOperation.enter("123123");
			PINInputFragment fragment = new PINInputFragment();
			fragment.setNeedCan(false);
			fragment.setAttempt(i);
			fragment.setConfirmPasswordOperation(confirmPasswordOperation);
			// show PINInputFragment
			getFragmentManager().beginTransaction().replace(R.id.fragment, fragment).addToBackStack(null).commitAllowingStateLoss();

		}

		@Override
		public void onPinCanRequest(ConfirmTwoPasswordsOperation confirmTwoPasswordsOperation) {
			LOG.debug("eacInteractionHandler::onPinCanRequest");

			PINInputFragment fragment = new PINInputFragment();
			fragment.setNeedCan(true);
			fragment.setConfirmTwoPasswordsOperation(confirmTwoPasswordsOperation);
			// show PINInputFragment
			getFragmentManager().beginTransaction().replace(R.id.fragment, fragment).addToBackStack(null).commitAllowingStateLoss();
		}

		@Override
		public void onCardBlocked() {
			LOG.debug("eacInteractionHandler::onCardBlocked");
		}

		@Override
		public void onCardDeactivated() {
			LOG.debug("eacInteractionHandler::onCardDeactivated");
		}

		@Override
		public void onServerData(ServerData serverData, String s, ConfirmAttributeSelectionOperation confirmAttributeSelectionOperation) {
			LOG.info("Server data is present.");

			Bundle bundle = new Bundle();
			bundle.putSerializable(BUNDLE_SERVER_DATA, serverData);
			bundle.putSerializable(BUNDLE_TRANSACTION_INFO, s);

			ServerDataFragment f = new ServerDataFragment();
			f.setArguments(bundle);
			f.setServerDataFragment(confirmAttributeSelectionOperation);

			getFragmentManager().beginTransaction().replace(R.id.fragment, f).addToBackStack(null).commitAllowingStateLoss();
			if (cancelBtn.getVisibility() != View.VISIBLE) {
				runOnUiThread(() -> {
					cancelBtn.setVisibility(View.VISIBLE);
				});
			}

		}

		@Override
		public void onCardInteractionComplete() {
			LOG.debug("eacInteractionHandler::onInteractionComplete");
		}

		@Override
		public void onCardAuthenticationSuccessful() {
			LOG.debug("eacInteractionHandler::onInteractionComplete");
		}

		@Override
		public void requestCardInsertion() {
			LOG.debug("eacInteractionHandler::requestCardInsertion");
			runOnUiThread(() -> {
				showUserInfoFragmentWithMessage("Please provide card",false, false);
			});
			NfcUtils.getInstance().enableNFCDispatch(EACActivity.this);
		}

		@Override
		public void onCardRecognized() {
			LOG.info("Card inserted.");
			runOnUiThread(() -> {
				showUserInfoFragmentWithMessage("Please don't move device or card!",false, true);
			});
		}

		@Override
		public void onCardRecognized(NFCOverlayMessageHandler nfcOverlayMessageHandler) {
			LOG.debug("eacInteractionHandler::onCardRecognized");
		}

		@Override
		public void onCardRemoved() {
			LOG.debug("eacInteractionHandler::onCardRemoved");
		}
	};

	@Override
	public void onBackPressed() {
		//deactivate
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
					eacFactory = activationSource.eacFactory();
					String encodedURL = "http://localhost/eID-Client?tcTokenURL="+ String.valueOf(getIntent().getData());
					actController = eacFactory.create(encodedURL,ccb, eacInteraction);
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

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onPause() {
		LOG.info("Pausing.");
		super.onPause();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		LOG.info("Creating.");
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_custom);

		cancelBtn = findViewById(R.id.cancelBtn);
		cancelBtn.setOnClickListener((view) -> {
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
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		LOG.info("On new intent.");
		super.onNewIntent(intent);
		try {
			oe.onNewIntent(intent);
		} catch (ApduExtLengthNotSupported apduExtLengthNotSupported) {
			LOG.error("Exception during start: {}", apduExtLengthNotSupported);
		}

		showUserInfoFragmentWithMessage("Please wait...", false, true);

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


}
