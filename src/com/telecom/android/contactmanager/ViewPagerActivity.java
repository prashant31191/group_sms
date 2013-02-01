package com.telecom.android.contactmanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.LocalActivityManager;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;

import com.telecom.android.base.Task;
import com.telecom.android.base.Util;

/**
 * 包含三个选人控件的主体框架
 * 
 * @author lsq
 * 
 */
public class ViewPagerActivity extends TabActivity implements Task {

	private ViewPager mPager;
	private List<View> listViews;
	private ImageView cursor;
	private TextView t1, t2, t3;
	private int offset = 0;
	private int currIndex = 0;
	private int bmpW;
	private LocalActivityManager manager = null;
	private final Context context = ViewPagerActivity.this;
	private TabHost mTabHost;
	private Intent tabIntent1;
	private Intent tabIntent2;
	private Intent tabIntent3;
	public static ViewPagerActivity mTabActivityInstance;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.viewpager);
		mTabActivityInstance = this;

		mTabHost = getTabHost();
		tabIntent1 = new Intent(this, HistoryManager.class);
		tabIntent2 = new Intent(this, TeamManager.class);
		tabIntent3 = new Intent(this, ContactManager.class);
		mTabHost.addTab(mTabHost.newTabSpec("A").setIndicator("")
				.setContent(tabIntent1));
		mTabHost.addTab(mTabHost.newTabSpec("B").setIndicator("")
				.setContent(tabIntent2));
		mTabHost.addTab(mTabHost.newTabSpec("C").setIndicator("")
				.setContent(tabIntent3));
		mTabHost.setCurrentTab(0);

		manager = new LocalActivityManager(this, true);
		manager.dispatchCreate(savedInstanceState);

		InitImageView();
		InitTextView();
		InitViewPager();
	}

	private void InitTextView() {
		Util.removeActivity();
		Util.addActivity(this);

		t1 = (TextView) findViewById(R.id.text1);
		t2 = (TextView) findViewById(R.id.text2);
		t3 = (TextView) findViewById(R.id.text3);

		t1.setOnClickListener(new MyOnClickListener(0));
		t2.setOnClickListener(new MyOnClickListener(1));
		t3.setOnClickListener(new MyOnClickListener(2));
	}

	private void InitViewPager() {
		mPager = (ViewPager) findViewById(R.id.vPager);
		listViews = new ArrayList<View>();
		MyPagerAdapter mpAdapter = new MyPagerAdapter(listViews);
		Intent intent = new Intent(context, HistoryManager.class);
		listViews.add(getView("Black", intent));
		Intent intent2 = new Intent(context, TeamManager.class);
		listViews.add(getView("Gray", intent2));
		Intent intent3 = new Intent(context, ContactManager.class);
		listViews.add(getView("White", intent3));
		mPager.setAdapter(mpAdapter);
		mPager.setCurrentItem(0);
		mPager.setOnPageChangeListener(new MyOnPageChangeListener());
	}

	private void InitImageView() {
		cursor = (ImageView) findViewById(R.id.cursor);
		bmpW = BitmapFactory.decodeResource(getResources(), R.drawable.footbar)
				.getWidth();
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int screenW = dm.widthPixels;
		offset = (screenW / 3 - bmpW) / 2;
		Matrix matrix = new Matrix();
		matrix.postTranslate(offset, 0);
		cursor.setImageMatrix(matrix);
	}

	public class MyPagerAdapter extends PagerAdapter {
		public List<View> mListViews;

		public MyPagerAdapter(List<View> mListViews) {
			this.mListViews = mListViews;
		}

		@Override
		public void destroyItem(View arg0, int arg1, Object arg2) {
			((ViewPager) arg0).removeView(mListViews.get(arg1));

		}

		@Override
		public int getItemPosition(Object object) {
			return POSITION_NONE;
		}

		@Override
		public void finishUpdate(View arg0) {
		}

		@Override
		public int getCount() {
			return mListViews.size();
		}

		@Override
		public Object instantiateItem(View arg0, int arg1) {
			View v = mListViews.get(arg1);
			View vvv = v.findViewById(R.id.dataList);
			vvv.setTag("list" + arg1);
			((ViewPager) arg0).addView(v, 0);
			return v;
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == (arg1);
		}

		@Override
		public void restoreState(Parcelable arg0, ClassLoader arg1) {
		}

		@Override
		public Parcelable saveState() {
			return null;
		}

		@Override
		public void startUpdate(View arg0) {
		}
	}

	private void changeColor(int i) {
		if (i == 0) {
			t1.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.options_group_pressed)); 
			t2.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.options_group_normal));
			t3.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.options_group_normal));
		} else if (i == 1) {
			t1.setBackgroundDrawable(getResources().getDrawable(R.drawable.options_group_normal));
			t2.setBackgroundDrawable(getResources().getDrawable(R.drawable.options_group_pressed));
			t3.setBackgroundDrawable(getResources().getDrawable(R.drawable.options_group_normal));
		} else {
			t1.setBackgroundDrawable(getResources().getDrawable(R.drawable.options_group_normal));
			t2.setBackgroundDrawable(getResources().getDrawable(R.drawable.options_group_normal));
			t3.setBackgroundDrawable(getResources().getDrawable(R.drawable.options_group_pressed));
		}
	}

	public class MyOnClickListener implements View.OnClickListener {
		private int index = 0;

		public MyOnClickListener(int i) {
			index = i;
		}

		@Override
		public void onClick(View v) {
			mPager.setCurrentItem(index);
			changeColor(index);
		}
	};

	public class MyOnPageChangeListener implements OnPageChangeListener {

		int one = offset * 2 + bmpW;
		int two = one * 2;

		@Override
		public void onPageSelected(int arg0) {
			Animation animation = null;
			Intent intent = new Intent();
			switch (arg0) {
			case 0:
				mTabHost.setCurrentTab(0);
				changeColor(0);
				if (currIndex == 1) {
					animation = new TranslateAnimation(one, 0, 0, 0);
				} else if (currIndex == 2) {
					animation = new TranslateAnimation(two, 0, 0, 0);
				}
				break;
			case 1:
				mTabHost.setCurrentTab(1);
				changeColor(1);
				if (currIndex == 0) {
					animation = new TranslateAnimation(offset, one, 0, 0);
				} else if (currIndex == 2) {
					animation = new TranslateAnimation(two, one, 0, 0);
				}
				break;
			case 2:
				mTabHost.setCurrentTab(2);
				changeColor(2);
				if (currIndex == 0) {
					animation = new TranslateAnimation(offset, two, 0, 0);
				} else if (currIndex == 1) {
					animation = new TranslateAnimation(one, two, 0, 0);
				}
				break;
			}
			currIndex = arg0;
			animation.setFillAfter(true);
			animation.setDuration(300);
			cursor.startAnimation(animation);
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
		}
	}

	private View getView(String id, Intent intent) {
		return manager.startActivity(id, intent).getDecorView();
	}

	private void byValue(Intent intent, HashMap<String, Object> map) {
		if (map != null) {
			intent.putExtra("tv1", map.get("tv1").toString());
		} else {
			intent.putExtra("tv1", "");
		}
	}

	@Override
	public void onRefresh(int page, HashMap<String, Object> map) {
		switch (page) {
		case 1:
			byValue(tabIntent1, map);
			mTabHost.setCurrentTabByTag("A");
			break;
		case 2:
			byValue(tabIntent2, map);
			mTabHost.setCurrentTabByTag("B");
			break;
		case 3:
			byValue(tabIntent3, map);
			mTabHost.setCurrentTabByTag("C");
			break;
		}
	}

	@Override
	public void closeThis() {
		this.finish();
	}
}