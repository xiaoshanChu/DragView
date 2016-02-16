package com.example.pkxutao.dragview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.widget.ImageView;

/**
 * Created by Xiaoshan on 2016/2/4.
 */
public class MyImageView extends ImageView {

    private int downX = 0;
    private int downY = 0;
    private float oldDistance = 1f;
    private float oldRotation = 0;
    private PointF midPoint;
    private Bitmap bitmap;
    private Matrix matrix;
    private Matrix matrix1;
    private Matrix savedMatrix;

    private enum MODE {
        NONE, DRAG, ZOOM
    }
    private MODE mode = MODE.NONE;

    public MyImageView(Context context) {
        this(context,null);
    }

    public MyImageView(Context context,AttributeSet attrs) {
        super(context, attrs);
        matrix = new Matrix();
        matrix1 = new Matrix();
        savedMatrix = new Matrix();
        midPoint = new PointF();
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        this.bitmap = bm;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        canvas.drawBitmap(bitmap, matrix, null);
        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mode = MODE.DRAG;
                downX = (int) event.getX();
                downY = (int) event.getY();
                savedMatrix.set(matrix);
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                mode = MODE.ZOOM;
                oldDistance = calculateDis(event);
                oldRotation = calculateRot(event);
                savedMatrix.set(matrix);
                midPoint(midPoint, event);
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == MODE.ZOOM) {//缩放和旋转
                    matrix1.set(savedMatrix);
                    float rotation = calculateRot(event) - oldRotation;
                    float dis = calculateDis(event);
                    float scale = dis / oldDistance;
                    matrix1.postScale(scale, scale, midPoint.x, midPoint.y);
                    matrix1.postRotate(rotation, midPoint.x, midPoint.y);
                    matrix.set(matrix1);
                    invalidate();
                } else if (mode == MODE.DRAG) {//移动
                    matrix1.set(savedMatrix);
                    matrix1.postTranslate(event.getX() - downX, event.getY() - downY);
                    matrix.set(matrix1);
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                mode = MODE.NONE;
                break;
        }
        return true;
    }

    //两点间距离
    private float calculateDis(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return FloatMath.sqrt(x * x + y * y);
    }

    //手势中间点
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    //旋转角度
    private float calculateRot(MotionEvent event) {
        double delta_x = event.getX(0) - event.getX(1);
        double delta_y = event.getY(0) - event.getY(1);
        double radians = Math.atan2(delta_y,delta_x);
        return (float) Math.toDegrees(radians);
    }
}
