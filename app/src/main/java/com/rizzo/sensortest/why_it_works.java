package com.rizzo.sensortest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Metodo per visualizzare il file pdf all'interno di una web view
 */
public class why_it_works extends AppCompatActivity {

    WebView webview;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_why_it_works);
        webview = (WebView)findViewById(R.id.webview);
        webview.getSettings().setJavaScriptEnabled(true);
        //Handling Page Navigation
        webview.setWebViewClient(new WebViewClient());
        //Load a URL on WebView
        webview.loadUrl("https://drive.google.com/file/d/1em5KtU7d_BbG7Glvr_IYrpokBgjjZE00/view?usp=sharing");
    }
    /*private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (Uri.parse(url).getHost().equals("stackoverflow.com")) { //Force to open the url in WEBVIEW
                return false;
            }
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
            return true;
        }
    }*/
}