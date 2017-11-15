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

import android.app.Fragment;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import java.util.List;
import org.openecard.android.R;
import org.openecard.android.lib.activities.AbstractActivationActivity;
import org.openecard.android.fragments.InitFragment;
import org.openecard.android.fragments.PINInputFragment;
import org.openecard.android.fragments.ServerDataFragment;
import org.openecard.gui.android.eac.EacGui;
import org.openecard.gui.android.eac.types.BoxItem;
import org.openecard.gui.android.eac.types.ServerData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Mike Prechtl
 */
public class BindingActivity extends AbstractActivationActivity {

	private static final Logger LOG = LoggerFactory.getLogger(BindingActivity.class);

	public static final String BUNDLE_SERVER_DATA = "ServerData";

	private EacGui eacService;

	///
	/// Basic Methods of an Activity
	///
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_binding);
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
	}

	@Override
	protected void onStart() {
		super.onStart();

		// show InitFragment
		if (findViewById(R.id.fragment) != null) {
			InitFragment fragment = new InitFragment();
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
	/// Methods to exchange data with the fragments.
	///

	public void enterAttributes(List<BoxItem> readAccessAttributes, List<BoxItem> writeAccessAttributes) {
		try {
			// use eac gui service to select attributes
			eacService.selectAttributes(readAccessAttributes, writeAccessAttributes);
			// retrieve pin status from eac gui service
			String status = eacService.getPinStatus();
			if (status.equals("PIN")) {
				// show PINInputFragment
				onPINIsRequired();
			} else {
				String msg = String.format("PIN Status '{0}' isn't supported yet.", status);
				LOG.error(msg);
			}
		} catch (RemoteException ex) {
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
		} catch (RemoteException ex) {
			LOG.error(ex.getMessage(), ex);
		}
	}

	public void onServerDataPresent(ServerData serverData) {
		Fragment fragment = new ServerDataFragment();

		Bundle bundle = new Bundle();
		bundle.putParcelable(BUNDLE_SERVER_DATA, serverData);
		fragment.setArguments(bundle);

		// show ServerDataFragment
		getFragmentManager().beginTransaction()
				.replace(R.id.fragment, fragment).addToBackStack(null).commit();
	}

	public void onPINIsRequired() {
		Fragment fragment = new PINInputFragment();

		// show PINInputFragment
		getFragmentManager().beginTransaction()
				.replace(R.id.fragment, fragment).addToBackStack(null).commit();
	}

	///
	/// Instance which holds the connection to the Eac Gui Service.
	///

	@Override
	public ServiceConnection getServiceConnection() {
		return serviceConnection;
	}

	private final ServiceConnection serviceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName componentName, IBinder service) {
			// Connected to Eac Gui Service
			eacService = EacGui.Stub.asInterface(service);
			try {
				// Get ServerData from Eac Gui Service
				ServerData serverData = eacService.getServerData();
				// show ServerData
				onServerDataPresent(serverData);
			} catch (RemoteException ex) {
				LOG.error(ex.getMessage(), ex);
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			eacService = null;
		}
	};

}
