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
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.openecard.demo.R;
import org.openecard.demo.activities.UseCaseSelectorActivity;

import androidx.fragment.app.Fragment;


/**
 * @author Mike Prechtl
 */
public class FailureFragment extends Fragment {

	private String txtMsg;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.fragment_failure, container, false);

		final TextView textView = view.findViewById(R.id.txtMsg);
		if (txtMsg != null) {
			textView.setText(txtMsg);
		}

		final Button button = view.findViewById(R.id.btnStartOpeneCardService);
		button.setOnClickListener(v -> {
			Activity activity = getActivity();
			Intent intent = new Intent(activity, UseCaseSelectorActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			activity.finish();
		});

		return view;
	}

	public void setErrorMessage(String error) {
		txtMsg = error;
	}

}
