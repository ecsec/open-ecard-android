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

import org.openecard.demo.R;
import org.openecard.demo.activities.EACActivity;
import org.openecard.demo.activities.PINManagementActivity;
import org.openecard.mobile.activation.ConfirmPasswordOperation;


public class PUKInputFragment extends Fragment {

    private ConfirmPasswordOperation op;

    public PUKInputFragment(){
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		final View view = inflater.inflate(R.layout.fragment_puk_input, container, false);

		final EditText pukText = view.findViewById(R.id.pukInput);
		pukText.setEnabled(true);
		pukText.setFocusable(true);

		final Button buttonContinue = view.findViewById(R.id.btnPUKInput);
		buttonContinue.setEnabled(false);

		pukText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				final String puk = getPuk(pukText);
				boolean canContinue = isValidPuk(puk);
				buttonContinue.setEnabled(canContinue);
			}

			@Override
			public void afterTextChanged(Editable s) { }
		});

		buttonContinue.setOnClickListener(v -> {
			final String puk = getPuk(pukText);

			if (isValidPuk(puk)) {
				buttonContinue.setEnabled(false);
				pukText.setEnabled(false);
				pukText.setFocusable(false);

				op.enter(puk);
				getFragmentManager().beginTransaction().replace(R.id.fragment, new UserInfoFragment()).addToBackStack(null).commitAllowingStateLoss();
			}
		});

		return view;
	}

	private boolean isValidPuk(String puk) {
		return puk.length() == 10;
	}

	private String getPuk(EditText pukText) {
		return pukText.getText().toString();
	}

	public void setConfirmPasswordOperation(ConfirmPasswordOperation confirmPasswordOperation) {
        this.op = confirmPasswordOperation;
	}
}
