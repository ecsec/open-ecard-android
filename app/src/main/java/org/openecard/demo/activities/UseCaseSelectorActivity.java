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
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import org.openecard.android.activation.ActivationImplementationInterface;
import org.openecard.android.system.OpeneCardContext;
import org.openecard.android.system.OpeneCardServiceClientHandler;
import org.openecard.android.system.OpeneCardServiceHandler;
import org.openecard.android.system.ServiceErrorResponse;
import org.openecard.android.system.ServiceWarningResponse;
import org.openecard.demo.R;
import org.openecard.demo.fragments.FailureFragment;
import org.openecard.demo.fragments.RedirectFragment;
import org.openecard.demo.fragments.URLInputFragment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


/**
 * Activity which provides an UI to choose what the next step would be, e.g. example authentication or PIN management.
 *
 * @author Mike Prechtl
 * @author Sebastian Schuberth
 */
public class UseCaseSelectorActivity extends AppCompatActivity {

	private static final Logger LOG = LoggerFactory.getLogger(UseCaseSelectorActivity.class);

	private static final String DEFAULT_TC_TOKEN_URL = "https://test.governikus-eid.de:443/Autent-DemoApplication/RequestServlet;?provider=demo_epa_20&redirect=true";

	private OpeneCardServiceClientHandler serviceClient;
	private Button stopBtn;

	// indicates if activity stack is thrown away or not
    // because activation URL from outside can only be used once
	private static boolean clearActivityHistory = false;

	@Override
	public void onBackPressed() {
		// stop OeC Service if it is running
		if (serviceClient.isInitialized()) {
			serviceClient.stopService();
		} else {
			goToMainActivity();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_use_case_selector);

		serviceClient = new OpeneCardServiceClientHandler(this, new UseCaseSelectorActivity.InitServiceHandler());
		serviceClient.startService();

		stopBtn = findViewById(R.id.stopBtn);
		stopBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				onBackPressed();
			}
		});

		if (findViewById(R.id.fragment) != null) {
			Intent intent = getIntent();
			Uri intentUri = intent.getData();

			if(intentUri != null) {
				if((intentUri.getHost().equals("localhost") || intentUri.getHost().equals("127.0.0.1"))
						&& intentUri.getPort() == 24727) {
					clearActivityHistory = true;
					activate(intentUri.toString());
				} else {
					showRedirectAddress(intentUri);
				}
			} else {
				init();
			}
		}
	}

	public void onUrlSelection(String url) {
		try {
			String encoded = URLEncoder.encode(url, "UTF-8");
			String actUrl = "/eID-Client?tcTokenURL=" + encoded;

			clearActivityHistory = false;
			activate(actUrl);
		} catch (UnsupportedEncodingException ex) {
			String msg = "The character encoding is not supported!";
			LOG.warn(msg, ex);
			throw new RuntimeException(msg, ex);
		}
	}

	private void activate(String url) {
        LOG.debug("Activation URL: {}", url);

		// perform explicit URL Intent to the Activation Activity
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setClass(UseCaseSelectorActivity.this, CustomActivationActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.setData(Uri.parse(url));

		// add class name for explicit redirect Intent
		i.putExtra(ActivationImplementationInterface.RETURN_CLASS, UseCaseSelectorActivity.class.getName());
		startActivity(i);

		enableCancel();
	}

	private void showRedirectAddress(Uri address) {
		RedirectFragment fragment = new RedirectFragment();
		fragment.setRedirectUrl(address.toString());
		fragment.clearHistory(clearActivityHistory);

		stopBtn.setVisibility(View.INVISIBLE);

		getFragmentManager().beginTransaction()
				.replace(R.id.fragment, fragment).addToBackStack(null).commitAllowingStateLoss();
	}

	public void init() {
		URLInputFragment fragment = new URLInputFragment();
		fragment.setDefaultUrl(DEFAULT_TC_TOKEN_URL);

		getFragmentManager().beginTransaction()
				.replace(R.id.fragment, fragment).addToBackStack(null).commitAllowingStateLoss();

		enableCancel();
	}

	public void enableCancel() {
		stopBtn.setEnabled(true);
	}

	public void goToMainActivity() {
		Intent intent = new Intent(UseCaseSelectorActivity.this, MainActivity.class);
		startActivity(intent);
		finish();
	}

	private void showFailureFragment(String errorMessage) {
		FailureFragment fragment = new FailureFragment();
		fragment.setErrorMessage(errorMessage);
		getFragmentManager().beginTransaction()
				.replace(R.id.fragment, fragment).addToBackStack(null).commitAllowingStateLoss();
	}


	///
	/// Handler functions for the initialization or termination of the Open eCard Stack management Android Service
	///

	private class InitServiceHandler implements OpeneCardServiceHandler {

		@Override
		public void onConnectionSuccess(OpeneCardContext ctx) { }

		@Override
		public void onConnectionFailure(ServiceErrorResponse serviceErrorResponse) { }

		@Override
		public void onConnectionFailure(ServiceWarningResponse serviceWarningResponse) { }

		@Override
		public void onDisconnectionSuccess() {
			goToMainActivity();
		}

		@Override
		public void onDisconnectionFailure(ServiceErrorResponse serviceErrorResponse) {
			// should not occur
			String errorMsg = serviceErrorResponse.getMessage();
			LOG.error("Disconnecting from Oec Service failed: {}", errorMsg);
			showFailureFragment(errorMsg);
		}

		@Override
		public void onDisconnectionFailure(ServiceWarningResponse serviceWarningResponse) {
			String warnMsg = serviceWarningResponse.getMessage();
			LOG.warn("Disconnecting from Oec Service failed: {}", warnMsg);
			goToMainActivity();
		}

	}
}
