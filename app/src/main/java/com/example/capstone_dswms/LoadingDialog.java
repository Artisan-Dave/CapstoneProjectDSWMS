package com.example.capstone_dswms;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

public class LoadingDialog {
    Context context;
    Dialog dialog;

    public LoadingDialog(Context context){
        this.context = context;
    }

    public void ShowDialog(){
        dialog = new Dialog(context);
        dialog.setContentView(R.layout.custom_dialog_loading);
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        dialog.create();
        dialog.show();
    }
    public void HideDialog(){
        dialog.dismiss();
    }
}
