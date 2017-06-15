package com.lalala.fangs.neutv;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lalala.fang.neutvshow.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by FANGs on 2017/6/14.
 */

public class AdapterList extends RecyclerView.Adapter<AdapterList.ViewHolder> {


    private ArrayList<ArrayList<String>> mList;
    private OnItemClickListener listener;
    private Context context;


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
        private OnItemClickListener listener;

        public ViewHolder(View itemView,OnItemClickListener l) {
            super(itemView);
            listener = l;
            bookView = itemView;
            liveName = (TextView) itemView.findViewById(R.id.text_name);
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


    AdapterList(ArrayList<ArrayList<String>> List) {
        mList = List;
    }


    private static final String TAG = "AdapterBookList";
    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tv_name_list_item, parent, false);
        final ViewHolder holder = new ViewHolder(view, listener);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        ArrayList<String> show = mList.get(position);
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        String time = format.format(new Date(Long.valueOf(show.get(0))*1000L));
        holder.liveName.setText(time+" "+show.get(2));
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }
}
