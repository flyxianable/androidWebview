package com.lyb.weblib.webview;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;


import com.lyb.weblib.album.PhotoAlbumUtils;
import com.lyb.weblib.js.JSInterface;
import com.lyb.weblib.utils.RequestCodes;


public abstract class BaseWebviewActivity extends Activity implements JSInterface {



    private ValueCallback<Uri> mUploadCallbackBelow;
    private ValueCallback<Uri[]> mUploadCallbackAboveL;

    public static void startForResult(Activity activity) {
        Intent intent = new Intent(activity, BaseWebviewActivity.class);
        activity.startActivityForResult(intent, RequestCodes.CODE_WEBVIEW);
    }

    public abstract void showLoading(boolean isShow);

    public abstract void showLoadFail(boolean isShow);

    public abstract String getHost();

    public abstract String getCookieLan();

    public abstract String getExpendUA();

    public abstract String[] getShieldUrl();



    private void retryLoad(WebView webView) {
        webView.reload();
        showLoading(true);
        showLoadFail(false);
    }

    private void setCookie(String host) {
        try {

            CookieManager cookieManager = CookieManager.getInstance();

            cookieManager.setCookie(host, getCookieLan());
            CookieSyncManager.getInstance().sync();
        } catch (Exception e) {

        }
    }



    /**
     * @param webView
     */
    public void goWebUrl(WebView webView, String goUrl) {
        initWebViewSetting(webView);
        setCookie(getHost());
        webView.loadUrl(goUrl);
        showLoading(true);

        /**
         * h5?????????????????????????????????
         */
        webView.setWebChromeClient(new WebChromeClient() {
            /**
             * 16(Android 4.1.2) <= API <= 20(Android 4.4W.2)???????????????
             */
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                mUploadCallbackBelow = uploadMsg;
                openPicture();
            }

            /**
             * API >= 21(Android 5.0.1)???????????????
             */
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                // (1)??????????????????????????????API >= 21??????????????????????????? mUploadCallbackAboveL????????? != null
                mUploadCallbackAboveL = filePathCallback;
                openPicture();
                return true;
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if (newProgress == 100) {
                    showLoading(false);
                }
            }
        });


        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if(!baseShouldOverrideUrlLoading(view, url)){
                    super.shouldOverrideUrlLoading(view, url);
                }
                return true;
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                onBaseReceivedError(view, request, error);
            }
        });
    }

    protected boolean baseShouldOverrideUrlLoading(WebView view, String url){
        if(getShieldUrl() == null || getShieldUrl().length == 0){
            return false;
        }
        for(String shieldUrl: getShieldUrl()){
            if(url.startsWith(shieldUrl)){
                return true;
            }
        }

        return false;
    }

    protected void onBaseReceivedError(WebView view, WebResourceRequest request, WebResourceError error){
        Log.v("webivew", "url =" + request.getUrl().getPath());
        showLoadFail(true);
//                view.loadUrl("about:blank");
    }


    private void openPicture() {
//        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
//        i.addCategory(Intent.CATEGORY_OPENABLE);
//        i.setType("image/*");
//        startActivityForResult(Intent.createChooser(i, "Image Chooser"), 100);

        PhotoAlbumUtils.gotoChoiceAlbum(this, 100);
    }


    @SuppressLint("JavascriptInterface")
    private void initWebViewSetting(WebView webView) {
        WebSettings ws = webView.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setSupportZoom(true);
        ws.setDisplayZoomControls(false);
        ws.setBuiltInZoomControls(true);
        ws.setSavePassword(false);
        // ??????file???
        ws.setAllowFileAccess(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            ws.setAllowFileAccessFromFileURLs(false);
            ws.setAllowUniversalAccessFromFileURLs(false);
        }

        ws.setDatabaseEnabled(true);
        String dir = this.getApplicationContext().getDir("database", Context.MODE_PRIVATE).getPath();
        ws.setGeolocationDatabasePath(dir); //??????????????????????????????

        ws.setGeolocationEnabled(true); // ????????????????????????
        String currentUa = ws.getUserAgentString();
        ws.setUserAgentString((!TextUtils.isEmpty(currentUa) ? currentUa : "") + getExpendUA());

        // ???????????????cookie??????
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
        }

        webView.addJavascriptInterface(this, "");
        webView.removeJavascriptInterface("searchBoxJavaBridge_");
        webView.removeJavascriptInterface("accessibility");
        webView.removeJavascriptInterface("accessibilityTraversal");

        ws.setDomStorageEnabled(true);
        ws.setSaveFormData(false);
        ws.setAppCacheEnabled(true);
        ws.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        ws.setUseWideViewPort(true);
        ws.setAllowContentAccess(true);//???????????????Content Provider????????????????????? true
    }


    @Override
    protected void onResume() {
        super.onResume();
        setCookie(getHost());
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            //??????5.0??????, ????????????????????????
            if (mUploadCallbackBelow != null) {
                chooseBelow(resultCode, data);
            } else if (mUploadCallbackAboveL != null) {
                chooseAbove(resultCode, data);
            } else {
                Toast.makeText(this, "????????????", Toast.LENGTH_SHORT).show();
            }
        }
    }


    /**
     * Android API >= 21(Android 5.0) ?????????????????????
     *
     * @param resultCode ?????????????????????????????????
     * @param data       ????????????????????????????????????
     */
    private void chooseAbove(int resultCode, Intent data) {
        if (RESULT_OK == resultCode) {

            if (data != null) {
                // ?????????????????????????????????????????????, ????????????????????????URI, ?????????URI[]
                Uri[] results;
                results = PhotoAlbumUtils.onAlbumUriActivityResult(data);
                mUploadCallbackAboveL.onReceiveValue(results);
            } else {
                mUploadCallbackAboveL.onReceiveValue(new Uri[0]);
            }
        } else {
            mUploadCallbackAboveL.onReceiveValue(null);
        }
        mUploadCallbackAboveL = null;
    }


    /**
     * Android API < 21(Android 5.0)?????????????????????
     *
     * @param resultCode ?????????????????????????????????
     * @param data       ????????????????????????????????????
     */
    private void chooseBelow(int resultCode, Intent data) {
        if (RESULT_OK == resultCode) {

            if (data != null) {
                // ?????????????????????????????????
                Uri uri = data.getData();
                if (uri != null) {
                    mUploadCallbackBelow.onReceiveValue(uri);
                } else {
                    mUploadCallbackBelow.onReceiveValue(null);
                }
            } else {
                // ??????????????????????????????????????????????????????????????????data??????
//                mUploadCallbackBelow.onReceiveValue(imageUri);
            }
        } else {
            mUploadCallbackBelow.onReceiveValue(null);
        }
        mUploadCallbackBelow = null;
    }


}
