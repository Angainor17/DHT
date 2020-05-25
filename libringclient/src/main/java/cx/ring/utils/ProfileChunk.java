package cx.ring.utils;

import cx.ring.daemon.StringVect;

public class ProfileChunk {
    public final static String TAG = ProfileChunk.class.getSimpleName();

    private long mNumberOfParts;
    private long mInsertedParts;
    private StringVect mParts;

    /**
     * Constructor
     *
     * @param numberOfParts Number of part to complete the Profile
     */
    public ProfileChunk(long numberOfParts) {
        Log.d(TAG, "Create ProfileChink of size " + numberOfParts);
        this.mInsertedParts = 0;
        this.mNumberOfParts = numberOfParts;
        this.mParts = new StringVect();
        this.mParts.reserve(mNumberOfParts);
        for (int i = 0; i < mNumberOfParts; i++) {
            this.mParts.add("");
        }
    }

    /**
     * Inserts a profile part in the data structure, at a given position
     *
     * @param part  the part to insert
     * @param index the given position to insert the part
     */
    public void addPartAtIndex(String part, int index) {
        mParts.set(index - 1, part);
        mInsertedParts++;
        Log.d(TAG, "Inserting part " + part + " at index " + index);
    }

    /**
     * Tells if the profile is complete: all the needed parts have been gathered
     *
     * @return true if complete, false otherwise
     */
    public boolean isProfileComplete() {
        return this.mInsertedParts == this.mNumberOfParts;
    }

    /**
     * Builds the profile based on the gathered parts.
     *
     * @return the complete profile as a String
     */
    public String getCompleteProfile() {
        if (this.isProfileComplete()) {
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < this.mParts.size(); ++i) {
                stringBuilder.append(this.mParts.get(i));
            }
            return stringBuilder.toString();
        } else {
            return null;
        }
    }
}