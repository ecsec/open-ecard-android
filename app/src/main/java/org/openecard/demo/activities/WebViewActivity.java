package org.openecard.demo.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebViewActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		WebView wv = new WebView(this);
		WebSettings sttngs = wv.getSettings();
		sttngs.setJavaScriptEnabled(true);
		wv.setWebViewClient(new WebViewClient(){
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url){
				System.out.println("url");
				return false;
			}
		});
		setContentView(wv);
		wv.loadUrl("http://www.hackzogtum-coburg.de");
	}
}
