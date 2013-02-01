package com.telecom.android.base;

/**
 * 系统常量.
 * 一般用于缓存的key值，方便不同activity之间传值，或者从缓存中取值.
 * @author lsq
 *
 */
public class Contants {
	/**
	 * 选择的字段建。
	 */
	public static final String PICK_ID = "ids";
	/**
	 * 选择人员的通知过滤器.
	 */
	public static final String PEOPLE_CHANGED_INTENT_FLITER = "changePeople";
	public static final String TEAM_CHANGED_INTENT_FLITER = "changeTeam";
	/**
	 * 分页的每页大小.
	 */
	public static final int  PAGE_SIZE = 100;
	/**
	 * 发送的短信
	 */
	public static final String SEND_MESSAGE = "sendMessage";
	/**
	 * 选择的人员
	 */
	public static final String SEND_PEOPLE = "sendPeople";
	/**
	 * 选择的联系人
	 */
	public static final String PICK_FROM = "pickcontact";
	/**
	 * 选择的电话号码
	 */
	public static final String PICK_NUMBER = "numbers";
	/**
	 * 选择的电话号码类型(废弃)
	 */
	public static final String PICK_TYPE = "types";
	/**
	 * 是否有电话号码
	 */
	public static final String HAS_PHONE = "hasPhone";
	public static final String PICK_NAME = "names";
	/**
	 * 首字母
	 */
	public static String FIRST_LETTER = "first_letter";
	public static String PINGYIN = "pinyin";
	public static String ALL_FIRST_LETTER = "all_first";
	public static String PHONE = "phone";
	public static String PHONETYPE = "phonetype";
}
