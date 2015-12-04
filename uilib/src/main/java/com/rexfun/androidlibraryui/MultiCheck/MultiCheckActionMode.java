package com.rexfun.androidlibraryui.MultiCheck;

import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class MultiCheckActionMode implements ActionMode.Callback 
{
	private int menuRes;
	private MultiCheckListView lv;
	private MultiCheckListView.MultiCheckActionModeListener listener;

	/**
	 * 构造器
	 * @param listener MultiCheckActionModeListener对象
	 * @param menuRes ActionMode菜单资源
	 * @param lv MultiCheckListView对象
	 */
	public MultiCheckActionMode(MultiCheckListView.MultiCheckActionModeListener listener, int menuRes, MultiCheckListView lv)
	{
		super();
		this.listener = listener;
		this.menuRes = menuRes;
		this.lv = lv;
	}

	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) 
	{
		//填充ActionMode的menu
		MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(menuRes, menu);
		return true;
	}

	@Override
	public boolean onPrepareActionMode(ActionMode mode, Menu menu) 
	{
		return false;
	}

	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item)
	{
		return listener.onActionItemClicked(mode, item);
	}
	
	@Override
	public void onDestroyActionMode(ActionMode mode) 
	{
		lv.isMultiMode(false);//关闭多选模式
	}
}
