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
	private static final int MEMENTOS = 1;//������������������ƥ��URI�ķ���ֵ
	private static final int MEMENTO = 2;
	MyDatabaseHelper dbHelper;
	SQLiteDatabase  db;
	static {//���URIƥ�����,�����ж�URI������
		matcher.addURI(Mementos.AUTHORITY, "mementos", MEMENTOS);
		matcher.addURI(Mementos.AUTHORITY, "memento/#", MEMENTO);
	}

	public boolean onCreate() {
		dbHelper = new MyDatabaseHelper(getContext(), "memento.db", null,1);
		//�������ݿ⹤����,����ȡ���ݿ�ʵ��
		db = dbHelper.getReadableDatabase();
		return true;
	}

	public Uri insert(Uri uri, ContentValues values) {//��Ӽ�¼		
		long rowID = db.insert("memento_tb", Mementos.Memento._ID, values);
		if (rowID > 0) {//�����ӳɹ�����֪ͨ���ݿ��¼��������
			Uri mementoUri = ContentUris.withAppendedId(uri, rowID);
			getContext().getContentResolver().notifyChange(mementoUri, null);
			return mementoUri;
		}
		return null;
	}
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int num = 0;//ɾ����¼	�����ڼ�¼ɾ���ļ�¼��
		switch (matcher.match(uri)) {
		case MEMENTOS://ɾ��������¼
			num = db.delete("memento_tb", selection, selectionArgs);
			break;
		case MEMENTO://ɾ��ָ��ID��Ӧ�ļ�¼
			long id = ContentUris.parseId(uri);//��ȡID
			String where = Mementos.Memento._ID + "=" + id;//ID�ֶ�����ϵ�����
			if (selection != null && !"".equals(selection)) {
				where = where + " and " + selection;//ƴ���������
			}
			num = db.delete("memento_tb", where, selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("δ֪Uri��" + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);//֪ͨ�仯
		return num;
	}
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {//���¼�¼
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
			throw new IllegalArgumentException("δ֪Uri��" + uri);
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
			throw new IllegalArgumentException("δ֪Uri��" + uri);
		}
	}

	public String getType(Uri uri) {
		switch (matcher.match(uri)) {
		case MEMENTOS:
			return "vnd.android.cursor.dir/mementos";
		case MEMENTO:
			return "vnd.android.cursor.item/memento";
		default:
			throw new IllegalArgumentException("δ֪Uri��" + uri);
		}
	}
}
