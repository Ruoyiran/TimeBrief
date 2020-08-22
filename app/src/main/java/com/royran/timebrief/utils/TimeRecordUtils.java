package com.royran.timebrief.utils;

import android.util.Pair;

import com.orhanobut.logger.Logger;
import com.royran.timebrief.models.RealmTimeRecord;

import com.royran.timebrief.pb.RealmTimeRecordPb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimeRecordUtils {

    public static List<List<RealmTimeRecord>> groupAndSortByEndTime(List<RealmTimeRecord> records, boolean ascend) {
        Map<Long, List<RealmTimeRecord>> mappedRecords = new HashMap<>();
        for (RealmTimeRecord record :  records) {
            long timeMs = TimeUtils.getChinaDate(record.getEndTimeMillis()).getTimeInMillis();
            if (!mappedRecords.containsKey(timeMs)) {
                mappedRecords.put(timeMs, new ArrayList<>());
            }
            mappedRecords.get(timeMs).add(record);
        }
        List<List<RealmTimeRecord>> allGroupedRecords = new ArrayList<>();
        List<Pair<Long, Integer>> allTimes = new ArrayList<>();
        int i = 0;
        for (Map.Entry<Long, List<RealmTimeRecord>> entry: mappedRecords.entrySet()) {
            if (entry.getValue() == null || entry.getValue().isEmpty())
                continue;
            allTimes.add(new Pair<>(entry.getKey(), i++));
            allGroupedRecords.add(entry.getValue());
            Logger.d("key: %s, size: %d", entry.getKey(), entry.getValue().size());
        }
        Collections.sort(allTimes, (p1, p2) -> {
            if (ascend) {
                if (p1.first > p2.first) {
                    return 1;
                } else if (p1.first < p2.first) {
                    return -1;
                }
            } else {
                if (p1.first > p2.first) {
                    return -1;
                } else if (p1.first < p2.first) {
                    return 1;
                }
            }
            return 0;
        });
        List<List<RealmTimeRecord>> newGroupedRecords = new ArrayList<>();
        for (i = 0; i < allTimes.size(); ++i) {
            int idx = allTimes.get(i).second;
            newGroupedRecords.add(allGroupedRecords.get(idx));
        }
        return newGroupedRecords;
    }

    public static long getTotalElapsedTime(List<RealmTimeRecord> records) {
        if (records == null) {
            return 0;
        }
        long total = 0;
        for (RealmTimeRecord record : records) {
            total += record.getElapsedTimeMillis();
        }
        return total;
    }


    public static RealmTimeRecord copyRealmTimeRecordFromPb(RealmTimeRecordPb.RealmTimeRecord record) {
        RealmTimeRecord newRecord = new RealmTimeRecord();
        newRecord.setId(record.getId());
        newRecord.setTitle(record.getTitle());
        newRecord.setSummary(record.getSummary());
        newRecord.setTimeStatus(record.getTimeStatus());
        newRecord.setCreateTimeMillis(record.getCreateTimeMillis());
        newRecord.setStartTimeMillis(record.getStartTimeMillis());
        newRecord.setEndTimeMillis(record.getEndTimeMillis());
        newRecord.setUpdateTimeMillis(record.getUpdateTimeMillis());
        newRecord.setActivityDateMillis(record.getActivityDateMillis());
        newRecord.setFireTimeMillis(record.getFireTimeMillis());
        newRecord.setPauseTimeMillis(record.getPauseTimeMillis());
        newRecord.setIncreasedTimeMillis(record.getIncreasedTimeMillis());
        return newRecord;
    }

    public static RealmTimeRecordPb.RealmTimeRecord copyRealmTimeRecordToPb(RealmTimeRecord record) {
        RealmTimeRecordPb.RealmTimeRecord.Builder builder = RealmTimeRecordPb.RealmTimeRecord.newBuilder();
        builder.setId(record.getId());
        builder.setTitle(record.getTitle());
        builder.setSummary(record.getSummary());
        builder.setTimeStatus(record.getTimeStatus());
        builder.setCreateTimeMillis(record.getCreateTimeMillis());
        builder.setStartTimeMillis(record.getStartTimeMillis());
        builder.setEndTimeMillis(record.getEndTimeMillis());
        builder.setUpdateTimeMillis(record.getUpdateTimeMillis());
        builder.setActivityDateMillis(record.getActivityDateMillis());
        builder.setFireTimeMillis(record.getFireTimeMillis());
        builder.setPauseTimeMillis(record.getPauseTimeMillis());
        builder.setIncreasedTimeMillis(record.getIncreasedTimeMillis());
        return builder.build();
    }
}
