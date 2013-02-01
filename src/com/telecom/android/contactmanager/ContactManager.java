package com.telecom.android.contactmanager;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.telecom.android.base.ContactBean;
import com.telecom.android.base.Contants;
import com.telecom.android.base.PickPeople;
import com.telecom.android.base.Task;

/**
 * 全部联系人的页面.
 * 
 * @author lsq
 * 
 */
public final class ContactManager extends Activity implements
		OnItemClickListener {
	public static final String TAG = "ContactManager";
	private ListView mContactList;
	private ContactAdapter adapter;
	private EditText search;
	private SideBar indexBar;
	private WindowManager mWindowManager;
	private TextView mDialogText;  
	private Button makesureBtn;
	private TextView text_notuse;
	private ArrayList<ContactBean> data;
	private int count = -1;
	private int pageCount = 0;
	private int currentPage = 1;
	private boolean loadedAll = false;
	private boolean isloading = false;// 表示是否真正加载中.
	private static final int DIALOG_KEY = 0;

	private void remove(PickPeople p) {
		MyApplication.checkedList.remove(p);
		sendBroadcast(new Intent(Contants.PEOPLE_CHANGED_INTENT_FLITER));
	}

	private void add(PickPeople p) {
		MyApplication.checkedList.add(p);
		sendBroadcast(new Intent(Contants.PEOPLE_CHANGED_INTENT_FLITER));
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mDialogText != null) {
			mWindowManager.removeView(mDialogText);
			mDialogText = null;
		}
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

	private MyReceiver receiver;

	private class MyReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			makesureBtn.setText("确定(" + MyApplication.checkedList.size() + ")");
		}
	}

	private Task parentActivity;
	private Button backBtn;

	public void onResume() {
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
	}

	private ProgressDialog dialog;

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_KEY: {
			dialog = new ProgressDialog(this);
			dialog.setMessage("正在查询数据...请稍候");
			dialog.setIndeterminate(true);
			dialog.setCancelable(true);
			return dialog;
		}
		}
		return null;
	}

	// 通讯社按中文拼音排序
	public class Mycomparator implements Comparator {
		public int compare(Object o1, Object o2) {
			ContactBean c1 = (ContactBean) o1;
			ContactBean c2 = (ContactBean) o2;
			Comparator cmp = Collator.getInstance(java.util.Locale.ENGLISH);
			if (c1.pinyin == null)
				return -1;
			if (c2.pinyin == null)
				return 1;
			return cmp.compare(c1.pinyin, c2.pinyin);
		}
	}

	private class MyListLoader extends AsyncTask<String, String, String> {
		private int start = 1;
		private int end = 0;
		private int ct = 0;
		private boolean showDialog;

		public MyListLoader(int ct, boolean showDialog) {
			this.ct = ct;
			this.showDialog = showDialog;
		}

		public String doInBackground(String... params) {
			isloading = true;
			if (ct == 1) {// 第一页
				count = MyApplication.queryContactCount();
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
				// 第一次的时候查询出来全部的草稿
				data = MyApplication.newQueryContact(false, start, end);

			} else if (ct <= pageCount) {
				start = Contants.PAGE_SIZE * (ct - 1);
				end = Contants.PAGE_SIZE * (ct) > count ? (count)
						: (Contants.PAGE_SIZE * (ct));
				ArrayList<ContactBean> d = MyApplication.newQueryContact(false,
						start, end);
				data.addAll(d);
			}
			return "";
		}

		@Override
		protected void onPreExecute() {
			if (showDialog)
				showDialog(DIALOG_KEY);
		}

		@Override
		public void onPostExecute(String Re) {
			if (data.size() == 0) {
				// emptytextView.setVisibility(View.VISIBLE);
			} else {
				// 按中文拼音顺序排序
				// Comparator comp = new Mycomparator();
				// Collections.sort(data, comp);

				adapter = new ContactAdapter(ContactManager.this, data, -1, -1);
				mContactList.setAdapter(adapter);

				indexBar.setListView(mContactList);

				mContactList.setSelection(lastItem);
			}
			if (showDialog)
				removeDialog(DIALOG_KEY);
			isloading = false;
		}
	}

	private int lastItem = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contact_manager);
		parentActivity = (Task) ViewPagerActivity.mTabActivityInstance;
		// getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
		// R.layout.titlebar);
		text_notuse = (TextView) findViewById(R.id.text_notuse);
		text_notuse.requestFocus();
		data = new ArrayList<ContactBean>();
		makesureBtn = (Button) findViewById(R.id.sure);
		mContactList = (ListView) findViewById(R.id.dataList);
		mContactList.setOnItemClickListener(this);
		mContactList.setOnScrollListener(new OnScrollListener() {
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
							new MyListLoader(currentPage++, true).execute("");
					}
				}
			}
		});
		search = (EditText) findViewById(R.id.searchText);
		// checkedList = new ArrayList<PickPeople>();
		search.addTextChangedListener(searchWatcher);
		search.setOnFocusChangeListener(new OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				if (search.getText().toString().trim()
						.equals(getText(R.string.search_hint).toString())) {
					search.setText("");
				}
			}
		});
		new MyListLoader(currentPage++, false).execute("");

		indexBar = (SideBar) findViewById(R.id.sideBar);
		// 添加一个浮动层用于索引的中间蓝色标识显示..
		mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		try {
			if (mDialogText == null) {
				mDialogText = (TextView) LayoutInflater.from(this).inflate(
						R.layout.list_position, null);
				mDialogText.setVisibility(View.INVISIBLE);
				WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
						LayoutParams.WRAP_CONTENT,
						LayoutParams.WRAP_CONTENT,
						WindowManager.LayoutParams.TYPE_APPLICATION,
						WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
								| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
						PixelFormat.TRANSLUCENT);
				mWindowManager.addView(mDialogText, lp);
				indexBar.setTextView(mDialogText);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		makesureBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ContactManager.this,
						NewSendManager.class);
				ViewPagerActivity mTabMainActivity = (ViewPagerActivity) getParent();
				mTabMainActivity.setResult(RESULT_OK, intent);
				finish();
			}
		});
		IntentFilter mFilter = new IntentFilter(
				Contants.PEOPLE_CHANGED_INTENT_FLITER);
		mFilter.setPriority(1000);
		receiver = new MyReceiver();
		registerReceiver(receiver, mFilter);

	}

	/**
	 * 搜索联系人.
	 */
	private TextWatcher searchWatcher = new TextWatcher() {

		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
		}

		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		public void afterTextChanged(Editable s) {
			if (s != null) {
				adapter.search(s.toString().trim());
			} else {
				adapter.search("");
			}
			mContactList.setAdapter(adapter);
		}
	};

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		RelativeLayout layout = (RelativeLayout) view;
		CheckBox ckb = (CheckBox) layout.getChildAt(0);
		ContactBean cb = (ContactBean) ckb.getTag();
		cb.checked = !cb.checked;
		PickPeople m = new PickPeople();
		m.id = cb.id;
		m.name = cb.displayName;
		m.type = "";
		m.number = cb.number;
		if (ckb.isChecked()) {
			remove(m);
		} else {
			add(m);
		}
		adapter.notifyDataSetChanged();
	}

}
