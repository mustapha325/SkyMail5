package com.example.skymail;

        import android.annotation.SuppressLint;
        import android.app.Activity;
        import android.app.AlertDialog;
        import android.content.Context;
        import android.content.Intent;
        import android.content.SharedPreferences;
        import android.database.Cursor;
        import android.net.Uri;
        import android.os.Build;
        import android.os.Bundle;
        import android.os.Environment;
        import android.os.Looper;
        import android.provider.MediaStore;
        import android.provider.OpenableColumns;
        import android.text.TextUtils;
        import android.util.Log;
        import android.view.Menu;
        import android.view.MenuInflater;
        import android.view.MenuItem;
        import android.view.View;
        import android.widget.AutoCompleteTextView;
        import android.widget.EditText;
        import android.widget.ImageButton;
        import android.widget.ImageView;
        import android.widget.TextView;
        import android.widget.Toast;

        import androidx.annotation.NonNull;
        import androidx.annotation.RequiresApi;
        import androidx.appcompat.app.AppCompatActivity;
        import androidx.appcompat.widget.Toolbar;
        import androidx.core.content.FileProvider;

        import com.asksira.bsimagepicker.BSImagePicker;
        import com.asksira.bsimagepicker.Utils;
        import com.bumptech.glide.Glide;
        import com.example.skymail.CloudMessaging.APIService;
        import com.example.skymail.CloudMessaging.Client;
        import com.example.skymail.CloudMessaging.Data;
        import com.example.skymail.CloudMessaging.MyResponse;
        import com.example.skymail.CloudMessaging.NotificationSender;
        import com.example.skymail.Data.Contacts;
        import com.example.skymail.Data.ImageCompression;
        import com.example.skymail.Data.Messages;
        import com.example.skymail.Data.Notifications;
        import com.example.skymail.Data.RealPathUtil;
        import com.example.skymail.Data.UploadImages;
        import com.example.skymail.Data.Users;
        import com.example.skymail.ui.TransitionDialog;
        import com.google.android.gms.tasks.Continuation;
        import com.google.android.gms.tasks.OnCompleteListener;
        import com.google.android.gms.tasks.OnFailureListener;
        import com.google.android.gms.tasks.Task;
        import com.google.firebase.BuildConfig;
        import com.google.firebase.auth.FirebaseAuth;
        import com.google.firebase.auth.FirebaseUser;
        import com.google.firebase.database.DataSnapshot;
        import com.google.firebase.database.DatabaseError;
        import com.google.firebase.database.DatabaseReference;
        import com.google.firebase.database.FirebaseDatabase;
        import com.google.firebase.database.Query;
        import com.google.firebase.database.ValueEventListener;
        import com.google.firebase.storage.FirebaseStorage;
        import com.google.firebase.storage.OnProgressListener;
        import com.google.firebase.storage.StorageReference;
        import com.google.firebase.storage.UploadTask;

        import org.jetbrains.annotations.NotNull;

        import java.io.File;
        import java.io.IOException;
        import java.text.SimpleDateFormat;
        import java.util.ArrayList;
        import java.util.Date;
        import java.util.List;
        import java.util.Objects;

        import retrofit2.Call;
        import retrofit2.Callback;
        import retrofit2.Response;

        import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;
        import static com.example.skymail.Attachment.showNotification;
        import static com.example.skymail.Data.io.access;
        import static com.example.skymail.Data.io.getRandomNumberUsingNextInt;


public class Message extends AppCompatActivity implements BSImagePicker.OnMultiImageSelectedListener,
        BSImagePicker.ImageLoaderDelegate{

    private static final int LAUNCH_SECOND_ACTIVITY = 1 ;
    private static final int TAKE_PICTURE = 9;
    private Messages sendmessage;
    EditText subject, object1, message;
    TextView from,id;
    String ID;
    String targetID;
    String email;
    FirebaseDatabase messagedatabase;
    FirebaseAuth fAuth;
    Toolbar toolbar;
    Boolean onlyOne ;
    Thread Image_uploader = null;

    public File photoFile;
    private static String imageFileName;
    private String realPath;
    private static Uri UriImage,UriSave;
    DatabaseReference DraftRreference;
    public String userID;
    private String EmailFromContactInformation;
    private static String userFULLNAME,object,subject1,message1,url,to1,messageid;
    static Boolean fail=false;
    DatabaseReference userdatabase;
    public static String result,resultList="";
    Query Query;
    AutoCompleteTextView to;
    public List<Messages> automessageslist;
    FirebaseUser fuser;
    SharedPreferences p;

    static OnProgressListener<UploadTask.TaskSnapshot> o;
    static UploadTask uploadTask;
    private APIService apiService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        fAuth = FirebaseAuth.getInstance();
        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);
        userdatabase = FirebaseDatabase.getInstance().getReference();
        Bundle bundle = getIntent().getExtras();
        automessageslist = (List<Messages>) bundle.getSerializable( "autolist" );
        p=getDefaultSharedPreferences(getApplicationContext());
        setContentView( R.layout.activity_message );

        BSImagePicker multiSelectionPicker = new BSImagePicker.Builder(BuildConfig.APPLICATION_ID+".provider")
                .isMultiSelect() //Set this if you want to use multi selection mode.
                .setMinimumMultiSelectCount(1) //Default: 1.
                .setMaximumMultiSelectCount(5) //Default: Integer.MAX_VALUE (i.e. User can select as many images as he/she wants)
                .setMultiSelectBarBgColor(R.color.backgroundcolor) //Default: #FFFFFF. You can also set it to a translucent color.
                .setMultiSelectTextColor(R.color.textcolor) //Default: #212121(Dark grey). This is the message in the multi-select bottom bar.
                .setMultiSelectDoneTextColor(R.color.colorAccent) //Default: #388e3c(Green). This is the color of the "Done" TextView.
                .setOverSelectTextColor(R.color.error_text) //Default: #b71c1c. This is the color of the message shown when user tries to select more than maximum select count.
                .disableOverSelectionMessage() //You can also decide not to show this over select message.
                .build();

        Users user=access(this);
        findViewById(R.id.btn_Attachment).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Bloqui message ila attachement mazal ma ykamal upload
                p.edit().putString("pass","false").commit();
                startActivityForResult(new Intent(Message.this ,Attachment.class),LAUNCH_SECOND_ACTIVITY);
            }
        });
        Intent intent = getIntent();
        from = findViewById( R.id.From );
        fuser=fAuth.getCurrentUser();
        userID = user.getUserID();
        userFULLNAME = user.getFullname();
        //messageid = intent.getStringExtra( "message_id" );
        EmailFromContactInformation = user.getEmail();

        to = findViewById( R.id.To );
        object1 = findViewById( R.id.Object1 );
        subject = findViewById( R.id.Subject );
        message = findViewById( R.id.messageText );
        toolbar = findViewById( R.id.MessageActivityToolbar );
        messagedatabase = FirebaseDatabase.getInstance();
        email = EmailFromContactInformation;
        ID = userID;
        from.setText( email );
        //8

        to.setText( EmailFromContactInformation );

        setSupportActionBar( toolbar );
        getSupportActionBar().setTitle( R.string.Message_actionbar_title );
        getSupportActionBar().setDisplayHomeAsUpEnabled( true );
        getSupportActionBar().setHomeAsUpIndicator( R.drawable.x );
        findViewById(R.id.btn_attach_pics).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                p.edit().putString("pass","false").commit();
                multiSelectionPicker.show(getSupportFragmentManager(), "picker_message");
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate( R.menu.items, menu );
        return super.onCreateOptionsMenu( menu );
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sendAction:
                sendmessage();
                return true;
            case android.R.id.home:
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference( "DraftMessages" );
                messageid = databaseReference.push().getKey();
                databaseReference.child( messageid ).child( "messageText" ).setValue( message.getText().toString().trim() );
                databaseReference.child( messageid ).child( "to" ).setValue( to.getText().toString().trim() );
                databaseReference.child( messageid ).child( "subject" ).setValue( subject.getText().toString().trim() );
                databaseReference.child( messageid ).child( "object" ).setValue( object1.getText().toString().trim() );
                if (!to.getText().toString().isEmpty() && (!object1.getText().toString().isEmpty() || !message.getText().toString().isEmpty() || !subject.getText().toString().isEmpty())){
                    DraftMessage();
                    finish();
                }else if (to.getText().toString().isEmpty()){
                    finish();
                }else{
                    finish();
                }


                return true;


        }

        return super.onOptionsItemSelected( item );
    }

    private String CompressImage(String path){
        ImageCompression ic = new ImageCompression(getApplicationContext());
        return ic.compressImage(path,getFileName(UriSave));
    }
    private String CompressImages(String path,Uri ur){
        ImageCompression ic = new ImageCompression(getApplicationContext());
        return ic.compressImage(path,getFileName(ur));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode==TAKE_PICTURE) {
            String path=CompressImage(realPath);
            Uploadfile(UriSave, getFileName(UriSave),path);
            //tran.startTransitionDialog();
            //StoreProfilePic(UriSave);
        }

        if (requestCode == LAUNCH_SECOND_ACTIVITY) {
            if(resultCode == Activity.RESULT_OK) {
                String uri = data.getStringExtra("Uri");
                String filename = data.getStringExtra("returned");
                String size = data.getStringExtra("size");
                //5mb ta3 pièce joint
                if ((Integer.parseInt(size) < (5 * 1000000))) {
                    Uri urim = Uri.parse(uri);
                    Uploadfile(urim, filename,null);
                }
                else showDialog(Message.this,getString(R.string.size_dialog_title),getString(R.string.size_dialog_message));
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, "Canceled", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void showDialog(Context ctx, String titre, String message){
        AlertDialog a=new AlertDialog.Builder(ctx)
                .setTitle(titre)
                .setMessage(message)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
        a.show();
    }
    /*public void getRecieverInfo(){
        DatabaseReference user = FirebaseDatabase.getInstance().getReference("users");
        Query query = user.orderByChild( "email" ).equalTo( to.getText().toString().trim() );
        query.addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot u : dataSnapshot.getChildren()){
                    Users user = u.getValue(Users.class);
                    assert user != null;
                    store2("email:"+user.getEmail()+";"+"nom:"+user.getFullname()+";"+"id:"+user.getUserID()+";"+"date:"+user.getBirthdate()+";"+"gender:"+user.getGender()+";"+"pass:"+user.getPassword()+";",Message.this);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        } );
    }*/


    public void DraftMessage() {
        String userID = ID;
        String messageID = DraftRreference.push().getKey();
        String From = from.getText().toString().trim();
        String To = to.getText().toString().trim();
        String Object = object1.getText().toString().trim();
        String Subject = subject.getText().toString().trim();
        String Message = message.getText().toString().trim();
        String userFullname = userFULLNAME;
        Date date = new Date();
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat DayOfweek = new SimpleDateFormat("EEEE");
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat DayNumber = new SimpleDateFormat("FF");
        String day = DayNumber.format( date )+" "+DayOfweek.format( date )+".";
        new Thread(new Runnable() {
            @Override
            public void run() {
                DraftRreference = FirebaseDatabase.getInstance().getReference("DraftMessages");
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference profilepicture = database.getReference("ProfilePicture").child(userID);
                profilepicture.addValueEventListener(new ValueEventListener() {
                    String ProfilePictureUri;
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot image : dataSnapshot.getChildren()) {
                            UploadImages ProfilePic = image.getValue(UploadImages.class);
                            assert ProfilePic != null;
                            ProfilePictureUri = ProfilePic.getmImageUrl();
                        }

                        Messages message = new Messages(userID, messageID, From, To, Subject, Object, Message, ProfilePictureUri, userFullname, result, day, null);
                        assert messageID != null;
                        DraftRreference.child(messageID).setValue(message);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }}).start();
    }



    public void sendmessage(){
        final DatabaseReference root,root2,root3;
        root = messagedatabase.getReference("InboxMessages");
        root2 =messagedatabase.getReference("SendedMessages");
        root3 = messagedatabase.getReference("Reply");
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        String userID = fuser.getUid();
        String messageID = root.push().getKey();
        String From = from.getText().toString().trim();
        String To = to.getText().toString().trim();
        String Object = object1.getText().toString().trim();
        String Subject = subject.getText().toString().trim();
        String Messagetext = message.getText().toString().trim();
        String Reply_Message_Id = root.push().getKey();
        Long tsLong = System.currentTimeMillis() / 1000;
        String day = tsLong.toString();
        final DatabaseReference profilepicture = database.getReference("ProfilePicture");
        boolean empty=false;
        if(TextUtils.isEmpty(From)){
            from.setError(getString(R.string.cannot_be_empty));
            Toast.makeText(Message.this, R.string.email_incomplet, Toast.LENGTH_SHORT).show();
            empty=true;
        }
        if(TextUtils.isEmpty(To)){
            to.setError(getString(R.string.cannot_be_empty));
            Toast.makeText(Message.this, R.string.email_incomplet, Toast.LENGTH_SHORT).show();
            empty=true;
        }
        if(TextUtils.isEmpty(Subject)){
            subject.setError(getString(R.string.cannot_be_empty));
            empty=true;
            Toast.makeText(Message.this, R.string.email_incomplet, Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(Messagetext)){
            message.setError(getString(R.string.cannot_be_empty));
            empty=true;
            Toast.makeText(Message.this, R.string.email_incomplet, Toast.LENGTH_SHORT).show();
        }
        if(p.getString("pass","true").equals("false")){
            Toast.makeText(this,R.string.synchro_message,Toast.LENGTH_LONG);
        }
        else if(!empty && p.getString("pass","true").equals("true")){
            Query query1 = profilepicture.child(userID);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    query1.addValueEventListener(new ValueEventListener() {
                        String ProfilePictureUri;

                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot image : dataSnapshot.getChildren()) {
                                UploadImages ProfilePic = image.getValue(UploadImages.class);
                                assert ProfilePic != null;
                                ProfilePictureUri = ProfilePic.getmImageUrl();
                                if (result == null) {
                                    sendmessage = new Messages(userID, messageID, From, To, Subject, Object, Messagetext, ProfilePictureUri, userFULLNAME, resultList, day, Reply_Message_Id);
                                } else
                                    sendmessage = new Messages(userID, messageID, From, To, Subject, Object, Messagetext, ProfilePictureUri, userFULLNAME, result, day, Reply_Message_Id);
                                assert messageID != null;
                                root.child(messageID).setValue(sendmessage);
                                root2.child(messageID).setValue(sendmessage);
                                root3.child(messageID).child("reply").child(Reply_Message_Id).setValue(sendmessage);
                                resultList = "";
                                getTargetId(to.getText().toString().trim(), sendmessage);
                                Notifications nt = new Notifications(sendmessage.getMessageText(), sendmessage.getMessagID(),
                                        sendmessage.getSubject(), sendmessage.getFrom(), sendmessage.getSenderFullName(),
                                        sendmessage.getTo(), sendmessage.getSenderProfilePicture(), sendmessage.getFileurl()
                                        , getRandomNumberUsingNextInt(0, 99 * 1000));
                                sendToDatabase(nt);
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                    contact();
                    finish();
                }
            }).start();
        }
    }

    void getTargetId(String email,Messages msg){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Query = userdatabase.child("users").orderByChild("email").equalTo(email);
                Query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot users : dataSnapshot.getChildren()) {
                                Users user = users.getValue(Users.class);
                                Log.d("TargetID", user.getUserID());
                                sendNotif(user.getUserID(), msg.getMessagID());
                            }
                        } else {
                            Toast.makeText(Message.this, R.string.toast_user_notfound, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.d("Message", "Connection to database Failed ");
                    }
                });
            }}).start();
    }

    void sendNotif(String ID,String id){
        new Thread(new Runnable() {
            @Override
            public void run() {
                FirebaseDatabase.getInstance().getReference().child("Tokens").child(ID).child("token").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String usertoken = dataSnapshot.getValue(String.class);
                        if (usertoken != null)
                            sendNotifications(usertoken, id);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.d("Notif ", "failed");
                    }
                });
            }}).start();
    }

    public void sendNotifications(String usertoken,String id) {
        Data data = new Data(id);
        NotificationSender sender = new NotificationSender(data, usertoken);
        apiService.sendNotifcation(sender).enqueue(new Callback<MyResponse>() {
            @Override
            public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                if (response.code() == 200) {
                    if (response.body().success != 1) {
                        Toast.makeText(Message.this, "Notification send Failed", Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<MyResponse> call, Throwable t) {
                Log.d("Notif ","failed");
            }
        });
    }

    void sendToDatabase(Notifications noti){
        FirebaseDatabase.getInstance().getReference("Notifications").push();
        FirebaseDatabase.getInstance().getReference("Notifications").child(noti.getMessageID()).setValue(noti);
    }

    void Uploadfile(Uri urim, String filename,String path) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference href = storage.getReference();
        StorageReference ref = href.child("Files/" + filename);
        final String[] tmp = null;
        new Thread(new Runnable() {
            @Override
            public void run() {
                uploadTask = ref.putFile(urim);
                p.edit().putString("pass","false").commit();
                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        return ref.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            showNotification(getString(R.string.app_name), getString(R.string.notification_upload_complete), 0, 0, Message.this, true);
                            result = task.getResult().toString();
                            p.edit().putString("pass","true").commit();
                            if (path!=null)
                                deletefile(path);
                        } else {
                            if (!(uploadTask.isCanceled()))
                                Toast.makeText(Message.this, R.string.toast_network_error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                uploadTask.addOnProgressListener(o = new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        long progress = ((taskSnapshot.getBytesTransferred() * 100) / taskSnapshot.getTotalByteCount());
                        if (!(uploadTask.isCanceled()))
                            showNotification(getString(R.string.app_name), getString(R.string.notification_upload), 100, (int) progress, Message.this, true);
                    }
                });
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //showNotification(R.string.notification_title, "annulé", 999, 999, Message.this, false);
                    }
                });
            }}).start();
    }
    public void OpenDeviceCamera(View view) {
        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            photoFile = createImageFile();
            UriSave = FileProvider.getUriForFile(Objects.requireNonNull(getApplicationContext()),
                    BuildConfig.APPLICATION_ID + ".provider", photoFile);
            takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, UriSave);
            startActivityForResult(takePhotoIntent, TAKE_PICTURE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        imageFileName = "IMG_JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        realPath= image.getAbsolutePath();
        return image;
    }
    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                assert cursor != null;
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            assert result != null;
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }
    public void contact(){

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
        Query query = databaseReference.orderByChild( "email" ).equalTo( to.getText().toString().trim() );
        new Thread(new Runnable() {
            @Override
            public void run() {
                query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot d : dataSnapshot.getChildren()) {
                            Users users = d.getValue(Users.class);

                            DatabaseReference profilepicture = FirebaseDatabase.getInstance().getReference("ProfilePicture");
                            assert users != null;
                            Query query1 = profilepicture.child(users.getUserID());

                            query1.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for (DataSnapshot i : dataSnapshot.getChildren()) {
                                        final UploadImages image = i.getValue(UploadImages.class);

                                        final DatabaseReference user = FirebaseDatabase.getInstance().getReference("users");
                                        assert image != null;
                                        Query query = user.orderByChild("email").equalTo(image.getUserEmail());
                                        query.addValueEventListener(new ValueEventListener() {

                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                                for (DataSnapshot u : dataSnapshot.getChildren()) {
                                                    Users user = u.getValue(Users.class);
                                                    assert user != null;
                                                    DatabaseReference Contacts = FirebaseDatabase.getInstance().getReference("Contacts").child(userID).child("contacts");
                                                    Contacts contact = new Contacts(image.getUserID(), image.getUserFullname(), image.getUserEmail(), image.getmImageUrl(), userID, user.getPhonenumber());
                                                    Contacts.child(image.getUserID()).setValue(contact);
                                                }

                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    }

                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                }
                            });

                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }}).start();
    }

    @Override
    public void loadImage(Uri imageUri, ImageView ivImage) {
        Glide.with(Message.this).load(imageUri).into(ivImage);
    }

    public void deletefile(String path){
        new File(path).getAbsoluteFile().delete();
    }
    public void deletefiles(ArrayList<String> paths){
        for (String p:paths
        ) {
            new File(p).getAbsoluteFile().delete();
        }
    }

    @Override
    public void onMultiImageSelected(List<Uri> uriList, String tag) {
        ArrayList<String> paths = new ArrayList<String>();
        List<Uri> uris = new ArrayList<Uri>();
        TransitionDialog tran = new TransitionDialog(Message.this);
        tran.startTransitionDialog();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                for (Uri u : uriList
                ) {
                    paths.add(CompressImages(RealPathUtil.getRealPath(Message.this, u), u));
                }
                for (String path : paths
                ) {
                    File img = new File(path);
                    uris.add(Uri.parse(img.toURI().toString()));
                }
                tran.dissmissDialog();
                UploadImages(uris, paths);
            }}).start();
    }

    void UploadImages(List<Uri> UriArray,ArrayList<String> paths) {
        boolean multiple = (UriArray.size() > 1);
        int i = 0;
        for (Uri uri:
                UriArray) {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference href = storage.getReference();
            StorageReference ref = href.child("Files/" + getFileName(uri));
            showNotification(getString(R.string.app_name), getString(R.string.synchro_message), 20, 20, Message.this, true);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    uploadTask = ref.putFile(uri);
                    p.edit().putString("pass","false").commit();
                    Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()) {
                                throw Objects.requireNonNull(task.getException());
                            }
                            return ref.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @RequiresApi(api = Build.VERSION_CODES.O)
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                if (multiple) {
                                    resultList = resultList + ";" + Objects.requireNonNull(task.getResult()).toString();
                                    Log.d("img ", resultList);
                                }
                                else {
                                    result = Objects.requireNonNull(task.getResult()).toString();
                                    Log.d("img one ", result);
                                    if (!fail) SynchroFin(paths,UriArray);
                                }
                                if (uri.equals(UriArray.get(UriArray.size()-1)))
                                    if (!fail) SynchroFin(paths,UriArray);
                            } else {
                                if (!(uploadTask.isCanceled()))
                                    Toast.makeText(Message.this, R.string.toast_network_error, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    uploadTask.addOnProgressListener(o = new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @RequiresApi(api = Build.VERSION_CODES.O)
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        }
                    });
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @RequiresApi(api = Build.VERSION_CODES.O)
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(Message.this, R.string.toast_network_error, Toast.LENGTH_SHORT).show();
                            fail = true;
                            showNotification(getString(R.string.app_name), getString(R.string.synchro_message), 0, 0, Message.this, false);
                        }
                    });
                }
            }).start();
        }
    }

    public void SynchroFin(ArrayList<String> paths,List<Uri> uris){
        p.edit().putString("pass","true").commit();
        deletefiles(paths);
        showNotification(getString(R.string.app_name), getString(R.string.synchro_message), 20, 20, Message.this, false);
        uris.clear();
        paths.clear();
    }
}
