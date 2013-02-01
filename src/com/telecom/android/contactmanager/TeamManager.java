package com.telecom.android.contactmanager;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;

import com.telecom.android.base.ContactBean;
import com.telecom.android.base.Contants;
import com.telecom.android.base.PickPeople;
import com.telecom.android.base.Task;
import com.telecom.android.base.TeamBean;

/**
 * 进行分组管理的页面.
 * 
 * @author lsq
 * 
 */
public final class TeamManager extends Activity implements
		ExpandableListView.OnGroupClickListener,
		ExpandableListView.OnChildClickListener {
	public static final String TAG = "TeamManager";
	private ExpandableListView teamList;
	private TeamAdapter adapter;
	private ArrayList<TeamBean> data;
	private static final int DIALOG_KEY = 0;
	private Button makesureBtn;

	public void remove(PickPeople id) {
		MyApplication.checkedList.remove(id);
		sendBroadcast(new Intent(Contants.PEOPLE_CHANGED_INTENT_FLITER));
	}

	public void add(PickPeople p) {
		MyApplication.checkedList.add(p);
		sendBroadcast(new Intent(Contants.PEOPLE_CHANGED_INTENT_FLITER));
	}

	private class MyListLoader extends AsyncTask<String, String, String> {

		private boolean showDialog;

		public MyListLoader(boolean showDialog) {
			this.showDialog = showDialog;
		}

		@Override
		protected void onPreExecute() {
			if (showDialog)
				showDialog(DIALOG_KEY);
		}

		public String doInBackground(String... params) {
			data = MyApplication.newGetTeam();
			if (data != null) {
				TeamBean t = new TeamBean();
				t.teamId = "-1";
				data.add(t);
			}
			return "";
		}

		@Override
		public void onPostExecute(String Re) {
			// 绑定LISTVIEW
			if (data.size() == 0) {
				// emptytextView.setVisibility(View.VISIBLE);
			} else {
				if (adapter == null) {
					adapter = new TeamAdapter(TeamManager.this, data);
					teamList.setAdapter(adapter);
				} else {
					adapter.notifyDataSetChanged();
				}
			}
			if (showDialog)
				removeDialog(DIALOG_KEY);
		}
	}

	private ProgressDialog dialog;

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_KEY: {
			dialog = new ProgressDialog(this);
			dialog.setMessage("获取群组信息中...请稍候");
			dialog.setIndeterminate(true);
			dialog.setCancelable(true);
			return dialog;
		}
		}
		return null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (dialog != null) {
			dialog.dismiss();
			dialog = null;
		}
		unregisterReceiver(receiver);
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.team_manager);
		parentActivity = (Task) ViewPagerActivity.mTabActivityInstance;

		teamList = (ExpandableListView) findViewById(R.id.dataList);
		teamList.setOnChildClickListener(this);
		teamList.setGroupIndicator(null);// 去掉默认的左边的箭头
		new MyListLoader(false).execute("");
		makesureBtn = (Button) findViewById(R.id.sure);

		makesureBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(TeamManager.this,
						NewSendManager.class);
				ViewPagerActivity mTabMainActivity = (ViewPagerActivity) getParent();
				mTabMainActivity.setResult(RESULT_OK, intent);
				finish();
			}
		}); 
		IntentFilter mFilter = new IntentFilter(Contants.PEOPLE_CHANGED_INTENT_FLITER);
		mFilter.setPriority(1000);
		receiver = new MyReceiver();
		registerReceiver(receiver, mFilter);
	}

	private MyReceiver receiver;

	private class MyReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			makesureBtn.setText("确定(" + MyApplication.checkedList.size() + ")");
		}
	}

	private Task parentActivity;
	private Button backBtn;

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		ViewPagerActivity.mTabActivityInstance.getWindow().setFeatureInt(
				Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar);
		backBtn = (Button) ViewPagerActivity.mTabActivityInstance
				.findViewById(R.id.back);
		backBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				parentActivity.closeThis();
			}
		});
		String tv1Str = getIntent().getStringExtra("tv1");
		if ("ok".equals(tv1Str)) {
			ViewPager vp = (ViewPager) getParent().findViewById(R.id.vPager);
			ExpandableListView vvv = (ExpandableListView) (vp.getChildAt(1)
					.findViewWithTag("list1"));
			data = MyApplication.newGetTeam();
			if (data != null) {
				TeamBean t = new TeamBean();
				t.teamId = "-1";
				data.add(t);
			}
			adapter = new TeamAdapter(TeamManager.this, data);
			if (vvv != null)
				vvv.setAdapter(adapter);
			adapter.notifyDataSetChanged();
		}
	}

	// 当分组行被点击时，让分组呈现“选中／取消选中”状态。
	@Override
	public boolean onChildClick(ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id) {
		TeamAdapter.ViewHolder holder = (TeamAdapter.ViewHolder) v.getTag();
		holder.checkBox.setChecked(!holder.checkBox.isChecked());
		ContactBean b = (ContactBean) holder.checkBox.getTag();
		PickPeople p = new PickPeople();
		p.id = b.id;
		p.number = b.number;
		p.name = b.displayName;
		if (holder.checkBox.isChecked()) {
			add(p);
		} else {
			remove(p);
		}
		return false;
	}

	@Override
	public boolean onGroupClick(ExpandableListView parent, View v,
			int groupPosition, long id) {
		return false;
	}

	public void newgroup(View view) {
		if (view instanceof Button) {
			Intent intent = new Intent(TeamManager.this, TeamAddManager.class);
			startActivity(intent);
		}
	}

	public void editthis(View view) {
		if (view instanceof ImageView) {
			TeamBean m = (TeamBean) view.getTag();
			Intent intent = new Intent(this, TeamAddManager.class);
			intent.putExtra("name", m.teamName);
			intent.putExtra("id", m.teamId);
			startActivity(intent);
		}
	}

}
