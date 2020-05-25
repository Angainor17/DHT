package cx.ring.services;

public interface LogService {

    void e(String tag, String message);

    void d(String tag, String message);

    void w(String tag, String message);

    void i(String tag, String message);

    void e(String tag, String message, Throwable e);

    void d(String tag, String message, Throwable e);

    void w(String tag, String message, Throwable e);

    void i(String tag, String message, Throwable e);
}
