package com.atakmap.android.plugintemplate.plugin;

public class LogUtils {
    public static String getLogPosition() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        // 0: getStackTrace, 1: getLogPosition, 2: 호출한 곳
        StackTraceElement element = stackTrace[3];
        return "[(" + element.getFileName() + ":" + element.getLineNumber() + ") " + element.getMethodName() + "()]";
    }
}
