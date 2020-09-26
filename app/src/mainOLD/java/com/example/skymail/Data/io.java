package com.example.skymail.Data;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.UiModeManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

import com.example.skymail.R;
import com.example.skymail.Receiver;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;
import java.util.Scanner;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.UI_MODE_SERVICE;
import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;


public class io {
    private static final String FILE_NAME = "localdata";
    private static final String TAG ="ClearUserData" ;
    static Users user = new Users("null","null","null","null","null","null","null","null");
    public static void restartApplication(final @NonNull Activity activity) {
        final PackageManager pm = activity.getPackageManager();
        final Intent intent = pm.getLaunchIntentForPackage(activity.getPackageName());
        activity.finishAffinity();
        activity.startActivity(intent);
        System.exit(0);
    }

    public static String getLocale(Context ctx) {
        //Language par defaut f tlfn--->defValue
        SharedPreferences prefs = getDefaultSharedPreferences(ctx);
        String deviceLocale = Locale.getDefault().getLanguage();
        String c = prefs.getString("locale",deviceLocale);
        return c;
    }

    public static void changeTheme(Context ctx){
        SharedPreferences prefs = getDefaultSharedPreferences(ctx);
        boolean Theme = prefs.getBoolean( "Theme", false );
        if (!Theme){
            AppCompatDelegate.setDefaultNightMode( AppCompatDelegate.MODE_NIGHT_NO );
        }else {
            AppCompatDelegate.setDefaultNightMode( AppCompatDelegate.MODE_NIGHT_YES );
        }
    }

    public static void store(String txt, Context ctx){
        clearuserdata(ctx);
        //String text = load("")+";"+txt;
        FileOutputStream fos = null;
        try {
            fos = ctx.openFileOutput(FILE_NAME, MODE_PRIVATE );
            fos.write( txt.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static Users access (Context ctx) {
        String content = null;
        FileInputStream fis = null;
        String[] multi;
        String[] divide;
        try {
            fis = ctx.openFileInput(FILE_NAME);
            Scanner scanner = new Scanner(fis);
            scanner.useDelimiter("//Z");
            content = scanner.next();
            scanner.close();
            if (content.contains(";")) {
                multi = content.split(";");
                for (String c : multi) {
                    divide = c.split(":");
                    switch (divide[0]) {
                        case ("email"):
                            user.setEmail(divide[1]);
                            break;
                        case ("name"):
                            user.setFullname(divide[1]);
                            break;
                        case ("id"):
                            user.setUserID(divide[1]);
                            break;
                        case ("number"):
                            user.setPhonenumber(divide[1]);
                            break;
                        case ("date"):
                            user.setBirthdate(divide[1]);
                            break;
                        case ("gender"):
                            user.setGender(divide[1]);
                            break;
                        case ("inscription"):
                            user.setInscriptionDate(divide[1]);
                            break;
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        finally {
            try {
                if (fis!=null)
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return user;
    }

    public static void clearuserdata (Context ctx){
    if(ctx.deleteFile(FILE_NAME))
    Log.i(TAG, "localdata deleted.");
     else Log.i(TAG, "Error localdata does not exist.");
     String s[]=ctx.fileList();
     for (String x: s) {
         if(x.equals(FILE_NAME))  Log.i(TAG, "localdata was not deleted !.");
     }
    }

    public static boolean  CheckUserDataPresence (Context ctx){
        FileInputStream fis = null;
        String s[]=ctx.fileList();
        for (String x: s) {
            if(x.equals(FILE_NAME))  {
                Log.i(TAG, "localdata exists!.");
                try {
                    fis = ctx.openFileInput(FILE_NAME);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                try {
                    if (fis.read() == -1) {
                        // empty file
                        return false;
                    } else {
                        // not empty
                        return true;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    static boolean isEmptyFile(String source) {
        try {
            for (String line : Files.readAllLines(Paths.get(source))) {
                if (line != null && !line.trim().isEmpty()) {
                    return false;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Default to true.
        return true;
    }

    public static void showFCMSync(Notifications nt,Context ctx) {
        NotificationManager mNotificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("FCM_CHANNEL",
                    "CHANNEL_UPDATE",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("FCM_CHANNEL_DESCRIPTION");
            mNotificationManager.createNotificationChannel(channel);
            Intent open = new Intent(ctx, Receiver.class);
            open.putExtra("remail",nt.getEmail())
                    .putExtra("picture",nt.getProfilePic())
                    .putExtra("action","FCM")
                    .putExtra("subject",nt.getSubject())
                    .putExtra("text",nt.getMessageText())
                    .putExtra("FULLNAME",nt.getFullname())
                    .putExtra("url",nt.getFileUrl());
            PendingIntent openIntent = PendingIntent.getBroadcast(ctx,7,open,PendingIntent.FLAG_ONE_SHOT);
            Notification.Builder mBuilder = new Notification.Builder(ctx, "FCM_CHANNEL");
            mBuilder.setSmallIcon(R.drawable.email) // notification icon
                    .setContentTitle(nt.getFullname()) // title for notification
                    .setSubText(nt.getEmail())    // email address for notification
                    .setContentText(nt.getSubject().toUpperCase()+":"+nt.getMessageText())// message for notification
                    .setAutoCancel(true)
                    .setContentIntent(openIntent);
            Notification noti =  mBuilder.build();
            mNotificationManager.notify(nt.getNotificationID(),noti);
            RemoveAfter(nt.getMessageID());
        }
    }

    public static void RemoveAfter(String mID){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                DatabaseReference noti = FirebaseDatabase.getInstance().getReference();
                noti.child("Notifications").child(mID).removeValue();
            }
        }).start();
    }

    public static int getRandomNumberUsingNextInt(int min, int max) {
        Random random = new Random();
        return random.nextInt(max - min) + min;
    }

}