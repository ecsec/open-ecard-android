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

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import org.openecard.android.activation.OpeneCard;
import org.openecard.demo.R;
import org.openecard.demo.fragments.FailureFragment;
import org.openecard.demo.fragments.InitFragment;
import org.openecard.demo.fragments.PINBlockedFragment;
import org.openecard.demo.fragments.PINInputFragment;
import org.openecard.demo.fragments.ServerDataFragment;
import org.openecard.demo.fragments.WaitFragment;
import org.openecard.mobile.activation.ActivationController;
import org.openecard.mobile.activation.ActivationResult;
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

	class ServerDataTransInfo{
		public final String BUNDLE_SERVER_DATA = "ServerData";
		public final String BUNDLE_TRANSACTION_INFO = "TransactionInfo";
		private final Bundle bundle;
		private boolean ti_set;
		private boolean sd_set;
		private ConfirmAttributeSelectionOperation confirmAttributeSelectionOperation;

		public ServerDataTransInfo(){
			this.bundle = new Bundle();
			ti_set=false;
			sd_set=false;
		}
		public void setServerData(ServerData sd, ConfirmAttributeSelectionOperation confirmAttributeSelectionOperation){
			this.bundle.putSerializable(BUNDLE_SERVER_DATA, sd.toString());
			this.confirmAttributeSelectionOperation = confirmAttributeSelectionOperation;
			sd_set = true;
			onComplete();
		}
		public void setTransactionInfo(String transactionInfo){
			this.bundle.putSerializable(BUNDLE_TRANSACTION_INFO, transactionInfo);
			ti_set = true;
			onComplete();
		}

		void onComplete(){
			if(ti_set && sd_set) {

				ServerDataFragment f = new ServerDataFragment();
				f.setArguments(this.bundle);
				f.setServerDataFragment(this.confirmAttributeSelectionOperation);

				getFragmentManager().beginTransaction().replace(R.id.fragment, f).addToBackStack(null).commitAllowingStateLoss();
				if (cancelBtn.getVisibility() != View.VISIBLE) {
					runOnUiThread(() -> {
						cancelBtn.setVisibility(View.VISIBLE);
					});
				}

			}
		}


	}
	private ServerDataTransInfo serverDataTransInfo = new ServerDataTransInfo();

	private Button cancelBtn;
	private OpeneCard oe;
	private ActivationController actController;

	private ControllerCallback ccb = new ControllerCallback() {
		@Override
		public void onStarted() {
			LOG.info("ccb::onStarted was called");
		}

		@Override
		public void onAuthenticationCompletion(ActivationResult activationResult) {
			LOG.debug("onAuthenticationSuccess Result={}", activationResult.getResultCode());
			LOG.debug("onAuthenticationSuccess ResultMinor={}", activationResult.getProcessResultMinor());
		}
	};

	private EacInteraction eacInteraction = new EacInteraction() {
		@Override
		public void onPinRequest(int i, ConfirmPasswordOperation confirmPasswordOperation) {
			LOG.debug("eacInteractionHandler::onPinRequest");
			PINInputFragment fragment = new PINInputFragment();
			fragment.setOnSite(isOnSite);

			// show PINInputFragment
			getFragmentManager().beginTransaction().replace(R.id.fragment, fragment).addToBackStack(null).commitAllowingStateLoss();
		}

		@Override
		public void onPinCanRequest(ConfirmTwoPasswordsOperation confirmTwoPasswordsOperation) {
			LOG.debug("eacInteractionHandler::onPinCanRequest");
			showPINBlockedFragment();
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
		public void onServerData(org.openecard.mobile.activation.ServerData serverData, ConfirmAttributeSelectionOperation confirmAttributeSelectionOperation) {
			LOG.info("Server data is present.");
			serverDataTransInfo.setServerData(serverData,confirmAttributeSelectionOperation);
		}

		@Override
		public void onTransactionInfo(String s) {
			LOG.debug("eacInteractionHandler::onTransactionInfo");
			serverDataTransInfo.setTransactionInfo(s);
		}

		@Override
		public void onInteractionComplete() {
			LOG.debug("eacInteractionHandler::onInteractionComplete");
		}

		@Override
		public void requestCardInsertion() {
			LOG.debug("eacInteractionHandler::requestCardInsertion");
		}

		@Override
		public void onCardRecognized() {
			LOG.info("Card inserted.");
			if (findViewById(R.id.fragment) != null) {
				runOnUiThread(() -> {
					Fragment fragment = new WaitFragment();
					cancelBtn.setVisibility(View.VISIBLE);
					fragment.setArguments(getIntent().getExtras());
					getFragmentManager().beginTransaction()
							.replace(R.id.fragment, fragment).addToBackStack(null).commit();
				});
			}
		}

		@Override
		public void onCardRecognized(NFCOverlayMessageHandler nfcOverlayMessageHandler) {
			LOG.debug("eacInteractionHandler::onCardRecognized");
		}

		@Override
		public void onCardRemoved() {
			LOG.debug("eacInteractionHandler::onCardRemoved");
			LOG.info("Showing the card removal dialog.");
			new AlertDialog.Builder(EACActivity.this)
					.setTitle("Remove the Card")
					.setMessage("Please remove the identity card.")
					.setNeutralButton("Proceed", (dialog, which) -> dialog.dismiss())
					.create();
		}
	};
	private ContextManager context;
	private EacControllerFactory eacFactory;

	private void showPINBlockedFragment(){
		PINBlockedFragment fragment = new PINBlockedFragment();
		getFragmentManager().beginTransaction()
				.replace(R.id.fragment, fragment).addToBackStack(null).commitAllowingStateLoss();
	}

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
					actController = eacFactory.create("URL",ccb, eacInteraction);
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
		LOG.info("Stopping.");
		this.context.stop(new StopServiceHandler() {
			@Override
			public void onSuccess() {
				LOG.info("OeC Framework stopped successfully");
			}

			@Override
			public void onFailure(ServiceErrorResponse serviceErrorResponse) {
				LOG.error("OeC Framework stopped with errors: {}", serviceErrorResponse);
			}
		});
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

			WaitFragment fragment = new WaitFragment();
			fragment.setWaitMessage("Cancelling authentication...");
			fragment.setArguments(getIntent().getExtras());
			getFragmentManager().beginTransaction()
					.replace(R.id.fragment, fragment).addToBackStack(null).commit();

			if(actController!=null) {
				actController.cancelAuthentication();
			}
			showFailureFragment("The User cancelled the authentication procedure, please wait for the process to end.");

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
		LOG.info("Resuming.");
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

		if (findViewById(R.id.fragment) != null) {
			// show InitFragment
			Fragment fragment = new WaitFragment();
			cancelBtn.setVisibility(View.VISIBLE);
			fragment.setArguments(getIntent().getExtras());
			getFragmentManager().beginTransaction()
					.replace(R.id.fragment, fragment).addToBackStack(null).commit();
		}
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
