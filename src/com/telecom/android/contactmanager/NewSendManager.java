package com.telecom.android.contactmanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;

import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.telecom.android.base.ContactBean;
import com.telecom.android.base.Contants;
import com.telecom.android.base.PickPeople;
import com.telecom.android.base.TeamBean;

/**
 * 发送短信的主界面.
 * 
 * @author lsq
 * 
 */
public final class NewSendManager extends Activity implements OnClickListener,
		ExpandableListView.OnGroupClickListener,
		ExpandableListView.OnChildClickListener, CallBack {
	public static final String TAG = "SendManager";
	private EditText inputMess;
	private Button addContat;
	private Button sendButton;
	private TextView allTeamNum;
	private TextView allPeopleNum;
	private TextView noPick;
	private ExpandableListView groupList;
	private GridView sendPeople;
	private SimpleTeamAdapter teamAdapter;
	private PickpeoplesAdapter peoplesAdapter;
	private ArrayList<PickPeople> listItem;
	private ArrayList<TeamBean> groups;
	private ArrayList<String> nameList;
	private ArrayList<String> numList;
	private final static int SEND = 1;// 点击发送按钮
	private final static int PICK = 0;// 点击选人按钮
	private TextView text_notuse;
	private HashMap<PickPeople, ArrayList<String>> pickpeopleToId;
	private ImageView choseNoTeam;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.newsend_manager);
		text_notuse = (TextView) findViewById(R.id.text_notuse);
		text_notuse.requestFocus();
		choseNoTeam = (ImageView) findViewById(R.id.choseNoTeam);
		groupList = (ExpandableListView) findViewById(R.id.groupList);
		groupList.setOnChildClickListener(this);
		groupList.setGroupIndicator(null);// 去掉默认的左边的箭头
		pickpeopleToId = new HashMap<PickPeople, ArrayList<String>>();
		sendPeople = (GridView) findViewById(R.id.sendPeople);
		inputMess = (EditText) findViewById(R.id.inputMess);
		addContat = (Button) findViewById(R.id.addContat);
		sendButton = (Button) findViewById(R.id.send);
		allPeopleNum = (TextView) findViewById(R.id.peopleCount);
		noPick = (TextView) findViewById(R.id.choseNo);
		allTeamNum = (TextView) findViewById(R.id.all_team_num);
		listItem = new ArrayList<PickPeople>();
		groups = new ArrayList<TeamBean>();
		nameList = new ArrayList<String>();
		numList = new ArrayList<String>();
		addContat.setOnClickListener(this);
		sendButton.setOnClickListener(this);
		// 设置GridView的高度是3个按钮的高度+6（间距）
		ViewGroup.LayoutParams params = sendButton.getLayoutParams();
		ViewGroup.LayoutParams gridPrams = sendPeople.getLayoutParams();
		gridPrams.height = (int) (params.height * 3 + 6);
		sendPeople.setLayoutParams(gridPrams);
		sendPeople.setVisibility(View.GONE);
	}

	BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context c, Intent intent) {
			// 判断短信是否成功
			// switch (getResultCode()) {
			Log.e(TAG, "收到成员组改变的通知!!");
			needFresh = true;
		}
	};

	@Override
	public void addP(PickPeople p) {
		if (nameList.indexOf(p.name) != -1 && numList.indexOf(p.number) != -1
				&& nameList.indexOf(p.name) == numList.indexOf(p.number))
			return;
		// 保存当前人员对应的组的id.
		ArrayList<String> teamIds = pickpeopleToId.get(p);
		if (teamIds == null && p.teamId != null) {
			teamIds = new ArrayList<String>();
			teamIds.add(p.teamId);
		} else if (p.teamId != null) {
			if (teamIds != null && teamIds.indexOf(p.teamId) == -1)
				teamIds.add(p.teamId);
		}
		pickpeopleToId.put(p, teamIds);
		listItem.add(p);
		numList.add(p.number);
		nameList.add(p.name);
	}

	@Override
	public void removeP(PickPeople p) {
		listItem.remove(p);
		int i = 0;
		for (String n : nameList) {
			if (p.name.equals(n)) {
				if (numList.get(i).equals(p.number)) {
					numList.remove(i);
					nameList.remove(i);
					break;
				}
			}
			i++;
		}
		ArrayList<String> teamIds = pickpeopleToId.get(p);
		if (teamIds != null)
			for (String tId : teamIds) {
				for (TeamBean b : groups) {
					if (b.teamId.equals(tId)) {
						b.checked = false;
						for (ContactBean cb : b.users)
							if (cb.number.equals(p.number)
									&& cb.displayName.equals(p.name)) {
								cb.checked = false;
							}
					}
				}
			}
	}

	@Override
	public void show() {
		if (listItem.size() > 0) {
			noPick.setVisibility(View.GONE);
			allPeopleNum.setText("(" + listItem.size() + ")");
			sendPeople.setVisibility(View.VISIBLE);
		} else {
			noPick.setVisibility(View.VISIBLE);
			allPeopleNum.setText("(0)");
			sendPeople.setVisibility(View.GONE);
		}
		peoplesAdapter = new PickpeoplesAdapter(this, listItem, true);
		sendPeople.setAdapter(peoplesAdapter);
		if (teamAdapter != null)
			teamAdapter.notifyDataSetChanged();
	}

	public void deleteThis(View view) {
		if (view instanceof ImageView) {
			ImageView r = (ImageView) view;
			removeP((PickPeople) r.getTag());
			show();
		}
	}

	// 设置当前选择的人员的姓名，和电话号码.
	private void setList(String numbers, String names) {
		String[] numberArr = numbers.split(",");
		String[] nameArr = names.split(",");
		for (int i = 0, j = numberArr.length; i < j; i++) {
			PickPeople p = new PickPeople();
			p.name = nameArr[i];
			p.number = numberArr[i];
			addP(p);
		}
		show();
	}

	private static final int DIALOG_KEY = 0;
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

	/**
	 * 加载分组列表信息.
	 * 
	 * @author lsq
	 * 
	 */
	private class GroupListLoader extends AsyncTask<String, String, String> {

		private boolean showDialog;

		public GroupListLoader(boolean showDialog) {
			this.showDialog = showDialog;
		}

		@Override
		protected void onPreExecute() {
			if (showDialog)
				showDialog(DIALOG_KEY);
		}

		public String doInBackground(String... params) {
			groups = MyApplication.newGetTeam();
			return "";
		}

		@Override
		public void onPostExecute(String Re) {
			// 绑定LISTVIEW
			if (groups.size() == 0) {
				allTeamNum.setText("(0)");
				choseNoTeam.setVisibility(View.VISIBLE);
				groupList.setVisibility(View.GONE);
			} else {
				choseNoTeam.setVisibility(View.GONE);
				groupList.setVisibility(View.VISIBLE);
				allTeamNum.setText("(" + groups.size() + ")");
				if (teamAdapter == null) {
					teamAdapter = new SimpleTeamAdapter(NewSendManager.this,
							groups);
					groupList.setAdapter(teamAdapter);
				} else {
					teamAdapter.notifyDataSetChanged();
				}
			}
			needFresh = false;
			if (showDialog)
				removeDialog(DIALOG_KEY);
		}
	}
 

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case PICK:
			if (resultCode == RESULT_OK) {
				String numbers = "";
				String names = "";
				if (MyApplication.checkedList.size() != 0) {
					StringBuilder _n = new StringBuilder();
					StringBuilder _u = new StringBuilder();
					for (PickPeople m : MyApplication.checkedList) {
						_n.append(m.name + ",");
						_u.append(m.number + ",");
					}
					_n = _n.deleteCharAt(_n.lastIndexOf(","));
					_u = _u.deleteCharAt(_u.lastIndexOf(","));
					numbers = _u.toString();
					names = _n.toString();
				}
				MyApplication.checkedList = new LinkedHashSet<PickPeople>();
				if (numbers != null && !"".equals(numbers))
					setList(numbers, names);
			} else {
				MyApplication.checkedList = new LinkedHashSet<PickPeople>();
			}
			break;
		case SEND:
			// 发送完毕之后，清空gridView.
			if (resultCode == RESULT_OK) {
				listItem = new ArrayList<PickPeople>();
				nameList = new ArrayList<String>();
				numList = new ArrayList<String>();
				MyApplication.checkedList = new LinkedHashSet<PickPeople>();
				inputMess.setText("");
				peoplesAdapter = new PickpeoplesAdapter(this, listItem, true);
				sendPeople.setAdapter(peoplesAdapter);
				for (TeamBean b : groups) {
					b.checked = false;
				}
				allPeopleNum.setText("(0)");
				teamAdapter = new SimpleTeamAdapter(NewSendManager.this, groups);
				groupList.setAdapter(teamAdapter);
				// teamAdapter.notifyDataSetChanged();
			} else {
				MyApplication.checkedList = new LinkedHashSet<PickPeople>();
			}
			break;
		default:
			break;
		}
	}

	@Override
	public void onClick(View v) {
		String message = inputMess.getText().toString();
		if (v.getId() == R.id.send) {
			if (message != null && !"".equals(message) && numList.size() > 0) {
				Intent intent = new Intent(NewSendManager.this,
						SendConfirm.class);
				intent.putExtra("message", message);
				intent.putExtra("numbers", MyApplication.listToString(numList));
				intent.putExtra("names", MyApplication.listToString(nameList));
				this.startActivityForResult(intent, SEND);
			} else {
				Toast.makeText(NewSendManager.this, "联系人或短信内容不能为空",
						Toast.LENGTH_SHORT).show();

			}
		} else if (v.getId() == R.id.addContat) {
			Intent intent = new Intent(NewSendManager.this,
					ViewPagerActivity.class);
			this.startActivityForResult(intent, PICK);
		}
	}

	private String SENT_SMS_ACTION = "SENT_SMS_ACTION";
	private String DELIVERED_SMS_ACTION = "DELIVERED_SMS_ACTION";
	public static final Uri SMS_URI = Uri.parse("content://sms/");

	private boolean needFresh = true;

	protected void onResume() {
		super.onResume();
		// 注册监听
		Log.e(TAG, "onResume");
		if (needFresh){
			teamAdapter = null;
			new GroupListLoader(true).execute("");
		}
		registerReceiver(sendMessage, new IntentFilter(SENT_SMS_ACTION));
		registerReceiver(receiver, new IntentFilter(
				Contants.TEAM_CHANGED_INTENT_FLITER));
	}

	BroadcastReceiver sendMessage = new BroadcastReceiver() {
		@Override
		public void onReceive(Context c, Intent intent) {
			// 判断短信是否成功
			switch (getResultCode()) {
			case Activity.RESULT_OK:
				Toast.makeText(NewSendManager.this, "发送成功！", Toast.LENGTH_SHORT)
						.show();
				break;
			default:
				Toast.makeText(NewSendManager.this, "发送失败！", Toast.LENGTH_SHORT)
						.show();
				break;
			}
		}
	};

	/**
	 * 群发短信.
	 * 
	 * @param phone
	 * @param body
	 * @param threadId
	 */
	public void sendSMS(ArrayList<String> phone, String body, long threadId) {
		SmsManager msg = SmsManager.getDefault();
		Intent send = new Intent(SENT_SMS_ACTION);
		PendingIntent sendPI = PendingIntent.getBroadcast(this, 0, send, 0);
		Intent delive = new Intent(DELIVERED_SMS_ACTION);
		PendingIntent deliverPI = PendingIntent
				.getBroadcast(this, 0, delive, 0);
		// 将数据插入数据库中以便系统默认短信信箱可以显示.
		ContentValues cv = new ContentValues();
		for (String pno : phone) {
			msg.sendTextMessage(pno, null, body, sendPI, deliverPI);
			cv.put("thread_id", threadId);
			cv.put("date", System.currentTimeMillis());
			cv.put("body", body);
			cv.put("read", 0);
			cv.put("type", 2);
			cv.put("address", pno);
			this.getContentResolver().insert(SMS_URI, cv);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(sendMessage);
		if (!"".equals(inputMess.getText().toString()) && numList.size() > 0)
			MyApplication.addCaogao(MyApplication.listToString(nameList),
					inputMess.getText().toString(), System.currentTimeMillis()
							+ "");
		unregisterReceiver(receiver);
	}

	@Override
	public boolean onChildClick(ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id) {
		RelativeLayout layout = (RelativeLayout) v;
		CheckBox ckb = (CheckBox) layout.getChildAt(2);
		TeamBean tb = (TeamBean) layout.getChildAt(0).getTag();
		ContactBean b = (ContactBean) ckb.getTag();
		b.checked = !b.checked;
		PickPeople p = new PickPeople();
		p.id = b.id;
		p.number = b.number;
		p.name = b.displayName;
		p.teamId = b.team;
		Log.e(TAG, "处理的人员" + p.toString());
		if (b.checked) {
			addP(p);
		} else {
			removeP(p);
			tb.checked = false;
		}
		show();
		teamAdapter.notifyDataSetChanged();
		return false;
	}

	@Override
	public boolean onGroupClick(ExpandableListView parent, View v,
			int groupPosition, long id) {
		return false;
	}
}

interface CallBack {
	void addP(PickPeople p);

	void removeP(PickPeople p);

	void show();
}
