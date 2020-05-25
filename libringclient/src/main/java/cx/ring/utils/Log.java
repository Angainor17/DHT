package cx.ring.utils;

import cx.ring.services.LogService;

public class Log {


    private static LogService mLogService;

    public static void injectLogService(LogService service) {
        mLogService = service;
    }

    public static void d(String tag, String message) {
        mLogService.d(tag, message);
    }

    public static void e(String tag, String message) {
        mLogService.e(tag, message);
    }

    public static void i(String tag, String message) {
        mLogService.i(tag, message);
    }

    public static void w(String tag, String message) {
        mLogService.w(tag, message);
    }

    public static void d(String tag, String message, Throwable e) {
        mLogService.d(tag, message, e);
    }

    public static void e(String tag, String message, Throwable e) {
        mLogService.e(tag, message, e);
    }

    public static void i(String tag, String message, Throwable e) {
        mLogService.i(tag, message, e);
    }

    public static void w(String tag, String message, Throwable e) {
        mLogService.w(tag, message, e);
    }

}
