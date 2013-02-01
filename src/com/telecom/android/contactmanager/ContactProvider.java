package com.telecom.android.contactmanager;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

/**
 * 主要的数据管理器.
 * 
 * @author lsq
 * 
 */
public class ContactProvider extends ContentProvider {

	private DBHelper dbHelper;
	private SQLiteDatabase contactsDB;

	public static final String AUTHORITY = "com.example.android.contactmanager.ContactProvider";
	public static final Uri CAOGAO_URI = Uri.parse("content://" + AUTHORITY
			+ "/" + DBHelper.TABLE_CAOGAO);

	/* ===================================================== */
	public static final int CAOGAOS = 5;
	public static final int CAOGAO_ID = 6;
	private static final UriMatcher uriMatcher;
	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(AUTHORITY, "caogao", CAOGAOS);
		uriMatcher.addURI(AUTHORITY, "caogao/#", CAOGAO_ID);
	}

	/* ===================================================== */

	@Override
	public boolean onCreate() {
		dbHelper = new DBHelper(getContext());
		// 执行创建数据库
		contactsDB = dbHelper.getWritableDatabase();
		return (contactsDB == null) ? false : true;
	}

	// 删除指定数据列
	@Override
	public int delete(Uri uri, String where, String[] selectionArgs) {
		int count;
		switch (uriMatcher.match(uri)) {
		case CAOGAOS:
			count = contactsDB.delete(DBHelper.TABLE_CAOGAO, where,
					selectionArgs);
			break;
		case CAOGAO_ID:
			String caogao = uri.getPathSegments().get(1);
			count = contactsDB.delete(
					DBHelper.TABLE_CAOGAO,
					DBHelper.CAOGAO_ID
							+ "="
							+ caogao
							+ (!TextUtils.isEmpty(where) ? " AND (" + where
									+ ")" : ""), selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	// URI类型转换
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)) {
		case CAOGAOS:
			return "vnd.android.cursor.dir/vnd.hvming.android.CAOGAOS";
		case CAOGAO_ID:
			return "vnd.android.cursor.item/vnd.hvming.android.CAOGAOS";
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
	}

	/**
	 * 返回uri的类型.
	 * 
	 * @param uri
	 * @return
	 */
	public static int getTypeNum(Uri uri) {
		switch (uriMatcher.match(uri)) {
		case CAOGAOS:
			return CAOGAOS;
		case CAOGAO_ID:
			return CAOGAO_ID;
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
	}

	// 插入数据
	public Uri insert(Uri uri, ContentValues initialValues) {
		if (uriMatcher.match(uri) != CAOGAOS) {
			throw new IllegalArgumentException("URI " + uri
					+ "非法。插入的uri类型不应该是：" + uriMatcher.match(uri));
		}
		ContentValues values;
		if (initialValues != null) {
			values = new ContentValues(initialValues);
		} else {
			throw new IllegalArgumentException("initialValues不得为空");
		}
		if (values.containsKey(DBHelper.CAOGAO_NAME) == false) {
			throw new IllegalArgumentException("caogao名字不可以为空!");
		}
		if (values.containsKey(DBHelper.CAOGAO_MESSAGE) == false) {
			throw new IllegalArgumentException("caogao内容不可以为空!");
		}
		long rowId = contactsDB.insert(DBHelper.TABLE_CAOGAO, null, values);
		if (rowId > 0) {
			Uri noteUri = ContentUris.withAppendedId(CAOGAO_URI, rowId);
			getContext().getContentResolver().notifyChange(noteUri, null);
			return noteUri;
		}
		throw new SQLException("Failed to insert row into " + uri);

	}

	// 查询数据
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		switch (uriMatcher.match(uri)) {
		case CAOGAOS:
		case CAOGAO_ID:
			qb.setTables(DBHelper.TABLE_CAOGAO);
			break;
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}

		switch (uriMatcher.match(uri)) {
		case CAOGAO_ID:
			qb.appendWhere(DBHelper.CAOGAO_ID + "="
					+ uri.getPathSegments().get(1));
			break;
		default:
			break;
		}

		String orderBy;
		if (TextUtils.isEmpty(sortOrder)) {
			if (uriMatcher.match(uri) == CAOGAOS)
				orderBy = DBHelper.CAOGAO_ID + " desc";
			else
				orderBy = null;
		} else {
			orderBy = sortOrder;
		}
		Cursor c = qb.query(contactsDB, projection, selection, selectionArgs,
				null, null, orderBy);
		if (c != null)
			c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	// 更新数据库
	public int update(Uri uri, ContentValues values, String where,
			String[] selectionArgs) {
		int count;
		switch (uriMatcher.match(uri)) {
		case CAOGAOS:
			count = contactsDB.update(DBHelper.TABLE_CAOGAO, values, where,
					selectionArgs);
			break;
		case CAOGAO_ID:
			String caogao = uri.getPathSegments().get(1);
			count = contactsDB.update(
					DBHelper.TABLE_CAOGAO,
					values,
					DBHelper.CAOGAO_ID
							+ "="
							+ caogao
							+ (!TextUtils.isEmpty(where) ? " AND (" + where
									+ ")" : ""), selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}
}
