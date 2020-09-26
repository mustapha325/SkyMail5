package com.example.skymail.ui.Inbox;

import android.annotation.SuppressLint;
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
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
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
import com.example.skymail.Data.Users;
import com.example.skymail.EditProfile;
import com.example.skymail.MainActivity;
import com.example.skymail.MainActivity5;
import com.example.skymail.R;
import com.example.skymail.ui.TransitionDialog;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.behavior.SwipeDismissBehavior;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import static com.example.skymail.Data.io.access;
import static com.example.skymail.Data.io.getLocale;
import static com.example.skymail.Data.io.showFCMSync;
import static com.example.skymail.Data.io.store;
import static java.lang.Long.parseLong;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class InboxFragment extends Fragment /*implements RecyclerItemClick*/ {


    private RecyclerView rv;
    private String email;
    private Context InboxFragmentContext;
    private ArrayList<Messages> InboxMessagesListe;
    private LinearLayoutManager manager;
    private String DeletedMessage;
    private Query query;
    private DatabaseReference messagedatabase;
    private DatabaseReference trashdatabase;
    private FirebaseDatabase database;
    private String locale;
    private int counter=1;
    private FirebaseRecyclerAdapter<Messages,InboxFragment.InboxRecyclerVH> firebaseRecyclerAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    TransitionDialog tr;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_inbox, container, false);
        email = access(InboxFragmentContext).getEmail();
        swipeRefreshLayout = root.findViewById( R.id.fragmentinbox );
        locale = getLocale(getContext());
        //Declaring the ArrayList
        InboxMessagesListe = new ArrayList<>();
        database = FirebaseDatabase.getInstance();
        //reference for the inboxMessages
        messagedatabase = database.getReference("InboxMessages");
        //query to get message information
        database = FirebaseDatabase.getInstance();
        query = messagedatabase.orderByChild( "to" ).equalTo(email);
        //REFERENCE
        rv=  root.findViewById(R.id.inboxRV);
        //MANAGER
        manager = new LinearLayoutManager(getContext());
        manager.setReverseLayout( true );
        manager.setStackFromEnd( true );
        //LAYOUT MANAGER
        rv.setLayoutManager(manager);
        //get user data by email
        getDataToArrayList();
        //Remove with Swipe
        SwipeToRemove();
        // Navigation Drawer Lock
        ((MainActivity5) requireActivity()).enableDisableDrawer( DrawerLayout.LOCK_MODE_UNLOCKED,true);

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
    public void onStart() {
        super.onStart();
        FireBaseRecyclerHandler();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        InboxFragmentContext=context;
        tr = new TransitionDialog(getActivity());
        tr.startTransitionDialog();
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
                                DeletedMessage = InboxMessagesListe.get(position).getMessagID();
                                trashdatabase = database.getReference("TrashMessages");
                                trashdatabase.child( InboxMessagesListe.get( position ).getMessagID() ).setValue(InboxMessagesListe.get( position ));
                                Query query1 = messagedatabase.orderByChild("messagID").equalTo(DeletedMessage);
                                query1.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NotNull DataSnapshot dataSnapshot) {

                                        for (DataSnapshot InboxMessages : dataSnapshot.getChildren()) {
                                            InboxMessages.getRef().removeValue();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NotNull DatabaseError databaseError) {
                                    }
                                });
                                InboxMessagesListe.remove(position);
                            }
                        }).start();
                        break;

                }

            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper( simpleCallback );
        itemTouchHelper.attachToRecyclerView( rv);

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
    public void getDataToArrayList(){
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
                        for (DataSnapshot m : dataSnapshot.getChildren()) {
                            Messages message = m.getValue(Messages.class);
                            InboxMessagesListe.add(message);
                        }
                    }
                    @SuppressLint("ShowToast")
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(getContext(), R.string.toast_network_error, Toast.LENGTH_LONG);
                        tr.setStarted();
                        tr.dissmissDialog();
                    }
                });
            }
        }).start();
    }

    public void FireBaseRecyclerHandler(){
                FirebaseRecyclerOptions<Messages> firebaseRecyclerOptions = new FirebaseRecyclerOptions.Builder<Messages>().setQuery(query, Messages.class)
                        .build();
                firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Messages, InboxFragment.InboxRecyclerVH>(firebaseRecyclerOptions) {
                    @Override
                    protected void onBindViewHolder(@NonNull InboxFragment.InboxRecyclerVH holder, int position, @NonNull Messages model) {


                        ScaleAnimation scaleAnimation = new ScaleAnimation(0f, 1.1f, 0f, 1.1f);
                        scaleAnimation.setFillAfter(false);
                        scaleAnimation.setDuration(800);
                        holder.cardView.setAnimation(scaleAnimation);
                        holder.user.setText(model.getSenderFullName());
                        holder.titel.setText(model.getSubject());
                        holder.message.setText(model.getMessageText());
                        holder.date.setText(GetTimeAgo.getTimeAgo(parseLong(model.getDate()), getContext(), locale));
                        Glide.with(getActivity()).load(model.getSenderProfilePicture()).into(holder.ProfileIcon);
                        Log.d("item ", String.valueOf(counter));
                        counter++;
                        if((counter == 6 || position==0) && tr.isStarted()) {
                            tr.setStarted();
                            tr.dissmissDialog();
                            resetCounter();
                        }

                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(getActivity(), Conversation.class);
                                intent.putExtra("remail", model.getTo());
                                intent.putExtra("text", model.getMessageText());
                                intent.putExtra("subject", model.getSubject());
                                intent.putExtra("object", model.getObject());
                                intent.putExtra("picture", model.getSenderProfilePicture());
                                intent.putExtra("FULLNAME", model.getSenderFullName());
                                intent.putExtra("url", model.getFileurl());
                                intent.putExtra("message_id", model.getMessagID());
                                intent.putExtra("id", model.getUserID());
                                intent.putExtra("from", model.getFrom());
                                startActivity(intent);
                            }
                        });

                    }
                    @NonNull
                    @Override
                    public InboxFragment.InboxRecyclerVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        return new InboxFragment.InboxRecyclerVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false));
                    }

                };
                //setAdapter
                rv.setAdapter(firebaseRecyclerAdapter);

        new Thread(new Runnable() {
            @Override
            public void run() {
                firebaseRecyclerAdapter.startListening();
            }}).start();
    }
    public static class InboxRecyclerVH extends RecyclerView.ViewHolder
    {
        TextView titel,message,user,date;
        CircleImageView ProfileIcon;
        ImageButton edit;
        CardView cardView;

        InboxRecyclerVH(View itemView) {
            super(itemView);
            titel =  itemView.findViewById(R.id.Title);
            message = itemView.findViewById( R.id.Message );
            user = itemView.findViewById( R.id.user );
            ProfileIcon = itemView.findViewById( R.id.profileIcon);
            date = itemView.findViewById( R.id.message_date );
            cardView = itemView.findViewById( R.id.cardView2 );
            edit = itemView.findViewById(R.id.editmessage);
            edit.setVisibility(View.INVISIBLE);
        }
    }

    public void resetCounter(){counter=1;}

}
