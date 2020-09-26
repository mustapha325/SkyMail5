package com.example.skymail.ui.send;

import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
import com.example.skymail.R;
import com.example.skymail.ui.TransitionDialog;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

import static com.example.skymail.Data.io.access;
import static com.example.skymail.Data.io.getLocale;
import static com.example.skymail.Data.io.showFCMSync;
import static java.lang.Long.parseLong;

public class SendFragment extends Fragment{

    public static Context SendFragmentContext;
    private String email;
    private ArrayList<Messages> SendedMessagesListe;
    private LinearLayoutManager manager;
    private RecyclerView rv;
    private String DeletedMessage;
    private Query query;
    private int counter=1;
    private DatabaseReference messagedatabase;
    private String locale;
    private ImageButton editmessage;
    private Map<String, Object> MessageMap;
    private SwipeRefreshLayout swipeRefreshLayout;
    TransitionDialog tr;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_send, container, false);
        MessageMap = new HashMap<>();
        swipeRefreshLayout = root.findViewById( R.id.fragmentsend );
        rv = root.findViewById( R.id.sendRV );
        locale = getLocale(getContext());
        email = access(SendFragmentContext).getEmail();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        messagedatabase = firebaseDatabase.getReference( "SendedMessages" );
        query = messagedatabase.orderByChild( "from" ).equalTo( email );
        //Declaring the ArrayList
        SendedMessagesListe = new ArrayList<>();
        //Layout Manager
        manager = new LinearLayoutManager( getActivity() );
        manager.setReverseLayout( true );
        manager.setStackFromEnd( true );

        rv.setLayoutManager( manager );




        getDataToArrayList();
        SwipeToRemove();

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
    public void onAttach(Context context) {
        super.onAttach(context);
        SendFragmentContext=context;
        tr = new TransitionDialog(getActivity());
        tr.startTransitionDialog();
    }





    private void SwipeToRemove(){
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
                                DeletedMessage = SendedMessagesListe.get(position).getMessagID();
                                Query query1 = messagedatabase.orderByChild("messagID").equalTo(DeletedMessage);
                                query1.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                                        for (DataSnapshot SendedMessages : dataSnapshot.getChildren()) {
                                            SendedMessages.getRef().removeValue();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                    }
                                });
                                SendedMessagesListe.remove(position);
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
                        for (DataSnapshot SendedMessages : dataSnapshot.getChildren()) {
                            Messages message = SendedMessages.getValue(Messages.class);
                            SendedMessagesListe.add(message);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }}).start();
    }
    private void FireBaseRecyclerHandler(){
        FirebaseRecyclerOptions<Messages> firebaseRecyclerOptions = new FirebaseRecyclerOptions.Builder<Messages>()
                .setQuery(query,Messages.class  ).build();

        FirebaseRecyclerAdapter<Messages,SendRecyclerVH> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Messages, SendRecyclerVH>
                (firebaseRecyclerOptions) {
            @Override
            protected void onBindViewHolder(@NonNull SendRecyclerVH holder, int position, @NonNull Messages model) {
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
                holder.date.setText( GetTimeAgo.getTimeAgo(parseLong(model.getDate()), getContext(), locale));
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
                        Intent intent = new Intent( getActivity(), Conversation.class );
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
                        startActivity( intent );
                    }

                } );
                holder.EditText.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDialog( position,model );

                    }
                } );
            }
            @NonNull
            @Override
            public SendRecyclerVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new SendRecyclerVH(LayoutInflater.from(parent.getContext()).inflate( R.layout.list_item ,parent,false ));
            }
        };
        //LAYOUT MANAGER
        rv.setLayoutManager(manager);
        //setAdapter
        rv.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }
    static class SendRecyclerVH extends RecyclerView.ViewHolder
    {
        TextView titel,message,user,date;
        CircleImageView ProfileIcon;
        ImageButton EditText;
        CardView cardView;

        SendRecyclerVH(View itemView) {
            super(itemView);

            titel =  itemView.findViewById( R.id.Title);
            message = itemView.findViewById( R.id.Message );
            user = itemView.findViewById( R.id.user );
            ProfileIcon = itemView.findViewById( R.id.profileIcon);
            date = itemView.findViewById( R.id.message_date );
            EditText = itemView.findViewById( R.id.editmessage );
            cardView = itemView.findViewById( R.id.cardView2 );
        }

    }

    protected void showDialog(int pos,Messages model){

        // set the custom layout
        final View customLayout = getLayoutInflater().inflate( R.layout.editmessage,null );
        //create an alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder( getActivity() );
        builder.setTitle( getString( R.string.Editor ) );
        EditText editsubject = customLayout.findViewById( R.id.editsubject );
        EditText editobject = customLayout.findViewById( R.id.editobject );
        EditText editmessagetext = customLayout.findViewById( R.id.editmessagetext );
        String subject = editsubject.getText().toString();
        String object = editobject.getText().toString();
        String messagetext = editmessagetext.getText().toString();

        editmessagetext.setText( SendedMessagesListe.get( pos ).getMessageText() );
        editsubject.setText( SendedMessagesListe.get( pos ).getSubject() );
        editobject.setText( SendedMessagesListe.get( pos ).getObject() );


        builder.setPositiveButton( "ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MessageMap.put( "messageText",  editmessagetext.getText().toString() );
                MessageMap.put( "subject",editsubject.getText().toString() );
                MessageMap.put( "object",editobject.getText().toString() );
                MessageMap.put( "messagID",model.getMessagID() );
                MessageMap.put( "from",model.getFrom() );
                MessageMap.put( "to",model.getTo() );
                MessageMap.put( "userID",model.getUserID() );
                MessageMap.put( "date",model.getDate() );
                MessageMap.put( "senderFullName",model.getSenderFullName() );
                MessageMap.put( "senderProfilePicture",model.getSenderProfilePicture() );
                MessageMap.put( "fileurl",model.getFileurl() );
                MessageMap.put( "replyMessageID",model.getReplyMessageID() );


                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        DatabaseReference SendedMessages = FirebaseDatabase.getInstance().getReference("SendedMessages").child(model.getMessagID());
                        DatabaseReference InboxMessages = FirebaseDatabase.getInstance().getReference("InboxMessages").child(model.getMessagID());
                        DatabaseReference Reply = FirebaseDatabase.getInstance().getReference("Reply").child(model.getMessagID()).child("reply").child(model.getReplyMessageID());
                        SendedMessages.setValue(MessageMap);
                        InboxMessages.setValue(MessageMap);
                        Reply.setValue(MessageMap);
                    }
                }).start();
            }
        } );
        builder.setView( customLayout );
        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    };


    private void UpdateMessage(String userid,String messageid,String subject,String object,
                               String message,String profilepic,String from,String to,
                               String result,String day,
                               String userfullname){

    }
    public void resetCounter(){counter=1;}

}