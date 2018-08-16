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

import org.openecard.android.activation.ActivationImplementationInterface;
import org.openecard.demo.R;
import org.openecard.demo.activities.IdsActivity;
import org.openecard.demo.activities.MainActivity;
import org.openecard.demo.activities.PINManagementActivity;

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
		}

		okBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				String url = urlInput.getText().toString();

				if(isValidUrl(url)) {
					cacheUrl(url);
					((IdsActivity)getActivity()).onUrlSelection(url);
				}
			}
		});

		final Button pinManagement = view.findViewById(R.id.btnPinManagement);
		pinManagement.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setClass(getActivity(), PINManagementActivity.class);
				i.setData(Uri.parse("/eID-Client?ShowUI=PINManagement"));
				i.putExtra(ActivationImplementationInterface.RETURN_CLASS, MainActivity.class.getName());
				startActivity(i);
			}
		});

		return view;
	}

	public void setDefaultUrl(String url) {
		if(isValidUrl(url)) {
			defaultUrl = url;
		}
	}

	private void cacheUrl(String url) {

		if(!urls.contains(url)) {
			adapter.add(url);
			urls.add(url);
			cacheUrlsToFile(urls);
		}
	}

	private boolean isValidUrl(String url) {
		return  Patterns.WEB_URL.matcher(url).matches();
	}

	private List<String> getUrlsFromCache(){
		try {
			List<String> urls = (List<String>) storage.readObject(getActivity().getApplicationContext());
			return  urls;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		return new ArrayList<>();
	}

	private void cacheUrlsToFile(List<String> urls) {
		try {
			storage.writeObject(getActivity().getApplicationContext(), urls);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private final class InternalStorage{

		private final String key = "CACHED_URLS";
		private InternalStorage() {}

		public void writeObject(Context context, Object object) throws IOException {
			FileOutputStream fos = context.openFileOutput(key, Context.MODE_PRIVATE);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(object);
			oos.close();
			fos.close();
		}

		public Object readObject(Context context) throws IOException,
				ClassNotFoundException {
			FileInputStream fis = context.openFileInput(key);
			ObjectInputStream ois = new ObjectInputStream(fis);
			Object object = ois.readObject();
			return object;
		}
	}
}