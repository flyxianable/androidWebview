package com.lyb.basewebview;


import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

import com.lyb.weblib.webview.BaseWebviewActivity;

public class MainActivity extends BaseWebviewActivity {

    WebView webView;

    private String url1 = "https://www.baidu.com";
    private String host = "www.baidu.com";

//    private String url2 = "https://lib.jd.id/richtext/index.html";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        webView = findViewById(R.id.webview);

        goWebUrl(webView, url1);
    }

    @Override
    public void showLoading(boolean isShow) {

    }

    @Override
    public void showLoadFail(boolean isShow) {

    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public String getCookieLan() {
        return "cn";
    }

    @Override
    public String getExpendUA() {
        return "";
    }

    @Override
    public String[] getShieldUrl() {
        return new String[]{"baiduboxlite","baiduboxapp"};
    }

    @Override
    public void callback() {

    }
}