package com.example.android.bibdiscovery.utils;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Typeface;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.bibdiscovery.R;

/**
 * Created by lottejespers.
 */
public class ViewDialog {

    public void showDialog(Activity activity, String msg, int color) {
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog);

        TextView text = dialog.findViewById(R.id.text_dialog);
        text.setTypeface(Typeface.createFromAsset(activity.getAssets(), "fonts/Oswald-Regular.ttf"));

        text.setText(msg);

        ImageView image = dialog.findViewById(R.id.image);
        image.setBackgroundColor(color);

        Button dialogButton = dialog.findViewById(R.id.btn_dialog);
        dialogButton.setBackgroundColor(color);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();

    }
}
