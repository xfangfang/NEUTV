package com.lalala.fangs.neutv;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lalala.fang.neutvshow.R;

import org.litepal.crud.DataSupport;

import java.util.List;

/**
 * Created by fang on 2018/3/22.
 */

@SuppressLint("ValidFragment")
public class AllLiveFragment extends Fragment {

    public AllLiveFragment() {
    }

    private RecyclerView recyclerView;

    private List<Live> liveList;

    private AdapterLive adapter;
    private LocalBroadcastManager localBroadcastManager;
    private AllLiveFragment.FavoriteReceiver receiver;
    private IntentFilter intentFilter;

    public AllLiveFragment(String id) {
        this.liveList = DataSupport.where("itemid = ?", id).find(Live.class);
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_all, container, false);
        init(rootView);

        adapter = new AdapterLive(liveList);
        StaggeredGridLayoutManager sm = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(sm);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new AdapterLive.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(getContext(), VideoActivity.class);
                intent.putExtra("live", liveList.get(position));
                startActivity(intent);
            }

            @Override
            public void onItemLongClick(View view, int position) {
                addToFavorite(position);
            }
        });

        return rootView;
    }

    private void init(View rootView) {
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);

        localBroadcastManager = LocalBroadcastManager.getInstance(getContext());
        intentFilter = new IntentFilter();
        intentFilter.addAction("com.lalala.fangs.neutv.LIVE_FAVORITE_CHANGE");
        receiver = new AllLiveFragment.FavoriteReceiver();
        localBroadcastManager.registerReceiver(receiver, intentFilter);
    }

    @Override
    public void onDestroy() {
        localBroadcastManager.unregisterReceiver(receiver);
        Log.e(TAG, "onDestroy: 销毁频道列表");
        super.onDestroy();
    }

    private static final String TAG = "FindBooksFragment";

    private void addToFavorite(int position) {
        Live t = liveList.get(position);
        ContentValues values = new ContentValues();
        Intent intent = new Intent("com.lalala.fangs.neutv.LIVE_FAVORITE_CHANGE");

        if (t.getIsFavorite()) {
            t.setIsFavorite(false);
            values.put("isFavorite", "0");
        } else {
            t.setIsFavorite(true);
            values.put("isFavorite", "1");
        }
        //更新数据库
        DataSupport.updateAll(Live.class, values, "name = ?", t.getName());
        intent.putExtra("live_name", t.getName());
        intent.putExtra("live_favorite", t.getIsFavorite());
        localBroadcastManager.sendBroadcast(intent);
        adapter.update(position);
    }

    class FavoriteReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String name = intent.getStringExtra("live_name");
            boolean isfavorite = intent.getBooleanExtra("live_favorite", false);
            if (name != null) {
                Live temp = DataSupport.where("name = ?", name).findFirst(Live.class);
                if (temp != null) {
                    int count = liveList.indexOf(temp);
                    if (count != -1) {
                        liveList.set(count, temp);
                        adapter.update(count);
                    }
                }
            }
        }
    }


}