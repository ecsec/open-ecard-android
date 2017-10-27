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

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import org.openecard.android.R;
import org.openecard.android.lib.AppResponse;
import org.openecard.android.lib.AppResponseStatusCodes;
import org.openecard.android.lib.activities.NfcActivity;
import org.openecard.android.lib.services.OpeneCardServiceConnection;
import org.openecard.android.lib.services.ServiceConnectionResponseHandler;
import org.openecard.android.lib.utils.NfcUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Mike Prechtl
 */
public class MainActivity extends NfcActivity implements ServiceConnectionResponseHandler {

	private static final Logger LOG = LoggerFactory.getLogger(MainActivity.class);

	private OpeneCardServiceConnection mConnection;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// initialize connection to Open eCard App
		mConnection = new OpeneCardServiceConnection(this, getApplicationContext());

		Button startBtn = findViewById(R.id.btnStart);
		startBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (! mConnection.isServiceAlreadyStarted()) {
					// or start immediately
					mConnection.startService();
				}
			}
		});
	}

	@Override
	protected void onDestroy() {
		if (mConnection.isServiceAlreadyStarted()) {
			mConnection.stopService();
		}
		super.onDestroy();
	}

	@Override
	public void handleServiceConnectionResponse(AppResponse response) {
		LOG.info("Status: " + response.getStatusCode() + " - Message: " + response.getMessage());
		if (response.getStatusCode() == AppResponseStatusCodes.NFC_NOT_ENABLED) {
			NfcUtils.getInstance().goToNFCSettings(this);
		} else if (response.getStatusCode() == AppResponseStatusCodes.INIT_SUCCESS) {
			// After successful initialization enable NFC
			NfcUtils.getInstance().enableNFCDispatch(this);
		}
	}

}
