package com.example.firebaseoc;

import android.app.Application;
import android.content.Context;

/**
 * Created by Yassine Abou on 5/19/2021.
 */


public class App extends Application{

        private static Context mContext;

        @Override
        public void onCreate() {
            super.onCreate();
            mContext = this;
        }

        public static Context getContext(){
            return mContext;
        }
}

