package com.royran.timebrief.utils;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;

public class FileUtils {
    private static final int BUFFER_SIZE = 16 * 1024;

    public static boolean saveToFile(File parent, String filename, byte[] data) {
        try {
            if (data == null) {
                return false;
            }
            if (parent == null) {
                return false;
            }
            if (!parent.exists()) {
                if (!parent.mkdirs()) {
                    return false;
                }
            }
            File file = new File(parent, filename);
            if (file.exists()) {
                file.delete();
            }
            if (!file.createNewFile()) {
                return false;
            }
            FileOutputStream fos = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            bos.write(data);
            bos.flush();
            bos.close();
            fos.close();
            return true;
        } catch (IOException e) {
            Log.e("TAG", e.getMessage());
            return false;
        }
    }

    public static boolean saveToFile(File parent, String filename, String contents) {
        try {
            if (parent == null) {
                return false;
            }
            if (!parent.exists()) {
                if (!parent.mkdirs()) {
                    return false;
                }
            }
            File file = new File(parent, filename);
            if (file.exists()) {
                file.delete();
            }
            if (!file.createNewFile()) {
                return false;
            }
            FileOutputStream outputStream = new FileOutputStream(file);
            OutputStreamWriter writer = new OutputStreamWriter(outputStream);
            writer.append(contents);
            writer.close();
            outputStream.flush();
            outputStream.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("TAG", e.getMessage());
        }
        return false;
    }

    public static boolean saveToFile(String dir, String filename, byte[] data) {
        if (dir == null || dir.isEmpty() || filename == null || filename.isEmpty() || data == null) {
            return false;
        }
        File parent = new File(dir);
        return saveToFile(parent, filename, data);
    }

    public static boolean saveToFile(String dir, String filename, String contents) {
        if (dir == null || dir.isEmpty() || filename == null || filename.isEmpty()) {
            return false;
        }
        File parent = new File(dir);
        return saveToFile(parent, filename, contents);
    }

    public static byte[] readBytes(String filePath) {
        File file = new File(filePath);
        return readBytes(file);
    }

    public static byte[] readBytes(File file) {
        if (file == null || !file.exists()) {
            Log.e("TAG", "file not exists");
            return null;
        }
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = new BufferedInputStream(new FileInputStream(file), BUFFER_SIZE);
            byte[] buffer = new byte[BUFFER_SIZE];
            int n = 0;
            while ((n = in.read(buffer)) > 0) {
                out.write(buffer, 0, n);
            }
            byte[] data = out.toByteArray();
            in.close();
            out.close();
            return data;
        } catch (Exception e) {
            Log.e("TAG", e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static String readTextFile(File file) {
        if (file == null || !file.exists()) {
            return null;
        }
        try {
            StringBuilder sb = new StringBuilder();
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            while(true) {
                String line = bufferedReader.readLine();
                if (line == null) {
                    break;
                }
                sb.append(line);
            }
            return sb.toString();
        } catch (FileNotFoundException e) {
            Log.e("TAG", e.getMessage());
        } catch (IOException e) {
            Log.e("TAG", e.getMessage());
        }
        return null;
    }

    public static boolean deleteFile(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            return false;
        }
        try {
            return file.delete();
        } catch (Exception e) {
            Log.e("TAG", e.getMessage());
        }
        return false;
    }
}
