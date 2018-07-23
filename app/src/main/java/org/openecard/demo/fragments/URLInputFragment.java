package org.openecard.demo.fragments;


import android.app.Fragment;
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
import org.openecard.demo.activities.IdsActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Sebastian Schuberth
 */

public class URLInputFragment extends Fragment {

	private String defaultUrl;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.fragment_url_input, container, false);

		final AutoCompleteTextView urlInput = view.findViewById(R.id.edt);
		final Button okBtn = view.findViewById(R.id.btnContinue);
		okBtn.setEnabled(false);

		List<String> urls = new ArrayList<>();
		urls.add("https://test.com");

		ArrayAdapter<String> adapter = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_list_item_1, urls);
		urlInput.setAdapter(adapter);

		urlInput.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				okBtn.setEnabled(isValidUrl(s.toString()));
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});

		if (defaultUrl != null) {
			urlInput.setText(defaultUrl);
			adapter.add(defaultUrl);
		}

		okBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				String url = urlInput.getText().toString();

				if(isValidUrl(url)) {
					((IdsActivity)getActivity()).onUrlSelection(url);
				}
			}
		});

		return view;
	}

	public void setDefaultUrl(String url) {
		if(isValidUrl(url)) {
			defaultUrl = url;
		}
	}

	private boolean isValidUrl(String url) {
		return  Patterns.WEB_URL.matcher(url).matches();
	}
}