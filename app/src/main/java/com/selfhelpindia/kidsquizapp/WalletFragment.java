package com.selfhelpindia.kidsquizapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.selfhelpindia.kidsquizapp.databinding.FragmentWalletBinding;
import com.selfhelpindia.kidsquizapp.modelclass.PaymentRequestModelClass;
import com.selfhelpindia.kidsquizapp.modelclass.UserModelClass;

import static com.selfhelpindia.kidsquizapp.modelclass.Constants.COINS_PER_INR;
import static com.selfhelpindia.kidsquizapp.modelclass.Constants.INR_NEED_FOR_WITHDRAWAL;

public class WalletFragment extends Fragment {

    FirebaseFirestore database;
    FragmentWalletBinding binding;
    UserModelClass userModelClass;
    private double currentINR;
    private static final String TAG = "WalletFragment";

    public WalletFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentWalletBinding.inflate(inflater, container, false);
        database = FirebaseFirestore.getInstance();
        database.collection("users").document(FirebaseAuth.getInstance().getUid()).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        userModelClass = documentSnapshot.toObject(UserModelClass.class);
                        binding.currentCoins.setText(String.valueOf(userModelClass.getCoins()));
                        currentINR = userModelClass.getCoins() / COINS_PER_INR;
                        binding.currentBalance.setText("₹" + currentINR);
                    }
                });

        binding.sendRequestForWithdraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String paytmNumber = binding.paytmNumber.getText().toString().trim();
                if (currentINR < INR_NEED_FOR_WITHDRAWAL) {
                    Toast.makeText(getContext(), "Your amount is less than ₹100", Toast.LENGTH_LONG).show();
                    return;
                } else if (paytmNumber.isEmpty()) {
                    binding.paytmNumber.setError("Paytm No. required!");
                    return;
                }

                PaymentRequestModelClass paymentRequestModelClass = new PaymentRequestModelClass(
                        FirebaseAuth.getInstance().getCurrentUser().getUid(),
                        paytmNumber,
                        currentINR
                );

                database.collection("withdrawal_requests")
                        .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .collection("multiple_requests")
                        .document(String.valueOf(System.currentTimeMillis()))
                        .set(paymentRequestModelClass).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        database.collection("users")
                                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .update("coins", 0L);
                        currentINR = 0.0;
                        Toast.makeText(getContext(), "Request sent Successfully", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

        binding.previousRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().startActivity(new Intent(getContext(),PaymentRequest.class));
            }
        });
        return binding.getRoot();
    }
}