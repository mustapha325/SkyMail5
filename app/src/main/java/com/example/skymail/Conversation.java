package com.example.skymail;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.os.Looper;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.asksira.bsimagepicker.BSImagePicker;
import com.bumptech.glide.Glide;
import com.example.skymail.Adapter.ConversationAdapter;
import com.example.skymail.Adapter.ImageAdapter;
import com.example.skymail.Data.ImageCompression;
import com.example.skymail.Data.Messages;
import com.example.skymail.Data.RealPathUtil;
import com.example.skymail.Data.UploadImages;
import com.example.skymail.Data.Users;
import com.example.skymail.Data.io;
import com.example.skymail.ui.Inbox.InboxFragment;
import com.example.skymail.ui.Inbox.InboxMessageContainer;
import com.example.skymail.ui.TransitionDialog;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.BuildConfig;
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
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;
import static com.example.skymail.Attachment.showNotification;
import static com.example.skymail.Data.io.access;

public class Conversation extends AppCompatActivity implements BSImagePicker.OnMultiImageSelectedListener,BSImagePicker.ImageLoaderDelegate{

    private static final int LAUNCH_SECOND_ACTIVITY = 1 ;
    private RecyclerView rv;
    SharedPreferences p;
    private LinearLayoutManager layoutManager;
    private ArrayList<Messages> messagesArrayList;
    private String remail,picture,sub,messagetext,fullname,url,message_id,object,from,resultList="",imageFileName,realPath;
    public static  String user_id;
    private Messages sendmessage;
    private EditText reply_message;
    private ImageButton reply_button;
    public static String result;
    ArrayList<Messages> InboxMessagesListe;
    private Query query;
    private FirebaseDatabase database;
    private DatabaseReference messagedatabase;
    private ImageButton Attachment;
    private Boolean fail=false;
    private int i;
    private BSImagePicker multiSelectionPicker;
    private Users user;
    private ImageAdapter imageAdapter;
    private Toolbar toolbar;
    private BottomSheetDialog bottomSheetDialog;
    private String[] uris;







    static OnProgressListener<UploadTask.TaskSnapshot> o;
    static UploadTask uploadTask;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.conversation );
        Intent intent = getIntent();
        user = access( Conversation.this);
        remail = intent.getStringExtra( "remail" );
        picture = intent.getStringExtra( "picture" );
        sub = intent.getStringExtra( "subject" );
        messagetext = intent.getStringExtra( "text" );
        fullname = intent.getStringExtra( "FULLNAME" );
        message_id = intent.getStringExtra( "message_id" );
        object = intent.getStringExtra( "object" );
        user_id = intent.getStringExtra( "id" );
        from = intent.getStringExtra( "from" );
        Log.d("tagzzz",remail+message_id);
        Attachment = findViewById( R.id.btn_download2 );
        toolbar = findViewById( R.id.conv_app_bar );
        setSupportActionBar( toolbar );
        getSupportActionBar().setDisplayHomeAsUpEnabled( true );
        getSupportActionBar().setTitle( "Message");

        InboxMessagesListe = new ArrayList<>();
        p=getDefaultSharedPreferences(getApplicationContext());
        reply_message = findViewById( R.id.reply_message );
        reply_button = findViewById( R.id.reply_button );
        rv = findViewById( R.id.conversation_RV );
        messagesArrayList = new ArrayList<>();
        layoutManager = new LinearLayoutManager(this);
        //reference for the inboxMessages
        database = FirebaseDatabase.getInstance();
        messagedatabase = database.getReference("InboxMessages");

        Attachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.show();
            }
        });




         multiSelectionPicker = new BSImagePicker.Builder( BuildConfig.APPLICATION_ID+".provider")
                .isMultiSelect() //Set this if you want to use multi selection mode.
                .setMinimumMultiSelectCount(1) //Default: 1.
                .setMaximumMultiSelectCount(5) //Default: Integer.MAX_VALUE (i.e. User can select as many images as he/she wants)
                .setMultiSelectBarBgColor(R.color.backgroundcolor) //Default: #FFFFFF. You can also set it to a translucent color.
                .setMultiSelectTextColor(R.color.textcolor) //Default: #212121(Dark grey). This is the message in the multi-select bottom bar.
                .setMultiSelectDoneTextColor(R.color.colorAccent) //Default: #388e3c(Green). This is the color of the "Done" TextView.
                .setOverSelectTextColor(R.color.error_text) //Default: #b71c1c. This is the color of the message shown when user tries to select more than maximum select count.
                .disableOverSelectionMessage() //You can also decide not to show this over select message.
                .build();

        CreaetBottomDialogSheet();
        getDataToArrayList();



    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference messagedatabase = database.getReference("Reply");
    query = messagedatabase.child( message_id ).child( "reply" );

        query.addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot m :dataSnapshot.getChildren()){
                    Messages message = m.getValue( Messages.class);
                    messagesArrayList.add( message );
                }

            }
            @SuppressLint("ShowToast")
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText( Conversation.this,R.string.Error,Toast.LENGTH_LONG).show();
            }
        } );

        reply_button.setOnClickListener( v -> {
            Reply();
        } );

    }

    @Override
    protected void onStart() {
        super.onStart();
        FireBaseRecyclerHandler();
    }


    @Override
    public void onMultiImageSelected(List<Uri> uriList, String tag) {
        ArrayList<String> paths = new ArrayList<String>();
        List<Uri> uris = new ArrayList<Uri>();
        TransitionDialog tran = new TransitionDialog(Conversation.this);
        tran.startTransitionDialog();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                for (Uri u : uriList
                ) {
                    paths.add(CompressImages(RealPathUtil.getRealPath(Conversation.this, u), u));
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
            showNotification(getString(R.string.app_name), getString(R.string.synchro_message), 20, 20, Conversation.this, true);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    uploadTask = ref.putFile(uri);
                    p.edit().putString("passConv","false").commit();
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
                                    Toast.makeText( Conversation.this, R.string.toast_network_error, Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(Conversation.this, R.string.toast_network_error, Toast.LENGTH_SHORT).show();
                            fail = true;
                            showNotification(getString(R.string.app_name), getString(R.string.synchro_message), 20, 20, Conversation.this, false);
                        }
                    });
                }
            }).start();
        }
    }

    public void SynchroFin(ArrayList<String> paths,List<Uri> uris){
        
        p.edit().putString("passConv","true").commit();
        deletefiles(paths);
        showNotification(getString(R.string.app_name), getString(R.string.synchro_message), 20, 20, Conversation.this, false);
        uris.clear();
        paths.clear();
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

    private String CompressImages(String path,Uri ur){
        ImageCompression ic = new ImageCompression(getApplicationContext());
        return ic.compressImage(path,getFileName(ur));
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex( OpenableColumns.DISPLAY_NAME));
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


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

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
                else showDialog(this,getString(R.string.size_dialog_title),getString(R.string.size_dialog_message));
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, R.string.Error, Toast.LENGTH_SHORT).show();
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

    public void Reply(){
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

        DatabaseReference profilepicture = firebaseDatabase.getReference("ProfilePicture");
        DatabaseReference reply = firebaseDatabase.getReference("Reply");

        Query query = profilepicture.child( user_id );

        query.addValueEventListener( new ValueEventListener() {
            String userID = Conversation.user_id;
            //new message the reply
            String current_messageID = reply.push().getKey();
            String From = from;
            String To =remail ;
            String Object = object.trim();
            String Subject = sub.trim();
            String Messagetext = reply_message.getText().toString().trim();
            String userFULLNAME = user.getFullname();
            String ProfilePictureUri;
            Date date = new Date();
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat DayOfweek = new SimpleDateFormat("EEEE");
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat DayNumber = new SimpleDateFormat("FF");
            String day = DayNumber.format( date )+" "+DayOfweek.format( date )+".";
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot image :dataSnapshot.getChildren()){
                    UploadImages ProfilePic = image.getValue( UploadImages.class);
                    assert ProfilePic != null;
                    ProfilePictureUri = ProfilePic.getmImageUrl();
                    if(result==null) {
                        sendmessage = new Messages(userID, current_messageID,
                                From, To, Subject, Object, Messagetext, ProfilePictureUri, userFULLNAME, resultList, day, current_messageID);

                    }else{
                        sendmessage = new Messages(userID,current_messageID,To,From,Subject,Object,Messagetext,ProfilePictureUri,userFULLNAME,result,day,current_messageID);
                        assert current_messageID != null;
                    }
                    reply.child(message_id).child("reply").child( current_messageID ).setValue(sendmessage);
                    result="";
                    resultList="";


                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        } );




    }

    @Override
    public void loadImage(Uri imageUri, ImageView ivImage) {
        Glide.with(Conversation.this).load(imageUri).into(ivImage);
    }




    public static class ConversationRecyclerVH extends RecyclerView.ViewHolder
    {
        CircleImageView circleImageView;
        TextView subjectHolder,senderfullname,recieveremail,textholder;
        ImageButton downloadButton;
        ImageButton btn_download_images;
        RecyclerView imageRV;



        ConversationRecyclerVH(View itemView) {
            super(itemView);
            circleImageView = itemView.findViewById( R.id.senderprofilepic);
            senderfullname = itemView.findViewById( R.id.senderfullname);
            subjectHolder = itemView.findViewById( R.id.SubjectHolder);
            recieveremail = itemView.findViewById( R.id.recieveremail);
            textholder = itemView.findViewById( R.id.textholder);
            downloadButton = itemView.findViewById( R.id.btn_download );
            btn_download_images = itemView.findViewById( R.id.btn_download_images );
            imageRV =itemView.findViewById(R.id.imageRV);
        }
    }

    public void FireBaseRecyclerHandler(){
        FirebaseRecyclerOptions<Messages> firebaseRecyclerOptions = new FirebaseRecyclerOptions.Builder<Messages>().setQuery(query,Messages.class)
                .build();
        FirebaseRecyclerAdapter<Messages,ConversationRecyclerVH> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Messages, ConversationRecyclerVH>(firebaseRecyclerOptions) {
            @Override
            protected void onBindViewHolder(@NonNull ConversationRecyclerVH holder, int position, @NonNull Messages model) {
                if (model.getFileurl().contains(";")){
                    holder.btn_download_images.setVisibility( View.VISIBLE );
                    uris = model.getFileurl().split( ";" );
                    ArrayList<Uri> list = new ArrayList<>();
                    holder.imageRV.setVisibility( View.VISIBLE );
                    for (int i=0;i<uris.length;i++){
                        list.add(Uri.parse( uris[i] ));
                    }
                    imageAdapter = new ImageAdapter( getApplication(),list );
                    holder.imageRV.setAdapter(imageAdapter);
                }else {
                    holder.btn_download_images.setVisibility( View.GONE );
                    holder.downloadButton.setVisibility( View.VISIBLE );
                    holder.imageRV.setVisibility( View.VISIBLE );
                    ArrayList<Uri> list;
                    list = new ArrayList<>();
                    list.add( Uri.parse( model.getFileurl()));
                    imageAdapter = new ImageAdapter( getApplication(),list );
                    holder.imageRV.setAdapter(imageAdapter);
                }
                if (model.getFileurl() == null || model.getFileurl().isEmpty()){
                        holder.imageRV.setVisibility( View.GONE );
                        holder.downloadButton.setVisibility( View.GONE );
                        holder.btn_download_images.setVisibility( View.GONE );
                    }
                holder.imageRV.setHasFixedSize( true );
                holder.senderfullname.setText( model.getSenderFullName());
                holder.subjectHolder.setText(model.getSubject());
                holder.textholder.setText(model.getMessageText());
                holder.recieveremail.setText(model.getTo());
                Picasso.get().load( model.getSenderProfilePicture()).fit().into( holder.circleImageView );
                url = model.getFileurl();
                holder.downloadButton.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (url != null){
                            try {
                                downloadFile( Conversation.this,model.getFileurl());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }else{
                            Toast.makeText( Conversation.this,"error no file",Toast.LENGTH_LONG );
                        }

                    }
                } );
                holder.btn_download_images.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(uris!=null) {
                            for (String uri : uris) {
                                try {
                                    downloadFile(Conversation.this, uri);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                } );
            }
            @NonNull
            @Override
            public ConversationRecyclerVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new ConversationRecyclerVH( LayoutInflater.from( parent.getContext() ).inflate( R.layout.message_container,parent,false ) );
            }
        };
        //LAYOUT MANAGER
        rv.setLayoutManager(layoutManager);
        //setAdapter
        rv.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();



    }
    void downloadFile(Context ctx,String url) throws IOException {
        //Download be download manager ta3 android bla firebase direct me URL
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference href = storage.getReferenceFromUrl(url);
        String Filename = href.getName();
        Uri uri= Uri.parse(url);
        DownloadManager downloadManager = (DownloadManager) getSystemService(ctx.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI |
                DownloadManager.Request.NETWORK_MOBILE);
        request.setTitle(getString(R.string.app_name));
        request.setDescription(getString(R.string.notification_download));
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        //Dossier ta3 telechargement
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,Filename);
        request.setMimeType("*/*");
        downloadManager.enqueue(request);
    }
    public void getDataToArrayList(){
        Query query2;
        query2 = messagedatabase.orderByChild( "to" ).equalTo( user.getEmail());
        query2.addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot m :dataSnapshot.getChildren()){
                    Messages message = m.getValue(Messages.class);
                    InboxMessagesListe.add( message );
                }


            }
            @SuppressLint("ShowToast")
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText( Conversation.this,"error",Toast.LENGTH_LONG);

            }
        } );




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
                p.edit().putString("passConv","false").commit();
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
                            showNotification(getString(R.string.app_name), getString(R.string.notification_upload_complete), 0, 0, Conversation.this, true);
                            result = task.getResult().toString();
                            p.edit().putString("passConv","true").commit();
                            if (path!=null)
                                deletefile(path);
                        } else {
                            if (!(uploadTask.isCanceled()))
                                Toast.makeText(Conversation.this, R.string.toast_network_error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                uploadTask.addOnProgressListener(o = new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        long progress = ((taskSnapshot.getBytesTransferred() * 100) / taskSnapshot.getTotalByteCount());
                        if (!(uploadTask.isCanceled()))
                            showNotification(getString(R.string.app_name), getString(R.string.notification_upload), 100, (int) progress, Conversation.this, true);
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


    public void CreaetBottomDialogSheet(){
        if (bottomSheetDialog==null){
            View view = LayoutInflater.from( this ).inflate( R.layout.file_selection,null );
            LinearLayout MuImage = view.findViewById( R.id.image_multiple );
            LinearLayout onefile = view.findViewById( R.id.one_file );
            bottomSheetDialog = new BottomSheetDialog( this );
            bottomSheetDialog.setContentView( view );

            MuImage.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    p.edit().putString("passConv","false").commit();
                    multiSelectionPicker.show(getSupportFragmentManager(), "picker_message");
                }
            } );
            onefile.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Bloqui message ila attachement mazal ma ykamal upload
                    p.edit().putString("passConv","false").commit();
                    startActivityForResult(new Intent(Conversation.this ,Attachment.class),LAUNCH_SECOND_ACTIVITY);
                }
            } );



        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case android.R.id.home:
                Intent back = new Intent(Conversation.this,MainActivity5.class);
                startActivity(back);

        }
        return super.onOptionsItemSelected( item );

    }
}//class
