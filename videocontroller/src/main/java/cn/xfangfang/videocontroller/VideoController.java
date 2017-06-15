package cn.xfangfang.videocontroller;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import java.text.DecimalFormat;

/**
 * TODO: document your custom view class.
 */
public class VideoController extends FrameLayout{

    public VideoController(Context context) {
        super(context);
        init(context,null, 0);
    }

    public VideoController(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context,attrs, 0);
    }

    public VideoController(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context,attrs, defStyle);
    }

    private static final String TAG = "VideoController";

    ImageButton ib_control,ib_menu,ib_list,ib_list_before,ib_back,ib_love;
    TextView txv_start_time,txv_end_time,txv_name,txv_centerTime;
    SeekBar seekBar;
    ProgressBar progressBar;
    RelativeLayout contentLayout;
    FrameLayout baseLayout;
    VideoView videoView;

    private boolean isLive = false;
    private int autoGoneTime;
    private Handler handler_autoGone = new Handler();
    private Handler handler_seekBar = new Handler();//每个一秒取一次当前播放时间

    private void init(final Context context, AttributeSet attrs, int defStyle) {

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.controller_layout, this);
        ib_control = (ImageButton) view.findViewById(R.id.btn_control);
        ib_back = (ImageButton) view.findViewById(R.id.btn_back);
        ib_list = (ImageButton) view.findViewById(R.id.btn_list);
        ib_list_before = (ImageButton) view.findViewById(R.id.btn_before_list);
        ib_menu = (ImageButton) view.findViewById(R.id.btn_menu);
        ib_love = (ImageButton) view.findViewById(R.id.btn_love);

        txv_name = (TextView) view.findViewById(R.id.txv_name);
        txv_start_time = (TextView) view.findViewById(R.id.txv_start_time);
        txv_end_time = (TextView) view.findViewById(R.id.txv_end_time);
        txv_centerTime = (TextView) view.findViewById(R.id.txv_center_time);

        seekBar = (SeekBar) view.findViewById(R.id.seekBar);
        progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);

        contentLayout = (RelativeLayout) view.findViewById(R.id.content);
        baseLayout = (FrameLayout) view.findViewById(R.id.base_layout);


        listenerEvent();


        contentVisible();
        handler_seekBar.post(run_time);

    }

    @Override
    public boolean isShown() {
        return contentLayout.isShown();
    }


    public void contentInvisible(){
        if(stateListener != null){
            stateListener.onInvisible();
        }
        contentLayout.setVisibility(INVISIBLE);
        handler_autoGone.removeCallbacks(autoGone);
    }

    public void contentVisible(){
        if(stateListener != null){
            stateListener.onVisible();
        }
        contentLayout.setVisibility(VISIBLE);
        reSetAutoGoneTime();
        handler_autoGone.post(autoGone);
    }

    private void reSetAutoGoneTime(){
        autoGoneTime = 0;
    }

    private Runnable autoGone = new Runnable() {
        public void run() {
            if(!videoView.isPlaying() || progressBar.isShown()){
                reSetAutoGoneTime();
            }
            if (autoGoneTime++ == 4) {
                contentInvisible();
            }
            handler_autoGone.postDelayed(autoGone, 1000);
        }
    };

    private Runnable run_time = new Runnable() {
        public void run() {
            long a = videoView.getCurrentPosition();
            if(!isLive) {
                txv_start_time.setText(intToString((int) a / 1000));
            }
            seekBar.setProgress((int) a / 1000);
            handler_seekBar.postDelayed(run_time, 1000);
        }
    };

    public void setIsLiveOrNot(boolean isLive){
        this.isLive = isLive;
        if(isLive){
            seekBar.setVisibility(INVISIBLE);
            txv_end_time.setVisibility(INVISIBLE);
            txv_start_time.setText("直播");
        }else{
            seekBar.setVisibility(VISIBLE);
            txv_end_time.setVisibility(VISIBLE);
            txv_start_time.setText("");
        }
    }

    public void setTitle(String title){
        txv_name.setText(title);
    }

    public void setVideoView(final VideoView videoView){
        this.videoView = videoView;

        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                if(stateListener != null){
                    stateListener.onError();
                }
                return false;
            }
        });

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                Log.e(TAG, "onPrepared: 准备好啦" );
                long a = videoView.getDuration();
                reSetAutoGoneTime();
                progressBar.setVisibility(INVISIBLE);
                seekBar.setMax((int) a / 1000);
                txv_end_time.setText(intToString((int) a / 1000));
                if(stateListener != null){
                    stateListener.onPrepared();
                }
            }
        });


        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int a = 0;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    txv_start_time.setText(intToString(progress));
                    if (progress > a)
                        txv_centerTime.setText("+ " + intToString(progress - a));
                    else
                        txv_centerTime.setText("- " + intToString(a - progress));
                }else{
                    if(progressBar.isShown()) {
                        progressBar.setVisibility(INVISIBLE);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                reSetAutoGoneTime();
                progressBar.setVisibility(INVISIBLE);
                a = seekBar.getProgress();
                txv_centerTime.setVisibility(VISIBLE);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                txv_centerTime.setVisibility(INVISIBLE);
                progressBar.setVisibility(VISIBLE);
                videoView.pause();
                videoView.seekTo(seekBar.getProgress() * 1000);
                videoView.start();
            }
        });

    }

    private void listenerEvent() {
        baseLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(contentLayout.isShown()) {
                    contentInvisible();
                }else{
                    contentVisible();
                }
            }
        });

        ib_back.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(clickEventListener != null){
                    clickEventListener.onBack();
                }
            }
        });

        ib_control.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(videoView != null){
                    if(videoView.isPlaying()){
                        videoView.pause();
                        ib_control.setImageResource(R.drawable.ic_action_start);
                        if(stateListener != null){
                            stateListener.onPause();
                        }
                        reSetAutoGoneTime();
                    }else{
                        videoView.start();
                        ib_control.setImageResource(R.drawable.ic_action_pause);
                        if(stateListener != null){
                            stateListener.onStart();
                        }
                        reSetAutoGoneTime();
                    }
                }
            }
        });

        ib_list.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(clickEventListener != null){
                    contentInvisible();
                    clickEventListener.onList();
                }
            }
        });

        ib_list_before.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(clickEventListener != null){
                    contentInvisible();
                    clickEventListener.onBeforeList();
                }
            }
        });

        ib_love.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(clickEventListener != null){
                    clickEventListener.onFavorite();
                    reSetAutoGoneTime();
                }
            }
        });

        ib_menu.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(clickEventListener != null){
                    clickEventListener.onMenu();
                    contentInvisible();
                    reSetAutoGoneTime();
                }
            }
        });
    }

    public void setFavorite(boolean isFavorite){
        if(isFavorite){
            toFavorite();
        }else{
            toHate();
        }
    }

    public void pause(){
        if(videoView != null){
            if(videoView.isPlaying()){
                videoView.pause();
                ib_control.setImageResource(R.drawable.ic_action_start);
                if(stateListener != null){
                    stateListener.onPause();
                }
                reSetAutoGoneTime();
            }
        }
    }

    public void start(){
        if(videoView != null) {
            if(!videoView.isPlaying()){
                progressBar.setVisibility(VISIBLE);
                videoView.start();
                ib_control.setImageResource(R.drawable.ic_action_pause);
                if (stateListener != null) {
                    stateListener.onStart();
                }
                reSetAutoGoneTime();
            }
        }
    }

    public void ProgressBarVisible(){
        progressBar.setVisibility(VISIBLE);
    }

    public void ProgressBarInvisible(){
        progressBar.setVisibility(INVISIBLE);
    }

    private void toFavorite(){
        ib_love.setImageResource(R.drawable.ic_action_favorite);
    }

    private void toHate(){
        ib_love.setImageResource(R.drawable.ic_action_favorite_border);

    }

    private  String intToString(int t) {
        String a;
        int sec = t % 60;
        t /= 60;
        int min = t % 60;
        int hour = t / 60;
        if (hour == 0)
            a = new DecimalFormat("00").format(min) + ":" + new DecimalFormat("00").format(sec);
        else
            a = new DecimalFormat("00").format(hour) + ":" + new DecimalFormat("00").format(min) + ":" + new DecimalFormat("00").format(sec);
        return a;
    }

    public interface OnClickEventListener{
        void onBack();
        void onFavorite();
        void onMenu();
        void onList();
        void onBeforeList();
    }

    public interface OnStateListener{
        void onError();
        void onPause();
        void onStart();
        void onPrepared();
        void onVisible();
        void onInvisible();
    }


    private OnStateListener stateListener;
    private OnClickEventListener clickEventListener;

    public void setOnStateListener(OnStateListener listener){
        this.stateListener = listener;
    }
    public void setOnClickEventListener(OnClickEventListener listener){
        this.clickEventListener = listener;
    }


}
