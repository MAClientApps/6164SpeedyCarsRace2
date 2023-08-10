//package com.example.gamebox2d;
//
//import android.app.Application;
//import android.content.SharedPreferences;
//
//import com.applovin.sdk.AppLovinMediationProvider;
//import com.applovin.sdk.AppLovinSdk;
//import com.appsflyer.AppsFlyerLib;
//import com.google.android.gms.ads.identifier.AdvertisingIdClient;
//import com.google.android.gms.common.ConnectionResult;
//import com.google.android.gms.common.GoogleApiAvailability;
//import com.google.firebase.FirebaseApp;
//import com.google.firebase.analytics.FirebaseAnalytics;
//import com.onesignal.OneSignal;
//
//import java.util.UUID;
//
//public class MyApp extends Application {
//    SharedPreferences sharedPreferences;
//    SharedPreferences.Editor editor;
//    @Override
//    public void onCreate() {
//        super.onCreate();
//        //Initialize shared preferences
//        sharedPreferences = this.getSharedPreferences("MyPrefs", MODE_PRIVATE);
//        editor = sharedPreferences.edit();
//
//        //Initializing AppLovin SDK
//        AppLovinSdk.getInstance(this).setMediationProvider(AppLovinMediationProvider.MAX);
//        AppLovinSdk.initializeSdk(this, appLovinSdkConfiguration -> {
//        });
//
//        //Initializing OneSignal SDK
//        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);
//        OneSignal.initWithContext(this);
//        OneSignal.setAppId("{onesignal_app_token}");
//
//        //Initializing AppsFlyer SDK
//        String afDevKey = "{appflyer_app_token}";
//        AppsFlyerLib appsflyer = AppsFlyerLib.getInstance();
//        appsflyer.setMinTimeBetweenSessions(0);
//        appsflyer.setDebugLog(BuildConfig.DEBUG);
//
//        appsflyer.init(afDevKey, null, this);
//        appsflyer.start(this);
//
//        FirebaseApp.initializeApp(this);
//        FirebaseAnalytics.getInstance(this)
//                .getAppInstanceId()
//                .addOnCompleteListener(task -> {
//                    String firebase_instance_id = task.getResult();
////                    save the firebase instance id in shared preferences.
//                    editor.putString("firebase_instance_id", firebase_instance_id);
//                });
//
//        retrieveAdvertiseID();
//        generateClickId();
//    }
//
//    private void retrieveAdvertiseID() {
//        // Check if Google Play Services is available
//        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
//        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
//
//        if (resultCode == ConnectionResult.SUCCESS) {
//            // Google Play Services is available
//            new Thread(() -> {
//                try {
//                    // Retrieve the Advertising ID
//                    AdvertisingIdClient.Info adInfo = AdvertisingIdClient.getAdvertisingIdInfo(UnLockeEzSplashActivity.this);
//                    String adid = adInfo.getId();
//
////                    save adid in the preferences.
//                    editor.putString("adid", adid);
//
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }).start();
//        }
//    }
//
//    private void generateClickId(){
//        String click_id = UUID.randomUUID().toString().replaceAll("-", "");
////        save click_id in preferences
//        editor.putString("click_id", click_id);
//    }
//}