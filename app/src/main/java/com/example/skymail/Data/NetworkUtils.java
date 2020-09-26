package com.example.skymail.Data;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.IOException;

public class NetworkUtils {
    public static boolean isConnected() {
        final String command = "ping -c 1 https://fcm.googleapis.com/";
        boolean isConnected = false;
        try {
            isConnected = Runtime.getRuntime().exec(command).waitFor() == 0;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return isConnected;
    }

    public static final boolean isInternetOn(Context ctx) {
        ConnectivityManager conMgr = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = conMgr.getActiveNetworkInfo();
        return (info != null && info.isConnected());
    }
}
