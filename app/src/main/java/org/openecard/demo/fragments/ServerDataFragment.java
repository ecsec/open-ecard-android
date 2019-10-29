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

package org.openecard.demo.fragments;

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
import org.openecard.demo.activities.EACActivity;
import org.openecard.mobile.activation.ConfirmAttributeSelectionOperation;
import org.openecard.mobile.activation.SelectableItem;
import org.openecard.mobile.activation.ServerData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author Mike Prechtl
 */
public class ServerDataFragment extends Fragment {
	private ConfirmAttributeSelectionOperation op;

	public void setServerDataFragment(ConfirmAttributeSelectionOperation op){
		this.op = op;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.fragment_server_data, container, false);

		final ServerData serverData = (ServerData) getArguments().getSerializable(EACActivity.BUNDLE_SERVER_DATA);
		final String transactionInfo = getArguments().getString(EACActivity.BUNDLE_TRANSACTION_INFO);

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

		TextView txInfoTxtView = view.findViewById(R.id.transactionInfo);
		txInfoTxtView.setText(transactionInfo);

		final Map<SelectableItem, CheckBox> readAccessAttributes = new HashMap<>();
		for (SelectableItem selItem : serverData.getReadAccessAttributes()) {
			String readableValue = mapBoxItemNameToReadableValue( selItem.getName());
			if (readableValue != null) {
				CheckBox checkBox = new CheckBox(getActivity().getApplicationContext());
				checkBox.setText(readableValue);
				checkBox.setChecked( selItem.isChecked());
				checkBox.setEnabled(!  selItem.isRequired());
				readAccessAttributes.put( selItem, checkBox);
				layout.addView(checkBox);
			}
		}

		Button button = view.findViewById(R.id.btnContinue);
		button.setOnClickListener(view2 -> {
			List<SelectableItem> readBoxes = serverData.getReadAccessAttributes();
			for (SelectableItem selItem : readBoxes) {
				CheckBox next = readAccessAttributes.get(selItem);
				if (next != null) {
					selItem.setChecked(next.isChecked());
				}
			}

			op.enter(readBoxes, serverData.getWriteAccessAttributes());
		});

		return view;
	}

	private String mapBoxItemNameToReadableValue(String boxItemName) {
		switch (boxItemName) {
			case "DG01":
				return "Document Type";
			case "DG02":
				return "Issuing State";
			case "DG03":
				return "Date of Expiry";
			case "DG04":
				return "Given Names";
			case "DG05":
				return "Family Names";
			case "DG06":
				return "Nom de Plume";
			case "DG07":
				return "Academic Title";
			case "DG08":
				return "Date of Birth";
			case "DG09":
				return "Place of Birth";
			case "DG10":
				return "Nationality";
			case "DG13":
				return "Birth Name";
			case "DG17":
				return "Normal Place of Residence (multiple)";
			case "DG19":
				return "Residence Permit I";
			case "AGE_VERIFICATION":
				return "Age Verification";
			case "COMMUNITY_ID_VERIFICATION":
				return "Address Verification";
			case "INSTALL_QUALIFIED_CERTIFICATE":
				return "Install signature certificate";
			case "RESTRICTED_IDENTIFICATION":
				return "Restricted Identification";
			case "CAN_ALLOWED":
				return "On-Site Verification";
			default:
				return null;
		}
	}

}
