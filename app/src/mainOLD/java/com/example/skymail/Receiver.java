 package com.example.skymail;

 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Build;
 import android.util.Log;

 import androidx.annotation.RequiresApi;

 import com.example.skymail.ui.Inbox.InboxMessageContainer;

 import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
 import static com.example.skymail.Attachment.showNotification;
 import static com.example.skymail.Message.o;
 import static com.example.skymail.Message.uploadTask;


public class Receiver extends BroadcastReceiver {

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getStringExtra("action")) {
            case ("cancel"):
            uploadTask.removeOnProgressListener(o);
            if (!uploadTask.isComplete()) {
                uploadTask.cancel();
            }
            showNotification(context.getString(R.string.app_name), context.getString(R.string.notification_aborted), 999, 999, context, false);
            break;
            case ("FCM"):
                Intent i = new Intent(context, Conversation.class);
                i.putExtra("remail",intent.getStringExtra("remail"))
                        .putExtra("picture",intent.getStringExtra("picture"))
                        .putExtra("subject",intent.getStringExtra("subject"))
                        .putExtra("text",intent.getStringExtra("text"))
                        .putExtra("FULLNAME",intent.getStringExtra("FULLNAME"))
                        .putExtra("url",intent.getStringExtra("url"))
                        .putExtra("message_id",intent.getStringExtra("message_id"))
                        .putExtra("from",intent.getStringExtra("from"))
                        .putExtra("object",intent.getStringExtra("object"))
                        .putExtra("id",intent.getStringExtra("id"));
                i.setFlags(FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
               Log.d("FCM","clicked");
            break;
        }
    }

}