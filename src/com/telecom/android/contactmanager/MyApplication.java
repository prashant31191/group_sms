package com.telecom.android.contactmanager;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.WeakHashMap;

import android.app.Application;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.CallLog;
import android.provider.Contacts.GroupMembership;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.Groups;
import android.support.v4.util.LruCache;
import android.telephony.SmsManager;
import android.util.FloatMath;
import android.util.Log;
import android.widget.Toast;

import com.telecom.android.base.ContactBean;
import com.telecom.android.base.HistoryBean;
import com.telecom.android.base.MessageBean;
import com.telecom.android.base.PickPeople;
import com.telecom.android.base.PingYinUtil;
import com.telecom.android.base.TeamBean;

/**
 * 应用程序的全局控制类. 包含重要的查询sql语句的全部方法： 1.查询联系人 2.查询短信 3.查询最近历史联系人 4.查询联系人分组
 * 5.添加联系人到分组 6.保存各种缓存：包括联系人id->电话号码,联系人id->联系人名称等等
 * 
 * @author lsq
 * 
 */
public class MyApplication extends Application {
	public static WeakHashMap<String, Bitmap> imageCache;
	public static HashMap<String, String> phoneNumber;
	public static MyApplication nowApplication;
	// public static final Uri MMSSMS_FULL_CONVERSATION_URI = Uri
	// .parse("content://mms-sms/conversations");
	// public static final Uri CONVERSATION_URI = MMSSMS_FULL_CONVERSATION_URI
	// .buildUpon().appendQueryParameter("simple", "true").build();
	public static final Uri smsUri = Uri.parse("content://sms/");
	private static Context mContext;
	public static HashMap<String, String> cache;
	// key是number，value是联系人数租ss[0]名字 + ",,," + ss[1]联系人id
	public static HashMap<String, String> numberToContact;
	// key为contactId，value是number号码 + ",,," + ss[1]名字
	public static HashMap<String, String> contactToNumber;
	public static HashMap<String, String[]> nameToPyin;
	public static HashMap<String, String> contactToName;
	public static LinkedHashSet<PickPeople> checkedList;

	/**
	 * 初始化各种缓存记录。
	 */
	public void onCreate() {
		// 图片缓存
		imageCache = new WeakHashMap<String, Bitmap>();
		// 电话号码
		phoneNumber = new HashMap<String, String>();
		// 缓存一个全局的选择的联系人列表
		checkedList = new LinkedHashSet<PickPeople>();
		// 记录当前activity所在的对象.用于得到context
		nowApplication = this;
		// 记录程序的一些环境变量.
		cache = new HashMap<String, String>();
		// 电话号码到联系人id的缓存
		numberToContact = new HashMap<String, String>();
		// 联系人id到电话号码的缓存
		contactToNumber = new HashMap<String, String>();
		// 联系人名字到汉字拼音的缓存。注意，值为字符串数组。分别为汉字拼音，首字母，以及全部的首字母
		nameToPyin = new HashMap<String, String[]>();
		// 联系人id到名称的缓存
		contactToName = new HashMap<String, String>();
	}

	/**
	 * 得到指定文件的指定大小。
	 * 
	 * @param imageFilePath
	 * @param displayWidth
	 * @param displayHeight
	 * @return
	 */
	public static Bitmap getBitmap(String imageFilePath, int displayWidth,
			int displayHeight) {
		BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
		bitmapOptions.inJustDecodeBounds = true;
		bitmapOptions.inPreferredConfig = Bitmap.Config.RGB_565;
		Bitmap bmp = BitmapFactory.decodeFile(imageFilePath, bitmapOptions);

		// 编码后bitmap的宽高,bitmap除以屏幕宽度得到压缩比
		int widthRatio = (int) FloatMath.ceil(bitmapOptions.outWidth
				/ (float) displayWidth);
		int heightRatio = (int) FloatMath.ceil(bitmapOptions.outHeight
				/ (float) displayHeight);

		if (widthRatio > 1 && heightRatio > 1) {
			if (widthRatio > heightRatio) {
				// 压缩到原来的(1/widthRatios)
				bitmapOptions.inSampleSize = widthRatio;
			} else {
				bitmapOptions.inSampleSize = heightRatio;
			}
		}
		bitmapOptions.inJustDecodeBounds = false;
		bmp = BitmapFactory.decodeFile(imageFilePath, bitmapOptions);
		return bmp;
	}

	/**
	 * 根据电话号码得到联系人id和联系人名字
	 * 
	 * @param number
	 * @return
	 */
	public static String[] getContactIdAndNameByNumber(String number) {
		String contactId = MyApplication.numberToContact.get(number);
		String contactName = MyApplication.contactToName.get(number);
		if (contactId == null && contactName == null) {
			mContext = MyApplication.nowApplication.getApplicationContext();
			String[] ss = new String[2];
			Uri uriNumberToContacts = Uri
					.parse("content://com.android.contacts/data/phones/filter/"
							+ number);
			Cursor contactCursor = mContext.getContentResolver().query(
					uriNumberToContacts,
					new String[] { "display_name", "contact_id" }, null, null,
					null);
			try {
				if (contactCursor != null && contactCursor.getCount() > 0) {
					contactCursor.moveToNext();
					ss[0] = contactCursor.getString(contactCursor
							.getColumnIndex("display_name"));
					ss[1] = contactCursor.getString(contactCursor
							.getColumnIndex("contact_id"));

				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (contactCursor != null && !contactCursor.isClosed()) {
					contactCursor.close();
				}
			}
			if (ss[0] != null && ss[1] != null) {
				MyApplication.numberToContact.put(number, ss[0]);
				MyApplication.contactToName.put(ss[1], ss[0]);
			}
			if (ss[1] != null)
				MyApplication.contactToNumber.put(ss[1], number);
			if (ss[0] == null)
				ss[0] = number;
			return ss;
		} else {
			return new String[] { contactId, contactName };
		}
	}

	private static String TAG = "MYApplication";
	// 查询可显示且未删除的群组的过滤条件
	public static final String VISIBLE_GROUP_SELECTION = ContactsContract.Groups.DELETED
			+ "=0";
	// 查询联系人组的条件
	public static final String[] RAW_PROJECTION = new String[] { Data.RAW_CONTACT_ID };
	// 查询指定分组联系人
	public static final String RAW_CONTACTS_WHERE = ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID
			+ "=?"
			+ " and "
			+ Data.MIMETYPE
			+ "="
			+ "'"
			+ ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE
			+ "'";
	// 查询不在指定分组的联系人
	public static final String RAW_CONTACTS_WITHOUT_TEAM_WHERE = ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID
			+ "!=?"
			+ " and "
			+ Data.MIMETYPE
			+ "="
			+ "'"
			+ ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE
			+ "'";
	// 查询未分组联系人的过滤条件
	public static final String RAW_CONTACTS_IN_NO_GROUP_SELECTION = "1=1) and "
			+ Data.RAW_CONTACT_ID + " not in( select " + Data.RAW_CONTACT_ID
			+ " from view_data_restricted where " + Data.MIMETYPE + "='"
			+ GroupMembership.CONTENT_ITEM_TYPE + "') group by ("
			+ Data.RAW_CONTACT_ID;

	/**
	 * 查询联系人分组信息.
	 * 
	 * @return
	 */
	public static ArrayList<TeamBean> newGetTeam() {
		mContext = MyApplication.nowApplication.getApplicationContext();
		ContentResolver cr = mContext.getContentResolver();
		ArrayList<TeamBean> beans = new ArrayList<TeamBean>();
		Cursor circleCursor = cr.query(Groups.CONTENT_URI, null,
				VISIBLE_GROUP_SELECTION, null, null);
		try {
			if (circleCursor != null) {
				int circleIdIndex = circleCursor.getColumnIndex(Groups._ID);
				int circleNameIndex = circleCursor.getColumnIndex(Groups.TITLE);
				while (circleCursor.moveToNext()) {
					TeamBean t = new TeamBean();
					t.teamId = "" + circleCursor.getLong(circleIdIndex);
					t.teamName = circleCursor.getString(circleNameIndex);
					ArrayList<ContactBean> users = getContactByTeamId(t.teamId);
					if (users.size() > 0) {
						t.users = users;
						beans.add(t);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (circleCursor != null && !circleCursor.isClosed()) {
				circleCursor.close();
			}
		}
		return beans;
	}

	/**
	 * 得到群组的数量
	 * 
	 * @return
	 */
	public static int newGetTeamCount() {
		mContext = MyApplication.nowApplication.getApplicationContext();
		ContentResolver cr = mContext.getContentResolver();
		ArrayList<TeamBean> beans = new ArrayList<TeamBean>();
		Cursor circleCursor = cr.query(Groups.CONTENT_URI, null,
				VISIBLE_GROUP_SELECTION, null, null);
		try {
			if (circleCursor != null) {
				return circleCursor.getCount();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (circleCursor != null && !circleCursor.isClosed()) {
				circleCursor.close();
			}
		}
		return 0;
	}

	/**
	 * 查询某一个分组里面的联系人集合.
	 * 
	 * @param cr
	 * @param teamId
	 * @return
	 */
	public static ArrayList<ContactBean> getContactByTeamId(String teamId) {
		mContext = MyApplication.nowApplication.getApplicationContext();
		// 查询组中的联系人人员.
		Cursor conRawIdCursor = mContext.getContentResolver().query(
				Data.CONTENT_URI, RAW_PROJECTION, RAW_CONTACTS_WHERE,
				new String[] { teamId }, null);
		ArrayList<ContactBean> list = new ArrayList<ContactBean>();
		try {
			if (conRawIdCursor != null) {
				int rawIdIndex = conRawIdCursor
						.getColumnIndex(Data.RAW_CONTACT_ID);
				LinkedHashSet<ContactBean> users = new LinkedHashSet<ContactBean>();
				while (conRawIdCursor.moveToNext()) {
					ContactBean b = new ContactBean();
					b.id = "" + conRawIdCursor.getLong(rawIdIndex);
					b.number = getPhone(mContext, b.id);
					b.displayName = getConDisplayNameByRawId(conRawIdCursor
							.getLong(rawIdIndex));
					b.flag = teamId;
					users.add(b);
				}
				list.addAll(users);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (conRawIdCursor != null && !conRawIdCursor.isClosed()) {
				conRawIdCursor.close();
			}
		}
		return list;
	}

	/**
	 * 返回不在当前小组的联系人数量.
	 * 
	 * @param teamId
	 * @return
	 */
	public static int getContactWithoutTeamIdCount(String teamId) {
		mContext = MyApplication.nowApplication.getApplicationContext();
		// 查询组中的联系人人员.
		Cursor conRawIdCursor = mContext.getContentResolver().query(
				Data.CONTENT_URI, RAW_PROJECTION,
				RAW_CONTACTS_WITHOUT_TEAM_WHERE, new String[] { teamId }, null);
		try {
			if (conRawIdCursor != null)
				return conRawIdCursor.getCount();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (conRawIdCursor != null && !conRawIdCursor.isClosed()) {
				conRawIdCursor.close();
			}
		}
		return 0;
	}

	/**
	 * 查询不在某一个分组里面的联系人集合.
	 * 
	 * @param cr
	 * @param teamId
	 * @return
	 */
	public static ArrayList<ContactBean> getContactWithoutTeamId(String teamId,
			int start, int end) {
		mContext = MyApplication.nowApplication.getApplicationContext();
		// 查询组中的联系人人员.
		Cursor conRawIdCursor = mContext.getContentResolver().query(
				Data.CONTENT_URI, RAW_PROJECTION,
				RAW_CONTACTS_WITHOUT_TEAM_WHERE, new String[] { teamId }, null);
		ArrayList<ContactBean> users = new ArrayList<ContactBean>();
		try {
			if (conRawIdCursor != null) {
				int rawIdIndex = conRawIdCursor
						.getColumnIndex(Data.RAW_CONTACT_ID);
				while (conRawIdCursor.moveToNext()) {
					ContactBean b = new ContactBean();
					b.id = "" + conRawIdCursor.getLong(rawIdIndex);
					b.number = getPhone(mContext, b.id);
					b.displayName = getConDisplayNameByRawId(conRawIdCursor
							.getLong(rawIdIndex));
					users.add(b);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (conRawIdCursor != null && !conRawIdCursor.isClosed()) {
				conRawIdCursor.close();
			}
		}
		return users;
	}

	/**
	 * 得到联系人的名称.
	 * 
	 * @param rawId
	 * @return
	 */
	private static String getConDisplayNameByRawId(long rawId) {
		String _nm = contactToName.get("" + rawId);
		if (_nm == null) {
			String[] name = getNameByRawId(rawId);

			String displayName = null;
			if (name[2] != null) {
				displayName = name[2];
			} else if (name[0] != null) {
				displayName = name[0] + "" + name[1];
			} else if (name[1] != null) {
				displayName = name[1];
			}
			_nm = displayName;
			contactToName.put("" + rawId, displayName);
		}
		return _nm;
	}

	/**
	 * 得到历史联系人数量.
	 * 
	 * @return
	 */
	public static int getHistoryBeanCount() {
		mContext = MyApplication.nowApplication.getApplicationContext();
		Cursor callLogCursor = mContext.getContentResolver().query(
				CallLog.Calls.CONTENT_URI,
				new String[] { "_id", "new", "number" }, null, null, null);
		try {
			if (callLogCursor != null) {
				HashSet<String> sss = new HashSet<String>();
				for (int i = 0; i < callLogCursor.getCount(); i++) {
					callLogCursor.moveToNext();
					sss.add(callLogCursor.getString(callLogCursor
							.getColumnIndex("number")));
				}
				return sss.size();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (callLogCursor != null && !callLogCursor.isClosed()) {
				callLogCursor.close();
			}
		}
		return 0;
	}

	/**
	 * 得到最近联系人集合.
	 * 
	 * @param cr
	 * @param start
	 * @param end
	 * @return
	 */
	public static ArrayList<HistoryBean> getHistoryBeans(ContentResolver cr,
			int start, int end) {
		LinkedHashMap<String, HistoryBean> mCallLogGroupMap = new LinkedHashMap<String, HistoryBean>();
		Cursor callLogCursor = cr.query(CallLog.Calls.CONTENT_URI,
				new String[] { "_id", "new", "number" }, null, null,
				"date desc limit " + start + "," + end);
		try {
			if (callLogCursor != null) {
				for (int i = 0; i < callLogCursor.getCount(); i++) {
					callLogCursor.moveToNext();
					// 历史记录电话号码.
					String strCallLogNumber = callLogCursor
							.getString(callLogCursor.getColumnIndex("number"));
					// 电话号码不重复记录.
					if (mCallLogGroupMap.get(strCallLogNumber) == null) {
						HistoryBean nowCallLogBean = new HistoryBean();
						nowCallLogBean.historyId = callLogCursor
								.getString(callLogCursor.getColumnIndex("_id"));
						nowCallLogBean.newCallLog = callLogCursor
								.getString(callLogCursor.getColumnIndex("new"));
						nowCallLogBean.number = callLogCursor
								.getString(callLogCursor
										.getColumnIndex("number"));
						// 根据电话号码查询联系人.
						Uri uriNumberToContacts = Uri
								.parse("content://com.android.contacts/data/phones/filter/"
										+ strCallLogNumber);
						Cursor c = cr.query(uriNumberToContacts,
								new String[] { "display_name" }, null, null,
								null);
						if (c != null && c.getCount() > 0) {
							c.moveToFirst();
							String displayName = c.getString(c
									.getColumnIndex("display_name"));
							if (displayName != null) {
								nowCallLogBean.displayName = displayName;
							}
							c.close();
						}
						mCallLogGroupMap.put(strCallLogNumber, nowCallLogBean);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (callLogCursor != null && !callLogCursor.isClosed()) {
				callLogCursor.close();
			}
		}
		ArrayList<HistoryBean> callLogBeans = new ArrayList<HistoryBean>();
		callLogBeans.addAll(mCallLogGroupMap.values());
		return callLogBeans;

	}

	/**
	 * 得到全部联系人的数量.
	 * 
	 * @return
	 */
	public static int queryContactCount() {
		mContext = MyApplication.nowApplication.getApplicationContext();
		Uri uri = ContactsContract.Contacts.CONTENT_URI;
		String[] projection = new String[] { ContactsContract.Contacts._ID };
		String selection = ContactsContract.Contacts.IN_VISIBLE_GROUP + " = '"
				+ ("1") + "'";
		Cursor cc = mContext.getContentResolver().query(uri, projection,
				selection, null, null);
		try {
			if (cc != null) {
				return cc.getCount();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cc != null && !cc.isClosed()) {
				cc.close();
			}
		}
		return 0;
	}

	/**
	 * 得到联系人集合.
	 * 
	 * @param mShowInvisible
	 * @param start
	 * @param limit
	 * @return
	 */
	public static ArrayList<ContactBean> newQueryContact(
			boolean mShowInvisible, int start, int limit) {
		mContext = MyApplication.nowApplication.getApplicationContext();
		ContentResolver cr = mContext.getContentResolver();
		ArrayList<ContactBean> data = new ArrayList<ContactBean>();
		Uri uri = ContactsContract.Contacts.CONTENT_URI;
		String[] projection = new String[] { ContactsContract.Contacts._ID,
				ContactsContract.Contacts.DISPLAY_NAME,
				ContactsContract.Contacts.HAS_PHONE_NUMBER };
		String selection = ContactsContract.Contacts.IN_VISIBLE_GROUP + " = '"
				+ (mShowInvisible ? "0" : "1") + "'";
		String[] selectionArgs = null;
		String sortOrder = ContactsContract.Contacts.DISPLAY_NAME
				+ " COLLATE LOCALIZED ASC limit " + start + " , " + limit;
		Cursor mCursor = cr.query(uri, projection, selection, selectionArgs,
				sortOrder);
		if (mCursor == null)
			return null;
		try {
			while (mCursor.moveToNext()) {
				ContactBean map = new ContactBean();
				String name = mCursor
						.getString(mCursor
								.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
				map.displayName = name;
				String[] s = getPyin(name);
				map.pinyin = s[0];
				map.allFirstName = s[2];
				map.firstLetter = s[1];
				map.id = ""
						+ mCursor.getLong(mCursor
								.getColumnIndex(ContactsContract.Contacts._ID));
				map.hasPhone = mCursor
						.getString(mCursor
								.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
				map.number = getPhone(map.hasPhone, mContext, map.id);
				if (map.number == null || "".equals(map.number.trim())) {
					continue;
				}
				data.add(map);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (mCursor != null && !mCursor.isClosed()) {
				mCursor.close();
			}
		}
		return data;
	}

	public static String[] getPyin(String name) {
		String[] s = nameToPyin.get(name);
		if (s == null) {
			s = PingYinUtil.getPy(name);
			nameToPyin.put(name, s);
		}
		return s;
	}

	public static String getPhoneType(int type) {
		if (type == 1) {
			return "家庭";
		} else if (type == 2) {
			return "手机";
		} else if (type == 3) {
			return "工作";
		} else
			return "其他";
	}

	/**
	 * 得到指定联系人的相关名称信息.
	 * 
	 * @param rawId
	 * @return
	 */
	public static String[] getNameByRawId(long rawId) {
		mContext = MyApplication.nowApplication.getApplicationContext();
		ContentResolver cr = mContext.getContentResolver();
		String name[] = new String[3];
		Cursor cursor = null;
		try {
			cursor = cr.query(Data.CONTENT_URI, null, Data.RAW_CONTACT_ID
					+ " = " + rawId + " and " + Data.MIMETYPE + " = '"
					+ StructuredName.CONTENT_ITEM_TYPE + "'", null, null);
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				name[0] = cursor.getString(cursor
						.getColumnIndex(StructuredName.FAMILY_NAME));
				if (name[0] == null) {
					name[0] = "";
				}
				name[1] = cursor.getString(cursor
						.getColumnIndex(StructuredName.GIVEN_NAME));
				if (name[1] == null) {
					name[1] = "";
				}
				name[2] = cursor.getString(cursor
						.getColumnIndex(StructuredName.DISPLAY_NAME));
				if (name[2] == null) {
					name[2] = name[0] + name[1];
				}

				return name;
			}
			return null;
		} catch (Exception e) {

			return null;
		} finally {
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
		}
	}

	/**
	 * 得到指定联系人的电话.
	 * 
	 * @deprecated
	 * @param hasPhone
	 * @param ct
	 * @param id
	 * @return
	 */
	public static String getPhone(String hasPhone, Context ct, String id) {
		String numberAndname = contactToNumber.get(id);
		if (numberAndname != null) {
			return numberAndname;
		} else {
			if (hasPhone.compareTo("1") == 0) {
				return getPhone(ct, id);
			} else
				return "";
		}
	}

	/**
	 * 
	 * @param ct
	 * @param contactId
	 * @return
	 */
	public static String getPhone(Context ct, String contactId) {
		String numberAndname = contactToNumber.get(contactId);
		if (numberAndname != null) {
			return numberAndname;
		} else {
			String ans = "";
			// 根据联系人的ID获取该联系人的电话号码的crusor，然后遍历该联系人的所有号码
			Cursor phoneCursor = ct.getContentResolver().query(
					ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
					null,
					ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = "
							+ contactId, null, null);
			if (phoneCursor != null) {
				phoneCursor.moveToFirst();

				if (phoneCursor.getCount() > 0) {
					do {
						String number_map = arrayToString(phoneCursor
								.getString(
										phoneCursor
												.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
								.split("-"));
						ans = number_map;
						break;// 只得到第一个电话即可.
					} while (phoneCursor.moveToNext());
					contactToNumber.put(contactId, ans);
					return ans;
				}
				return "";
			} else
				return "";
		}
	}

	private static String arrayToString(String[] a) {
		StringBuilder bu = new StringBuilder();
		for (String s : a)
			bu.append(s);
		return bu.toString();
	}

	/* 下面是使用LruCache进行缓存处理的代码* */

	private final int hardCacheSize = 8 * 1024 * 1024;// 8M private final
	private LruCache<String, Bitmap> hardBitmapCache = new LruCache<String, Bitmap>(
			hardCacheSize) {

		@Override
		public int sizeOf(String key, Bitmap v) {
			return v.getRowBytes() * v.getHeight();
		}

		@Override
		protected void entryRemoved(boolean evicted, String key,
				Bitmap oldValue, Bitmap newValue) {
			Log.v("tag", "hard cache 满了，将值放到软缓存中");
			softBitmapCache.put(key, new SoftReference<Bitmap>(oldValue));
		}
	};

	private static final int SOFT_CAPACITY = 40;
	private LinkedHashMap<String, SoftReference<Bitmap>> softBitmapCache = new LinkedHashMap<String, SoftReference<Bitmap>>(
			SOFT_CAPACITY, 0.75f, true) {

		@Override
		public SoftReference<Bitmap> put(String key, SoftReference<Bitmap> value) {
			return super.put(key, value);
		}

		@Override
		protected boolean removeEldestEntry(
				LinkedHashMap.Entry<String, SoftReference<Bitmap>> eldest) {
			if (size() > SOFT_CAPACITY) {
				Log.v("tag", "软引用极限到了，删除一个!");
				return true;
			}
			return false;
		}
	};

	public Bitmap getBitmap(String key) {
		synchronized (hardBitmapCache) {
			final Bitmap m = hardBitmapCache.get(key);
			if (m != null)
				return m;
		}
		synchronized (softBitmapCache) {
			SoftReference<Bitmap> ref = softBitmapCache.get(key);
			if (ref != null) {
				final Bitmap m = ref.get();
				if (m != null)
					return m;
				else {
					Log.v("tag", "软引用中的已经被回收了!");
					softBitmapCache.remove(key);
				}
			}
		}
		return null;
	}

	public static String getSdRoot(String packageName) {
		File sdCardFile = Environment.getExternalStorageDirectory();
		File f = new File(sdCardFile.getPath() + File.separator + packageName
				+ File.separator + "assets" + File.separator);
		return f.getAbsolutePath();
	}

	/**
	 * 添加分组.
	 * 
	 * @param teamName
	 * @param contacts
	 * @param override
	 *            false就新建分组，true就删除以前旧的分组信息.
	 * @return
	 */
	public static int addTeam(String teamName,
			ArrayList<TeamAddManager.P> contacts, boolean override) {
		mContext = MyApplication.nowApplication.getApplicationContext();
		try {
			long teamId = checkExistByName(teamName);
			if (contacts.size() == 0) {// 如果联系人没有选择，就清空当前分组.
				if (teamId > 0)
					mContext.getContentResolver()
							.delete(ContactsContract.Data.CONTENT_URI,
									ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID
											+ "=? and "
											+ ContactsContract.CommonDataKinds.GroupMembership.MIMETYPE
											+ "=?",
									new String[] {
											"" + teamId,
											ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE });
				return 1;
			}
			if (teamId == -1)// 说明是新建分组
			{
				ContentValues values = new ContentValues();
				values.put(Groups.TITLE, teamName);
				Uri u = mContext.getContentResolver().insert(
						Groups.CONTENT_URI, values);
				teamId = ContentUris.parseId(u);
			} else {// 说明有重复的分组
				if (override)// 如果要覆盖原有的分组,先删除原有的分组里面的全部数据！
				{
					mContext.getContentResolver()
							.delete(ContactsContract.Data.CONTENT_URI,
									ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID
											+ "=? and "
											+ ContactsContract.CommonDataKinds.GroupMembership.MIMETYPE
											+ "=?",
									new String[] {
											"" + teamId,
											ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE });
				} else {// 否则新建一个分组
					int c = 1;
					String _oldname = teamName;
					teamName = _oldname + c++;
					// 循环判断新建的分组名是否存在
					for (int i = 0; i < 1;) {
						long temp = checkExistByName(teamName);
						if (temp != -1)
							teamName = _oldname + c++;
						else {

							ContentValues values = new ContentValues();
							values.put(Groups.TITLE, teamName);
							values.put(ContactsContract.Groups.GROUP_VISIBLE,
									"1");
							values.put(ContactsContract.Groups.DELETED, "0");
							// ContactsContract.Groups.GROUP_VISIBLE
							// + "=1 and " + ContactsContract.Groups.DELETED +
							// "=0";
							values.put(Groups.TITLE, teamName);
							Uri u = mContext.getContentResolver().insert(
									Groups.CONTENT_URI, values);
							teamId = ContentUris.parseId(u);
							i++;
						}
					}

				}
			}
			// 新建分组
			for (TeamAddManager.P p : contacts) {
				ContentValues values = new ContentValues();
				values.put(
						ContactsContract.CommonDataKinds.GroupMembership.RAW_CONTACT_ID,
						p.id);
				values.put(
						ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID,
						teamId);
				values.put(
						ContactsContract.CommonDataKinds.GroupMembership.MIMETYPE,
						ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE);
				mContext.getContentResolver().insert(
						ContactsContract.Data.CONTENT_URI, values);
			}
			return 1;
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}

	}

	public static int addCaogao(String name, String message, String time) {
		mContext = MyApplication.nowApplication.getApplicationContext();
		try {
			ContentValues mNewValues = new ContentValues();
			mNewValues.put(DBHelper.CAOGAO_MESSAGE, message);
			mNewValues.put(DBHelper.CAOGAO_NAME, name);
			mNewValues.put(DBHelper.CAOGAO_TIME, time);
			mContext.getContentResolver().insert(ContactProvider.CAOGAO_URI,
					mNewValues);
			return 1;
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}

	}

	public static ArrayList<MessageBean> queryCaogao() {
		ArrayList<MessageBean> beans = new ArrayList<MessageBean>();
		try {
			ContentResolver cr = MyApplication.nowApplication
					.getApplicationContext().getContentResolver();
			Cursor cursor = cr.query(ContactProvider.CAOGAO_URI, null, null,
					null, null);
			if (cursor != null) {
				for (int i = 0; i < cursor.getCount(); i++) {
					cursor.moveToNext();
					MessageBean bean = new MessageBean();
					bean.smsAddress = cursor.getString(cursor
							.getColumnIndex(DBHelper.CAOGAO_NAME));
					bean.smsBody = cursor.getString(cursor
							.getColumnIndex(DBHelper.CAOGAO_MESSAGE));
					bean.smsDate = Long.parseLong(cursor.getString(cursor
							.getColumnIndex(DBHelper.CAOGAO_TIME)));
					bean.isCaogao = true;
					beans.add(bean);
				}
				cursor.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return beans;

	}

	public static String listToString(ArrayList<String> l) {
		StringBuilder bui = new StringBuilder();
		for (String n : l) {
			bui.append(n + ",");
		}
		if (bui.length() > 0)
			bui = bui.deleteCharAt(bui.lastIndexOf(","));
		return bui.toString();
	}

	/**
	 * 群发短信.
	 * 
	 * @param address
	 * @param messageText
	 */
	public static void sendMessage(String address, String messageText) {
		mContext = MyApplication.nowApplication.getApplicationContext();
		if (address.trim().length() != 0 && messageText.trim().length() != 0) {
			// 拆分短信
			String[] temp = address.split(",");
			int k = temp.length;
			int success = 0;
			int faliure = 0;
			for (int i = 0; i < k; ++i) {
				try {
					SmsManager sms = SmsManager.getDefault();
					PendingIntent pi = PendingIntent.getBroadcast(mContext, 0,
							new Intent(), 0);
					List<String> texts = sms.divideMessage(messageText);
					for (String text : texts) {
						sms.sendTextMessage(temp[i], null, text, pi, null);
					}
					Thread.sleep(100);
					success++;
				} catch (Exception e) {
					e.printStackTrace();
					faliure++;
				}
				// 将短信保存在发件箱

				try {
					ContentValues values = new ContentValues();
					values.put("address", temp[i]);
					values.put("body", messageText);
					mContext.getContentResolver().insert(
							Uri.parse("content://sms/sent"), values);
				} catch (Exception e) {
					e.printStackTrace();

				}
			}
			Toast.makeText(mContext, "发送成功" + success + "条.失败" + faliure + "条",
					Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(mContext, "联系人或短信内容不能为空", Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * 判断分组名称是否已经存在!
	 * 
	 * @param teamName
	 * @return
	 */
	public static long checkExistByName(String teamName) {
		mContext = MyApplication.nowApplication.getApplicationContext();
		Cursor result = mContext.getContentResolver().query(Groups.CONTENT_URI,
				new String[] { Groups._ID }, Groups.TITLE + "=?",
				new String[] { teamName }, null);
		if (result != null) {
			if (result.getCount() > 0) {
				result.moveToFirst();
				return result.getLong(result.getColumnIndex("_id"));
			} else
				return (long) -1;
		}
		return (long) -1;
	}
}
