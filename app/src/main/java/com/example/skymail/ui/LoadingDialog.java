package com.example.skymail.ui;


import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;

import com.example.skymail.R;

public class LoadingDialog {
    private Activity activity;
    private AlertDialog alertDialog;

    public LoadingDialog(Activity myactivity){
        activity=myactivity;
    }

    public void startLoadingDialog(){
      AlertDialog.Builder builder= new AlertDialog.Builder(activity);
      LayoutInflater inflater = activity.getLayoutInflater();
      builder.setView(inflater.inflate(R.layout.loadingdialog,null));
      builder.setCancelable(false);
      alertDialog = builder.create();
      alertDialog.show();
    }

    public void dissmissDialog(){
        alertDialog.dismiss();
    }

}
