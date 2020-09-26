package com.example.skymail;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.skymail.Adapter.MyContactListAdapter;
import com.example.skymail.Data.Contacts;
import com.example.skymail.Data.Messages;
import com.example.skymail.Data.io;
import com.example.skymail.Interface.RecyclerItemClick;
import com.example.skymail.ui.Inbox.InboxMessageContainer;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class MyContactLIst extends AppCompatActivity implements RecyclerItemClick {

    FloatingActionButton add;
    Toolbar contactlisttoolbar;
    ArrayList<Contacts> MyContactList;
    RecyclerView.LayoutManager layoutManager;
    RecyclerView rv;
     MyContactListAdapter myContactListAdapter;
    String userID;
     DatabaseReference messagedatabase;
     String DeletedMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.contactslist );
        contactlisttoolbar = findViewById( R.id.contactlisttoolbar );
        setSupportActionBar( contactlisttoolbar );
        getSupportActionBar().setTitle( "Contacts" );
        getSupportActionBar().setDisplayHomeAsUpEnabled( true );
        userID = io.access(this).getUserID();
        messagedatabase = FirebaseDatabase.getInstance().getReference("Contacts");

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

        SwipeToRemove();


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

    @Override
    public void OnSearchItemClick(View v, Messages messages) {

    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case android.R.id.home:
                onBackPressed();
                return  true;
        }

        return super.onOptionsItemSelected( item );
    }

    public void SwipeToRemove(){
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        .addBackgroundColor( ContextCompat.getColor(MyContactLIst.this, R.color.red))
                        .addActionIcon(R.drawable.delete)
                        .create()
                        .decorate();
                super.onChildDraw( c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive );
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                switch (direction){
                    case ItemTouchHelper.LEFT:
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                DeletedMessage = MyContactList.get(position).getContacterID();
                                Query query1 = messagedatabase.child(userID).child( "contacts" ).orderByChild("contacterID").equalTo(DeletedMessage);
                                query1.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NotNull DataSnapshot dataSnapshot) {

                                        for (DataSnapshot contact : dataSnapshot.getChildren()) {
                                            contact.getRef().removeValue();

                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NotNull DatabaseError databaseError) {
                                    }
                                });

                            }
                        }).start();
                        break;

                }
                MyContactList.remove(position);
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper( simpleCallback );
        itemTouchHelper.attachToRecyclerView( rv);

    }

}
