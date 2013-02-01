package com.telecom.android.base;

import java.util.HashMap;

public interface Task {
	/**
	 * 刷新跳转的当前页面
	 * @param page 跳转到该页面的标识
	 * @param map  传入的参数(自己可根据需要修改参数)
	 */
	void onRefresh(int page,HashMap<String, Object> map);
	
	void closeThis();
}
