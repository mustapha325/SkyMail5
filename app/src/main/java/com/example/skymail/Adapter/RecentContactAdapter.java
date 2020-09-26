package com.example.skymail.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.skymail.Data.Contacts;
import com.example.skymail.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import de.hdodenhof.circleimageview.CircleImageView;

public class RecentContactAdapter extends RecyclerView.Adapter<RecentContactAdapter.ViewHolder> {
    private Context c;
    private ArrayList<Contacts> ContactsListe;


    public RecentContactAdapter(Context c, ArrayList<Contacts> contactsListe) {
        this.c = c;
        this.ContactsListe = contactsListe;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from( c ).inflate( R.layout.contact_item,parent,false);
        return new ViewHolder( view );

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Picasso.get().load( ContactsListe.get( position ).getUserProfilePicURI() ).fit().into( holder.profilepic );
        holder.fullname.setText( ContactsListe.get( position ).getUserFullname());
    }


    @Override
    public int getItemCount() {
        return ContactsListe.size();

    }



       static class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profilepic;
        TextView fullname;

             ViewHolder(@NonNull View itemView) {
                 super( itemView );
                 profilepic =  itemView.findViewById( R.id.contactpic );
                 fullname = itemView.findViewById( R.id.contactfullname );

             }
         }



}
