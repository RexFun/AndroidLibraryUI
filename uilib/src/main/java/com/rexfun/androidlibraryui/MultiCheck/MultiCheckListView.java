package com.rexfun.androidlibraryui.MultiCheck;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.rexfun.androidlibraryorm.BaseModel;
import com.rexfun.androidlibraryui.R;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class MultiCheckListView extends ListView 
{
	private Context ctx;
	private List<BaseModel> dataList = new ArrayList<BaseModel>();//数据集
	/*MultiCheck属性**********/
	private MultiCheckAdapter adapter;//自定义适配器
	private List<Long> idList = new ArrayList<Long>();//主键集合
    private int checkNum;//多选模式下，被选中个数
    private boolean isMultiChoose = false;//判断是否多选模式 （默认false）
    private ActionMode mMode;//ActionMode
	private MultiCheckActionModeListener listener;//MultiCheckActionMode监听器
	private OnItemClickNotMultiListener itemClickNotMultiListener;//item项单击监听器（非多选模式下）
	/*PullRefresh属性**********/
	private Boolean hasFooterView = false;//是否已有底部布局
	private RelativeLayout footerView;//底部布局
	private LinearLayout moretipsView;//更多...
	private LinearLayout loadingView;//上拉刷新进度条
	private int MaxDataNum;//最大数据条数
	private int AvgDataNum = 10;//每次获取数据数
	private int PerDataNum = 20;//每秒获取数据数
	private int CurDataNum;//已取数据条数
	private int LastDataNum;//剩余数据条数
    private int lastVisibleIndex;// 最后可见条目的索引
    private int firstVisibleIndex;// 第一可见条目的索引
    private Handler handler;
    private PullUpToRefreshListener uListener;//上拉刷新监听器
    
    public MultiCheckListView(Context ctx, AttributeSet attrs) 
    {
    	super(ctx, attrs);
    	this.ctx = ctx;
    	initFooterView();
    }
    
    /*MultiCheck方法**********/
    /**
     * 初始化多选模式
     * @param _dataList 数据集合
     * @param _adapter 自定义适配器（必须继承自MultiCheckAdapter）
     */
	public void init(List _dataList, MultiCheckAdapter _adapter)
	{
		this.dataList = _dataList;
		this.adapter = _adapter;
		this.setAdapter(adapter);
		//初始化列表数据和监听事件
		this.setOnItemClickListener(new MyOnItemClickListener());//单击事件
		this.setOnItemLongClickListener(new MyOnItemLongClickListener());//长按事件
		initFooterView();
	}
	
	/**
	 * 切换多选模式
	 * @param b (true:启用；false:关闭)
	 */
	public void isMultiMode(boolean b)
	{
		isMultiChoose = b;
		if(!isMultiChoose) noCheckAll();//若非多选模式，隐藏多选CheckBox，勾掉所有列表项的CheckBox
    	adapter.setIsMultiChoose(isMultiChoose);
    	adapter.notifyDataSetChanged();
	}
	
	/**
	 * 获取主键值集合
	 * @return List<Long>
	 */
	public List<Long> getIdList()
	{
		return this.idList;
	}
	/**
	 * 获取主键值数组
	 * @return Long[]
	 */
	public Long[] getIdArray()
	{
		Long[] ids = new Long[this.idList.size()];
		for(int i=0; i<this.idList.size(); i++)
		{
			ids[i] = this.idList.get(i);
			System.out.println("id_arr["+i+"]:"+ids[i]);
		}
		return ids;
	}
	/**
	 * 获取主键值集合字符串，以逗号隔开
	 * @return String
	 */
	public String getIds()
	{
		String ids = "";
		for(int i=0; i<this.idList.size(); i++)
		{
			ids += String.valueOf(idList.get(i)) + (i+1<idList.size()?",":"");
		}
		Log.i("ids is :", ids);
		return ids;
	}
	
	/**
	 * 刷新列表
	 * @param _dataList 数据集合
	 */
	public void refreshListView(List _dataList)
	{
		if(mMode!=null)noCheckAll();
		dataList.clear();
		dataList.addAll(_dataList);
		adapter.notifyDataSetChanged();
//		this.setAdapter(adapter);//刷新adapter
	}
	
	/**
	 * 切换全选/全不选（用于MenuItem）
	 * @param item
	 */
	public void toggleChecked(MenuItem item)
	{
		idList.clear();
		if(item.isChecked())
		{
			item.setChecked(false);
			item.setIcon(R.drawable.btn_check_off_holo_light);
			noCheckAll();
		}
		else 
		{
			item.setChecked(true);
			item.setIcon(R.drawable.btn_check_on_holo_light);
			CheckAll();
		}
		adapter.notifyDataSetChanged();
	}
	
	/**
	 * 设置快速滚动条图片
	 * @param _drawable
	 */
	public void setFastScrollDrawable(int _drawable)
	{
		try { 
			Field f = AbsListView.class.getDeclaredField("mFastScroller"); 
			f.setAccessible(true); 
			Object o=f.get(this); 
			f=f.getType().getDeclaredField("mThumbDrawable"); 
			f.setAccessible(true); 
			Drawable drawable=(Drawable) f.get(o); 
			drawable=getResources().getDrawable(_drawable); 
			f.set(o,drawable); 
		} catch (Exception e) { 
			throw new RuntimeException(e); 
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) 
	{
		//键盘返回键
		if (keyCode == KeyEvent.KEYCODE_BACK )
        {
			if(isMultiChoose)
			{
				isMultiMode(false);
			}
        }
		return super.onKeyDown(keyCode, event);
	}
	
	//listView单击item监听类
	private class MyOnItemClickListener implements OnItemClickListener
	{
		@Override
		public void onItemClick(AdapterView<?> arg0, View v, int pos, long arg3) 
		{
            if(isMultiChoose)
            {//多选模式，单击选中
            	checkOne(v, pos);
            }
            else
            {//非多选模式，由用户实现接口
            	itemClickNotMultiListener.onClick(v);
            } 
		}
	}
	//listView长按item监听类
	private class MyOnItemLongClickListener implements OnItemLongClickListener
	{
		@Override
		public boolean onItemLongClick(AdapterView<?> arg0, View v, int pos, long arg3) 
		{
			if(null==mMode)
			{
				mMode = startActionMode(listener.getActionModeCallback());//启动ActionMode
				isMultiMode(true);//设为多选模式
				checkOne(v, pos);//选中一项
				return true;
			}
			else
			{
				mMode.finish();//关闭ActionMode
				mMode = null;
				isMultiMode(false);//设为非多选模式
				return false;
			}
		}
	}
	
	//单选
	private void checkOne(View v, int pos)
	{
		// 取得ViewCache对象，这样就省去了通过层层的findViewById去实例化我们需要的cb实例的步骤    
    	ViewCache holder = (ViewCache) v.getTag();    
        // 改变CheckBox的状态    
        holder.chk.toggle();    
        // 将CheckBox的选中状况记录下来    
        adapter.getIsSelected().put(pos, holder.chk.isChecked());     
        // 调整选定条目    
        if (holder.chk.isChecked() == true) 
        {
        	v.setSelected(true);
        	checkNum++;
        	idList.add(Long.parseLong(String.valueOf(holder.idView.getText())));
        }
        else 
    	{
        	v.setSelected(false);
        	checkNum--;
        	idList.remove(Long.parseLong(String.valueOf(holder.idView.getText())));
    	}
        mMode.setSubtitle(checkNum+" selected");		
	}
	
	//全选
	private void CheckAll()
	{
		HashMap<Integer, Boolean> isSelected = new HashMap<Integer, Boolean>();      
        for (int i = 0; i < dataList.size(); i++)
        {
        	isSelected.put(i, true);
        	idList.add(dataList.get(i).getId());
        }
        adapter.setIsSelected(isSelected);
        checkNum = dataList.size();
        mMode.setSubtitle(checkNum+" selected");
	}
	
	//全不选
	private void noCheckAll()
	{
		HashMap<Integer, Boolean> isSelected = new HashMap<Integer, Boolean>();      
        for (int i = 0; i < dataList.size(); i++) isSelected.put(i, false);  
        adapter.setIsSelected(isSelected);
        idList.clear();
        checkNum = 0;
        if(mMode!=null)mMode.setSubtitle(checkNum+" selected");
	}
	
	//视图缓存类
	public static class ViewCache
	{
		public CheckBox chk;
		public TextView idView;
		public ImageButton itemMenu;
	}
	
	/**
	 * ListView项单击监听接口（非多选模式）
	 * @param listener OnItemClickNotMultiListener对象
	 */
	public void setOnItemClickNotMultiListener(OnItemClickNotMultiListener listener)
	{
		this.itemClickNotMultiListener = listener;
	}
	public interface OnItemClickNotMultiListener
	{
		public void onClick(View v);
	}
	
	/**
	 * ActionMode监听器
	 * @param listener MultiCheckActionModeListener对象
	 */
	public void setMultiCheckActionModeListener(MultiCheckActionModeListener listener)
	{
		this.listener = listener;
	}
	public interface MultiCheckActionModeListener
	{
		/**
		 * 实列化MultiCheckActionMode
		 * @return MultiCheckActionMode对象
		 */
		public Callback getActionModeCallback();
		/**
		 * ActionItem点击事件
		 * @param mode ActionMode对象
		 * @param item MenuItem对象
		 * @return
		 */
		public boolean onActionItemClicked(ActionMode mode, MenuItem item);
	}
	
	/*PullRefresh方法**********/
	/**
	 * 获取最大数据条数
	 * @return
	 */
	public int getMaxDataNum() {
		return MaxDataNum;
	}
	/**
	 * 设置最大数据条数
	 * @return
	 */
	public void setMaxDataNum(int maxDataNum) {
		MaxDataNum = maxDataNum;
	}
	/**
	 * 获取每秒读取数据数
	 * @return
	 */
	public int getPerDataNum() {
		return PerDataNum;
	}
	/**
	 * 设置每秒读取数据数
	 * @return
	 */
	public void setPerDataNum(int perDataNum) {
		PerDataNum = perDataNum;
	}
	/**
	 * 获取每次读取数据条数
	 * @return
	 */
	public int getAvgDataNum() {
		return AvgDataNum;
	}
	/**
	 * 设置每次读取数据条数
	 * @return
	 */
	public void setAvgDataNum(int avgDataNum) {
		AvgDataNum = avgDataNum;
	}
	/**
	 * 获取当前显示数据条数
	 * @return
	 */
	public int getCurDataNum() {
		return CurDataNum = adapter.getCount();
	}
	/**
	 * 获取剩余数据条数
	 * @return
	 */
	public int getLastDataNum() {
		return LastDataNum = MaxDataNum - getCurDataNum();
	}
	/**
	 * 获取下次加载数据条数
	 * @return
	 */
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
	/**
	 * 添加item
	 * @param item
	 */
	public void addDataItem(BaseModel item)
	{
		this.dataList.add(item);
	}
	/**
	 * 初始化底部上拉刷新视图
	 */
	private void initFooterView()
	{
		handler = new Handler();
		drawFooterView();
		//滑动加载数据事件
		super.setOnScrollListener(new OnScrollListener()
		{
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
			{
				firstVisibleIndex = firstVisibleItem;// 计算第一可见条目的索引
		        lastVisibleIndex = firstVisibleItem + visibleItemCount - 1;// 计算最后可见条目的索引
			}
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) 
			{
		        if (lastVisibleIndex==adapter.getCount() && scrollState==OnScrollListener.SCROLL_STATE_IDLE) 
		        {//上拉(滑到底部后自动加载，判断listview已经停止滚动并且最后可视的条目等于adapter的条目)
			        if(MaxDataNum>adapter.getCount())
			        {
			        	pullUpDelalyLoad();
			        	footerView.setVisibility(VISIBLE);
					}
			        else
			        {
			        	footerView.setVisibility(GONE);
			            Toast.makeText(ctx, "到底了！", Toast.LENGTH_SHORT).show();
					}
		        }
			}
		});
	}
	//画底部上拉刷新视图
	private void drawFooterView()
	{
		if(!hasFooterView)
		{
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			footerView = (RelativeLayout) inflater.inflate(R.layout.footer_refresh, null);
			footerView.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v){
					pullUpDelalyLoad();
					return;
				}
			});
			moretipsView = (LinearLayout) footerView.findViewById(R.id.moretips);
			loadingView = (LinearLayout) footerView.findViewById(R.id.loading);
			addFooterView(footerView);
			hasFooterView = true;
		}
		footerView.setVisibility(VISIBLE);
	}
	//上拉加载数据
	private void pullUpDelalyLoad()
	{
		moretipsView.setVisibility(GONE);
		loadingView.setVisibility(VISIBLE);
		handler.postDelayed(new Runnable()
		{
			@Override
			public void run() 
			{
				uListener.pullUpToRefresh();
				if(MaxDataNum == CurDataNum){
					TextView _tips = (TextView)moretipsView.findViewById(R.id.tips);
					_tips.setText(R.string.footer_clickToRefresh);
				}
				moretipsView.setVisibility(VISIBLE);
				loadingView.setVisibility(GONE);
				adapter.notifyDataSetChanged();
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
}
