package chencheng.bwie.com.chencheng20171130_flowlayout;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dell on 2017/11/30.
 */

public class FlowLayout extends ViewGroup {
    public final String TAG = "FlowLayout";
    public FlowLayout(Context context) {
        this(context,null);
    }

    public FlowLayout(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public FlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(),attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // 获取父容器为其设置的测量模式及大小
        int iWidthMode = MeasureSpec.getMode(widthMeasureSpec);
        int iHeightMode = MeasureSpec.getMode(heightMeasureSpec);
        int iWidthSizeSpec = MeasureSpec.getSize(widthMeasureSpec);
        int iHeightSizeSpec = MeasureSpec.getSize(heightMeasureSpec);
        // 如果是 warp_content 情况下，记录宽高
        int measuredWidth = 0;
        int measuredHeight = 0;
        // 记录每一行的宽度和高度，取宽高的最大值
        int iCurLineW = 0;
        int iCurLineH = 0;
        // 获取 View 的数量
        int iCount = getChildCount();
        // 遍历 View
        for (int i = 0; i < iCount; i++) {
            View childView = getChildAt(i);
            // 测量 View 的真实宽高
            measureChild(childView, widthMeasureSpec, heightMeasureSpec);
            // 获取 View 的 layoutParams
            MarginLayoutParams layoutParams = (MarginLayoutParams) childView.getLayoutParams();
            // View 的实际宽、高应该加上左右、上下的 Margin 大小
            int childWidth = childView.getMeasuredWidth() + layoutParams.leftMargin + layoutParams.rightMargin;
            int childHeight = childView.getMeasuredHeight() + layoutParams.topMargin + layoutParams.bottomMargin;
            // 当行累计宽度加上当前 View 的实际宽度大于父容器设定的宽度，就换行。这里考虑到 Padding 会占父容器给定宽度的大小，所以要减掉这一部分
            if (childWidth + iCurLineW > iWidthSizeSpec - getPaddingLeft() - getPaddingRight()) {
                // measuredWidth 宽度等于最大的宽度值
                measuredWidth = Math.max(childWidth, iCurLineW);
                // 换行之后，行宽度等于第一个 View 的实际宽度
                iCurLineW = childWidth;
                // measuredHeight 等于叠加当前高度
                measuredHeight += iCurLineH;
                // 换行之后，行高度等于第一个 View 的实际高度
                iCurLineH = childHeight;
            } else {
                // 行宽度继续累加 View 的宽度
                iCurLineW += childWidth;
                // 对比 iCurLineH 和 childHeight，取最大的值做行高度
                iCurLineH = Math.max(iCurLineH, childHeight);
            }
            // 如果是最后一个 View，比较当前最大的宽度，累计高度
            if (i == iCount - 1) {
                measuredWidth = Math.max(childWidth, iCurLineW);
                measuredHeight += iCurLineH;
            }
        }
        // 如果是 MeasureSpec.EXACTLY 模式，直接使用父容器提供的宽高，否则用直接测量的宽高
        setMeasuredDimension((iWidthMode == MeasureSpec.EXACTLY) ? iWidthSizeSpec : measuredWidth + getPaddingLeft() + getPaddingRight(), (iHeightMode == MeasureSpec.EXACTLY) ? iHeightSizeSpec : measuredHeight + getPaddingTop() + getPaddingBottom());
    }
    // 存储每一行的 View
    private List<List<View>> mAllViews = new ArrayList<List<View>>();
    // 存储每一行的高度
    private List<Integer> mLineHeight = new ArrayList<Integer>();
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mAllViews.clear();
        mLineHeight.clear();
        int width = getWidth();
        int iCurLineW = 0;
        int iCurLineH = 0;
        // 存储每一个 View
        List<View> mLineViews = new ArrayList<View>();
        int iCount = getChildCount();
        for (int i = 0; i < iCount; i++) {
            View childView = getChildAt(i);
            MarginLayoutParams layoutParams = (MarginLayoutParams) childView.getLayoutParams();
            // 获取 View 的测量宽高
            int childWidth = childView.getMeasuredWidth();
            int childHeight = childView.getMeasuredHeight();
            // 当行累计宽度加上当前 View 宽度大于布局的宽度，就换行
            if (childWidth + iCurLineW + layoutParams.leftMargin + layoutParams.rightMargin > width - getPaddingLeft() - getPaddingRight()) {
                // 记录这一行的高度
                mLineHeight.add(iCurLineH);
                // 记录这一行的 View list
                mAllViews.add(mLineViews);
                // 换行后，行宽度重置
                iCurLineW = 0;
                // 行高度累加
                iCurLineH = childHeight + layoutParams.topMargin + layoutParams.bottomMargin;
                mLineViews = new ArrayList<View>();
            }
            // 累加行的宽高
            iCurLineW += childWidth + layoutParams.leftMargin + layoutParams.rightMargin;
            iCurLineH = Math.max(iCurLineH, childHeight + layoutParams.topMargin + layoutParams.bottomMargin);
            mLineViews.add(childView);
        }
        // 记录最后一行的高度和行 View list
        mLineHeight.add(iCurLineH);
        mAllViews.add(mLineViews);
        int curLeft = getPaddingLeft();
        int curTop = getPaddingTop();
        // 得到总行数
        int lineNums = mAllViews.size();
        for (int i = 0; i < lineNums; i++) {
            // 获取每一行的 View list
            mLineViews = mAllViews.get(i);
            // 获取每一行的高度
            iCurLineH = mLineHeight.get(i);
            for (int j = 0; j < mLineViews.size(); j++) {
                View childView = mLineViews.get(j);
                // 如果 View 设置了 Visibility = View.GONE，就跳过
                if (childView.getVisibility() == View.GONE) {
                    continue;
                }
                MarginLayoutParams lp = (MarginLayoutParams) childView.getLayoutParams();
                // 计算每一个 View 的上下左右位置
                int left = curLeft + lp.leftMargin;
                int top = curTop + lp.topMargin;
                int right = left + childView.getMeasuredWidth();
                int bottom = top + childView.getMeasuredHeight();
                Log.e(TAG, "left = " + left + ", top = " + top + ", right = " + right + ", bottom = " + bottom);
                childView.layout(left, top, right, bottom);
                curLeft += childView.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
            }
            // 下一行
            curLeft = getPaddingLeft();
            curTop += iCurLineH;
        }
    }

}
