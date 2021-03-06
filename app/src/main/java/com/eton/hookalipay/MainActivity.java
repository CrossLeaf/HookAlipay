package com.eton.hookalipay;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.eton.hookalipay.xposed.MainHook;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;

public class MainActivity extends AppCompatActivity {
    //    String qrCodeURL = "https://qr.alipay.com/fkx13238jokttv2dzbwf336?t=1587462740354";     // 自行設定金額
    String qrCodeURL = "https://qr.alipay.com/fkx19699yf1mjdmiyi1jjc7";     // 小A給的收款
    MyWebSocketClient client;
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toast.makeText(this, isModuleActive() ? "module is active" : "module not active", Toast.LENGTH_SHORT).show();

        webView = findViewById(R.id.wv_alipay);
        Button broadcastBtn = findViewById(R.id.btn_broadcast);
        Button intentToMyBtn = findViewById(R.id.btn_intent_to_my);
        Button intentToBusinessBtn = findViewById(R.id.btn_intent_to_business);

        intentToMyBtn.setOnClickListener((v) -> intentToMyAlipay());
        intentToBusinessBtn.setOnClickListener((v) -> intentToBuisinessAlipay());
        broadcastBtn.setOnClickListener(v -> broadcastToAlipay());
        initWebSocket();
    }

    @Override
    protected void onDestroy() {
        closeConnect();
        super.onDestroy();
    }

    private void intentToMyAlipay() {
        String qrCodeURL = "https://qr.alipay.com/fkx15333wmsxbsrkla5wzdb?t=1587524197818";   // 17元，暫不支持此種方式，請在支付寶內打開操作
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = null;
        try {
            uri = Uri.parse("alipays://platformapi/startapp?saId=10000007&qrcode=" + URLEncoder.encode(qrCodeURL, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(uri);
        startActivity(intent);
    }

    private void intentToBuisinessAlipay() {
        String qrCodeURL = "https://qr.alipay.com/fkx11610ffw88fueppfky39?t=1587539225274";   // 商家收款
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = null;
        try {
            uri = Uri.parse("alipays://platformapi/startapp?saId=10000007&qrcode=" + URLEncoder.encode(qrCodeURL, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(uri);
        startActivity(intent);
    }

    private void webViewToAlipay() {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        // 設定可以訪問檔案
        webSettings.setAllowFileAccess(true);
        // 設定支援縮放
        webSettings.setBuiltInZoomControls(true);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        // webSettings.setDatabaseEnabled(true);
        // 使用localStorage則必須開啟
        webSettings.setDomStorageEnabled(true);
        webSettings.setGeolocationEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.contains("platformapi/startapp")) {
                    try {
                        Uri uri = Uri.parse(url);
                        Intent intent;
                        intent = Intent.parseUri(url,
                                Intent.URI_INTENT_SCHEME);
                        intent.addCategory("android.intent.category.BROWSABLE");
                        intent.setComponent(null);
                        intent.setSelector(null);
                        startActivity(intent);
                    } catch (Exception e) {
                    }
                } else {
                    view.loadUrl(url);
                }
                return true;
            }
        });
        webView.loadUrl(qrCodeURL);
    }

    private void broadcastToAlipay() {
        Intent intent = getPackageManager().getLaunchIntentForPackage(MainHook.Alipay);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private boolean isModuleActive() {
        return false;
    }

    private void initWebSocket() {
        URI uri = URI.create("ws://10.200.252.185:17803/register-mobile?token=da9a7a0c-968d-4aed-86c1-4f2dda2dc193");
//        URI uri = URI.create("ws://dev-zqb.paradise-soft.com.tw:17803/register-mobile?token=da9a7a0c-968d-4aed-86c1-4f2dda2dc193");
        client = new MyWebSocketClient(uri);
        try {
            client.connectBlocking();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 斷開連接
     */
    private void closeConnect() {
        try {
            if (null != client) {
                client.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            client = null;
        }
    }
}