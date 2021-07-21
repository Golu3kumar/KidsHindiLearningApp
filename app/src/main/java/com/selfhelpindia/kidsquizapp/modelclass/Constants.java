package com.selfhelpindia.kidsquizapp.modelclass;

import java.util.ArrayList;

public class Constants {

    public static final int COINS_PER_RIGHT_ANSWER = 20;

    public static final double COINS_PER_INR = 100.0;
    public static final long COINS_PER_REFERRAL = 1000;
    public static final double COINS_NEED_FOR_WITHDRAWAL = 10000.0;
    public static final double INR_NEED_FOR_WITHDRAWAL = 100.0;

    public static final ArrayList<Integer> EXTRA_COINS_EARNED_BY_ADS = new ArrayList<Integer>(){
        {
            add(50);
            add(100);
            add(150);
            add(200);
            add(500);
        }
    };

}
