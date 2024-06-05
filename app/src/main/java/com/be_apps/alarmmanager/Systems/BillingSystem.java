package com.be_apps.alarmmanager.Systems;

import android.app.Activity;
import android.app.Dialog;
import android.app.Notification;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.preference.PreferenceManager;
import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.be_apps.alarmmanager.Constant;
import com.be_apps.alarmmanager.R;

import java.util.ArrayList;
import java.util.List;

public class BillingSystem {

    private final Context activity ;
    private BillingClient billingClient ;
    private final ViewGroup Root ;
    BillingProcess billingProcess ;

    public BillingSystem(Context activity  , ViewGroup viewGroup) {
        this.activity = activity ;
        this.Root = viewGroup ;
        SetUpBillingClient();
    }
    public void AttachListener(Activity activity) {
        billingProcess = (BillingProcess) activity;
    }
    public void StartBillingProcess() {
        Log.e("ad_trace" , "StartBillingConnection");
        if (billingClient==null) {
            SetUpBillingClient();
        }
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                Log.e("ad_trace" , "onBillingSetupFinished");
                //---------------Remove !---------------------//
                //PrePareNoAdsDialog();
                //billingProcess.OnSuccessOperation();
                //PrepareRemoveAdsNotification();
                //RemoveAds();
                //------------------------------------//
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    PrepareBillingProcess();
                }
            }
            @Override
            public void onBillingServiceDisconnected() {
                Log.e("ad_trace" , "onBillingServiceDisconnected");
                // try to reconnect :
                StartBillingProcess();
            }
        });
    }
    public void StartFetchingPurchasesProcess() {
        Log.e("ad_trace" , "StartFetchingPurchasesProcess");
        if (billingClient==null)
            SetUpBillingClient();
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                FetchingPurchases();
            }

            @Override
            public void onBillingServiceDisconnected() {
                  Log.e("ad_trace" , "onBillingServiceDisconnected");
                  StartFetchingPurchasesProcess();
            }
        });
    }
    private void FetchingPurchases() {
        Log.e("ad_trace" , "FetchingPurchases");
        Purchase.PurchasesResult purchasesResult = billingClient.queryPurchases(BillingClient.SkuType.INAPP) ;
        List<Purchase> purchases = purchasesResult.getPurchasesList() ;
        Log.e("ad_trace" , "purchases size is " + (purchases!=null ? purchases.size() : 404));
        if (purchases!=null && purchases.size()!=0) {
            for (Purchase purchase : purchases) {
                handlePurchase(purchase);
            }
        }
        else
            Log.e("ad_trace" , "purchases size  0");
    }
    private void SetUpBillingClient() {
        Log.e("ad_trace" , "SetUpBillingClient") ;
        // BillingClient is the main interface for communication between the Google Play Billing Library and the rest of your app.
        billingClient = BillingClient.newBuilder(activity)
                .enablePendingPurchases()
                .setListener(new PurchasesUpdatedListener() {
                    @Override
                    public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> list) {
                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && list != null) {
                            // the user has buy the item
                            Log.e("ad_trace" , "the user has buy the item") ;
                            for (Purchase purchase : list) {
                                handlePurchase(purchase);
                            }
                        }
                        else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
                            Toast.makeText(activity, "Cancelled", Toast.LENGTH_LONG).show();
                            Log.e("ad_trace" , "Cancelled") ;
                        }
                        else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
                            Toast.makeText(activity, "You already Remove The Ads from your app", Toast.LENGTH_LONG).show();
                            Log.e("ad_trace" , "You already Remove The Ads from your app") ;
                        }
                        else {
                            Toast.makeText(activity, "There is an error please try again", Toast.LENGTH_LONG).show();
                            Log.e("ad_trace" , "There is an error please try again") ;
                        }
                    }
                })
                .build() ;
    }
    private void handlePurchase(Purchase purchase) {
        Log.e("ad_trace" , "handlePurchase ");
        AcknowledgePurchaseResponseListener acknowledgePurchaseResponseListener = new AcknowledgePurchaseResponseListener() {
            @Override
            public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {
                    Log.e("ad_trace" , "Doneeeeeee");
                    RemoveAds();
                    PrepareRemoveAdsNotification();
                    if (Root!=null) {
                        PrePareNoAdsDialog();
                        billingProcess.OnSuccessOperation();
                    }
            }
        } ;
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            if (purchase.getSku().equals(Constant.REMOVE_ADS_PRODUCT_ID)) {
                Log.e("ad_trace" , "fine");
                if (!purchase.isAcknowledged()) {
                    Log.e("ad_trace" , "Not isAcknowledged");
                    AcknowledgePurchaseParams acknowledgePurchaseParams =
                            AcknowledgePurchaseParams.newBuilder()
                                    .setPurchaseToken(purchase.getPurchaseToken())
                                    .build();
                    billingClient.acknowledgePurchase(acknowledgePurchaseParams, acknowledgePurchaseResponseListener);
                }
                else {
                    Log.e("ad_trace" , "isAcknowledged");
                    RemoveAds();
                }
            }
        }
    }
    private void PrepareBillingProcess() {
        Log.e("ad_trace" , "PrepareBillingProcess");

        SkuDetailsParams.Builder builder ;
        List<String> SkuList = new ArrayList<>();
        SkuList.add(Constant.REMOVE_ADS_PRODUCT_ID) ; // add the ids of your products
        if (billingClient.isReady()) {
            Log.e("ad_trace" , "billing Client isReady");
            builder = SkuDetailsParams.newBuilder()
                    .setType(BillingClient.SkuType.INAPP)
                    .setSkusList(SkuList) ;
            billingClient.querySkuDetailsAsync(builder.build(), new SkuDetailsResponseListener() {
                @Override
                public void onSkuDetailsResponse(@NonNull BillingResult billingResult, @Nullable List<SkuDetails> list) {
                    Log.e("ad_trace" , "Result " + billingResult.getDebugMessage() + "  " + billingResult.getResponseCode());
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        Log.e("ad_trace", "onSkuDetailsResponse");
                        if (list != null && list.size() != 0) {
                            for (SkuDetails skuDetails : list) {
                                Log.e("ad_trace", "id " + skuDetails.getSku());
                                Log.e("ad_trace", "price " + skuDetails.getPrice());
                                Log.e("ad_trace", "Description " + skuDetails.getDescription());
                                Log.e("ad_trace", "Title " + skuDetails.getTitle());
                                BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                                        .setSkuDetails(skuDetails)
                                        .build();
                                BillingResult res = billingClient.launchBillingFlow((Activity) activity, flowParams);
                                Log.e("ad_trace", "launchBillingFlow Result : " + res.getResponseCode() + " " + res.getDebugMessage());
                            }
                        }
                        else {
                            Toast.makeText(activity , "There is an error please try again" , Toast.LENGTH_LONG).show();
                            Log.e("ad_trace", "the list is empty");
                        }
                    }
                    else {
                        Toast.makeText(activity , "There is an error please try again" , Toast.LENGTH_LONG).show();
                        Log.e("ad_trace", "Error while launching the flow");
                    }
                }
            });
        }
        else {
            // not ready :
            Log.e("ad_trace" , "not ready");
            Toast.makeText(activity , "Please Wait" , Toast.LENGTH_LONG).show();
            StartBillingProcess();
        }
    }
    private void RemoveAds() {
        PreferenceManager.getDefaultSharedPreferences(activity).edit().putBoolean(Constant.AdsRemoved , true).apply();
        Log.e("ad_trace" , "Ads Removed") ;
    }
    private void PrePareNoAdsDialog() {
        View view = ((Activity)activity).getLayoutInflater().inflate(R.layout.remove_ads_dialog , Root , false) ;
        TextView textView = view.findViewById(R.id.RemoveAdTxt);
        textView.setTypeface(Typeface.createFromAsset(activity.getAssets() , "fonts/Calistoga-Regular.ttf"));
        Dialog dialog = new Dialog(activity);
        dialog.setContentView(view);
        if (dialog.getWindow()!=null)
        dialog.getWindow().getAttributes().windowAnimations = R.style.CustomDialogAnimation;
        dialog.show();
    }
    private void PrepareRemoveAdsNotification() {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(activity) ;
        Bitmap largeIcon = BitmapFactory.decodeResource(activity.getResources(), R.drawable.check);
        NotificationCompat.Builder buildNotification;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            buildNotification = new NotificationCompat.Builder(activity , Constant.NOTIFICATION_CHANNEL_SYSTEM);
        else {
            buildNotification = new NotificationCompat.Builder(activity , "") ;
            buildNotification.setPriority(NotificationCompat.PRIORITY_MAX);
            buildNotification.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);
            buildNotification.setLights(Color.RED, 2000, 500);
          }
        buildNotification.setSmallIcon(R.drawable.ic_plan);
        buildNotification.setLargeIcon(largeIcon);
        buildNotification.setContentText("Congratulation !! You Remove Ads For ever");
        buildNotification.setCategory(NotificationCompat.CATEGORY_ALARM);
        buildNotification.setAutoCancel(true);
        buildNotification.setContentTitle("Your Operation has successfully Completed");
        buildNotification.setTicker("Your Operation has successfully Completed");
        buildNotification.setBadgeIconType(NotificationCompat.BADGE_ICON_LARGE);
        buildNotification.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        Notification notification = buildNotification.build();
        notificationManager.notify(5555 , notification);
    }
    public interface BillingProcess {
        void OnSuccessOperation();
    }
}
