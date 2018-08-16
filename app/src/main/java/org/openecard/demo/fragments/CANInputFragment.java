package org.openecard.demo.fragments;

import org.openecard.demo.activities.PINManagementActivity;

public class CANInputFragment extends GenericInputFragment {


    public CANInputFragment(){
        super("Enter CAN:", "CAN");
    }

    @Override
    protected void enterNumber(String number, PINManagementActivity activity) {
        activity.enterCan(number);
    }

    @Override
    protected int lengthOfNumber() {
        return 6;
    }
}