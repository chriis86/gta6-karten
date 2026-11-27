package de.chriis86.gta6karten;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends Activity {

    private static final String HOME = "file:///android_asset/home.html";

    private WebView web;
    private View bar;
    private ProgressBar prog;
    private TextView title;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.main);

        web = findViewById(R.id.web);
        bar = findViewById(R.id.bar);
        prog = findViewById(R.id.prog);
        title = findViewById(R.id.title);

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);          // Fortschritt der Karten-Seiten (localStorage)
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);
        s.setBuiltInZoomControls(false);
        s.setMediaPlaybackRequiresUserGesture(true);

        CookieManager cm = CookieManager.getInstance();
        cm.setAcceptCookie(true);              // Login bleibt erhalten
        cm.setAcceptThirdPartyCookies(web, true);

        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest r) {
                Uri u = r.getUrl();
                String sc = u.getScheme();
                if ("http".equals(sc) || "https".equals(sc) || "file".equals(sc)) return false;
                openExternal(u);               // mailto:, intent: usw. an andere Apps
                return true;
            }

            @Override
            public void onPageStarted(WebView v, String url, android.graphics.Bitmap fav) {
                prog.setVisibility(View.VISIBLE);
                updateBar(url);
            }

            @Override
            public void onPageFinished(WebView v, String url) {
                prog.setVisibility(View.GONE);
                updateBar(url);
                CookieManager.getInstance().flush();
            }
        });

        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView v, int p) {
                prog.setProgress(p);
            }

            @Override
            public void onReceivedTitle(WebView v, String t) {
                if (t != null && !t.isEmpty()) title.setText(t);
            }
        });

        findViewById(R.id.btnHome).setOnClickListener(v -> web.loadUrl(HOME));
        findViewById(R.id.btnReload).setOnClickListener(v -> web.reload());
        findViewById(R.id.btnBrowser).setOnClickListener(v -> {
            String u = web.getUrl();
            if (u != null && u.startsWith("http")) openExternal(Uri.parse(u));
        });

        if (state != null) web.restoreState(state);
        else web.loadUrl(HOME);
    }

    private void updateBar(String url) {
        boolean home = url == null || url.startsWith("file:");
        bar.setVisibility(home ? View.GONE : View.VISIBLE);
    }

    private void openExternal(Uri u) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, u));
        } catch (Exception ignored) { }
    }

    @Override
    public void onBackPressed() {
        if (web.canGoBack()) web.goBack();
        else super.onBackPressed();
    }

    @Override
    protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        web.saveState(out);
    }

    @Override
    protected void onPause() {
        super.onPause();
        CookieManager.getInstance().flush();
    }
}
