package com.crackdress.wordgrab.settings;

import android.app.ProgressDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.crackdress.wordgrab.R;

public class PrivacyPolicy extends AppCompatActivity {

    public static final String POLICY_URL = "https://crackdress.wordgrab/policy";
    WebView webView;

    ProgressDialog progDailog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);
        progDailog = ProgressDialog.show(this, "Loading","Please wait...", true);
        progDailog.setCancelable(false);



        webView = (WebView) findViewById(R.id.wvPolicy);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.setWebViewClient(new WebViewClient(){

            @Override
            public void onPageFinished(WebView view, final String url) {
                progDailog.dismiss();
            }
        });

        webView.loadUrl(POLICY_URL);

    }
}
