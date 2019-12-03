/****************************************************************************
 * Copyright (C) 2018 ecsec GmbH.
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

package org.openecard.demo.fragments;

import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import org.openecard.demo.R;
import org.openecard.demo.activities.UseCaseSelectorActivity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import androidx.fragment.app.Fragment;


/**
 * @author Sebastian Schuberth
 */
public class URLInputFragment extends Fragment {

    private static final Logger LOG = LoggerFactory.getLogger(URLInputFragment.class);

	private String defaultDirectUrl;
	private String defaultTestServerUrl;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.fragment_url_input, container, false);

		final AutoCompleteTextView testServerUrlInput = view.findViewById(R.id.testServiceURL);
		Button btnWebView = view.findViewById(R.id.btnWebView);

		if (defaultTestServerUrl!= null) {
			testServerUrlInput.setText(defaultTestServerUrl);
		}

		if(btnWebView != null){
			btnWebView.setOnClickListener(v->{
				String url = testServerUrlInput.getText().toString();
				WebViewFragment wvFragment = WebViewFragment.newInstance(url);
				getFragmentManager().beginTransaction().replace(R.id.fragment, wvFragment).addToBackStack(null).commitAllowingStateLoss();
			});
		}

		final AutoCompleteTextView directUrlInput = view.findViewById(R.id.directURL);
		Button directEAC = view.findViewById(R.id.directEAC);

		if (defaultDirectUrl!= null) {
			directUrlInput.setText(defaultDirectUrl);
		}

		if(directEAC!= null){
			directEAC.setOnClickListener(v->{
				String url = null;
				try {
					url = "http://localhost/eID-Client?tcTokenURL="+ URLEncoder.encode(directUrlInput.getText().toString(), "UTF-8");
					((UseCaseSelectorActivity)getActivity()).activate(url);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			});
		}

		return view;
	}
	public void setDefaultDirectUrl(String url) {
		if (isValidUrl(url)) {
			defaultDirectUrl = url;
		}
	}

	public void setDefaultTestServerUrl(String url) {
		if (isValidUrl(url)) {
			defaultTestServerUrl = url;
		}
	}

	private boolean isValidUrl(String url) {
		return Patterns.WEB_URL.matcher(url).matches();
	}

}