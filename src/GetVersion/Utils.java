package GetVersion;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;

public class Utils {

    /**
     * 睡眠当前线程指定的{@code time} 毫秒数
     * 
     * @param time
     *            单位毫秒
     */
    public void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            System.out.println(e.toString());
        }
    }

    /**
     * 删除指定路径下的所有文件,然后删除自己
     * 
     * @param filePath
     *            文件的绝对路径
     * @return {@code true} 所有文件都正常删除{@code false} 有一个文件删除报错都是{@code false}
     */
    public boolean deleteFileOrDir(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return true;
        }
        if (file.isFile()) {
            return file.delete();
        }
        for (String fileName : file.list()) {
            if (!deleteFileOrDir(filePath + File.separator + fileName)) {
                return false;
            }
        }
        return file.delete();
    }

    public boolean fileExist(String filePath) {
        File file = new File(filePath);
        if (file.exists())
            return true;
        return false;
    }

    /**
     * 创建文件或目录
     * 
     * @param filePath
     *            路径
     * @param dir
     *            是否为目录, {@code true} 表示创建目录, {@code false} 表示创建文件
     * @param delete
     *            是否删除已存在的目录
     * @return
     */
    public boolean createFileOrDir(String filePath, boolean dir, boolean delete) {
        File file = new File(filePath);
        boolean flag = true;
        if (file.exists()) {
            if (delete || (dir != file.isDirectory())) {
                flag = deleteFileOrDir(filePath);
                if (!flag) {
                    return false;
                }
            } else {
                return true;
            }
        }
        if (dir) {
            flag = file.mkdirs();
        } else {
            if (!file.getParentFile().exists()) {
                flag = file.getParentFile().mkdirs();
                if (!flag) {
                    return false;
                }
            }
            try {
                flag = file.createNewFile();
            } catch (IOException e) {
                flag = false;
            }
        }
        return flag;
    }

    /**
     * 文件复制,支持复制目录以及目录下所有文件
     * 
     * @param fromPath
     *            来源文件夹或文件路径
     * @param toPath
     *            目标文件夹或文件路径. 注意:
     *            当目标文件或文件夹和来源文件和文件夹类型不一致时会被删除,如来源为文件,目标存在同名的文件夹,
     *            则文件夹会被删除,以便文件复制能够正常进行,反之亦然
     * @param delete
     *            {@code true} 删除目标位置的原有文件或文件夹{@code false} 表示覆盖目标位置原有的文件或文件夹
     * @return {@code true} 表示复制成功,{@code false} 表示任意一步失败
     */
    public boolean copyFileOrDir(String fromPath, String toPath, boolean delete) {
        File from = new File(fromPath);
        if (!from.exists()) {
            return false;
        }
        boolean dir = from.isDirectory();
        boolean flag = createFileOrDir(toPath, dir, delete);
        if (!flag) {
            return false;
        }
        if (!dir) {
            flag = deleteFileOrDir(toPath);
            if (!flag) {
                return false;
            }
            FileChannel in = null;
            FileChannel out = null;
            try {
                in = new FileInputStream(fromPath).getChannel();
                out = new FileOutputStream(toPath).getChannel();
                in.transferTo(0, in.size(), out);
                in.close();
                out.close();
                return true;
            } catch (IOException e) {
                return false;
            }
        }
        for (String fileName : from.list()) {
            flag = copyFileOrDir(fromPath + File.separator + fileName, toPath + File.separator
                    + fileName, false);
            if (!flag) {
                return false;
            }
        }
        return true;
    }

    /**
     * 读取文件的{@link Buffer}
     * 
     * @param filePath
     *            文件路径
     * @return
     */
    private byte[] readFile(String filePath) {
        FileInputStream fis = null;
        byte[] buffer = null;
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                System.out.println(filePath + "文件找不到");
                return buffer;
            }
            fis = new FileInputStream(filePath);
            buffer = new byte[fis.available()];
            fis.read(buffer);
            fis.close();
        } catch (IOException e) {
        }
        return buffer;
    }

    /**
     * 写入{@link Buffer}到文件中
     * 
     * @param buffer
     *            文件流
     * @param filePath
     *            文件路径
     * @param num
     *            重复写入流的次数,用于创建大文件
     * @param append
     *            是否追加,{@code true} 表示追加,[{@code false} 表示覆写
     */
    public void writeFile(byte[] buffer, String filePath, long num, boolean append) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(filePath, append);
            while (num-- > 0) {
                fos.write(buffer);
            }
            fos.close();
        } catch (IOException e) {
        }
    }

    /**
     * 读取文件中的内容并转化为String文本
     * 
     * @param filePath
     * @return 文件路径
     */
    public String readText(String filePath) {
        String text = "";
        return readFile(filePath) == null ? text : new String(readFile(filePath));
    }

    /**
     * 写入文本内容到文件中
     * 
     * @param text
     *            文本内容
     * @param filePath
     *            文件路径
     * @param append
     *            是否追加,{@code true} 表示追加,[{@code false} 表示覆写
     */
    public void writeText(String text, String filePath, boolean append) {
        byte[] buffer = null;
        try {
            buffer = text.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        writeFile(buffer, filePath, 1, append);
    }

    /**
     * 获取文件或文件夹列表中所有文件和相对路径的对应关系
     * 
     * @param files
     *            文件或文件夹列表
     * @param dirName
     *            初始相对路径
     * @return 返回文件和路径对应关系
     */
    private HashMap<String, File> getFileMap(File[] files, String dirName) {
        HashMap<String, File> tarFiles = new HashMap<String, File>();
        for (File file : files) {
            if (!file.exists()) {
                continue;
            }
            if (file.isFile()) {
                tarFiles.put(dirName + File.separator, file);
            } else {
                tarFiles.putAll(getFileMap(file.listFiles(),
                        dirName + File.separator + file.getName()));
            }
        }
        return tarFiles;
    }

    /**
     * 压缩文件到tar.gz格式, 支持多文件和文件夹
     * 
     * @param gzPath
     *            tar.gz文件路径和名称
     * @param files
     *            文件或文件夹列表
     */
    public void compress2Gz(String gzPath, File[] files) {
        if (files == null) {
            return;
        }
        GzipCompressorOutputStream gcos = null;
        FileInputStream fis = null;
        ArchiveOutputStream aos = null;
        try {
            gcos = new GzipCompressorOutputStream(new FileOutputStream(gzPath));
            aos = new ArchiveStreamFactory().createArchiveOutputStream("tar", gcos);
            HashMap<String, File> tarHashMap = getFileMap(files, ".");
            Set<String> keys = tarHashMap.keySet();
            for (String key : keys) {
                File file = tarHashMap.get(key);
                TarArchiveEntry entry = new TarArchiveEntry(file, key + file.getName());
                aos.putArchiveEntry(entry);
                fis = new FileInputStream(file);
                byte[] buffer = new byte[4 * 1024];
                int len = -1;
                while (-1 != (len = fis.read(buffer))) {
                    aos.write(buffer, 0, len);
                }
                aos.closeArchiveEntry();
                fis.close();
                aos.close();
            }
        } catch (IOException | ArchiveException e) {
            e.printStackTrace();
        } finally {
            try {
                aos.close();
                gcos.close();
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 解压tar.gz文件到指定的目录下
     * 
     * @param zipFilePath
     *            压缩文件路径
     * @param destDir
     *            目标文件夹路径
     */
    public void extractTarGz(String zipFilePath, String destDir) {
        createFileOrDir(destDir, true, true);
        try {
            GZIPInputStream is = new GZIPInputStream(new BufferedInputStream(new FileInputStream(
                    zipFilePath)));
            ArchiveInputStream in = new ArchiveStreamFactory().createArchiveInputStream("tar", is);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(in);
            TarArchiveEntry entry = (TarArchiveEntry) in.getNextEntry();
            while (entry != null) {
                String name = entry.getName();
                String[] names = name.split("/");
                String fileName = destDir;
                for (int i = 0; i < names.length; i++) {
                    String str = names[i];
                    fileName = fileName + File.separator + str;
                }
                if (name.endsWith("/")) {
                    new File(fileName).mkdir();
                } else {
                    File file = new File(fileName);
                    file.createNewFile();
                    BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(
                            new FileOutputStream(file));
                    int b;
                    while ((b = bufferedInputStream.read()) != -1) {
                        bufferedOutputStream.write(b);
                    }
                    bufferedOutputStream.flush();
                    bufferedOutputStream.close();
                }
                entry = (TarArchiveEntry) in.getNextEntry();
            }
            if (bufferedInputStream != null) {
                bufferedInputStream.close();
            }
        } catch (IOException | ArchiveException e) {
            e.printStackTrace();
        }
    }

    /**
     * 将输入流中的结果读取到{@link ArrayList} 中
     * 
     * @author chchen
     *
     */
    class ReadInputStream implements Runnable {
        private ArrayList<String> out;
        private InputStream inputStream;

        /**
         * 
         * @param out
         *            存放输入流的结果
         * @param inputStream
         *            输入流
         */
        public ReadInputStream(final ArrayList<String> out, InputStream inputStream) {
            this.out = out;
            this.inputStream = inputStream;
        }

        public void start() {
            Thread thread = new Thread(this);
            thread.setDaemon(true);
            thread.start();
        }

        public void run() {
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                String line;
                while ((line = reader.readLine()) != null) {
                    out.add(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    inputStream.close();
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 执行本地命令,并将执行结果写入到{@link ArrayList}中
     * 
     * @param out
     *            存放执行结果,为{@code null} 时不记录
     * @param command执行的命令
     * @param timeout
     *            等待时间,单位为秒
     * @return 是否正常执行完成
     */
    public boolean runtimeExec(ArrayList<String> out, String command, int timeout) {
        ArrayList<String> backupOut = new ArrayList<String>();
        Process process = null;
        boolean result = false;
        try {
            process = Runtime.getRuntime().exec(command);
            backupOut = out != null ? out : backupOut;
            new ReadInputStream(backupOut, process.getInputStream()).start();
            new ReadInputStream(backupOut, process.getErrorStream()).start();
            result = process.waitFor(timeout, TimeUnit.SECONDS);
            sleep(1000);// 增加多线程执行后的等待
        } catch (IOException | InterruptedException e) {
            return false;
        }
        return result;
    }

    /**
     * 打印文本列表中的内容到控制台
     * 
     * @param contents
     */
    public void printArrayString(ArrayList<String> contents) {
        for (String string : contents) {
            System.out.println(string);
        }
    }

    /**
     * 将{@link String}数组转化为一个{@link Arraylist}
     * 
     * @param strings
     * @return
     */
    public ArrayList<String> toArrayList(String[] strings) {
        ArrayList<String> arrays = new ArrayList<String>();
        for (int i = 0; i < strings.length; i++) {
            arrays.add(strings[i]);
        }
        return arrays;
    }

    /**
     * 连接URL得到的输入流
     * 
     * @param url
     * @return
     */
    public InputStream getInputStreamByUrl(String url) {
        InputStream is = null;
        try {
            URLConnection urlConnection = new URL(url).openConnection();
            urlConnection.connect();
            is = urlConnection.getInputStream();
        } catch (IOException e) {
        }
        return is;
    }

    /**
     * 移除数组中指定的元素
     * 
     * @param origin
     *            原始数组
     * @param index
     *            需要移除的元素索引
     * @return
     */
    public int[] removeElement(int[] origin, int index) {
        int[] result = new int[origin.length - 1];
        int flag = 0;
        for (int i = 0; i < result.length; i++) {
            if (i == index) {
                flag = 1;
            }
            result[i] = origin[i + flag];
        }
        return result;
    }

    /**
     * 计算字符串的MD5值
     * 
     * @param string
     * @return
     */
    public String MD5(String string) {
        MessageDigest digester = null;
        try {
            digester = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        byte[] bytes = string.getBytes();
        digester.update(bytes);
        StringBuffer des = new StringBuffer();
        String tmp = null;
        byte[] byteDigest = digester.digest();
        for (int i = 0; i < byteDigest.length; i++) {
            tmp = (Integer.toHexString(byteDigest[i] & 0xFF));
            if (tmp.length() == 1) {
                des.append("0");
            }
            des.append(tmp);
        }
        return des.toString();
    }

    /**
     * 获取用户选择
     * 
     * @param contents
     *            可选择的数组
     * @return
     */
    public String selectInput(ArrayList<String> contents, boolean all) {
        while (true) {
            int length = contents.size();
            if (length == 0) {
                System.out.println("当前没有可选项, 请确认后输入任意整数重试");
            } else {
                for (int i = 0; i < length; i++) {
                    String content = contents.get(i);
                    System.out.println((i + 1) + ":" + content);
                }
                if (all) {
                    System.out.println((length + 1) + ":all");
                }
            }
            int input = getIntOfSystemIn();
            if (input < 1 || input > length + 1) {
                continue;
            } else if (input == length + 1) {
                if (all) {
                    return "all";
                } else {
                    continue;
                }
            } else {
                return contents.get(input - 1);
            }
        }
    }

    /**
     * 从控制台获取一个{@code int}类型的值输入
     * 
     * @return 返回输入的{@code int}值
     */
    public int getIntOfSystemIn() {
        int input = 0;
        try {
            input = new Scanner(System.in).nextInt();
        } catch (Exception e) {
        }
        return input;
    }

    /**
     * 从控制台获取一个{@link String}类型的值输入
     * 
     * @return 返回输入的{@link String}值
     */
    public String getStringOfSystemIn() {
        String input = "";
        try {
            input = new Scanner(System.in).nextLine();
        } catch (Exception e) {
        }
        return input;
    }

    /**
     * 获取所有在线设备序列号列表
     * 
     * @return
     */
    public ArrayList<String> getAllDevices() {
        String cmd = "adb devices";
        ArrayList<String> result = new ArrayList<String>();
        runtimeExec(result, cmd, 10);
        ArrayList<String> devices = new ArrayList<String>();
        for (String string : result) {
            String[] device = string.trim().split("\\t");
            if (device.length == 2 && device[1].equals("device")) {
                devices.add(device[0].trim());
            }
        }
        return devices;
    }

    /**
     * 向指定URL发送GET方法的请求
     * 
     * @param url发送请求的URL
     * @param param
     *            请求附带的参数
     * @return 所代表远程资源的响应结果
     */
    public String sendGet(String url, String param) {
        String result = "";
        BufferedReader in = null;
        try {
            URL realUrl = new URL(url + "?" + param);
            // 打开和URL之间的连接
            URLConnection connection = realUrl.openConnection();
            // 建立实际的连接
            connection.connect();
            // 定义 BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("发送GET请求出现异常！" + e);
            e.printStackTrace();
        }
        // 使用finally块来关闭输入流
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 向指定 URL 发送POST方法的请求
     * 
     * @param url
     *            发送请求的 URL
     * @param param
     *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return 所代表远程资源的响应结果
     */
    public String sendPost(String url, String param) {
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            HttpURLConnection conn = (HttpURLConnection) realUrl.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");
            // 获取URLConnection对象对应的输出流
            out = new PrintWriter(conn.getOutputStream());
            // 发送请求参数
            out.print(param);
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("发送 POST 请求出现异常！" + e);
            e.printStackTrace();
        }
        // 使用finally块来关闭输出流、输入流
        finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return result;
    }
}
