package cx.ring.model;

public class Phone {

    private NumberType mNumberType;

    private Uri mNumber;
    private int mCategory; // Home, work, custom etc.
    private String mLabel;


    public Phone(Uri number, int category) {
        mNumberType = NumberType.UNKNOWN;
        mNumber = number;
        mLabel = null;
        mCategory = category;
    }

    public Phone(String number, int category) {
        this(number, category, null);
    }

    public Phone(String number, int category, String label) {
        mNumberType = NumberType.UNKNOWN;
        mCategory = category;
        mNumber = new Uri(number);
        mLabel = label;
    }

    public Phone(Uri number, int category, String label) {
        mNumberType = NumberType.UNKNOWN;
        mCategory = category;
        mNumber = number;
        mLabel = label;
    }

    public Phone(String number, int category, String label, NumberType numberType) {
        mNumberType = numberType;
        mNumber = new Uri(number);
        mLabel = label;
        mCategory = category;
    }

    public Phone(Uri number, int category, String label, NumberType numberType) {
        mNumberType = numberType;
        mNumber = number;
        mLabel = label;
        mCategory = category;
    }

    public NumberType getType() {
        return getNumbertype();
    }

    public void setType(int type) {
        setNumberType(NumberType.fromInteger(type));
    }

    public Uri getNumber() {
        return mNumber;
    }

    public void setNumber(String number) {
        setNumber(new Uri(number));
    }

    public NumberType getNumbertype() {
        return mNumberType;
    }

    public void setNumber(Uri number) {
        this.mNumber = number;
    }

    public int getCategory() {
        return mCategory;
    }

    public String getLabel() {
        return mLabel;
    }

    public void setNumberType(NumberType numberType) {
        this.mNumberType = numberType;
    }

    public enum NumberType {
        UNKNOWN(0),
        TEL(1),
        SIP(2),
        IP(2),
        RING(3);

        private final int type;

        NumberType(int t) {
            type = t;
        }

        private static final NumberType[] VALS = NumberType.values();

        public static NumberType fromInteger(int id) {
            for (NumberType type : VALS) {
                if (type.type == id) {
                    return type;
                }
            }
            return UNKNOWN;
        }
    }
}
