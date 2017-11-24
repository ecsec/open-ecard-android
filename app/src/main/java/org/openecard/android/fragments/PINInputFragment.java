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
import android.widget.EditText;
import android.widget.TextView;
import org.openecard.android.R;
import org.openecard.android.activities.BindingActivity;


/**
 * @author Mike Prechtl
 */
public class PINInputFragment extends Fragment {

	private static final String PERFORM_PIN_INPUT = "Please wait a moment...";
	
	private static final String PROVIDE_PIN = "Please provide the PIN to the corresponding identity card.";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.fragment_pin_input, container, false);

		final TextView logLabel = view.findViewById(R.id.txtLog);
		logLabel.setVisibility(View.INVISIBLE);

		final EditText editText = view.findViewById(R.id.pinInput);
		editText.setEnabled(true);
		editText.setFocusable(true);

		final Button buttonContinue = view.findViewById(R.id.btnPINInput);
		buttonContinue.setEnabled(true);
		buttonContinue.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				final Activity activity = getActivity();
				if (activity instanceof BindingActivity) {
					final String pin = editText.getText().toString();
					logLabel.setVisibility(View.VISIBLE);
					if (pin.length() == 6) {
						buttonContinue.setEnabled(false);
						editText.setEnabled(false);
						editText.setFocusable(false);
						logLabel.setText(PERFORM_PIN_INPUT);
						// disable cancel if service is working
						((BindingActivity) activity).disableCancel();
						new Thread(new Runnable() {
							public void run() {
								((BindingActivity) activity).enterPIN(null, pin);
							}
						}).start();
					} else {
						logLabel.setText(PROVIDE_PIN);
					}
				}
			}
		});

		return view;
	}

}
