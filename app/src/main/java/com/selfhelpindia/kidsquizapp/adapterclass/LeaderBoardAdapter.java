package com.selfhelpindia.kidsquizapp.adapterclass;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.selfhelpindia.kidsquizapp.R;
import com.selfhelpindia.kidsquizapp.databinding.LeaderBoardSampleBinding;
import com.selfhelpindia.kidsquizapp.modelclass.UserModelClass;

import java.util.ArrayList;

public class LeaderBoardAdapter extends RecyclerView.Adapter<LeaderBoardAdapter.LeaderBoardViewHolder> {

    Context context;
    ArrayList<UserModelClass> userModelClassArrayList;
    private static final String TAG = "LeaderBoardAdapter";

    public LeaderBoardAdapter(Context context, ArrayList<UserModelClass> userModelClassArrayList) {
        this.context = context;
        this.userModelClassArrayList = userModelClassArrayList;
    }

    @NonNull
    @Override
    public LeaderBoardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.leader_board_sample,parent,false);
        return new LeaderBoardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LeaderBoardViewHolder holder, int position) {
        holder.binding.userBg.requestLayout();
        UserModelClass userModelClass = userModelClassArrayList.get(position);
        if (userModelClass.getuId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
            holder.binding.userBg.removeAllViews();
            return;
        }

        holder.binding.userName.setText(userModelClass.getName());
        holder.binding.userPoints.setText(String.valueOf(userModelClass.getCoins()));
        holder.binding.userRank.setText(String.format("#%d",position+1));
        if (userModelClass.getProfileUrl()!=null){
            //Log.d(TAG, "onBindViewHolder:"+userModelClass.getProfileUrl());
            Glide.with(context).load(Uri.parse(userModelClass.getProfileUrl())).into(holder.binding.userProfile);
        }else {
            holder.binding.userProfile.setImageResource(R.drawable.profile);
        }
    }

    @Override
    public int getItemCount() {
        return userModelClassArrayList.size();
    }

    public static class LeaderBoardViewHolder extends RecyclerView.ViewHolder{

        LeaderBoardSampleBinding binding;
        public LeaderBoardViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = LeaderBoardSampleBinding.bind(itemView);
        }
    }
}
