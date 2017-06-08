package org.buzzar.appnative.activities.settings;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.buzzar.appnative.R;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AboutUsActivity extends AppCompatActivity {

    @BindView(R.id.about_us_webview)
    WebView aboutUsWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);

        ButterKnife.bind(this);

        aboutUsWebView.setWebChromeClient(new WebChromeClient());
        aboutUsWebView.setWebViewClient(new WebViewClient());
        aboutUsWebView.getSettings().setJavaScriptEnabled(true);
        String lang = Locale.getDefault().getLanguage();
        if("ru".equals(lang))
            aboutUsWebView.loadUrl("https://shiners.ru/about-us?isiframe=true");
        else
            aboutUsWebView.loadUrl("https://shiners.mobi/about-us?lat=37&lng=-120&isiframe=true");
    }
}
