package code.com.customview;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

/**
 * Author: lihui1
 * Date: 2018/5/27
 * Desc:
 */

public class TickView extends View{

    private Paint mPaint;//画运动的弧
    private Paint mLinePaint;//对号画笔
    private Paint mCirclePaint;//向圆心收缩的画笔
    private Paint mTickPaint;//对钩画笔
    private Path mPath;
    private PathMeasure mPathMeasure;
    private RectF mRectF;//
    private int progress;
    private int mRadius;
    private float mPoints[] = new float[8];
    private ObjectAnimator mCircleAnimator;

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
        postInvalidate();
    }

    public int getRadius() {
        return mRadius;
    }

    public void setRadius(int radius) {
        mRadius = radius;
        postInvalidate();
    }

    public TickView(Context context) {
        super(context);
        init();
    }

    public TickView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TickView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * 初始化
     */
    private void init(){

        mRectF = new RectF();

        mPaint = new Paint();
        mPaint.setStrokeWidth(15);
        mPaint.setColor(Color.YELLOW);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeCap(Paint.Cap.ROUND);

        mCirclePaint = new Paint();
        mCirclePaint.setColor(Color.WHITE);
        mCirclePaint.setStyle(Paint.Style.FILL);
        mCirclePaint.setAntiAlias(true);

        mTickPaint = new Paint();
        mTickPaint.setStrokeWidth(6);
        mTickPaint.setColor(Color.WHITE);
        mTickPaint.setStyle(Paint.Style.FILL);
        mTickPaint.setAntiAlias(true);

        mCircleAnimator = ObjectAnimator.ofInt(this, "radius", (int) 250/2, 0);
        mCircleAnimator.setInterpolator(new DecelerateInterpolator());//减速
        mCircleAnimator.setDuration(3000);

        final ObjectAnimator animator = ObjectAnimator.ofInt(this, "progress", 0, 360);
        animator.setDuration(2000);
        animator.setInterpolator(null);
        animator.start();
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mCircleAnimator.start();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    private void initRight(){
        float startX = 250 / 4;
        float startY = 250 / 2;

        float cornerX = 250/2;
        float cornerY = 250 - 250 / 4;

        float endX = 250 / 2;
        float endY = 250 / 4;

        mPath = new Path();
        mPath.moveTo(startX, startY);
        mPath.lineTo(cornerX, cornerY);
        mPath.lineTo(endX, endY);
        mPathMeasure.setPath(mPath, false);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);

        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width, height;
        if (widthMode == MeasureSpec.EXACTLY){
            width = widthSize;
        } else {
            width = 250;
        }
        if (heightMode == MeasureSpec.EXACTLY){
            height = heightSize;
        } else {
            height = 250;
        }
        setMeasuredDimension(width, height);
        mRectF.set(15/2,15/2, width - 15/2, height - 15/2);
        mPoints[0] = mRectF.centerX() - mRectF.width()/2 + 20;
        mPoints[1] = mRectF.centerY();
        mPoints[2] = mRectF.centerX() - mRectF.width()/4 + 20;
        mPoints[3] = mRectF.centerY() + mRectF.width()/4 + 24;
        mPoints[4] = mRectF.centerX() - mRectF.width()/4 + 20;
        mPoints[5] = mRectF.centerY() + mRectF.width()/4 + 22;
        mPoints[6] = mRectF.centerX() + mRectF.width()/4 + 20;
        mPoints[7] = mRectF.centerY() - mRectF.width()/4;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawArc(mRectF, 90, progress, false, mPaint);
        mCirclePaint.setColor(Color.YELLOW);
        canvas.drawCircle(mRectF.centerX(), mRectF.centerY(), progress == 360 ? mRectF.width()/2-15/2:0, mCirclePaint);
        if (progress == 360){
            mCirclePaint.setColor(Color.WHITE);
            canvas.drawCircle(mRectF.centerX(), mRectF.centerY(), mRadius, mCirclePaint);
            if (mRadius == 0){
                canvas.drawLines(mPoints, mTickPaint);
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }
}
