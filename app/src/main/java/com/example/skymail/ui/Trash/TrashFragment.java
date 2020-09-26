package com.example.skymail.ui.Trash;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.example.skymail.Adapter.TrashAdapter;
import com.example.skymail.Conversation;
import com.example.skymail.Data.GetTimeAgo;
import com.example.skymail.Data.Messages;
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

import org.jetbrains.annotations.NotNull;

import static com.example.skymail.Data.io.access;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

import static com.example.skymail.Data.io.getLocale;
import static java.lang.Long.parseLong;

public class TrashFragment extends Fragment {

    private RecyclerView rv;
    private DatabaseReference messagedatabase;
    private FirebaseDatabase database;
    private TrashAdapter trashAdapter;
    private String email;
    private Query query;
    private int counter=1;
    private String locale;
    private String DeletedMessage;
    private ArrayList<Messages> TrashMessagesListe;
    private LinearLayoutManager manager;
    private Context TrashFragmentContext;
    private FirebaseRecyclerAdapter<Messages,TrashFragment.TrashRecyclerVH> firebaseRecyclerAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    TransitionDialog tr;
    private static ImageButton edit;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_trash, container, false);
        email = access( getActivity() ).getEmail();
        locale = getLocale(getContext());
        rv=  root.findViewById(R.id.trashRV);
        //MANAGER
        manager = new LinearLayoutManager(getContext());
        manager.setReverseLayout( true );
        manager.setStackFromEnd( true );
        //LAYOUT MANAGER
        rv.setLayoutManager(manager);
        //reference for the inboxMessages

        database = FirebaseDatabase.getInstance();
        messagedatabase = database.getReference("TrashMessages");

        TrashMessagesListe = new ArrayList<>();
        query = messagedatabase.orderByChild( "to" ).equalTo( email);
        SwipeToRemove();
        getDataToArrayList();




        return root;
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach( context );
        TrashFragmentContext=context;
        tr = new TransitionDialog(getActivity());
        tr.startTransitionDialog();
    }

    public void SwipeToRemove(){
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                 new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                         .addSwipeLeftActionIcon(R.drawable.delete)
                         .addSwipeRightActionIcon(R.drawable.ic_inbox_black_24dp)
                         .addSwipeLeftBackgroundColor( ContextCompat.getColor(getActivity(), R.color.red) )
                         .addSwipeRightBackgroundColor( ContextCompat.getColor(getActivity(), R.color.yellow) )
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
                                DeletedMessage = TrashMessagesListe.get(position).getMessagID();
                                Query query1 = messagedatabase.orderByChild("messagID").equalTo(DeletedMessage);
                                query1.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                                        for (DataSnapshot TrashMessages : dataSnapshot.getChildren()) {
                                            TrashMessages.getRef().removeValue();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NotNull DatabaseError databaseError) {
                                    }
                                });
                                TrashMessagesListe.remove(position);

                            }
                        }).start();
                        break;
                    case ItemTouchHelper.RIGHT :
                        new Thread( new Runnable() {
                            @Override
                            public void run() {
                                DatabaseReference root;
                                DeletedMessage = TrashMessagesListe.get(position).getMessagID();
                                root = database.getReference("InboxMessages");
                                root.child( TrashMessagesListe.get( position ).getMessagID() ).setValue( TrashMessagesListe.get( position ) );
                                Query query1 = messagedatabase.orderByChild("messagID").equalTo(DeletedMessage);
                                query1.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                                        for (DataSnapshot TrashMessages : dataSnapshot.getChildren()) {
                                            TrashMessages.getRef().removeValue();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NotNull DatabaseError databaseError) {
                                    }
                                });
                                TrashMessagesListe.remove(position);
                            }
                        } ).start();

                }

            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper( simpleCallback );
        itemTouchHelper.attachToRecyclerView( rv);

    }

    @Override
    public void onStart() {
        super.onStart();
        FireBaseRecyclerHandler();
    }

    public void FireBaseRecyclerHandler(){
        FirebaseRecyclerOptions<Messages> firebaseRecyclerOptions = new FirebaseRecyclerOptions.Builder<Messages>().setQuery(query, Messages.class)
                .build();
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Messages, TrashFragment.TrashRecyclerVH>(firebaseRecyclerOptions) {
            @Override
            protected void onBindViewHolder(@NonNull TrashFragment.TrashRecyclerVH holder, int position, @NonNull Messages model) {

                AlphaAnimation alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
                alphaAnimation.setDuration(500);
                alphaAnimation.setFillAfter(false);

                TranslateAnimation translateAnimation = new TranslateAnimation(-100.0f, 0.0f, 0f, 0f);
                translateAnimation.setDuration(800);

                ScaleAnimation scaleAnimation = new ScaleAnimation(0f, 1.1f, 0f, 1.1f);
                scaleAnimation.setFillAfter(false);
                scaleAnimation.setDuration(800);
                holder.cardView.setAnimation(scaleAnimation);
                holder.user.setText(model.getSenderFullName());
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
            public TrashFragment.TrashRecyclerVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new TrashFragment.TrashRecyclerVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false));
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

    public void resetCounter(){counter=1;}

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
                            TrashMessagesListe.add(message);
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



    public static class TrashRecyclerVH extends RecyclerView.ViewHolder
    {
        TextView titel,message,user,date;
        CircleImageView ProfileIcon;
        CardView cardView;

        TrashRecyclerVH(View itemView) {
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
}