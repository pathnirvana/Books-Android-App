package lk.pitaka.books;

import android.annotation.SuppressLint;
import android.content.Intent;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends AppCompatActivity {
    private WebView myWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myWebView =  findViewById(R.id.mainWebView);

        setupWebView();

        // 4. Handle Back Button
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (myWebView.canGoBack()) {
                    myWebView.goBack();
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        // my code below
        WebViewClient client = new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                try {
                    // facebook links should open in the Facebook app
                    java.net.URL givenUrl = new java.net.URL(url);
                    String host = givenUrl.getHost();
                    if (host.contains("facebook.com") || host.contains("pitaka.lk")) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url));
                        startActivity(intent);
                        return true;
                    }
                } catch (java.net.MalformedURLException e) {
                }
                return false; // open other links in the webview itself
            }
        };

        android.webkit.WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        webSettings.setLayoutAlgorithm(android.webkit.WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        myWebView.setWebViewClient(client);
        myWebView.addJavascriptInterface(new WebAppInterface(this), "Android");

        String webviewLoadUrl = "file:///android_asset/";
        Intent appLinkIntent = getIntent();
        if (appLinkIntent != null && appLinkIntent.getData() != null) { // app link
            // paths eg /books/bauddhayage-athpotha/6-1.html or /books/bauddhayage-athpotha/
            // more eg /books/ /books
            String appLinkPath = appLinkIntent.getData().getPath();
            Log.e("LOG_TAG", "intent path : " + appLinkPath);

            if (appLinkPath.length() > 7) { // if longer than "/books/"
                appLinkPath = appLinkPath.substring(7); // remove /books/
                if (!appLinkPath.endsWith(".html")) { // if no html file specified add the index.html
                    if (!appLinkPath.endsWith("/")) appLinkPath += "/";
                    appLinkPath += "index.html";
                }
                webviewLoadUrl += appLinkPath;
            } else { // /books/ or /books
                webviewLoadUrl += "index.html";
            }
        } else { // app launcher
            webviewLoadUrl += "index.html";
        }

        Log.e("LOG_TAG", "webview Url : " + webviewLoadUrl);
        myWebView.loadUrl(webviewLoadUrl);
        //myWebView.loadUrl("file:///android_asset/app-index.html");
    }
}
