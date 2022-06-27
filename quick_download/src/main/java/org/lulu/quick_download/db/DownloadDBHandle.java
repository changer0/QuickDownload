package org.lulu.quick_download.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;

import org.lulu.quick_download.DownloadUtil;
import org.lulu.quick_download.QuickDownloadInitializer;

import java.util.ArrayList;
import java.util.List;

/**
 * author: changer0
 * date: 2022/6/26
 */
public class DownloadDBHandle {

    public static final String DB_NAME = "quick_download.db";

    public static final String ID = "id";
    public static final String TOTAL_LENGTH = "total_length";
    public static final String STATE = "state";
    public static final String INDEX = "segment_index";
    public static final String DOWNLOAD_ID = "download_id";
    public static final String DOWNLOAD_POS = "download_pos";

    public static final String TABLE_FILE_INFO = "name_table_file_info";
    public static final String TABLE_SEGMENT_INFO = "name_table_segment_info";

    private static volatile DownloadDBHandle sInstance;

    private final DBHelper dbHelper;

    private DownloadDBHandle() {
        dbHelper = new DBHelper(QuickDownloadInitializer.sContext);
    }

    public static DownloadDBHandle getInstance() {
        if (sInstance == null) {
            synchronized (DownloadDBHandle.class) {
                if (sInstance == null) {
                    sInstance = new DownloadDBHandle();
                }
            }
        }
        return sInstance;
    }

    /**
     * 插入一条 DownloadInfo
     */
    public synchronized void saveFileInfo(FileInfo info) {
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            db.beginTransaction();
            ContentValues initialValues = new ContentValues();
            initialValues.put(ID, info.getId());
            initialValues.put(TOTAL_LENGTH, info.getLength());
            initialValues.put(STATE, info.getStatus());
            db.replace(TABLE_FILE_INFO, null, initialValues);
            db.setTransactionSuccessful();
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.endTransaction();
            }
            DownloadUtil.close(db);
            dbHelper.close();
        }
    }

    public synchronized FileInfo getFileInfo(String id) {
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = dbHelper.getWritableDatabase();
            cursor = db.query(TABLE_FILE_INFO, null, " " + ID + " = ? ", new String[]{id}, null, null, null);
            if (cursor.moveToNext()) {
                FileInfo fileInfo = new FileInfo(id);
                fileInfo.setLength(cursor.getLong(cursor.getColumnIndex(TOTAL_LENGTH)));
                fileInfo.setStatus(cursor.getInt(cursor.getColumnIndex(STATE)));
                return fileInfo;
            }
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            DownloadUtil.close(cursor);
            DownloadUtil.close(db);
            dbHelper.close();
        }
        return null;
    }


    /**
     * 插入一条 DownloadInfo
     */
    public synchronized void saveSegmentInfo(SegmentInfo segment) {
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            db.beginTransaction();
            ContentValues initialValues = new ContentValues();
            initialValues.put(ID, segment.getId());
            initialValues.put(DOWNLOAD_ID, segment.getDownloadId());
            initialValues.put(DOWNLOAD_POS, segment.getDownloadPos());
            initialValues.put(INDEX, segment.getIndex());
            initialValues.put(STATE, segment.getFinished());
            db.replace(TABLE_SEGMENT_INFO, null, initialValues);
            db.setTransactionSuccessful();
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.endTransaction();
            }
            DownloadUtil.close(db);
            dbHelper.close();
        }
    }

    @NonNull
    public synchronized List<SegmentInfo> getSegmentInfo(String downloadId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        List<SegmentInfo> list = new ArrayList<>();
        try {
            db = dbHelper.getWritableDatabase();
            cursor = db.query(TABLE_SEGMENT_INFO, null, " " + DOWNLOAD_ID  + " = ? ", new String[]{downloadId}, null, null, null);

            while (cursor.moveToNext()) {
                list.add(new SegmentInfo(
                        cursor.getString(cursor.getColumnIndex(ID)),
                        cursor.getString(cursor.getColumnIndex(DOWNLOAD_ID)),
                        cursor.getLong(cursor.getColumnIndex(DOWNLOAD_POS)),
                        cursor.getInt(cursor.getColumnIndex(INDEX)),
                        cursor.getInt(cursor.getColumnIndex(STATE))
                ));
            }
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            DownloadUtil.close(cursor);
            DownloadUtil.close(db);
            dbHelper.close();
        }
        return list;
    }

    public synchronized int deleteDownloadSegment(String downloadId) {
        int ret = 0;
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            db.beginTransaction();
            ret = db.delete(TABLE_SEGMENT_INFO, DOWNLOAD_ID + " = ? ", new String[]{downloadId});
            db.setTransactionSuccessful();
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.endTransaction();
            }
            DownloadUtil.close(db);
            dbHelper.close();
        }
        return ret;
    }




    public static class DBHelper extends SQLiteOpenHelper {
        public DBHelper(Context context) {
            super(context, DB_NAME, null, DownloadUtil.getVersionCode(context));
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            //下载信息表
            db.execSQL("create table if not exists " + TABLE_FILE_INFO + " ("
                    + ID + " TEXT NOT NULL PRIMARY KEY,"
                    + TOTAL_LENGTH + " LONG DEFAULT 0,"
                    + STATE + " INTEGER DEFAULT 0"
                    + ")");
            //下载块表
            db.execSQL("create table if not exists " + TABLE_SEGMENT_INFO + " ("
                    + ID + " TEXT PRIMARY KEY,"
                    + DOWNLOAD_ID + " TEXT DEFAULT 0,"
                    + DOWNLOAD_POS + " LONG DEFAULT 0,"
                    + INDEX + " INTEGER DEFAULT 0,"
                    + STATE + " INTEGER DEFAULT 0"
                    + ")");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            //对于下载来讲，其实是不存在这种升级数据库的业务的.所以我们直接删除重新建表
            db.execSQL("drop table if exists " + TABLE_FILE_INFO);
            db.execSQL("drop table if exists " + TABLE_SEGMENT_INFO);
            onCreate(db);
        }
    }


}
