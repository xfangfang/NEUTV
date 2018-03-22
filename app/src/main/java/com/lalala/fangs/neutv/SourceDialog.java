package com.lalala.fangs.neutv;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.lalala.fang.neutvshow.R;

import java.util.ArrayList;

/**
 * Created by fang on 2018/3/22.
 */

public class SourceDialog extends Dialog {
    public SourceDialog(Context context) {
        super(context);
    }

    public static class Builder {
        private Context context;
        private DialogInterface.OnClickListener listener;
        private ImageView sourceDialogAddbuton;


        public Builder(Context context) {
            this.context = context;
        }


        public SourceDialog create() {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final SourceDialog dialog = new SourceDialog(context);
            View layout = inflater.inflate(R.layout.source_dialog, null);
            sourceDialogAddbuton = (ImageView) layout.findViewById(R.id.source_dialog_addbuton);
            dialog.setContentView(layout);
            return dialog;
        }

    }

}
