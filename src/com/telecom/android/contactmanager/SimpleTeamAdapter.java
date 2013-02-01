package com.telecom.android.contactmanager;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.telecom.android.base.ContactBean;
import com.telecom.android.base.PickPeople;
import com.telecom.android.base.TeamBean;

/**
 * 用于进行联系人分组显示的适配器.
 * 
 * @author lsq
 * 
 */
public class SimpleTeamAdapter extends BaseExpandableListAdapter {
	private ArrayList<TeamBean> data;
	private static String TAG = "TeamAdapter";
	private LayoutInflater mChildInflater; // 用于加载listitem的布局xml
	private LayoutInflater mGroupInflater; // 用于加载group的布局xml
	private Context ct;
	private CallBack cb;

	public SimpleTeamAdapter(Context ct, ArrayList<TeamBean> data) {
		this.data = data;
		this.ct = ct;
		this.cb = (CallBack) ct;

		mChildInflater = (LayoutInflater) ct
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mGroupInflater = (LayoutInflater) ct
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

	}

	public ArrayList<TeamBean> getData() {
		return data;
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		if (data.get(groupPosition).users != null)
			return data.get(groupPosition).users.get(childPosition);
		else
			return null;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		if (data.get(groupPosition).users != null)
			return data.get(groupPosition).users.size();
		else
			return 0;
	}

	@Override
	public TeamBean getGroup(int groupPosition) {
		return data.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return data.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	static class ViewHolder {
		CheckBox checkBox;
		TextView name;
		TextView jiantou;
		TextView phone;
		ImageView view;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View view,
			ViewGroup parent) {
		ViewHolder holder = null;
		if (view == null) {
			view = mGroupInflater.inflate(R.layout.item_team2, null);
			holder = new ViewHolder();
			holder.name = (TextView) view.findViewById(R.id.teamname);
			holder.jiantou = (TextView) view.findViewById(R.id.jiantou);
			holder.view = (ImageView) view.findViewById(R.id.edit);
			holder.checkBox = (CheckBox) view.findViewById(R.id.team_select);
			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}
		final TeamBean info = this.data.get(groupPosition);
		if (isExpanded) {
			holder.jiantou.setBackgroundResource(R.drawable.bottom);
		} else {
			holder.jiantou.setBackgroundResource(R.drawable.right);
		}
		if (info != null && !"-1".equals(info.teamId)) {
			holder.name.setText(info.teamName + "(" + info.users.size() + ")");
			holder.checkBox.setTag(info);
			holder.view.setTag(info);
			holder.checkBox.setVisibility(View.VISIBLE);
			holder.name.setVisibility(View.VISIBLE);
			holder.view.setVisibility(View.GONE);
			holder.jiantou.setVisibility(View.VISIBLE);
			holder.checkBox.setChecked(info.checked);
			holder.checkBox.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					TeamBean group = (TeamBean) v.getTag();
					group.setChecked(!group.checked);
					if (group.users != null)
						if (group.checked) {
							for (ContactBean b : group.users) {
								PickPeople p = new PickPeople();
								b.team = info.teamId;
								p.id = b.id;
								p.name = b.displayName;
								p.number = b.number;
								p.teamId = b.team;
								cb.addP(p);
							}
						} else {
							for (ContactBean b : group.users) {
								PickPeople p = new PickPeople();
								b.team = info.teamId;
								p.id = b.id;
								p.name = b.displayName;
								p.number = b.number;
								p.teamId = b.team;
								cb.removeP(p);
							}
						}
					cb.show();
					notifyDataSetChanged();
				}
			});
		} else {
			holder.checkBox.setVisibility(View.GONE);
			holder.name.setVisibility(View.GONE);
			holder.view.setVisibility(View.GONE);
			holder.jiantou.setVisibility(View.GONE);
		}
		return view;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			convertView = mChildInflater.inflate(R.layout.item_team_contact,
					null);
			holder = new ViewHolder();
			holder.name = (TextView) convertView.findViewById(R.id.item_name);
			holder.phone = (TextView) convertView.findViewById(R.id.item_phone);
			holder.checkBox = (CheckBox) convertView
					.findViewById(R.id.contactitem_select_cb);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		ContactBean info = this.data.get(groupPosition).users
				.get(childPosition);
		if (info != null) {
			// 根据模型值设置textview的文本
			holder.name.setText(info.displayName);
			holder.name.setTag(this.data.get(groupPosition));
			holder.phone.setText(info.number);
			holder.checkBox.setChecked(info.checked);
			info.team = this.data.get(groupPosition).teamId;
			holder.checkBox.setTag(info);
		}
		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
}
