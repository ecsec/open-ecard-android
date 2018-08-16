package org.openecard.demo.fragments;

import org.openecard.demo.activities.PINManagementActivity;

public class PUKInputFragment extends GenericInputFragment {


    public PUKInputFragment(){
        super("Enter PUK:", "PUK");
    }

    @Override
    protected void enterNumber(String number, PINManagementActivity activity) {
        activity.enterPUK(number);
    }

    @Override
    protected int lengthOfNumber() {
        return 10;
    }
}
