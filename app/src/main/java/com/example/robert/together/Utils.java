package com.example.robert.together;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * Created by robert on 11/12/15.
 */
public class Utils {
    private static final String TAG = "Utils";

    public static boolean  copyFile(String srcPath, String dstPath) {
        if (srcPath == null || dstPath == null) {
            return false;
        }

        if (srcPath.equals(dstPath)) {
            return true;
        }

        Log.i(TAG, "copy file "  + srcPath + " to file: " + dstPath);

        boolean ok = false;
        try {
            File srcFile = new File(srcPath);
            File dstFile = new File(dstPath);
            if (dstFile.exists()) {
                dstFile.delete();
            }
            if (!dstFile.exists()) {
                dstFile.createNewFile();
            }
            FileChannel srcChan = new FileInputStream(srcFile).getChannel();
            FileChannel dstChan = new FileOutputStream(dstFile).getChannel();
            if (srcChan != null && dstChan != null) {
                dstChan.transferFrom(srcChan, 0, srcChan.size());
            }
            if (srcChan != null) {
                srcChan.close();
            }
            if (dstChan != null) {
                dstChan.close();
            }
            ok = true;
        } catch (FileNotFoundException e) {
            Log.e(TAG, "err copy file: " + e);
        } catch (IOException e) {
            Log.e(TAG, "err copy file: " + e);
        }
        return ok;

    }

    public static boolean  deleteFile(String path) {
        return false;
    }
}
