package org.openecard.demo.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.openecard.android.activation.ActivationImplementationInterface;
import org.openecard.demo.R;
import org.openecard.demo.activities.CustomActivationActivity;
import org.openecard.demo.activities.MainActivity;
import org.openecard.demo.activities.PINManagementActivity;

public class PINBlockedFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_pin_blocked, container, false);

        final Button button = view.findViewById(R.id.btnStartOpeneCardService);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), MainActivity.class);
                ((CustomActivationActivity) getActivity()).cancelAll();
                startActivity(intent);
                getActivity().finish();
            }
        });

        final Button toPinManage = view.findViewById(R.id.btnPinManagement);
        toPinManage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setClass(getActivity(), PINManagementActivity.class);
                i.setData(Uri.parse("/eID-Client?ShowUI=PINManagement"));
                i.putExtra(ActivationImplementationInterface.RETURN_CLASS, MainActivity.class.getName());
                ((CustomActivationActivity) getActivity()).cancelAll();
                startActivity(i);
                getActivity().finish();
            }
        });

        return view;
    }
}
