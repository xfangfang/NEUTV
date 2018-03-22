package cn.xfangfang.videocontroller;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import java.text.DecimalFormat;

import static android.media.AudioManager.FLAG_SHOW_UI;
import static android.media.AudioManager.STREAM_MUSIC;
import static android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL;

/**
 * TODO: document your custom view class.
 */
public class VideoController extends RelativeLayout{

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
    RelativeLayout baseLayout;
    VideoView videoView;
    private Context context;
    private Activity activity;

    private boolean isLive = false;
    private int autoGoneTime;
    private Handler handler_autoGone = new Handler();
    private Handler handler_seekBar = new Handler();//每个一秒取一次当前播放时间

    private void init(final Context context, AttributeSet attrs, int defStyle) {
        this.context = context;

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
        baseLayout = (RelativeLayout) view.findViewById(R.id.base_layout);


        listenerEvent();


        contentVisible();
        handler_seekBar.post(run_time);

        setScreenMode(SCREEN_BRIGHTNESS_MODE_MANUAL);
        liangdu =  Settings.System.getInt(context.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS, 8);
        liangdu /= LEVEL;
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
            int a = videoView.getCurrentPosition();
            if(!isLive) {
                txv_start_time.setText(intToString(a / 1000));
                if(a != 0) {
                    if(timeListener != null){
                        timeListener.onTimeChange(a);
                    }
                }
            }
            seekBar.setProgress(a / 1000);
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
                progressBar.setVisibility(INVISIBLE);
                long a = videoView.getDuration();
                reSetAutoGoneTime();
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
                    autoGoneTime = 0;
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
                ProgressBarInvisible();
                a = seekBar.getProgress();
                txv_centerTime.setVisibility(VISIBLE);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                txv_centerTime.setVisibility(INVISIBLE);
                ProgressBarVisible();
                pause();
                videoView.seekTo(seekBar.getProgress() * 1000);
                resume();
            }
        });
    }

    float startX,startY;
    int liangdu = 1;
    final int MAX = 19;
    final int LEVEL = 15;
    float addition=0;
    float volumeAddition = 0;
    float volume = 1;
    float volumeMax;


    private void setScreenMode(int paramInt) {
        try {
            Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, paramInt);
        } catch (Exception localException) {
            localException.printStackTrace();
        }
    }

    private void setVolume(float param){
        AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int f;
        if(param < 4){
            f = (int) (param / 16 / 2 * volumeMax);
        }else {
            param-=2;
            f = (int) (param / 16  * volumeMax);
        }
        mAudioManager.setStreamVolume(STREAM_MUSIC,f,FLAG_SHOW_UI);
    }

    private void setScreenBrightness(int paramInt) {
        if(activity == null) return;
        Window localWindow = activity.getWindow();
        WindowManager.LayoutParams localLayoutParams = localWindow.getAttributes();
        double f = (paramInt*1.0) / (LEVEL*1.0);
        localLayoutParams.screenBrightness = (float) f;
        localWindow.setAttributes(localLayoutParams);
    }

    public void setActivity(Activity a){
        activity = a;
    }

    private void getVolume(){
        AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        volumeMax = mAudioManager.getStreamMaxVolume( STREAM_MUSIC );
        volume = mAudioManager.getStreamVolume( STREAM_MUSIC );
        volume = volume / volumeMax * 16;
        if(volume < 2){
            volume = (int) (2*volume);
        }else{
            volume = (int) volume + 2;
        }
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

        baseLayout.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int screenWidth;
                int screenHeight;
                int maxCap = dp2px(10);
                if(videoView!=null) {
                    screenWidth = videoView.getWidth();
                    screenHeight = videoView.getHeight();
                }else return false;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getX();
                        startY = event.getY();
                        addition = 0;
                        volumeAddition = 0;
                        getVolume();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float endY = event.getY();
                        float distanceY = startY - endY;
                        if (startX > screenWidth / 2) {
                            //右侧 音量
                            if (Math.abs(distanceY) > maxCap) {
                                volumeAddition = ((distanceY/screenHeight)*MAX);
                                if(volume + volumeAddition >= MAX-1) {
                                    volumeAddition = MAX - 1 - volume;
                                }
                                else if(volume + volumeAddition < 0){
                                    volumeAddition = -volume;
                                }
                                int text = (int)volume+(int)volumeAddition;
//                                if(text < 4 )
//                                    textValume.setText(String.valueOf(text*0.25));
//                                else
//                                    textValume.setText(String.valueOf(text-3));

                                setVolume(volume+volumeAddition);
                            }
                        } else {
                            //左侧 亮度
                            if (Math.abs(distanceY) > maxCap) {
                                addition = ((distanceY/screenHeight)*LEVEL);
                                if(liangdu + addition >= LEVEL) addition = LEVEL-liangdu;
                                else if(liangdu + addition < 1) addition = -liangdu + 1;
                                setScreenBrightness(liangdu+(int)addition);
                            }
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        if(Math.abs(startX - event.getX()) < maxCap && Math.abs(startY - event.getY()) < maxCap){
                            return false;
                        }else{
                            liangdu += (int) addition;
                            volume += (int) volumeAddition;
                            return true;
                        }
                }
                return false;
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
                        pause();
                        if(stateListener != null){
                            stateListener.onPause();
                        }
                        reSetAutoGoneTime();
                    }else{
                        resume();
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

    public void resume(){
        if(videoView != null) {
            if(!videoView.isPlaying()){
                videoView.start();
                ib_control.setImageResource(R.drawable.ic_action_pause);
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

    public interface OnTimeListener{
        void onTimeChange(int currentTime);
    }

    private OnStateListener stateListener;
    private OnClickEventListener clickEventListener;
    private OnTimeListener timeListener;

    public void setOnStateListener(OnStateListener listener){
        this.stateListener = listener;
    }
    public void setOnClickEventListener(OnClickEventListener listener){
        this.clickEventListener = listener;
    }
    public void setOnTimeListener(OnTimeListener listener){
        this.timeListener = listener;
    }

    private int dp2px(float dp) {
        float scale = getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public void setBeforeBtnClickable(boolean isClickable){
        this.ib_list_before.setClickable(isClickable);

        if(isClickable){
            this.ib_list_before.setVisibility(VISIBLE);
        }else{
            this.ib_list_before.setVisibility(INVISIBLE);
        }
    }

}
