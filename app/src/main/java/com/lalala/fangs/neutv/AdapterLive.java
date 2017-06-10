package com.lalala.fangs.neutv;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.lalala.fang.neutvshow.R;

import java.util.HashMap;
import java.util.List;


/**
 * Created by FANGs on 2017/1/25.
 */




public class AdapterLive extends RecyclerView.Adapter<AdapterLive.ViewHolder> {


    private List<Live> mLiveList;
    private OnItemClickListener listener;
    private Context context;
    private HashMap<String,String> map;


    public interface OnItemClickListener {
        void onItemClick(View view, int position);
        void onItemLongClick(View view, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }


    static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnLongClickListener {
        View bookView;
        TextView liveName;
        ImageView img,favoriteImg;
        private OnItemClickListener listener;

        public ViewHolder(View itemView,OnItemClickListener l) {
            super(itemView);
            listener = l;
            bookView = itemView;
            liveName = (TextView) itemView.findViewById(R.id.livename);
            img = (ImageView) itemView.findViewById(R.id.imageView);
            favoriteImg = (ImageView) itemView.findViewById(R.id.favorite);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (listener != null) {
                listener.onItemClick(v, getAdapterPosition());
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if(listener != null){
               listener.onItemLongClick(v,getAdapterPosition());
                return true;
            }
            return false;
        }
    }

    public void update(int position){
        if(position < mLiveList.size() && position>=0){
            notifyItemChanged(position);
        }
    }


    AdapterLive(List<Live> liveList) {
        mLiveList = liveList;
    }


    private static final String TAG = "AdapterBookList";
    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.live_item, parent, false);
        final ViewHolder holder = new ViewHolder(view, listener);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Live live = mLiveList.get(position);
        holder.liveName.setText(live.getName());
        Log.e(TAG, "onBindViewHolder: "+live.getName() );
        if(live.getIsFavorite()){
            holder.favoriteImg.setBackgroundColor(Color.parseColor("#FF0000"));
        }else{
            holder.favoriteImg.setBackgroundColor(Color.parseColor("#000000"));
        }

//        "http://media2.neu6.edu.cn/hls/cctv1hd.m3u8#http://media2.neu6.edu.cn/hls/jlu_cctv1hd.m3u8"

        String[] name = live.getUrllist().split("/");
        int q = name.length;
        int len = name[q-1].length();
        String ans = name[q-1].substring(0,len-5);

        long time=System.currentTimeMillis();
            Glide.with(context)
                    .load("http://hdtv.neu6.edu.cn/wall/img/"+ans+"_s.png?time="+String.valueOf((time-5000)/60000))
//                    .skipMemoryCache(true)
//                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .placeholder(R.color.cardview_dark_background)
                    .error(R.color.colorPrimary)
                    .into(holder.img);
//        }
    }

    @Override
    public int getItemCount() {
        return mLiveList.size();
    }
}
