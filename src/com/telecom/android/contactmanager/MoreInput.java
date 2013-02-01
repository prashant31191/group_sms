package com.telecom.android.contactmanager;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.telecom.android.base.Contants;

/**
 * 更多输入.
 * 
 * @author lsq
 * 
 */
public final class MoreInput extends Dialog implements OnClickListener {
	public static final String TAG = "MoreInput";
	private Context context;

	public MoreInput(Context context) {
		super(context);
		this.context = context;
	}

	public MoreInput(Context context, int theme) {
		super(context, theme);
		this.context = context;
	}

	private Button makesureBtn;
	private Button cancelBtn;
	private Button sendBtn;
	private EditText inputEdit;
	private TextView tips;
	private String peoples;

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.more_input);

		inputEdit = (EditText) findViewById(R.id.inputEdit);
		makesureBtn = (Button) findViewById(R.id.sure);
		cancelBtn = (Button) findViewById(R.id.cancel);
		sendBtn = (Button) findViewById(R.id.send);
		tips = (TextView) findViewById(R.id.tips);
		makesureBtn.setOnClickListener(this);
		cancelBtn.setOnClickListener(this);
		sendBtn.setOnClickListener(this);
		if (MyApplication.cache.get(Contants.SEND_MESSAGE) != null) {
			inputEdit.setText(MyApplication.cache.get(Contants.SEND_MESSAGE));
		}
		peoples = MyApplication.cache.get(Contants.SEND_PEOPLE);
		inputEdit.addTextChangedListener(inputWatcher);
	}

	private TextWatcher inputWatcher = new TextWatcher() {

		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
		}

		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		public void afterTextChanged(Editable s) {
			tips.setText("(" + s.length() + ")" + (s.length() / 70 + 1));
		}
	};

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.send) {
			if (peoples == null
					|| "".equals(inputEdit.getText().toString().trim())) {
				Toast.makeText(context, "没有输入联系人或者输入短信内容!", Toast.LENGTH_LONG)
						.show();
			} else {
				MyApplication.sendMessage(peoples, inputEdit.getText()
						.toString());
				MyApplication.cache.put(Contants.SEND_MESSAGE, "");
				MyApplication.cache.put(Contants.SEND_PEOPLE, "");
				dismiss();
			}
		} else if (v.getId() == R.id.sure) {
			MyApplication.cache.put(Contants.SEND_MESSAGE, inputEdit.getText()
					.toString());
			dismiss();
		} else if (v.getId() == R.id.cancel) {
			// this.finish();
			dismiss();
		}
	}

}
