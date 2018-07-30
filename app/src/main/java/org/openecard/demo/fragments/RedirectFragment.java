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

package org.openecard.demo.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.openecard.demo.R;
import org.openecard.demo.activities.MainActivity;


/**
 * @author Mike Prechtl
 */
public class RedirectFragment extends Fragment {

	private String url;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.fragment_redirect, container, false);

		final TextView textView = view.findViewById(R.id.txtMsg1);
		if (url != null) {
			textView.setText("You would be redirected to: "+url);
		}

		final Button button = view.findViewById(R.id.btnStartOpeneCardService1);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(getActivity(), MainActivity.class);
				startActivity(intent);
			}
		});

		final Button browser = view.findViewById(R.id.btnOpenBrowser);
		browser.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(url));
				startActivity(i);
			}
		});

		return view;
	}

	public void setRedirectUrl(String redirectUrl) {
		url = redirectUrl;
	}

}
