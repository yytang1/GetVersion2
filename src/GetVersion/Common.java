package GetVersion;

import java.io.File;

import CodeReuse.FindFile;

public class Common {
    public String markStr = "//***\r\n";
    public int txtMaxNum = 1000;
    // 正则匹配
    public static String re_begin = "^";
    public static String re_end = "$";
    public static String re_version = "(([0-9]+\\.)*[0-9]+\\.)*[0-9]+";
    public static String re_versionList = "((([0-9]+\\.)*[0-9]+\\.)*[0-9]+\\,)*(" + re_version
            + ")";
    public static String re_version_x = "(([0-9]+\\.)*[0-9]+\\.)*([0-9]+|x)";
    public static String re_throughA = re_begin + "through\\s" + re_version + re_end;

    public static String re_AthroughB = re_begin + re_version_x + "(\\s)*through(\\s)*"
            + re_version_x + re_end;
    public static String re_beforeA = re_begin + "before\\s" + re_version + re_end;
    public static String re_AbeforeB = re_begin + re_version_x + "\\sbefore\\s" + re_version
            + re_end;
    public static String re_earlier = re_begin + re_version + "\\searlier" + re_end;

    public static String functionNameIsNull = "functionName is null";
    public static String reuseCode = "reuseCode";
    public static String reuseCodeMostMatch = "reuseCodeMostMatch";
    public static String reVersionList = re_begin + re_version + re_end;

    public double threshold = 0.6;

    public String getCodefilePath(String codePath, String versionPrefix, String version,
            String fileName) {
        String path1 = codePath + File.separator + versionPrefix + version + File.separator
                + fileName;
        String path2 = codePath + File.separator + versionPrefix + version;
        Utils utils = new Utils();
        if (utils.fileExist(path1)) {
            return path1;
        } else {
            FindFile findFile = new FindFile();
            File folder = new File(path2);
            File[] result = findFile.searchFile(folder, fileName);
            if (result.length == 1) {
                return result[0].getAbsolutePath();
            }
        }
        return "";
    }

    public String getDifftxtPath(String diffPath, String cve) {
        return diffPath + File.separator + cve + ".txt";
    }

    /**
     * 获取复用实例代码的存放路径
     * 
     * @param path
     *            存放复用实例代码路径
     * @param cve
     *            漏洞cve号
     * @param functionName
     *            漏洞函数名
     * @return 返回路径 如：
     */
    public String getMarkFunctionPath(String path, String software, String cve, String functionName) {
        return path + File.separator + reuseCode + File.separator + cve + File.separator + software
                + File.separator + functionName + File.separator;
    }

    // CVE-2015-3811_wireshark1.12.3_decompressed_entry_N
    public String getMostMarkFunctionPath(String software, String cve, String functionName,
            String version) {
        return cve + "_" + software + version + "_" + functionName;
    }

    /**
     * 将window下换行符“\r\n”替换为“\n”
     * 
     * @param string
     *            需要处理的字符串
     * @return
     */
    public String handleLineBreak(String string) {
        if (string.contains("\r\n")) {
            string = string.replace("\r\n", "\n");
        }
        return string;
    }
}
