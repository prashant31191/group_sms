package com.telecom.android.contactmanager;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.telecom.android.base.PickPeople;

/**
 * 发送短信的主界面.
 * 
 * @author lsq
 * 
 */
public final class SendConfirm extends Activity implements OnClickListener {
	private Button confirm_sure;
	private Button confirm_cancel;
	private TextView sendPeopleNumber;
	private GridView sendPeople;
	private TextView sendMessage;
	private TextView messageNumber; 
	private String message;
	private String numbers;
	private ScrollView messageContent;
	private String names;
	private ArrayList<PickPeople> pkNames; 

	private final static String TAG = "SendConfirm";

	static class ViewHolder {
		Button pickPeople;
		ImageView deleteImg;
	}

	private PickpeoplesAdapter adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.send_confirm);
		Intent intent = getIntent();
		names = intent.getStringExtra("names");
		numbers = intent.getStringExtra("numbers");
		message = intent.getStringExtra("message"); 
		messageContent= (ScrollView) findViewById(R.id.messageContent);
		confirm_sure = (Button) findViewById(R.id.confirm_sure);
		confirm_cancel = (Button) findViewById(R.id.confirm_cancel);
		sendPeople = (GridView) findViewById(R.id.sendPeople);
		sendMessage = (TextView) findViewById(R.id.sendMessage);
		sendPeopleNumber = (TextView) findViewById(R.id.sendPeopleNumber);
		messageNumber = (TextView) findViewById(R.id.messageNumber);
		sendMessage.setText(message);
		messageNumber.setText(message.length() + "("
				+ ((message.length() / 70 + 1) + ")"));
		pkNames = new ArrayList<PickPeople>();
		String[] _nms = names.split(",");
		String[] _numbers = numbers.split(",");
		for (int i = 0, j = _nms.length; i < j; i++) {
			PickPeople p = new PickPeople();
			p.name = _nms[i];
			p.number = _numbers[i];
			pkNames.add(p);
		} 
		if (adapter == null) {
			adapter = new PickpeoplesAdapter(this, pkNames, false);
			sendPeople.setAdapter(adapter);
		}
		sendPeopleNumber.setText("收件人(" + numbers.split(",").length + ")"); 
		confirm_sure.setOnClickListener(this);
		confirm_cancel.setOnClickListener(this);
		ViewGroup.LayoutParams params = confirm_sure.getLayoutParams();
		ViewGroup.LayoutParams gridPrams = sendPeople.getLayoutParams();
		gridPrams.height = (int) (params.height * 3 + 6);
		sendPeople.setLayoutParams(gridPrams); 
		ViewGroup.LayoutParams messageContentParams = messageContent.getLayoutParams();
		messageContentParams.height = (int) (params.height * 3 + 6);
		messageContent.setLayoutParams(messageContentParams);
	}
 
	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.confirm_sure) {
			MyApplication.sendMessage(numbers, message);
			Intent intent = new Intent(SendConfirm.this, NewSendManager.class);
			setResult(RESULT_OK, intent);
			finish();
		} else if (v.getId() == R.id.confirm_cancel) {
			this.finish();
		} 
	}

}
