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

package org.openecard.demo.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import org.openecard.android.activation.ActivationImplementationInterface;
import org.openecard.demo.R;
import org.openecard.demo.fragments.FailureFragment;
import org.openecard.demo.fragments.RedirectFragment;
import org.openecard.demo.fragments.URLInputFragment;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


/**
 * @author Mike Prechtl
 * @author Sebastian Schuberth
 */
public class IdsActivity extends AppCompatActivity {

	private String defaultTcTokenURL = "https://test.governikus-eid.de:443/Autent-DemoApplication/RequestServlet;?provider=demo_epa_20&redirect=true";
	private Button cancelBtn;

	@Override
	public void onBackPressed() {
		Intent intent = new Intent(IdsActivity.this, MainActivity.class);
		startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ids);

		cancelBtn = findViewById(R.id.cancelBtn);
		cancelBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				onBackPressed();
			}
		});

		if (findViewById(R.id.fragment) != null) {

			Uri uri = getIntent().getData();

			if(uri != null) {

				if((uri.getHost().equals("localhost") || uri.getHost().equals("127.0.0.1")) && uri.getPort() == 24727) {  //activate
					activate(uri.toString());
				} else { //redirect
					showRedirectAddress(getIntent().getData());
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
			activate(actUrl);

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	private void activate(String url)
	{// perform explicit URL Intent to the Activation Activity
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setClass(IdsActivity.this, CustomActivationActivity.class);
		i.setData(Uri.parse(url));
		// add class name for explicit redirect Intent
		i.putExtra(ActivationImplementationInterface.RETURN_CLASS, IdsActivity.class.getName());
		startActivity(i);

		enableCancel();}

	private void showRedirectAddress(Uri address) {
		RedirectFragment fragment = new RedirectFragment();
		fragment.setRedirectUrl(address.toString());

		cancelBtn.setVisibility(View.INVISIBLE);

		getFragmentManager().beginTransaction()
				.replace(R.id.fragment, fragment).addToBackStack(null).commitAllowingStateLoss();
	}


	public void init() {
		URLInputFragment fragment = new URLInputFragment();
		fragment.setDefaultUrl(defaultTcTokenURL);

		getFragmentManager().beginTransaction()
				.replace(R.id.fragment, fragment).addToBackStack(null).commitAllowingStateLoss();

		enableCancel();
	}

	public void enableCancel() {
		cancelBtn.setEnabled(true);
	}
}
