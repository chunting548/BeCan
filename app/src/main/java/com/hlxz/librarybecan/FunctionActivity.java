package com.hlxz.librarybecan;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import static android.view.View.SCROLLBARS_OUTSIDE_OVERLAY;

public class FunctionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_function);

        WebView webView = (WebView) findViewById(R.id.web_library);
        webView.requestFocus();
        WebSettings websettings = webView.getSettings();
        websettings.setSupportZoom(true);
        websettings.setBuiltInZoomControls(true);
        websettings.setDisplayZoomControls(false);
        websettings.setJavaScriptEnabled(true);
        websettings.setAppCacheEnabled(true);
        websettings.setSaveFormData(true);
        websettings.setAllowFileAccess(true);
        websettings.setDomStorageEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());

        String url = "http://www.lib.pu.edu.tw/mobile/";
        webView.loadUrl(url);

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation_bar);
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(3);
        menuItem.setChecked(true);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.navigation_navigation:
                        Intent intent_navigation = new Intent(FunctionActivity.this, MainActivity.class);
                        startActivity(intent_navigation);
                        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                        break;
                    case R.id.navigation_search:
                        Intent intent_search = new Intent(FunctionActivity.this, SearchActivity.class);
                        startActivity(intent_search);
                        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                        break;
                    case R.id.navigation_tutorial:
                        Intent intent_tutorial = new Intent(FunctionActivity.this, TutorialActivity.class);
                        startActivity(intent_tutorial);
                        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                        break;
                    case R.id.navigation_function:
                        break;
                    case R.id.navigation_adjust:
                        Intent intent_adjust = new Intent(FunctionActivity.this, AdjustActivity.class);
                        startActivity(intent_adjust);
                        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                        break;
                }
                return false;
            }
        });
    }

    public void library(View view){
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://webpac.lib.pu.edu.tw/webpac/webpacIndex.jsp"));
        startActivity(browserIntent);
    }
    public void signup(View view){
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://activity.pu.edu.tw/main.php"));
        startActivity(browserIntent);
    }
    WebViewClient mWebViewClient = new WebViewClient() {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.d("CHECK URL", "url= " + url);

            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            // TODO Auto-generated method stub
            super.onPageFinished(view, url);
        }
    };
}


