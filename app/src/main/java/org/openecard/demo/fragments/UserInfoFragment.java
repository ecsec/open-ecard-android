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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import org.openecard.demo.R;

import androidx.fragment.app.Fragment;


/**
 * @author Mike Prechtl
 */
public class UserInfoFragment extends Fragment {

	private String txtMsg;
	private boolean confirmActive = false;
	private boolean spinnerVisible = false;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		final View view = inflater.inflate(R.layout.fragment_wait, container, false);
		final Button confirm = view.findViewById(R.id.btnWaitConfirm);
		confirm.setOnClickListener((v -> {
			getActivity().finish();

		}));
		if(confirmActive) {
			confirm.setVisibility(View.VISIBLE);
		}else{
			confirm.setVisibility(View.INVISIBLE);
		}

		final ProgressBar progressBar = view.findViewById(R.id.progressBar);
		progressBar.setVisibility(spinnerVisible ? View.VISIBLE : View.INVISIBLE);



		final TextView textView = view.findViewById(R.id.txtMsg);
		if (txtMsg != null) {
			textView.setText(txtMsg);
		}

		return view;
	}

	public void setSpinner(boolean active){
		this.spinnerVisible = active;
	}


	public void setConfirmBtn(boolean active){
		this.confirmActive = active;

	}
	public void setWaitMessage(String msg) {
		txtMsg = msg;
	}
}
