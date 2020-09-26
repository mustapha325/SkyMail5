package com.example.skymail;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.akshay.library.CurveBottomBar;
import com.example.skymail.CloudMessaging.Token;
import com.example.skymail.Data.Messages;
import com.example.skymail.Data.Notifications;
import com.example.skymail.Data.UploadImages;
import com.example.skymail.Data.Users;
import com.example.skymail.Interface.DrawerLocker;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;
import static com.example.skymail.Data.io.access;
import static com.example.skymail.Data.io.clearuserdata;
import static com.example.skymail.Data.io.getLocale;
import static com.example.skymail.Data.io.showFCMSync;


public class MainActivity5 extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, DrawerLocker {

    private AppBarConfiguration mAppBarConfiguration;
    static String email;
    private Thread SyncNotifications;
    private static final String REQUEST_ACCEPT = "AC";
    private final int nav_menu_id = 16908332;
    private DrawerLayout mDrawerLayout;
    private NavController navController;
    private CircleImageView profileimage;
    private String id;
    private TextView  useremail, userfullname;
    private String fullname;
    private CurveBottomBar bottomNavigationView;
    static String userID;
    static String phonenumber;
    Users user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setApplicationLanguage(getLocale(getApplicationContext()));
        setContentView(R.layout.fragment_main_reception);
        profileimage = findViewById(R.id.profileimage);
        SharedPreferences p=getDefaultSharedPreferences(getApplicationContext());
        user=access(this);
        fullname = user.getFullname();
        email = user.getEmail();
        userID = user.getUserID();
        phonenumber = user.getPhonenumber();
         bottomNavigationView = findViewById(R.id.bottom_navigation);
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        lbm.registerReceiver(receiver, new IntentFilter(REQUEST_ACCEPT));
        mDrawerLayout = findViewById(R.id.drawer_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById( R.id.fab );
        fab.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent Message = new Intent( MainActivity5.this, Message.class );
                Message.putExtra("id",userID);
                startActivity( Message );
            }
        } );
        DrawerLayout drawer = findViewById( R.id.drawer_layout );
        NavigationView navigationView = findViewById( R.id.nav_view );
        View headerView = navigationView.getHeaderView( 0 );
        userfullname = headerView.findViewById( R.id.n );
        useremail= headerView.findViewById( R.id.e );
        userfullname.setText( fullname );
        useremail.setText( email );
        if(p.getBoolean("notifications",false))
            SyncNotifications(MainActivity5.this);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_draft,R.id.nav_inbox, R.id.nav_send,R.id.nav_Trash )
                .setDrawerLayout( drawer )
                .build();

        navController = Navigation.findNavController( this, R.id.nav_host_fragment );
        NavigationUI.setupActionBarWithNavController( this, navController, mAppBarConfiguration );
        NavigationUI.setupWithNavController( bottomNavigationView,navController );
        NavigationUI.setupWithNavController( navigationView, navController );

        DatabaseReference users = FirebaseDatabase.getInstance().getReference("users");
        Query query = users.orderByChild( "email" ).equalTo( useremail.getText().toString().trim() );
        query.addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot u : dataSnapshot.getChildren()){
                    Users user = u.getValue(Users.class);
                    assert user != null;
                    DatabaseReference databaseReference =FirebaseDatabase.getInstance().getReference("ProfilePicture").child( user.getUserID() );
                    databaseReference.addValueEventListener( new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for(DataSnapshot pic :dataSnapshot.getChildren()){
                                UploadImages picture = pic.getValue(UploadImages.class);
                                assert picture != null;
                                Picasso.get().load( picture.getmImageUrl() ).into( profileimage );
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    } );
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        } );


        profileimage.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity5.this,EditProfile.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        } );
        UpdateToken();
    }

    @Override
    public void onResume() {
        super.onResume();
        userfullname.setText(access(MainActivity5.this).getFullname());
        useremail.setText(access(MainActivity5.this).getEmail());
    }

    private void UpdateToken(){
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser!=null)
        if(firebaseUser.getEmail().equals(user.getEmail())) {
            String refreshToken = FirebaseInstanceId.getInstance().getToken();
            Token token = new Token(refreshToken);
            FirebaseDatabase.getInstance().getReference("Tokens").child(user.getUserID()).setValue(token);
        }
        }

    public BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                /*
               LayoutInflater inflater = getLayoutInflater();
               View NotiView = inflater.inflate(R.layout.notifications_layout, null);
               ImageView pic = NotiView.findViewById(R.id.notification_image);*/
            }
        }
    };

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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate( R.menu.main_activity5, menu );
        return true;
    }


    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController( this, R.id.nav_host_fragment );
        return NavigationUI.navigateUp( navController, mAppBarConfiguration )
                || super.onSupportNavigateUp();

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.settings:
                Intent i = new Intent( MainActivity5.this, SettingsActivity.class );
                startActivity( i );
                break;

            case nav_menu_id:
                if (getLocale(this).equals("ar")) mDrawerLayout.openDrawer( Gravity.RIGHT );
                else mDrawerLayout.openDrawer( Gravity.LEFT );
                break;


            case R.id.nav_search:
                Bundle bundle = new Bundle();
                bundle.putString( "ID",userID);
                research research = new research();
                research.setArguments( bundle );

        }


        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {


        return false;
    }

    public void settings(View view) {
        Intent settings = new Intent( MainActivity5.this, SettingsActivity.class );
        startActivity( settings );
    }

    public void exit(View view) {
        Intent intent = new Intent(MainActivity5.this,MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        RemoveTokenAtExit();
        clearuserdata(MainActivity5.this);
        finish();
    }

    public void RemoveTokenAtExit(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                DatabaseReference tkn = FirebaseDatabase.getInstance().getReference();
                tkn.child("Tokens").child(user.getUserID()).removeValue();
            }
        }).start();
    }


    public void SyncNotifications(Context ctx) {
        SyncNotifications = new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                DatabaseReference notis = FirebaseDatabase.getInstance().getReference();
                Query Query = notis.child("Notifications").orderByChild("emailto").equalTo(user.getEmail());
                Query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot users : dataSnapshot.getChildren()) {
                                Notifications noti = users.getValue(Notifications.class);
                                GetNotiMessage(ctx,noti.getMessageID());
                            }
                        } else {
                            Toast.makeText(ctx, R.string.No_notifications_tosync, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.d("SyncNotifications","Connection to database Failed ");
                    }
                });

            }
        });
        SyncNotifications.start();
    }
    public void GetNotiMessage(Context ctx,String id) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                DatabaseReference notis = FirebaseDatabase.getInstance().getReference();
                Query Query = notis.child("InboxMessages").orderByChild("messagID").equalTo(id);
                Query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot mssgs : dataSnapshot.getChildren()) {
                                Messages msg = mssgs.getValue(Messages.class);
                                showFCMSync(ctx,msg);
                            }
                        } else {
                            Toast.makeText(ctx, R.string.No_notifications_tosync, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.d("SyncNotifications","Connection to database Failed ");
                    }
                });

            }
        }).start();
    }

    @Override
    //DrawerLocker method
    public void enableDisableDrawer(int mode,boolean mode2) {
        //disable
        mDrawerLayout.setDrawerLockMode(mode);
        //disable action button in toolbar
        Objects.requireNonNull( getSupportActionBar() ).setDisplayHomeAsUpEnabled(mode2);
    }


}
