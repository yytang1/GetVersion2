package GetVersion;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HandleVersion {

    static CheckDiff checkDiff = new CheckDiff();

    /**
     * 解析software version格式，获取满足条件的软件版本列，支持“ before 2.1.6, 2.2.x through
     * 2.3.x，and 2.4.x before 2.4.15” 支持 and 1.1.3"
     * 
     * @param versionList
     *            所有版本列
     * @param excelVersionStr
     *            版本区间，多个版本区间，以逗号区分
     * @return 满足版本区间版本列
     * @throws Exception
     */
    //

    public ArrayList<String> getCodeVersion(ArrayList<String> versionList, String excelVersionStr)
            throws Exception {

        ArrayList<String> codeVersions = new ArrayList<String>();
        // 无意义字符处理 String test = "2.0.0";
        // 多个版本区间，逗号划分
        String[] excelVersions = null;
        if (!excelVersionStr.contains(",") && !excelVersionStr.contains("，")
                && excelVersionStr.contains("and") && !excelVersionStr.contains("earlier")) {
            excelVersions = excelVersionStr.split("and");

        } else {
            excelVersions = excelVersionStr.split(",|，");

        }
        for (int i = 0; i < excelVersions.length; i++) {
            String excelVersion = excelVersions[i];
            if (excelVersion.length() < 1) {
                continue;
            }
            excelVersion = filterStr(excelVersion);
            // 类型识别
            int type = getVersionType(excelVersion);
            if (type == 0) {
                return null;
            }

            String[] versionStr = excelVersion.split(" ");
            switch (type) {
            case 1:
                // before 2.1.6情况
                for (String string : versionList) {
                    if (compareVersion(string, versionStr[1]) < 0)
                        codeVersions.add(string);
                }
                break;
            case 2:
                // 2.4.x before 2.4.4 情况
                // 2.4 before 2.4.34
                for (String string : versionList) {
                    if (compareVersion(string, versionStr[0]) >= 0
                            && compareVersion(string, versionStr[2]) < 0)
                        codeVersions.add(string);
                }
                break;
            case 3:
                // through 2.3.6 情况
                for (String string : versionList) {
                    if (compareVersion(string, versionStr[1]) <= 0)
                        codeVersions.add(string);
                }
                break;
            case 4:
                // 2.2.x through 2.3.6 情况
                // 2.6.9 through 2.6.20
                for (String string : versionList) {
                    if (compareVersion(string, versionStr[0]) >= 0
                            && compareVersion(string, versionStr[2]) <= 0)
                        codeVersions.add(string);
                }
                break;
            case 5:
                // 2.1.4 earlier情况
                for (String string : versionList) {
                    if (compareVersion(string, versionStr[0]) <= 0)
                        codeVersions.add(string);
                }
                break;
            case 6:
                String[] versionStrList=versionStr[0].split(".");                
                for (String string : versionList) {
                    if (compareVersion(string, versionStr[0]) == 0)
                        codeVersions.add(string);
                    if (versionStrList.length==4&&compareVersion(string, versionStrList[0]+"."+versionStrList[1]+"."+versionStrList[2]) == 0) {
                        codeVersions.add(string);
                    }
                }
                break;

            default:
                return null;
            }
        }
        return codeVersions;
    }

    /**
     * 对应关系： 1：before 2.1.6； 2：2.4.x before 2.4.4； 3：through 1.1.3； 4：2.2.x
     * through 2.3.x； 5：2.1.4 earlier；7：2.4 before 2.4.34
     * 
     * @param versionStr
     * @return 编码
     */
    int getVersionType(String versionStr) {
        int type = 0;

        Pattern pa_beA = Pattern.compile(Common.re_beforeA, Pattern.MULTILINE);
        Matcher ma_beA = pa_beA.matcher(versionStr);
        if (ma_beA.find()) {
            type = 1;
            return type;
        }

        Pattern pa_AbeB = Pattern.compile(Common.re_AbeforeB, Pattern.MULTILINE);
        Matcher ma_AbeB = pa_AbeB.matcher(versionStr);
        if (ma_AbeB.find()) {
            type = 2;
            return type;
        }

        Pattern pa_thA = Pattern.compile(Common.re_throughA, Pattern.MULTILINE);
        Matcher ma_thA = pa_thA.matcher(versionStr);
        if (ma_thA.find()) {
            type = 3;
            return type;
        }

        Pattern pa_AthB = Pattern.compile(Common.re_AthroughB, Pattern.MULTILINE);
        Matcher ma_AthB = pa_AthB.matcher(versionStr);
        if (ma_AthB.find()) {
            type = 4;
            return type;
        }
        Pattern pa_earlier = Pattern.compile(Common.re_earlier, Pattern.MULTILINE);
        Matcher ma_earlier = pa_earlier.matcher(versionStr);
        if (ma_earlier.find()) {
            type = 5;
            return type;
        }
        Pattern pa_version = Pattern.compile(Common.re_begin + Common.re_version_x + Common.re_end,
                Pattern.MULTILINE);
        Matcher ma_version = pa_version.matcher(versionStr);
        if (ma_version.find()) {
            type = 6;
            return type;
        }
        return type;
    }

    /**
     * 过滤无意义词，比如and；或者其他语义处理（待扩充）
     * 
     * @param versionStr
     *            需要过滤的原始版本区间
     * @return
     */
    String filterStr(String versionStr) {
        String fiterVersion = versionStr;
        String andStr = "and ";
        if (versionStr.contains(andStr)) {
            fiterVersion = versionStr.replaceAll(andStr, "");
        }
        return fiterVersion.trim();
    }

    /**
     * 比较版本号的大小,前者大则返回一个正数,后者大返回一个负数,相等则返回0
     * 
     * @param version1
     *            2.0.1或者2.1.x
     * @param version2
     * @return 返回0，表示相等，比如2.1.x
     */
    public int compareVersion(String version1, String version2) throws Exception {
        if (version1 == null || version2 == null) {
            throw new Exception("compareVersion error:illegal params.");
        }
        String[] versionArray1 = version1.split("\\.");// 注意此处为正则匹配，不能用"."；
        String[] versionArray2 = version2.split("\\.");
        int idx = 0;
        int minLength = Math.min(versionArray1.length, versionArray2.length);// 取最小长度值
        int len = versionArray1.length - versionArray2.length;
        int diff = 0;
        // 比较前两个数字
        while (idx < minLength) {
            if ((versionArray1[idx].equals("x") || versionArray2[idx].equals("x"))) {
                diff = 0;
                len = 0;
                break;
            }
            if ((diff = versionArray1[idx].length() - versionArray2[idx].length()) != 0) {
                break;
            }
            if ((diff = versionArray1[idx].compareTo(versionArray2[idx])) != 0) {
                break;
            }
            ++idx;
        }
        // 如果已经分出大小，则直接返回，如果未分出大小，则再比较位数，有子版本的为大；
        diff = (diff != 0) ? diff : len;
        return diff;
    }

    public static void main(String[] args) throws Exception {
        DealSoftware dealSoftware = new DealSoftware();
        // String test1 = "before 2.1.6";
        String temp = "before 2.6.38.4";
        // String test4 = "2.2.x through 2.3.x";
        // String test2 = "2.x before 2.4.15";
        // String test3 = "and through 2.1.6";
        // String test5 = "2.1.6 and earlier";
        // String all = "before 0.5.6 and 2.2.x through 2.3.x";

        String Codepath = "C:\\Users\\wt\\Desktop\\tyy\\software\\";
        String versionPrefix = "linux-";
        String software = "linux";
        HandleVersion handleVersion = new HandleVersion();
        ArrayList<String> fileList = dealSoftware.getFileName(Codepath, software);

        fileList = checkDiff.getFileVersions(fileList, versionPrefix);
        System.out.println(fileList.size() + "\nbegin");

        ArrayList<String> versions = handleVersion.getCodeVersion(fileList, temp);
        for (String string : versions) {
            System.out.println(string);
        }
        System.out.println(versions.size() + "end");
    }
}
