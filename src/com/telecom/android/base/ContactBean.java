package com.telecom.android.base;

/**
 * 联系人实体类.
 * 
 * @author lsq
 * 
 */
public class ContactBean {
	/**
	 * 显示名
	 */
	public String displayName;
	/**
	 * 汉字拼音
	 */
	public String pinyin;
	/**
	 * 第一个字母
	 */
	public String firstLetter;
	/**
	 * 全部首字母
	 */
	public String allFirstName;
	/**
	 * 联系人主键
	 */
	public String id;
	/**
	 * 是否含有电话号码字段
	 */
	public String hasPhone;
	/**
	 * 电话号码和类型字段(废弃)
	 */
	public String numberWithType;
	/**
	 * 电话号码
	 */
	public String number;
	/**
	 * 是否有分组,有的话，就显示为分组id
	 */
	public String flag;
	/**
	 * 电话类型
	 */
	public String phoneType;
	/**
	 * 所在分组id
	 */
	public String team;
	/**
	 * 是否被选择.
	 */
	public boolean checked;

	@Override
	public boolean equals(Object otherObject) {
		if (this == otherObject)
			return true;
		if (otherObject == null)
			return false;
		if (getClass() != otherObject.getClass())
			return false;
		ContactBean other = (ContactBean) otherObject;
		return other.id.equals(this.id);
	}

	@Override
	public int hashCode() {
		int tp = this.getClass().hashCode();
		int code = tp * 31 + displayName.hashCode();
		code = code * 31 + number.hashCode();
		code = code * 31 + id.hashCode();
		return code;
	}

	public String toString() {
		return "id=" + id + ",pinyin=" + pinyin + ",firstLetter=" + firstLetter
				+ ",allFirstName=" + allFirstName + ",displayName="
				+ displayName + ",hasPhone=" + hasPhone + ",number=" + number
				+ ",phoneType=" + phoneType + ",flag=" + flag + ",team=" + team;
	}
}
