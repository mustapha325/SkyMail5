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
import android.view.View;
import android.widget.ImageView;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import com.bumptech.glide.Glide;
import com.example.skymail.Message;
import com.example.skymail.R;
import com.example.skymail.Receiver;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;
import java.util.Scanner;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.UI_MODE_SERVICE;
import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;


public class io {
    private static final String FILE_NAME = "localdata";
    private static final String TAG ="ClearUserData" ;
    private static final String REQUEST_ACCEPT = "AC";
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
                        case ("password"):
                            user.setPassword(divide[1]);
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
        SharedPreferences p=getDefaultSharedPreferences(ctx);
        if(p.getString("FirstTime","true").equals("true")) {
            p.edit().putString("FirstTime","false").apply();
            clearuserdata(ctx);}
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

    public static void showFCMSync(Context ctx, Messages msg) {
        NotificationManager mNotificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel("FCM_CHANNEL",
                "CHANNEL_UPDATE",
                NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription("FCM_CHANNEL_DESCRIPTION");
        mNotificationManager.createNotificationChannel(channel);
        Intent open = new Intent(ctx, Receiver.class);

        open.putExtra("remail",msg.getFrom())
                .putExtra("picture",msg.getSenderProfilePicture())
                .putExtra("action","FCM")
                .putExtra("subject",msg.getSubject())
                .putExtra("text",msg.getMessageText())
                .putExtra("FULLNAME",msg.getSenderFullName())
                .putExtra("url",msg.getFileurl())
                .putExtra("message_id",msg.getMessagID())
                .putExtra("from",msg.getFrom())
                .putExtra("object",msg.getObject())
                .putExtra("id",msg.getUserID());
        RemoteViews contentView = new RemoteViews(ctx.getPackageName(), R.layout.notifications_layout);
        contentView.setTextViewText(R.id.title, msg.getSenderFullName());
        contentView.setTextViewText(R.id.text, msg.getSubject());
        contentView.setTextViewText(R.id.textContent, msg.getMessageText());
        SimpleDateFormat df = new SimpleDateFormat("HH:mm");
        contentView.setTextViewText(R.id.notification_email, ". "+msg.getFrom());
        contentView.setTextViewText(R.id.noti_time, df.format(Calendar.getInstance().getTime()));

        PendingIntent openIntent = PendingIntent.getBroadcast(ctx,9,open,PendingIntent.FLAG_ONE_SHOT);
        Notification.Builder mBuilder = new Notification.Builder(ctx, "FCM_CHANNEL");
        mBuilder.setSmallIcon(R.drawable.email) // notification icon
                .setContent(contentView)
                .setAutoCancel(true)
                .setContentIntent(openIntent);
        Notification noti =  mBuilder.build();
        int notid=getRandomNumberUsingNextInt(0,99*1000);
        mNotificationManager.notify(notid,noti);
        Picasso.get().load(msg.getSenderProfilePicture()).resize(70, 70)
                .transform(new CropCircleTransformation()).into(contentView,R.id.notification_image,notid,noti);
        /*
        Intent intent = new Intent(REQUEST_ACCEPT);
        //pass ProfilePic link MainActivity5
        intent.putExtra("pic", nt.getProfilePic());
        LocalBroadcastManager.getInstance(ctx).sendBroadcast(intent);*/
        RemoveAfter(msg.getMessagID());
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