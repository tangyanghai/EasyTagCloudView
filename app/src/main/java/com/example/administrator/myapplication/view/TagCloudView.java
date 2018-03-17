package com.example.administrator.myapplication.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;


import com.example.administrator.myapplication.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>项目名：DongNaoXueYuan</p>
 * <p>包名：com.alpha.mylibrary.com.example.administrator.myapplication.view</p>
 * <p>文件名：TagCloudView</p>
 * <p>创建者：tyh</p>
 * <p>创建时间：2018/1/19 15:37</p>
 * <p>描述：自定义标签云</p>
 */
public class TagCloudView extends ViewGroup {
    /** 标签动画时间长度 */
    private int animator_time = 150;
    /** 标签横向间距 */
    private int tag_distance_line;
    /** 标签纵向间距 */
    private int tag_distance_row;

    /** 所有的标签的集合 */
    SparseArray<ViewHolder> mViewHolders = new SparseArray<>(30);

    /** 每行第一个标签的位置,在onMeasure时记录,在onLayout中使用 */
    List<Integer> rowPosition;

    /** 删除时是否有动画 */
    private boolean tag_hasDeleteAnimator;

    private boolean canClick = true;

    private int wMS = -1;
    private int hMS = -1;


    private TagAdapter mAdapter;

    public TagCloudView(Context context) {
        super(context);
    }

    public TagCloudView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttrs(context, attrs);
    }

    public TagCloudView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //记录一下测量的原始数据
        if (wMS == -1) {
            wMS = widthMeasureSpec;
            hMS = heightMeasureSpec;
        }

        //确定宽度  宽度只能写死,设置wrap_content没什么意义
        measureSelf(widthMeasureSpec, heightMeasureSpec, -1);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        layoutSelf();
    }

    private void measureSelf(int widthMeasureSpec, int heightMeasureSpec, int nullPosition) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = measuredHeight(width, widthMeasureSpec, heightMeasureSpec, nullPosition);
        setMeasuredDimension(width, height);
    }

    private void layoutSelf() {
        int rowCount = rowPosition.size();
        int ll;
        int tt = 0;
        int rr;
        int bb = 0;
        for (int i = 0; i < rowCount; i++) {
            //每行的起头view的位置
            int firstPosition = rowPosition.get(i);
            //每行的结尾view的位置
            int lastPostion = i == rowCount - 1 ? getChildCount() - 1 : rowPosition.get(i + 1) - 1;

            //每行左右都是从0开始
            ll = 0;
            rr = 0;

            //每一行的头部和底部
            if (i != 0) {
                tt = bb + tag_distance_row;
            }
            bb = tt + getChildAt(firstPosition).getMeasuredHeight();

            for (int j = firstPosition; j <= lastPostion; j++) {
                View view = getChildAt(j);
                //每一个view的左边和右边
                if (j != firstPosition) {
                    ll = rr + tag_distance_line;
                }
                rr = ll + view.getMeasuredWidth();
                //每个view的布局
                view.layout(ll, tt, rr, bb);
            }
        }
    }

    /**
     * 测量高度
     *
     * @param width             总宽度
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    private int measuredHeight(int width, int widthMeasureSpec, int heightMeasureSpec, int nullPosition) {
        rowPosition = new ArrayList<>();
        int result = 0;//默认高度
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightModel = MeasureSpec.getMode(heightMeasureSpec);
        int w = 0;//当前行宽度
        int row = 0;//行数
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        boolean isFirst = true;
        for (int i = 0; i < getChildCount(); i++) {
            if (i == nullPosition) {
                continue;
            }
            View view = getChildAt(i);
            int cW = view.getMeasuredWidth();
            if (isFirst || w + tag_distance_line + cW > width) {
                isFirst = false;
                //换行,计算高度
                //宽度超过总宽度
                w = cW;
                if (row == 0) {
                    result = view.getMeasuredHeight();
                } else {
                    result += view.getMeasuredHeight() + tag_distance_row;
                }
                row++;
                rowPosition.add(i);
            } else {
                w += tag_distance_line + cW;
            }
        }
        switch (heightModel) {
            case MeasureSpec.EXACTLY:
                result = heightSize;
                break;
            case MeasureSpec.UNSPECIFIED://在可滑动的视图内
            case MeasureSpec.AT_MOST://包裹内容
                break;
        }
        return result;
    }

    /**
     * 初始化属性
     * 是否有删除动画   tag_hasDeleteAnimator
     * 标签内容左右padding tag_padding_left
     * 标签内容上下padding tag_padding_top
     */
    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray at = context.obtainStyledAttributes(attrs, R.styleable.AutoScrollRecyclerView);
        tag_distance_line = at.getDimensionPixelSize(R.styleable.AutoScrollRecyclerView_tag_distance_line, 30);
        tag_distance_row = at.getDimensionPixelSize(R.styleable.AutoScrollRecyclerView_tag_distance_row, 20);
        tag_hasDeleteAnimator = at.getBoolean(R.styleable.AutoScrollRecyclerView_tag_hasDeleteAnimator, false);
    }

    /**
     * 初始化所有标签
     * 标签都是TextView
     * 最后刷新界面
     */
    private void initChildren() {
        removeAllViews();
        mViewHolders.clear();
        for (int i = 0; i < mAdapter.mDatas.size(); i++) {
            View view = creatTag(i);
            addView(view);
        }
        invalidate();
    }

    /**
     * 删除一条数据
     * 现在的删除条目条目流程:
     * 1.删除数据集中相应数据
     * 2.隐藏删除条目,移动删除条目后面的条目到新的位置
     * 3.重新initChildren,将界面刷新
     * 待优化:
     * 第3条,应该优化,做到不用重新创建子view
     * @param position 删除位置
     *
     */
    public void deleteTag(int position) {
        if (canClick) {
            mAdapter.mDatas.remove(position);
            measureSelf(wMS, hMS, position);
            if (tag_hasDeleteAnimator) {
                initDeleteAnimator(position, animator_time);
            } else {
                //时间设置为0,就成为了没有动画,避免了重新添加所有的view
                initDeleteAnimator(position, 0);
            }
        }
    }

    private SparseArray<ViewHolder> getAllTags() {
        return mViewHolders;
    }

    /** 初始化删除动画 */
    private void initDeleteAnimator(int realPosition, long time) {
        List<Animator> animators = new ArrayList<>();
        int rowCount = rowPosition.size();
        int ll = 0;
        int tt = 0;
        int rr = 0;
        int bb = 0;
        for (int i = 0; i < rowCount; i++) {
            //每行的起头view的位置
            int firstPosition = rowPosition.get(i);
            //每行的结尾view的位置
            int lastPostion = i == rowCount - 1 ? getChildCount() - 1 : rowPosition.get(i + 1) - 1;

            ll = 0;
            rr = 0;

            //每一行的头部和底部
            if (i != 0) {
                tt = bb + tag_distance_row;
            }
            bb = tt + getChildAt(firstPosition).getMeasuredHeight();

            for (int j = firstPosition; j <= lastPostion; j++) {
                if (j == realPosition) {
                    continue;
                }
                View view = getChildAt(j);
                int oldX = view.getLeft();
                int oldY = view.getTop();
                //每一个view的左边和右边
                if (j != firstPosition) {
                    ll = rr + tag_distance_line;
                }
                rr = ll + view.getMeasuredWidth();
                //每个view的布局
                if (j > realPosition) {
                    traslateViewAlfterDelete(animators, view, ll - oldX, tt - oldY);
                }
            }
        }

        final View view = getChildAt(realPosition);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
        animators.add(alpha);
        AnimatorSet set = new AnimatorSet();
        set.setDuration(animator_time);
        set.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                canClick = false;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                initChildren();
                canClick = true;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        set.playTogether(animators);
        set.start();
    }

    /**
     * 移动动画
     *
     * @param list 动画集合
     * @param view 要移动的view
     * @param toX  x方向移动的距离
     * @param toY  y方向移动的距离
     */
    private void traslateViewAlfterDelete(List<Animator> list, View view, int toX, int toY) {
        ObjectAnimator translationX = ObjectAnimator.ofFloat(view, "translationX", 0, toX);
        ObjectAnimator translationY = ObjectAnimator.ofFloat(view, "translationY", 0, toY);
        list.add(translationX);
        list.add(translationY);
    }

    /**
     * 创建单个标签
     *
     * @param position 标签所在位置
     */
    private View creatTag(final int position) {
        if (mAdapter != null) {
            ViewHolder holder;
            if (mViewHolders.size() > position) {
                holder = mViewHolders.get(position);
            } else {
                holder = mAdapter.createViewHolder(position);
                mAdapter.onBindViewHolder(holder, position, mAdapter.mDatas.get(position));
                mViewHolders.append(position, holder);
            }
            return holder.item;
        }
        return null;
    }

    public TagAdapter getmAdapter() {
        return mAdapter;
    }

    public void setAdapter(TagAdapter mAdapter) {
        this.mAdapter = mAdapter;
        mAdapter.setTagCloudView(this);
    }

    /** 设置时间长度 */
    public void setAnimator_time(int animator_time) {
        this.animator_time = animator_time;
    }

    /** 设置是否有删除数据动画 */
    public void setTag_hasDeleteAnimator(boolean tag_hasDeleteAnimator) {
        this.tag_hasDeleteAnimator = tag_hasDeleteAnimator;
    }

    /**
     *  标签adapter
     *  便于功能拓展
     *
     * @param <T> 标签实体类 泛型
     * @param <VH> ViewHolder实体类  泛型
     */
    public abstract static class TagAdapter<T,VH extends ViewHolder> {
        public Context mContext;

        /** 数据 */
        protected List<T> mDatas;

        /** tagview */
        private WeakReference<TagCloudView> mTagView;

        public TagAdapter(List<T> mDatas) {
            this.mDatas = mDatas;
        }

        /** 删除条目 */
        public void removeTag(int position) {
            TagCloudView tagCloudView = mTagView.get();
            if (tagCloudView != null) {
                tagCloudView.deleteTag(position);
            }
        }

        /** 配置tagview 内部调用 */
        private void setTagCloudView(TagCloudView tagCloudView) {
            mTagView = new WeakReference<>(tagCloudView);
            mContext = tagCloudView.getContext();
            tagCloudView.initChildren();
        }

        /** 刷新界面 */
        public void notifyDataChanged() {
            TagCloudView tagCloudView = mTagView.get();
            if (tagCloudView != null) {
                tagCloudView.initChildren();
            }
        }

        protected SparseArray<VH> getAllTagView() {
            TagCloudView tagCloudView = mTagView.get();
            if (tagCloudView != null) {
                return (SparseArray<VH>) tagCloudView.getAllTags();
            }
            return new SparseArray<>();
        }

        /**
         * @param position 位置
         * @return 对应位置的ViewHolder
         */
        public abstract VH createViewHolder(int position);


        /**
         * @param holder        相应位置的ViewHolder
         * @param position      位置
         * @param bean          相应位置的数据
         */
        public abstract void onBindViewHolder(VH holder, int position, T bean);
    }

    /**
     * 持有每个tag里面的view,便于操作
     */
    public static class ViewHolder {
        protected View item;

        public ViewHolder(View item) {
            this.item = item;
        }

        public View getItem() {
            return item;
        }
    }
}
