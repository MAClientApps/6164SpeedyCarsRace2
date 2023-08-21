package co.speedycar.speedyracetwo;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.ads.MaxInterstitialAd;
import com.appsflyer.AppsFlyerLib;
import co.speedycar.speedyracetwo.R;


import java.util.concurrent.TimeUnit;

public class WebviewActivity extends AppCompatActivity {
    private MaxInterstitialAd interstitialAd;
    private WebView webview;
    LinearLayout layoutCheckConnection;
    Button btnRetry;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    int retryAttempt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        initView();
        loadInterstitialAds();
    }

    @SuppressLint("SetJavaScriptEnabled")
    public void initView() {
        webview = findViewById(R.id.webview);
        layoutCheckConnection = findViewById(R.id.no_connection_container);
        CookieManager.getInstance().setAcceptCookie(true);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setUseWideViewPort(true);
        webview.getSettings().setLoadWithOverviewMode(true);
        webview.getSettings().setDomStorageEnabled(true);
        webview.getSettings().setPluginState(WebSettings.PluginState.ON);
        webview.setWebChromeClient(new WebChromeClient());
        webview.setVisibility(View.VISIBLE);

        webview.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request,
                                        WebResourceError error) {
                super.onReceivedError(view, request, error);
                String url = request.getUrl().toString();
                if (!url.startsWith("http")) {
                    startActivity(new Intent(WebviewActivity.this, MainActivity.class));
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(url));
                        startActivity(intent);
                        finish();
                    } catch (Exception e) {
                        if (interstitialAd != null && interstitialAd.isReady()) {
                            interstitialAd.showAd();
                        }
                        finish();
                    }
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return super.shouldOverrideUrlLoading(view, request);
            }
        });

        loadDataView();
    }

    public void checkInternetConnection() {
        layoutCheckConnection.setVisibility(View.VISIBLE);
        btnRetry = findViewById(R.id.try_again);
        btnRetry.setOnClickListener(view -> {
            layoutCheckConnection.setVisibility(View.GONE);
            loadDataView();
        });
    }

    protected void loadDataView() {
        if (Util.isNetworkAvailable(this)) {
            String url = buildTheUrl();
            webview.loadUrl(url);
        } else {
            checkInternetConnection();
        }
    }

    private String buildTheUrl(){
        String end = sharedPreferences.getString("end", ""); // the one we get from remote config and saved it in shared preferences
        String adid = sharedPreferences.getString("adid", "");
        String afid = AppsFlyerLib.getInstance().getAppsFlyerUID(this);
        String packageName = getPackageName();
        String click_id = sharedPreferences.getString("click_id", "");
        String firebase_instance_id = sharedPreferences.getString("firebase_instance_id", "");
        String mnc = getMnc(this);
        String mcc = getMcc(this);

        String url = end + "?naming=&adid="+adid+"&afid="+afid+"&package="+packageName+"&click_id="+click_id+"&firebase_instance_id="+firebase_instance_id+"&mnc="+mnc+"&mcc="+mcc;
        return url;
    }

    @Override
    public void onResume() {
        super.onResume();
        webview.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        webview.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        webview.loadUrl("about:blank");
    }

    @Override
    public void onBackPressed() {
    }

    public void loadInterstitialAds() {
        interstitialAd = new MaxInterstitialAd(this.getString(R.string.applovin_interstitial_2), this);
        interstitialAd.setListener(new MaxAdListener() {
            @Override
            public void onAdLoaded(MaxAd ad) {
                retryAttempt = 0;
            }

            @Override
            public void onAdDisplayed(MaxAd ad) {
            }

            @Override
            public void onAdHidden(MaxAd ad) {
                interstitialAd.loadAd();
            }

            @Override
            public void onAdClicked(MaxAd ad) {
            }

            @Override
            public void onAdLoadFailed(String adUnitId, MaxError error) {
                retryAttempt++;
                long delayMillis = TimeUnit.SECONDS.toMillis((long) Math.pow(2, Math.min(6, retryAttempt)));
                new Handler().postDelayed(() -> interstitialAd.loadAd(), delayMillis);
            }

            @Override
            public void onAdDisplayFailed(MaxAd ad, MaxError error) {
                interstitialAd.loadAd();
            }
        });

        interstitialAd.loadAd();
    }

    public static String getMnc(Context context) {
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager == null) {
                return "";
            }
            String mCCMncCode = telephonyManager.getNetworkOperator();
            String mncCode = "";
            if (TextUtils.isEmpty(mCCMncCode)) {
                return "";
            }

            final int MNC_CODE_LENGTH = 3;

            if (mCCMncCode.length() > MNC_CODE_LENGTH) {
                mncCode = mCCMncCode.substring(MNC_CODE_LENGTH);
            }
            return mncCode;
        } catch (Exception e) {
            return "";
        }
    }

    public static String getMcc(Context context) {
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager == null) {
                return "";
            }
            String mCCMccCode = telephonyManager.getNetworkOperator();
            String mccCode = "";
            if (TextUtils.isEmpty(mCCMccCode)) {
                return "";
            }

            final int MCC_CODE_LENGTH = 3;
            if (mCCMccCode.length() >= MCC_CODE_LENGTH) {
                mccCode = mCCMccCode.substring(0, MCC_CODE_LENGTH);
            }

            return mccCode;
        } catch (Exception e) {
            return "";
        }
    }
}