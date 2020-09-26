package com.example.skymail.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.skymail.Data.Messages;
import com.example.skymail.Interface.RecyclerItemClick;
import com.example.skymail.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;


import de.hdodenhof.circleimageview.CircleImageView;

public class Search_result_adapter extends RecyclerView.Adapter<Search_result_adapter.RecyclerVH> implements Filterable{
    public ArrayList<Messages> search_messages_list;
    private List<Messages> full_search_messages_list;
    private RecyclerItemClick itemClick;
    private  Context context;

    public Search_result_adapter(Context context, ArrayList<Messages> search_messages_list, RecyclerItemClick itemClick){
        this.search_messages_list = search_messages_list;
        this.full_search_messages_list = new ArrayList<>( search_messages_list );
        this.itemClick = itemClick;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Search_result_adapter.RecyclerVH( LayoutInflater.from( context ).inflate( R.layout.list_item,parent,false),itemClick,search_messages_list);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerVH holder, int position) {

        holder.user.setText( search_messages_list.get( position ).getTo());
        holder.titel.setText(search_messages_list.get(position).getSubject());
        holder.message.setText(search_messages_list.get( position ).getMessageText());
        Picasso.get().load( search_messages_list.get( position ).getSenderProfilePicture()).fit().into( holder.ProfileIcon );

        ScaleAnimation scaleAnimation = new ScaleAnimation(0f, 1.1f, 0f, 1.1f);
        scaleAnimation.setFillAfter(false);
        scaleAnimation.setDuration(800);
        holder.cardView.setAnimation( scaleAnimation );


    }

    @Override
    public int getItemCount() {
        return search_messages_list.size();
    }



    @Override
    public Filter getFilter() {
        return FilterdList;
    }

    public Filter FilterdList = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            ArrayList<Messages> newList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                newList.addAll( full_search_messages_list );
            }else {
                String filterpattern = constraint.toString().toLowerCase().trim();
                for (Messages item : full_search_messages_list) {
                    if (item.getMessageText().toLowerCase().contains( filterpattern ) ||
                            item.getObject().toLowerCase().contains( filterpattern )||
                            item.getTo().toLowerCase().contains( filterpattern )||
                            item.getFrom().toLowerCase().contains( filterpattern )||
                            item.getSubject().toLowerCase().contains( filterpattern ))
                    {
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
            search_messages_list.addAll((List) results.values);
            notifyDataSetChanged();

        }
    };

    public void setFilterdList(ArrayList<Messages> list) {
        search_messages_list = list;
        notifyDataSetChanged();
    }


    public static class RecyclerVH extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView titel,message,user;
        RecyclerItemClick itemClick;
        CircleImageView ProfileIcon;
        List<Messages> list;
        CardView cardView;
        ImageButton edit;



        public RecyclerVH(@NonNull View itemView,RecyclerItemClick itemClick,List<Messages> list) {
            super( itemView );
            titel =  itemView.findViewById( R.id.Title);
            user = itemView.findViewById( R.id.user );
            message = itemView.findViewById( R.id.Message );
            ProfileIcon = itemView.findViewById( R.id.profileIcon);
            this.itemClick = itemClick;
            this.list=list;
            cardView = itemView.findViewById( R.id.cardView2 );
            edit = itemView.findViewById(R.id.editmessage);
            edit.setVisibility(View.INVISIBLE);
            itemView.setOnClickListener( this );
        }


        @Override
        public void onClick(View v) {
            itemClick.OnSearchItemClick( v,list.get( getAdapterPosition()));
        }
    }
}
