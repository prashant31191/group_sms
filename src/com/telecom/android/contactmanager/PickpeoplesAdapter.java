package com.telecom.android.contactmanager;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;

import com.telecom.android.base.PickPeople;
import com.telecom.android.contactmanager.SendConfirm.ViewHolder;

/**
 * 选择的人员的适配器,用于显示在gridView中.
 * 
 * @author lsq
 * 
 */
public class PickpeoplesAdapter extends BaseAdapter {
	private ArrayList<PickPeople> names;
	private LayoutInflater mInflater;
	private boolean candelete;

	public PickpeoplesAdapter(Context ct, ArrayList<PickPeople> names,
			boolean candelete) {
		mInflater = LayoutInflater.from(ct);
		this.names = names;
		this.candelete = candelete;
	}

	@Override
	public int getCount() {
		return names.size();
	}

	@Override
	public Object getItem(int position) {
		return names.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	private String TAG = "PickpeopleAdapter";

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		PickPeople map = names.get(position);
		ViewHolder viewHolder = null;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.item_pickpeople, null);
			viewHolder = new ViewHolder();
			viewHolder.pickPeople = (Button) convertView
					.findViewById(R.id.people);
			viewHolder.deleteImg = (ImageView) convertView
					.findViewById(R.id.deleteThis);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		if (candelete)
			viewHolder.deleteImg.setTag(map);
		else
			viewHolder.deleteImg.setVisibility(View.GONE);
		viewHolder.pickPeople.setTag(map);
		viewHolder.pickPeople.setText(map.name);
		return convertView;
	}
}