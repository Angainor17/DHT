package cx.ring.utils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;

public class HashUtils {

    private static final String TAG = HashUtils.class.getSimpleName();

    private HashUtils() {
    }

    public static String md5(String s) {
        return hash(s, "MD5");
    }

    public static String sha1(String s) {
        return hash(s, "SHA-1");
    }

    private static String hash(final String s, final String algo) {
        String result = null;
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(algo);
            messageDigest.update(s.getBytes(), 0, s.length());
            result = new BigInteger(1, messageDigest.digest()).toString(16);
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Not able to find MD5 algorithm", e);
        }
        return result;
    }

    @SafeVarargs
    public static <T> Set<T> asSet(T... items) {
        HashSet<T> s = new HashSet<>(items.length);
        for (T t : items)
            s.add(t);
        return s;
    }
}
