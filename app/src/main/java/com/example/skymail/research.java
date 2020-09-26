package com.example.skymail;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.skymail.Adapter.RecentContactAdapter;
import com.example.skymail.Adapter.Search_result_adapter;
import com.example.skymail.Adapter.Search_result_adapter.RecyclerVH;
import com.example.skymail.Data.Contacts;
import com.example.skymail.Data.GetTimeAgo;
import com.example.skymail.Data.Messages;
import com.example.skymail.Interface.DrawerLocker;
import com.example.skymail.Interface.RecyclerItemClick;
import com.example.skymail.ui.Inbox.InboxFragment;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.security.cert.PKIXRevocationChecker;
import java.security.cert.PolicyQualifierInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static java.lang.Long.parseLong;


@SuppressWarnings("ALL")
public class research extends Fragment implements Serializable, RecyclerItemClick {
    private ArrayList<Contacts> ContactsListe;
    private String userID;
    private RecyclerView rv;
    private RecyclerView.LayoutManager layoutManager;
    private RecentContactAdapter recentContactAdapter;
    private ImageButton add;
    private LinearLayout ToContact;
    private DrawerLocker drawerLocker;
    public Search_result_adapter search_result_adapter;
    private RecyclerView search_RV;
    public List<Messages> fullmessageList;
    public  EditText search_bar;
    public DatabaseReference database;
    private ArrayList<Messages> FiltredMessageList;
    private FirebaseAuth fAuth;
    private FirebaseUser firebaseUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        database = FirebaseDatabase.getInstance().getReference( "InboxMessages" );
        View root = inflater.inflate( R.layout.fragment_research, container, false );
        fAuth = FirebaseAuth.getInstance();
        firebaseUser = fAuth.getCurrentUser();
        search_bar = root.findViewById( R.id.search_bar );
        search_RV = root.findViewById( R.id.search_result );
        search_RV.setVisibility(View.INVISIBLE);
        fullmessageList = new ArrayList<>();
        FiltredMessageList = new ArrayList<>( fullmessageList );
        userID = firebaseUser.getUid();

        add = root.findViewById( R.id.addcontact );
        ToContact = root.findViewById( R.id.ToContactList );

        //this line using Drawer Interface method for disable navigation drawer (DrawerLayout.LOCK_MODE_LOCKED_CLOSED) and false for
        ((MainActivity5) requireActivity()).enableDisableDrawer( DrawerLayout.LOCK_MODE_LOCKED_CLOSED, false );

        ContactsListe = new ArrayList<>();
        Search();
        search_result_adapter = new Search_result_adapter( getActivity(), (ArrayList<Messages>) fullmessageList ,this);


        Contact();


        add.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addcontact = new Intent( getActivity(), AddContact.class );
                startActivity( addcontact );
            }
        } );

        ToContact.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent contact = new Intent( getActivity(), MyContactLIst.class );
                startActivity( contact );
            }
        } );





        search_bar.addTextChangedListener( new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                filter(s.toString());

            }
        } );


        return root;
    }


    @Override
    public void onStart() {
        super.onStart();
        //Search_Filter();





        // Search_Filter(  );


    }



    @Override
    public  void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu( menu, inflater );
        inflater.inflate( R.menu.main_activity5, menu );
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id= item.getItemId();
        switch (id){
            case R.id.nav_inbox:
                Intent intent = new Intent( getActivity(),MainActivity5.class );
                startActivity( intent );
                break;
        }





        return super.onOptionsItemSelected( item );
    }

    public void Search(){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference( "InboxMessages" );
        Query query = databaseReference.orderByChild( "to" ).equalTo( MainActivity5.email );
        query.addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot m :snapshot.getChildren() ){
                    Messages messages = m.getValue(Messages.class);
                    fullmessageList.add( messages );
                    search_RV.setAdapter( search_result_adapter );


                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        } );
    }
    public void filter(String txt){
        ArrayList<Messages> filtredlist = new ArrayList<>();

        String filterpattern = txt.toLowerCase().trim();
        if (filterpattern.isEmpty()){
            search_RV.setVisibility(View.INVISIBLE);
        }else {
            search_RV.setVisibility(View.VISIBLE);
            for (Messages item : fullmessageList){
                if (item.getMessageText().contains( filterpattern ) || item.getSenderFullName().contains( filterpattern ) || item.getObject().contains( filterpattern ) || item.getSubject().contains( filterpattern )){

                    filtredlist.add( item );
                }
            }
            search_result_adapter.setFilterdList( (ArrayList<Messages>) filtredlist );
        }


    }

   /* public static class Search_RecyclerVH extends RecyclerView.ViewHolder{

        TextView titel,message,user;
        CircleImageView ProfileIcon;


        public Search_RecyclerVH(@NonNull View itemView) {
            super( itemView );
            titel =  itemView.findViewById( R.id.Title);
            user = itemView.findViewById( R.id.user );
            message = itemView.findViewById( R.id.Message );
            ProfileIcon = itemView.findViewById( R.id.profileIcon);
        }




    }*/

   /* public void Search_Filter(  ){
        Query query = database.orderByChild( "to" ).equalTo( MainActivity5.email );




        FirebaseRecyclerOptions<Messages> firebaseRecyclerOptions = new FirebaseRecyclerOptions.Builder<Messages>()
                .setQuery(query ,Messages.class ).build();

        FirebaseRecyclerAdapter<Messages, Search_RecyclerVH> Search_recyclerAdapter = new FirebaseRecyclerAdapter<Messages, Search_RecyclerVH>( firebaseRecyclerOptions ) {
            @Override
            protected void onBindViewHolder(@NonNull Search_RecyclerVH holder, int position, @NonNull Messages model) {

                    holder.user.setText( fullmessageList.get( position ).getTo() );
                    holder.titel.setText( fullmessageList.get( position ).getSubject() );
                    holder.message.setText( fullmessageList.get( position ).getMessageText() );
                    Picasso.get().load( fullmessageList.get( position ).getSenderProfilePicture() ).fit().into( holder.ProfileIcon );





            }

            @NonNull
            @Override
            public Search_RecyclerVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new Search_RecyclerVH( LayoutInflater.from( parent.getContext() ).inflate( R.layout.list_item, parent, false ) );
            }
        };


        search_RV.setAdapter( Search_recyclerAdapter );
        Search_recyclerAdapter.startListening();





    }*/

    public void Contact(){
        DatabaseReference contacts = FirebaseDatabase.getInstance().getReference( "Contacts" ).child( userID ).child( "contacts" );
        Query query = contacts.orderByChild( "userID" ).equalTo( userID );
        Toast.makeText( getContext(), userID, Toast.LENGTH_LONG ).show();
        query.addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot c : dataSnapshot.getChildren()) {
                    Contacts contacts1 = c.getValue( Contacts.class );
                    ContactsListe.add( contacts1 );

                    // rv.setLayoutManager( layoutManager );
                    //rv.setAdapter( recentContactAdapter );
                    // rv.setItemAnimator(new DefaultItemAnimator());

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        } );
    }

    @Override
    public void OnItemClick(View v, int position) {

    }

    @Override
    public void OnSearchItemClick(View v, Messages messages) {
        Intent intent = new Intent( getActivity(), Conversation.class );
        intent.putExtra("remail", messages.getTo());
        intent.putExtra("text", messages.getMessageText());
        intent.putExtra("subject", messages.getSubject());
        intent.putExtra("object", messages.getObject());
        intent.putExtra("picture", messages.getSenderProfilePicture());
        intent.putExtra("FULLNAME", messages.getSenderFullName());
        intent.putExtra("url", messages.getFileurl());
        intent.putExtra("message_id", messages.getMessagID());
        intent.putExtra("id", messages.getUserID());
        intent.putExtra("from", messages.getFrom());
        startActivity(intent);
    }
}
