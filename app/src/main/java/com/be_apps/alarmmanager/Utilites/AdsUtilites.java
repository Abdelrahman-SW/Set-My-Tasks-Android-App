package com.be_apps.alarmmanager.Utilites;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Insets;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowMetrics;
import androidx.preference.PreferenceManager;
import com.be_apps.alarmmanager.Constant;
import com.google.android.ads.nativetemplates.TemplateView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.android.gms.ads.formats.UnifiedNativeAd;

public class AdsUtilites {
    int NumOfFailedRequestsForBanner = 0 ;
    int NumOfFailedRequestsForInterstitialAd = 0 ;
    int NumOfFailedRequestsForNativeAd = 0 ;
    private final int MAXIMUM_NUMBER_OF_AD_REQUEST = 5 ;
    AdLoader adLoader ;
    UnifiedNativeAd TheUnifiedNativeAd ;

    public AdsUtilites () {
    }

    private AdSize getAdSize(Activity activity) {
        //We need to Determine the screen width (less decorations) to use for the ad width.
        int adWidth ;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowMetrics windowMetrics = activity.getWindowManager().getCurrentWindowMetrics();
            Insets insets = windowMetrics.getWindowInsets()
                    .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars());
            float WidthInPixel = windowMetrics.getBounds().width() - insets.left - insets.right;
            Configuration config = activity.getResources().getConfiguration();
            float densityDpi = (config.densityDpi) ;
            float density = densityDpi/160 ;
            adWidth = (int) (WidthInPixel / density);
        }
        else {
            Display display = activity.getWindowManager().getDefaultDisplay();
            DisplayMetrics outMetrics = new DisplayMetrics();
            display.getMetrics(outMetrics);
            float widthPixels = outMetrics.widthPixels;
            float density = outMetrics.density;
            // get width in dp :
            adWidth = (int) (widthPixels / density);
        }
        // Step 3 - Get adaptive ad size and return for setting on the ad view.
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth);
    }
    public  AdView PrepareBannerAd (Activity activity , String UnitId , ViewGroup Container) {
        NumOfFailedRequestsForBanner = 0 ;
        AdView adView = new AdView(activity);
        adView.setAdUnitId(UnitId);
        // Get The adaptive banner size :
        AdSize adSize = getAdSize(activity);
        Log.d("MyApp" , "AdSizeWidth " + adSize.getWidth());
        Log.d("MyApp" , "AdSizeHeight " + adSize.getHeight());
        adView.setAdSize(adSize);
        Container.addView(adView);
        adView.loadAd(new AdRequest.Builder().build());
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(LoadAdError loadAdError) {
                Log.e("MyApp" , "BannerOnAdFailedToLoad " + loadAdError.getMessage() + " " + loadAdError.getCode());
                if (NumOfFailedRequestsForBanner++ < MAXIMUM_NUMBER_OF_AD_REQUEST) {
                    Log.d("MyApp" , "Try To Load Again") ;
                    adView.loadAd(new AdRequest.Builder().build());
                }
                super.onAdFailedToLoad(loadAdError);
            }

            @Override
            public void onAdLoaded() {
                NumOfFailedRequestsForBanner = 0 ;
                super.onAdLoaded();
                Log.d("MyApp" , "onAdLoaded") ;
                if (adView.getResponseInfo()!=null && adView.getResponseInfo().getMediationAdapterClassName()!=null)
                Log.d("MyApp" , adView.getResponseInfo().getMediationAdapterClassName());
            }

            @Override
            public void onAdClosed() {
                super.onAdClosed();
                Log.e("ab_do" , "onAdClosed") ;
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
                Log.e("ab_do" , "onAdOpened") ;
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
                Log.e("ab_do" , "onAdClicked") ;
            }

            @Override
            public void onAdImpression() {
                super.onAdImpression();
                Log.e("ab_do" , "onAdImpression") ;
            }
        });
        return adView ;
    }
    public  InterstitialAd PrepareInterstitialAd (Activity activity , String UnitID) {
        NumOfFailedRequestsForInterstitialAd = 0 ;
        InterstitialAd mInterstitialAd = new InterstitialAd(activity);
        mInterstitialAd.setAdUnitId(UnitID);
        //AdColonyBundleBuilder.setShowPrePopup(true);
        //AdColonyBundleBuilder.setShowPostPopup(true);
        AdRequest request = new AdRequest.Builder()
                //.addNetworkExtrasBundle(AdColonyAdapter.class, AdColonyBundleBuilder.build())
                .build();
        mInterstitialAd.loadAd(request);
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(LoadAdError loadAdError) {
                Log.d("MyApp" , "InterstitialOnAdFailedToLoad " + loadAdError.getMessage() + " " + loadAdError.getCode());
                if (NumOfFailedRequestsForInterstitialAd++ < MAXIMUM_NUMBER_OF_AD_REQUEST) {
                    Log.e("MyApp" , "Try To Load Again For InterstitialAd ") ;
                    mInterstitialAd.loadAd(new AdRequest.Builder().build());
                }
                super.onAdFailedToLoad(loadAdError);
            }

            @Override
            public void onAdLoaded() {
                NumOfFailedRequestsForInterstitialAd = 0 ;
                super.onAdLoaded();
                if (mInterstitialAd.getResponseInfo()!=null && mInterstitialAd.getResponseInfo().getMediationAdapterClassName() !=null)
                    Log.d("MyApp" , "onInterstitialAdLoaded  " + mInterstitialAd.getResponseInfo().getMediationAdapterClassName()) ;
            }

            @Override
            public void onAdClosed() {
                super.onAdClosed();
                Log.e("ab_do" , "onAdClosed") ;
                // Load the next interstitial.
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
            }


            @Override
            public void onAdOpened() {
                super.onAdOpened();
                Log.e("ab_do" , "onAdOpened") ;
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
                Log.e("ab_do" , "onAdClicked") ;
            }

            @Override
            public void onAdImpression() {
                super.onAdImpression();
                Log.e("ab_do" , "onAdImpression") ;
            }
        });
        return mInterstitialAd ;
    }
    public void PrepareNativeAd(Activity activity , String UnitID , ViewGroup TemplateContainer , TemplateView Template , ViewGroup viewGroup) {
        NumOfFailedRequestsForNativeAd = 0 ;
        AdLoader.Builder adLoaderBuilder = new AdLoader.Builder(activity, UnitID) ;
        adLoaderBuilder.forUnifiedNativeAd(new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {
                    @Override
                    public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAd) {
                        // the ad has been successfully loaded
                        NumOfFailedRequestsForNativeAd = 0 ;
                        if (unifiedNativeAd.getResponseInfo()!=null && unifiedNativeAd.getResponseInfo().getMediationAdapterClassName() != null)
                         Log.d("MyApp" , "onUnifiedNativeAdLoaded  " + unifiedNativeAd.getResponseInfo().getMediationAdapterClassName());
                         TemplateContainer.setVisibility(View.VISIBLE);
                         Template.setVisibility(View.VISIBLE);
                         Template.setNativeAd(unifiedNativeAd);
                        if (viewGroup!=null) {
                            viewGroup.addView(TemplateContainer);
                            viewGroup.setVisibility(View.VISIBLE);
                        }
                         DestroyTheCurrentNativeAd();
                         TheUnifiedNativeAd = unifiedNativeAd ;
                        // Show the ad.
                        // If this callback occurs after the activity is destroyed, you
                        // must call destroy and return or you may get a memory leak.
                        if (activity.isDestroyed()) {
                            unifiedNativeAd.destroy();
                        }
                    }
                });
        adLoaderBuilder.withAdListener(new AdListener() {
            // hint :  onAdLoaded() method from AdListener is not called when a native ad loads successfully.
            @Override
            public void onAdFailedToLoad(LoadAdError loadAdError) {
                    // Handle the failure by logging, altering the UI, and so on.
                    Log.d("MyApp" , "onNativeAdFailedToLoad  " + loadAdError.getMessage() + " " + loadAdError.getCode());
                    TemplateContainer.setVisibility(View.GONE);
                    if (viewGroup!=null) {
                        viewGroup.setVisibility(View.GONE);
                    }
                    Template.setVisibility(View.GONE);
                if (NumOfFailedRequestsForNativeAd++ < MAXIMUM_NUMBER_OF_AD_REQUEST) {
                    Log.d("MyApp" , "Try To Load Again NativeAd ") ;
                    adLoader.loadAd(new AdRequest.Builder().build());
                }
                   super.onAdFailedToLoad(loadAdError);
            }
        });

        adLoaderBuilder.withNativeAdOptions(new NativeAdOptions.Builder()
                        // Methods in the NativeAdOptions.Builder class can be
                        // used here to specify individual options settings.
                        .setRequestMultipleImages(true)
                        .build());
        adLoader = adLoaderBuilder.build();
        adLoader.loadAd(new AdRequest.Builder().build());
    }
    public static boolean IsAdsRemoved(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(Constant.AdsRemoved , false) ;
    }
    public void DestroyTheCurrentNativeAd() {
        if (TheUnifiedNativeAd!=null)
            TheUnifiedNativeAd.destroy();
    }

}
