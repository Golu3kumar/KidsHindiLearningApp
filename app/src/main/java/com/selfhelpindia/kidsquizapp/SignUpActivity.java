package com.selfhelpindia.kidsquizapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.selfhelpindia.kidsquizapp.databinding.ActivitySignUpBinding;
import com.selfhelpindia.kidsquizapp.modelclass.UserModelClass;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Random;

import static com.selfhelpindia.kidsquizapp.modelclass.Constants.COINS_PER_REFERRAL;

public class SignUpActivity extends AppCompatActivity {

    ActivitySignUpBinding binding;
    FirebaseAuth auth;
    FirebaseFirestore database;
    ProgressDialog dialog;
    String email, pass, name, referCode;
    private static final String TAG = "SignUpActivity";
    SharedPreferences sf;
    SharedPreferences.Editor editor;
    SimpleDateFormat formatter;
    public static final String REFER_CODE_SF = "REFER_CODE_SF";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // initializing firebase components
        auth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();
        dialog = new ProgressDialog(this);
        sf = getSharedPreferences(REFER_CODE_SF, MODE_PRIVATE);
        formatter = new SimpleDateFormat("dd/MM/yyyy");
        dialog.setMessage("Please Wait...");
        binding.signUpSubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                dialog.show();
                email = binding.signUpEmail.getText().toString().trim();
                pass = binding.signUpPassword.getText().toString().trim();
                name = binding.signUpName.getText().toString().trim();
                referCode = binding.signUpRefer.getText().toString().trim();

                if (email.isEmpty() || pass.isEmpty() || name.isEmpty()) {
                    Toast.makeText(SignUpActivity.this, "One of the field is missing!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!referCode.isEmpty()) {
                    findReferalCodeInDatabase(referCode);
                    //database.collection("users");
                } else {
                    signUpNewUser(0L);
                }

            }
        });
    }

    private String getUniqueRefralCode() {
        final String DATA = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        Random RANDOM = new Random();
        StringBuilder sb = new StringBuilder(5);

        for (int i = 0; i < 5; i++) {
            sb.append(DATA.charAt(RANDOM.nextInt(DATA.length())));
        }

        return sb.toString();
    }


    private void findReferalCodeInDatabase(final String referCode) {
        database.collection("users")
                .orderBy("referCode")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        boolean flag = false;
                        for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                            UserModelClass userModelClass = snapshot.toObject(UserModelClass.class);
                            if (referCode.equals(userModelClass.getReferCode())) {
                                if (userModelClass.getReferCounter() < 3) {
                                    database.collection("users")
                                            .document(userModelClass.getuId())
                                            .update("coins", FieldValue.increment(COINS_PER_REFERRAL)
                                                    , "referCounter", FieldValue.increment(1));
                                    signUpNewUser(COINS_PER_REFERRAL);
                                    Log.d(TAG, "Document id : " + userModelClass.getuId());


                                }else {
                                    dialog.cancel();
                                    new AlertDialog.Builder(SignUpActivity.this)
                                            .setMessage("This refer code has expired for today. Please tell his/her friend to send a new link tomorrow or Signup without refer code!")
                                            .setCancelable(true)
                                            .setNegativeButton("Cancel",null)
                                            .show();
                                    binding.signUpRefer.setText("");
                                }
                                flag = true;
                                break;
                            }
                        }

                        if (!flag) {
                            dialog.dismiss();
                            binding.signUpRefer.setError("wrong refer code!");
                            Log.d(TAG, "Error getting documents: ");
                        }

                    }
//                }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                binding.signUpRefer.setError("wrong refer code!");
//                Log.d(TAG, "Error getting documents: ");
//            }
                });
    }

    private void signUpNewUser(final long predefinedCoins) {
        final String userReferCode = getUniqueRefralCode();
        auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                dialog.dismiss();
                if (task.isSuccessful()) {
                    String uid = task.getResult().getUser().getUid();
                    final UserModelClass userModelClass = new UserModelClass(name, email, pass, userReferCode, predefinedCoins, uid);
                    editor = sf.edit();
                    editor.putString("ReferCode", userReferCode);
                    editor.putString("currentDate",formatter.format(new Date()));
                    editor.apply();
                    UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder()
                            .setDisplayName(name).build();
                    FirebaseAuth.getInstance().getCurrentUser().updateProfile(userProfileChangeRequest);
                    database
                            .collection("users")
                            .document(uid)
                            .set(userModelClass).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(SignUpActivity.this, "SignUp successful.", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                finish();
                            } else {
                                Toast.makeText(SignUpActivity.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    Toast.makeText(SignUpActivity.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void goToLoginPage(View view) {
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
        finish();
    }
}