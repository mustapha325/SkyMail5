package com.example.skymail;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.skymail.Data.Users;
import com.example.skymail.SplashScreen.SplashScreen;
import com.example.skymail.ui.LoadingDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static com.example.skymail.Data.NetworkUtils.isConnected;
import static com.example.skymail.Data.NetworkUtils.isInternetOn;
import static com.example.skymail.Data.io.CheckUserDataPresence;
import static com.example.skymail.Data.io.store;

public class MainActivity extends AppCompatActivity {
    public static final int MULTIPLE_PERMISSIONS = 10;
    Button signin;
    Button login;
    DatabaseReference userdatabase;
    EditText email,password;
    Query Query;
    private static Context context;
    FirebaseAuth fAuth;
    ProgressBar loadbar;
    final LoadingDialog LoadDialog=new LoadingDialog(MainActivity.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        animate();
        signin = findViewById(R.id.Signin);
        login = findViewById(R.id.Login);
        userdatabase = FirebaseDatabase.getInstance().getReference();
        email = findViewById(R.id.Email2);
        password = findViewById(R.id.Password2);
        fAuth = FirebaseAuth.getInstance();
        loadbar = findViewById(R.id.loadBar);
        checkPermissions();
        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sign = new Intent(MainActivity.this, MainActivity2.class);
                startActivity(sign);
            }
        });


        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String Email,Password;
                Email = email.getText().toString().trim();
                Password = password.getText().toString().trim();
                if(TextUtils.isEmpty(Email)){
                    email.setError("Email is Required.");
                    return;
                }

                if(TextUtils.isEmpty(Password)){
                    password.setError("Password is Required.");
                    return;
                }
                else {
                    LoadDialog.startLoadingDialog();
                }
                fAuth.signInWithEmailAndPassword(Email,Password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Log.d("LOGIN", "Logged in Successfully");
                            changeInfo(email.getText().toString().trim());
                        }else {
                            Log.d("LOGIN", "Error ! " + task.getException().getMessage());
                            if(!isInternetOn(MainActivity.this)) password.setError("Turn on internet connection");
                            else if(!isConnected()) password.setError("Error no connection");
                            //password.setError(task.getException().getMessage());
                            LoadDialog.dissmissDialog();
                        }

                    }
                });
            }
        });
    }
    public void changeInfo(String email){
        Thread updateInfo = new Thread(new Runnable() {
            @Override
            public void run() {

                Query = userdatabase.child("users").orderByChild("email").equalTo(email);
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
                                        + "inscription:" + user.getInscriptionDate() + ";", MainActivity.this);
                                Log.d("ChangeInfo",user.getFullname()+" "+user.getEmail());
                                assert LoadDialog!=null;
                                LoadDialog.dissmissDialog();
                                Intent i = new Intent(getApplicationContext(),MainActivity5.class);
                                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(i);
                                FirebaseUser fuser = fAuth.getCurrentUser();
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

    public void setApplicationLanguage(String newLanguage) {
        Resources activityRes = getResources();
        Configuration activityConf = activityRes.getConfiguration();
        Locale newLocale = new Locale(newLanguage);
        activityConf.setLocale(newLocale);
        activityRes.updateConfiguration(activityConf, activityRes.getDisplayMetrics());
        Resources applicationRes = getResources();
        Configuration applicationConf = applicationRes.getConfiguration();
        applicationConf.setLocale(newLocale);
        applicationRes.updateConfiguration(applicationConf,
                applicationRes.getDisplayMetrics());
        getResources().updateConfiguration(activityConf, getApplicationContext().getResources().getDisplayMetrics());
    }

    //request permission ta3 android >=6.0
    public void checkPermissions() {
        List<String> requiredPermissions = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d(getString(R.string.app_name),"write perms");
            requiredPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d(getString(R.string.app_name),"read perms");
            requiredPermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d(getString(R.string.app_name),"cam perms");
            requiredPermissions.add(Manifest.permission.CAMERA);
        }
        if (!requiredPermissions.isEmpty()) {
            Log.d(getString(R.string.app_name),"asking for perms");
            ActivityCompat.requestPermissions(MainActivity.this,
                    requiredPermissions.toArray(new String[]{}),
                    MULTIPLE_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MULTIPLE_PERMISSIONS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(getString(R.string.app_name),"All permissions are set!");
                } else {
                    Toast.makeText(this, "Failed to get some perms!", Toast.LENGTH_LONG).show();
                }
                break;
            }
            default:
                // no use
                break;
        }
    }

    public void animate(){
        createClouds();
        setAnimation();
    }
    private void setAnimation() {
        ConstraintLayout linearLayout = (ConstraintLayout) findViewById(R.id.linearLayoutmain);
        AnimationDrawable animationDrawable = (AnimationDrawable) linearLayout.getBackground();
        animationDrawable.setEnterFadeDuration(2500);
        animationDrawable.setExitFadeDuration(5000);
        animationDrawable.start();
    }
    private void createClouds() {
        for (int x = 0; x < 25; x++) {
            ImageView cloud = createCloud();
            ConstraintLayout parentLayout = findViewById(R.id.login_cloud_container);
            int parentLayoutid = findViewById(R.id.login_cloud_container).getId();
            cloud.setId(View.generateViewId());
            parentLayout.addView(cloud);
            ConstraintSet set = new ConstraintSet();
            set.clone(parentLayout);
            set.connect(cloud.getId(), ConstraintSet.TOP, parentLayoutid, ConstraintSet.TOP, 0);
            set.connect(cloud.getId(), ConstraintSet.END, parentLayoutid, ConstraintSet.END, 0);
            set.connect(cloud.getId(), ConstraintSet.BOTTOM, parentLayoutid, ConstraintSet.BOTTOM, 0);
            set.setVerticalBias(cloud.getId(), (float) getRandomNumberUsingNextDouble(0.0, 1.0));
            set.applyTo(parentLayout);
            animateCloud(cloud);
        }
    }
        @SuppressLint("UseCompatLoadingForDrawables")
        public ImageView createCloud(){
            ImageView img = new ImageView(this);
            img.setImageDrawable(getDrawable(R.drawable.ic_cloud));
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    getRandomNumberUsingNextInt(100, 250),
                    getRandomNumberUsingNextInt(100, 250));
            img.setLayoutParams(layoutParams);
            float alpha = (float)getRandomNumberUsingNextDouble(0.4, 0.8);
            float translationX = (float)getRandomNumberUsingNextDouble(-500.0, 450.0);
            img.setTranslationX(translationX);
            img.setAlpha(alpha);
            return img;
            }
        private void animateCloud(ImageView cloud) {
        ObjectAnimator CloudX = ObjectAnimator.ofFloat(cloud, "translationX",-1250f);
            Long duration = getRandomNumberUsingNextLong(4500, 9500);
            int repeatCount = ValueAnimator.INFINITE;
            CloudX.setDuration(duration);
            CloudX.setRepeatCount(repeatCount);
            CloudX.start();
        }
        public int getRandomNumberUsingNextInt(int min, int max) {
            Random random = new Random();
            return random.nextInt(max - min) + min;
        }
        public double getRandomNumberUsingNextDouble(double rangeMin, double rangeMax) {
            Random r = new Random();
            double randomValue = rangeMin + (rangeMax - rangeMin) * r.nextDouble();
            return randomValue;
        }
    public Long getRandomNumberUsingNextLong(int rangeMin, int rangeMax) {
        return ThreadLocalRandom.current().nextLong(rangeMin, rangeMax);
    }

    }

