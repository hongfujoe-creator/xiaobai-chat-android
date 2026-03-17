package com.xiaobai.chat;

import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.CookieManager;
import android.view.KeyEvent;
import androidx.appcompat.app.AppCompatActivity;

/**
 * 小白聊天助手 - 主界面
 * 使用 WebView 加载 OpenClaw 网页界面
 */
public class MainActivity extends AppCompatActivity {
    
    private WebView webView;
    
    // ⚠️ 修改这里：你的 OpenClaw 服务器地址
    // 本地地址：http://127.0.0.1:18789 或 http://localhost:18789
    // 局域网：http://192.168.x.x:18789
    private static final String SERVER_URL = "http://127.0.0.1:18789";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 创建 WebView 全屏显示
        webView = new WebView(this);
        setContentView(webView);
        
        // 配置 WebView
        setupWebView();
        
        // 加载服务器
        webView.loadUrl(SERVER_URL);
    }
    
    private void setupWebView() {
        WebSettings settings = webView.getSettings();
        
        // 启用 JavaScript
        settings.setJavaScriptEnabled(true);
        
        // 启用 DOM 存储
        settings.setDomStorageEnabled(true);
        
        // 启用数据库
        settings.setDatabaseEnabled(true);
        
        // 设置缓存
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        
        // 支持缩放
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        
        // 设置 User-Agent（可选，让服务器知道是移动端）
        settings.setUserAgentString(settings.getUserAgentString() + " XiaoBaiApp/1.0");
        
        // 允许文件访问
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        
        // 启用混合内容（HTTP + HTTPS）
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        
        // 设置 WebViewClient，让链接在 WebView 内打开
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        
        // 启用 Cookie
        CookieManager.getInstance().setAcceptCookie(true);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
        }
    }
    
    @Override
    public void onBackPressed() {
        // 如果 WebView 可以后退，则后退
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 处理返回键
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    
    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroy();
    }
}
