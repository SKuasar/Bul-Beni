package com.example.deneme4;

import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class Takip {
    private String takipEden,takipEdilen,key;

    public Takip(){

    }

    public Takip(String takipEden, String takipEdilen) {
        this.takipEden = takipEden;
        this.takipEdilen = takipEdilen;
    }

    public String getTakipEden() {
        return takipEden;
    }

    public void setTakipEden(String takipEden) {
        this.takipEden = takipEden;
    }

    public String getTakipEdilen() {
        return takipEdilen;
    }

    public void setTakipEdilen(String takipEdilen) {
        this.takipEdilen = takipEdilen;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

}
