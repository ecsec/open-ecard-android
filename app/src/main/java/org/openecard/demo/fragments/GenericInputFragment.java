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
import org.openecard.demo.activities.PINManagementActivity;

public abstract class GenericInputFragment extends Fragment {


    private String logMsg;
    private String inputName;
    private String hint;

    public GenericInputFragment(String inputName, String hint){
        this.inputName = inputName;
        this.hint = hint;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_generic_input, container, false);

        final TextView logLabel = view.findViewById(R.id.txtLog);
        final EditText inputField = view.findViewById(R.id.inputField);
        inputField.setHint(hint);

        final TextView inputType = view.findViewById(R.id.typeOfInput);
        inputType.setText(inputName);

        if(logMsg != null) {
            logLabel.setText(logMsg);
        } else {
            logLabel.setVisibility(View.INVISIBLE);
        }


        final Button buttonContinue = view.findViewById(R.id.btnContinue);
        buttonContinue.setEnabled(false);

        inputField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean lengthCorrect = inputField.getText().toString().length() == lengthOfNumber();
                buttonContinue.setEnabled(lengthCorrect);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        buttonContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Activity activity = getActivity();
                if (activity instanceof PINManagementActivity) {

                    final String number = inputField.getText().toString();

                    if (number.length() == lengthOfNumber()) {
                        buttonContinue.setEnabled(false);
                        inputField.setEnabled(false);
                        inputField.setFocusable(false);

                        new Thread(new Runnable() {
                            public void run() {
                                enterNumber(number, (PINManagementActivity)activity);
                            }
                        }).start();
                    }
                }
            }
        });


        return view;

    }

    public void setMessage(String logMsg) {
        this.logMsg = logMsg;
    }

    protected abstract void enterNumber(String number, PINManagementActivity activity);
    protected abstract int lengthOfNumber();
}
