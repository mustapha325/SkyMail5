package com.example.skymail.Adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.skymail.Data.Messages;
import com.example.skymail.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.skymail.Adapter.SendAdapter.RecyclerVH;


public  class SendAdapter extends RecyclerView.Adapter<RecyclerVH> {


    private Context c;
    private ArrayList<Messages> Messages;

    public SendAdapter(Context c, ArrayList<Messages> Messages) {
        this.c = c;
        this.Messages = Messages;
    }

    @NonNull
    @Override
    public RecyclerVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RecyclerVH(LayoutInflater.from(c).inflate(R.layout.list_item,parent,false));
    }

    @Override
    public void onBindViewHolder(RecyclerVH holder, int position) {
        holder.user.setText( Messages.get( position ).getTo());
        holder.titel.setText(Messages.get(position).getSubject());
        holder.message.setText(Messages.get( position ).getMessageText());
        Picasso.get().load( Messages.get( position ).getSenderProfilePicture()).fit().into( holder.ProfileIcon );


    }

    @Override
    public int getItemCount() {
        return Messages.size();
    }


    static class RecyclerVH extends RecyclerView.ViewHolder
    {
        TextView titel,message,user;
        CircleImageView ProfileIcon;


        RecyclerVH(View itemView) {
            super(itemView);

            titel =  itemView.findViewById(R.id.Title);
            message = itemView.findViewById( R.id.Message );
            user = itemView.findViewById( R.id.user );
            ProfileIcon = itemView.findViewById( R.id.profileIcon);

        }
    }
}