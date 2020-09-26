package com.example.skymail;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.skymail.Data.Contacts;
import com.example.skymail.Data.UploadImages;
import com.example.skymail.Data.Users;
import com.example.skymail.Data.io;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class AddContact extends AppCompatActivity {

    Button cancel,save;
    EditText name,email,phonenumber;
    String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.add_contact );
        id = MainActivity5.userID;

        cancel = findViewById( R.id.cancel );
        save = findViewById( R.id.save );


        name = findViewById( R.id.contactname );
        email = findViewById( R.id.contactemail );
        phonenumber = findViewById(R.id.contactphonenumber);



        save.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contact();
                Toast.makeText(AddContact.this,"done",Toast.LENGTH_LONG ).show();
            }
        } );

        cancel.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goback = new Intent(AddContact.this,MyContactLIst.class);
                startActivity(goback);
            }
        } );



    }

    public void contact(){

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
        Query query = databaseReference.orderByChild( "email" ).equalTo( email.getText().toString().trim() );
        query.addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot d : dataSnapshot.getChildren()){
                    Users users = d.getValue(Users.class);

                    DatabaseReference profilepicture = FirebaseDatabase.getInstance().getReference("ProfilePicture");
                    assert users != null;
                    Query query1 = profilepicture.child( users.getUserID() );

                    query1.addValueEventListener( new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot i : dataSnapshot.getChildren()){
                                final UploadImages image = i.getValue(UploadImages.class);;

                                final DatabaseReference user = FirebaseDatabase.getInstance().getReference("users");
                                assert image != null;
                                Query query = user.orderByChild( "email" ).equalTo( image.getUserEmail() );
                                query.addValueEventListener( new ValueEventListener() {

                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        for (DataSnapshot u : dataSnapshot.getChildren()) {
                                            Users user = u.getValue( Users.class );
                                            assert user != null;
                                            DatabaseReference Contacts = FirebaseDatabase.getInstance().getReference("Contacts").child(id).child( "contacts" );
                                            com.example.skymail.Data.Contacts contact = new Contacts(image.getUserID(),image.getUserFullname(),image.getUserEmail(),image.getmImageUrl(),id,user.getPhonenumber());
                                            Contacts.child( image.getUserID() ).setValue( contact );
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








                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        } );



    }
}
