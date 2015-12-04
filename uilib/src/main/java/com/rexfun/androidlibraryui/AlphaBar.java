package com.rexfun.androidlibraryui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import net.sourceforge.pinyin4j.PinyinHelper;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AlphaBar extends View 
{
	private Map<String, Integer> alphaMap = new HashMap<String, Integer>();
    private OnAlphaBarListener listener;
    private String bg = "#F2FFFF";
    private boolean showbg = false;
    private Paint paint = new Paint();
    private String sort = "abcdefghijklmnopqrstuvwxyz#";
    private int choose = -1;

    public AlphaBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public AlphaBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AlphaBar(Context context) {
        super(context);
    }
    
    /**
     * 初始化字母映射alphaMap
     * @param list 数据集
     * @param listType list的类型(0:List(String); 1:List(Model); 2:List(Map))
     * @param sortkey listType=0时，传null
     * @param mysort 自定义排序规则（传null则采用默认规则）
     */
    public void initAlphaMap(List list, int listType, String sortkey, String mysort)
    {
    	sort = mysort!=null?mysort:sort;
    	//<1> 定义按sort排序的alphaGroup
    	Map<String, List> alphaGroup = new HashMap<String, List>();
    	for(int i=0; i<sort.length(); i++)
    	{
    		alphaGroup.put(String.valueOf(sort.charAt(i)), new ArrayList());
    	}
    	//<2> list按alphaGroup分组
    	switch(listType)
    	{
	    	case 0://List<String>
	    		for(int i=0; i<list.size(); i++)
	    		{//遍历list获取每条sortkey首字母，并将list按字母分组
	    			try
	    			{
	    				groupingList(alphaGroup, list.get(i), list.get(i));
	    			}
	    			catch(Exception e)
	    			{
	    				e.printStackTrace();
	    			}
	    		}
	    		break;
	    	case 1://List<Model>
	    		for(int i=0; i<list.size(); i++)
	    		{//遍历list获取每条sortkey首字母，并将list按字母分组
	    			try
	    			{
		    			Method m = list.get(i).getClass().getMethod("get"+sortkey.substring(0,1).toUpperCase()+sortkey.substring(1));
		    			groupingList(alphaGroup, m.invoke(list.get(i)), list.get(i));
	    			}
	    			catch(Exception e)
	    			{
	    				e.printStackTrace();
	    			}
	    		}
	    		break;
	    	case 2://List<Map>
	    		for(int i=0; i<list.size(); i++)
	    		{//遍历list获取每条sortkey首字母，并将list按字母分组
	    			try
	    			{
	    				groupingList(alphaGroup, ((Map)list.get(i)).get(sortkey), list.get(i));
	    			}
	    			catch(Exception e)
	    			{
	    				e.printStackTrace();
	    			}
	    		}
	    		break;
    	}
		//<3> 按sort生成有序的list
		list.clear();
		for(int i=0; i<sort.length(); i++)
		{
			String _alphaKey = String.valueOf(sort.charAt(i));
			alphaMap.put(_alphaKey, list.size());
			list.addAll(alphaGroup.get(_alphaKey));
		}
    }
    //为list分组
    private void groupingList(Map<String, List> alphaGroup, Object sortkey, Object listItem)
    {
		String curAlpha = String.valueOf(PinyinHelper.toHanyuPinyinStringArray(String.valueOf(sortkey).charAt(0))[0].charAt(0));
		if(sort.contains(curAlpha))
		{//a~z
			alphaGroup.get(curAlpha).add(listItem);
		}
		else
		{//#
			alphaGroup.get(String.valueOf(sort.charAt(sort.length()-1))).add(listItem);
		}
    }
    
    /**
     * 获取首字母所在位置
     * @param s 字母
     * @return int
     */
    public int getAlphaPos(String s)
    {
    	Integer pos = alphaMap.get(s);
    	System.out.println("pos:"+pos);
    	return pos!=null?pos:-1;
    }
    
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        int item = (int) (event.getY() / getHeight() * sort.length());
        System.out.println("item:"+item);
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            if (item >= 0 && item < sort.length()){
                showbg = true;
                choose = item;
                invalidate();
                if (listener != null) {
                	System.out.println("b["+item+"]:"+sort.charAt(item));
                	listener.getStr(String.valueOf(sort.charAt(item)));
                }
            }
            break;
        case MotionEvent.ACTION_MOVE:
            if (item >= 0 && item < sort.length()){
                showbg = true;
                choose = item;
                invalidate();
                if (listener != null) {
                	listener.getStr(String.valueOf(sort.charAt(item)));
                }
            }
            break;
        case MotionEvent.ACTION_UP:
            showbg = false;
            choose = -1;
            invalidate();
            if (listener != null) {
            	listener.getStr(String.valueOf(sort.charAt(item)));
            }
            break;
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int singleHeight = getHeight() / sort.length();
        if (showbg) {
            canvas.drawColor(Color.parseColor(bg));
        }
        for (int i = 0; i < sort.length(); i++) {
            paint.setColor(Color.GRAY);
            paint.setAntiAlias(true);
            paint.setTextSize(25);
            float textWidth = paint.measureText(String.valueOf(sort.charAt(i)));
            if (i == choose) {
                paint.setColor(Color.RED);
                paint.setFakeBoldText(true);
            }
            canvas.drawText(String.valueOf(sort.charAt(i)), (getWidth() - textWidth) / 2, (i + 1)
                    * singleHeight, paint);
            paint.reset();
        }
        super.onDraw(canvas);
    }

    public void setAlphaBarListener(OnAlphaBarListener listener) {
        this.listener = listener;
    }

    public interface OnAlphaBarListener {
        public void getStr(String str);
    }
}
