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
         * h5选择图片功能及回调处理
         */
        webView.setWebChromeClient(new WebChromeClient() {
            /**
             * 16(Android 4.1.2) <= API <= 20(Android 4.4W.2)回调此方法
             */
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                mUploadCallbackBelow = uploadMsg;
                openPicture();
            }

            /**
             * API >= 21(Android 5.0.1)回调此方法
             */
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                // (1)该方法回调时说明版本API >= 21，此时将结果赋值给 mUploadCallbackAboveL，使之 != null
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
        // 关闭file域
        ws.setAllowFileAccess(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            ws.setAllowFileAccessFromFileURLs(false);
            ws.setAllowUniversalAccessFromFileURLs(false);
        }

        ws.setDatabaseEnabled(true);
        String dir = this.getApplicationContext().getDir("database", Context.MODE_PRIVATE).getPath();
        ws.setGeolocationDatabasePath(dir); //设置定位的数据库路径

        ws.setGeolocationEnabled(true); // 设置开启定位权限
        String currentUa = ws.getUserAgentString();
        ws.setUserAgentString((!TextUtils.isEmpty(currentUa) ? currentUa : "") + getExpendUA());

        // 增加第三方cookie设置
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
        ws.setAllowContentAccess(true);//是否可访问Content Provider的资源，默认值 true
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
            //针对5.0以上, 以下区分处理方法
            if (mUploadCallbackBelow != null) {
                chooseBelow(resultCode, data);
            } else if (mUploadCallbackAboveL != null) {
                chooseAbove(resultCode, data);
            } else {
                Toast.makeText(this, "发生错误", Toast.LENGTH_SHORT).show();
            }
        }
    }


    /**
     * Android API >= 21(Android 5.0) 版本的回调处理
     *
     * @param resultCode 选取文件或拍照的返回码
     * @param data       选取文件或拍照的返回结果
     */
    private void chooseAbove(int resultCode, Intent data) {
        if (RESULT_OK == resultCode) {

            if (data != null) {
                // 这里是针对从文件中选图片的处理, 区别是一个返回的URI, 一个是URI[]
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
     * Android API < 21(Android 5.0)版本的回调处理
     *
     * @param resultCode 选取文件或拍照的返回码
     * @param data       选取文件或拍照的返回结果
     */
    private void chooseBelow(int resultCode, Intent data) {
        if (RESULT_OK == resultCode) {

            if (data != null) {
                // 这里是针对文件路径处理
                Uri uri = data.getData();
                if (uri != null) {
                    mUploadCallbackBelow.onReceiveValue(uri);
                } else {
                    mUploadCallbackBelow.onReceiveValue(null);
                }
            } else {
                // 以指定图像存储路径的方式调起相机，成功后返回data为空
//                mUploadCallbackBelow.onReceiveValue(imageUri);
            }
        } else {
            mUploadCallbackBelow.onReceiveValue(null);
        }
        mUploadCallbackBelow = null;
    }


}
