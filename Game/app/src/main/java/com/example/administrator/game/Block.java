package com.example.administrator.game;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * Created by Administrator on 2017-03-11.
 */

public class Block implements DrawableItem{

    private final float mTop;
    private final float mLeft;
    private final float mBottom;
    private final float mRight;
    private int mHard;

    private boolean mIsCollision = false;   // 충돌상태 기록 플래그
    private boolean mIsExist = true;

    public Block(float top, float left, float bottom, float right) {
        mTop =top;
        mLeft =left;
        mBottom = bottom;
        mRight = right;
        mHard=1;
    }

    public void draw(Canvas canvas, Paint paint) {
        //if(mHard > 0) {
        if(mIsExist) {

            if(mIsCollision) {
                mHard--;
                mIsCollision = false;
                if(mHard <=0) {
                    mIsExist = false;
                    return;
                }
            }

            paint.setColor(Color.BLUE);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawRect(mLeft,mTop, mRight, mBottom, paint);

            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(4f);
            canvas.drawRect(mLeft,mTop,mRight,mBottom, paint);
        }
    }

    public void collision() {
        mIsCollision = true;
    }

    public boolean isExist() {
        return mIsExist;
    }

}
