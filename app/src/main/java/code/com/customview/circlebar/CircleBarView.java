package code.com.customview.circlebar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;
import android.widget.TextView;

import code.com.customview.R;
import code.com.customview.utils.DpUtils;

/**
 * Author: lihui1
 * Date: 2018/5/13
 * Desc:
 */

public class CircleBarView extends View{

    private Paint mPaint;//绘制矩形的画笔
    private Paint mCirclePaint;//绘制圆弧的画笔
    private Paint mCircleBgPaint;//绘制圆弧背景的画笔
    private float barWidth;//圆弧进度条宽度
    private int progressColor;
    private int bgColor;
    private float startAngle;
    private float sweepAngle;
    private float progressAngle;
    private int progressNum;
    private int maxNum;
    private RectF mRectF;//绘制圆弧的矩形区域
    private int defaultSize;//自定义view的默认宽和高
    private TextView mTextView;
    private OnAnimationListener mOnAnimationListener;
    public interface OnAnimationListener{
        String textChanged(float interpolatedTime, int progressNum, int maxNum);
        void progressColorChange(Paint paint, float interpolatedTime, int progressNum, int maxNum);
    }
    public void setOnAnimationListener(OnAnimationListener onAnimationListener){
        this.mOnAnimationListener = onAnimationListener;
    }
    private CircleBarAnim mAnim;

    public CircleBarView(Context context) {
        super(context);
    }

    public CircleBarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CircleBarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs){
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.CircleBarView);
        progressColor = array.getColor(R.styleable.CircleBarView_progress_color, Color.GREEN);
        bgColor = array.getColor(R.styleable.CircleBarView_bg_color, Color.GRAY);
        startAngle = array.getFloat(R.styleable.CircleBarView_start_angle, 0.0f);
        sweepAngle = array.getFloat(R.styleable.CircleBarView_sweep_angle, 360.0f);
        barWidth = array.getDimension(R.styleable.CircleBarView_bar_width, DpUtils.dip2px(context, 10));
        array.recycle();

        progressNum = 0;
        maxNum = 100;

        defaultSize = DpUtils.dip2px(context, 100);
        mRectF = new RectF();
        /*画矩形*/
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);//只描边, 不填充
        mPaint.setColor(Color.BLUE);
        /*画圆弧*/
        mCirclePaint = new Paint();
        mCirclePaint.setStyle(Paint.Style.STROKE);//只描边, 不填充
        mCirclePaint.setColor(progressColor);
        mCirclePaint.setStrokeWidth(barWidth);
        mCirclePaint.setAntiAlias(true);//设置抗锯齿
        mCirclePaint.setStrokeCap(Paint.Cap.ROUND);//设置圆角
        /*画圆弧背景*/
        mCircleBgPaint = new Paint();
        mCircleBgPaint.setStyle(Paint.Style.STROKE);//只描边, 不填充
        mCircleBgPaint.setColor(bgColor);
        mCircleBgPaint.setStrokeWidth(barWidth);
        mCircleBgPaint.setAntiAlias(true);//设置抗锯齿
        mCircleBgPaint.setStrokeCap(Paint.Cap.ROUND);//设置圆角

        mAnim = new CircleBarAnim();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = measureSize(defaultSize, widthMeasureSpec);
        int height = measureSize(defaultSize, heightMeasureSpec);
        int min = Math.min(width, height);
        setMeasuredDimension(min, min);
        if (min >= barWidth * 2){
            mRectF.set(barWidth / 2, barWidth / 2, min - barWidth / 2, min - barWidth / 2);
        }
    }

    private int measureSize(int defaultSize, int measureSpec) {
        int result = defaultSize;
        int specMode = View.MeasureSpec.getMode(measureSpec);
        int specSize = View.MeasureSpec.getSize(measureSpec);
        if (specMode == MeasureSpec.EXACTLY){
            result = specSize;
        } else if (specMode == MeasureSpec.AT_MOST){
            result = Math.min(result, specSize);
        }
        return result;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //画圆弧背景
        canvas.drawArc(mRectF, startAngle, sweepAngle, false, mCircleBgPaint);
        //画圆弧,0对应3点钟方向
        canvas.drawArc(mRectF, startAngle, progressAngle, false, mCirclePaint);
        //canvas.drawRect(mRectF, mPaint);

    }

    public void setAnimTime(int progressNum, int time){
        mAnim.setDuration(time);
        this.startAnimation(mAnim);
        this.progressNum = progressNum;
    }

    public void setTextView(TextView textView){
        this.mTextView = textView;
    }

    public class CircleBarAnim extends Animation{

        public CircleBarAnim() {
        }

        /**
         * 动画初始化
         * @param width
         * @param height
         * @param parentWidth
         * @param parentHeight
         */
        @Override
        public void initialize(int width, int height, int parentWidth, int parentHeight) {
            super.initialize(width, height, parentWidth, parentHeight);
        }

        /**
         * 实现自定义动画效果
         * interpolatedTime: 0-1, 1表示结束
         * @param interpolatedTime
         * @param t
         */
        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            progressAngle = interpolatedTime * sweepAngle * progressNum / maxNum;
            if (mTextView != null && mOnAnimationListener != null){
                mTextView.setText(mOnAnimationListener.textChanged(interpolatedTime, progressNum, maxNum));
            }
            mOnAnimationListener.progressColorChange(mCirclePaint, interpolatedTime, progressNum, maxNum);
            /*
            * invalidate()方法只能在UI Thread中执行, 非UI Thread使用postInvalidate()方法;
            * postInvalidate底层是使用了Handler,这就是为什么能在子线程更新UI的原因。
            * 同时postInvalidate可以指定一个延迟时间。
            * */
            postInvalidate();
        }
    }
}
