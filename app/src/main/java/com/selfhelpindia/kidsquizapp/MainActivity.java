package com.selfhelpindia.kidsquizapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.selfhelpindia.kidsquizapp.databinding.ActivityMainBinding;
import com.selfhelpindia.kidsquizapp.modelclass.UserModelClass;

import me.ibrahimsn.lib.OnItemSelectedListener;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    FragmentTransaction transaction;
    FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        //setSupportActionBar(binding.toolbar);
        setContentView(binding.getRoot());

        database = FirebaseFirestore.getInstance();
        transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, new HomeFragment());
        transaction.commit();
        if (FirebaseAuth.getInstance().getCurrentUser() != null)
            database.collection("users")
                    .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                            UserModelClass userModelClass = value.toObject(UserModelClass.class);
                            binding.coins.setText(String.valueOf(userModelClass.getCoins()));
                            binding.name.setText(String.valueOf(userModelClass.getName()));
                        }
                    });

        binding.bottomBar.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public boolean onItemSelect(int i) {
                final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                switch (i) {
                    case 0:
                        transaction.replace(R.id.container, new HomeFragment());
                        transaction.commit();
                        break;
                    case 1:
                        transaction.replace(R.id.container, new LeaderBoardFragment());
                        transaction.commit();
                        break;
                    case 2:
                        transaction.replace(R.id.container, new WalletFragment());
                        transaction.commit();
                        break;
                    case 3:
                        transaction.replace(R.id.container, new ProfileFragment());
                        transaction.commit();
                        break;
                }
                return false;
            }
        });


    }


}