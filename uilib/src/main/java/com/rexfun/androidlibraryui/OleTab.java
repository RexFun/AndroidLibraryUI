package com.rexfun.androidlibraryui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("ResourceAsColor")
public class OleTab extends LinearLayout
{
	private Context ctx;    
	private LinearLayout titleBar;//标题布局
	private LinearLayout scrollBar;//滚动条布局
	private ImageView scrollLine;//滚动条图片
	private List<TextView> tabTitles = new ArrayList<TextView>();//页签标题
	private int tabBgColor = getResources().getColor(android.R.color.white);//OleTab底色
	private int titleColor = Color.GRAY;//标题字体颜色（未选中）
	private int curTitleColor = Color.GREEN;//标题字体颜色（选中）
	private int titleBarBgColor = getResources().getColor(android.R.color.white);//标题行底色
	private int titleHeight = 80;//标题行高
	private int titleSize = 18;//标题字体大小
	private Drawable scrollLineColor = getResources().getDrawable(R.color.green);//滚动条颜色
    private int currIndex = 0;//页签索引
    private int[] xOffset;//滚动条针对每个页签的偏移坐标
    private OnOleTabListener listener;
    
	public OleTab(Context ctx, AttributeSet attrs)
	{
		super(ctx, attrs);
		this.ctx = ctx;
	}
	
	/**
	 * OleTab底色
	 * @param tabBgColor
	 * 例:getResources().getColor(android.R.color.black)
	 */
	public void setOleTabBgColor(int tabBgColor)
	{
		this.tabBgColor = tabBgColor;
	}
	/**
	 * 标题字体颜色(未选中)
	 * @param titleColor
	 * 例:getResources().getColor(android.R.color.black)
	 */
	public void setOleTabTitleColor(int titleColor) {
		this.titleColor = titleColor;
	}
	/**
	 * 标题字体颜色(选中)
	 * @param curTitleColor
	 * 例:getResources().getColor(android.R.color.black)
	 */
	public void setOleTabCurTitleColor(int curTitleColor) {
		this.curTitleColor = curTitleColor;
	}
	/**
	 * 标题行底色
	 * @param titleBarBgColor
	 * 例:getResources().getColor(android.R.color.black)
	 */
	public void setOleTabTitleBarBgColor(int titleBarBgColor) {
		this.titleBarBgColor = titleBarBgColor;
	}
	/**
	 * 标题行高
	 * @param titleHeight
	 */
	public void setOleTabTitleHeight(int titleHeight) {
		this.titleHeight = titleHeight;
	}
	/**
	 * 滚动条颜色
	 * @param scrollLineColor
	 * 例:getResources().getDrawable(android.R.color.black)
	 */
	public void setOleTabScrollLineColor(Drawable scrollLineColor) {
		this.scrollLineColor = scrollLineColor;
	}
	/**
	 * 标题字体大小
	 * @param titleSize
	 */
	public void setOleTabTitleSize(int titleSize) {
		this.titleSize = titleSize;
	}
	
	public void addTabTitle(String[] titles)
	{
		for(int i=0; i<titles.length; i++)
		{
			TextView _tv = new TextView(ctx);
			_tv.setId(i);//id作为tab索引
			_tv.setText(titles[i]);
			_tv.setGravity(Gravity.CENTER);
			_tv.setTextColor(i==0?curTitleColor:titleColor);
			_tv.setTextSize(titleSize);
			_tv.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v) 
				{
					listener.onTabSelected(v.getId());
				}
			});
			tabTitles.add(_tv);
		}
		initTab();
		initScrollLine();
	}
	
	private void initTab()
	{
		//OleTab底色
		this.setBackgroundColor(tabBgColor);
		//创建标题布局
		titleBar = new LinearLayout(ctx);
		titleBar.setOrientation(LinearLayout.HORIZONTAL);
		titleBar.setBackgroundColor(titleBarBgColor);
		titleBar.setPadding(0, 10, 0, 5);
		//添加标题
		for(TextView tv : tabTitles)
		{
			titleBar.addView(tv,new LayoutParams(0, titleHeight, 1.0f));
		}
		this.addView(titleBar);
	}
	
	private void initScrollLine()
	{
		//创建滚动条布局
		scrollBar = new LinearLayout(ctx);
		scrollBar.setOrientation(LinearLayout.VERTICAL);
		scrollBar.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 8));
		scrollBar.setGravity(Gravity.BOTTOM);
		//添加滚动条图片
		scrollLine = new ImageView(ctx);
		DisplayMetrics dm = getResources().getDisplayMetrics();
        int screenW = dm.widthPixels;//获取屏幕宽度
        int scrollW = screenW/tabTitles.size();//获取滚动条宽度
        scrollLine.setScaleType(ScaleType.MATRIX);
        scrollLine.setImageDrawable(scrollLineColor);//用白色填充滚动条
        scrollLine.setLayoutParams(new LayoutParams(scrollW, LayoutParams.MATCH_PARENT));//设置滚动条宽高
        scrollBar.addView(scrollLine);
        this.addView(scrollBar);
        //计算滚动条坐标
        xOffset = new int[tabTitles.size()];
        for(int i=0;i<xOffset.length;i++)
        {
        	xOffset[i] = i*scrollW;
        }
	}

	public void setScrollPos(int index)
	{
		TextView tv = null;
		//标题变色
		tv = (TextView) titleBar.findViewById(index);
		tv.setTextColor(curTitleColor);
		for(int i=0;i<tabTitles.size();i++)
		{
			if(i!=index)
			{
				tv = (TextView) titleBar.findViewById(i);
				tv.setTextColor(titleColor);
			}
		}
		//滚动条偏移
		Animation animation = null;
		animation = new TranslateAnimation(xOffset[currIndex], xOffset[index], 0, 0);
		currIndex = index;
		animation.setFillAfter(true);
		animation.setDuration(300);
		scrollLine.startAnimation(animation);
	}
	
	public void setOnOleTabListener(OnOleTabListener listener) {
		this.listener = listener;
	}

	public interface OnOleTabListener
	{
		public void onTabSelected(int pos);
	}
}
