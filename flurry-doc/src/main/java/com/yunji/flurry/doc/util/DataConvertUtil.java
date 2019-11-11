package com.yunji.flurry.doc.util;

import java.io.BufferedReader;
import java.io.Reader;
import java.sql.Clob;
import java.sql.SQLException;

/**
 * @author with struy.
 * Create by 2018/4/11 21:24
 * email :yq1724555319@gmail.com
 */

public class DataConvertUtil {

    /**
     * Clob类型转换成String类型
     *
     * @param clob
     * @return
     */
    public static String Clob2String(final Clob clob) {

        if (clob == null) {
            return null;
        }

        Reader is = null;
        try {
            is = clob.getCharacterStream();
        } catch (Exception e) {
            e.printStackTrace();
        }
        BufferedReader br = new BufferedReader(is);

        String str = null;
        try {
            // 读取第一行
            str = br.readLine();
        } catch (Exception e) {
            e.printStackTrace();
        }

        StringBuffer sb = new StringBuffer();
        // 如果没有到达流的末尾，则继续读取下一行
        while (str != null) {
            sb.append(str);
            try {
                str = br.readLine();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        String returnString = sb.toString();

        return returnString;
    }

    /**
     * String类型转换成Clob类型
     *
     * @param string
     * @return
     */
    public static Clob String2Clob(final String string) throws SQLException {

        if (null == string || string.trim().length() == 0) {
            return null;
        }
        return new javax.sql.rowset.serial.SerialClob(string.toCharArray());
    }

}
