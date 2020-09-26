package com.example.skymail.CloudMessaging;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.skymail.Data.Messages;
import com.example.skymail.R;
import com.example.skymail.Receiver;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;


public class MyFireBaseMessagingService extends FirebaseMessagingService {
    private static final String REQUEST_ACCEPT = "AC";
    Messages msg;
    String id;
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
            super.onMessageReceived(remoteMessage);
            SharedPreferences pref = getDefaultSharedPreferences(getApplicationContext());
            boolean nf=pref.getBoolean("notification",false);
            id=remoteMessage.getData().get("Id");
            if (nf){
                getMessageAndShowFCM(id);
            }

    }
    private void getMessageAndShowFCM(String id){
        new Thread(new Runnable() {
            @Override
            public void run() {
                DatabaseReference messagedatabase = FirebaseDatabase.getInstance().getReference();
                Query Query;
                Query = messagedatabase.child("InboxMessages").orderByChild("messagID").equalTo(id);
                Query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot messages : dataSnapshot.getChildren()) {
                                msg = messages.getValue(Messages.class);
                                assert msg != null;
                                showFCMNotification(msg);
                            }
                        } else {
                            Log.d("FCM", "No message matches ID failed to get message");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.d("FCM", "Connection to database Failed ");
                    }
                });
            }}).start();
    }

    public void RemoveAfter(String mID){
        new Thread(new Runnable() {
            @Override
            public void run() {
                DatabaseReference noti = FirebaseDatabase.getInstance().getReference();
                noti.child("Notifications").child(mID).removeValue();
            }
        }).start();
    }

    public void showFCMNotification(Messages msg) {
        SimpleDateFormat df = new SimpleDateFormat("HH:mm");
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel("FCM_CHANNEL",
                "CHANNEL_UPDATE",
                NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription("FCM_CHANNEL_DESCRIPTION");

        mNotificationManager.createNotificationChannel(channel);
        Intent open = new Intent(this, Receiver.class);
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
        PendingIntent openIntent = PendingIntent.getBroadcast(this,99,open,PendingIntent.FLAG_ONE_SHOT);
        Notification.Builder mBuilder = new Notification.Builder(this, "FCM_CHANNEL");
        RemoteViews contentView = new RemoteViews(this.getPackageName(), R.layout.notifications_layout);
        contentView.setTextViewText(R.id.title, msg.getSenderFullName());
        contentView.setTextViewText(R.id.text, msg.getSubject());
        contentView.setTextViewText(R.id.textContent, msg.getMessageText());
        contentView.setTextViewText(R.id.notification_email, ". "+msg.getFrom());
        contentView.setTextViewText(R.id.noti_time, df.format(Calendar.getInstance().getTime()));
        mBuilder.setSmallIcon(R.drawable.email)
        .setContent(contentView)
        .setAutoCancel(true)
        .setContentIntent(openIntent);
        Notification noti =  mBuilder.build();
        int notid=getRandomNumberUsingNextInt(0,99*1000);
        mNotificationManager.notify(notid,noti);
        Picasso.get().load(msg.getSenderProfilePicture()).resize(70, 70)
                .transform(new CropCircleTransformation()).into(contentView,R.id.notification_image,notid,noti);
        /*Intent intent = new Intent(REQUEST_ACCEPT);
        //pass ProfilePic link MainActivity5
        intent.putExtra("pic", msg.getSenderProfilePicture());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);*/
        RemoveAfter(id);
    }

    public int getRandomNumberUsingNextInt(int min, int max) {
        Random random = new Random();
        return random.nextInt(max - min) + min;
    }

    @Override
    public void onNewToken(String s)
    {
        Log.d("FireMessagingService", "Refreshed token: " + s);
        super.onNewToken(s);
        FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        String refreshToken= FirebaseInstanceId.getInstance().getToken();
        if(firebaseUser!=null){
            updateToken(refreshToken);
        }
    }
    private void updateToken(String refreshToken){
        FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        Token token1= new Token(refreshToken);
        Log.d("Tokenrefresh","New token");
        FirebaseDatabase.getInstance().getReference("Tokens").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(token1);
    }


}
