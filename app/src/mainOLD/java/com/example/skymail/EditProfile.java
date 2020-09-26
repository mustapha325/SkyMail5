package com.example.skymail;

import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
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
import com.example.skymail.Data.UploadImages;
import com.example.skymail.Data.Users;
import com.example.skymail.ui.TransitionDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import static com.example.skymail.Data.io.access;
import static com.example.skymail.Data.io.getLocale;
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
    private String phoneNumber;
    private ImageView Circleprofileimage;
    private ImageButton name,password,number;
    private static String imageFileName;
    private Users user;
    public File photoFile;
    private StorageReference storageReference;
    private String realPath;
    private static Uri UriImage,UriSave;
    private Thread StorePic,LoadPic;
    private TextView Email,Gender,Inscription,Birthdate,Fullname,Phone;
    private TransitionDialog tran=new TransitionDialog(EditProfile.this);
    private CircleImageView changeProfilePic;
    Button changeuserInfo,addImage,takePicture;

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
        Log.d("Editprofile",phoneNumber+birthdate+inscriptionDate+gender);
        Email = findViewById(R.id.Editprofile_useremail);
        Gender = findViewById(R.id.Editprofile_currentGender);
        Inscription = findViewById(R.id.Editprofile_currentInscriptionDate);
        Birthdate = findViewById(R.id.Editprofile_userBirthDay);
        Fullname = findViewById(R.id.Editprofile_displayName);
        Phone = findViewById(R.id.Editprofile_usernumber);
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
        }
    }

    private void CompressImage(String path){
       ImageCompression ic = new ImageCompression(getApplicationContext());
       String s = ic.compressImage(path,getFileName(UriSave));
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode==TAKE_PICTURE) {
            CompressImage(realPath);
            tran.startTransitionDialog();
            StoreProfilePic(UriSave);
        }
    }
    @Override
    public void onSingleImageSelected(Uri uri, String tag) {
        //Do something with your Uri
        UriImage = uri;
        Log.d("mmm ",uri.getPath());
        Circleprofileimage = findViewById(R.id.profile_image);
        assert UriImage != null;
        tran.startTransitionDialog();
        StoreProfilePic(UriImage);
        Circleprofileimage.setImageURI(UriImage);
        loadProfilePic(user.getUserID());
    }


    private void StoreProfilePic(Uri UriImage){
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
                        Toast.makeText(EditProfile.this, "Image uploaded", Toast.LENGTH_LONG).show();
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
                                tran.dissmissDialog();
                            }
                        });


                    }
                })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                Toast.makeText(EditProfile.this, "Error", Toast.LENGTH_LONG).show();
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
        android.app.AlertDialog alert;
        android.app.AlertDialog.Builder builder;
        builder = new android.app.AlertDialog.Builder(ctx);
        builder.setTitle(EditTitle);
        if(EditTitle.equals(getString(R.string.edit_displayname)))
        builder.setView(R.layout.edit_fullname);
        else builder.setView(R.layout.edit_phonenumber);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // Edit Firebase
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.setIcon(R.drawable.edit);
        alert = builder.create();
        alert.show();
    }

    void EditPass(String EditTitle,Context ctx,String id){
        android.app.AlertDialog alert;
        android.app.AlertDialog.Builder builder;
        builder = new android.app.AlertDialog.Builder(ctx);
        builder.setTitle(EditTitle);
        builder.setView(R.layout.edit_password);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // Edit Firebase
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.setIcon(R.drawable.edit);
        alert = builder.create();
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