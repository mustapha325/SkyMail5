package com.example.skymail.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.skymail.Data.Messages;
import com.example.skymail.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AutoCompleteAdapter extends ArrayAdapter<Messages> {

    private List<Messages> messagesArrayListFull;


    public AutoCompleteAdapter(@NonNull Context context, @NonNull List<Messages> messages) {
        super( context, 0, messages );
        this.messagesArrayListFull = new ArrayList<>( messages );

    }



    @Override
    public Filter getFilter() {
        return ListFilter;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null){
            convertView = LayoutInflater.from( getContext() ).inflate( R.layout.custom_auto_complete_adapter,parent,false );
        }
        TextView fullname = convertView.findViewById( R.id.auto_fullname );
        TextView email = convertView.findViewById( R.id.auto_email );
        CircleImageView pic = convertView.findViewById( R.id.auto_pic );

        Messages messages = getItem( position );
        if (messages !=null){
            fullname.setText( messages.getSenderFullName() );
            email.setText( messages.getFrom() );
            Picasso.get().load( getItem( position ).getSenderProfilePicture()).fit().into( pic );
        }

        return  convertView;
    }

    private Filter ListFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            List<Messages> suggestions = new ArrayList<>();
            if (constraint == null || constraint.length() == 0){
                suggestions.addAll( messagesArrayListFull );
            }else {
                String FilterPattern = constraint.toString().toLowerCase().trim();
                for (Messages m : messagesArrayListFull){
                    if (m.getSenderFullName().startsWith( FilterPattern )){
                        suggestions.add( m );
                    }
                }

            }
            results.values = suggestions;
            results.count = suggestions.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            clear();
            addAll( (List)results.values );
            notifyDataSetChanged();
        }

        @Override
        public CharSequence convertResultToString(Object resultValue) {
            return ((Messages)resultValue).getFrom();
        }
    };



}
