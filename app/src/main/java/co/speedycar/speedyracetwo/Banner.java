package co.speedycar.speedyracetwo;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdViewAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.ads.MaxAdView;

public class Banner
        implements MaxAdViewAdListener {

    private MaxAdView adView;
    private Context context;
    private LinearLayout main_layout;

    public Banner(Context context, LinearLayout main_layout){
        this.context = context;
        this.main_layout = main_layout;

    }

    void createBannerAd()
    {
        adView = new MaxAdView(context.getString(R.string.applovin_banner), context );
        adView.setListener( this );

        // Stretch to the width of the screen for banners to be fully functional
        int width = MainActivity.screenWidth;

        // Banner height on phones and tablets is 50 and 90, respectively
        int heightPx = R.dimen.banner_height;

        adView.setLayoutParams( new LinearLayout.LayoutParams( width, heightPx ) );

        // Set background or background color for banners to be fully functional
        adView.setBackgroundColor(Color.WHITE);

//        ViewGroup rootView = findViewById( android.R.id.content )

        // Load the ad
        adView.loadAd();

        main_layout.addView( adView );
    }

    // MAX Ad Listener
    @Override
    public void onAdLoaded(final MaxAd maxAd) {}

    @Override
    public void onAdLoadFailed(final String adUnitId, final MaxError error) {}

    @Override
    public void onAdDisplayFailed(final MaxAd maxAd, final MaxError error) {}

    @Override
    public void onAdClicked(final MaxAd maxAd) {}

    @Override
    public void onAdExpanded(final MaxAd maxAd) {}

    @Override
    public void onAdCollapsed(final MaxAd maxAd) {}

    @Override
    public void onAdDisplayed(final MaxAd maxAd) { /* DO NOT USE - THIS IS RESERVED FOR FULLSCREEN ADS ONLY AND WILL BE REMOVED IN A FUTURE SDK RELEASE */ }

    @Override
    public void onAdHidden(final MaxAd maxAd) { /* DO NOT USE - THIS IS RESERVED FOR FULLSCREEN ADS ONLY AND WILL BE REMOVED IN A FUTURE SDK RELEASE */ }

}
