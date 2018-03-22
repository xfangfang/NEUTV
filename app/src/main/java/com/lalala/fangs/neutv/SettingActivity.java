package com.lalala.fangs.neutv;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.lalala.fang.neutvshow.R;

public class SettingActivity extends AppCompatActivity {

    private RelativeLayout settingActivityChooseList;
    private RelativeLayout settingActivityHistory;
    private RelativeLayout settingActivityQqGroup;
    private RelativeLayout settingActivityGithub;
    private RelativeLayout settingActivityUpdate;
    private RelativeLayout settingActivityThirdParty;

    public void initViews() {
        settingActivityHistory = (RelativeLayout) findViewById(R.id.setting_activity_history);
        settingActivityChooseList = (RelativeLayout) findViewById(R.id.setting_activity_choose_list);
        settingActivityQqGroup = (RelativeLayout) findViewById(R.id.setting_activity_qq_group);
        settingActivityGithub = (RelativeLayout) findViewById(R.id.setting_activity_github);
        settingActivityUpdate = (RelativeLayout) findViewById(R.id.setting_activity_update);
        settingActivityThirdParty = (RelativeLayout) findViewById(R.id.setting_activity_third_party);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        initViews();
        initClicks();
    }

    private void initClicks(){
        settingActivityHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        settingActivityChooseList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SourceDialog dialog = new SourceDialog.Builder(SettingActivity.this).create();
                dialog.show();
            }
        });
        settingActivityQqGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                utils.joinQQGroup("OVbiu9aw_bqHtOgXM_fb17lOW0LpzKeA",SettingActivity.this);
            }
        });
        settingActivityGithub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        settingActivityUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        settingActivityThirdParty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }


}
