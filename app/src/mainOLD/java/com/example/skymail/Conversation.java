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
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.skymail.Adapter.ConversationAdapter;
import com.example.skymail.Data.Messages;
import com.example.skymail.Data.UploadImages;
import com.example.skymail.Data.Users;
import com.example.skymail.Data.io;
import com.example.skymail.ui.Inbox.InboxFragment;
import com.example.skymail.ui.Inbox.InboxMessageContainer;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
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

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.skymail.Attachment.showNotification;
import static com.example.skymail.Data.io.access;

public class Conversation extends AppCompatActivity {

    private static final int LAUNCH_SECOND_ACTIVITY = 1 ;
    private RecyclerView rv;
    private LinearLayoutManager layoutManager;
    private ArrayList<Messages> messagesArrayList;
    private ConversationAdapter conversationAdapter;
    private String remail,picture,sub,messagetext,fullname,url,message_id,object,from;
    public static  String user_id;
    private EditText reply_message;
    private ImageButton reply_button;
    public static String result;
    ArrayList<Messages> InboxMessagesListe;
    private Query query;
    private FirebaseDatabase database;
    private DatabaseReference messagedatabase;
    private ImageButton Attachment;
    private Boolean pass = true;
    private Users user;





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
        Attachment = findViewById( R.id.btn_download2 );

        InboxMessagesListe = new ArrayList<>();

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
                // Bloqui message ila attachement mazal ma ykamal upload
                pass=false;
                startActivityForResult(new Intent(Conversation.this ,Attachment.class),LAUNCH_SECOND_ACTIVITY);
            }
        });



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
                Toast.makeText( Conversation.this,"error",Toast.LENGTH_LONG);

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
                    Uploadfile(urim, filename);
                }
                else showDialog(this,getString(R.string.size_dialog_title),getString(R.string.size_dialog_message));
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, "Result not ok", Toast.LENGTH_SHORT).show();
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
                    Messages message = new Messages(userID,current_messageID,To,From,Subject,Object,Messagetext,ProfilePictureUri,userFULLNAME,result,day,current_messageID);
                    assert current_messageID != null;
                    reply.child(message_id).child("reply").child( current_messageID ).setValue(message);

                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        } );




    }



   /* @RequiresApi(api = Build.VERSION_CODES.O)
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
                    Uploadfile(urim, filename);
                }
                else com.example.skymail.Data.io.showDialog(this,"Ce Fichier dépasse la limite","Les pièces joints sont limités a 5mb par message");
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, "Result not ok", Toast.LENGTH_SHORT).show();
            }
        }
    }*/


    public static class ConversationRecyclerVH extends RecyclerView.ViewHolder
    {
        CircleImageView circleImageView;
        TextView subjectHolder,senderfullname,recieveremail,textholder;
        ImageButton downloadButton;



        ConversationRecyclerVH(View itemView) {
            super(itemView);
            circleImageView = itemView.findViewById( R.id.senderprofilepic);
            senderfullname = itemView.findViewById( R.id.senderfullname);
            subjectHolder = itemView.findViewById( R.id.SubjectHolder);
            recieveremail = itemView.findViewById( R.id.recieveremail);
            textholder = itemView.findViewById( R.id.textholder);
            downloadButton = itemView.findViewById( R.id.btn_download );
        }
    }

    public void FireBaseRecyclerHandler(){
        FirebaseRecyclerOptions<Messages> firebaseRecyclerOptions = new FirebaseRecyclerOptions.Builder<Messages>().setQuery(query,Messages.class)
                .build();
        FirebaseRecyclerAdapter<Messages,ConversationRecyclerVH> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Messages, ConversationRecyclerVH>(firebaseRecyclerOptions) {
            @Override
            protected void onBindViewHolder(@NonNull ConversationRecyclerVH holder, int position, @NonNull Messages model) {
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
                                Toast.makeText( Conversation.this,"9999fefefe",Toast.LENGTH_LONG );
                                downloadFile( Conversation.this );
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }else{
                            Toast.makeText( Conversation.this,"error no file",Toast.LENGTH_LONG );
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
    void downloadFile(Context ctx) throws IOException {
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

        //code ta3 download bel firebase la tebghi tsiyi

       /* File rootPath = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "file-name");
        if(!rootPath.exists()) {
            rootPath.mkdirs();
        }

        final File localFile = new File(rootPath,Filename);
        //File localFile = File.createTempFile(getname(Filename), "."+getformat(Filename));
        //com.example.skymail.Data.io.showDialog(this,getname(Filename),"."+getformat(Filename));
        href.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                //Dismiss Progress Dialog
                showNotification("Téléchargement","Téléchargement fini",0,0,getApplicationContext(),true);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //Dismiss Progress Dialog\\
                Toast.makeText(getApplicationContext(), "Problème réseau", Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                //calculating progress percentage
                long progress = ((taskSnapshot.getBytesTransferred() * 100) / taskSnapshot.getTotalByteCount());
                //displaying percentage in progress dialog
                showNotification("Téléchargement","en cours",100,(int)progress,getApplicationContext(),true);
            }
        });*/
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


    void Uploadfile(Uri urim, String filename) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference href = storage.getReference();
        StorageReference ref = href.child("Files/" + filename);
        final String[] tmp = null;
        uploadTask = ref.putFile(urim);
        pass=false;
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
                    showNotification(getString(R.string.app_name), getString(R.string.notification_upload_complete),0,0,Conversation.this,true);
                    result=task.getResult().toString();
                    pass=true;

                } else {
                    if (!(uploadTask.isCanceled()))
                        Toast.makeText(Conversation.this, R.string.toast_network_error, Toast.LENGTH_SHORT).show();
                }
            }
        });
        uploadTask.addOnProgressListener(o = new OnProgressListener <UploadTask.TaskSnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onProgress( UploadTask.TaskSnapshot taskSnapshot) {
                long progress = ((taskSnapshot.getBytesTransferred() * 100) / taskSnapshot.getTotalByteCount());
                if (!(uploadTask.isCanceled()))
                    showNotification(getString(R.string.app_name),getString(R.string.notification_upload),100,(int)progress,Conversation.this,true);
            }
        });
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onFailure(@NonNull Exception e) {
                //showNotification(R.string.notification_title, "annulé", 999, 999, Message.this, false);
            }
        });
    }







}//class
