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
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import androidx.fragment.app.FragmentActivity;


import org.openecard.demo.R;
import org.openecard.demo.fragments.WebViewFragment;
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
public class UseCaseSelectorActivity extends FragmentActivity {

	private static final Logger LOG = LoggerFactory.getLogger(UseCaseSelectorActivity.class);

	//can
	//private static final String DIRECT_ACTIVATION_URL = "https://test.governikus-eid.de:443/Autent-DemoApplication/RequestServlet;?provider=demo_epa_can&redirect=true";
	//eac
	private static final String DIRECT_ACTIVATION_URL = "https://test.governikus-eid.de:443/Autent-DemoApplication/RequestServlet;?provider=demo_epa_20&redirect=true";
	private static final String TEST_SERVICE_URL = "https://eid.mtg.de/eid-server-demo-app/index.html";


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_use_case_selector);

		final AutoCompleteTextView testServerUrlInput = findViewById(R.id.testServiceURL);
		if (TEST_SERVICE_URL!= null) {
			testServerUrlInput.setText(TEST_SERVICE_URL);
		}

		Button btnWebView = findViewById(R.id.btnWebView);
		if(btnWebView != null){
			btnWebView.setOnClickListener(v->{
				String url = testServerUrlInput.getText().toString();
				setContentView(R.layout.activity_custom);
				WebViewFragment wvFragment = WebViewFragment.newInstance(url);
				getSupportFragmentManager().beginTransaction().replace(R.id.fragment, wvFragment).addToBackStack(null).commitAllowingStateLoss();

				Button btn  = findViewById(R.id.cancelBtn);
				if(btn != null) {
					btn.setOnClickListener(__ -> {

						Intent intent = new Intent(this, UseCaseSelectorActivity.class);
						int flag = Intent.FLAG_ACTIVITY_CLEAR_TOP;
						intent.setFlags(flag);
						startActivity(intent);
						this.finish();
					});
				}

			});
		}


		final AutoCompleteTextView directUrlInput = findViewById(R.id.directURL);
		if (DIRECT_ACTIVATION_URL!= null) {
			directUrlInput.setText(DIRECT_ACTIVATION_URL);
		}

		Button directEAC = findViewById(R.id.directEAC);
		if(directEAC!= null){
			directEAC.setOnClickListener(v->{
				try {
					String url = "http://localhost/eID-Client?tcTokenURL="+ URLEncoder.encode(directUrlInput.getText().toString(), "UTF-8");
					performEACWithURL(url);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			});
		}

		Button btnPinMgmt = findViewById(R.id.btnPinManagement);
		if(btnPinMgmt!=null) {
			btnPinMgmt.setOnClickListener(v -> {
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setClass(UseCaseSelectorActivity.this, PINManagementActivity.class);
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(i);
			});
		}

	}
	public void performEACWithURL(String url) {
		LOG.debug("Activation URL: {}", url);

		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setClass(this, EACActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.setData(Uri.parse(url));

		startActivity(i);
	}
}
