package com.example.skymail.SplashScreen;

import androidx.appcompat.app.AppCompatActivity;
import androidx.slidingpanelayout.widget.SlidingPaneLayout;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;

import com.example.skymail.MainActivity;
import com.example.skymail.MainActivity5;
import com.example.skymail.R;
import com.example.skymail.ShowProfile;

import java.util.Locale;

import static com.example.skymail.Data.io.CheckUserDataPresence;
import static com.example.skymail.Data.io.changeTheme;
import static com.example.skymail.Data.io.getLocale;

public class SplashScreen extends AppCompatActivity {
    private static final int  SPLASH_TIME_OUT =1200 ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setApplicationLanguage(getLocale(getApplicationContext()));
        setContentView( R.layout.activity_splash_screen );
        changeTheme(getApplicationContext());
        boolean back = CheckUserDataPresence(getApplicationContext());
        Intent intent = new Intent( SplashScreen.this, MainActivity.class );
        new Handler().postDelayed( new Runnable() {
            @Override
            public void run() {
                if(back) startActivity(new Intent(SplashScreen.this, MainActivity5.class));
                else startActivity( intent );
                finish();
            }
        },SPLASH_TIME_OUT );
    }

    public void setApplicationLanguage(String newLanguage) {
        Resources activityRes = getResources();
        Configuration activityConf = activityRes.getConfiguration();
        Locale newLocale = new Locale(newLanguage);
        activityConf.setLocale(newLocale);
        activityRes.updateConfiguration(activityConf, activityRes.getDisplayMetrics());
        Resources applicationRes = getResources();
        Configuration applicationConf = applicationRes.getConfiguration();
        applicationConf.setLocale(newLocale);
        applicationRes.updateConfiguration(applicationConf,
                applicationRes.getDisplayMetrics());
        getResources().updateConfiguration(activityConf, getApplicationContext().getResources().getDisplayMetrics());
    }
}
