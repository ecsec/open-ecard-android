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

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.openecard.demo.R;
import org.openecard.demo.activities.EACActivity;
import org.openecard.mobile.activation.ConfirmPasswordOperation;
import org.openecard.mobile.activation.ConfirmPinCanOperation;


/**
 * @author Mike Prechtl
 */
public class PINInputFragment extends Fragment {

	private static final String PERFORM_PIN_INPUT = "Please wait a moment...";
	private static final String PROVIDE_PIN = "Please provide the PIN to the corresponding identity card.";
	private static final String PROVIDE_CAN = "Please provide the CAN to the corresponding identity card.";

	private ConfirmPasswordOperation confirmPasswordOperation = null;
	private ConfirmPinCanOperation confirmTwoPasswordsOperation = null;
	private boolean needCan;
	private int attempt;

	public void setConfirmPasswordOperation(ConfirmPasswordOperation op){
		this.confirmPasswordOperation = op;
	}
	public void setConfirmTwoPasswordsOperation(ConfirmPinCanOperation op){
		this.confirmTwoPasswordsOperation = op;
	}

	public void setNeedCan(boolean needed){
		this.needCan = needed;
	}

	public void setAttempt(int attempt){
		this.attempt = attempt;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.fragment_pin_input, container, false);

		final TextView logLabel = view.findViewById(R.id.txtLog);
		logLabel.setVisibility(View.INVISIBLE);

		final TextView titleLabel = view.findViewById(R.id.pinInputTxtView);

		final EditText pinText = view.findViewById(R.id.pinInput);
		pinText.setEnabled(true);
		pinText.setFocusable(true);

		final EditText canText = view.findViewById(R.id.canInput);
		if (!needCan) {
			canText.setVisibility(View.GONE);
		}
		canText.setEnabled(true);
		canText.setFocusable(true);

		final Button buttonContinue = view.findViewById(R.id.btnPINInput);
		buttonContinue.setEnabled(false);

		pinText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				boolean canContinue = pinText.getText().toString().length() == 6;

				if (canText.getVisibility() == View.VISIBLE) {
					canContinue = canContinue && canText.getText().toString().length() == 6;
				}
				buttonContinue.setEnabled(canContinue);
			}

			@Override
			public void afterTextChanged(Editable s) { }
		});

		canText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				boolean canContinue = pinText.getText().toString().length() == 6;

				if(canText.getVisibility() == View.VISIBLE) {
					canContinue = canContinue && canText.getText().toString().length() == 6;
				}
				buttonContinue.setEnabled(canContinue);
			}

			@Override
			public void afterTextChanged(Editable s) { }
		});


		buttonContinue.setOnClickListener(v -> {
			final Activity activity = getActivity();
			if (activity instanceof EACActivity) {
				final String pin = pinText.getText().toString();
				final String can;
				if (canText.getVisibility() == View.VISIBLE) {
					can = canText.getText().toString();
				} else {
					can = null;
				}

				if (pin.length() == 6) {
					buttonContinue.setEnabled(false);
					pinText.setEnabled(false);
					pinText.setFocusable(false);
					canText.setEnabled(false);
					canText.setFocusable(false);
					logLabel.setText(PERFORM_PIN_INPUT);

					if(!needCan){
						confirmPasswordOperation.enter(pin);
					}else{
						confirmTwoPasswordsOperation.enter(pin, can);
					}
					getFragmentManager().beginTransaction().replace(R.id.fragment, new UserInfoFragment()).addToBackStack(null).commitAllowingStateLoss();
				}
			}
		});

		return view;
	}

}
