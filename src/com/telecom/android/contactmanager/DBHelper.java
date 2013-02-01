package com.telecom.android.contactmanager;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 数据库操作工具.
 * 
 * @author lsq
 * 
 */
public class DBHelper extends SQLiteOpenHelper {
	public static final String DATABASE_NAME = "com.example.android.database";// 数据库名
	public static final int DATABASE_VERSION = 1; // 版本
	// 草稿表
	public static final String TABLE_CAOGAO = "caogao";
	// 主键
	public static final String CAOGAO_ID = "_id";
	// 草稿
	public static final String CAOGAO_NAME = "peoples";
	// 草稿
	public static final String CAOGAO_PEOPLE_ID = "peopleIds";
	// 草稿信息
	public static final String CAOGAO_MESSAGE = "message";
	// 时间.
	public static final String CAOGAO_TIME = "time";

	public static final String[] CAOGAO_PROJECTION = { CAOGAO_ID, CAOGAO_NAME,
			CAOGAO_MESSAGE, CAOGAO_TIME };

	private static final String CREATE_CAOGAO = "CREATE TABLE " + TABLE_CAOGAO
			+ " (" + CAOGAO_ID + " integer primary key autoincrement,"
			+ CAOGAO_NAME + " text," + CAOGAO_TIME + " text," + CAOGAO_MESSAGE
			+ " text );";

	public DBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_CAOGAO);
	}

	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_CAOGAO);
		onCreate(db);
	}
}
