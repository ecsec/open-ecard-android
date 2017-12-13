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

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.openecard.demo.R;
import org.openecard.demo.activities.CustomActivationActivity;
import org.openecard.gui.android.eac.types.BoxItem;
import org.openecard.gui.android.eac.types.ServerData;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author Mike Prechtl
 */
public class ServerDataFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.fragment_server_data, container, false);

		final ServerData serverData = (ServerData) getArguments().getSerializable(CustomActivationActivity.BUNDLE_SERVER_DATA);

		final LinearLayout layout = view.findViewById(R.id.linearLayout);

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

		final Map<BoxItem, CheckBox> readAccessAttributes = new HashMap<>();
		for (BoxItem boxItem : serverData.getReadAccessAttributes()) {
			String readableValue = mapBoxItemNameToReadableValue(boxItem.getName());
			if (readableValue != null) {
				CheckBox checkBox = new CheckBox(getActivity().getApplicationContext());
				checkBox.setText(readableValue);
				checkBox.setChecked(boxItem.isSelected());
				checkBox.setEnabled(! boxItem.isDisabled());
				readAccessAttributes.put(boxItem, checkBox);
				layout.addView(checkBox);
			}
		}

		Button button = view.findViewById(R.id.btnContinue);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				List<BoxItem> readBoxes = serverData.getReadAccessAttributes();
				for (BoxItem boxItem : readBoxes) {
					CheckBox next = readAccessAttributes.get(boxItem);
					if (next != null) {
						boxItem.setSelected(next.isChecked());
					}
				}

				Activity activity = getActivity();
				if (activity instanceof CustomActivationActivity) {
					// disable cancel if service is working
					((CustomActivationActivity) activity).disableCancel();
					((CustomActivationActivity) activity).enterAttributes(readBoxes,
							serverData.getWriteAccessAttributes());
				}
			}
		});

		return view;
	}

	private String mapBoxItemNameToReadableValue(String boxItemName) {
		switch (boxItemName) {
			case "DG04":
				return "Given Names";
			case "DG05":
				return "Family Names";
			case "DG07":
				return "Academic Title";
			case "DG08":
				return "Date of Birth";
			case "DG17":
				return "Normal Place of Residence (multiple)";
			default:
				return null;
		}
	}

}
