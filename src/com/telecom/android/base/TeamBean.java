package com.telecom.android.base;

import java.util.ArrayList;

/**
 * 联系人分组的实体类.
 * @author lsq
 *
 */
public class TeamBean {
	public String teamId;
	public String teamName;
	public String other;
	public ArrayList<ContactBean> users;
	public boolean checked;
	public String toString() {
		return "teamId=" + teamId + ",teamName=" + teamName + ",other=" + other;
	}
	public void setChecked(boolean b) {
		checked = b;
		if (users != null && users.size() > 0) {// 若children不为空，循环设置children的checked
			for (ContactBean each : users) {
				each.checked = checked;
			}
		}
	}
}
