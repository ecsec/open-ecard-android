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
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.openecard.demo.R;
import org.openecard.demo.activities.CustomActivationActivity;
import org.openecard.gui.android.eac.types.PinStatus;


/**
 * @author Mike Prechtl
 */
public class PINInputFragment extends Fragment {

	private static final String PERFORM_PIN_INPUT = "Please wait a moment...";
	private static final String PROVIDE_PIN = "Please provide the PIN to the corresponding identity card.";
	private static final String WRONG_PIN = "The entered PIN was wrong, please try again.";
	private static final String NEED_CAN = "The entered PIN was wrong, please try again and also enter your CAN.";

	private PinStatus status;
	private EditText pinTextField;

	public void setStatus(PinStatus status) {
		this.status = status;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.fragment_pin_input, container, false);

		final TextView logLabel = view.findViewById(R.id.txtLog);
		logLabel.setVisibility(View.INVISIBLE);

		final EditText pinText = view.findViewById(R.id.pinInput);
		pinText.setEnabled(true);
		pinText.setFocusable(true);
		//pinText.requestFocus();

		final EditText canText = view.findViewById(R.id.canInput);
		if (status != null) {
			if (!status.needsCan()) {
				canText.setVisibility(View.GONE);

				if (status == PinStatus.RC2) {
					logLabel.setVisibility(View.VISIBLE);
					logLabel.setText(WRONG_PIN);
				} else if (status == PinStatus.RC3) {
					logLabel.setVisibility(View.VISIBLE);
					logLabel.setText(PROVIDE_PIN);
				}

			} else {
				logLabel.setVisibility(View.VISIBLE);
				logLabel.setText(NEED_CAN);
			}
		}
		canText.setEnabled(true);
		canText.setFocusable(true);

		final Button buttonContinue = view.findViewById(R.id.btnPINInput);
		buttonContinue.setEnabled(true);

		pinText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {


				boolean pinLengthCorrect = pinText.getText().toString().length() == 6;
				boolean canContinue = pinLengthCorrect;

				if(canText.getVisibility() == View.VISIBLE) {
					canContinue = canContinue && canText.getText().toString().length() == 6;
				}

				buttonContinue.setEnabled(canContinue);
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});

		canText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {


				boolean pinLengthCorrect = pinText.getText().toString().length() == 6;
				boolean canContinue = pinLengthCorrect;

				if(canText.getVisibility() == View.VISIBLE) {
					canContinue = canContinue && canText.getText().toString().length() == 6;
				}

				buttonContinue.setEnabled(canContinue);
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});


		buttonContinue.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				final Activity activity = getActivity();
				if (activity instanceof CustomActivationActivity) {
					final String pin = pinText.getText().toString();
					final String can;
					if (canText.getVisibility() == View.VISIBLE) {
						can = canText.getText().toString();
					} else {
						can = null;
					}

					logLabel.setVisibility(View.VISIBLE);
					if (pin.length() == 6) {
						buttonContinue.setEnabled(false);
						pinText.setEnabled(false);
						pinText.setFocusable(false);
						canText.setEnabled(false);
						canText.setFocusable(false);
						logLabel.setText(PERFORM_PIN_INPUT);
						// disable cancel if service is working
						((CustomActivationActivity) activity).disableCancel();
						new Thread(new Runnable() {
							public void run() {
								((CustomActivationActivity) activity).enterPIN(can, pin);
							}
						}).start();
					}
				}
			}
		});

		return view;
	}

}
