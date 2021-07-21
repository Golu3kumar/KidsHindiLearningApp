package com.selfhelpindia.kidsquizapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.selfhelpindia.kidsquizapp.databinding.ActivityQuizBinding;
import com.selfhelpindia.kidsquizapp.modelclass.QuestionModelClass;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class QuizActivity extends AppCompatActivity {

    ActivityQuizBinding binding;
    ArrayList<QuestionModelClass> questionModelClasses;
    QuestionModelClass questionModelClass;
    CountDownTimer countDownTimer;
    int index, correctAnswerCounter, wrongAnswerCounter = 0;
    FirebaseFirestore database;
    private static final String TAG = "QuizActivity";
    LoadingDialog loadingDialog;
    String categoryId;
    ArrayList<String> optionList;
    ArrayList<View> viewArrayList;
    //SharedPreferences sf;
    boolean isFiftyFiftyAvailable = true;
    RewardedAd rewardedAd;
//    SharedPreferences.Editor editor;
//    public static final String FIFTY_FIFTY_PREF = "fifty_fifty_pref";
//    public static final String FIFTY_FIFTY_FLAG_NAME = "fifty_fifty_available";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQuizBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        questionModelClasses = new ArrayList<>();
        optionList = new ArrayList<>();
        viewArrayList = new ArrayList<>();
        database = FirebaseFirestore.getInstance();
        //sf = getSharedPreferences(FIFTY_FIFTY_PREF, MODE_PRIVATE);
        loadingDialog = new LoadingDialog(this);
        loadingDialog.startDialog();
        categoryId = getIntent().getStringExtra("categoryId");
        Log.d(TAG, "categoryId: " + categoryId);
        Random r = new Random();
        int low = 1;
        int high = 30;
        //TODO fix this high value
        final int randomNumber = r.nextInt(high - low) + low;
        database.collection("categories")
                .document(categoryId)
                .collection("questions")
                .whereGreaterThanOrEqualTo("index", randomNumber)
                .orderBy("index")
                .limit(10).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (queryDocumentSnapshots.getDocuments().size() < 10) {
                    database.collection("category")
                            .document(categoryId)
                            .collection("questions")
                            .whereLessThanOrEqualTo("index", randomNumber)
                            .orderBy("index")
                            .limit(10).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                            for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                                QuestionModelClass questionModelClass = snapshot.toObject(QuestionModelClass.class);
                                questionModelClasses.add(questionModelClass);
                                Collections.shuffle(questionModelClasses);
                            }
                            loadingDialog.dismissDialog();
                            setNextQuestion();
                        }
                    });
                } else {
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                        QuestionModelClass questionModelClass = snapshot.toObject(QuestionModelClass.class);
                        questionModelClasses.add(questionModelClass);
                        Collections.shuffle(questionModelClasses);
                    }
                    loadingDialog.dismissDialog();
                    setNextQuestion();
                }
            }
        });

        MobileAds.initialize(getApplicationContext(), new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });


        binding.fiftyFiftyOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFiftyFiftyAvailable) {
                    //editor = sf.edit();
                    isFiftyFiftyAvailable = false;
                    //editor.putBoolean(FIFTY_FIFTY_FLAG_NAME, false);
                    //editor.apply();
                    binding.fiftyFiftyLock.setVisibility(View.VISIBLE);
                    hideWrongAnswers();
                } else {
                    //Toast.makeText(QuizActivity.this, "You don't have Fifty Fifty option!", Toast.LENGTH_SHORT).show();
                    if (countDownTimer!=null)
                        countDownTimer.cancel();

                    new AlertDialog.Builder(QuizActivity.this)
                            .setMessage("You don't have Fifty Fifty, to get it click on 'Get it!'")
                            .setCancelable(false)
                            .setPositiveButton("Get it!", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    showAds();
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    resetCounter();
                                    countDownTimer.start();
                                }
                            })
                            .show();
                    
                }
            }
        });

    }

    private void showAds() {
        AdRequest adRequest = new AdRequest.Builder().build();

        RewardedAd.load(getApplicationContext(), getResources().getString(R.string.fifty_fifty_ads_id),
                adRequest, new RewardedAdLoadCallback() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error.
                        Log.d(TAG, loadAdError.getMessage());
                        rewardedAd = null;
                    }

                    @Override
                    public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                       QuizActivity.this.rewardedAd = rewardedAd;
                        Log.d(TAG, "Ad loaded!");
                    }
                });

        if (rewardedAd != null) {
            Activity activityContext = QuizActivity.this;
            rewardedAd.show(activityContext, new OnUserEarnedRewardListener() {
                @Override
                public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                    Toast.makeText(getApplicationContext(), "Congratulation! You got Fifty Fifty!", Toast.LENGTH_SHORT).show();
                    isFiftyFiftyAvailable = true;
                    binding.fiftyFiftyLock.setVisibility(View.INVISIBLE);
                    resetCounter();
                    countDownTimer.start();
                    int rewardAmount = rewardItem.getAmount();
                    String rewardType = rewardItem.getType();
                    Log.d(TAG, "onUserEarnedReward: " + rewardAmount);
                }

            });
        } else {
            Log.d("TAG", "The rewarded ad wasn't ready yet.");
        }

    }

    private void hideWrongAnswers() {
        if (countDownTimer != null)
            countDownTimer.cancel();
        resetCounter();
        countDownTimer.start();
        viewArrayList.clear();
        if (binding.option1.getText().toString().toLowerCase().equals(questionModelClass.getAns().toLowerCase())) {
            viewArrayList.add(binding.option2);
            viewArrayList.add(binding.option3);
            viewArrayList.add(binding.option4);
            Collections.shuffle(viewArrayList);
            viewArrayList.get(0).setVisibility(View.INVISIBLE);
            viewArrayList.get(1).setVisibility(View.INVISIBLE);

        } else if (binding.option2.getText().toString().toLowerCase().equals(questionModelClass.getAns().toLowerCase())) {
            viewArrayList.add(binding.option1);
            viewArrayList.add(binding.option3);
            viewArrayList.add(binding.option4);
            Collections.shuffle(viewArrayList);
            viewArrayList.get(0).setVisibility(View.INVISIBLE);
            viewArrayList.get(1).setVisibility(View.INVISIBLE);

        } else if (binding.option3.getText().toString().toLowerCase().equals(questionModelClass.getAns().toLowerCase())) {
            viewArrayList.add(binding.option1);
            viewArrayList.add(binding.option2);
            viewArrayList.add(binding.option4);
            Collections.shuffle(viewArrayList);
            viewArrayList.get(0).setVisibility(View.INVISIBLE);
            viewArrayList.get(1).setVisibility(View.INVISIBLE);

        } else if (binding.option4.getText().toString().toLowerCase().equals(questionModelClass.getAns().toLowerCase())) {
            viewArrayList.add(binding.option1);
            viewArrayList.add(binding.option2);
            viewArrayList.add(binding.option3);
            Collections.shuffle(viewArrayList);
            viewArrayList.get(0).setVisibility(View.INVISIBLE);
            viewArrayList.get(1).setVisibility(View.INVISIBLE);
        }
    }

    private void resetCounter() {
        countDownTimer = new CountDownTimer(21000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                binding.timer.setText(String.valueOf(millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                Toast.makeText(QuizActivity.this, "Your time is finished!", Toast.LENGTH_SHORT).show();
                manageAutomaticQuestion();
            }
        };
    }

    private void quizQuitDialog() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure want to quit the Quiz?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        showResultActivity();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void setNextQuestion() {
        if (countDownTimer != null)
            countDownTimer.cancel();
        if (index < questionModelClasses.size()) {
            optionList.clear();
            resetCounter();
            countDownTimer.start();
            binding.questionCounter.setText(String.format("%d/%d", index + 1, questionModelClasses.size()));
            questionModelClass = questionModelClasses.get(index);
            optionList.add(questionModelClass.getA());
            optionList.add(questionModelClass.getB());
            optionList.add(questionModelClass.getC());
            optionList.add(questionModelClass.getD());

            Collections.shuffle(optionList);
            binding.question.setText(questionModelClass.getQ());
            binding.option1.setText(optionList.get(0));
            binding.option2.setText(optionList.get(1));
            binding.option3.setText(optionList.get(2));
            binding.option4.setText(optionList.get(3));
        }

    }

    private void showAnswer() {
        if (binding.option1.getText().toString().toLowerCase().equals(questionModelClass.getAns().toLowerCase()))
            binding.option1.setBackground(getResources().getDrawable(R.drawable.correct_option));
        else if (binding.option2.getText().toString().toLowerCase().equals(questionModelClass.getAns().toLowerCase()))
            binding.option2.setBackground(getResources().getDrawable(R.drawable.correct_option));
        else if (binding.option3.getText().toString().toLowerCase().equals(questionModelClass.getAns().toLowerCase()))
            binding.option3.setBackground(getResources().getDrawable(R.drawable.correct_option));
        else if (binding.option4.getText().toString().toLowerCase().equals(questionModelClass.getAns().toLowerCase()))
            binding.option4.setBackground(getResources().getDrawable(R.drawable.correct_option));
    }

    private void checkAnswer(TextView textView) {
        String selectedAnswer = textView.getText().toString().toLowerCase();
        if (selectedAnswer.equals(questionModelClass.getAns().toLowerCase())) {
            textView.setBackground(getResources().getDrawable(R.drawable.correct_option));
            correctAnswerCounter++;
        } else {
            textView.setBackground(getResources().getDrawable(R.drawable.wrong_option));
            wrongAnswerCounter++;
            showAnswer();
        }
        binding.option1.setClickable(false);
        binding.option2.setClickable(false);
        binding.option3.setClickable(false);
        binding.option4.setClickable(false);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                manageAutomaticQuestion();
            }
        }, 1500);
    }

    private void resetQuestionOption() {
        binding.option1.setBackground(getResources().getDrawable(R.drawable.unselected_option));
        binding.option2.setBackground(getResources().getDrawable(R.drawable.unselected_option));
        binding.option3.setBackground(getResources().getDrawable(R.drawable.unselected_option));
        binding.option4.setBackground(getResources().getDrawable(R.drawable.unselected_option));

        binding.option1.setVisibility(View.VISIBLE);
        binding.option2.setVisibility(View.VISIBLE);
        binding.option3.setVisibility(View.VISIBLE);
        binding.option4.setVisibility(View.VISIBLE);

        binding.option1.setClickable(true);
        binding.option2.setClickable(true);
        binding.option3.setClickable(true);
        binding.option4.setClickable(true);
    }

    private void manageAutomaticQuestion() {
        index++;
        if (index < questionModelClasses.size()) {
            resetQuestionOption();
            setNextQuestion();
        } else {
            Toast.makeText(this, "Quiz finished!", Toast.LENGTH_SHORT).show();
            showResultActivity();

        }
    }


    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.option_1:
            case R.id.option_2:
            case R.id.option_3:
            case R.id.option_4:
                if (countDownTimer != null)
                    countDownTimer.cancel();
                TextView selected = (TextView) view;
                checkAnswer(selected);
                break;
            case R.id.quit:
            case R.id.quit_image:
                quizQuitDialog();

        }
    }

    private void showResultActivity() {
        Intent intent = new Intent(getApplicationContext(), ResultActivity.class);
        intent.putExtra("correctAnswer", correctAnswerCounter);
        intent.putExtra("wrongAnswer", wrongAnswerCounter);
        intent.putExtra("questionCounter", index);
        intent.putExtra("categoryId", categoryId);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        if (countDownTimer != null)
            countDownTimer.cancel();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        loadingDialog.dismissDialog();
        quizQuitDialog();
    }
}