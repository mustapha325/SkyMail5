package com.example.skymail;

import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.asksira.bsimagepicker.BSImagePicker;
import com.asksira.bsimagepicker.Utils;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class ShowProfile extends AppCompatActivity implements BSImagePicker.OnSingleImageSelectedListener,
        BSImagePicker.OnMultiImageSelectedListener,
        BSImagePicker.ImageLoaderDelegate,
        BSImagePicker.OnSelectImageCancelledListener {

    private Button btn;
    private String imagePath;
    private List<String> imagePathList;
    String action;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_profile);
        //TODO
        //todo

        findViewById(R.id.profile_add_contact).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //singleSelectionPicker.show(getSupportFragmentManager(), "picker");
                multiSelectionPicker.show(getSupportFragmentManager(), "picker");
            }
        });
    }

    BSImagePicker singleSelectionPicker = new BSImagePicker.Builder(BuildConfig.APPLICATION_ID+".provider")
            .setMaximumDisplayingImages(24) //Default: Integer.MAX_VALUE. Don't worry about performance :)
            .setSpanCount(3) //Default: 3. This is the number of columns
            .setGridSpacing(Utils.dp2px(2)) //Default: 2dp. Remember to pass in a value in pixel.
            .setPeekHeight(Utils.dp2px(360)) //Default: 360dp. This is the initial height of the dialog.
            .hideCameraTile() //Default: show. Set this if you don't want user to take photo.
            .hideGalleryTile() //Default: show. Set this if you don't want to further let user select from a gallery app. In such case, I suggest you to set maximum displaying images to Integer.MAX_VALUE.
            .setTag("A request ID") //Default: null. Set this if you need to identify which picker is calling back your fragment / activity.
            .useFrontCamera() //Default: false. Launching camera by intent has no reliable way to open front camera so this does not always work.
            .build();
    BSImagePicker multiSelectionPicker = new BSImagePicker.Builder(BuildConfig.APPLICATION_ID+".provider")
            .isMultiSelect() //Set this if you want to use multi selection mode.
            .setMinimumMultiSelectCount(3) //Default: 1.
            .setMaximumMultiSelectCount(6) //Default: Integer.MAX_VALUE (i.e. User can select as many images as he/she wants)
            .setMultiSelectBarBgColor(android.R.color.white) //Default: #FFFFFF. You can also set it to a translucent color.
            .setMultiSelectTextColor(R.color.primary_text) //Default: #212121(Dark grey). This is the message in the multi-select bottom bar.
            .setMultiSelectDoneTextColor(R.color.colorAccent) //Default: #388e3c(Green). This is the color of the "Done" TextView.
            .setOverSelectTextColor(R.color.error_text) //Default: #b71c1c. This is the color of the message shown when user tries to select more than maximum select count.
            .disableOverSelectionMessage() //You can also decide not to show this over select message.
            .build();


    @Override
    public void onSingleImageSelected(Uri uri, String tag) {
        //Do something with your Uri
    }

    @Override
    public void onMultiImageSelected(List<Uri> uriList, String tag) {
        //Do something with your Uri list
        for (Uri  u : uriList){
        }
    }


    //Optional
    @Override
    public void onCancelled(boolean isMultiSelecting, String tag) {
        //Do whatever you want when user cancelled
    }

    @Override
    public void loadImage(Uri imageUri, ImageView ivImage) {
        Glide.with(ShowProfile.this).load(imageUri).into(ivImage);
    }
}