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
import android.os.StrictMode;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import androidx.fragment.app.FragmentActivity;
import org.openecard.demo.R;
import org.openecard.demo.fragments.WebViewFragment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * Activity which provides an UI to choose what the next step would be, e.g. example authentication or PIN management.
 *
 * @author Mike Prechtl
 * @author Sebastian Schuberth
 */
public class UseCaseSelectorActivity extends FragmentActivity {

	private static final Logger LOG = LoggerFactory.getLogger(UseCaseSelectorActivity.class);

	private static final String DIRECT_ACTIVATION_URL = "https://test.governikus-eid.de/Autent-DemoApplication/samlstationary";
	private static final String TEST_SERVICE_URL = "https://eid.mtg.de/eid-server-demo-app/index.html";


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//THIS IS BAD PRACTICE - this apps purpose is however showcasing the usage of the sdk and this makes things easier to read
		//it is needed for extracting the tcTokenURL in the function below
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);

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
				performEACWithURL(directUrlInput.getText().toString());
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

	private String getTCTokenUrl(String direcUrl) {
		try {
			URL obj = new URL(direcUrl);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("POST");
			con.setInstanceFollowRedirects(false);

			// For POST only - START
			con.setDoOutput(true);
			OutputStream os = con.getOutputStream();
			String postParams = "changeAllNatural=ALLOWED&requestedAttributesEidForm.documentType=ALLOWED&requestedAttributesEidForm.issuingState=ALLOWED&requestedAttributesEidForm.dateOfExpiry=ALLOWED&requestedAttributesEidForm.givenNames=ALLOWED&requestedAttributesEidForm.familyNames=ALLOWED&requestedAttributesEidForm.artisticName=ALLOWED&requestedAttributesEidForm.academicTitle=ALLOWED&requestedAttributesEidForm.dateOfBirth=ALLOWED&requestedAttributesEidForm.placeOfBirth=ALLOWED&requestedAttributesEidForm.nationality=ALLOWED&requestedAttributesEidForm.birthName=ALLOWED&requestedAttributesEidForm.placeOfResidence=ALLOWED&requestedAttributesEidForm.communityID=ALLOWED&requestedAttributesEidForm.residencePermitI=ALLOWED&requestedAttributesEidForm.restrictedId=ALLOWED&ageVerificationForm.ageToVerify=0&ageVerificationForm.ageVerification=PROHIBITED&placeVerificationForm.placeToVerify=02760401100000&placeVerificationForm.placeVerification=PROHIBITED&eidTypesForm.cardCertified=ALLOWED&eidTypesForm.seCertified=ALLOWED&eidTypesForm.seEndorsed=ALLOWED&eidTypesForm.hwKeyStore=ALLOWED&transactionInfo=&levelOfAssurance=BUND_HOCH";
			os.write(postParams.getBytes());
			os.flush();
			os.close();
			// For POST only - END

			int responseCode = con.getResponseCode();

			if (responseCode > 300 && responseCode < 400) {
				String locUrl = con.getHeaderField("Location");
				if (locUrl != null) {
					return locUrl;
				} else {
					throw new RuntimeException("No location received from server.");
				}
			} else {
				throw new RuntimeException("Wrong Status code received from server.");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException(("Failed to fetch localhost link"));
		}
	}

	public void performEACWithURL(String directUrl) {
		LOG.debug("Activation URL: {}", directUrl);
		String tcTokenUrl = getTCTokenUrl(directUrl);

		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setClass(this, EACActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.setData(Uri.parse(tcTokenUrl));

		startActivity(i);
	}
}
