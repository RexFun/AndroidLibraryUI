package com.rexfun.androidlibraryui;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PosPoint extends LinearLayout
{
	private Context ctx;
	private TextView[] points;
	private String shape = "●";
	private String curColor = "#0099cc";
	private String uncurColor = "#DDDDDD";
	private int curColorRes = R.color.blue_light;
	private int uncurColorRes = R.color.gray;
	private int curAlpha = 255;
	private int uncurAlpha = 1;
	private int curpoint = 0;//默认选中第一个指示小圆点
	private boolean isRes = true;//是否使用资源文件设置颜色
	
	public PosPoint(Context ctx, AttributeSet attrs)
	{
		super(ctx, attrs);
		this.ctx = ctx;
	}

	/**
	 * 初始化PosPoint（默认颜色）
	 * @param size 圆点个数
	 */
	public void initPosPoint(int size) 
	{
		this.points = new TextView[size];
		drawPoints();
	}

	/**
	 * 初始化PosPoint（自定义颜色）
	 * @param size 圆点个数
	 * @param shape 圆点形状
	 * @param curColor 当前圆点颜色
	 * @param uncurColor 非当前圆点颜色
	 * @param curAlpha 当前圆点透明度
	 * @param uncurAlpha 非当前圆点透明度
	 */
	public void initPosPoint(int size, String shape, String curColor,String uncurColor, int curAlpha, int uncurAlpha) 
	{
		this.points = new TextView[size];
		this.shape = shape;
		this.curColor = curColor;
		this.uncurColor = uncurColor;
		this.curAlpha = curAlpha;
		this.uncurAlpha = uncurAlpha;
		this.isRes = false;
		drawPoints();
	}
	/**
	 * 初始化PosPoint（自定义颜色）
	 * @param size 圆点个数
	 * @param shape 圆点形状
	 * @param curColorRes 当前圆点颜色
	 * @param uncurColorRes 非当前圆点颜色
	 * @param curAlpha 当前圆点透明度
	 * @param uncurAlpha 非当前圆点透明度
	 */
	public void initPosPoint(int size, String shape, int curColorRes, int uncurColorRes, int curAlpha, int uncurAlpha) 
	{
		this.points = new TextView[size];
		this.shape = shape;
		this.curColorRes = curColorRes;
		this.uncurColorRes = uncurColorRes;
		this.uncurAlpha = uncurAlpha;
		this.isRes = true;
		drawPoints();
	}
	
	//初始化指示小圆点
	private void drawPoints()
	{
		for(int i=0;i<points.length;i++)
		{
			points[i] = new TextView(ctx);
			points[i].setText(shape);
			setPointColor(points[i],i);
			this.addView(points[i]);
		}
	}
	//设置圆点颜色
	private void setPointColor(View point,int index)
	{
		if(index == curpoint)
		{
			points[index].setTextColor(isRes?this.getResources().getColor(curColorRes):Color.parseColor(curColor));
			points[index].setAlpha(1);
		}
		else
		{
			points[index].setTextColor(isRes?this.getResources().getColor(uncurColorRes):Color.parseColor(uncurColor));
			points[index].setAlpha(1);
		}
	}
	
	public void setCurPoint(int pos)
	{
		curpoint = pos;
		for(int i=0;i<points.length;i++)
		{
			setPointColor(points[i],i);
		}
	}
}
