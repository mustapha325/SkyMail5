package com.example.skymail.Adapter;


import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.skymail.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;


public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.RecyclerVH> {


    private Context c;
    private ArrayList<Uri> uris;



        public ImageAdapter(Context c, ArrayList<Uri> uris) {
        this.c = c;
        this.uris = uris;
    }

    @NonNull
    @Override
    public RecyclerVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ImageAdapter.RecyclerVH( LayoutInflater.from(c).inflate( R.layout.imageholder,parent,false));

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerVH holder, int position) {
        Picasso.get().load( uris.get( position ) ).into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return uris.size();
    }


    public static class RecyclerVH extends RecyclerView.ViewHolder
    {
        ImageView imageView;


         RecyclerVH(View itemView) {
            super(itemView);
            imageView = itemView.findViewById( R.id.imageholder);

        }


    }



}