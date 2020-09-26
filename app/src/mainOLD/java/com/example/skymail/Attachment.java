package com.example.skymail;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


public class Attachment extends AppCompatActivity {

    private NotificationManager mNotifyManager;
    private String filename;
    private Uri urim;
    private Intent returnIntent;
    private PendingIntent pi;
    private static final int CHOOSE_FILE_REQUESTCODE = 88;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        returnIntent = new Intent();
        super.onCreate(savedInstanceState);
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                Intent chooserIntent;
                chooserIntent = Intent.createChooser(intent, String.valueOf(R.string.open_with));
                try {
                    startActivityForResult(chooserIntent, CHOOSE_FILE_REQUESTCODE);

                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(getApplicationContext(),  R.string.no_file_manager, Toast.LENGTH_SHORT).show();
                }
            }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CHOOSE_FILE_REQUESTCODE:
                if (resultCode == RESULT_OK) {
                    urim = data.getData();
                    returnIntent.putExtra("Uri",urim.toString());
                    Cursor returnCursor = getContentResolver().query(urim, null, null, null, null);
                    int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
                    returnCursor.moveToFirst();
                    String name = returnCursor.getString(nameIndex);
                    String size = (Long.toString(returnCursor.getLong(sizeIndex)));
                    returnIntent.putExtra("returned",name);
                    returnIntent.putExtra("size",size);
                    setResult(Activity.RESULT_OK,returnIntent);
                    finish();
                }
            default:
                setResult(Activity.RESULT_CANCELED,returnIntent);
                finish();
                break;
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void showNotification(String title, String message,int max,int progress,Context ctx,Boolean F) {
        NotificationManager mNotificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("CUSTOM_CHANNEL",
                    "CHANNEL_UPDATE",
                    NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("YOUR_NOTIFICATION_CHANNEL_DESCRIPTION");
            mNotificationManager.createNotificationChannel(channel);
        Intent broadcast = new Intent(ctx,Receiver.class);
        broadcast.putExtra("action","cancel");
        PendingIntent actionIntent = PendingIntent.getBroadcast(ctx,0,broadcast,PendingIntent.FLAG_ONE_SHOT);
        Notification.Builder mBuilder = new Notification.Builder(ctx, "CUSTOM_CHANNEL")
                .setSmallIcon(R.drawable.ic_action_upload_file) // notification icon
                .setContentTitle(title) // title for notification
                .setContentText(message);// message for notification
        if(progress>0||max>0) {
            mBuilder.setContentTitle(ctx.getString(R.string.app_name)); // title for notification progress
            mBuilder.setContentText(ctx.getString(R.string.notification_upload));// message for notification progress
            mBuilder.setProgress(max,progress,false);
            mBuilder.addAction(R.drawable.ic_attachment,  ctx.getString(R.string.button_abort), actionIntent);
        }
        if(!F) {
            mBuilder.setContentTitle(ctx.getString(R.string.app_name)); // title for notification progress
            mBuilder.setContentText(ctx.getString(R.string.notification_aborted));// message for notification progress
            mBuilder.setProgress(0,0,true);
            mBuilder.setTimeoutAfter(1000*3);
        }
        if(progress==20&&max==20&& !F) {
                mBuilder.setContentTitle(ctx.getString(R.string.app_name)); // title for notification progress
                mBuilder.setContentText(ctx.getString(R.string.synchro_message));// message for notification progress
                mBuilder.setProgress(0,0,true);
            mBuilder.setTimeoutAfter(500);
            }
        if(progress==20&&max==20) {
                mBuilder.setContentTitle(ctx.getString(R.string.app_name)); // title for notification progress
                mBuilder.setContentText(ctx.getString(R.string.synchro_message));// message for notification progress
                mBuilder.setProgress(0,0,true);
            }
        Notification noti =  mBuilder.build();
        mNotificationManager.notify(0,noti);
        }
    }

}