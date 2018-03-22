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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lalala.fang.neutvshow.R;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by fang on 2018/3/22.
 */

@SuppressLint("ValidFragment")
public class FavoriteLiveFragment extends Fragment {

    public FavoriteLiveFragment() {
    }

    private RecyclerView recyclerView;
    private List<Live> liveList = new ArrayList<>();
    private AdapterLive adapter;
    private LocalBroadcastManager localBroadcastManager;
    private FavoriteReceiver receiver;
    private IntentFilter intentFilter;


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_all, container, false);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        liveList = DataSupport.where("isFavorite = ?", "1").find(Live.class);
        deleteSame();
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
        localBroadcastManager = LocalBroadcastManager.getInstance(getContext());
        intentFilter = new IntentFilter();
        intentFilter.addAction("com.lalala.fangs.neutv.LIVE_FAVORITE_CHANGE");
        receiver = new FavoriteReceiver();
        localBroadcastManager.registerReceiver(receiver, intentFilter);
        return rootView;
    }

    private void addToFavorite(int position) {
        Live t = liveList.get(position);
        ContentValues values = new ContentValues();
        Intent intent = new Intent("com.lalala.fangs.neutv.LIVE_FAVORITE_CHANGE");

        if (t.getIsFavorite()) {
            t.setIsFavorite(false);
            values.put("isFavorite", "0");
        } else {
            //对于收藏夹，这个程序块不会被运行
            t.setIsFavorite(true);
            values.put("isFavorite", "1");
            adapter.update(position);
        }
        //更新数据库
        DataSupport.updateAll(Live.class, values, "name = ?", t.getName());

        //发送给分类
        intent.putExtra("live_name", t.getName());
        intent.putExtra("live_favorite", t.getIsFavorite());
        localBroadcastManager.sendBroadcast(intent);
    }

    class FavoriteReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getBooleanExtra("live_favorite", true)) {
                liveList = DataSupport.where("isFavorite = ?", "1").find(Live.class);
                deleteSame();
                adapter.updateAll(liveList);
            } else {
                String name = intent.getStringExtra("live_name");
                if (name != null) {
                    Live temp = DataSupport.where("name = ?", name).findFirst(Live.class);
                    if (temp != null) {
                        int position = liveList.indexOf(temp);
                        adapter.remove(position);
                    }
                }
            }
        }
    }

    private static final String TAG = "FavoriteLiveFragment";

    private void deleteSame() {
        HashSet<Live> lives = new HashSet<>(liveList);
        liveList = new ArrayList<>(lives);
    }

}