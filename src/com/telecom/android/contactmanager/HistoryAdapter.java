package com.telecom.android.contactmanager;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.telecom.android.base.HistoryBean;

/**
 * 最近联系人适配器.
 * 
 * @author lsq
 * 
 */
public class HistoryAdapter extends BaseAdapter {
	private LayoutInflater mInflater;
	ArrayList<HistoryBean> data;

	public HistoryAdapter(Context ct, ArrayList<HistoryBean> data) {
		mInflater = LayoutInflater.from(ct);
		this.data = data;
	}

	public ArrayList<HistoryBean> getItemList() {
		return data;
	}

	public void setItemList(ArrayList<HistoryBean> data) {
		this.data = data;
	}

	public int getCount() {
		return data.size();
	}

	public Object getItem(int position) {
		return position;
	}

	public long getItemId(int position) {
		return position;
	}

	static class ViewHolder {
		CheckBox checkBox;
		TextView name;
		TextView phone;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		HistoryBean map = data.get(position);
		ViewHolder viewHolder = null;
		if (convertView == null) {
			convertView = mInflater
					.inflate(R.layout.item_contact_checked, null);

			viewHolder = new ViewHolder();
			viewHolder.checkBox = (CheckBox) convertView
					.findViewById(R.id.contactitem_select_cb);
			viewHolder.name = (TextView) convertView
					.findViewById(R.id.item_name);
			viewHolder.phone = (TextView) convertView
					.findViewById(R.id.item_phone);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		if (map != null) {
			viewHolder.checkBox.setTag(map);
			viewHolder.checkBox.setClickable(false);
			viewHolder.checkBox.setChecked(map.checked);
			viewHolder.phone.setText(map.number);
			viewHolder.name.setText(map.displayName);
		}
		return convertView;
	}

	public ArrayList<HistoryBean> getData() {
		return data;
	}

}