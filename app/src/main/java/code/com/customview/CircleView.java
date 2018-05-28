package code.com.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * Author: lihui1
 * Date: 2018/5/12
 * Desc:
 */

public class CircleView extends ViewGroup{

    private float mCircleMargin;
    private boolean isReverse = true;
    private CircleAdapter mCircleAdapter;
    private DataSetObserver mObserver;

    public float getCircleMargin() {
        return mCircleMargin;
    }

    public void setCircleMargin(float circleMargin) {
        mCircleMargin = circleMargin;
    }

    public CircleView(Context context) {
        super(context);
    }

    public CircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CircleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.circleStyle);
        mCircleMargin = array.getFloat(R.styleable.circleStyle_circleMargin, 0.0f);//默认0
        isReverse = array.getBoolean(R.styleable.circleStyle_isReverse, isReverse);
        array.recycle();
        setChildrenDrawingOrderEnabled(true);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        /*1.测量控件的宽和高*/
        /*1.1获取自己的测量模式*/
        int modeWidth = MeasureSpec.getMode(widthMeasureSpec);
        int modeHeight = MeasureSpec.getMode(heightMeasureSpec);
        /*1.2获取自己的宽和高*/
        int sizeWidth = MeasureSpec.getSize(widthMeasureSpec);
        int sizeHeight = MeasureSpec.getSize(heightMeasureSpec);

        int count = getChildCount();
        int width = 0;
        int height = 0;

        for (int i = 0; i<count; i++){
            View child = getChildAt(i);
            measureChild(child, widthMeasureSpec, heightMeasureSpec);
            int measureWidth = child.getMeasuredWidth();
            if (i == 0){
                width = measureWidth;
            } else {
                width += (int) (mCircleMargin * width + 0.5f);
            }
            int measureHeight = child.getMeasuredHeight();
            height = Math.max(height, measureHeight);
        }
        width += getPaddingLeft() + getPaddingRight();
        height += getPaddingTop() + getPaddingBottom();
        /*设置自己的宽和高*/
        setMeasuredDimension(modeWidth == MeasureSpec.EXACTLY ? sizeWidth : width,
                modeHeight == MeasureSpec.EXACTLY ? sizeHeight : height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (changed){
            int count = getChildCount();
            int paddingLeft = getPaddingLeft();
            int paddingTop = getPaddingTop();
            /*摆放位置*/
            for (int i = 0; i<count;i++){
                View child = getChildAt(i);
                int width = child.getMeasuredWidth();
                int height = child.getMeasuredHeight();
                if (i>0){
                    paddingLeft += (mCircleMargin * width + 0.5f);
                }
                child.layout(paddingLeft, paddingTop, paddingLeft + width, paddingTop + height);
            }
        }
    }

    @Override
    protected int getChildDrawingOrder(int childCount, int i) {
        if (!isReverse){
            return i;
        }
        return childCount - 1 - i;
    }

    public void setAdapter(CircleAdapter circleAdapter){
        this.mCircleAdapter = circleAdapter;
        // 移除监听
        if (mCircleAdapter != null && mObserver != null) {
            mCircleAdapter.unregisterDataSetObserver(mObserver);
            mCircleAdapter = null;
            mObserver = null;
        }
        if (mCircleAdapter == null) {
            throw new NullPointerException("FlowBaseAdapter is null");
        }
        resetLayout();
        mObserver = new DataSetObserver() {
            @Override
            public void onChanged() {
                resetLayout();
            }
        };
        mCircleAdapter.registerDataSetObserver(mObserver);
        
    }

    private void resetLayout() {
        removeAllViews();
        int count = mCircleAdapter.getCount();
        for (int i = 0; i < count; i++) {
            View view = mCircleAdapter.getView(i, this);
            addView(view);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        if (mCircleAdapter != null && mObserver != null){
            mCircleAdapter.unregisterDataSetObserver(mObserver);
            mCircleAdapter = null;
            mObserver = null;
        }
        super.onDetachedFromWindow();
    }

}
