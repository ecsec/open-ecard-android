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

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import org.openecard.android.R;
import org.openecard.android.fragments.InitFragment;
import org.openecard.android.fragments.PINInputFragment;
import org.openecard.android.fragments.ServerDataFragment;
import org.openecard.android.lib.ServiceErrorResponse;
import org.openecard.android.lib.ServiceWarningResponse;
import org.openecard.android.lib.activities.EacActivity;
import org.openecard.android.lib.ex.BindingTaskStillRunning;
import org.openecard.android.lib.ex.ContextNotInitialized;
import org.openecard.android.lib.intent.binding.IntentBinding;
import org.openecard.android.lib.services.EacServiceConnection;
import org.openecard.android.lib.services.EacServiceConnectionHandler;
import org.openecard.gui.android.eac.types.BoxItem;
import org.openecard.gui.android.eac.types.ServerData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;


/**
 * @author Mike Prechtl
 */
public class BindingActivity extends Activity implements EacServiceConnectionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(BindingActivity.class);

	public static final String BUNDLE_SERVER_DATA = "ServerData";

	private EacServiceConnection mEacGuiConnection;
	private EacActivity eacActivity;

	///
	/// Basic Methods of an Activity
	///

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_binding);

		eacActivity = new EacActivity(this);
		eacActivity.onCreate(savedInstanceState);

		mEacGuiConnection = EacServiceConnection.createConnection(this, getApplicationContext());
	}

	@Override
	protected void onResume() {
		super.onResume();
		eacActivity.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		eacActivity.onPause();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		eacActivity.onNewIntent(intent);
		if (! mEacGuiConnection.isConnected()) {
			mEacGuiConnection.startService();
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		eacActivity.onStart();

		if (findViewById(R.id.fragment) != null) {
			InitFragment fragment = new InitFragment();
			fragment.setArguments(getIntent().getExtras());
			getFragmentManager().beginTransaction()
					.replace(R.id.fragment, fragment).addToBackStack(null).commit();
		}

		HandleRequestTask task = new HandleRequestTask();
		task.execute(eacActivity.getBindingURI(getIntent()));
	}

	private class HandleRequestTask extends AsyncTask<String, Void, Void> {
		@Override
		protected Void doInBackground(String... uri) {
			IntentBinding binding = IntentBinding.getInstance();
			try {
				binding.handleRequest(uri[0]);
			} catch (ContextNotInitialized | BindingTaskStillRunning e) {
				e.printStackTrace();
			}
			return null;
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		eacActivity.onStop();
		eacActivity.cancelRequest();
		if (mEacGuiConnection.isConnected()) {
			mEacGuiConnection.stopService();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		eacActivity.onDestroy();
	}

	///
	/// Methods for interacting with the Eac Service.
	///

	@Override
	public void onServerDataPresent(ServerData serverData) {
		FragmentManager fragmentManager = getFragmentManager();

		Fragment fragment = new ServerDataFragment();

		Bundle bundle = new Bundle();
		bundle.putParcelable(BUNDLE_SERVER_DATA, serverData);
		fragment.setArguments(bundle);

		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.replace(R.id.fragment, fragment);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.commit();
	}

	@Override
	public void onPINIsRequired() {
		FragmentManager fragmentManager = getFragmentManager();

		Fragment fragment = new PINInputFragment();

		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.replace(R.id.fragment, fragment);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.commit();
	}

	@Override
	public void onPINInputSuccess() {
		LOG.info("PIN is correct.");
	}

	@Override
	public void onPINInputFailure() {
		LOG.info("PIN is not correct.");
	}

	@Override
	public void onRemoteError(ServiceErrorResponse serviceErrorResponse) {
		LOG.error(serviceErrorResponse.getMessage());
	}

	///
	/// Methods which show whether the connection was successful established.
	///

	@Override
	public void onConnectionSuccess() {
		LOG.info("Successful connected to Eac Gui Service.");
	}

	@Override
	public void onConnectionFailure(ServiceErrorResponse serviceErrorResponse) {

	}

	@Override
	public void onConnectionFailure(ServiceWarningResponse serviceWarningResponse) {

	}

	@Override
	public void onDisconnectionSuccess() {

	}

	@Override
	public void onDisconnectionFailure(ServiceErrorResponse serviceErrorResponse) {

	}

	@Override
	public void onDisconnectionFailure(ServiceWarningResponse serviceWarningResponse) {

	}

	///
	/// Methods to exchange data with the fragments.
	///

	public void enterAttributes(List<BoxItem> readAttributes, List<BoxItem> writeAttributes) {
		mEacGuiConnection.selectAttributes(readAttributes, writeAttributes);
	}

	public void enterPIN(String pin) {
		mEacGuiConnection.enterPIN(pin);
	}

}
