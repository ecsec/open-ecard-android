/****************************************************************************
 * Copyright (C) 2017 ecsec GmbH.
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

package org.openecard.android.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import java.util.List;
import org.openecard.android.R;
import org.openecard.android.activation.ActivationResult;
import org.openecard.android.activation.AbstractActivationActivity;
import org.openecard.android.fragments.InitFragment;
import org.openecard.android.fragments.PINInputFragment;
import org.openecard.android.fragments.ServerDataFragment;
import org.openecard.gui.android.eac.EacGui;
import org.openecard.gui.android.eac.types.BoxItem;
import org.openecard.gui.android.eac.types.PinStatus;
import org.openecard.gui.android.eac.types.ServerData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Mike Prechtl
 */
public class CustomActivationActivity extends AbstractActivationActivity {

	private static final Logger LOG = LoggerFactory.getLogger(CustomActivationActivity.class);

	public static final String BUNDLE_SERVER_DATA = "ServerData";

	private Button cancelBtn;

	private EacGui eacService;

	///
	/// Basic Methods of an Activity
	///

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_custom);

		cancelBtn = findViewById(R.id.cancelBtn);
		cancelBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (eacService != null) {
					eacService.cancel();
				}
				finish();
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		// if you receive a nfc tag, disable the cancel button until the next fragment comes in
		disableCancel();
	}

	@Override
	protected void onStart() {
		super.onStart();

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
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
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
	/// Methods to exchange data with the fragments.
	///

	public void enterAttributes(List<BoxItem> readAccessAttributes, List<BoxItem> writeAccessAttributes) {
		try {
			// use eac gui service to select attributes
			eacService.selectAttributes(readAccessAttributes, writeAccessAttributes);
			// retrieve pin status from eac gui service
			PinStatus status = eacService.getPinStatus();
			if (status == PinStatus.PIN) {
				// show PINInputFragment
				onPINIsRequired();
			} else {
				String msg = String.format("PIN Status '{0}' isn't supported yet.", status);
				LOG.error(msg);
			}
		} catch (InterruptedException ex) {
			LOG.error(ex.getMessage(), ex);
		}
	}

	public void enterPIN(String can, String pin) {
		try {
			// Retrieve PIN from PINInputFragment and send it to Eac Gui Service
			boolean pinCorrect = eacService.enterPin(can, pin);
			if (pinCorrect) {
				LOG.info("The PIN is correct.");
			} else {
				LOG.info("The PIN isn't correct, the CAN is required.");
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
				.replace(R.id.fragment, fragment).addToBackStack(null).commit();

		enableCancel(); // enable cancel if no action is performed by the Open eCard Service
	}

	public void onPINIsRequired() {
		Fragment fragment = new PINInputFragment();

		// show PINInputFragment
		getFragmentManager().beginTransaction()
				.replace(R.id.fragment, fragment).addToBackStack(null).commit();

		enableCancel(); // enable cancel if no action is performed by the Open eCard Service
	}

	///
	/// Callback to receive the Eac Gui interface which is used to interact with the Open eCard library.
	///

	@Override
	public void onEacIfaceSet(EacGui eacGui) {
		eacService = eacGui;
		try {
			final ServerData serverData = eacService.getServerData();
			// show ServerData on ui thread, async
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

	///
	/// Callbacks where you can open a Dialog which says that the card should be removed.
	///

	@Override
	public Dialog showCardRemoveDialog() {
		AlertDialog dialog = new AlertDialog.Builder(this)
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

	///
	/// Methods which indicate whether the authentication was successful or incorrectly.
	///

	@Override
	public void authenticationFailure(ActivationResult activationResult) {
		LOG.info("Authentication failed: " + activationResult.getResultCode().name());
		// maybe show a message with the failure and then finish
		finish();
	}


}
