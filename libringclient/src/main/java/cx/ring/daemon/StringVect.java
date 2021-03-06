/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 4.0.1
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package cx.ring.daemon;

public class StringVect extends java.util.AbstractList<String> implements java.util.RandomAccess {
    private transient long swigCPtr;
    protected transient boolean swigCMemOwn;

    protected StringVect(long cPtr, boolean cMemoryOwn) {
        swigCMemOwn = cMemoryOwn;
        swigCPtr = cPtr;
    }

    protected static long getCPtr(StringVect obj) {
        return (obj == null) ? 0 : obj.swigCPtr;
    }

    @SuppressWarnings("deprecation")
    protected void finalize() {
        delete();
    }

    public synchronized void delete() {
        if (swigCPtr != 0) {
            if (swigCMemOwn) {
                swigCMemOwn = false;
                RingserviceJNI.delete_StringVect(swigCPtr);
            }
            swigCPtr = 0;
        }
    }

    public StringVect(String[] initialElements) {
        this();
        reserve(initialElements.length);

        for (String element : initialElements) {
            add(element);
        }
    }

    public StringVect(Iterable<String> initialElements) {
        this();
        for (String element : initialElements) {
            add(element);
        }
    }

    public String get(int index) {
        return doGet(index);
    }

    public String set(int index, String e) {
        return doSet(index, e);
    }

    public boolean add(String e) {
        modCount++;
        doAdd(e);
        return true;
    }

    public void add(int index, String e) {
        modCount++;
        doAdd(index, e);
    }

    public String remove(int index) {
        modCount++;
        return doRemove(index);
    }

    protected void removeRange(int fromIndex, int toIndex) {
        modCount++;
        doRemoveRange(fromIndex, toIndex);
    }

    public int size() {
        return doSize();
    }

    public StringVect() {
        this(RingserviceJNI.new_StringVect__SWIG_0(), true);
    }

    public StringVect(StringVect other) {
        this(RingserviceJNI.new_StringVect__SWIG_1(StringVect.getCPtr(other), other), true);
    }

    public long capacity() {
        return RingserviceJNI.StringVect_capacity(swigCPtr, this);
    }

    public void reserve(long n) {
        RingserviceJNI.StringVect_reserve(swigCPtr, this, n);
    }

    public boolean isEmpty() {
        return RingserviceJNI.StringVect_isEmpty(swigCPtr, this);
    }

    public void clear() {
        RingserviceJNI.StringVect_clear(swigCPtr, this);
    }

    public StringVect(int count, String value) {
        this(RingserviceJNI.new_StringVect__SWIG_2(count, value), true);
    }

    private int doSize() {
        return RingserviceJNI.StringVect_doSize(swigCPtr, this);
    }

    private void doAdd(String x) {
        RingserviceJNI.StringVect_doAdd__SWIG_0(swigCPtr, this, x);
    }

    private void doAdd(int index, String x) {
        RingserviceJNI.StringVect_doAdd__SWIG_1(swigCPtr, this, index, x);
    }

    private String doRemove(int index) {
        return RingserviceJNI.StringVect_doRemove(swigCPtr, this, index);
    }

    private String doGet(int index) {
        return RingserviceJNI.StringVect_doGet(swigCPtr, this, index);
    }

    private String doSet(int index, String val) {
        return RingserviceJNI.StringVect_doSet(swigCPtr, this, index, val);
    }

    private void doRemoveRange(int fromIndex, int toIndex) {
        RingserviceJNI.StringVect_doRemoveRange(swigCPtr, this, fromIndex, toIndex);
    }

}
