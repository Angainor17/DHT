package cx.ring.utils;

import android.text.InputFilter;
import android.text.SpannableString;
import android.text.Spanned;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisteredNameFilter implements InputFilter {
    private static final Pattern REGISTERED_NAME_CHAR_PATTERN = Pattern.compile("[a-z0-9_\\-]", Pattern.CASE_INSENSITIVE);
    private final Matcher nameCharMatcher = REGISTERED_NAME_CHAR_PATTERN.matcher("");

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        boolean keepOriginal = true;
        StringBuilder sb = new StringBuilder(end - start);
        for (int i = start; i < end; i++) {
            char c = source.charAt(i);
            if (isCharAllowed(c)) {
                sb.append(c);
            } else {
                keepOriginal = false;
            }
        }
        if (keepOriginal) {
            return null;
        } else {
            if (source instanceof Spanned) {
                return new SpannableString(sb);
            } else {
                return sb;
            }
        }
    }

    private boolean isCharAllowed(char c) {
        return nameCharMatcher.reset(String.valueOf(c)).matches();
    }
}
