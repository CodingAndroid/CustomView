package code.com.customview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Author: lihui1
 * Date: 2018/5/24
 * Desc: 自定义view至少重写一个构造函数
 */

public class WaveButton extends View{

    /**
     * 涟漪默认颜色
     */
    private final int DEFAULT_WAVE_COLOR = Color.WHITE;

    /**
     * 进度条默认颜色
     */
    private final int DEFAULT_PROGRESS_COLOR = Color.parseColor("#0277bd");

    /**
     * 涟漪默认数目
     */
    private final int DEFAULT_WAVE_COUNT = 5;

    /**
     * 涟漪默认速度
     */
    private final int DEFAULT_SPEED = 600;

    /**
     * 涟漪默认持续时间
     */
    private final int DEFAULT_DURATION_TIME = 3000;

    private float progress = 0;

    private int waveColor = DEFAULT_WAVE_COLOR;

    private int progressColor = DEFAULT_PROGRESS_COLOR;

    /**
     * 渐变颜色数组
     */
    private int arcColors[];

    /**
     * 梯度渐变扫描渲染器
     */
    private SweepGradient mSweepGradient;

    /**
     * 控件宽、高
     */
    private int mWidth, mHeight;

    /**
     * bitmap宽、高
     */
    private int bitmapWidth, bitmapHeight;

    private int mWidthHalf, mHeightHalf;

    private float progressRadius;

    private float mMinRadius, mMaxRadius;

    /**
     * 涟漪中心的Drawable资源
     */
    private Drawable waveSrc;

    /**
     * bitmap
     */
    private Bitmap mBitmap;

    /**
     * 涟漪画笔
     */
    private Paint wavePaint;

    /**
     * bitmap画笔
     */
    private Paint bitmapPaint;

    /**
     * 进度条画笔
     */
    private Paint progressPaint;

    /**
     * 判断动画是否结束
     */
    private boolean isEnd = false;

    /**
     * 判断动画是否在运行
     */
    private boolean isRunning = false;

    /**
     * 涟漪最后创建时间
     */
    private long mLastCreateTime;

    /**
     * 涟漪集合
     */
    private List<Wave> mWaveList = new ArrayList<>();

    /**
     * 线性插值器（匀速）
     */
    private Interpolator mInterpolator = new LinearInterpolator();

    /**
     * 进度条属性动画
     */
    private ValueAnimator progressAnimator;

    /**
     * 矩形
     */
    private RectF rectF = new RectF();

    public WaveButton(Context context) {
        super(context);
        init();
    }

    public WaveButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        //取出自定义属性
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.WaveButton);
        waveSrc = typedArray.getDrawable(R.styleable.WaveButton_waveSrc);
        if (waveSrc == null){
            waveSrc = getResources().getDrawable(R.mipmap.ic_launcher_round);
        }
        //如果没有指定取默认值
        waveColor = typedArray.getColor(R.styleable.WaveButton_waveColor, DEFAULT_WAVE_COLOR);
        arcColors = new int[]{Color.WHITE, progressColor, Color.WHITE};
        //资源回收
        typedArray.recycle();
        init();
    }

    public WaveButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * 初始化
     */
    private void init(){
        //涟漪画笔
        wavePaint = new Paint(Paint.ANTI_ALIAS_FLAG);//消除锯齿
        wavePaint.setColor(waveColor);
        //bitmap画笔
        bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        //进度条画笔
        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setDither(true);//防止抖动
        progressPaint.setStyle(Paint.Style.STROKE);//描边, 不填充   设置画笔的样式, 为FILL, FILL_OR_STROKE, 或STROKE
        progressPaint.setStrokeCap(Paint.Cap.ROUND);//设置笔刷圆形式样
        progressPaint.setColor(progressColor);//设置颜色
        progressPaint.setStrokeWidth(12);//设置宽度
        //获取屏幕大小
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int screenHeight = dm.heightPixels;
        //创建bitmap
        BitmapDrawable drawable = (BitmapDrawable) waveSrc;
        mBitmap = Bitmap.createScaledBitmap(drawable.getBitmap(), screenHeight / 8, screenHeight / 8, true);
        //获取bitmap宽、高
        bitmapWidth = mBitmap.getWidth();
        bitmapHeight = mBitmap.getHeight();
        progressRadius = (bitmapWidth + 40) / 2;

        mMinRadius = (float) (bitmapWidth * 0.8 / 2);
        mMaxRadius = bitmapWidth;

        setLayerType(LAYER_TYPE_HARDWARE, null);

        progressAnimator = ObjectAnimator.ofFloat(this, "progress", 0, 360);
        progressAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                stop();
                isEnd = true;
                setLayerType(LAYER_TYPE_NONE, null);
            }
        });
        progressAnimator.setDuration(5000);
        progressAnimator.start();
        start();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = getWidth();
        mHeight = getHeight();

        mWidthHalf = mWidth / 2;
        mHeightHalf = mHeight / 2;

        mSweepGradient = new SweepGradient(mWidth / 2, mHeight / 2, arcColors, null);
    }

    /**
     * 确定的view大小
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     * MeasureSpec(32位, int类型)包括测量模式mode(占MeasureSpec的高2位)和测量大小size(占MeasureSpec的低30位)
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        /*获取WaveButton MeasureSpec大小和模式(宽度和高度)*/
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        /*检查MeasureSpec设置和调整view(宽度和高度)的尺寸和模式*/
        int width, height;
        /**
         * EXACTLY--表示父视图希望子视图的大小应该是由specSize的值来决定的, 系统默认会按照这个规则来设置子视图的大小,
         * 简单的说(当设置width或height为match_parent时,模式为EXACTLY,因为子view会占据剩余容器的空间,所以它大小是确定的)
         *
         * AT_MOST--表示子视图最多只能是specSize中指定的大小。
         * 当设置为wrap_content时, 模式为AT_MOST, 表示子view的大小最多是多少, 这样子view会根据这个上限来设置自己的尺寸
         *
         * UNSPECIFIED--表示开发人员可以将视图按照自己的意愿设置成任意的大小, 没有任何限制。这种情况比较少见, 不太会用到。
         */
        if (widthMode == MeasureSpec.EXACTLY){
            width = widthSize;
        } else {
            width = bitmapWidth * 2;
        }
        if (heightMode == MeasureSpec.EXACTLY){
            height = heightSize;
        } else {
            height = bitmapHeight * 2;
        }
        setMeasuredDimension(width, height);//子视图告诉父视图具体大小
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //开启硬件加速
        setLayerType(View.LAYER_TYPE_SOFTWARE, wavePaint);
        setLayerType(View.LAYER_TYPE_SOFTWARE, bitmapPaint);
        setLayerType(View.LAYER_TYPE_SOFTWARE, progressPaint);
        //涟漪的迭代器
        Iterator<Wave> iterator = mWaveList.iterator();
        while (iterator.hasNext()){
            Wave wave = iterator.next();
            if (System.currentTimeMillis() - wave.mCreateTime < DEFAULT_DURATION_TIME){
                wavePaint.setAlpha(wave.getAlpha());
                canvas.drawCircle(mWidthHalf, mHeightHalf, wave.getCurrentRadius(), wavePaint);
            } else {
                iterator.remove();
            }
        }
        canvas.drawBitmap(mBitmap, (mWidth - bitmapWidth) / 2, (mHeight - bitmapHeight) / 2, bitmapPaint);
        canvas.rotate(-90, mWidthHalf, mHeightHalf);
        rectF.set(mWidthHalf - progressRadius, mHeightHalf - progressRadius, mWidthHalf + progressRadius, mHeightHalf + progressRadius);
        progressPaint.setShader(mSweepGradient);
        //第一个参数圆弧的形状和轮廓，第二个参数开始的角度，
        //第三个参数圆弧顺时针扫过的角度， 第四个参数是否经过圆心
        canvas.drawArc(rectF, 0, progress, false, progressPaint);

        //关闭硬件加速
        setLayerType(LAYER_TYPE_NONE, bitmapPaint);
        setLayerType(LAYER_TYPE_NONE, progressPaint);
        setLayerType(LAYER_TYPE_NONE, wavePaint);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int bitmapMargin = (mWidth - bitmapWidth) / 2;
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                if (!isEnd){
                    if (event.getX() >= bitmapMargin && event.getX() <= bitmapMargin + bitmapWidth && event.getY() >= bitmapMargin && event.getY() <= bitmapMargin + bitmapHeight){
                        if (isRunning){
                            stop();
                            progressAnimator.pause();
                        } else {
                            start();
                            progressAnimator.resume();
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                break;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }

    private void createWave(){
        long currentTime = System.currentTimeMillis();
        if (currentTime - mLastCreateTime < DEFAULT_SPEED){
            return;
        }
        Wave wave = new Wave();
        mWaveList.add(wave);
        invalidate();
        mLastCreateTime = currentTime;
    }

    private class Wave{
        private long mCreateTime;

        public Wave(){
            this.mCreateTime = System.currentTimeMillis();
        }

        public int getAlpha(){
            float percent = (System.currentTimeMillis() - mCreateTime) * 1.0f / DEFAULT_DURATION_TIME;
            return (int) ((1.0f - mInterpolator.getInterpolation(percent)) * 255);
        }

        public float getCurrentRadius(){
            float percent = (System.currentTimeMillis() - mCreateTime) * 1.0f / DEFAULT_DURATION_TIME;
            return mMinRadius + mInterpolator.getInterpolation(percent) * (mMaxRadius - mMinRadius);
        }
    }

    private Runnable mCreateWave = new Runnable() {
        @Override
        public void run() {
            if (isRunning){
                createWave();
                postDelayed(mCreateWave, DEFAULT_SPEED);
            }
        }
    };

    private void stop() {
        isRunning = false;
        mWaveList.clear();
    }

    private void start(){
        if (!isRunning){
            isRunning = true;
            mCreateWave.run();
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }

    public float getProgress() {
        return progress;
    }

    public void setProgress(float progress) {
        this.progress = progress;
        invalidate();
    }

    public int getProgressColor() {
        return progressColor;
    }

    public void setProgressColor(int progressColor) {
        this.progressColor = progressColor;
        invalidate();
    }
}
