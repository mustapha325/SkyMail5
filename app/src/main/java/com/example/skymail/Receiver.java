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
                Intent rcv = intent;
                Intent i = new Intent(context, Conversation.class);
                i.putExtra("remail",intent.getStringExtra("remail"))
                        .putExtra("picture",rcv.getStringExtra("picture"))
                        .putExtra("subject",rcv.getStringExtra("subject"))
                        .putExtra("text",rcv.getStringExtra("text"))
                        .putExtra("FULLNAME",rcv.getStringExtra("FULLNAME"))
                        .putExtra("url",rcv.getStringExtra("url"))
                        .putExtra("message_id",rcv.getStringExtra("message_id"))
                        .putExtra("from",rcv.getStringExtra("from"))
                        .putExtra("object",rcv.getStringExtra("object"))
                        .putExtra("id",rcv.getStringExtra("id"));
                i.setFlags(FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
               Log.d("FCM","clicked");
            break;
        }
    }

}