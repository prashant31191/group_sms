package com.telecom.android.contactmanager;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.telecom.android.base.Util;

/**
 * 用于添加联系人到分组的适配器.
 * 
 * @author lsq
 * 
 */
public final class TeamAddManager extends Activity implements
		OnItemClickListener {
	public static final String TAG = "TeamAddManager";
	private ListView mContactList;
	private ContactAdapter adapter;
	private EditText search;
	private EditText teamName;
	private SideBar indexBar;
	private WindowManager mWindowManager;
	private TextView mDialogText;
	private ArrayList<P> checkedList;
	private int countChecked = 0;// 得到选中的数量.
	private Button makesureBtn;
	private Button cancelBtn;
	private LinkedHashSet<ContactBean> data;
	private String id;
	private String name;
	private TextView title_bar;
	private int count = -1;
	private int pageCount = 0;
	private int currentPage = 1;
	private boolean loadedAll = false;
	private boolean isloading = false;// 表示是否真正加载中.

	public class P {
		String id;
		String name;
		String number;

		public String toString() {
			return id + ",," + name + ",," + number;
		}
	}

	private int findCheckedPosition(String id) {
		int i = -1;
		for (P m : checkedList) {
			i++;
			if (id.equals(m.id)) {
				return i;
			}
		}
		return -1;
	}

	private void remove(String id) {
		checkedList.remove(findCheckedPosition(id));
		makesureBtn.setText("确定(" + --countChecked + ")");
	}

	private void add(P p) {
		checkedList.add(p);
		makesureBtn.setText("确定(" + ++countChecked + ")");
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
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	private static final int DIALOG_KEY = 0;
	private ProgressDialog dialog;
	private int lastItem = 0;

	// 弹出"查看"对话框---在主UI线程里面创建的进度条.
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

	public class Mycomparator2 implements Comparator {
		public int compare(Object o1, Object o2) {
			ContactBean c1 = (ContactBean) o1;
			ContactBean c2 = (ContactBean) o2;
			Comparator cmp = Collator.getInstance(java.util.Locale.CHINESE);
			if (c1.displayName == null)
				return -1;
			if (c2.displayName == null)
				return 1;
			return cmp.compare(c1.displayName, c2.displayName);
		}
	}

	private int allCount = 0;
	private int memberCount = 0;

	private class MyListLoader extends AsyncTask<String, String, String> {
		private int start = 1;
		private int end = 0;
		private int ct = 0;
		private boolean showDialog;
		private String teamId;

		public MyListLoader(int ct, boolean showDialog, String teamId) {
			this.ct = ct;
			this.showDialog = showDialog;
			this.teamId = teamId;
		}

		public String doInBackground(String... params) {
			isloading = true;
			// 如果当前分组已经存在，就查询当前组里面的联系人.
			if (teamId != null) {
				data.addAll(MyApplication.getContactByTeamId(teamId));
				memberCount = data.size();
			}
			if (ct == 1) {// 第一页
				count = MyApplication.queryContactCount();
				if (teamId != null) {
					allCount = count - memberCount; // MyApplication.getContactWithoutTeamIdCount(teamId);
				}
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
				data.addAll(MyApplication.newQueryContact(false, start, end));

			} else if (ct <= pageCount) {
				start = Contants.PAGE_SIZE * (ct - 1);
				end = Contants.PAGE_SIZE * (ct) > count ? (count)
						: (Contants.PAGE_SIZE * (ct));
				data.addAll(MyApplication.newQueryContact(false, start, end));
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
				ArrayList<ContactBean> listSource = new ArrayList<ContactBean>();
				listSource.addAll(data);
				adapter = new ContactAdapter(TeamAddManager.this, listSource,
						allCount, memberCount);
				mContactList.setAdapter(adapter);
				indexBar.setListView(mContactList);
				mContactList.setSelection(lastItem);
			}
			if (showDialog)
				removeDialog(DIALOG_KEY);
			isloading = false;
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.teamadd_manager);
		Intent intent = getIntent();
		id = intent.getStringExtra("id");
		name = intent.getStringExtra("name");
		data = new LinkedHashSet<ContactBean>();
		makesureBtn = (Button) findViewById(R.id.sure);
		cancelBtn = (Button) findViewById(R.id.cancel);
		title_bar = (TextView) findViewById(R.id.title_bar);
		if (id != null) {
			title_bar.setText("编辑群组");
		}
		mContactList = (ListView) findViewById(R.id.contactList);
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
							new MyListLoader(currentPage++, true, id)
									.execute("");
					}
				}
			}
		});
		search = (EditText) findViewById(R.id.searchText);
		teamName = (EditText) findViewById(R.id.TeamName);
		if (name != null)
			teamName.setText(name);
		else
			teamName.setText("群组" + (MyApplication.newGetTeamCount() + 1));
		checkedList = new ArrayList<P>();
		search.addTextChangedListener(searchWatcher);
		search.setOnFocusChangeListener(new OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				if (search.getText().toString().trim()
						.equals(getText(R.string.search_hint).toString())) {
					search.setText("");
				}
			}
		});
		new MyListLoader(currentPage++, false, id).execute("");

		indexBar = (SideBar) findViewById(R.id.sideBar);
		// 添加一个浮动层.
		mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		if (mDialogText == null) {
			mDialogText = (TextView) LayoutInflater.from(this).inflate(
					R.layout.list_position, null);
			mDialogText.setVisibility(View.INVISIBLE);
			WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
					WindowManager.LayoutParams.TYPE_APPLICATION,
					WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
							| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
					PixelFormat.TRANSLUCENT);
			mWindowManager.addView(mDialogText, lp);
			indexBar.setTextView(mDialogText);
		}
		makesureBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final Intent intent = new Intent(TeamAddManager.this,
						TeamManager.class);
				final String _teamName = teamName.getText().toString();
				if ("".equals(_teamName)) {
					new AlertDialog.Builder(TeamAddManager.this)
							.setTitle(R.string.dialog_title)
							.setMessage("必须输入组名")
							.setPositiveButton("确定",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int which) {
											return;
										}
									}).show();
				} else {
					if (checkedList.size() == 0) {
						new AlertDialog.Builder(TeamAddManager.this)
								.setTitle(R.string.dialog_title)
								.setMessage("没有选择成员,是否要清空该组成员?")
								.setPositiveButton("对,清空",
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int which) {
												returnFrontpage(true, _teamName);
											}
										})
								.setNegativeButton("不,直接返回",
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int which) {
												setResult(RESULT_CANCELED,
														intent);
												finish();
											}
										}).show();
					} else {
						if (checkedList.size() > 0) {
							if (id != null) {// 说明是修改小组页面
								new AlertDialog.Builder(TeamAddManager.this)
										.setTitle(R.string.dialog_title)
										.setMessage("群组成员发生变化,是否覆盖现有群组?")
										.setPositiveButton(
												"对,覆盖",
												new DialogInterface.OnClickListener() {
													public void onClick(
															DialogInterface dialog,
															int which) {
														returnFrontpage(true,
																_teamName);
													}
												})
										.setNegativeButton(
												"不,新建",
												new DialogInterface.OnClickListener() {
													public void onClick(
															DialogInterface dialog,
															int which) {
														returnFrontpage(false,
																_teamName);
													}
												}).show();
							} else {
								returnFrontpage(true, _teamName);
							}
						} else {
							setResult(RESULT_OK, intent);
							finish();
						}
					}
				}
			}
		});

		cancelBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(TeamAddManager.this,
						TeamManager.class);
				intent.putExtra(Contants.PICK_ID, "");
				intent.putExtra(Contants.PICK_NAME, "");
				intent.putExtra(Contants.PICK_TYPE, "");
				setResult(RESULT_OK, intent);
				finish();
			}
		});
	}

	private void returnFrontpage(boolean override, String _teamName) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		if (MyApplication.addTeam(_teamName, checkedList, override) > 0)
			map.put("tv1", "ok");
		else
			map.put("tv1", "notok");
		Util.getActivityByName("ViewPagerActivity").onRefresh(2, map);
		sendBroadcast(new Intent(Contants.TEAM_CHANGED_INTENT_FLITER));
		finish();
	}

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
		P m = new P();
		m.id = cb.id;
		m.name = cb.displayName;
		m.number = cb.number;
		if (ckb.isChecked()) {
			remove(m.id);
		} else {
			add(m);
		}
		adapter.notifyDataSetChanged();
	}
}
