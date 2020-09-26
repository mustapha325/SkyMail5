package com.example.skymail.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.skymail.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;


public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.RecyclerVH> {

    private Context context;
    private ArrayList<com.example.skymail.Data.Messages> Messages;

    public ConversationAdapter(Context context, ArrayList<com.example.skymail.Data.Messages> Messages){
        this.context = context;
        this.Messages = Messages;
    }




    @NonNull
    @Override
    public ConversationAdapter.RecyclerVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RecyclerVH( LayoutInflater.from( context ).inflate( R.layout.message_container,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerVH holder, int position) {

        holder.senderfullname.setText( Messages.get( position ).getSenderFullName());
        holder.subjectHolder.setText(Messages.get(position).getSubject());
        holder.textholder.setText(Messages.get( position ).getMessageText());
        holder.recieveremail.setText(Messages.get( position ).getTo());
        Picasso.get().load( Messages.get( position ).getSenderProfilePicture()).fit().into( holder.circleImageView );



    }


    @Override
    public int getItemCount() {
        return Messages.size();
    }


    public static class RecyclerVH extends RecyclerView.ViewHolder
    {
        CircleImageView circleImageView;
        TextView subjectHolder,senderfullname,recieveremail,textholder;



        RecyclerVH(View itemView) {
            super(itemView);
            circleImageView = itemView.findViewById( R.id.senderprofilepic);
            senderfullname = itemView.findViewById( R.id.senderfullname);
            subjectHolder = itemView.findViewById( R.id.SubjectHolder);
            recieveremail = itemView.findViewById( R.id.recieveremail);
            textholder = itemView.findViewById( R.id.textholder);


        }


    }





}
