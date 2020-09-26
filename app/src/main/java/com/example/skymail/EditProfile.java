package com.example.skymail;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.asksira.bsimagepicker.BSImagePicker;
import com.asksira.bsimagepicker.Utils;
import com.bumptech.glide.Glide;
import com.example.skymail.Data.ImageCompression;
import com.example.skymail.Data.Messages;
import com.example.skymail.Data.RealPathUtil;
import com.example.skymail.Data.UploadImages;
import com.example.skymail.Data.Users;
import com.example.skymail.SplashScreen.SplashScreen;
import com.example.skymail.ui.TransitionDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
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
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Objects;

import static com.example.skymail.Data.io.access;
import static com.example.skymail.Data.io.getLocale;
import static com.example.skymail.Data.io.store;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfile extends AppCompatActivity implements BSImagePicker.OnSingleImageSelectedListener,
        BSImagePicker.ImageLoaderDelegate{
    private static final int PICK_IMAGE_REQUEST = 89;
    private static final int TAKE_PICTURE = 94;
    private String email;
    private String gender;
    private String inscriptionDate;
    private String birthdate;
    private String Name;
    private String phoneNumber,userpassword;
    private ImageView Circleprofileimage;
    private ImageButton name,password,number;
    private static String imageFileName;
    private Users user;
    public File photoFile;
    private StorageReference storageReference;
    private String realPath;
    private static Uri UriImage,UriSave;
    private Thread StorePic,LoadPic;
    private TextView Email,Gender,Inscription,Birthdate,Fullname,Phone,Password;
    private EditText editpass,editnewpass,phone,fullname;
    private TransitionDialog tran=new TransitionDialog(EditProfile.this);
    private CircleImageView changeProfilePic;
    Button addImage,takePicture;
    Boolean HasReplies=false;
    ArrayList <String> RepliesToModify= new ArrayList<String>();
    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        user=access(this);
        loadProfilePic(user.getUserID());
        email = user.getEmail();
        gender = user.getGender();
        inscriptionDate = user.getInscriptionDate();
        birthdate = user.getBirthdate();
        Name = user.getFullname();
        phoneNumber = user.getPhonenumber();
        userpassword = user.getPassword();
        Log.d("Editprofile",phoneNumber+birthdate+inscriptionDate+gender+userpassword);
        Email = findViewById(R.id.Editprofile_useremail);
        Gender = findViewById(R.id.Editprofile_currentGender);
        Inscription = findViewById(R.id.Editprofile_currentInscriptionDate);
        Birthdate = findViewById(R.id.Editprofile_userBirthDay);
        Fullname = findViewById(R.id.Editprofile_displayName);
        Phone = findViewById(R.id.Editprofile_usernumber);
        Password = findViewById(R.id.Editprofile_userPassword);
        String pass="";
        for(int x=0;x<userpassword.length();x++) pass = pass+"*";
        Password.setText(pass);
        Email.setText(email);
        storageReference = FirebaseStorage.getInstance().getReference("ProfilePicture");
        if (gender.equals("Male") || gender.equals("Masculin") || gender.equals("ذكر")) {
            Gender.setText(getStringByLocal(getApplicationContext(),R.string.Male,getLocale(getApplicationContext())));
        }
        else Gender.setText(getStringByLocal(getApplicationContext(),R.string.Female,getLocale(getApplicationContext())));
        Inscription.setText(inscriptionDate);
        Birthdate.setText(birthdate);
        Fullname.setText(Name.toUpperCase());
        Phone.setText(phoneNumber);
        changeProfilePic = findViewById(R.id.profile_image);

        name = findViewById(R.id.nameEdit);
        password = findViewById(R.id.Password_Edit);
        number = findViewById(R.id.numberEdit);

        password.setOnClickListener( new View.OnClickListener() {
        @Override
        public void onClick(View v) {
        EditPass(getString(R.string.edit_password),EditProfile.this,user.getUserID());
        }
        });
        name.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditThis(getString(R.string.edit_displayname),EditProfile.this,user.getUserID());
            }
        });
        number.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditThis(getString(R.string.edit_phone),EditProfile.this,user.getUserID());
            }
        });

        changeProfilePic.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // create an alert builder
                AlertDialog.Builder builder = new AlertDialog.Builder(EditProfile.this);
                builder.setTitle(getString(R.string.image_picker));
                // set the custom layout
                final View customLayout = getLayoutInflater().inflate(R.layout.image_choser, null);
                builder.setView(customLayout);
                // create and show the alert dialog
                AlertDialog dialog = builder.create();
                dialog.show();
                addImage=customLayout.findViewById( R.id.addimage );
                takePicture = customLayout.findViewById( R.id.takepicture );
                addImage.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        OpenPicChoser();
                        dialog.dismiss();
                    }
                } );
                takePicture.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        OpenDeviceCamera();
                        dialog.dismiss();
                    }
                } );
            }
        } );
    }


    void loadProfilePic(String ID){
        LoadPic = new Thread(new Runnable() {
            @Override
            public void run() {
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("ProfilePicture").child(ID);
                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot pic : dataSnapshot.getChildren()) {
                            UploadImages picture = pic.getValue(UploadImages.class);
                            assert picture != null;
                            Picasso.get().load(picture.getmImageUrl()).into(changeProfilePic);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
        LoadPic.start();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (UriSave != null) {
            //
        }
    }

    private String CompressImage(String path){
       ImageCompression ic = new ImageCompression(getApplicationContext());
       String s = ic.compressImage(path,getFileName(UriSave));
       return s;
    }
    private String CompressImg(String path,Uri uri){
        ImageCompression ic = new ImageCompression(getApplicationContext());
        String s = ic.compressImage(path,getFileName(uri));
        return s;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode==TAKE_PICTURE) {
            File tempo = new File(CompressImage(realPath));
            UriSave = FileProvider.getUriForFile(Objects.requireNonNull(getApplicationContext()),
                    BuildConfig.APPLICATION_ID + ".provider", tempo);
            tran.startTransitionDialog();
            StoreProfilePic(UriSave,tempo.getAbsolutePath());
        }
    }
    @Override
    public void onSingleImageSelected(Uri uri, String tag) {
        //Do something with your Uri
        UriImage = uri;
        Circleprofileimage = findViewById(R.id.profile_image);
        assert UriImage != null;
        tran.startTransitionDialog();
        File tmp = new File(UriImage.getPath());
        CompressImg(RealPathUtil.getRealPathFromURI_API19(this,uri),uri);
        StoreProfilePic(UriImage,tmp.getAbsolutePath());
        Circleprofileimage.setImageURI(UriImage);
        loadProfilePic(user.getUserID());
    }

        @Override
        public void onBackPressed() {
            super.onBackPressed();
            finish();
        }

    public void updateMessages(@Nullable String fullname,@Nullable String profilepic) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DatabaseReference mref = FirebaseDatabase.getInstance().getReference("InboxMessages");
                mref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            Messages msg = data.getValue(Messages.class);
                            Log.d("mssg ", msg.getMessagID());
                            assert msg != null;
                            if (user.getEmail().equals(msg.getTo())) {
                                if (fullname != null)
                                    mref.child(msg.getMessagID()).child("senderFullName").setValue(fullname);
                                else if (profilepic != null)
                                    mref.child(msg.getMessagID()).child("senderProfilePicture").setValue(profilepic);
                            }
                            //if(HasReplies(msg.getMessagID()))
                                UpdateReplies(msg.getMessagID(),fullname,profilepic);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(EditProfile.this, R.string.toast_network_error, Toast.LENGTH_SHORT).show();
                    }
                });
            UpdateSentMessages(fullname,profilepic);
            }}).start();
        }

    public void UpdateSentMessages(@Nullable String fullname,@Nullable String profilepic){
        new Thread(new Runnable() {
            @Override
            public void run() {
                DatabaseReference mref = FirebaseDatabase.getInstance().getReference("SendedMessages");
                mref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            Messages msg = data.getValue(Messages.class);
                            Log.d("mssg sent", msg.getMessagID());
                            assert msg != null;
                            if (user.getEmail().equals(msg.getFrom())) {
                                //if(HasReplies(msg.getMessagID()))
                                if (fullname != null)
                                    mref.child(msg.getMessagID()).child("senderFullName").setValue(fullname);
                                else if (profilepic != null)
                                    mref.child(msg.getMessagID()).child("senderProfilePicture").setValue(profilepic);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(EditProfile.this, R.string.toast_network_error, Toast.LENGTH_SHORT).show();
                    }
                });
            }}).start();
    }

        public void UpdateReplies(String key,@Nullable String name,@Nullable String pic){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    DatabaseReference Rref = FirebaseDatabase.getInstance().getReference("Reply");
                    Rref.child(key).child("reply").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot data : dataSnapshot.getChildren()) {
                                Messages msg = data.getValue(Messages.class);
                                Log.d("Reply ", msg.getReplyMessageID());
                                if(msg.getFrom().equals(user.getEmail())){
                                if (name != null)
                                    Rref.child(key).child("reply").child(msg.getReplyMessageID()).child("senderFullName").setValue(name);
                                if (pic != null)
                                    Rref.child(key).child("reply").child(msg.getReplyMessageID()).child("senderProfilePicture").setValue(pic);
                            }}
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(EditProfile.this, R.string.toast_network_error, Toast.LENGTH_SHORT).show();
                        }
                    });
                }}).start();
        }
    /*public boolean HasReplies(String key){
        final boolean[] is = new boolean[1];
        new Thread(new Runnable() {
            @Override
            public void run() {
                DatabaseReference Rref = FirebaseDatabase.getInstance().getReference("Reply");
                Rref.child(key).child("reply").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            Messages msg = data.getValue(Messages.class);
                            assert msg != null;
                            if (msg.getReplyMessageID() != null) {
                                is[0] =true;
                                return;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(EditProfile.this, R.string.toast_network_error, Toast.LENGTH_SHORT).show();
                    }
                });

            }}).start();
        return is[0];
    }
*/
    public void deletefile(String path){
        new File(path).getAbsoluteFile().delete();
    }

    void reloadActivity(){
        Intent intent = new Intent( EditProfile.this,EditProfile.class );
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity( intent );
        this.finish();
    }

    private void StoreProfilePic(Uri UriImage,String path){
        StorePic = new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                //Storage reference
                StorageReference ref = storageReference.child(System.currentTimeMillis() + "." + getExtension(UriImage));
                // Upload the Profile picture that we picked up to ref
                ref.putFile(UriImage).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(EditProfile.this, R.string.changes_applied, Toast.LENGTH_LONG).show();
                        deletefile(path);
                    }
                }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        //get the uploaded image reference to retrieve her DownloadUrl
                        StorageReference UploadedImageReference = Objects.requireNonNull(Objects.requireNonNull(task.getResult()).getMetadata()).getReference();
                        assert UploadedImageReference != null;
                        UploadedImageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                // add the DownloadUrl to a String variable
                                String URL = uri.toString();
                                //add the image information to the firebase database
                                UploadImages uploadImages = new UploadImages(user.getUserID(), user.getFullname(), user.getEmail().trim(), URL);
                                DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference("ProfilePicture").child(user.getUserID()).child(user.getUserID());
                                databaseReference1.setValue(uploadImages);
                                updateMessages(null,URL);
                                tran.dissmissDialog();
                            }
                        });
                    }
                })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                Toast.makeText(EditProfile.this, R.string.Error, Toast.LENGTH_LONG).show();
                                deletefile(path);
                                tran.dissmissDialog();
                            }
                        });
            }
        });
        StorePic.start();
    }

    public void openSettings(View v){
        startActivity(new Intent(EditProfile.this,SettingsActivity.class));
    }

    private String getExtension(Uri uri){
        ContentResolver cr = getContentResolver();
        MimeTypeMap mim = MimeTypeMap.getSingleton();
        return MimeTypeMap.getFileExtensionFromUrl(cr.getType( uri ));
    }

    BSImagePicker singleSelectionPicker = new BSImagePicker.Builder(BuildConfig.APPLICATION_ID+".provider")
            .setMaximumDisplayingImages(24) //Default: Integer.MAX_VALUE. Don't worry about performance :)
            .setSpanCount(3) //Default: 3. This is the number of columns
            .setGridSpacing(Utils.dp2px(2)) //Default: 2dp. Remember to pass in a value in pixel.
            .setPeekHeight(Utils.dp2px(360)) //Default: 360dp. This is the initial height of the dialog.
            .hideCameraTile() //Default: show. Set this if you don't want user to take photo.
            .hideGalleryTile() //Default: show. Set this if you don't want to further let user select from a gallery app. In such case, I suggest you to set maximum displaying images to Integer.MAX_VALUE.
            .setTag("A request ID") //Default: null. Set this if you need to identify which picker is calling back your fragment / activity.
            .useFrontCamera() //Default: false. Launching camera by intent has no reliable way to open front camera so this does not always work.
            .build();

    private void OpenPicChoser(){
        singleSelectionPicker.show(getSupportFragmentManager(), "picker");

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
    private void OpenDeviceCamera() {
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

    void EditThis(String EditTitle,Context ctx,String id){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference();
        android.app.AlertDialog alert;
        android.app.AlertDialog.Builder builder;
        builder = new android.app.AlertDialog.Builder(ctx);
        builder.setTitle(EditTitle);
        if(EditTitle.equals(getString(R.string.edit_displayname))){
            final View editLayout = getLayoutInflater().inflate(R.layout.edit_fullname, null);
            builder.setView(editLayout);
            DialogInterface.OnClickListener ListnerCancel = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(EditProfile.this, R.string
                            .settings_delete_aborted, Toast.LENGTH_SHORT).show();
                }
            };
            DialogInterface.OnClickListener ListnerAccept = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //Placeholder
                }
            };
            builder.setNegativeButton(R.string.button_abort, ListnerCancel);
            builder.setPositiveButton(R.string.button_confirm, ListnerAccept);
            builder.setIcon(R.drawable.edit);
            alert = builder.create();
            alert.setOnShowListener(new DialogInterface.OnShowListener() {

                @Override
                public void onShow(DialogInterface dialogInterface) {

                    Button button = (alert).getButton(AlertDialog.BUTTON_POSITIVE);
                    button.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            // Edit Firebase
                            fullname = editLayout.findViewById(R.id.edit_fullname);
                            String val = fullname.getText().toString().trim();
                            if (val.length() >= 8) {
                                ref.child("users").child(id).child("fullname").setValue(val);
                                changeInfo(user.getEmail());
                                updateMessages(val,null);
                                alert.dismiss();
                            } else {
                                fullname.setError(getStringByLocal(EditProfile.this, R.string.toast_namewrong, getLocale(EditProfile.this)));
                            }
                        }
                    });
                }
            });
            alert.show();
        }
        else{
            final View editLayout = getLayoutInflater().inflate(R.layout.edit_phonenumber, null);
            builder.setView(editLayout);
            DialogInterface.OnClickListener ListnerCancel = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(EditProfile.this, R.string
                            .settings_delete_aborted, Toast.LENGTH_SHORT).show();
                }
            };
            DialogInterface.OnClickListener ListnerAccept = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //Placeholder
                }
            };
            builder.setNegativeButton(R.string.button_abort, ListnerCancel);
            builder.setPositiveButton(R.string.button_confirm, ListnerAccept);
            builder.setIcon(R.drawable.phone);
            alert = builder.create();
            alert.setOnShowListener(new DialogInterface.OnShowListener() {

                @Override
                public void onShow(DialogInterface dialogInterface) {

                    Button button = (alert).getButton(AlertDialog.BUTTON_POSITIVE);
                    button.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            // Edit Firebase
                            phone = editLayout.findViewById(R.id.edit_phone_number);
                            String val = phone.getText().toString().trim();
                            if (TextUtils.isDigitsOnly(val) && val.length() >= 10) {
                                ref.child("users").child(id).child("phonenumber").setValue(phone.getText().toString().trim());
                                changeInfo(user.getEmail());
                                alert.dismiss();
                            } else {
                                EditText phone = editLayout.findViewById(R.id.edit_phone_number);
                                phone.setError(getStringByLocal(EditProfile.this, R.string.toast_phonewrong, getLocale(EditProfile.this)));
                            }
                        }
                    });
                }
            });
            alert.show();
        }
    }

    public void changeInfo(String email){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userdatabase = database.getReference();
        Thread updateInfo = new Thread(new Runnable() {
            @Override
            public void run() {

                Query Query = userdatabase.child("users").orderByChild("email").equalTo(email);
                Query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot users : dataSnapshot.getChildren()) {
                                Users user = users.getValue(Users.class);
                                assert user != null;
                                store("email:" + user.getEmail() + ";"
                                        + "name:" + user.getFullname() + ";"
                                        + "password:" + user.getPassword() + ";"
                                        + "id:" + user.getUserID() + ";" +
                                        "date:" + user.getBirthdate() + ";"
                                        + "gender:" + user.getGender() + ";"
                                        + "number:" + user.getPhonenumber() + ";"
                                        + "inscription:" + user.getInscriptionDate() + ";", EditProfile.this);
                                Log.d("ChangeInfo",user.getFullname()+" "+user.getEmail());
                                reloadActivity();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.d("ChangeInfo","Failed no correspondence in database");
                    }
                });
            }
        });
        updateInfo.start();
    }


    void EditPass(String EditTitle,Context ctx,String id){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference();
        final View editLayout = getLayoutInflater().inflate( R.layout.edit_password,null);
        android.app.AlertDialog alert;
        android.app.AlertDialog.Builder builder;
        builder = new android.app.AlertDialog.Builder(ctx);
        builder.setTitle(EditTitle);
        builder.setView(editLayout);
        DialogInterface.OnClickListener ListnerCancel = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(EditProfile.this, R.string
                        .settings_delete_aborted, Toast.LENGTH_SHORT).show();
            }
        };
        DialogInterface.OnClickListener ListnerAccept = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Placeholder
            }
        };
        builder.setNegativeButton(R.string.button_abort,ListnerCancel);
        builder.setPositiveButton(R.string.button_confirm,ListnerAccept);
        builder.setIcon(R.drawable.password_focus);
        alert = builder.create();
        alert.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialogInterface) {

                Button button = (alert).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        // Edit Firebase
                        editnewpass = editLayout.findViewById(R.id.editnewpass);
                        editpass = editLayout.findViewById(R.id.editpass);
                        String newval = editnewpass.getText().toString().trim();
                        if (editpass.getText().toString().trim().equals(userpassword)) {
                            ref.child("users").child(id).child("password").setValue(newval);
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            AuthCredential credential = EmailAuthProvider
                                    .getCredential(email, userpassword);
                            user.reauthenticate(credential)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                user.updatePassword(newval).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Log.d("PASSWORD UPDATE", "Password updated");
                                                            changeInfo(user.getEmail());
                                                        } else {
                                                            Log.d("PASSWORD UPDATE", "Error password not updated");
                                                        }
                                                    }
                                                });
                                            } else {
                                                Log.d("PASSWORD UPDATE", "Error auth failed");
                                            }
                                        }
                                    });
                            alert.dismiss();
                        }
                        else {
                            EditText pass = editLayout.findViewById(R.id.editpass);
                            pass.setError(getStringByLocal(EditProfile.this,R.string.toast_passwordwrong,getLocale(EditProfile.this)));
                        }
                    }
                });
            }
        });
        alert.show();
    }
    public static String getStringByLocal(Context context, int id, String locale) {
        Configuration configuration = new Configuration(context.getResources().getConfiguration());
        configuration.setLocale(new Locale(locale));
        return context.createConfigurationContext(configuration).getResources().getString(id);
    }

    @Override
    public void loadImage(Uri imageUri, ImageView ivImage) {
        Glide.with(EditProfile.this).load(imageUri).into(ivImage);
    }
}