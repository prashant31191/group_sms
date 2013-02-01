package com.telecom.android.contactmanager;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.telecom.android.contactmanager.R;
import com.telecom.android.base.ContactBean;

/**
 * 全部联系人的列表适配器.
 * 
 * @author lsq
 * 
 */
public class ContactAdapter extends BaseAdapter implements SectionIndexer {
	private Context ct;
	private LayoutInflater mInflater;
	ArrayList<ContactBean> data, dataFromDb;
	private int allCount, memeberCount;

	public ContactAdapter(Context ct, ArrayList<ContactBean> data,
			int allCount, int memeberCount) {
		mInflater = LayoutInflater.from(ct);
		dataFromDb = new ArrayList<ContactBean>();
		this.allCount = allCount;
		this.memeberCount = memeberCount;
		if (data != null && data.size() > 0) {
			dataFromDb.addAll(data);
		}
		this.ct = ct;
		this.data = data;
	}

	public ArrayList<ContactBean> getItemList() {
		return data;
	}

	public void setItemList(ArrayList<ContactBean> data) {
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
		LinearLayout contactitem_title;
		TextView contactitem_catalog;
	}

	private static String title = "群组成员";

	public View getView(int position, View convertView, ViewGroup parent) {
		ContactBean map = data.get(position);
		ViewHolder viewHolder = null;
		if (convertView == null) {
			convertView = mInflater
					.inflate(R.layout.item_contact_checked, null);

			viewHolder = new ViewHolder();
			viewHolder.checkBox = (CheckBox) convertView
					.findViewById(R.id.contactitem_select_cb);
			viewHolder.name = (TextView) convertView
					.findViewById(R.id.item_name);
			viewHolder.contactitem_title = (LinearLayout) convertView
					.findViewById(R.id.contactitem_title);
			viewHolder.contactitem_catalog = (TextView) convertView
					.findViewById(R.id.contactitem_catalog);
			viewHolder.phone = (TextView) convertView
					.findViewById(R.id.item_phone);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		// 如果是从分组进入的
		if (memeberCount > 0) {
			if (position == 0) {
				viewHolder.contactitem_title.setVisibility(View.VISIBLE);
				viewHolder.contactitem_catalog.setText("群组成员(" + memeberCount
						+ ")");
			} else if (position > 0) {
				ContactBean lastMap = data.get(position - 1);
				if (lastMap.flag != null && map.flag == null) {
					viewHolder.contactitem_title.setVisibility(View.VISIBLE);
					viewHolder.contactitem_catalog.setText("非群组成员(" + allCount
							+ ")");
				} else {
					viewHolder.contactitem_title.setVisibility(View.GONE);
				}
			}
		}
		viewHolder.checkBox.setTag(map);
		viewHolder.checkBox.setChecked(map.checked);
		viewHolder.phone.setText(map.number);
		map.numberWithType = "";
		viewHolder.name.setText(map.displayName);
		return convertView;
	}

	private static String TAG = "ContactAdapter";

	public void search(String searchText) {
		try {
			data.clear();
			if (searchText == null || searchText.trim().equals("")) {
				if (dataFromDb != null && dataFromDb.size() > 0) {
					data.addAll(dataFromDb);
				}
			} else {
				for (ContactBean m : dataFromDb) {
					if (!data.contains(m)) {
						boolean hit = false;
						// 按照名字查询
						if (!hit) {
							String text = m.displayName;
							if (text != null && text.contains(searchText)) {
								hit = true;
							}
						}

						// 按照首字母查询
						if (!hit) {
							String text = m.firstLetter;
							if (text != null
									&& text.contains(searchText.toUpperCase())) {
								hit = true;
							}
						}

						// 按照电话号码查询
						if (!hit) {
							String text = m.number;
							if (text != null
									&& text.contains(searchText.toUpperCase())) {
								hit = true;
							}
						}

						// 按照拼音查询
						if (!hit) {
							String text = m.pinyin;
							if (text != null
									&& text.toString().contains(
											searchText.toUpperCase())) {
								hit = true;
							}
						}
						// 查询电话号码
						if (!hit) {
							String text = m.number;
							if (text != null
									&& text.toString().startsWith(
											searchText.toUpperCase())) {
								hit = true;
							}
						}
						if (hit) {
							data.add(m);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public ArrayList<ContactBean> getData() {
		return data;
	}

	@Override
	public int getPositionForSection(int section) {
		for (int i = 0; i < data.size(); i++) {
			ContactBean map = data.get(i);
			if (map.flag != null)
				continue;
			String l = map.firstLetter;
			if (l != null && !"".equals(l)) { 
				char firstChar = l.toUpperCase().charAt(0);
				if (firstChar == section) {
					return i;
				}
			}
		}
		return -1;
	}

	@Override
	public int getSectionForPosition(int position) {
		return 0;
	}

	@Override
	public Object[] getSections() {
		// TODO Auto-generated method stub
		return null;
	}

}