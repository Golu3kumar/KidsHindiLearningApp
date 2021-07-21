package com.selfhelpindia.kidsquizapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.selfhelpindia.kidsquizapp.databinding.RewardSuccessfullBoardBinding;

public class RewardSuccessDialog {
    private Activity activity;
    private AlertDialog dialog;
    TextView coins;
    public RewardSuccessDialog(Activity activity) {
        this.activity = activity;
    }

    void startDialog(int getCoins) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.reward_successfull_board, null);
        coins = view.findViewById(R.id.reward_earned_coins);
        coins.setText(String.valueOf(getCoins));
        builder.setView(view);
        builder.setCancelable(true);
        dialog = builder.create();
        dialog.show();
    }

    void dismissDialog() {
        if (dialog != null)
            dialog.dismiss();
    }
}
