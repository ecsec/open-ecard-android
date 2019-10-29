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
import org.openecard.demo.activities.EACActivity;
import org.openecard.demo.activities.PINManagementActivity;
import org.openecard.demo.activities.UseCaseSelectorActivity;


public class PINBlockedFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_pin_blocked, container, false);

        final Button button = view.findViewById(R.id.btnStartOpeneCardService);
        button.setOnClickListener(v -> ((EACActivity) getActivity()).cancelEacGui());

        final Button toPinManage = view.findViewById(R.id.btnPinManagement);
        toPinManage.setOnClickListener(v -> {
            EACActivity activity = ((EACActivity) getActivity());
            activity.cancelEacGui();
            activity.setStartPinManagementDialog(() -> {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setClass(getActivity(), PINManagementActivity.class);
                i.setData(Uri.parse("/eID-Client?ShowUI=PINManagement"));
                i.putExtra(ActivationImplementationInterface.RETURN_CLASS, UseCaseSelectorActivity.class.getName());
                activity.startActivity(i);
                activity.finish();
            });
        });

        return view;
    }
}
