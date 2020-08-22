package com.royran.timebrief.ui.views.searchview;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Carson_Ho on 17/8/10.
 */

// 继承自SQLiteOpenHelper数据库类的子类
public class RecordSQLiteOpenHelper extends SQLiteOpenHelper {

    private static String name = "history.db";
    private static Integer version = 1;

    public RecordSQLiteOpenHelper(Context context) {
        super(context, name, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 打开数据库 & 建立了一个叫records的表，里面只有一列name来存储历史记录：
        String sql = getCreateTableSql();
        Logger.d("execSQL: %s", sql);
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    private String getQuerySQL(String history) {
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT history FROM search_history ");
        builder.append("WHERE history ");
        builder.append("LIKE '%").append(history).append("%'");
        builder.append("ORDER BY query_time DESC");
        return builder.toString();
    }

    public List<String> queryHistories(String history) {
        List<String> records = new ArrayList<>();
        String sql = getQuerySQL(history);
        SQLiteDatabase db = this.getReadableDatabase();
        Logger.d("queryHistories - sql: %s", sql);
        Cursor cursor = db.rawQuery(sql, null);
        while (cursor.moveToNext()) {
            String record = cursor.getString(0);
            records.add(record);
        }
        cursor.close();
        db.close();
        Logger.d("queryHistories - total records: %d", records.size());
        return records;
    }

    public boolean insertOrUpdateHistory(String history) {
        String sql = getInsertSQL(history);
        return execSQL(sql);
    }


    public boolean deleteTable() {
        String sql = "DELETE FROM search_history";
        return execSQL(sql);
    }

    private String getCreateTableSql() {
        StringBuilder builder = new StringBuilder();
        builder.append("CREATE TABLE search_history");
        builder.append("(");
        builder.append("id INTEGER PRIMARY KEY AUTOINCREMENT,");
        builder.append("history TEXT NOT NULL UNIQUE,");
        builder.append("query_time DATATIME NOT NULL DEFAULT CURRENT_TIMESTAMP");
        builder.append(")");
        return builder.toString();
    }

    private String getInsertSQL(String history) {
        StringBuilder builder = new StringBuilder();
        builder.append("INSERT OR REPLACE INTO search_history(history, query_time) ");
        builder.append("VALUES(");
        builder.append("'").append(history).append("',");
        builder.append("strftime('%Y-%m-%d %H:%M:%f','now'))");
        return builder.toString();
    }

    private boolean execSQL(String sql) {
        Logger.d("execSQL: %s", sql);
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            db.execSQL(sql);
            db.close();
            return true;
        } catch (SQLException e) {
            Logger.e("execSQL failed, error: %s", e.getMessage());
            return false;
        }
    }
}
