package com.example.skymail.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.skymail.Data.Contacts;
import com.example.skymail.Interface.RecyclerItemClick;
import com.example.skymail.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyContactListAdapter extends RecyclerView.Adapter<MyContactListAdapter.RecyclerVH>  {

    Context c;
    ArrayList<Contacts> MyContactList;
    RecyclerItemClick recyclerItemClick;

    public MyContactListAdapter(Context c, ArrayList<Contacts> MyContactList,RecyclerItemClick recyclerItemClick) {
        this.c = c;
        this.MyContactList = MyContactList;
        this.recyclerItemClick = recyclerItemClick;

    }

    @NonNull
    @Override
    public RecyclerVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from( c ).inflate( R.layout.list_item,parent,false );
        return new RecyclerVH(view,recyclerItemClick);
    }

    @Override
    public void onBindViewHolder( RecyclerVH holder, int position) {
        holder.user.setText(MyContactList.get( position ).getUserFullname());
        holder.titel.setText(MyContactList.get( position ).getUserEmail());
        Picasso.get().load( MyContactList.get( position ).getUserProfilePicURI()).into( holder.ProfileIcon );
        holder.editmessage.setVisibility( View.INVISIBLE );

        ScaleAnimation scaleAnimation = new ScaleAnimation(0f, 1.1f, 0f, 1.1f);
        scaleAnimation.setFillAfter(false);
        scaleAnimation.setDuration(800);
        holder.cardView.setAnimation( scaleAnimation );


    }

    @Override
    public int getItemCount() {
        return MyContactList.size();
    }

    public class RecyclerVH extends RecyclerView.ViewHolder implements View.OnClickListener  {

        TextView titel,message,user;
        CircleImageView ProfileIcon;
        RecyclerItemClick recyclerItemClick;
        ImageButton editmessage;
        CardView cardView;

        public RecyclerVH(@NonNull View itemView, RecyclerItemClick recyclerItemClick) {
            super( itemView );
            titel =  itemView.findViewById(R.id.Title);
            message = itemView.findViewById( R.id.Message );
            user = itemView.findViewById( R.id.user );
            ProfileIcon = itemView.findViewById( R.id.profileIcon);
            editmessage =itemView.findViewById( R.id.editmessage );
            this.recyclerItemClick = recyclerItemClick;
            cardView = itemView.findViewById( R.id.cardView2 );
            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            recyclerItemClick.OnItemClick( v,getAdapterPosition() );
        }
    }


}
