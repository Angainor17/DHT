package cx.ring.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cx.ring.daemon.Message;
import cx.ring.daemon.MessageVect;
import cx.ring.daemon.StringMap;
import cx.ring.daemon.StringVect;
import cx.ring.daemon.VectMap;

public class SwigNativeConverter {

    public static VectMap toSwig(List creds) {
        ArrayList<HashMap<String, String>> todecode = (ArrayList<HashMap<String, String>>) creds;
        VectMap toReturn = new VectMap();

        for (HashMap<String, String> aTodecode : todecode) {
            toReturn.add(StringMap.toSwig(aTodecode));
        }
        return toReturn;
    }

    public static ArrayList<String> toJava(StringVect vector) {
        ArrayList<String> toReturn = new ArrayList<>();
        toReturn.addAll(vector);
        return toReturn;
    }

    public static ArrayList<Message> toJava(MessageVect vector) {
        int size = (int) vector.size();
        ArrayList<Message> toReturn = new ArrayList<>(size);
        for (int i = 0; i < size; i++)
            toReturn.add(vector.get(i));
        return toReturn;
    }

}
