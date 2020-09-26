package com.example.skymail.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
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
    }

    @Override
    public int getItemCount() {
        return MyContactList.size();
    }

    public class RecyclerVH extends RecyclerView.ViewHolder implements View.OnClickListener  {

        TextView titel,message,user;
        CircleImageView ProfileIcon;
        RecyclerItemClick recyclerItemClick;

        public RecyclerVH(@NonNull View itemView, RecyclerItemClick recyclerItemClick) {
            super( itemView );
            titel =  itemView.findViewById(R.id.Title);
            message = itemView.findViewById( R.id.Message );
            user = itemView.findViewById( R.id.user );
            ProfileIcon = itemView.findViewById( R.id.profileIcon);
            this.recyclerItemClick = recyclerItemClick;
            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            recyclerItemClick.OnItemClick( v,getAdapterPosition() );
        }
    }


}
