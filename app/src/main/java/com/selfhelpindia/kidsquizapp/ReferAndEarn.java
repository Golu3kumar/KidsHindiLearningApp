package com.selfhelpindia.kidsquizapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.selfhelpindia.kidsquizapp.databinding.ActivityReferAndEarnBinding;

import static com.selfhelpindia.kidsquizapp.SignUpActivity.REFER_CODE_SF;

public class ReferAndEarn extends AppCompatActivity {

    ActivityReferAndEarnBinding binding;
    SharedPreferences sf1;
    String referCode;
    AdView mAdView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReferAndEarnBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        sf1 = getSharedPreferences(REFER_CODE_SF,MODE_PRIVATE);
        referCode = sf1.getString("ReferCode",null);
        binding.referCode.setText(referCode);
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        AdRequest adRequest = new AdRequest.Builder().build();

        mAdView = findViewById(R.id.refer_and_earn_ad);
        mAdView.loadAd(adRequest);
        binding.referBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String playStoreLink = "https://play.google.com/store/apps/details?id=" + getApplicationContext().getPackageName();
                String yourShareText = "Hey, Give the correct answer and get the Rewards. Signup on 'Learn and Earn' using my referral code "
                        +referCode+" and get Rs. 10 \n " + playStoreLink;
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, yourShareText);
                startActivity(Intent.createChooser(shareIntent, "Share via"));
            }
        });

        binding.referCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager manager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("text",binding.referCode.getText());
                manager.setPrimaryClip(clipData);
                Toast.makeText(ReferAndEarn.this, "Refer Code is Copied!", Toast.LENGTH_SHORT).show();
            }
        });

        binding.closeReferActivityBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}