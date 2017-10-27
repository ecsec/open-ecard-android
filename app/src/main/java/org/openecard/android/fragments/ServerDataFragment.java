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

package org.openecard.android.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import org.openecard.android.R;
import org.openecard.android.activities.BindingActivity;
import org.openecard.gui.android.eac.types.ServerData;


/**
 * @author Mike Prechtl
 */
public class ServerDataFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.fragment_server_data, container, false);

		final ServerData serverData = getArguments().getParcelable(BindingActivity.BUNDLE_SERVER_DATA);

		TextView issuerTxtView = view.findViewById(R.id.issuer);
		issuerTxtView.setText(serverData.getIssuer());

		TextView issuerUrlTxtView = view.findViewById(R.id.issuerUrl);
		issuerUrlTxtView.setText(serverData.getIssuerUrl());

		TextView subjectTxtView = view.findViewById(R.id.subject);
		subjectTxtView.setText(serverData.getSubject());

		TextView subjectUrlTxtView = view.findViewById(R.id.subjectURL);
		subjectUrlTxtView.setText(serverData.getSubjectUrl());

		TextView validityTxtView = view.findViewById(R.id.validity);
		validityTxtView.setText(serverData.getValidity());

		Button button = view.findViewById(R.id.btnContinue);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Activity activity = getActivity();
				if (activity instanceof BindingActivity) {
					((BindingActivity) activity).enterAttributes(serverData.getReadAccessAttributes(),
							serverData.getWriteAccessAttributes());
				}
			}
		});

		return view;
	}

}
