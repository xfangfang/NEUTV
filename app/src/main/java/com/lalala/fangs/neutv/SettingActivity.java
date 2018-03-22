package com.lalala.fangs.neutv;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.lalala.fang.neutvshow.R;

public class SettingActivity extends AppCompatActivity {

    private RelativeLayout settingActivityChooseList;


    public void initViews() {
        settingActivityChooseList = (RelativeLayout) findViewById(R.id.setting_activity_choose_list);

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        initViews();
        initClicks();
    }

    private void initClicks(){
        settingActivityChooseList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SourceDialog dialog = new SourceDialog.Builder(SettingActivity.this).create();
                dialog.show();
            }
        });

    }


}
