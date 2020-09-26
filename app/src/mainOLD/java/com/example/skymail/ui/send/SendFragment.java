package com.example.skymail.ui.send;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.skymail.Conversation;
import com.example.skymail.Data.GetTimeAgo;
import com.example.skymail.Data.Messages;
import com.example.skymail.R;
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

import de.hdodenhof.circleimageview.CircleImageView;
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

import static com.example.skymail.Data.io.access;
import static com.example.skymail.Data.io.getLocale;
import static java.lang.Long.parseLong;

public class SendFragment extends Fragment{

    public static Context SendFragmentContext;
    private String email;
    private ArrayList<Messages> SendedMessagesListe;
    private LinearLayoutManager manager;
    private RecyclerView rv;
    private String DeletedMessage;
    private Query query;
    private DatabaseReference messagedatabase;
    private GetTimeAgo time_calc = new GetTimeAgo();
    private String locale;
    private ImageButton editmessage;
    private Map<String, Object> MessageMap;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_send, container, false);
        MessageMap = new HashMap<>();
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
    private void getDataToArrayList(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
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
                String date_calc=time_calc.getTimeAgo(parseLong(model.getDate()),getContext(),locale);
                holder.user.setText( model.getTo());
                holder.titel.setText(model.getSubject());
                holder.message.setText(model.getMessageText());
                holder.date.setText( date_calc);
                Picasso.get().load( model.getSenderProfilePicture()).fit().into( holder.ProfileIcon );

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

        SendRecyclerVH(View itemView) {
            super(itemView);

            titel =  itemView.findViewById( R.id.Title);
            message = itemView.findViewById( R.id.Message );
            user = itemView.findViewById( R.id.user );
            ProfileIcon = itemView.findViewById( R.id.profileIcon);
            date = itemView.findViewById( R.id.message_date );
            EditText = itemView.findViewById( R.id.editmessage );
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

}