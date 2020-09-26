package com.example.skymail.CloudMessaging;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.skymail.Data.Messages;
import com.example.skymail.Data.Notifications;
import com.example.skymail.Data.Users;
import com.example.skymail.Message;
import com.example.skymail.R;
import com.example.skymail.Receiver;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.jetbrains.annotations.NotNull;

public class MyFireBaseMessagingService extends FirebaseMessagingService {
    Messages msg;
    String id;
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
            super.onMessageReceived(remoteMessage);
            id=remoteMessage.getData().get("Id");
            getMessageAndShowFCM(id);
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
                                showFCMNotification(msg);
                                RemoveAfter(id);
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
                Looper.prepare();
                DatabaseReference noti = FirebaseDatabase.getInstance().getReference();
                noti.child("Notifications").child(mID).removeValue();
            }
        }).start();
    }

    public void showFCMNotification(Messages msg) {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
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

            PendingIntent openIntent = PendingIntent.getBroadcast(this,7,open,PendingIntent.FLAG_ONE_SHOT);
            Notification.Builder mBuilder = new Notification.Builder(this, "FCM_CHANNEL");
                    mBuilder.setSmallIcon(R.drawable.email) // notification icon
                    .setContentTitle(msg.getSenderFullName()) // title for notification
                    .setSubText(msg.getFrom())    // email address for notification
                    .setContentText(msg.getSubject().toUpperCase()+":"+msg.getMessageText())// message for notification
                    .setAutoCancel(true)
                    .setContentIntent(openIntent);
            Notification noti =  mBuilder.build();
            mNotificationManager.notify(2,noti);
        }
    }
}
