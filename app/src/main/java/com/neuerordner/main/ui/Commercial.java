package com.neuerordner.main.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.neuerordner.main.R;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.neuerordner.main.BuildConfig;

public class Commercial extends Fragment {
    // ← This must be a *Native* ad unit ID, not the “~” App ID.
    private static final String NATIVE_AD_UNIT_ID =
            "ca-app-pub-3940256099942544/2247696110"; // Google’s TEST Native-Advanced ID

    private NativeAd nativeAd;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        // Make sure this XML file has your <NativeAdView> as the root or inside it
        return inflater.inflate(R.layout.fragement_native_add, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View root, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(root, savedInstanceState);

        // 1) Look up the NativeAdView by its ID (match your XML)
        if (!BuildConfig.DEBUG) {
            NativeAdView adView = root.findViewById(R.id.native_ad_view);
            loadNativeAd(adView);
        }

    }

    private void loadNativeAd(NativeAdView adView) {
        AdLoader loader = new AdLoader.Builder(requireContext(), NATIVE_AD_UNIT_ID)
                .forNativeAd(ad -> {
                    // keep a reference so we can destroy it later
                    nativeAd = ad;
                    populateAd(ad, adView);
                })
                .withAdListener(new AdListener() {
                    @Override public void onAdFailedToLoad(@NonNull LoadAdError error) {
                        Log.e("AdFragment", "Commercial failed: " + error.getMessage());
                    }
                })
                .build();

        loader.loadAd(new AdRequest.Builder().build());
    }

    private void populateAd(NativeAd ad, NativeAdView adView) {
        // wire up your views exactly as before…
        TextView headline = adView.findViewById(R.id.native_headline);
        ImageView icon    = adView.findViewById(R.id.native_icon);
        TextView body     = adView.findViewById(R.id.native_body);
        Button cta        = adView.findViewById(R.id.native_cta);

        headline.setText(ad.getHeadline());
        adView.setHeadlineView(headline);

        if (ad.getBody() != null) {
            body.setText(ad.getBody());
            adView.setBodyView(body);
        }

        if (ad.getIcon() != null) {
            icon.setImageDrawable(ad.getIcon().getDrawable());
            adView.setIconView(icon);
        }

        if (ad.getCallToAction() != null) {
            cta.setText(ad.getCallToAction());
            adView.setCallToActionView(cta);
        }

        adView.setNativeAd(ad);
    }

    @Override
    public void onDestroyView() {
        if (nativeAd != null) nativeAd.destroy();
        super.onDestroyView();
    }
}
