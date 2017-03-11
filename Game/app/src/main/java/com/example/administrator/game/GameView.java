package com.example.administrator.game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.SurfaceTexture;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;

import java.util.ArrayList;
import java.util.logging.Handler;

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


    //public GameView(Context context) {
    public GameView(final Context context) {
        super(context);
        setSurfaceTextureListener(this);
        setOnTouchListener(this);
        /*

        mHandler = new Handler() {


            @Override
            public void publish(LogRecord record) {

            }

            @Override
            public void flush() {

            }

            @Override
            public void close() throws SecurityException {

            }

            @Override
            public void handleMessage(Message message) {
                // 실행할 처리
                Intent intent = new Intent(context, ClearActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent.putExtra(message.getData());
                context.startActivity(intent);


            }

        };
        */


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

        mThread = new Thread(new Runnable() {
            @Override
            public void run() {


                Paint paint = new Paint();
                paint.setColor(Color.RED);
                paint.setStyle(Paint.Style.FILL);



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
                        }
                        /*

                        if(ballTop < 0 || ballBottom > getHeight()) {
                            mBall.setSpeedY(-mBall.getSpeedY());
                        }
                        */

                        if(ballTop < 0 ) {
                            mBall.setSpeedY(-mBall.getSpeedY());
                        }
                        if(ballTop > getHeight()) {
                            if(mLife > 0) {
                                mLife--;
                                mBall.reset();
                            }
                        }

                        Block leftBlock = getBlock(ballLeft, mBall.getY());
                        Block topBlock = getBlock(mBall.getX(), ballTop);
                        Block rightBlock = getBlock(ballRight, mBall.getY());
                        Block bottomBlock = getBlock(mBall.getX(), ballBottom);

                        if(leftBlock != null) {
                            mBall.setSpeedX(-mBall.getSpeedX());
                            leftBlock.collision();
                        }
                        if(topBlock != null) {
                            mBall.setSpeedY(-mBall.getSpeedY());
                            topBlock.collision();
                        }
                        if(rightBlock != null) {
                            mBall.setSpeedX(-mBall.getSpeedX());
                            rightBlock.collision();
                        }
                        if(bottomBlock != null) {
                            mBall.setSpeedY(-mBall.getSpeedY());
                            bottomBlock.collision();
                        }

                        float padTop = mPad.getTop();
                        float ballSpeedY = mBall.getSpeedY();

                        if(ballBottom > padTop && ballBottom - ballSpeedY <  padTop && padLeft < ballRight && padRight > ballLeft) {
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
            //mItemList.add(new Block(blockTop, blockLeft, blockBottom, blockRight));
            mBlockList.add(new Block(blockTop, blockLeft, blockBottom, blockRight));

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


}
