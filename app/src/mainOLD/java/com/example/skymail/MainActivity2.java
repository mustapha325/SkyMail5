package com.example.skymail;


import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.skymail.Data.Messages;
import com.example.skymail.Data.UploadImages;
import com.example.skymail.Data.Users;
import com.example.skymail.Data.io;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.skymail.Data.io.clearuserdata;
import static com.example.skymail.Data.io.store;


public class MainActivity2 extends AppCompatActivity {
    EditText fullname;
    EditText email;
    EditText password;
    EditText date;
    DatePickerDialog datePickerDialog;
    EditText phone;
    Button confirm;
    ImageView back;
    RadioGroup radioGroup;
    RadioButton radioButton;
    FirebaseDatabase database;
    DatabaseReference userdatabase;
    Uri ProfilePicUri;
    StorageReference storageReference;
    CircleImageView profilepic;
    FirebaseAuth fAuth;
    FirebaseUser fuser;
    String TAG = "Registartion";
    String Fullname ;
    String Email ;
    String Password ;
    String Gender ;
    String Date ;
    String phonenumber ;
    String Inscriptiondate ;
    Calendar calendar;
    DateFormat dateFormat;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main2 );
        setAnimation();
        fullname = findViewById( R.id.Fullname );
        email = findViewById( R.id.Email );
        password = findViewById( R.id.Password );
        date = findViewById( R.id.Date );
        phone = findViewById( R.id.phonenumber);
        confirm = findViewById( R.id.Signin );
        profilepic = findViewById( R.id.profileIcon );
        radioGroup = findViewById( R.id.radioGroup );
        back = findViewById( R.id.GoBackIcon );
        database = FirebaseDatabase.getInstance();
        userdatabase = database.getReference("users");
        fAuth = FirebaseAuth.getInstance();
        date.setOnClickListener( new View.OnClickListener() {

         Calendar calendar = Calendar.getInstance();
         final int year = calendar.get( Calendar.YEAR );
         final int month = calendar.get( Calendar.MONTH );
         final  int day = calendar.get( Calendar.DAY_OF_MONTH );
         public void onClick(View v) {
                datePickerDialog=new DatePickerDialog( MainActivity2.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int i, int ii, int iii) {
                        date.setText(day+"/"+(month+1)+"/"+year);

                    }
                },year,month,day );
                datePickerDialog.show();
            }
        } );



        confirm.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Adduser();
            }
        } );


        back.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent back = new Intent( MainActivity2.this, MainActivity.class );
                startActivity( back );
            }
        } );

    }
    private String getExtension(Uri uri){
        ContentResolver cr = getContentResolver();
        MimeTypeMap mim = MimeTypeMap.getSingleton();
        return MimeTypeMap.getFileExtensionFromUrl(cr.getType( uri ));
    }


    public void Adduser(){
        int radiobuttinid = radioGroup.getCheckedRadioButtonId();
        radioButton = findViewById(radiobuttinid);
        Fullname = fullname.getText().toString().trim();
        Email = email.getText().toString().trim();
        Password = password.getText().toString().trim();
        Gender = radioButton.getText().toString().trim();
        Date = date.getText().toString().trim();
        phonenumber = phone.getText().toString().trim();
        calendar = Calendar. getInstance();
        dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Inscriptiondate = dateFormat.format(calendar.getTime());

        //TODO check informations
        /*if(TextUtils.isEmpty(email.getText())){
            email.setError("Email is Required.");
            return;
        }

        if(TextUtils.isEmpty(password.getText())){
            password.setError("Password is Required.");
            return;
        }

        if(password.length() < 6){
            mPassword.setError("Password Must be >= 6 Characters");
            return;
        }*/
        if(Fullname.length() >= 4 && Password.length() >= 6) {
            fAuth.createUserWithEmailAndPassword(Email, Password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {

                        fuser = fAuth.getCurrentUser();
                        assert fuser != null;
                        fuser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(MainActivity2.this, "Registered.", Toast.LENGTH_SHORT).show();
                                Users user = new Users(fuser.getUid(), Fullname, Email, Password, Date, Gender, phonenumber,Inscriptiondate);
                                userdatabase.child(fuser.getUid()).setValue(user);
                                Registered();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "onFailure: Email not sent " + e.getMessage());
                            }
                        });
                    }
                }
            });

        }
        else if(Fullname.length() < 4){
            fullname.setError("real name must be more than 4 letters!");
            return;
        }
        else if(Password.length() < 6){
            password.setError("Password must contain at least 6 characters!");
            return;
        }
        //add default profile picture with the new user
        //get image uri from drawable folder
        ProfilePicUri = (new Uri.Builder())
                .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                .authority(getResources().getResourcePackageName(R.drawable.user))
                .appendPath(getResources().getResourceTypeName(R.drawable.user))
                .appendPath(getResources().getResourceEntryName(R.drawable.user))
                .build();
        //Storage reference
        storageReference = FirebaseStorage.getInstance().getReference("DefaultProfilePicture");
        StorageReference ref = storageReference.child( System.currentTimeMillis()+ "." + getExtension( ProfilePicUri ));
        ref.putFile( ProfilePicUri ).addOnSuccessListener( new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

            }
        } ).addOnCompleteListener( new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                StorageReference UploadedImageReference = Objects.requireNonNull( Objects.requireNonNull( task.getResult() ).getMetadata() ).getReference();
                assert UploadedImageReference != null;
                UploadedImageReference.getDownloadUrl().addOnSuccessListener( new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        // add the DownloadUrl to a String variable
                        String URL = uri.toString();
                        //add the image information to the firebase database
                        UploadImages uploadImages = new UploadImages( fuser.getUid(), Fullname, Email, URL );
                        DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference( "ProfilePicture" ).child( fuser.getUid() ).child(fuser.getUid());
                        databaseReference1.setValue( uploadImages );
                    }
                } );


            }
        });
    }
    void Registered(){
        clearuserdata(this);
        Intent confirm = new Intent(MainActivity2.this, MainActivity5.class);
        store("email:" + fuser.getEmail() + ";"
                + "name:" + Fullname + ";"
                + "id:" + fuser.getUid() + ";" +
                "date:" + date + ";"
                + "gender:" + Gender + ";"
                + "number:" + phonenumber + ";"
                + "inscription:" + Inscriptiondate + ";", this);
        confirm.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(confirm);
    }
    private void setAnimation() {
        ConstraintLayout linearLayout = (ConstraintLayout) findViewById(R.id.linearLayout);
        AnimationDrawable animationDrawable = (AnimationDrawable) linearLayout.getBackground();
        animationDrawable.setEnterFadeDuration(2500);
        animationDrawable.setExitFadeDuration(5000);
        animationDrawable.start();
    }
}





