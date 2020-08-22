package com.royran.timebrief.models;

import java.io.Serializable;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class RealmTimeRecord extends RealmObject implements Comparable<RealmTimeRecord>, Serializable {
    @PrimaryKey
    private long id;
    private String title;
    private String summary;
    private int timeStatus = TimeStatus.IDLE_STATUS;
    private long createTimeMillis;
    private long updateTimeMillis;
    private long startTimeMillis;
    private long fireTimeMillis;
    private long endTimeMillis;
    private long pauseTimeMillis;
    private long increasedTimeMillis;
    private long activityDateMillis;

    public int compareTo(RealmTimeRecord record) {
        if (record.timeStatus == this.timeStatus) {
            if (record.endTimeMillis > this.endTimeMillis) {
                return 1;
            } else if (record.endTimeMillis == this.endTimeMillis) {
                return 0;
            } else {
                return -1;
            }
        } else if (this.timeStatus < record.timeStatus) {
            return -1;
        } else {
            return 1;
        }
    }

    public boolean equals(RealmTimeRecord record) {
        if (this.id != record.getId()) {
            return false;
        }
        if (!this.title.equals(record.getTitle())) {
            return false;
        }
        if (!this.summary.equals(record.getSummary())) {
            return false;
        }
        if (this.timeStatus != record.getTimeStatus()) {
            return false;
        }
        if (this.createTimeMillis != record.getCreateTimeMillis()) {
            return false;
        }
        if (this.updateTimeMillis != record.getUpdateTimeMillis()) {
            return false;
        }
        if (this.startTimeMillis != record.getStartTimeMillis()) {
            return false;
        }
        if (this.fireTimeMillis != record.getFireTimeMillis()) {
            return false;
        }
        if (this.endTimeMillis != record.getEndTimeMillis()) {
            return false;
        }
        if (this.increasedTimeMillis != record.getIncreasedTimeMillis()) {
            return false;
        }
        if (this.activityDateMillis != record.getActivityDateMillis()) {
            return false;
        }
        return true;
    }

    public RealmTimeRecord() {
        createTimeMillis = System.currentTimeMillis();
        updateTimeMillis = createTimeMillis;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        if (title == null) {
            return "";
        }
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getTimeStatus() {
        return timeStatus;
    }

    public void setTimeStatus(int timeStatus) {
        this.timeStatus = timeStatus;
        switch (timeStatus) {
            case TimeStatus.FINISHED_STATUS:
                if (pauseTimeMillis > 0) {
                    setEndTimeMillis(pauseTimeMillis);
                } else {
                    setEndTimeMillis(System.currentTimeMillis());
                }
                updateIncreasedTimeMillis();
                break;
            case TimeStatus.RUNNING_STATUS:
                setPauseTimeMillis(0);
                setFireTimeMillis(System.currentTimeMillis());
                break;
            case TimeStatus.PAUSED_STATUS:
                setPauseTimeMillis(System.currentTimeMillis());
                updateIncreasedTimeMillis();
                break;
            default:
                setFireTimeMillis(0);
        }
    }

    public long getCreateTimeMillis() {
        return createTimeMillis;
    }

    public void setCreateTimeMillis(long createTimeMillis) {
        this.createTimeMillis = createTimeMillis;
    }

    public long getStartTimeMillis() {
        return startTimeMillis;
    }

    public void setStartTimeMillis(long startTimeMillis) {
        this.startTimeMillis = startTimeMillis;
    }

    public long getFireTimeMillis() {
        return fireTimeMillis;
    }

    public void setFireTimeMillis(long fireTimeMillis) {
        this.fireTimeMillis = fireTimeMillis;
        if (this.startTimeMillis == 0) {
            this.startTimeMillis = fireTimeMillis;
        }
    }

    public void setIncreasedTimeMillis(long increasedTimeMillis) {
        if (increasedTimeMillis < 0) {
            increasedTimeMillis = 0;
        }
        this.increasedTimeMillis = increasedTimeMillis;
    }

    public void setPauseTimeMillis(long pauseTimeMillis) {
        this.pauseTimeMillis = pauseTimeMillis;
    }

    public long getPauseTimeMillis() {
        return pauseTimeMillis;
    }
    public long getIncreasedTimeMillis() {
        return this.increasedTimeMillis;
    }

    public long getUpdateTimeMillis() {
        return updateTimeMillis;
    }

    public void setUpdateTimeMillis(long updateTimeMillis) {
        this.updateTimeMillis = updateTimeMillis;
    }

    public long getEndTimeMillis() {
        return endTimeMillis;
    }

    public void setEndTimeMillis(long endTimeMillis) {
        this.endTimeMillis = endTimeMillis;
        setActivityDateMillis(endTimeMillis);
    }

    public long getElapsedTimeMillis() {
        if (fireTimeMillis == 0) {
            return increasedTimeMillis;
        }
        return System.currentTimeMillis() - fireTimeMillis + increasedTimeMillis;
    }

    public void updateIncreasedTimeMillis() {
        if (fireTimeMillis == 0) {
            return;
        }
        increasedTimeMillis += System.currentTimeMillis() - fireTimeMillis;
        fireTimeMillis = 0;
    }

    public String getSummary() {
        if (summary == null) {
            return "";
        }
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public long getActivityDateMillis() {
        return activityDateMillis;
    }

    public void setActivityDateMillis(long activityDateMillis) {
        this.activityDateMillis = activityDateMillis;
    }

    public void copyTo(RealmTimeRecord record) {
        if (record == null) {
            return;
        }
        record.setId(this.id);
        record.setTitle(this.title);
        record.setSummary(this.summary);
        record.setTimeStatus(this.timeStatus);
        record.setCreateTimeMillis(this.createTimeMillis);
        record.setUpdateTimeMillis(this.updateTimeMillis);
        record.setStartTimeMillis(this.startTimeMillis);
        record.setFireTimeMillis(this.fireTimeMillis);
        record.setEndTimeMillis(this.endTimeMillis);
        record.setIncreasedTimeMillis(this.increasedTimeMillis);
        record.setActivityDateMillis(this.activityDateMillis);
    }
}
