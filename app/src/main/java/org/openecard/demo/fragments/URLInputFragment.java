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

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import org.openecard.demo.R;
import org.openecard.demo.activities.UseCaseSelectorActivity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.Fragment;


/**
 * @author Sebastian Schuberth
 */
public class URLInputFragment extends Fragment {

    private static final Logger LOG = LoggerFactory.getLogger(URLInputFragment.class);

	private String defaultUrl;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.fragment_url_input, container, false);

		final AutoCompleteTextView urlInput = view.findViewById(R.id.edt);
		Button btnWebView = view.findViewById(R.id.btnWebView);
		btnWebView.setEnabled(false);

		urlInput.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				btnWebView.setEnabled(isValidUrl(s.toString()));
			}

			@Override
			public void afterTextChanged(Editable s) { }
		});

		if (defaultUrl != null) {
			urlInput.setText(defaultUrl);
		}

		if(btnWebView != null){
			btnWebView.setOnClickListener(v->{
				String url = urlInput.getText().toString();
				WebViewFragment wvFragment = WebViewFragment.newInstance(url);
				getFragmentManager().beginTransaction().replace(R.id.fragment, wvFragment).addToBackStack(null).commitAllowingStateLoss();
			});
		}

		return view;
	}

	public void setDefaultUrl(String url) {
		if (isValidUrl(url)) {
			defaultUrl = url;
		}
	}

	private boolean isValidUrl(String url) {
		return Patterns.WEB_URL.matcher(url).matches();
	}

}