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
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import org.openecard.demo.R;
import org.openecard.demo.activities.UseCaseSelectorActivity;
import org.openecard.demo.activities.PINManagementActivity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;


/**
 * @author Sebastian Schuberth
 */
public class URLInputFragment extends Fragment {

    private static final Logger LOG = LoggerFactory.getLogger(URLInputFragment.class);

	private String defaultUrl;
	private final InternalStorage storage = new InternalStorage();
	private List<String> urls;
	private ArrayAdapter<String> adapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.fragment_url_input, container, false);

		final AutoCompleteTextView urlInput = view.findViewById(R.id.edt);
		final Button okBtn = view.findViewById(R.id.btnContinue);
		okBtn.setEnabled(false);

		urls = getUrlsFromCache();
		adapter = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_list_item_1, urls);
		urlInput.setAdapter(adapter);

		urlInput.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				okBtn.setEnabled(isValidUrl(s.toString()));
			}

			@Override
			public void afterTextChanged(Editable s) { }
		});

		if (defaultUrl != null) {
			urlInput.setText(defaultUrl);
		}

		okBtn.setOnClickListener(v -> {
			String url = urlInput.getText().toString();

			if (isValidUrl(url)) {
				cacheUrl(url);
				((UseCaseSelectorActivity)getActivity()).onUrlSelection(url);
			}
		});

//		final Button pinManagement = view.findViewById(R.id.btnPinManagement);
//		pinManagement.setOnClickListener(v -> {
//			Intent i = new Intent(Intent.ACTION_VIEW);
//			i.setClass(getActivity(), PINManagementActivity.class);
//			i.setData(Uri.parse("/eID-Client?ShowUI=PINManagement"));
//			i.putExtra(ActivationImplementationInterface.RETURN_CLASS, UseCaseSelectorActivity.class.getName());
//			startActivity(i);
//		});

		return view;
	}

	public void setDefaultUrl(String url) {
		if (isValidUrl(url)) {
			defaultUrl = url;
		}
	}

	private void cacheUrl(String url) {
		if (! urls.contains(url)) {
			adapter.add(url);
			urls.add(url);
			cacheUrlsToFile(urls);
		}
	}

	private boolean isValidUrl(String url) {
		return Patterns.WEB_URL.matcher(url).matches();
	}

    @SuppressWarnings("unchecked")
	private List<String> getUrlsFromCache() {
		try {
			return new ArrayList<>((List<String>) storage.readObject(getActivity().getApplicationContext()));
		} catch (IOException | ClassNotFoundException e) {
			String msg = "Unable to retrieve cached urls from internal storage.";
			LOG.error(msg);
		}
		return new ArrayList<>();
	}

	private void cacheUrlsToFile(List<String> urls) {
		try {
			storage.writeObject(getActivity().getApplicationContext(), urls);
		} catch (IOException e) {
			String msg = "Unable to store url in internal storage.";
			LOG.error(msg);
		}
	}

	private final class InternalStorage {

		private final String key = "CACHED_URLS";

        private InternalStorage() {}

        void writeObject(Context context, Object object) throws IOException {
			FileOutputStream fos = context.openFileOutput(key, Context.MODE_PRIVATE);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(object);
			oos.close();
			fos.close();
		}

        Object readObject(Context context) throws IOException, ClassNotFoundException {
			FileInputStream fis = context.openFileInput(key);
			ObjectInputStream ois = new ObjectInputStream(fis);
            return ois.readObject();
		}
	}
}