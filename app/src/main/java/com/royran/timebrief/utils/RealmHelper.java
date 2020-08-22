package com.royran.timebrief.utils;

import com.orhanobut.logger.Logger;
import com.royran.timebrief.models.RealmTimeRecord;
import com.royran.timebrief.models.TimeStatus;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

public class RealmHelper {
    public static final String DB_NAME = "TimeRecordingRealmDB";
    private static Realm mRealm;
    private static List<RealmTimeRecord> mRecords;
    private static boolean mIsChanged = true;

    public static void initRealmHelper(Realm realm) {
        mRealm = realm;
    }

    public static void addTimeRecord(final RealmTimeRecord record) {
        if (mRealm == null) {
            return;
        }
        if (mRealm.isClosed()) {
            mRealm = Realm.getDefaultInstance();
        }
        mRealm.executeTransaction(realm -> {
            Number maxValue = mRealm.where(RealmTimeRecord.class).max("id");
            long id = (maxValue != null) ? maxValue.longValue() + 1 : 0;
            record.setId(id);
            if (record.getActivityDateMillis() == 0) {
                record.setActivityDateMillis(record.getCreateTimeMillis());
            }
            mRealm.copyToRealm(record);
        });
        mIsChanged = true;
    }

    public static int addTimeRecords(List<RealmTimeRecord> records) {
        if (mRealm == null) {
            return -1;
        }
        if (mRealm.isClosed()) {
            mRealm = Realm.getDefaultInstance();
        }
        mRealm.beginTransaction();
        Number maxValue = mRealm.where(RealmTimeRecord.class).max("id");
        long id = (maxValue != null) ? maxValue.longValue() + 1 : 1;
        for (RealmTimeRecord record : records) {
            record.setId(id++);
            if (record.getEndTimeMillis() != 0) {
                record.setActivityDateMillis(record.getEndTimeMillis());
            } else if (record.getActivityDateMillis() == 0) {
                record.setActivityDateMillis(record.getCreateTimeMillis());
            }
        }
        List<RealmTimeRecord> addedRecords = mRealm.copyToRealm(records);
        mRealm.commitTransaction();
        mRealm.close();
        if (addedRecords == null) {
            return -1;
        }
        mIsChanged = true;
        return addedRecords.size();
    }

    public static int deleteTimeRecordById(long id) {
        Realm realm = Realm.getDefaultInstance();
        if (realm == null) {
            return 0;
        }
        realm.beginTransaction();
        List<RealmTimeRecord> records = realm.where(RealmTimeRecord.class).equalTo("id", id).findAll();
        if (records == null) {
            realm.close();
            return 0;
        }
        int size = records.size();
        for (RealmTimeRecord record : records) {
            record.deleteFromRealm();
        }
        realm.commitTransaction();
        realm.close();
        mIsChanged = true;
        return size;
    }

    public static RealmTimeRecord queryTimeRecordById(long id) {
        if (mRealm == null) {
            return null;
        }
        if (mRealm.isClosed()) {
            mRealm = Realm.getDefaultInstance();
        }
        return mRealm.where(RealmTimeRecord.class).equalTo("id", id).findFirst();
    }

    public static List<RealmTimeRecord> queryTimeRecordsByDate(Date date) {
        Realm realm = Realm.getDefaultInstance();
        if (realm == null) {
            return new ArrayList<>();
        }
        Calendar calendar = TimeUtils.getChinaTime(date);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        calendar.clear();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        Date startDate = new Date(calendar.getTimeInMillis());
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        Date endDate = new Date(calendar.getTimeInMillis());
        Logger.d("startDate: %s, endDate: %s", TimeUtils.getChinaTimeString(startDate), TimeUtils.getChinaTimeString(endDate));
        RealmResults<RealmTimeRecord> records = realm.where(RealmTimeRecord.class).
                greaterThanOrEqualTo("activityDateMillis", startDate.getTime()).
                lessThan("activityDateMillis", endDate.getTime()).findAll();
        List<RealmTimeRecord>  results = mRealm.copyFromRealm(records);
        Collections.sort(results);
        return results;
    }

    public static boolean removeAllRecords() {
        Realm realm = Realm.getDefaultInstance();
        if (realm == null) {
            return false;
        }
        realm.executeTransaction(r -> realm.deleteAll());
        mIsChanged = true;
        return true;
    }

    public static List<RealmTimeRecord> getAllTimeRecords() {
        Realm realm = Realm.getDefaultInstance();
        if (realm == null) {
            return new ArrayList<>();
        }

        RealmResults<RealmTimeRecord> records = realm.where(RealmTimeRecord.class).findAll();
        List<RealmTimeRecord>  results = mRealm.copyFromRealm(records);
        Collections.sort(results);
        return results;
    }

    public static List<RealmTimeRecord> getAllFinishedTimeRecords() {
        Realm realm = Realm.getDefaultInstance();
        if (realm == null) {
            return new ArrayList<>();
        }
        if (mRecords != null && !mIsChanged) {
            return mRecords;
        }
        RealmResults<RealmTimeRecord> records = realm.where(RealmTimeRecord.class).
                equalTo("timeStatus", TimeStatus.FINISHED_STATUS).findAll();
        mRecords = mRealm.copyFromRealm(records);
        Collections.sort(mRecords);
        mIsChanged = false;
        return mRecords;
    }

    public static List<RealmTimeRecord> getTodayFinishedTimeRecords() {
        return getLastDaysFinishedTimeRecords(1);
    }

    public static List<RealmTimeRecord> getYesterdayFinishedTimeRecords() {
        Calendar todayCalendar = TimeUtils.getChinaDate(System.currentTimeMillis());
        long todayMillis = todayCalendar.getTimeInMillis();
        todayCalendar.add(Calendar.DAY_OF_MONTH, -1);
        long yesterdayMillis = todayCalendar.getTimeInMillis();
        return getTimeRecordsByTime(yesterdayMillis, todayMillis);
    }

    public static List<RealmTimeRecord> getLastOneWeekFinishedTimeRecords() {
        return getLastDaysFinishedTimeRecords(7);
    }

    public static List<RealmTimeRecord> getLastOneMonthFinishedTimeRecords() {
        return getLastDaysFinishedTimeRecords(30);
    }

    public static List<RealmTimeRecord> getLastDaysFinishedTimeRecords(int lastDays) {
        if (lastDays < 1) {
            return new ArrayList<>();
        }
        Calendar todayCalendar = TimeUtils.getChinaDate(System.currentTimeMillis());
        todayCalendar.add(Calendar.DAY_OF_MONTH, 1);
        long tomorrowMillis = todayCalendar.getTimeInMillis();
        todayCalendar.add(Calendar.DAY_OF_MONTH, -lastDays);
        long lastDaysMillis = todayCalendar.getTimeInMillis();
        return getTimeRecordsByTime(lastDaysMillis, tomorrowMillis);
    }

    public static List<RealmTimeRecord> getTimeRecordsByTime(long beginTimeInMillis, long endTimeInMillis) {
        Realm realm = Realm.getDefaultInstance();
        if (realm == null) {
            return new ArrayList<>();
        }
        RealmResults<RealmTimeRecord> records = realm.where(RealmTimeRecord.class).
                equalTo("timeStatus", TimeStatus.FINISHED_STATUS).
                greaterThanOrEqualTo("activityDateMillis", beginTimeInMillis).
                lessThan("activityDateMillis", endTimeInMillis).
                findAll();
        List<RealmTimeRecord>  results = mRealm.copyFromRealm(records);
        Collections.sort(results);
        return results;
    }

    public static boolean updateTimeRecord(RealmTimeRecord record) {
        if (mRealm == null || record == null) {
            return false;
        }
        if (mRealm.isClosed()) {
            mRealm = Realm.getDefaultInstance();
        }
        mRealm.beginTransaction();
        record.setUpdateTimeMillis(System.currentTimeMillis());
        RealmTimeRecord updated = mRealm.copyToRealmOrUpdate(record);
        mRealm.commitTransaction();
        mIsChanged = true;
        return updated != null;
    }

    public static void updateTimeRecords(List<RealmTimeRecord> records) {
        if (mRealm == null || records == null) {
            return;
        }
        if (mRealm.isClosed()) {
            mRealm = Realm.getDefaultInstance();
        }
        mRealm.beginTransaction();
        for (RealmTimeRecord record : mRecords) {
            record.setUpdateTimeMillis(System.currentTimeMillis());
        }
        mRealm.copyToRealmOrUpdate(mRecords);
        mRealm.commitTransaction();
        mIsChanged = true;
    }

    public static void updateAllTimeRecords() {
        updateTimeRecords(mRecords);
    }

    public static void close() {
        if (mRealm == null) {
            return;
        }
        if (!mRealm.isClosed()) {
            mRealm.close();
        }
    }
}
