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
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import org.openecard.demo.R;
import org.openecard.demo.activities.PINManagementActivity;
import org.openecard.mobile.activation.ConfirmPasswordOperation;

import android.app.Fragment;


public class CANInputFragment extends Fragment {

    private ConfirmPasswordOperation op;

    public CANInputFragment(){
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        final View view = inflater.inflate(R.layout.fragment_pin_input, container, false);

        final Button buttonContinue = view.findViewById(R.id.btnPINInput);
        buttonContinue.setEnabled(false);

        final EditText canText = view.findViewById(R.id.canInput);
        canText.setEnabled(true);
        canText.setFocusable(true);
        canText.setVisibility(View.VISIBLE);
        canText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            	boolean canContinue = canText.getText().toString().length() == 6;
                buttonContinue.setEnabled(canContinue);
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });


		buttonContinue.setOnClickListener(v -> {
            final Activity activity = getActivity();
            if (activity instanceof PINManagementActivity) {
                final String can = canText.getText().toString();
                if (can.length() == 6) {
                    buttonContinue.setEnabled(false);
                    canText.setEnabled(false);
                    canText.setFocusable(false);
                    op.enter(can);
                    getFragmentManager().beginTransaction().replace(R.id.fragment, new UserInfoFragment()).addToBackStack(null).commitAllowingStateLoss();
                }
            }
        });

        return view;

    }

    public void setConfirmPasswordOperation(ConfirmPasswordOperation confirmPasswordOperation) {
        this.op = confirmPasswordOperation;
    }

}
