package com.yunji.backup;

import net.jpountz.lz4.LZ4FrameInputStream;
import net.jpountz.lz4.LZ4FrameOutputStream;

import java.io.*;

/**
 * @author Denim.leihz 2019-11-05 8:05 PM
 */
public class LZ4Test {

    private static int BUFFER_SIZE = 8192;

    public static void main(String[] args) {
        long st = System.currentTimeMillis();
        compress("/Users/maple/logs/dubbo-invoke.2019-11-04.0.log");
//        decompress("/Users/maple/logs/dubbo-invoke.2019-11-04.0.log.lz4");
        System.out.println("压缩/解压缩 耗时: " + (System.currentTimeMillis() - st) + " ms.");
    }


    public static void compress(String src) {
        String dest = src + ".lz4";
        File file2lz4 = new File(src);

        if (!file2lz4.exists()) {
            throw new RuntimeException("文件不存在");
        }

        File lz4edFile = new File(dest);

        if (lz4edFile.exists()) {
//            file2lz4.delete();
            throw new RuntimeException("文件已经存在");
        }

        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file2lz4));
             LZ4FrameOutputStream outStream = new LZ4FrameOutputStream(new FileOutputStream(lz4edFile))
        ) {

            byte[] inbuf = new byte[BUFFER_SIZE];
            int n;
            while ((n = bis.read(inbuf)) != -1) {
                outStream.write(inbuf, 0, n);
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static void decompress(String src) {
        String dest = src.substring(0, src.lastIndexOf("."));

        File file2lz4 = new File(src);

        if (!file2lz4.exists()) {
            throw new RuntimeException("文件不存在");
        }

        File lz4edFile = new File(dest);

        if (lz4edFile.exists()) {
            throw new RuntimeException("文件已经存在");
        }

        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(dest));
             LZ4FrameInputStream in = new LZ4FrameInputStream(new FileInputStream(new File(src)))

        ) {
            byte[] readBuf = new byte[BUFFER_SIZE];
            int n;
            while ((n = in.read(readBuf)) != -1) {
                bos.write(readBuf, 0, n);
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

}
