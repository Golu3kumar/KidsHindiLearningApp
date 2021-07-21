package com.selfhelpindia.kidsquizapp;

import android.net.Uri;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.selfhelpindia.kidsquizapp.adapterclass.LeaderBoardAdapter;
import com.selfhelpindia.kidsquizapp.databinding.FragmentLeaderboardBinding;
import com.selfhelpindia.kidsquizapp.modelclass.UserModelClass;

import java.util.ArrayList;


public class LeaderBoardFragment extends Fragment {

    private static final String TAG = "LeaderBoardFragment";
    //private static final int STORAGE_PERMISSION_CODE = 1;
    int count = 0;
    LoadingDialog loadingDialog;
    public LeaderBoardFragment() {
        // Required empty public constructor

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

    }

    FragmentLeaderboardBinding leaderboardBinding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        loadingDialog = new LoadingDialog(getActivity());
        loadingDialog.startDialog();
        // Inflate the layout for this fragment
        leaderboardBinding = FragmentLeaderboardBinding.inflate(inflater, container, false);
        final ArrayList<UserModelClass> userModelClassArrayList = new ArrayList<>();
        final LeaderBoardAdapter leaderBoardAdapter = new LeaderBoardAdapter(getContext(), userModelClassArrayList);
        leaderboardBinding.leaderBoardRecyclerView.setAdapter(leaderBoardAdapter);
        leaderboardBinding.leaderBoardRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection("users")
                .orderBy("coins", Query.Direction.DESCENDING)
                .limit(60)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                    count++;
                    UserModelClass userModelClass = snapshot.toObject(UserModelClass.class);
                    if (userModelClass.getuId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                        leaderboardBinding.include.userRank.setText(String.format("#%d", count));
                        leaderboardBinding.include.userName.setText(userModelClass.getName());
                        leaderboardBinding.include.userPoints.setText(String.valueOf(userModelClass.getCoins()));
                        if (userModelClass.getProfileUrl() != null)
                            Glide.with(getContext()).load(Uri.parse(userModelClass.getProfileUrl())).into(leaderboardBinding.include.userProfile);
                        else
                            leaderboardBinding.include.userProfile.setImageResource(R.drawable.profile);
                        leaderboardBinding.include.userBg.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bg_with_color));
                    }
                    userModelClassArrayList.add(userModelClass);
                }
                leaderBoardAdapter.notifyDataSetChanged();
                loadingDialog.dismissDialog();
            }
        });
        return leaderboardBinding.getRoot();
    }
}