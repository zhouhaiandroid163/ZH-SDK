package com.zjw.sdkdemo.utils;

import android.content.Context;
import android.text.TextUtils;

import com.zhapp.ble.utils.BleLogger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MySaveLog {
    //日志名
    public static String FileName = "";
    private static File logFile = null;
    private static Context mContext;
    //日志路径
    public static String filePath = "";
    //日志目录
    public static String fileDir = "";

    //清除日志时间天数
    private static int expiredDay = 10;

    public static void init(Context context) {
        mContext = context;
        //清缓存
        clearExpiredFile();
    }

    /**
     * 设置过期清除天数
     *
     * @param expiredDay
     */
    public static void setExpiredDay(int expiredDay) {
        MySaveLog.expiredDay = expiredDay;
    }

    private static void createFile() {
        try {
            if (TextUtils.isEmpty(fileDir)) {
                fileDir = mContext.getExternalFilesDir("log/ble").getAbsolutePath();
            }
            if (TextUtils.isEmpty(FileName)) {
                FileName = createFileName();
            }
            File dirFile = new File(fileDir);
            if (dirFile != null && (dirFile.exists() ? dirFile.isDirectory() : dirFile.mkdirs())) {
                logFile = new File(fileDir, FileName);
                filePath = logFile.getAbsolutePath();
                if (!logFile.exists()) {
                    logFile.createNewFile();
                }
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    public static void writeFile(String level, String tag, String msg) {
        createFile();
        String mTag = TextUtils.isEmpty(tag) ? "" : tag.toLowerCase();
        StringBuffer buffer = new StringBuffer();
        buffer.append(logTime());
        buffer.append(" ----> ");
        buffer.append(level);
        buffer.append(" ----> ");
        buffer.append(mTag);
        buffer.append(" ");
        for (int i = mTag.length(); i < 25; i++) {
            buffer.append("-");
        }
        buffer.append("> ");
        buffer.append(msg);
        buffer.append("\r\n");
        writeFileFromString(logFile, buffer.toString(), true);
    }

    public static boolean writeFileFromString(final File file,
                                              final String content,
                                              final boolean append) {
        if (file == null || content == null) return false;
        if (!file.isFile() || !file.exists()) {
            return false;
        }
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(file, append));
            bw.write(content);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (bw != null) {
                    bw.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static final SimpleDateFormat logTimeDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS", Locale.ENGLISH);

    public static String logTime() {
        return logTimeDateFormat.format(new Date());
    }

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

    private static String createFileName() {
        return "BLE_" + dateFormat.format(new Date()) + ".zh";
    }

    //region 清缓存

    /**
     * 清除过期文件
     * 保留expiredDay天前的日志
     */
    private static void clearExpiredFile() {
        try {
            File dir = mContext.getExternalFilesDir("log/ble");
            List<File> logFiles = listFilesInDirWithFilterInner(dir, new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    //取文件夹内所有zh txt 文件
                    if (pathname != null && (pathname.getAbsolutePath().endsWith("txt") || pathname.getAbsolutePath().endsWith("zh"))) {
                        return true;
                    }
                    return false;
                }
            }, false);

            for (int i = 0; i < logFiles.size(); i++) {
                String fileNameDate = getFileNameNoExtension(logFiles.get(i).getAbsolutePath()).replace("BLE_", "");
                long fileTime = dateFormat.parse(fileNameDate).getTime();
                boolean expired = Math.abs(System.currentTimeMillis() - fileTime) > expiredDay * 24 * 60 * 60 * 1000L;
                if (expired) {
                    boolean isDel = logFiles.get(i).isFile() && logFiles.get(i).delete();
                    BleLogger.d("SaveLog", logFiles.get(i) + " expired， delete:" + isDel);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static List<File> listFilesInDirWithFilterInner(final File dir, final FileFilter filter, final boolean isRecursive) {
        List<File> list = new ArrayList<>();
        if (!(dir != null && dir.exists() && dir.isDirectory())) return list;
        File[] files = dir.listFiles();
        if (files != null && files.length > 0) {
            for (File file : files) {
                if (filter.accept(file)) {
                    list.add(file);
                }
                if (isRecursive && file.isDirectory()) {
                    list.addAll(listFilesInDirWithFilterInner(file, filter, true));
                }
            }
        }
        return list;
    }

    private static String getFileNameNoExtension(final String filePath) {
        if (TextUtils.isEmpty(filePath)) return "";
        int lastPoi = filePath.lastIndexOf('.');
        int lastSep = filePath.lastIndexOf(File.separator);
        if (lastSep == -1) {
            return (lastPoi == -1 ? filePath : filePath.substring(0, lastPoi));
        }
        if (lastPoi == -1 || lastSep > lastPoi) {
            return filePath.substring(lastSep + 1);
        }
        return filePath.substring(lastSep + 1, lastPoi);
    }
    //endregion
}
