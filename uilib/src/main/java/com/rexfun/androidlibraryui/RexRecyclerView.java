package com.rexfun.androidlibraryui;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by mac373 on 15/12/14.
 */
public class RexRecyclerView extends RecyclerView{
    private Context ctx;
    private int lastVisibleItem;
    private float y_tmp1, y_tmp2;
    private static final int PULL_UP = 0;
    private static final int PULL_DOWN = 1;
    private static int PULL_DIRECTION;
    private PullUpRefreshListener mListener;

    public RexRecyclerView(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
        this.ctx = ctx;
        this.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItem + 1 == getAdapter().getItemCount() && PULL_DIRECTION == PULL_UP) {
                    mListener.pullUpToRefresh();
                }
            }
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (RexRecyclerView.this.getLayoutManager() instanceof LinearLayoutManager) {
                    lastVisibleItem = ((LinearLayoutManager) RexRecyclerView.this.getLayoutManager()).findLastVisibleItemPosition();
                } else if (RexRecyclerView.this.getLayoutManager() instanceof GridLayoutManager) {
                    lastVisibleItem = ((GridLayoutManager) RexRecyclerView.this.getLayoutManager()).findLastVisibleItemPosition();
                } else if (RexRecyclerView.this.getLayoutManager() instanceof StaggeredGridLayoutManager) {
                    int[] lastPositions = new int[((StaggeredGridLayoutManager) RexRecyclerView.this.getLayoutManager()).getSpanCount()];
                    ((StaggeredGridLayoutManager) RexRecyclerView.this.getLayoutManager()).findLastVisibleItemPositions(lastPositions);
                    lastVisibleItem = findMax(lastPositions);
                }
            }
        });
    }

    /**
     * 判断是向上还是向下的滑动方向
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent event){
        //获取当前坐标
        float y = event.getY();
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                y_tmp1 = y;
                break;
            case MotionEvent.ACTION_UP:
                y_tmp2 = y;
                System.out.println("滑动参值 y1=" + y_tmp1 + "; y2=" + y_tmp2);
                if(y_tmp1 != 0){
                    if(y_tmp1 - y_tmp2 > 8){
                        PULL_DIRECTION = PULL_UP;
                        System.out.println("pull_up");
                    }
                    if(y_tmp2 - y_tmp1 > 8){
                        PULL_DIRECTION = PULL_DOWN;
                        System.out.println("pull_down");
                    }
                }
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    private int findMax(int[] lastPositions) {
        int max = lastPositions[0];
        for (int value : lastPositions) {
            if (value > max) {
                max = value;
            }
        }
        return max;
    }

    /**
     * 上拉刷新监听器
     * @param listener
     */
    public void setOnPullUpRefreshListener(PullUpRefreshListener listener)
    {
        this.mListener = listener;
    }

    public interface PullUpRefreshListener
    {
        public void pullUpToRefresh();
    }
}
