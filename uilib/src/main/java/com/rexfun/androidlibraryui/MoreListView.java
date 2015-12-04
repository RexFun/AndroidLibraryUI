package com.rexfun.androidlibraryui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MoreListView extends ListView 
{
	private Context ctx;
	private List<HashMap<String,Object>> data;
	private BaseAdapter adapter;
	private LinearLayout headerView;//顶部布局
	private LinearLayout inHeaderView;//headerView内的布局
	private TextView RefreshTips;//刷新更多...
	private LinearLayout footerView;//底部布局
	private LinearLayout inFooterView;//footerView内的布局
	private TextView MoreTips;//更多...
	private ProgressBar downRefreshPb;
	private ProgressBar upRefreshPb;
	private PullUpToRefreshListener uListener;
	private PullDownToRefreshListener dListener;
	private Handler handler;
	private int MaxDataNum;//最大数据条数
	private int AvgDataNum = 10;//每次获取数据数
	private int PerDataNum = 20;//每秒获取数据数
	private int CurDataNum;//已取数据条数
	private int LastDataNum;//剩余数据条数
    private int lastVisibleIndex;// 最后可见条目的索引
    private int firstVisibleIndex;// 第一可见条目的索引
	
	//采用findById方式实例化控件，需用以下方式定义控件类构造函数
	public MoreListView(Context ctx, AttributeSet attrs) {
		super(ctx, attrs);
		this.ctx = ctx;
		initView();
	}
	//获取与设置定义适配器(SimpleAdapter)时所需要的List<HashMap<String, Object>>集合
	public List<HashMap<String, Object>> getData() {
		return data;
	}
	public void setData(List<HashMap<String, Object>> data) {
		this.data = data;
	}
	public void addDataItem(HashMap<String, Object> item)
	{
		this.data.add(item);
	}
	//设置适配器
	public void setAdapter(BaseAdapter adapter)
	{
		this.adapter = adapter;
		super.setAdapter(adapter);
	}
	//获取与设置最大数据条数
	public int getMaxDataNum() {
		return MaxDataNum;
	}
	public void setMaxDataNum(int maxDataNum) {
		MaxDataNum = maxDataNum;
	}
	//获取与设置每秒获取数据数
	public int getPerDataNum() {
		return PerDataNum;
	}
	public void setPerDataNum(int perDataNum) {
		PerDataNum = perDataNum;
	}
	//获取与设置每次获取数据条数
	public int getAvgDataNum() {
		return AvgDataNum;
	}
	public void setAvgDataNum(int avgDataNum) {
		AvgDataNum = avgDataNum;
	}
	//获取当前显示数据条数
	public int getCurDataNum() {
		return CurDataNum = adapter.getCount();
	}
	//获取剩余数据条数
	public int getLastDataNum() {
		return LastDataNum = MaxDataNum - getCurDataNum();
	}
	//获取下次加载数据条数
	public int getLoadDataNum()
	{
		if(getLastDataNum() > 0)
		{
	    	if(LastDataNum > AvgDataNum)
	    		return AvgDataNum;
	    	else
	    		return LastDataNum;
		}
		return 0;
	}
	
	//初始化视图
	private void initView()
	{
		handler = new Handler();
		data = new ArrayList<HashMap<String,Object>>();//初始化data
		initHeaderView();//画header布局
		initFooterView();//画footer布局
		//滑动加载数据
		super.setOnScrollListener(new OnScrollListener()
		{
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
			{
				firstVisibleIndex = firstVisibleItem;// 计算第一可见条目的索引
		        lastVisibleIndex = firstVisibleItem + visibleItemCount - 1;// 计算最后可见条目的索引
		        // 所有的条目已经和最大条数相等，则移除底部的View
		        if (totalItemCount == MaxDataNum + 1) 
		        {
		        	MoreListView.this.removeFooterView(footerView);
		            Toast.makeText(ctx, "数据全部加载完成，没有更多数据！", Toast.LENGTH_LONG).show();
		        }
			}
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) 
			{
		        if (lastVisibleIndex == (adapter.getCount()+1) && scrollState == OnScrollListener.SCROLL_STATE_IDLE) 
		        {//上拉(滑到底部后自动加载，判断listview已经停止滚动并且最后可视的条目等于adapter的条目)
		        	pullUpDelalyLoad();
		        }
		        else if(firstVisibleIndex == 0 && scrollState == OnScrollListener.SCROLL_STATE_IDLE)
		        {//下拉
		        	pullDownDelalyLoad();
		        }
			}
		});
//    	pullDownDelalyLoad();
	}
	private void initHeaderView()
	{
		//创建顶部布局
		headerView = new LinearLayout(ctx);
		headerView.setOrientation(LinearLayout.VERTICAL);
		headerView.setGravity(Gravity.CENTER_HORIZONTAL);
		//创建"刷新更多..."提示
		RefreshTips = new TextView(ctx);
		RefreshTips.setText("↓ 释放更多...");
		inHeaderView = new LinearLayout(ctx);
		inHeaderView.setGravity(Gravity.CENTER);
		ViewGroup.LayoutParams RefreshTipsParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		inHeaderView.addView(RefreshTips, RefreshTipsParams);
		ViewGroup.LayoutParams headerViewParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 150);
		headerView.addView(inHeaderView, headerViewParams);
		//创建ProgressBar
		downRefreshPb = new ProgressBar(ctx);
		downRefreshPb.setVisibility(GONE);
		headerView.addView(downRefreshPb, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		//添加顶部布局
		addHeaderView(headerView);
	}
	private void initFooterView()
	{
		//创建底部布局
		footerView = new LinearLayout(ctx);
		footerView.setOrientation(LinearLayout.VERTICAL);
		footerView.setGravity(Gravity.CENTER_HORIZONTAL);
		//创建"更多..."提示
		MoreTips = new TextView(ctx);
		MoreTips.setText("↑ 更多...");
		inFooterView = new LinearLayout(ctx);
		inFooterView.setGravity(Gravity.CENTER);
		ViewGroup.LayoutParams MoreTipsParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		inFooterView.addView(MoreTips, MoreTipsParams);
		ViewGroup.LayoutParams bottomViewParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 150);
		footerView.addView(inFooterView, bottomViewParams);
		//创建ProgressBar
		upRefreshPb = new ProgressBar(ctx);
		upRefreshPb.setVisibility(GONE);
		footerView.addView(upRefreshPb, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		//添加底部布局
		addFooterView(footerView);
	}
	//上拉加载数据
	private void pullUpDelalyLoad()
	{
		inFooterView.setVisibility(View.GONE);
		upRefreshPb.setVisibility(View.VISIBLE);
		handler.postDelayed(new Runnable()
		{
			@Override
			public void run() 
			{
				uListener.pullUpToRefresh();
				inFooterView.setVisibility(View.VISIBLE);
				upRefreshPb.setVisibility(View.GONE);
				adapter.notifyDataSetChanged();
			}
		},1000*(AvgDataNum/PerDataNum));
	}
	//下拉加载数据
	private void pullDownDelalyLoad()
	{
		inHeaderView.setVisibility(View.GONE);
		downRefreshPb.setVisibility(View.VISIBLE);
		handler.postDelayed(new Runnable()
		{
			@Override
			public void run() 
			{
				data.clear();
				dListener.pullDownToRefresh();
				inHeaderView.setVisibility(View.VISIBLE);
				downRefreshPb.setVisibility(View.GONE);
				adapter.notifyDataSetChanged();
				MoreListView.this.setSelection(1);
			}
		},1000*(AvgDataNum/PerDataNum));
	}
	
	/**
	 * 上拉刷新监听器
	 * @param listener
	 */
	public void setPullUpToRefreshListener(PullUpToRefreshListener listener)
	{
		this.uListener = listener;
	}
	
	public interface PullUpToRefreshListener
	{
		public void pullUpToRefresh();
	}
	
	/**
	 * 下拉刷新监听器
	 * @param listener
	 */
	public void setPullDownToRefreshListener(PullDownToRefreshListener listener)
	{
		this.dListener = listener;
	}
	
	public interface PullDownToRefreshListener
	{
		public void pullDownToRefresh();
	}
	
}
