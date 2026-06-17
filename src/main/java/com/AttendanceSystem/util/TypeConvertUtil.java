package com.AttendanceSystem.util;

import java.util.Arrays;
import java.util.List;

public class TypeConvertUtil {

    public static List<String> convertIdsToList(String[] ids){
        List<String> list  = Arrays.stream(ids).toList();
        return list;
    }
}
