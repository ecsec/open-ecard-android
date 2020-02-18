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


import androidx.fragment.app.Fragment;


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

                    getFragmentManager().beginTransaction().replace(R.id.fragment, new UserInfoFragment()).addToBackStack(null).commitAllowingStateLoss();

                    op.confirmPassword(can);
                }
            }
        });

        return view;

    }

    public void setConfirmPasswordOperation(ConfirmPasswordOperation confirmPasswordOperation) {
        this.op = confirmPasswordOperation;
    }

}
