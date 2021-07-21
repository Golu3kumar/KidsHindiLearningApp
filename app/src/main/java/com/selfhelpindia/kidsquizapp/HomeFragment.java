package com.selfhelpindia.kidsquizapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.selfhelpindia.kidsquizapp.adapterclass.TopicAdapter;
import com.selfhelpindia.kidsquizapp.databinding.FragmentHomeBinding;
import com.selfhelpindia.kidsquizapp.modelclass.TopicModelClass;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import static com.selfhelpindia.kidsquizapp.SignUpActivity.REFER_CODE_SF;
import static com.selfhelpindia.kidsquizapp.modelclass.Constants.EXTRA_COINS_EARNED_BY_ADS;

public class HomeFragment extends Fragment {

    private RewardedAd rewardedAd;
    private static final String TAG = "HomeFragment";
    public static final String CURRENT_DATE_AND_COUNTER_SF = "reward_helper1";
    SharedPreferences sf;
    SharedPreferences.Editor editor;
    SimpleDateFormat formatter;
    FirebaseAuth mAuth;
    RewardSuccessDialog rewardSuccessDialog;
    ShowInternetDialog showInternetDialog;

    public HomeFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    FragmentHomeBinding binding;
    FirebaseFirestore database;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();
        rewardSuccessDialog = new RewardSuccessDialog(getActivity());
        showInternetDialog = new ShowInternetDialog(getActivity());
        formatter = new SimpleDateFormat("dd/MM/yyyy");
        sf = getActivity().getSharedPreferences(CURRENT_DATE_AND_COUNTER_SF, Context.MODE_PRIVATE);
        editor = sf.edit();
        if (!sf.getBoolean("isRewardedAdsHelperStored", false)) {
            editor.putString("adsCreationDate", formatter.format(new Date()));
            editor.putInt("adsCounter", 0);
            editor.putBoolean("isRewardedAdsHelperStored", true);
            editor.putBoolean("isAdsAvailable", true);
            editor.apply();
        }

        if (sf.getString("adsCreationDate", null).equals(formatter.format(new Date()))) {
            if (!sf.getBoolean("isAdsAvailable", false)) {
                binding.lockEarnCoins.setVisibility(View.VISIBLE);
            }
        }

        try {
            if (formatter.parse(sf.getString("adsCreationDate", null)).before(formatter.parse(formatter.format(new Date())))) {
                editor.putString("adsCreationDate", formatter.format(new Date()));
                editor.putBoolean("isAdsAvailable", true);
                editor.apply();
                binding.lockEarnCoins.setVisibility(View.INVISIBLE);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        final ArrayList<TopicModelClass> model = new ArrayList<>();
        final TopicAdapter topicAdapter = new TopicAdapter(getContext(), model);

        database.collection("categories")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        model.clear();
                        for (DocumentSnapshot snapshot : value.getDocuments()) {
                            TopicModelClass topicModelClass = snapshot.toObject(TopicModelClass.class);
                            topicModelClass.setCategoryId(snapshot.getId());
                            model.add(topicModelClass);
                        }
                        topicAdapter.notifyDataSetChanged();
                    }
                });
        binding.recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.recyclerView.setAdapter(topicAdapter);

        //
        MobileAds.initialize(getContext(), new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        binding.earnCoins.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sf.getBoolean("isAdsAvailable", false)) {
                    loadTheAds();
                    startAd();
                } else {
                    Toast.makeText(getContext(), "Your Daily Limit over, Wait for next day!", Toast.LENGTH_SHORT).show();
                }

            }
        });

        if (!showInternetDialog.isInternetConnected()){
            showInternetDialog.showDialog();
        }
        binding.inviteFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(),ReferAndEarn.class));
            }
        });
        return binding.getRoot();
    }

    private void loadTheAds() {
        AdRequest adRequest = new AdRequest.Builder().build();

        RewardedAd.load(getContext(), getResources().getString(R.string.rewarded_ads_id),
                adRequest, new RewardedAdLoadCallback() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error.
                        Log.d(TAG, loadAdError.getMessage());
                        rewardedAd = null;
                    }

                    @Override
                    public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                        HomeFragment.this.rewardedAd = rewardedAd;
                        Log.d(TAG, "onAdFailedToLoad");
                    }
                });
    }

    private void startAd() {

        if (rewardedAd != null) {
            Activity activityContext = getActivity();
            rewardedAd.show(activityContext, new OnUserEarnedRewardListener() {
                @Override
                public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                    if (sf.getInt("adsCounter", 0) == 5) {
                        editor.putBoolean("isAdsAvailable", false);
                        editor.putInt("adsCounter", 0);
                        editor.apply();
                        binding.lockEarnCoins.setVisibility(View.VISIBLE);
                        return;
                    } else {
                        Log.d(TAG, "onUserEarnedReward: " + sf.getInt("adsCounter", 0));
                        editor.putInt("adsCounter", sf.getInt("adsCounter", 0) + 1);
                        editor.apply();
                    }

                    int getRewardedCoins = getRandomElement(EXTRA_COINS_EARNED_BY_ADS);
                    database.collection("users")
                            .document(mAuth.getCurrentUser().getUid())
                            .update("coins", FieldValue.increment(getRewardedCoins));
                    rewardSuccessDialog.startDialog(getRewardedCoins);
                    //int rewardAmount = rewardItem.getAmount();
                    //String rewardType = rewardItem.getType();
                    //Log.d(TAG, "onUserEarnedReward: " + rewardAmount);
                }

            });
        } else {
            Log.d("TAG", "The rewarded ad wasn't ready yet.");
        }

    }

    public int getRandomElement(ArrayList<Integer> arrayList) {
        Random rand = new Random();
        return arrayList.get(rand.nextInt(arrayList.size()));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        showInternetDialog.dismissDialog();
    }
}