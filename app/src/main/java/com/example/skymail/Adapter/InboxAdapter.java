package com.example.skymail.Adapter;



import android.content.*;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.skymail.Data.Messages;
import com.example.skymail.Interface.RecyclerItemClick;
import com.example.skymail.R;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import de.hdodenhof.circleimageview.CircleImageView;



public class InboxAdapter extends RecyclerView.Adapter<InboxAdapter.RecyclerVH> {


    private Context c;
    private ArrayList<Messages> Messages;
    private  RecyclerItemClick recyclerItemClick;


    public InboxAdapter(Context c, ArrayList<Messages> Messages,RecyclerItemClick recyclerItemClick) {
        this.c = c;
        this.Messages = Messages;
        this.recyclerItemClick = recyclerItemClick;

    }

    @NonNull
    @Override
    public RecyclerVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RecyclerVH( LayoutInflater.from( c ).inflate( R.layout.list_item, parent, false ), recyclerItemClick );
    }

    @Override
    public void onBindViewHolder(RecyclerVH holder, int position) {
        holder.user.setText( Messages.get( position ).getSenderFullName());
        holder.titel.setText(Messages.get(position).getSubject());
        holder.message.setText(Messages.get( position ).getMessageText());
        Picasso.get().load( Messages.get( position ).getSenderProfilePicture()).fit().into( holder.ProfileIcon );




    }

    @Override
    public int getItemCount() {
        return Messages.size();
    }




    public static class RecyclerVH extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        TextView titel,message,user;
        CircleImageView ProfileIcon;
        private RecyclerItemClick recyclerItemClick;


         RecyclerVH(View itemView,RecyclerItemClick recyclerItemClick) {
            super(itemView);

            titel =  itemView.findViewById(R.id.Title);
            message = itemView.findViewById( R.id.Message );
            user = itemView.findViewById( R.id.user );
            ProfileIcon = itemView.findViewById( R.id.profileIcon);
            this.recyclerItemClick = recyclerItemClick;
            itemView.setOnClickListener( this );
        }

        @Override
        public void onClick(View v) {
            recyclerItemClick.OnItemClick( v,getAdapterPosition() );

        }
    }

}