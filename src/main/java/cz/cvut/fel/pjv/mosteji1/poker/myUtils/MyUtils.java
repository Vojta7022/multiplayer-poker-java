package cz.cvut.fel.pjv.mosteji1.poker.myUtils;

import java.util.List;
import java.util.stream.Stream;

public abstract class MyUtils {

    public static List rotateList(List list, int shift) {
        return Stream.concat(list.subList(shift, list.size()).stream(), list.subList(0, shift).stream()).toList();
    }
}
