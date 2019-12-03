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
import android.widget.Button;

import androidx.fragment.app.FragmentActivity;


import org.openecard.demo.R;
import org.openecard.demo.fragments.RedirectFragment;
import org.openecard.demo.fragments.URLInputFragment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Activity which provides an UI to choose what the next step would be, e.g. example authentication or PIN management.
 *
 * @author Mike Prechtl
 * @author Sebastian Schuberth
 */
public class UseCaseSelectorActivity extends FragmentActivity {

	private static final Logger LOG = LoggerFactory.getLogger(UseCaseSelectorActivity.class);

	private static final String DIRECT_ACTIVATION_URL = "https://test.governikus-eid.de:443/Autent-DemoApplication/RequestServlet;?provider=demo_epa_20&redirect=true";
	private static final String TEST_SERVICE_URL = "https://eid.mtg.de/eid-server-demo-app/index.html";

	//indicates if activity stack is thrown away or not
    //because activation URL from outside can only be used once
	private static boolean clearActivityHistory = false;

	@Override
	public void onBackPressed() {

			goToUseCaseSelectorActivity();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_use_case_selector);

		initEACURLInputFragmet();
		registerPinMgmtHandler();

	}

	private void registerPinMgmtHandler(){
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



	public void initEACURLInputFragmet() {
		URLInputFragment fragment = new URLInputFragment();
		fragment.setDefaultTestServerUrl(TEST_SERVICE_URL);
		fragment.setDefaultDirectUrl(DIRECT_ACTIVATION_URL);

		getSupportFragmentManager().beginTransaction()
				.replace(R.id.fragment, fragment).addToBackStack(null).commitAllowingStateLoss();

	}

	public void goToUseCaseSelectorActivity() {
		Intent intent = new Intent(UseCaseSelectorActivity.this, UseCaseSelectorActivity.class);
		startActivity(intent);
		finish();
	}


}
