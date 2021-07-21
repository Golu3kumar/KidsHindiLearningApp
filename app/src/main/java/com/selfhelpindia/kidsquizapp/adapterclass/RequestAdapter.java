package com.selfhelpindia.kidsquizapp.adapterclass;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.selfhelpindia.kidsquizapp.R;
import com.selfhelpindia.kidsquizapp.databinding.RequestSampleBinding;
import com.selfhelpindia.kidsquizapp.modelclass.PaymentRequestModelClass;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder> {

    Context context;
    ArrayList<PaymentRequestModelClass> paymentRequestModelClassArrayList;
    private static final String TAG = "RequestAdapter";
    Format dateFormat;

    public RequestAdapter(Context context, ArrayList<PaymentRequestModelClass> paymentRequestModelClassArrayList) {
        this.context = context;
        this.paymentRequestModelClassArrayList = paymentRequestModelClassArrayList;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.request_sample, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        PaymentRequestModelClass paymentRequestModelClass = paymentRequestModelClassArrayList.get(position);
        dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        holder.requestSampleBinding.requestDate.setText(String.valueOf(dateFormat.format(paymentRequestModelClass.getCreatedAt())));
        //Date date = paymentRequestModelClass.getCreatedAt();
        holder.requestSampleBinding.requestAmount.setText(String.valueOf(paymentRequestModelClass.getAmount()));
        Log.d(TAG, "onBindViewHolder: " + paymentRequestModelClass.getAmount());
        if (paymentRequestModelClass.getStatus() == 0)
            holder.requestSampleBinding.requestStatus.setText("Pending");
        else if (paymentRequestModelClass.getStatus() == 1)
            holder.requestSampleBinding.requestStatus.setText("Successful");
    }

    @Override
    public int getItemCount() {
        return paymentRequestModelClassArrayList.size();
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder {

        RequestSampleBinding requestSampleBinding;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            requestSampleBinding = RequestSampleBinding.bind(itemView);
        }
    }
}
