package com.example.skymail;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.skymail.Adapter.MyContactListAdapter;
import com.example.skymail.Data.Contacts;
import com.example.skymail.Interface.RecyclerItemClick;
import com.example.skymail.ui.Inbox.InboxMessageContainer;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MyContactLIst extends AppCompatActivity implements RecyclerItemClick {

    FloatingActionButton add;
    Toolbar contactlisttoolbar;
    ArrayList<Contacts> MyContactList;
    RecyclerView.LayoutManager layoutManager;
    RecyclerView rv;
     MyContactListAdapter myContactListAdapter;
    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.contactslist );
        contactlisttoolbar = findViewById( R.id.contactlisttoolbar );
        setSupportActionBar( contactlisttoolbar );
        getSupportActionBar().setTitle( "Contacts" );
        getSupportActionBar().setDisplayHomeAsUpEnabled( true );
        userID = MainActivity5.userID;

        rv = findViewById( R.id.contactlistRV);
        layoutManager = new LinearLayoutManager(this);
        Toast.makeText( this,userID,Toast.LENGTH_LONG ).show();
        MyContactList = new ArrayList<>();
        myContactListAdapter = new MyContactListAdapter( this,MyContactList,this);
        add = findViewById(R.id.addfab);

        add.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addcontact = new Intent( MyContactLIst.this, AddContact.class );
                startActivity( addcontact );
            }
        } );



        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Contacts").child( userID ).child("contacts");
        Query query = databaseReference.orderByChild( "userID" ).equalTo(userID);
        query.addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot c : dataSnapshot.getChildren()){
                    Contacts contacts = c.getValue(Contacts.class);
                    MyContactList.add(contacts);
                    rv.setLayoutManager( layoutManager );
                    rv.setAdapter(myContactListAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        } );





    }

    @Override
    public void OnItemClick(View v, int position) {
        Intent intent = new Intent( MyContactLIst.this, ContactInformation.class );
        intent.putExtra( "name",MyContactList.get( position ).getUserFullname() );
        intent.putExtra( "icon",MyContactList.get( position ).getUserProfilePicURI());
        intent.putExtra( "email",MyContactList.get( position ).getUserEmail());
        intent.putExtra( "phone",MyContactList.get( position ).getUserPhonenumber());
        startActivity(intent);
    }
}
