package com.telecom.android.base;

/**
 * 最近联系人实体类.
 * 
 * @author lsq
 * 
 */
public class HistoryBean {
	/**
	 * 主键.
	 */
	public String historyId;
	/**
	 * 电话号码
	 */
	public String number;
	/**
	 * 类型
	 */
	public String type;
	public boolean checked;
	/**
	 * 联系人id
	 */
	public long contactId = -1;
	public String newCallLog;
	public String displayName = "未知联系人";

	@Override
	public boolean equals(Object otherObject) {
		if (this == otherObject)
			return true;
		if (otherObject == null)
			return false;
		if (getClass() != otherObject.getClass())
			return false;
		HistoryBean other = (HistoryBean) otherObject;
		return other.historyId.equals(this.historyId);
	}

	@Override
	public int hashCode() {
		int tp = this.getClass().hashCode();
		int code = tp * 31 + displayName.hashCode();
		code = code * 31 + number.hashCode();
		code = code * 31 + historyId.hashCode();
		return code;
	}

	public String toString() {
		return historyId + "," + number + "," + checked + "," + displayName;
	}
}
