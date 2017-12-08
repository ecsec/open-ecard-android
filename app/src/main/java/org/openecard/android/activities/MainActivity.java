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
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import org.openecard.android.R;
import org.openecard.android.ServiceResponseStatusCodes;
import org.openecard.android.system.ConnectionHandler;
import org.openecard.android.system.OpeneCardServiceConnector;
import org.openecard.android.system.ServiceErrorResponse;
import org.openecard.android.system.ServiceWarningResponse;
import org.openecard.android.utils.NfcUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Mike Prechtl
 */
public class MainActivity extends Activity implements ConnectionHandler {

	private static final Logger LOG = LoggerFactory.getLogger(MainActivity.class);

	private OpeneCardServiceConnector mConnection;

	private TextView txtView;
	private Button startBtn;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// set up gui components
		txtView = findViewById(R.id.textView2);
		txtView.setVisibility(View.INVISIBLE);

		// initialize connection to Open eCard App
		mConnection = OpeneCardServiceConnector.createConnection(this);
		mConnection.setConnectionHandler(this);

		startBtn = findViewById(R.id.btnStart);
		startBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (! mConnection.isConnected()) {
					// or start immediately
					mConnection.startService();
				}
			}
		});

		if (mConnection.isConnected()) {
			onConnectionSuccess();
		}
	}

	@Override
	protected void onDestroy() {
		if (mConnection.isConnected()) {
			mConnection.stopService();
		}
		super.onDestroy();
	}

	@Override
	public void onConnectionSuccess() {
		String idsUri = "https://service.skidentity.de/ids/#ctx=idm";
		Intent i = new Intent(getApplicationContext(), IdsActivity.class);
		i.setData(Uri.parse(idsUri));
		startActivity(i);
	}

	@Override
	public void onConnectionFailure(ServiceErrorResponse serviceErrorResponse) {
		txtView.setText(serviceErrorResponse.getMessage());
		txtView.setVisibility(View.VISIBLE);
	}

	@Override
	public void onConnectionFailure(ServiceWarningResponse serviceWarningResponse) {
		if (serviceWarningResponse.getStatusCode() == ServiceResponseStatusCodes.NFC_NOT_ENABLED) {
			// maybe go to nfc settings
			NfcUtils.getInstance().goToNFCSettings(this);
		}
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

}
