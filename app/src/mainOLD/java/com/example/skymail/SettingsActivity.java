package com.example.skymail;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Presentation;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.SwitchPreference;

import com.example.skymail.Data.Users;
import com.example.skymail.SplashScreen.SplashScreen;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import javax.annotation.Nullable;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;
import static com.example.skymail.Data.io.access;
import static com.example.skymail.Data.io.changeTheme;
import static com.example.skymail.Data.io.getLocale;
import static com.example.skymail.Data.io.restartApplication;

public class SettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences pref = getDefaultSharedPreferences(getApplicationContext());
        boolean darkTheme = pref.getBoolean("Theme",false);
        setTheme(darkTheme ? R.style.SettingsThemeDark : R.style.Theme_AppCompat_DayNight);
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment() ).commit();
    }

    public static class SettingsFragment extends PreferenceFragment {

        private  SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;
        private Context ctx;
        Users currentUser ;
        FirebaseUser user;
        @Override
        public void onAttach (Context context) {
            super.onAttach(context);
            ctx = context;
            currentUser = access(context.getApplicationContext());
            FirebaseAuth auth = FirebaseAuth.getInstance();
            user = auth.getCurrentUser();
        }
        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.root_preferences);
            final Preference prefList = findPreference("settings_language_pick");
            prefList.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object value) {
                    SharedPreferences prefs = getDefaultSharedPreferences(ctx);
                    prefs.edit().putString("locale",value.toString()).commit();
                    reload(getActivity());
                    return true;
                }
            });

            Preference switchpref = findPreference("switch");
            switchpref.setOnPreferenceChangeListener( new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    SharedPreferences prefs = getDefaultSharedPreferences(ctx);
                    prefs.edit().putBoolean( "Theme", (Boolean) newValue ).commit();
                    Log.d( "gg",newValue.toString() );
                    changeTheme(getActivity().getApplication().getApplicationContext());
                    getActivity().finish();
                    return true;
                }
            } );

            Preference notification = findPreference( "notifications" );
            notification.setOnPreferenceChangeListener( new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());
                    prefs.edit().putBoolean( "notification", (Boolean) newValue ).commit();
                    Log.d( "hhg",newValue.toString() );
                    Intent intent = new Intent( getActivity(),MainActivity5.class );
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity( intent );
                    getActivity().finish();
                    return true;
                }
            } );


        }
        @Override
        public boolean onPreferenceTreeClick (PreferenceScreen preferenceScreen,
                                              Preference preference)
        {
            String key = preference.getKey();
            switch(key) {
                case "settings_language_pick":
                    Log.d("tag","Pressed");
                    return true;
                case "check":
                    Log.d("tag","Pressed check");
                    return true;
                case "settings_delete":
                    Deleteshow(ctx);
                    Log.d("tag","Pressed delete");
                    return true;
                case "switch":

                    return true;
            }
            return false;
        }
        public void Deleteshow(Context ctx){
            AlertDialog alert;
            AlertDialog.Builder builder;
            if(ctx!=null) {
                builder = new AlertDialog.Builder(ctx);
                builder.setTitle(R.string.Settings_dialog_delete_title);
                builder.setMessage(R.string.settings_delete_message);
                builder.setPositiveButton(R.string.button_confirm, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        DeleteUser();
                    }
                });
                builder.setNegativeButton(R.string.button_abort, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(ctx, R.string.settings_delete_aborted, Toast.LENGTH_LONG).show();
                    }
                });
                builder.setIcon(R.drawable.delete);
                alert = builder.create();
                alert.show();
            }
        }
        public void DeleteUser(){
            String email = currentUser.getEmail();
            // continue with delete
            new Thread(new Runnable() {
                @Override
                public void run() {
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                    Query deleteQuery = ref.child("users").orderByChild("email").equalTo(email);
                    deleteQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot mSnapshot : dataSnapshot.getChildren()) {
                                mSnapshot.getRef().removeValue();
                                user.delete();
                                Toast.makeText(ctx, email + R.string.settings_delete_successful, Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                }}).start();
            exit(getActivity());
        }
        public void exit(Activity activity){
            Intent intent = new Intent(activity, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            activity.finish();
        }

        public void reload(Activity activity){
            Intent intent = new Intent(activity,SplashScreen.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            activity.finish();
        }



    }
}