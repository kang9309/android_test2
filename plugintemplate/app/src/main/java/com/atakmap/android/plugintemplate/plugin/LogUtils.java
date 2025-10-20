
package com.atakmap.android.plugintemplate.plugin;
import android.util.Log;

public class LogUtils {
    public static String getLogPosition() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        // 0: getStackTrace, 1: getLogPosition, 2: 호출한 곳
        StackTraceElement element = stackTrace[3];
        return "[(" + element.getFileName() + ":" + element.getLineNumber() + ") " + element.getMethodName() + "()]";
    }

    private static final int MAX_LOG_LENGTH = 4000;

    public static void logReceivedData(String received) {
        final String TAG = "logReceivedData";

        String message = received;

        // 길이에 따라 나눠서 출력
        for (int i = 0; i < message.length(); i += MAX_LOG_LENGTH) {
            int end = Math.min(message.length(), i + MAX_LOG_LENGTH);
            Log.d(TAG, message.substring(i, end));
        }
    }
}
