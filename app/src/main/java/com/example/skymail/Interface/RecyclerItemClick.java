package com.example.skymail.Interface;

import android.view.View;

import com.example.skymail.Data.Messages;

public interface RecyclerItemClick {
    void OnItemClick(View v, int position);
    void OnSearchItemClick(View v, Messages messages);
}
