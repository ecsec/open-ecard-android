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
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import org.openecard.addon.bind.AuxDataKeys;
import org.openecard.addon.bind.BindingResult;
import org.openecard.android.R;
import org.openecard.android.fragments.InitFragment;
import org.openecard.android.fragments.PINInputFragment;
import org.openecard.android.fragments.ServerDataFragment;
import org.openecard.android.interfaces.EacFragmentNavigator;
import org.openecard.android.lib.AppResponse;
import org.openecard.android.lib.AppResponseStatusCodes;
import org.openecard.android.lib.activities.EacActivity;
import org.openecard.android.lib.async.tasks.BindingTaskResponse;
import org.openecard.android.lib.ex.BindingTaskStillRunning;
import org.openecard.android.lib.ex.CardNotPresent;
import org.openecard.android.lib.ex.ContextNotInitialized;
import org.openecard.gui.android.eac.EacGui;
import org.openecard.gui.android.eac.types.BoxItem;
import org.openecard.gui.android.eac.types.ServerData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;


/**
 * @author Mike Prechtl
 */
public class BindingActivity extends EacActivity implements EacFragmentNavigator {

    private static final Logger LOG = LoggerFactory.getLogger(BindingActivity.class);

	public static final String BUNDLE_SERVER_DATA = "ServerData";

	private EacGui mService;

	@Override
	public void enterAttributes(List<BoxItem> readAttributes, List<BoxItem> writeAttributes) {
		FragmentManager fragmentManager = getFragmentManager();

		try {
			mService.selectAttributes(readAttributes, writeAttributes);
			String required = mService.getPinStatus();
			if (required.equals("PIN")) {
				Fragment fragment = new PINInputFragment();
				FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
				fragmentTransaction.replace(R.id.fragment, fragment);
				fragmentTransaction.addToBackStack(null);
				fragmentTransaction.commit();
			}
		} catch (RemoteException e) {
			LOG.error(e.getMessage(), e);
		}
	}

	@Override
	public void showServerData(ServerData serverData) {
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
	public void enterPIN(String pin) {
		try {
			boolean correct = mService.enterPin(null, pin);
			LOG.info("Correct PIN: " + correct);
			// show something... or try again...
		} catch (RemoteException e) {
			LOG.error(e.getMessage(), e);
		}
	}

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_binding);
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (findViewById(R.id.fragment) != null) {
			InitFragment fragment = new InitFragment();
			fragment.setArguments(getIntent().getExtras());
			getFragmentManager().beginTransaction()
					.replace(R.id.fragment, fragment).addToBackStack(null).commit();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		handleBindingRequest();
	}

	@Override
	protected void onStop() {
		super.onStop();
		cancelRequest();
	}

	@Override
	public void setResultOfBindingTask(BindingTaskResponse response) {
		BindingResult result = response.getBindingResult();
		sendResultBasedOnBindingResult(result);
	}

	private void sendResultBasedOnBindingResult(BindingResult result) {
		String location = result.getAuxResultData().get(AuxDataKeys.REDIRECT_LOCATION);
		Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(location));
		startActivity(i);
	}

	@Override
	public void handleServiceConnectionResponse(AppResponse response) {
		// connected to Eac gui service
		mService = getEacGui();
		if (response.getStatusCode() == AppResponseStatusCodes.EAC_SERVICE_CONNECTED) {
			try {
				ServerData data = mService.getServerData();
			 	showServerData(data);
			} catch (RemoteException e) {
				LOG.error(e.getMessage(), e);
			}
		}
	}

	@Override
	public void cardRecognized() {
		LOG.info("Card recognized!");
		handleBindingRequest();
	}

	private void handleBindingRequest() {
		String uri = getBindingURI();
		LOG.info("Binding URI: " + uri);
		if (uri != null) {
			try {
				handleRequest(uri);
				bindEacGui(); // card is here bind to eac gui service
			} catch (BindingTaskStillRunning e) {
				LOG.error("Binding Task is still running.");
			} catch (ContextNotInitialized e) {
				LOG.error("Context not initialized. Open the Open eCard App and start it!");
			} catch (CardNotPresent e) {
				LOG.info("Card not present. Wait til nfc tag is received.");
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
			}
		}
	}

}
