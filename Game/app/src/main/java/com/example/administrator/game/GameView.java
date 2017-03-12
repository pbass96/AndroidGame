package com.example.administrator.game;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;

import java.util.ArrayList;


/**
 * Created by Administrator on 2017-03-11.
 */

public class GameView extends TextureView implements
        TextureView.SurfaceTextureListener, View.OnTouchListener {

    volatile private boolean mIsRunnable;

    volatile private float mTouchedX;
    volatile private float mTouchedY;

    private Thread mThread;

    private ArrayList<DrawableItem> mItemList;

    //
    private  ArrayList<Block> mBlockList;

    // 패드 선언
    private Pad mPad;
    private  float mPadHalfWidth;

    // 볼처리 선언
    private Ball mBall;
    private float mBallRadius;

    //충돌 처리
    private float mBlockWidth;
    private float mBlockHeight;

    // 블록갯수 설정
    static final int BLOCK_COUNT =100;

    // 라이프
    private  int mLife;

    // 게임시작 시간
    private long mGameStartTime;

    // 핸들러 추가
    private Handler mHandler;

    // 화면변경시 저장한 정보를 모음
    private static final String KEY_LIFE = "life";
    private static final String KEY_GAME_START_TIME = "game_start_time";
    private static final String KEY_BALL = "ball";
    private static final String KEY_BLOCK = "block";

    private final Bundle mSavedInstanceState;



    //public GameView(Context context) {
    public GameView(final Context context, Bundle savedInstanceState) {
        super(context);
        setSurfaceTextureListener(this);
        setOnTouchListener(this);
        mSavedInstanceState = savedInstanceState;


        mHandler = new Handler() {

            @Override
            public void handleMessage(Message message) {
                // 실행할 처리
                Intent intent = new Intent(context, ClearActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent.putExtras(message.getData());

                context.startActivity(intent);


            }

        };



    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        readyObjects(width, height);

    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        readyObjects(width, height);

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        synchronized (this) {
            return true;
        }
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    public void start() {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        final boolean isEnableSound = sharedPreferences.getBoolean("enable_sound", true);
        final boolean isEnableVibrator = sharedPreferences.getBoolean("enable_vibrate", true);

        mThread = new Thread(new Runnable() {
            @Override
            public void run() {


                Paint paint = new Paint();
                //paint.setColor(Color.RED);
                //paint.setStyle(Paint.Style.FILL);

                ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, ToneGenerator.MAX_VOLUME);

                int collisionTime = 0;
                int soundIndex = 0;

                Vibrator vibrator= (Vibrator)getContext().getSystemService(Context.VIBRATOR_SERVICE);


                //while(mIsRunnable) {
                while(true) {

                    long startTime = System.currentTimeMillis();

                    synchronized (GameView.this) {

                        if(!mIsRunnable) {
                            break;
                        }

                        Canvas canvas = lockCanvas();

                        if (canvas == null) {
                            continue;
                        }
                        canvas.drawColor(Color.BLACK);

                        float padLeft = mTouchedX - mPadHalfWidth;
                        float padRight = mTouchedX + mPadHalfWidth;

                        mPad.setLeftRight(padLeft, padRight);
                        mBall.move();

                        float ballTop = mBall.getY() - mBallRadius;
                        float ballLeft = mBall.getX() - mBallRadius;
                        float ballBottom = mBall.getY() + mBallRadius;
                        float ballRight = mBall.getX() + mBallRadius;

                        if(ballLeft < 0 && mBall.getSpeedX() < 0 || ballRight >= getWidth() && mBall.getSpeedX() > 0 ) {
                            mBall.setSpeedX(-mBall.getSpeedX());
                            if(isEnableSound) {
                                toneGenerator.startTone(ToneGenerator.TONE_DTMF_0, 10);
                            }
                            if(isEnableVibrator) {
                                vibrator.vibrate(50);
                            }
                        }
                        /*

                        if(ballTop < 0 || ballBottom > getHeight()) {
                            mBall.setSpeedY(-mBall.getSpeedY());
                        }
                        */

                        if(ballTop < 0 ) {
                            mBall.setSpeedY(-mBall.getSpeedY());
                            if(isEnableSound) {
                                toneGenerator.startTone(ToneGenerator.TONE_DTMF_0, 10);
                            }
                            if(isEnableVibrator) {
                                vibrator.vibrate(50);
                            }

                        }
                        if(ballTop > getHeight()) {
                            if(mLife > 0) {
                                mLife--;
                                mBall.reset();
                            }
                            else {
                                if(isEnableSound) {
                                    toneGenerator.startTone(ToneGenerator.TONE_DTMF_0, 10);
                                }
                                if(isEnableVibrator) {
                                    vibrator.vibrate(50);
                                }

                                unlockCanvasAndPost(canvas);
                                Message message =  Message.obtain();

                                Bundle bundle = new Bundle();
                                bundle.putBoolean(ClearActivity.EXTRA_IS_CLEAR, false);
                                bundle.putInt(ClearActivity.EXTRA_BLOCK_COUNT, getBlockCount());
                                bundle.putLong(ClearActivity.EXTRA_TIME, System.currentTimeMillis() - mGameStartTime);

                                message.setData(bundle);
                                mHandler.sendMessage(message);

                                return;

                            }
                        }

                        Block leftBlock = getBlock(ballLeft, mBall.getY());
                        Block topBlock = getBlock(mBall.getX(), ballTop);
                        Block rightBlock = getBlock(ballRight, mBall.getY());
                        Block bottomBlock = getBlock(mBall.getX(), ballBottom);

                        boolean isCollision = false;

                        if(leftBlock != null) {
                            mBall.setSpeedX(-mBall.getSpeedX());
                            leftBlock.collision();
                            isCollision = true;
                        }
                        if(topBlock != null) {
                            mBall.setSpeedY(-mBall.getSpeedY());
                            topBlock.collision();
                            isCollision = true;
                        }
                        if(rightBlock != null) {
                            mBall.setSpeedX(-mBall.getSpeedX());
                            rightBlock.collision();
                            isCollision = true;
                        }
                        if(bottomBlock != null) {
                            mBall.setSpeedY(-mBall.getSpeedY());
                            bottomBlock.collision();
                            isCollision = true;
                        }

                        if(isCollision) {
                            if(collisionTime > 0 ) {
                                if(soundIndex < 15) {
                                    soundIndex++;
                                }
                            } else {
                                soundIndex = 1;
                            }
                            collisionTime=10;
                            if(isEnableSound) {
                                toneGenerator.startTone(ToneGenerator.TONE_DTMF_0, 10);
                            }
                            if(isEnableVibrator) {
                                vibrator.vibrate(50);
                            }

                        } else if(collisionTime > 0) {
                            collisionTime--;
                        }

                        float padTop = mPad.getTop();
                        float ballSpeedY = mBall.getSpeedY();

                        if(ballBottom > padTop && ballBottom - ballSpeedY <  padTop && padLeft < ballRight && padRight > ballLeft) {
                            if(isEnableSound) {
                                toneGenerator.startTone(ToneGenerator.TONE_DTMF_0, 10);
                            }
                            if(isEnableVibrator) {
                                vibrator.vibrate(50);
                            }
                            if(ballSpeedY < mBlockHeight / 3) {
                                ballSpeedY *= -1.05f;
                            }
                            else {
                                ballSpeedY = - ballSpeedY;
                            }

                            float ballSpeedX = mBall.getSpeedX() + (mBall.getX() - mTouchedX) /10;
                            if(ballSpeedX > mBlockWidth / 5) {
                                ballSpeedX = mBlockWidth / 5;
                            }
                            mBall.setSpeedY(ballSpeedY);
                            mBall.setSpeedX(ballSpeedX);
                        }



                        //mPad.draw(canvas, paint);

                        for (DrawableItem item : mItemList) {
                            item.draw(canvas, paint);
                        }
                        unlockCanvasAndPost(canvas);

                        if(isCollision && getBlockCount() == 0) {
                            Message message = Message.obtain();
                            Bundle bundle = new Bundle();
                            bundle.putBoolean(ClearActivity.EXTRA_IS_CLEAR, true);
                            bundle.putInt(ClearActivity.EXTRA_BLOCK_COUNT, 0);
                            bundle.putLong(ClearActivity.EXTRA_TIME, System.currentTimeMillis() - mGameStartTime);

                            message.setData(bundle);
                            mHandler.sendMessage(message);
                        }
                    }

                    long sleepTime = 16 - System.currentTimeMillis() + startTime;
                    if(sleepTime > 0 ) {
                        try {
                            Thread.sleep(sleepTime);
                        }
                        catch (InterruptedException e) {

                        }
                    }



                }
                toneGenerator.release();
            }
        });
        mIsRunnable = true;
        mThread.start();
    }

    public void stop() {
        mIsRunnable = false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        mTouchedX = event.getX();
        mTouchedY = event.getY();

        return true;
    }

    public void readyObjects(int width, int height) {
        mBlockWidth = width/10;
        mBlockHeight = height/20;

        mItemList = new ArrayList<DrawableItem>();
        mBlockList = new ArrayList<Block>();

        for(int i=0; i < BLOCK_COUNT; i++) {
            float blockTop = i/10*mBlockHeight;
            float blockLeft = i%10*mBlockWidth;
            float blockBottom = blockTop + mBlockHeight;
            float blockRight = blockLeft + mBlockWidth;

            int blockHard = 1;
            double pp = Math.random() * 5d;
            if(pp > 4d) {
                blockHard =2;
            }
            //mItemList.add(new Block(blockTop, blockLeft, blockBottom, blockRight));
            mBlockList.add(new Block(blockTop, blockLeft, blockBottom, blockRight, blockHard));

        }
        mItemList.addAll(mBlockList);

       // 패드 선언
        mPad = new Pad(height*0.8f, height*0.85f);
        mItemList.add(mPad);
        mPadHalfWidth = width /10;

        // 볼 처리 선언
        mBallRadius = width < height ? width /40 : height /40;
        mBall = new Ball(mBallRadius, width/2, height/2);
        mItemList.add(mBall);

        // 라이프
        mLife=3;

        // 게임시작시간
        mGameStartTime = System.currentTimeMillis();

        if(mSavedInstanceState != null) {
            mLife = mSavedInstanceState.getInt(KEY_LIFE);
            mGameStartTime = mSavedInstanceState.getLong(KEY_GAME_START_TIME);
            mBall.restore(mSavedInstanceState.getBundle(KEY_BALL), width, height);

            for(int i=0; i <BLOCK_COUNT; i++) {
                mBlockList.get(i).restore(mSavedInstanceState.getBundle(KEY_BLOCK + String.valueOf(i)));
            }
        }
    }

    private Block getBlock( float x, float y) {

        int index = (int) (x/mBlockWidth) + (int) (y/mBlockHeight)*10;
        if( index >= 0 && index < BLOCK_COUNT) {
            Block block = (Block) mItemList.get(index);
            if(block.isExist()) {
                return block;
            }
        }
        return null;
    }

    // 살아있는 블럭수 세기
    private int getBlockCount() {
        int count =0;
        for(Block block : mBlockList) {
            if(block.isExist()) {
                count++;
            }
        }
        return count;
    }

    // 화면 상태값을 저장하는 메소드
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(KEY_LIFE, mLife);
        outState.putLong(KEY_GAME_START_TIME, mGameStartTime);
        outState.putBundle(KEY_BALL, mBall.save(getWidth(), getHeight()));

        for(int i=0; i<BLOCK_COUNT; i++) {
            outState.putBundle(KEY_BLOCK + String.valueOf(i), mBlockList.get(i).save());
        }
    }


}
