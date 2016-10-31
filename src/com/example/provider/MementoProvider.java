package com.example.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class MementoProvider extends ContentProvider {
	private static UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
	private static final int MEMENTOS = 1;//定义两个常量，用于匹配URI的返回值
	private static final int MEMENTO = 2;
	MyDatabaseHelper dbHelper;
	SQLiteDatabase  db;
	static {//添加URI匹配规则,用于判断URI的类型
		matcher.addURI(Mementos.AUTHORITY, "mementos", MEMENTOS);
		matcher.addURI(Mementos.AUTHORITY, "memento/#", MEMENTO);
	}

	public boolean onCreate() {
		dbHelper = new MyDatabaseHelper(getContext(), "memento.db", null,1);
		//创建数据库工具类,并获取数据库实例
		db = dbHelper.getReadableDatabase();
		return true;
	}

	public Uri insert(Uri uri, ContentValues values) {//添加记录		
		long rowID = db.insert("memento_tb", Mementos.Memento._ID, values);
		if (rowID > 0) {//如果添加成功，则通知数据库记录发生更新
			Uri mementoUri = ContentUris.withAppendedId(uri, rowID);
			getContext().getContentResolver().notifyChange(mementoUri, null);
			return mementoUri;
		}
		return null;
	}
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int num = 0;//删除记录	，用于记录删除的记录数
		switch (matcher.match(uri)) {
		case MEMENTOS://删除多条记录
			num = db.delete("memento_tb", selection, selectionArgs);
			break;
		case MEMENTO://删除指定ID对应的记录
			long id = ContentUris.parseId(uri);//获取ID
			String where = Mementos.Memento._ID + "=" + id;//ID字段需符合的条件
			if (selection != null && !"".equals(selection)) {
				where = where + " and " + selection;//拼接条件语句
			}
			num = db.delete("memento_tb", where, selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("未知Uri：" + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);//通知变化
		return num;
	}
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {//更新记录
		int num = 0;
		switch (matcher.match(uri)) {
		case MEMENTOS:
			num = db.update("memento_tb", values, selection, selectionArgs);
			break;
		case MEMENTO:
			long id = ContentUris.parseId(uri);
			String where = Mementos.Memento._ID + "=" + id;
			if (selection != null && !"".equals(selection)) {
				where = where + " and " + selection;
			}
			num = db.update("memento_tb", values, where, selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("未知Uri：" + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return num;
	}

	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		switch (matcher.match(uri)) {
		case MEMENTOS:
			return db.query("memento_tb", projection, selection, selectionArgs,
					null, null, sortOrder);
		case MEMENTO:
			long id = ContentUris.parseId(uri);
			String where = Mementos.Memento._ID + "=" + id;
			if (selection != null && !"".equals(selection)) {
				where = where + " and " + selection;
			}
			return db.query("memento_tb", projection, where, selectionArgs,
					null, null, sortOrder);
		default:
			throw new IllegalArgumentException("未知Uri：" + uri);
		}
	}

	public String getType(Uri uri) {
		switch (matcher.match(uri)) {
		case MEMENTOS:
			return "vnd.android.cursor.dir/mementos";
		case MEMENTO:
			return "vnd.android.cursor.item/memento";
		default:
			throw new IllegalArgumentException("未知Uri：" + uri);
		}
	}
}
