package com.example.skymail.ui.Draft;

import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.skymail.Conversation;
import com.example.skymail.Data.GetTimeAgo;
import com.example.skymail.Data.Messages;
import com.example.skymail.Data.io;
import com.example.skymail.Message;
import com.example.skymail.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

import static com.example.skymail.Data.io.access;
import static com.example.skymail.Data.io.getLocale;
import static java.lang.Long.parseLong;

public class DraftFragment extends Fragment implements Serializable {

    private DatabaseReference databaseReference;
    private RecyclerView rv;
    private GetTimeAgo time_calc = new GetTimeAgo();
    private String locale;
    private LinearLayoutManager layoutManager;
    private ArrayList<Messages> DraftedMessagesListe;
    private String DeletedMessage;
    private Query query;
    private List<Messages> AutoCompletemessagesListe;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_draft, container, false);
        AutoCompletemessagesListe = new ArrayList<>();
        rv = root.findViewById( R.id.Draftrv );
        locale = getLocale(getContext());
        DraftedMessagesListe = new ArrayList<>();
        layoutManager = new LinearLayoutManager( getActivity() );
        layoutManager.setReverseLayout( true );
        layoutManager.setStackFromEnd( true );
        databaseReference = FirebaseDatabase.getInstance().getReference("DraftMessages");
        String email = access(requireActivity()).getEmail();
        query = databaseReference.orderByChild( "from" ).equalTo( email );


        AutoCompleteFilter();
        getDataToArrayList();
        SwipeToRemove();

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();

        FireBaseRecyclerHandler();
    }

    private void SwipeToRemove(){
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }
            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        .addBackgroundColor( ContextCompat.getColor(getActivity(), R.color.yellow))
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
                                DeletedMessage = DraftedMessagesListe.get(position).getMessagID();
                                Query query1 = databaseReference.orderByChild("messagID").equalTo(DeletedMessage);
                                query1.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        for (DataSnapshot mSnapshot : dataSnapshot.getChildren()) {
                                            mSnapshot.getRef().removeValue();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                    }
                                });
                                DraftedMessagesListe.remove(position);
                            }}).start();
                        break;

                }
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper( simpleCallback );
        itemTouchHelper.attachToRecyclerView( rv);
    }
    private void FireBaseRecyclerHandler(){
        FirebaseRecyclerOptions<Messages> firebaseRecyclerOptions = new FirebaseRecyclerOptions.Builder<Messages>()
                .setQuery(query,Messages.class)
                .build();

        FirebaseRecyclerAdapter<Messages,DraftRecyclerVH> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Messages, DraftRecyclerVH>
                (firebaseRecyclerOptions) {
            @Override
            protected void onBindViewHolder(@NonNull DraftRecyclerVH holder, int position, @NonNull Messages model) {
                String date_calc=time_calc.getTimeAgo(parseLong(model.getDate()),getContext(),locale);
                holder.user.setText( model.getTo());
                holder.titel.setText(model.getSubject());
                holder.message.setText(model.getMessageText());
                holder.message.setText( model.getMessageText());
                holder.date.setText( date_calc);
                Picasso.get().load( model.getSenderProfilePicture()).fit().into( holder.ProfileIcon );

                holder.itemView.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent( getActivity(), Message.class );
                        Bundle bundle = new Bundle();
                        intent.putExtra( "remail",model.getTo() );
                        intent.putExtra( "text",model.getMessageText() );
                        intent.putExtra( "subject",model.getSubject() );
                        intent.putExtra( "object",model.getObject() );
                        intent.putExtra( "picture",model.getSenderProfilePicture() );
                        intent.putExtra( "FULLNAME",model.getSenderFullName() );
                        intent.putExtra( "url",model.getFileurl() );
                        intent.putExtra( "message_id",model.getMessagID() );
                        intent.putExtra( "id",model.getUserID() );
                        intent.putExtra( "from",model.getFrom() );
                        intent.putExtra( "from_where","draft" );
                        bundle.putSerializable( "autolist", (Serializable) AutoCompletemessagesListe );
                        intent.putExtras( bundle );
                        startActivity( intent );
                    }
                } );
            }

            @NonNull
            @Override
            public DraftRecyclerVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new DraftRecyclerVH( LayoutInflater.from( parent.getContext() ).inflate( R.layout.list_item ,parent,false ));
            }
        };
        //LAYOUT MANAGER
        rv.setLayoutManager(layoutManager);
        //setAdapter
        rv.setAdapter(firebaseRecyclerAdapter);

        firebaseRecyclerAdapter.startListening();



    }
    private void getDataToArrayList(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot draft : dataSnapshot.getChildren()) {
                            Messages DraftedMessages = draft.getValue(Messages.class);
                            DraftedMessagesListe.add(DraftedMessages);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
            }}).start();
    }

    static class DraftRecyclerVH extends RecyclerView.ViewHolder
    {
        TextView titel,message,user,date;
        CircleImageView ProfileIcon;


        DraftRecyclerVH(View itemView) {
            super(itemView);

            titel =  itemView.findViewById( R.id.Title);
            message = itemView.findViewById( R.id.Message );
            user = itemView.findViewById( R.id.user );
            ProfileIcon = itemView.findViewById( R.id.profileIcon);
            date = itemView.findViewById( R.id.message_date );

        }
    }
    public void AutoCompleteFilter() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("InboxMessages");
                Query query = databaseReference.orderByChild("to").equalTo(access(requireActivity()).getEmail());

                query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot m : snapshot.getChildren()) {
                            Messages messages = m.getValue(Messages.class);
                            AutoCompletemessagesListe.add(messages);


                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }


                });
            }}).start();
            }
}