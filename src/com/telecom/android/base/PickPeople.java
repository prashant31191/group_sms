package com.telecom.android.base;

/**
 * 选择的发信息的人员实体对象.
 * 
 * @author lsq
 * 
 */
public class PickPeople {
	public String id;
	public String name;
	public String type;
	public String number;
	public String teamId;

	@Override
	public boolean equals(Object otherObject) {
		if (this == otherObject)
			return true;
		if (otherObject == null)
			return false;
		if (getClass() != otherObject.getClass())
			return false;
		PickPeople other = (PickPeople) otherObject;
		if (other.id != null && this.id != null)
			return other.id.equals(this.id) && (other.name.equals(this.name))
					&& (other.number.equals(this.number));
		else
			return (other.name.equals(this.name))
					&& (other.number.equals(this.number));
	}

	@Override
	public int hashCode() {
		int tp = this.getClass().hashCode();
		int code = 0;
		if (id != null)
			code = tp * 31 + id.hashCode();
		code = code * 31 + name.hashCode();
		code = code * 31 + number.hashCode();
		return code;
	}

	public String toString() {
		return "type=" + id + ",type=" + name + ",type=" + type + ",number="
				+ number + ",teamId=" + teamId;
	}
}
