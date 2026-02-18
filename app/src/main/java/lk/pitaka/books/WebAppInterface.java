package lk.pitaka.books;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.util.Log;

public class WebAppInterface {

    private Context mContext;
    private static final String TAG = "WebAppInterface";

    public WebAppInterface(Context c) {
        mContext = c;
    }

    @JavascriptInterface
    public void setStatusBarColor(final String colorHex) {
        if (mContext instanceof Activity) {
            ((Activity) mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Activity activity = (Activity) mContext;
                        int color = android.graphics.Color.parseColor(colorHex);
                        activity.findViewById(R.id.main).setBackgroundColor(color);

                        double luminance = (0.299 * android.graphics.Color.red(color) +
                                0.587 * android.graphics.Color.green(color) +
                                0.114 * android.graphics.Color.blue(color)) / 255;

                        View decorView = activity.getWindow().getDecorView();
                        int flags = decorView.getSystemUiVisibility();

                        if (luminance > 0.5) {
                            // Light background -> Dark icons
                            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                        } else {
                            // Dark background -> Light icons
                            flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                        }
                        decorView.setSystemUiVisibility(flags);

                    } catch (Exception e) {
                        Log.e("LOG_TAG", "Failed to set status bar color: " + e.getMessage());
                    }
                }
            });
        }
    }
}
