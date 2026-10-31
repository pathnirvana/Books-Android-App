package lk.pitaka.books;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.webkit.WebViewAssetLoader;

/**
 * The books and dhammapada sites of pitaka.lk, offline. The pages are in the assets at the same paths as on the web
 * (books/, dhammapada/, tools/unicode/fonts/web/, static/singlish-search/, put there by pitaka.lk's dev/sync-apps.sh)
 * and served at https://appassets.androidplatform.net/ so that the pages work as on the web: search (fetch), the
 * absolute /tools and /static paths, and localStorage (bookmarks, continue reading, settings). Other links open outside.
 */
public class MainActivity extends AppCompatActivity {
    static final String HOST = "appassets.androidplatform.net";
    static final String HOME = "https://" + HOST + "/books/index.html";
    private WebView myWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myWebView = findViewById(R.id.mainWebView);

        setupWebView();
        if (savedInstanceState != null && myWebView.restoreState(savedInstanceState) != null) {
            // back where it was
        } else if (!getPreferences(MODE_PRIVATE).getBoolean(MIGRATED, false)) {
            migrateStorage(startUrl(getIntent()));
        } else {
            myWebView.loadUrl(startUrl(getIntent()));
        }

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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        myWebView.saveState(outState);
    }

    /*
     * the apps before 3.0 showed the pages from file:///android_asset/, so their bookmarks and settings are in the
     * localStorage of file://, which the pages at https://appassets… can not see: read them once from a file:// page
     * and hand them to reader.js (WebAppInterface.takeOldStorage), which adds the ones the new pages do not have
     */
    static final String MIGRATED = "storage-migrated";
    private boolean clearHistoryOnLoad = false;
    @SuppressLint("SetJavaScriptEnabled")
    private void migrateStorage(final String then) {
        myWebView.getSettings().setAllowFileAccess(true);
        myWebView.addJavascriptInterface(new Object() {
            @android.webkit.JavascriptInterface
            public void done(final String json) {
                android.util.Log.i("pitaka", "storage of the old app: " + json.length() + " chars");
                runOnUiThread(() -> {
                    getPreferences(MODE_PRIVATE).edit().putBoolean(MIGRATED, true).apply();
                    WebAppInterface.oldStorage = json;
                    myWebView.removeJavascriptInterface("Migrate");
                    myWebView.getSettings().setAllowFileAccess(false);
                    clearHistoryOnLoad = true; // not back to the file:// page
                    myWebView.loadUrl(then);
                });
            }
        }, "Migrate");
        myWebView.loadDataWithBaseURL("file:///android_asset/", "<script>var o={};try{for(var i=0;i<localStorage.length;i++)" +
                "{var k=localStorage.key(i);o[k]=localStorage.getItem(k)}}catch(e){}Migrate.done(JSON.stringify(o))</script>",
                "text/html", "utf-8", null);
    }

    // a pitaka.lk/books/… or pitaka.lk/dhammapada/… link (when app links are enabled) opens that page, otherwise the books index
    static String startUrl(Intent intent) {
        Uri data = intent != null ? intent.getData() : null;
        String path = data != null ? data.getPath() : null;
        if (path == null || !(path.startsWith("/books/") || path.startsWith("/dhammapada/"))) return HOME;
        if (!path.endsWith(".html")) path += path.endsWith("/") ? "index.html" : "/index.html";
        return "https://" + HOST + path;
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        final WebViewAssetLoader assetLoader = new WebViewAssetLoader.Builder()
                .addPathHandler("/", new WebViewAssetLoader.AssetsPathHandler(this))
                .build();
        myWebView.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return assetLoader.shouldInterceptRequest(request.getUrl());
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (clearHistoryOnLoad && url.contains(HOST)) {
                    clearHistoryOnLoad = false;
                    view.clearHistory();
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri url = request.getUrl();
                if (HOST.equals(url.getHost())) return false; // a page of the app
                try { // the web, the stores, facebook: in their own apps
                    startActivity(new Intent(Intent.ACTION_VIEW, url));
                } catch (ActivityNotFoundException e) {
                    // nothing can open it
                }
                return true;
            }
        });

        if ((getApplicationInfo().flags & android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0) WebView.setWebContentsDebuggingEnabled(true);
        android.webkit.WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAllowFileAccess(false);
        webSettings.setAllowContentAccess(false);
        webSettings.setMediaPlaybackRequiresUserGesture(false); // the next gatha recitation plays when one ends
        myWebView.addJavascriptInterface(new WebAppInterface(this), "Android");
    }
}
