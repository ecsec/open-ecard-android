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
import android.app.Dialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import org.openecard.android.activation.ActivationResult;
import org.openecard.android.activation.EacActivationHandler;
import org.openecard.demo.R;
import org.openecard.demo.fragments.FailureFragment;
import org.openecard.demo.fragments.InitFragment;
import org.openecard.demo.fragments.PINBlockedFragment;
import org.openecard.demo.fragments.PINInputFragment;
import org.openecard.demo.fragments.ServerDataFragment;
import org.openecard.demo.fragments.WaitFragment;
import org.openecard.gui.android.eac.EacGui;
import org.openecard.gui.android.eac.types.BoxItem;
import org.openecard.gui.android.eac.types.PinStatus;
import org.openecard.gui.android.eac.types.ServerData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


/**
 * Implementation of the Activation Activity.
 * The abstract base class takes care of handling the Intents and the Open eCard Stack initialization.
 *
 *
 * @author Mike Prechtl
 * @author Tobias Wich
 */
public class CustomActivationActivity extends AppCompatActivity {

	private static final Logger LOG = LoggerFactory.getLogger(CustomActivationActivity.class);

	public static final String BUNDLE_SERVER_DATA = "ServerData";

	private Button cancelBtn;

	private final EacActivationHandler<CustomActivationActivity> activationImpl;
	private EacGui eacGui;

	public CustomActivationActivity() {
		this.activationImpl = new ActivationImpl();
	}


	///
	/// Implementation of the ActivationHandler
	///

	private class ActivationImpl extends EacActivationHandler<CustomActivationActivity> {

		public ActivationImpl() {
			super(CustomActivationActivity.this);
		}

		///
		/// Must implement methods as defined in AbstractActivationActivity
		/// Note that the success case is already fully implemented in AbstractActivationImpl
		///

		// Callback to receive the Eac Gui interface which is used to interact with the Open eCard library.
		@Override
		public void onGuiIfaceSet(EacGui eacGui) {
			CustomActivationActivity.this.eacGui = eacGui;
			try {
				// this one blocks until the data is available, but it's ok as this is run in the background
				final ServerData serverData = CustomActivationActivity.this.eacGui.getServerData();
				// show ServerData on ui thread to move context out of the background
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						onServerDataPresent(serverData);
					}
				});
			} catch (InterruptedException ex) {
				LOG.error(ex.getMessage(), ex);
			}
		}


		// Callbacks where you can open a Dialog which says that the card should be removed.
		@Override
		public Dialog showCardRemoveDialog() {
			AlertDialog dialog = new AlertDialog.Builder(CustomActivationActivity.this)
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


		// Methods which indicate whether the authentication was successful or incorrectly.
		@Override
		public void onAuthenticationFailure(ActivationResult activationResult) {
			LOG.info("Authentication failed: " + activationResult.getResultCode().name());
			if (activationResult.getErrorMessage() != null) {
				showFailureFragment(activationResult.getErrorMessage());
 			} else {
				showFailureFragment("Authentication failed...");
			}
		}

		@Override
		public void onAuthenticationInterrupted(ActivationResult result) {
			LOG.info("authentication interrupted");
			showFailureFragment("User cancelled authentication.");
		}

	}

	@Override
	public void onBackPressed() {
		//deactivate
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

				WaitFragment fragment = new WaitFragment();
				fragment.setWaitMessage("Cancelling authentication...");
				fragment.setArguments(getIntent().getExtras());
				getFragmentManager().beginTransaction()
						.replace(R.id.fragment, fragment).addToBackStack(null).commit();

				Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						if (eacGui != null) {
							eacGui.cancel();
						} else {
							activationImpl.cancelAuthentication();
						}
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


	///
	/// Methods to enable or disable the Cancel Button
	///

	public void enableCancel() {
		cancelBtn.setEnabled(true);
	}

	public void disableCancel() {
		cancelBtn.setEnabled(false);
	}


	///
	/// Methods to exchange data with the fragments (aka the EAC process interaction)
	///

	public void enterAttributes(List<BoxItem> readAccessAttributes, List<BoxItem> writeAccessAttributes) {
		try {
			// use eac gui service to select attributes
			eacGui.selectAttributes(readAccessAttributes, writeAccessAttributes);
			// retrieve pin status from eac gui service
			PinStatus status = eacGui.getPinStatus();
			if (status.isOperational()) {
				// show PINInputFragment
				onPINIsRequired(status);
			} else if(status.equals(PinStatus.BLOCKED)) {
				showPINBlockedFragment();
				//eacGui.cancel();
			} else {
				String msg = String.format("PIN Status is '%s'.", status);
				showFailureFragment(msg);
				LOG.error(msg);
				eacGui.cancel();
			}
		} catch (InterruptedException ex) {
			LOG.error(ex.getMessage(), ex);
		}
	}

	public void onPINIsRequired(PinStatus status) {
		PINInputFragment fragment = new PINInputFragment();
		fragment.setStatus(status);

		// show PINInputFragment
		getFragmentManager().beginTransaction()
				.replace(R.id.fragment, fragment).addToBackStack(null).commitAllowingStateLoss();

		enableCancel(); // enable cancel if no action is performed by the Open eCard Service
	}

	public void enterPIN(String can, String pin) {
		try {
			// Retrieve PIN from PINInputFragment and send it to Eac Gui Service
			boolean pinCorrect = eacGui.enterPin(can, pin);
			if (pinCorrect) {
				LOG.info("The PIN is correct.");
			} else {
				LOG.info("The PIN isn't correct, the CAN is required.");
				final PinStatus status = eacGui.getPinStatus();
				if (status.isOperational()) {
					// show PINInputFragment
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							onPINIsRequired(status);
						}
					});
				} else if(status.equals(PinStatus.BLOCKED)) {
					showPINBlockedFragment();
					//eacGui.cancel();

				} else {
					String msg = String.format("PIN Status is '%s'.", status);
					showFailureFragment(msg);
					LOG.error(msg);
					eacGui.cancel();
				}
			}
		} catch (InterruptedException ex) {
			LOG.error(ex.getMessage(), ex);
		}
	}

	public void onServerDataPresent(ServerData serverData) {
		Fragment fragment = new ServerDataFragment();

		Bundle bundle = new Bundle();
		bundle.putSerializable(BUNDLE_SERVER_DATA, serverData);
		fragment.setArguments(bundle);

		// show ServerDataFragment
		getFragmentManager().beginTransaction()
				.replace(R.id.fragment, fragment).addToBackStack(null).commitAllowingStateLoss();

		enableCancel(); // enable cancel if no action is performed by the Open eCard Service
	}

	private void showFailureFragment(String errorMessage) {
		FailureFragment fragment = new FailureFragment();
		fragment.setErrorMessage(errorMessage);

		cancelBtn.setVisibility(View.INVISIBLE);

		// show ServerDataFragment
		getFragmentManager().beginTransaction()
				.replace(R.id.fragment, fragment).addToBackStack(null).commitAllowingStateLoss();
	}

	private void showPINBlockedFragment(){
		PINBlockedFragment fragment = new PINBlockedFragment();
		getFragmentManager().beginTransaction()
				.replace(R.id.fragment, fragment).addToBackStack(null).commitAllowingStateLoss();
	}

}
