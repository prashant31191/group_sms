package com.telecom.android.contactmanager;

import java.util.ArrayList;
import java.util.LinkedHashSet;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.telecom.android.base.Contants;
import com.telecom.android.base.HistoryBean;
import com.telecom.android.base.PickPeople;
import com.telecom.android.base.Task;

/**
 * 最近联系人.
 * 
 * @author lsq
 * 
 */
public final class HistoryManager extends Activity implements
		OnItemClickListener {
	private int lastItem = 0;

	public static final String TAG = "HistoryManager";
	private ListView historyList;
	private HistoryAdapter adapter;
	private ArrayList<HistoryBean> data;
	private LinkedHashSet<HistoryBean> sets;
	private static final int DIALOG_KEY = 0;
	private Button makesureBtn;
	private int count = -1;
	private int pageCount = 0;
	private int currentPage = 1;
	private boolean loadedAll = false;
	private boolean isloading = false;// 表示是否真正加载中.
	 private Task parentActivity;

	BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context c, Intent intent) {
			makesureBtn.setText("确定(" + MyApplication.checkedList.size() + ")");
		}
	};
 
	private class MyListLoader extends AsyncTask<String, String, String> {
		private int start = 1;
		private int end = 0;
		private int ct = 0;

		public MyListLoader(int ct) {
			this.ct = ct;
		}

		public String doInBackground(String... params) {
			isloading = true;
			if (ct == 1) {// 第一页
				count = MyApplication.getHistoryBeanCount();
				if (count > 0)
					pageCount = count / Contants.PAGE_SIZE + 1;
				start = 0;
				if (count > 0)
					if (count >= Contants.PAGE_SIZE)
						end = Contants.PAGE_SIZE;
					else
						end = count;
				else {
					start = 0;
					end = 0;
				}
				sets.addAll(MyApplication.getHistoryBeans(getContentResolver(),
						start, end));

			} else if (ct <= pageCount) {
				start = Contants.PAGE_SIZE * (ct - 1);
				end = Contants.PAGE_SIZE * (ct) > count ? (count)
						: (Contants.PAGE_SIZE * (ct));
				sets.addAll(MyApplication.getHistoryBeans(getContentResolver(),
						start, end));
			}
			data = new ArrayList<HistoryBean>();
			data.addAll(sets);
			return "";
		}

		@Override
		protected void onPreExecute() {
			showDialog(DIALOG_KEY);
		}

		@Override
		public void onPostExecute(String Re) {
			if (data.size() == 0) {
				// emptytextView.setVisibility(View.VISIBLE);
			} else {
				adapter = new HistoryAdapter(HistoryManager.this, data);
				historyList.setAdapter(adapter);
				historyList.setSelection(lastItem);
			}
			removeDialog(DIALOG_KEY);
			isloading = false;
		}
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

	private void remove(PickPeople p) {
		MyApplication.checkedList.remove(p);
		sendBroadcast(new Intent(Contants.PEOPLE_CHANGED_INTENT_FLITER));
	}

	private void add(PickPeople p) {
		MyApplication.checkedList.add(p);
		sendBroadcast(new Intent(Contants.PEOPLE_CHANGED_INTENT_FLITER));
	}

	private ProgressDialog dialog;

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_KEY: {
			dialog = new ProgressDialog(this);
			dialog.setMessage("正在获取最近联系人...请稍候");
			dialog.setIndeterminate(true);
			dialog.setCancelable(true);
			return dialog;
		}
		}
		return null;
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	private Button backBtn;

	@Override
	public void onResume() {
		super.onResume();
		Log.e(TAG, "onResume");
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
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.history_manager);
		historyList = (ListView) findViewById(R.id.dataList);
		parentActivity = (Task) ViewPagerActivity.mTabActivityInstance;
		historyList.setOnItemClickListener(this);
		sets = new LinkedHashSet<HistoryBean>();
		makesureBtn = (Button) findViewById(R.id.sure);
		historyList.setOnScrollListener(new OnScrollListener() {
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub

			}

			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				lastItem = firstVisibleItem + visibleItemCount - 1;
				if (totalItemCount <= 0) {
					return;
				}
				if (firstVisibleItem + visibleItemCount == totalItemCount) {
					if (isloading) {
						return;
					}
					if (!loadedAll) {
						if (currentPage <= pageCount)
							new MyListLoader(currentPage++).execute("");
					}
				}
			}
		});
		makesureBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(HistoryManager.this,
						NewSendManager.class);
				ViewPagerActivity mTabMainActivity = (ViewPagerActivity) getParent();
				mTabMainActivity.setResult(RESULT_OK, intent);
				finish();
			}
		});
		IntentFilter mFilter = new IntentFilter(
				Contants.PEOPLE_CHANGED_INTENT_FLITER);
		mFilter.setPriority(1000); 
		registerReceiver(receiver, mFilter);
		new MyListLoader(currentPage++).execute("");
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int position,
			long arg3) {
		RelativeLayout layout = (RelativeLayout) view;
		CheckBox ckb = (CheckBox) layout.getChildAt(0);
		HistoryBean b = (HistoryBean) ckb.getTag();
		b.checked = !b.checked;
		PickPeople m = new PickPeople();
		m.id = "" + b.contactId;
		m.name = b.displayName;
		m.type = b.type;
		m.number = b.number;
		if (!b.checked) {
			remove(m);
		} else {
			add(m);
		}
		adapter.notifyDataSetChanged();
	}
}
