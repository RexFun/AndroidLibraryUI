package com.rexfun.androidlibraryui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

public class MoreGridView extends HeaderFooterGridView implements OnScrollListener
{
	private Context ctx;
	private View footerView;
	private int _lastVisibleItem;
	private int _totalItemCount;
	private boolean isLoading = false;
	private OnLoadListener mOnLoadListener;
	
	//采用findById方式实例化控件，需用以下方式定义控件类构造函数
	public MoreGridView(Context ctx, AttributeSet attrs) 
	{
		super(ctx, attrs);
		this.ctx = ctx;
		initView();
	}
	
	public void initView()
	{
		LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		footerView = inflater.inflate(R.layout.footer_more, null);
		addFooterView(footerView);
		footerView.setVisibility(GONE);
		this.setOnScrollListener(this);
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState)
	{
		if(_lastVisibleItem == _totalItemCount && scrollState == OnScrollListener.SCROLL_STATE_IDLE)
		{
			if(!isLoading)
			{
				isLoading = true;
				footerView.setVisibility(VISIBLE);
				mOnLoadListener.onLoad();
			}
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
	{
		_lastVisibleItem = firstVisibleItem + visibleItemCount;
		_totalItemCount = totalItemCount;
	}
	
	public void OnLoadComplete()
	{
		isLoading = false;
		footerView.setVisibility(GONE);
	}
	
    public void setOnLoadListener(OnLoadListener listener) 
    {
    	mOnLoadListener = listener;
    }
	
	public interface OnLoadListener
	{
		public void onLoad();
	}
}
