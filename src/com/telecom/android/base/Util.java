package com.telecom.android.base;

import java.util.ArrayList;

/**
 * 用于viewpager的调用指定页面的工具类.
 * @author lsq
 *
 */
public class Util {

	private static ArrayList<Task> allActivity;

	/**
	 * 重置allActivity
	 * 
	 * @param iw
	 *            需要更新Activity
	 */
	public static void removeActivity() {
		allActivity = null;
		allActivity = new ArrayList<Task>();
	}

	/**
	 * 把需要更新Activity添加到集合里
	 * 
	 * @param iw
	 *            需要更新Activity
	 */
	public static void addActivity(Task iw) {
		allActivity.add(iw);
	}

	/**
	 * 根据Activity的名字获取要更新的界面
	 * 
	 * @param name
	 *            需要更新Activity的名字
	 * @return
	 */
	public static Task getActivityByName(String name) { 
		for (Task iw : allActivity) {
			if (iw.getClass().getName().indexOf(name) >= 0) {
				return iw;
			}
		}
		return null;
	}
}
