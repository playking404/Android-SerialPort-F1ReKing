package me.f1reking.android_serialport;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Util {
    public static String bytesToHex(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }
        // 用于存储十六进制字符串的字符数组（每个字节对应2个十六进制字符）
        char[] hexChars = new char[bytes.length * 2];
        // 十六进制字符表
        char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

        for (int i = 0; i < bytes.length; i++) {
            // 将字节转换为无符号整数（& 0xFF 确保高位不补符号位）
            int val = bytes[i] & 0xFF;
            // 高4位对应十六进制字符（右移4位）
            hexChars[i * 2] = hexDigits[val >>> 4];
            // 低4位对应十六进制字符（与0x0F取余）
            hexChars[i * 2 + 1] = hexDigits[val & 0x0F];
        }
        return new String(hexChars);
    }

    public static String formatCurrentTime(String format) {
        if (format == null || format.isEmpty()) {
            format = "yyyy-MM-dd HH:mm:ss";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date());
    }
}
