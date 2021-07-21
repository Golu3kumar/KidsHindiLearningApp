package com.selfhelpindia.kidsquizapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.selfhelpindia.kidsquizapp.databinding.ActivityResultBinding;

import static com.selfhelpindia.kidsquizapp.SignUpActivity.REFER_CODE_SF;
import static com.selfhelpindia.kidsquizapp.modelclass.Constants.COINS_PER_RIGHT_ANSWER;

public class ResultActivity extends AppCompatActivity {

    ActivityResultBinding binding;
    FirebaseFirestore database;
    SharedPreferences sf1;
    private InterstitialAd mInterstitialAd;
    private AdView mAdView;
    private static final String TAG = "ResultActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityResultBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        int correctAnswer = getIntent().getIntExtra("correctAnswer", 0);
        int questionCounter = getIntent().getIntExtra("questionCounter", 0);
        int wrongAnswer = getIntent().getIntExtra("wrongAnswer", 0);
        sf1 = getSharedPreferences(REFER_CODE_SF, MODE_PRIVATE);
        binding.totalAttempts.setText(String.valueOf(questionCounter));
        binding.correctAttempts.setText(String.valueOf(correctAnswer));
        binding.wrongAttempts.setText(String.valueOf(wrongAnswer));
        final long totalEarnedPoints = correctAnswer * COINS_PER_RIGHT_ANSWER;
        binding.earnCoins.setText(String.valueOf(totalEarnedPoints));
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        AdRequest adRequest = new AdRequest.Builder().build();

        mAdView = findViewById(R.id.adView);
        mAdView.loadAd(adRequest);

        if (totalEarnedPoints > 0L) {
            database = FirebaseFirestore.getInstance();
            database.collection("users")
                    .document(FirebaseAuth.getInstance().getUid())
                    .update("coins", FieldValue.increment(totalEarnedPoints));

        }

        InterstitialAd.load(this, getResources().getString(R.string.result_full_screen_ads_id), adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                // The mInterstitialAd reference will be null until
                // an ad is loaded.
                mInterstitialAd = interstitialAd;
                Log.i(TAG, "onAdLoaded");

            }
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                // Handle the error
                Log.i(TAG, loadAdError.getMessage());
                mInterstitialAd = null;
            }
        });

        binding.restartQuiz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), QuizActivity.class);
                intent.putExtra("categoryId", getIntent().getStringExtra("categoryId"));
                startActivity(intent);
                finish();
            }
        });
        binding.shareScore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String referCode = sf1.getString("ReferCode", null);
                String playStoreLink = "https://play.google.com/store/apps/details?id=" + ResultActivity.this.getPackageName();
                String yourShareText = "Hey, I give the answers of some basic Questions and Earned " + totalEarnedPoints + " coins. Signup on 'Learn and Earn' using my referral code "
                        + referCode + " and get Rs. 10 \n " + playStoreLink;
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, yourShareText);
                startActivity(Intent.createChooser(shareIntent, "Share via"));
            }
        });

        binding.close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public void onBackPressed() {
        //startActivity(new Intent(getApplicationContext(),MainActivity.class));
        finish();
        if (mInterstitialAd != null) {
            mInterstitialAd.show(ResultActivity.this);
        } else {
            Log.d("TAG", "The interstitial ad wasn't ready yet.");
        }
    }
}