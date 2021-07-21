package com.selfhelpindia.kidsquizapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.selfhelpindia.kidsquizapp.modelclass.UserModelClass;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.selfhelpindia.kidsquizapp.SignUpActivity.REFER_CODE_SF;

public class SplashActivity extends AppCompatActivity {

    FirebaseAuth auth;
    SimpleDateFormat formatter;
    SharedPreferences sf;
    SharedPreferences.Editor editor;
    FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash_activty);
        auth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();
        formatter = new SimpleDateFormat("dd/MM/yyyy");
        sf = getSharedPreferences(REFER_CODE_SF, MODE_PRIVATE);
        int SPLASH_TIME = 3000;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (auth.getCurrentUser() != null) {
                    try {
                        if (formatter.parse(sf.getString("currentDate", null)).before(formatter.parse(formatter.format(new Date())))) {
                            editor = sf.edit();
                            editor.putString("currentDate", formatter.format(new Date()));
                            editor.apply();
                            database.collection("users")
                                    .document(auth.getCurrentUser().getUid())
                                    .update("referCounter", 0);
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                } else {
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                }

                finish();
            }
        }, SPLASH_TIME);
    }
}