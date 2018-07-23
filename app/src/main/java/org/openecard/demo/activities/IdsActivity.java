/****************************************************************************
 * Copyright (C) 2017 ecsec GmbH.
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

package org.openecard.demo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import org.openecard.demo.R;
import org.openecard.demo.fragments.WebViewFragment;
import org.openecard.demo.fragments.URLInputFragment;


/**
 * @author Mike Prechtl
 * @author Sebastian Schuberth
 */
public class IdsActivity extends AppCompatActivity {

	private Button cancelBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ids);

		cancelBtn = findViewById(R.id.cancelBtn);
		cancelBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(IdsActivity.this, MainActivity.class);
				startActivity(intent);
			}
		});

		if (findViewById(R.id.fragment) != null) {
			init();
		}

	}

	public void onUrlSelection(String url) {
		WebViewFragment fragment = new WebViewFragment();
		fragment.setUrl(url);
		getFragmentManager().beginTransaction()
				.replace(R.id.fragment, fragment).addToBackStack(null).commitAllowingStateLoss();

		enableCancel();
	}


	public void init() {
		URLInputFragment fragment = new URLInputFragment();
		fragment.setDefaultUrl(getIntent().getData().toString());

		getFragmentManager().beginTransaction()
				.replace(R.id.fragment, fragment).addToBackStack(null).commitAllowingStateLoss();

		enableCancel();
	}

	public void enableCancel() {
		cancelBtn.setEnabled(true);
	}
}
