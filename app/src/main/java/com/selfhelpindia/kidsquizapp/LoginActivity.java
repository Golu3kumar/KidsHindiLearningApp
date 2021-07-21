package com.selfhelpindia.kidsquizapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.selfhelpindia.kidsquizapp.databinding.ActivityLoginActivtiyBinding;
import com.selfhelpindia.kidsquizapp.modelclass.UserModelClass;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.selfhelpindia.kidsquizapp.SignUpActivity.REFER_CODE_SF;

public class LoginActivity extends AppCompatActivity {

    ActivityLoginActivtiyBinding binding;
    FirebaseAuth auth;
    ProgressDialog dialog;
    FirebaseFirestore database;
    SharedPreferences sf1;
    SharedPreferences.Editor editor;
    SimpleDateFormat formatter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginActivtiyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        auth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();
        sf1 = getSharedPreferences(REFER_CODE_SF,MODE_PRIVATE);
        formatter = new SimpleDateFormat("dd/MM/yyyy");
        dialog = new ProgressDialog(this);
        binding.loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email,pass;

                email = binding.loginEmail.getText().toString().trim();
                pass = binding.loginPassword.getText().toString().trim();

                if (email.isEmpty() || pass.isEmpty())
                    return;
                dialog.setMessage("Please wait...");
                dialog.show();
                auth.signInWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()){
                            Toast.makeText(LoginActivity.this, "Login Successful.", Toast.LENGTH_SHORT).show();
                            database.collection("users")
                                    .document(auth.getCurrentUser().getUid())
                                    .update("pass",pass);
                            database.collection("users")
                                    .document(auth.getCurrentUser().getUid())
                                    .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    UserModelClass userModelClass= documentSnapshot.toObject(UserModelClass.class);
                                    editor = sf1.edit();
                                    editor.putString("ReferCode", userModelClass.getReferCode());
                                    editor.putString("currentDate",formatter.format(new Date()));
                                    editor.apply();
                                }
                            });
                            dialog.dismiss();
                            startActivity(new Intent(getApplicationContext(),MainActivity.class));
                            finish();
                        }else {
                            dialog.dismiss();
                            Toast.makeText(LoginActivity.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        binding.forgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                builder.setTitle("Recover Password");
                builder.setCancelable(false);

                LinearLayout linearLayout = new LinearLayout(LoginActivity.this);
                final EditText getEmail = new EditText(LoginActivity.this);
                getEmail.setHint("Enter Email ");
                getEmail.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                getEmail.setMinEms(16);
                linearLayout.addView(getEmail);
                linearLayout.setPadding(10,10,10,10);
                builder.setView(linearLayout);
                builder.setPositiveButton("Recover", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String getEmailFormUser = getEmail.getText().toString().trim();
                        if (getEmailFormUser.isEmpty()){
                            Toast.makeText(LoginActivity.this, "Email Required!", Toast.LENGTH_LONG).show();
                            return;
                        }
                        beginRecovery(getEmailFormUser);
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                builder.create().show();
            }
        });
    }

    private void beginRecovery(String getEmailFormUser) {
        dialog.setMessage("Sending Email...");
        dialog.show();
        auth.sendPasswordResetEmail(getEmailFormUser)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        dialog.dismiss();
                        if (task.isSuccessful()){
                            Toast.makeText(LoginActivity.this, "Email sent!", Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(LoginActivity.this, "Failed...", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(LoginActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void goToRegistrationPage(View view) {
        Intent intent = new Intent(getApplicationContext(),SignUpActivity.class);
        startActivity(intent);
        finish();
    }
}