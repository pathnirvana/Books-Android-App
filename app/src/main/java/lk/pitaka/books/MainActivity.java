package lk.pitaka.books;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        WebView myWebView = (WebView) findViewById(R.id.mainWebView);
        android.webkit.WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        webSettings.setLayoutAlgorithm(android.webkit.WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        myWebView.setWebViewClient(client);

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
                webviewLoadUrl = "https://pitaka.lk/books/index.html";
            }
        } else { // app launcher
            webviewLoadUrl += "app-index.html";
        }

        Log.e("LOG_TAG", "webview Url : " + webviewLoadUrl);
        myWebView.loadUrl(webviewLoadUrl);
        //myWebView.loadUrl("file:///android_asset/app-index.html");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        WebView myWebView = (WebView) findViewById(R.id.mainWebView);
        // Check if the key event was the Back button and if there's history
        if ((keyCode == KeyEvent.KEYCODE_BACK) && myWebView.canGoBack()) {
            myWebView.goBack();
            return true;
        }
        // If it wasn't the Back key or there's no web page history, bubble up to the default
        // system behavior (probably exit the activity)
        return super.onKeyDown(keyCode, event);
    }
}
