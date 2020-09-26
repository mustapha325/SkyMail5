package com.example.skymail.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.skymail.Data.Messages;
import com.example.skymail.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;


import de.hdodenhof.circleimageview.CircleImageView;

public class Search_result_adapter extends RecyclerView.Adapter<Search_result_adapter.RecyclerVH> implements Filterable {
    private ArrayList<Messages> search_messages_list;
    private List<Messages> full_search_messages_list;
    private  Context context;

    public Search_result_adapter(Context context,ArrayList<Messages> search_messages_list){
        this.search_messages_list = search_messages_list;
        this.full_search_messages_list = new ArrayList<>( search_messages_list );
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Search_result_adapter.RecyclerVH( LayoutInflater.from( context ).inflate( R.layout.list_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerVH holder, int position) {

        holder.user.setText( search_messages_list.get( position ).getTo());
        holder.titel.setText(search_messages_list.get(position).getSubject());
        holder.message.setText(search_messages_list.get( position ).getMessageText());
        Picasso.get().load( search_messages_list.get( position ).getSenderProfilePicture()).fit().into( holder.ProfileIcon );

    }

    @Override
    public int getItemCount() {
        return search_messages_list.size();
    }


    @Override
    public Filter getFilter() {
        return FilterdList;
    }

    private Filter FilterdList = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            ArrayList<Messages> newList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                newList.addAll( full_search_messages_list );
            }else {
                String filterpattern = constraint.toString().toLowerCase().trim();
                for (Messages item : full_search_messages_list) {
                    if (item.getMessageText().toLowerCase().contains( filterpattern )) {
                        newList.add( item );
                    }
                }
            }


            FilterResults results = new FilterResults();
            results.values = newList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            search_messages_list.clear();
            search_messages_list.addAll((List) results.values );
            notifyDataSetChanged();

        }
    };

    public void setFilterdList(ArrayList<Messages> list) {
        search_messages_list = list;
        notifyDataSetChanged();
    }


    public static class RecyclerVH extends RecyclerView.ViewHolder{

        TextView titel,message,user;
        CircleImageView ProfileIcon;



        public RecyclerVH(@NonNull View itemView) {
            super( itemView );
            titel =  itemView.findViewById( R.id.Title);
            user = itemView.findViewById( R.id.user );
            message = itemView.findViewById( R.id.Message );
            ProfileIcon = itemView.findViewById( R.id.profileIcon);
        }



    }
}
