package com.AttendanceSystem.util;

import org.mindrot.jbcrypt.BCrypt;


//密码加密算法
public class BCryptUtil {

    //密码加密
    public static String getpw(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    //校验密码是否一致
    public static boolean checkpw(String password, String hash) {
        return BCrypt.checkpw(password, hash);
    }
}