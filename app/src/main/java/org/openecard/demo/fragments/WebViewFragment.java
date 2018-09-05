package org.openecard.demo.fragments;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.openecard.android.activation.ActivationImplementationInterface;
import org.openecard.demo.R;
import org.openecard.demo.activities.CustomActivationActivity;
import org.openecard.demo.activities.UseCaseSelectorActivity;

/**
 * @author Sebastian Schuberth
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class WebViewFragment extends Fragment {

	private String url;

	@Override
	@SuppressLint("SetJavaScriptEnabled")
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.fragment_ids, container, false);

		final WebView webView = view.findViewById(R.id.webView);

		// set up web view settings
		WebSettings settings = webView.getSettings();
		settings.setJavaScriptEnabled(true);
		settings.setDomStorageEnabled(true);

		// add interceptor for eID-Client URLs (match criteria should be more precise in production code)
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
				String activationUri = request.getUrl().toString();
				if (activationUri.contains(":24727/eID-Client")) {
					// perform explicit URL Intent to the Activation Activity
					Intent i = new Intent(Intent.ACTION_VIEW);
					i.setClass(getActivity(), CustomActivationActivity.class);
					i.setData(Uri.parse(activationUri));
					// add class name for explicit redirect Intent
					i.putExtra(ActivationImplementationInterface.RETURN_CLASS, UseCaseSelectorActivity.class.getName());
					startActivity(i);
				}
				return false;
			}
		});

		webView.loadUrl(url);

		return view;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}
