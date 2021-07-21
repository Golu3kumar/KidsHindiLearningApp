package com.selfhelpindia.kidsquizapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.selfhelpindia.kidsquizapp.adapterclass.RequestAdapter;
import com.selfhelpindia.kidsquizapp.databinding.ActivityPaymentRequestBinding;
import com.selfhelpindia.kidsquizapp.modelclass.PaymentRequestModelClass;

import java.util.ArrayList;

public class PaymentRequest extends AppCompatActivity {

    FirebaseFirestore database;
    ActivityPaymentRequestBinding binding;
    private static final String TAG = "PaymentRequest";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPaymentRequestBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        database = FirebaseFirestore.getInstance();
        final ArrayList<PaymentRequestModelClass> paymentRequestModelClassArrayList = new ArrayList<>();
        final RequestAdapter requestAdapter = new RequestAdapter(getApplicationContext(),paymentRequestModelClassArrayList);
        binding.requestRecyclerView.setAdapter(requestAdapter);
        binding.requestRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        database.collection("withdrawal_requests")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("multiple_requests")
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots){
                    Log.d(TAG, "onSuccess: Data fetched");
                    PaymentRequestModelClass paymentRequestModelClass = documentSnapshot.toObject(PaymentRequestModelClass.class);
                    paymentRequestModelClassArrayList.add(paymentRequestModelClass);
                    Log.d(TAG, "onSuccess: "+paymentRequestModelClass.getAmount()+paymentRequestModelClass.getPaytmNumber());
                }
                requestAdapter.notifyDataSetChanged();
                Log.d(TAG, "onSuccess: Complete");
            }
        });

        binding.close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}