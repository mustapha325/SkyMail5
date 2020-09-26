package com.example.skymail.ui.Draft;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.example.skymail.Conversation;
import com.example.skymail.Data.GetTimeAgo;
import com.example.skymail.Data.Messages;
import com.example.skymail.Data.Notifications;
import com.example.skymail.Data.io;
import com.example.skymail.Message;
import com.example.skymail.R;
import com.example.skymail.ui.TransitionDialog;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

import static com.example.skymail.Data.io.access;
import static com.example.skymail.Data.io.getLocale;
import static com.example.skymail.Data.io.showFCMSync;
import static java.lang.Long.parseLong;

public class DraftFragment extends Fragment implements Serializable {

    private DatabaseReference databaseReference;
    private RecyclerView rv;
    private String locale,email;
    private LinearLayoutManager layoutManager;
    private ArrayList<Messages> DraftedMessagesListe;
    private String DeletedMessage;
    private Query query;
    private int counter=1;
    private List<Messages> AutoCompletemessagesListe;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Context DraftFragmentContext;
    TransitionDialog tr;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_draft, container, false);
        AutoCompletemessagesListe = new ArrayList<>();
        swipeRefreshLayout = root.findViewById( R.id.fragmentdraft );
        rv = root.findViewById( R.id.Draftrv );
        locale = getLocale(getContext());
        DraftedMessagesListe = new ArrayList<>();
        layoutManager = new LinearLayoutManager( getActivity() );
        layoutManager.setReverseLayout( true );
        layoutManager.setStackFromEnd( true );
        databaseReference = FirebaseDatabase.getInstance().getReference("DraftMessages");
        email = access(requireActivity()).getEmail();
        query = databaseReference.orderByChild( "from" ).equalTo( email );


        AutoCompleteFilter();
        getDataToArrayList();
        if(DraftedMessagesListe.size()==0){
            tr.setStarted();
            tr.dissmissDialog();
        }        SwipeToRemove();

        swipeRefreshLayout.setOnRefreshListener( new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                FireBaseRecyclerHandler();
                SyncNotifications(requireContext());
                swipeRefreshLayout.setRefreshing( false );
            }
        } );

        return root;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        DraftFragmentContext=context;
        tr = new TransitionDialog(getActivity());
        tr.startTransitionDialog();
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
                        .addBackgroundColor( ContextCompat.getColor(getActivity(), R.color.red))
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
                AlphaAnimation alphaAnimation = new AlphaAnimation( 0.0f,1.0f );
                alphaAnimation.setDuration( 500 );
                alphaAnimation.setFillAfter( false );

                TranslateAnimation translateAnimation = new TranslateAnimation(-100.0f,0.0f,0f,0f);
                translateAnimation.setDuration( 800 );

                ScaleAnimation scaleAnimation = new ScaleAnimation(0f,1.1f,0f,1.1f );
                scaleAnimation.setFillAfter( false );
                scaleAnimation.setDuration( 800 );

                holder.cardView.setAnimation( scaleAnimation );

                holder.user.setText( model.getTo());
                holder.titel.setText(model.getSubject());
                holder.message.setText(model.getMessageText());
                holder.message.setText( model.getMessageText());
                holder.date.setText( GetTimeAgo.getTimeAgo(parseLong(model.getDate()),getContext(),locale));
                Glide.with(getActivity()).load(model.getSenderProfilePicture()).into(holder.ProfileIcon);
                Log.d("item ", String.valueOf(counter));
                counter++;
                if((counter == 6 || position==0) && tr.isStarted()) {
                    tr.setStarted();
                    tr.dissmissDialog();
                    resetCounter();
                }
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
                        if(!(dataSnapshot.hasChildren())){
                            tr.setStarted();
                            tr.dissmissDialog();
                        }
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
        CardView cardView;


        DraftRecyclerVH(View itemView) {
            super(itemView);

            titel =  itemView.findViewById( R.id.Title);
            message = itemView.findViewById( R.id.Message );
            user = itemView.findViewById( R.id.user );
            ProfileIcon = itemView.findViewById( R.id.profileIcon);
            date = itemView.findViewById( R.id.message_date );
            cardView = itemView.findViewById( R.id.cardView2 );
        }
    }

    public void SyncNotifications(Context ctx) {
        Thread SyncNotifications = new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                DatabaseReference notis = FirebaseDatabase.getInstance().getReference();
                Query Query = notis.child("Notifications").orderByChild("emailto").equalTo(email);
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
    public void resetCounter(){counter=1;}

}