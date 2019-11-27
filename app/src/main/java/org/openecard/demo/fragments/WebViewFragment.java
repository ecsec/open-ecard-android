package org.openecard.demo.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;


import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.openecard.demo.R;



public class WebViewFragment extends Fragment {

	private WebView wv;
	private String URL;

	public WebViewFragment() {
		// Required empty public constructor
	}

	public static WebViewFragment newInstance(String url) {
		WebViewFragment fragment = new WebViewFragment();
		fragment.setURL(url);

		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.fragment_web_view, container, false);
		wv = (WebView) v.findViewById(R.id.webView);
		wv.setWebViewClient(new WebViewClient(){
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url){
				System.out.println("url");
				return false;
			}
		});
		wv.loadUrl("http://www.hackzogtum-coburg.de");	// Inflate the layout for this fragment
		return v;
	}




	public void setURL(String url) {
		this.URL = url;
	}

}

