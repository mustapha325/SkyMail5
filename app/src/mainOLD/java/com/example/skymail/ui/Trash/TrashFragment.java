package com.example.skymail.ui.Trash;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.skymail.Adapter.TrashAdapter;
import com.example.skymail.Data.Messages;
import com.example.skymail.R;

import java.util.ArrayList;

public class TrashFragment extends Fragment {


    private RecyclerView.LayoutManager manager;
    private ArrayList<Messages> TrashMessagesListe;
    private RecyclerView rv;
    private TrashAdapter trashAdapter;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_trash, container, false);
        //REFERENCE
        rv=  root.findViewById(R.id.inboxRV);
        //MANAGER
        manager = new LinearLayoutManager(getContext());
        //ADAPTER
        trashAdapter = new TrashAdapter( getActivity(),TrashMessagesListe);




        return root;
    }
}