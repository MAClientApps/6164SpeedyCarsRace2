package co.speedycar.speedyracetwo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import co.speedycar.speedyracetwo.R;

public class WebGame extends AppCompatActivity {

    WebView myWebView;
    public static String gameURL;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_game);
        myWebView = (WebView) findViewById(R.id.webview);

        // Configure WebView settings
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true); // Enable JavaScript
        webSettings.setDomStorageEnabled(true); // Enable local storage
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);

        // Set a WebViewClient to handle page navigation within the WebView
        myWebView.setWebViewClient(new WebViewClient());

        // Set a WebChromeClient to handle JavaScript alerts, progress, etc.
        myWebView.setWebChromeClient(new WebChromeClient());

        // Load the game URL
        if (gameURL != null) {
            myWebView.loadUrl(gameURL);
        }
    }

    @Override
    public void onBackPressed() {
        myWebView.destroy();
        // Check if the WebView can go back in its history
        if (myWebView.canGoBack()) {
            myWebView.goBack(); // Navigate back within the WebView's history
        } else {
            super.onBackPressed(); // Default behavior (exit the activity)
        }
    }
}