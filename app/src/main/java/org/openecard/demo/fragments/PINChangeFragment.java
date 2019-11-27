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
import org.openecard.mobile.activation.ConfirmOldSetNewPasswordOperation;
import org.openecard.mobile.activation.ConfirmPinCanNewPinOperation;

import androidx.fragment.app.Fragment;


public class PINChangeFragment extends Fragment {

    private static final String PERFORM_PIN_CHANGE = "Please wait while PIN is changed...";
    private static final String WRONG_PIN = "The entered PIN was wrong, please try again.";
    private static final String MISMATCHING_PINS = "The two new PINs do not match.";


    private boolean needCan = false;
    private EditText pinText;
    private EditText newPin;
    private EditText canText;
    private EditText newPinConfirm;
    private int attempt;
    private ConfirmOldSetNewPasswordOperation op;
    private ConfirmPinCanNewPinOperation confirmPinCanNewPinOperation;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_pin_change, container, false);

        final TextView logLabel = view.findViewById(R.id.txtLog);
        logLabel.setVisibility(View.INVISIBLE);

        pinText = view.findViewById(R.id.pinInput);
        pinText.setEnabled(true);
        pinText.setFocusable(true);
        //pinText.requestFocus();

        newPin = view.findViewById(R.id.newPinInput);
        newPinConfirm = view.findViewById(R.id.newPinInputConfirm);

        final Button buttonContinue = view.findViewById(R.id.btnPINInput);
        buttonContinue.setEnabled(false);

        pinText = view.findViewById(R.id.pinInput);
        canText = view.findViewById(R.id.canInput);
        if (!needCan) {
            canText.setVisibility(View.GONE);
        } else {
            canText.setEnabled(true);
            canText.setFocusable(true);
        }

        TextWatcher textChangeListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                buttonContinue.setEnabled(canContinue());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };

        pinText.addTextChangedListener(textChangeListener);
        newPin.addTextChangedListener(textChangeListener);
        newPinConfirm.addTextChangedListener(textChangeListener);
        canText.addTextChangedListener(textChangeListener);

        buttonContinue.setOnClickListener(v -> {
            final String newPIN = newPin.getText().toString();
            final String newPIN2 = newPinConfirm.getText().toString();

            if (! newPIN.equals(newPIN2)) {
                logLabel.setText(MISMATCHING_PINS);
                logLabel.setVisibility(View.VISIBLE);
                return;
            }

            final String pin = pinText.getText().toString();

            if (pin.length() == 6 || pin.length() == 5) { // for transport PIN
                buttonContinue.setEnabled(false);
                pinText.setEnabled(false);
                pinText.setFocusable(false);
                newPin.setEnabled(false);
                newPin.setFocusable(false);
                newPinConfirm.setEnabled(false);
                newPinConfirm.setFocusable(false);
                canText.setEnabled(false);
                canText.setFocusable(false);
                logLabel.setText(PERFORM_PIN_CHANGE);
                logLabel.setVisibility(View.VISIBLE);

                if (needCan) {
                    confirmPinCanNewPinOperation.enter(
                            pinText.getText().toString(),
                            canText.getText().toString(),
                            newPIN);
                } else {
                    op.enter(pinText.getText().toString(), newPIN);
                }

                getFragmentManager().beginTransaction().replace(R.id.fragment, new UserInfoFragment()).addToBackStack(null).commitAllowingStateLoss();
            }
        });

        return view;
    }

    private boolean canContinue() {
        boolean pinLengthCorrect = pinText.getText().toString().length() == 6
                || pinText.getText().toString().length() == 5; //transport PIN

        boolean newPinLengthCorrect = (newPin.getText().toString().length() == 6
                && newPinConfirm.getText().toString().length() == 6);

        return pinLengthCorrect && newPinLengthCorrect;
    }

    public void setAttempt(int i) {
        this.attempt = i;
    }

    public void setConfirmPasswordOperation(ConfirmOldSetNewPasswordOperation confirmOldSetNewPasswordOperation) {
        this.op = confirmOldSetNewPasswordOperation;
    }

    public void setNeedCan(boolean needCan) {
        this.needCan = needCan;
    }

    public void setConfirmPinCanNewPinOperation(ConfirmPinCanNewPinOperation confirmPinCanNewPinOperation) {
        this.confirmPinCanNewPinOperation = confirmPinCanNewPinOperation;
    }
}
