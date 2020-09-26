package com.example.skymail;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactInformation extends AppCompatActivity {


    CircleImageView circleImageView;
    TextView contactername, contacterphone, contacteremail;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.contact_information );

        Intent intent = getIntent();
        String email = intent.getStringExtra( "email" );
        String image = intent.getStringExtra( "icon" );
        String name = intent.getStringExtra( "name" );
        String phone = intent.getStringExtra( "phone" );


        contactername = findViewById( R.id.contactname );
        contacterphone = findViewById( R.id.contactphone );
        contacteremail = findViewById( R.id.contactemail );
        circleImageView = findViewById( R.id.icon_inf );


        contacterphone.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent dialIntent = new Intent();
                dialIntent.setAction(Intent.ACTION_DIAL);
                dialIntent.setData(Uri.parse("tel:"+contacterphone.getText().toString()));
                startActivity( dialIntent );
            }
        } );

        contacteremail.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent email = new Intent(ContactInformation.this,Message.class);
                email.putExtra("email",contacteremail.getText().toString().trim());
                startActivity(email);
            }
        } );


        contactername.setText( name );
        contacteremail.setText( email );
        contacterphone.setText( phone );
        Picasso.get().load( image ).into( circleImageView );


    }



}
