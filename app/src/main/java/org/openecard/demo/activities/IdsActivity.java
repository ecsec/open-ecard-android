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
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import org.openecard.android.activation.ActivationImplementationInterface;
import org.openecard.demo.R;


/**
 * @author Mike Prechtl
 */
public class IdsActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ids);

		WebView webView = findViewById(R.id.webView);

		// set up web view settings
		WebSettings settings = webView.getSettings();
		settings.setJavaScriptEnabled(true);
		settings.setDomStorageEnabled(true);

		// add interceptor for eID-Client URLs (match criteria should be more precise in production code)
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					String activationUri = request.getUrl().toString();
					if (activationUri.contains(":24727/eID-Client")) {
						// perform explicit URL Intent to the Activation Activity
						Intent i = new Intent(Intent.ACTION_VIEW);
						i.setClass(IdsActivity.this, CustomActivationActivity.class);
						i.setData(Uri.parse(activationUri));
						// add class name for explicit redirect Intent
						i.putExtra(ActivationImplementationInterface.RETURN_CLASS, IdsActivity.class.getName());
						startActivity(i);
					}
				}
				return false;
			}
		});

		setContentView(webView);

		if (getIntent().getData() != null) {
			webView.loadUrl(getIntent().getData().toString());
		}
	}
}
