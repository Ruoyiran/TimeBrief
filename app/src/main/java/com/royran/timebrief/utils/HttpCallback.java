package com.royran.timebrief.utils;

public interface HttpCallback {
    void onSuccess(byte[] body);
    void onFailure(Exception error);
}
