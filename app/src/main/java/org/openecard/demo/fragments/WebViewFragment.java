package org.openecard.demo.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import org.openecard.demo.R;
import org.openecard.demo.activities.UseCaseSelectorActivity;

import androidx.fragment.app.Fragment;


public class WebViewFragment extends Fragment {

	private WebView wv;
	private String URL;

	public WebViewFragment() {
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
		wv = v.findViewById(R.id.wbViewID);


		Button btn  = v.findViewById(R.id.btnWebViewCancel);
		btn.setOnClickListener(__->{
			Activity activity = getActivity();

			Intent intent = new Intent(activity, UseCaseSelectorActivity.class);
			int flag = Intent.FLAG_ACTIVITY_CLEAR_TOP;
			intent.setFlags(flag);

			startActivity(intent);
			activity.finish();
		});


		wv.setWebViewClient(new WebViewClient(){
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url){
				System.out.println("URLS: " + url);
				if(url.contains("127.0.0.1") || url.contains("localhost")) {
					((UseCaseSelectorActivity)getActivity()).onUrlSelection(url);
					return true;
				}
				else{
					return false;
				}
			}
		});
		wv.loadUrl(this.URL);	// Inflate the layout for this fragment
		return v;
	}


	public void setURL(String url) {
		this.URL = url;
	}


}

