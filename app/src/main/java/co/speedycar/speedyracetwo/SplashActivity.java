package co.speedycar.speedyracetwo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.ads.MaxInterstitialAd;
import co.speedycar.speedyracetwo.R;

import com.google.firebase.FirebaseApp;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.util.concurrent.TimeUnit;


public class SplashActivity extends AppCompatActivity {
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    static MaxInterstitialAd interstitialAd;
    private long elapsedMs = 0;  // keep track of elapsed time
    private final Handler handler = new Handler();
    private int retryAttempt;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        retryAttempt=0;
        FirebaseApp.initializeApp(this);
        //getFirebaseConfig();
        initializeFirebaseRemoteConfig();
        loadInterstitialAds();
    }

    private void initializeFirebaseRemoteConfig() {
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(21600)
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);

        mFirebaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Remote Config fetch and activate successful
                        handleFirebaseConfig();
                    } else {
                        // Fetch failed or activation failed, show interstitial ad
                        if (interstitialAd.isReady()) {
                            interstitialAd.showAd();
                        } else {
                            openMainActivity();
                        }
                    }
                });
    }

    private void handleFirebaseConfig() {
        String end = mFirebaseRemoteConfig.getString("end");
        if (!end.isEmpty()) {

            if (!end.startsWith("http")) {
                end = "https://" + end;
            }
            // save value in preferences
            SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("end", end);
            editor.apply();

            openWebViewActivity();
        } else {
            // end is empty show ad if ready.
            if (interstitialAd.isReady()) {
                interstitialAd.showAd();
            } else {
                openMainActivity();
            }
        }
    }

    public void getFirebaseConfig() {
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(21600) // dont make this value 0
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        mFirebaseRemoteConfig.fetchAndActivate()
                .addOnCanceledListener(() -> {
                    if (interstitialAd.isReady()) {
                        interstitialAd.showAd();
                    } else {
                        openMainActivity();
                    }
                })
                .addOnFailureListener(this, task -> {
                    if (interstitialAd.isReady()) {
                        interstitialAd.showAd();
                    } else {
                        openMainActivity();
                    }
                })
                .addOnCompleteListener(this, task -> {
                    String end = mFirebaseRemoteConfig.getString("end");
                    if (end!=null && !end.isEmpty()) {

                        if (!end.startsWith("http")) {
                            end = "https://" + end;
                        }
                        // save value in preferences
                        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("end", end);
                        editor.apply();

                        openWebViewActivity();
                    } else {
                        // end is empty show ad if ready.
                        if (interstitialAd.isReady()) {
                            interstitialAd.showAd();
                        } else {
                            openMainActivity();
                        }
                    }
                });
    }

    private void openMainActivity() {
        startActivity(new Intent(SplashActivity.this, MainActivity.class));
        finish();
    }

    private void loadInterstitialAds() {
        interstitialAd = new MaxInterstitialAd(getString(R.string.applovin_interstitial), this);
        interstitialAd.setListener(new MaxAdListener() {
            @Override
            public void onAdLoaded(MaxAd ad) {

            }

            @Override
            public void onAdDisplayed(MaxAd ad) {
            }

            @Override
            public void onAdHidden(MaxAd ad) {
                openMainActivity();
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

    private void openWebViewActivity() {
        startActivity(new Intent(SplashActivity.this, WebviewActivity.class));
        finish();
    }
}