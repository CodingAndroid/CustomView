package code.com.customview;

import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;

/**
 * Author: lihui1
 * Date: 2018/5/12
 * Desc:
 */

public abstract class CircleAdapter {
    private DataSetObservable mObservable = new DataSetObservable();

    /**
     * 数量
     */
    public abstract int getCount();

    /**
     * 条目的布局
     */
    public abstract View getView(int position, ViewGroup parent);

    /**
     * 注册数据监听
     */
    public void registerDataSetObserver(DataSetObserver observer) {
        mObservable.registerObserver(observer);
    }

    /**
     * 移除数据监听
     */
    public void unregisterDataSetObserver(DataSetObserver observer) {
        mObservable.unregisterObserver(observer);
    }

    /**
     * 内容改变
     */
    public void notifyDataSetChanged() {
        mObservable.notifyChanged();
    }
}
