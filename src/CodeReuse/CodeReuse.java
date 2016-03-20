package CodeReuse;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import GetVersion.Common;
import GetVersion.ExecuteExcel;
import GetVersion.HandDiff;
import GetVersion.Utils;
import GetVersion.VulnerabilityInfo.VulnerInfo;

public class CodeReuse {
    Utils utils = new Utils();
    HandDiff handDiff = new HandDiff();
    Common common = new Common();
    
    public String getFunction(String filePath, String functionName) {
        // 读取文件源码
        String code = utils.readText(filePath);
        String function = "";
        if (functionName.equals(Common.functionNameIsNull)) {
            return code;
        }
        String re = "^[\\w\\s]+[\\s\\*]+" + functionName
        // + "\\s*\\([\\w\\[\\w\\]*\\s\\*\\,]*\\)\\s*\\{";
                + "\\s*\\([\\s\\S]*?\\)\\s*\\{";
        Pattern pattern = Pattern.compile(re, Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(code);

        while (matcher.find()) {
            if (!matcher.group().contains(";") && !matcher.group().contains("}")) {
                System.out.println(matcher.group());
                // 获取函数开始行号
                int start = matcher.start();
                int i = 0;
                int end = 0;
                int brackets = 0;
                for (i = matcher.start(); i < code.length(); i++) {
                    if (code.charAt(i) == '{') {
                        brackets++;
                    }
                    if (code.charAt(i) == '}') {
                        brackets--;
                        if (brackets == 0)
                            break;
                    }
                }
                end = i;
                function = code.substring(start, end + 1);
                break;
            }
        }
        return function;
    }

    public ArrayList<String> getHalfMatchFile(String diffFilePath, String codePath,
            String versionPrefix, VulnerInfo vulnerInfo, ArrayList<String> versions,
            String resultPath) {

        ArrayList<String> reuseVersionList = new ArrayList<String>();

        String[] functionList = null;
        if (vulnerInfo.functionName == null) {
            functionList = new String[] { "functionName is null" };
        } else {
            functionList = vulnerInfo.functionName.split("[;；]");
        }
        String[] fileList = vulnerInfo.fileName.split("[;；]");

        for (int i = 0; i < fileList.length; i++) {
            ArrayList<String> reuseCodeList = new ArrayList<String>();
            String fileName = fileList[i].replaceAll("[ |　]", " ").replaceAll("\\u00A0", "").trim();
            ArrayList<String> diffList = handDiff.handleDiff(diffFilePath, vulnerInfo.fileName,
                    functionList[i], true);
            String[] functionNameList = functionList[i].split("[,，]");
            System.out.println(Arrays.toString(functionNameList));

            // 获取diff文件
            for (String functionName : functionNameList) {
                System.out.println(functionName);
                int num = 0;
                for (String version : versions) {
                    // 如果超过 10个 ，退出该函数
                    if (num >= common.txtMaxNum)
                        break;
                    String functionPath = common.getCodefilePath(codePath, versionPrefix, version,
                            fileName);
                    String functionCode = getFunction(functionPath, functionName);
                    if (functionCode.length() < 1) {
                        System.out.println(functionPath + "中" + functionName + "不存在");
                        continue;
                    }
                    String path = common.getMarkFunctionPath(resultPath, vulnerInfo.softeware,
                            vulnerInfo.cve,
                            functionName.equals(Common.functionNameIsNull) ? fileName
                                    : functionName);
                    // 找到不完全匹配的函数
                    int flag = 0;
                    for (String diffStr : diffList) {
                        String markFunction = matchDiffLine(functionCode, diffStr);
                        // 存在不完全匹配的函数，将不完全匹配的函数，存到txt中
                        if (!markFunction.contains(common.markStr)) {
                            break;
                        }
                        if (markFunction.contains(common.markStr)
                                && !reuseCodeList.contains(markFunction)) {
                            num++;
                            new File(path).mkdirs();
                            String filePath = path + version + ".txt";
                            if (utils.fileExist(filePath))
                                flag++;
                            filePath = flag > 0 ? (path + version + "(" + flag + ").txt") : path
                                    + version + ".txt";
                            utils.writeText(markFunction, filePath, false);
                            reuseCodeList.add(markFunction);
                            reuseVersionList.add(version);
                            System.out.println(filePath);
                        }
                    }
                }
            }
        }
        return reuseVersionList;
    }

    public String matchDiffLine(String funciton, String diff) {
        String markFunction = "";
        String[] functionList = funciton.trim().split("\n");
        diff = common.handleLineBreak(diff);
        String[] diffList = diff.trim().split("\n");
        for (String string : diffList) {
            String temp = string.trim();
            if (temp.length() > 0) {
                for (int i = 0; i < functionList.length; i++) {
                    if (functionList[i].trim().equals(temp)) {
                        functionList[i] = common.markStr + functionList[i];
                    }
                }
            }
        }
        for (int i = 0; i < functionList.length; i++) {
            markFunction += (markFunction.length() > 0 ? "\r\n" + functionList[i] : functionList[i]);
        }
        return markFunction;
    }

    public String MostmatchDiffLine(String funciton, String diff, Boolean isSimilarValue,
            String SimilarValue) {
        // 测试
        String functionMatch = "";
        int len1, len2;
        String[] functionList = funciton.trim().split("\n");
        diff = common.handleLineBreak(diff);
        String[] diffList = diff.trim().split("\n");
        len1 = functionList.length;
        len2 = diffList.length;
        int maxLen = len1 > len2 ? len1 : len2;

        int[] max = new int[maxLen];// 保存最长子串长度的数组
        int[] maxIndex = new int[maxLen];// 保存最长子串长度最大索引的数组
        int[] c = new int[maxLen];

        int i, j;
        for (i = 0; i < len2; i++) {
            for (j = len1 - 1; j >= 0; j--) {
                if (diffList[i].equals(functionList[j].trim())) {
                    if ((i == 0) || (j == 0))
                        c[j] = 1;
                    else
                        c[j] = c[j - 1] + 1;// 此时C[j-1]还是上次循环中的值，因为还没被重新赋值
                } else {
                    c[j] = 0;
                }

                // 如果是大于那暂时只有一个是最长的,而且要把后面的清0;
                if (c[j] > max[0]) {
                    max[0] = c[j];
                    maxIndex[0] = j;

                    for (int k = 1; k < maxLen; k++) {
                        max[k] = 0;
                        maxIndex[k] = 0;
                    }
                }
                // 有多个是相同长度的子串
                else if (c[j] == max[0]) {
                    for (int k = 1; k < maxLen; k++) {
                        if (max[k] == 0) {
                            max[k] = c[j];
                            maxIndex[k] = j;
                            break; // 在后面加一个就要退出循环了
                        }
                    }
                }
            }
        }
        // 打印最长子字符串
        int len = 0;
        for (j = 0; j < maxLen; j++) {
            if (max[j] > 0) {
                len = 0;
                for (i = maxIndex[j] - max[j] + 1; i <= maxIndex[j]; i++)
                    len++;
            }
        }
        double similarValue = (double) len / (double) len2;
        System.out.println("最长匹配长度为：" + len + "\t相似值：" + similarValue);
        if (isSimilarValue) {
            return String.valueOf(similarValue);
        }
        if (similarValue - common.threshold < 0) {
            System.out.println("相似度小于阈值");
            return functionMatch;
        }
        if (similarValue >= 1.0) {
            System.out.println("相似度大于等于1");
            return functionMatch;
        }
        if (SimilarValue != null && (similarValue - Double.parseDouble(SimilarValue) < 0)) {
            System.out.println("相似度小于修补后的相似度");
            return functionMatch;
        }

        for (j = 0, len = 0; j < maxLen; j++) {
            if (max[j] > 0) {
                // System.out.println("第" + (j + 1) + "个公共子串:");
                int count = maxIndex[j] - maxIndex[j] + max[j];
                int temp1 = len2 - count;
                int begin = (maxIndex[j] - max[j] + 1 - temp1);
                int end = (begin >= 0 ? maxIndex[j] : maxIndex[j] - begin);
                end = (end > len1 - 1 ? len1 - 1 : end);
                begin = (begin < 0 ? 0 : begin);
                for (i = begin; i <= end; i++) {
                    functionMatch += (functionMatch.length() > 0 ? "\r\n" + functionList[i]
                            : functionList[i]);
                }
                break;
            }
        }
        return functionMatch;
    }

    public ArrayList<String> getMostMatch(String diffFilePath, String codePath,
            String versionPrefix, VulnerInfo vulnerInfo, ArrayList<String> versions,
            String resultPath) {

        ArrayList<String> reuseVersionList = new ArrayList<String>();

        String[] functionList = null;
        if (vulnerInfo.functionName == null) {
            functionList = new String[] { "functionName is null" };
        } else {
            functionList = vulnerInfo.functionName.split("[;；]");
        }
        String[] fileList = vulnerInfo.fileName.split("[;；]");

        for (int i = 0; i < fileList.length; i++) {
            ArrayList<String> reuseCodeList = new ArrayList<String>();
            String fileName = fileList[i].replaceAll("[ |　]", " ").replaceAll("\\u00A0", "").trim();
            ArrayList<String> diffListOld = handDiff.handleDiff(diffFilePath, vulnerInfo.fileName,
                    functionList[i], true);
            ArrayList<String> diffListFix = handDiff.handleDiff(diffFilePath, vulnerInfo.fileName,
                    functionList[i], false);

            String[] functionNameList = functionList[i].split("[,，]");
            System.out.println(Arrays.toString(functionNameList));

            // 获取diff文件
            for (String functionName : functionNameList) {
                System.out.println(functionName);
                int num = 0;
                for (String version : versions) {
                    // 如果超过 10个 ，退出该函数
                    if (num > common.txtMaxNum)
                        break;
                    String functionPath = common.getCodefilePath(codePath, versionPrefix, version,
                            fileName);
                    System.out.println(functionPath);
                    String functionCode = getFunction(functionPath, functionName);
                    if (functionCode.length() < 1) {
                        System.out.println(functionPath + "中" + functionName + "不存在");
                        continue;
                    }
                    String funcPath=resultPath + File.separator + "复用函数文件";
                    new File(funcPath).mkdirs();
                    String nLinePath=resultPath + File.separator + "复用N行文件";
                    new File(nLinePath).mkdirs();
                    String funcFileName=common.getMostMarkFunctionPath(vulnerInfo.softeware,
                            vulnerInfo.cve,
                            functionName.equals(Common.functionNameIsNull) ? fileName
                                    : functionName,version);
                    
                    // 找到不完全匹配的函数
                    for (int j = 0; j < diffListOld.size(); j++) {
                        int flag = 0;
                        String functionFix = MostmatchDiffLine(functionCode, diffListFix.get(j),
                                true, null);
                        String functionOld = MostmatchDiffLine(functionCode, diffListOld.get(j),
                                false, functionFix);
                        // 存在不完全匹配的函数，将不完全匹配的函数，存到txt中
                        if (functionOld.length() < 1) {
                            continue;
                        }
                        if (functionOld.length() > 0 && !reuseCodeList.contains(functionOld)) {
                            
                            String filePathN = nLinePath+File.separator + funcFileName+"_N.txt";
                            System.out.println(filePathN);
                            String filePath2 = funcPath+File.separator+funcFileName + ".txt";
                            if (utils.fileExist(filePathN))
                                flag++;
                            filePathN = flag > 0 ? (nLinePath+File.separator + funcFileName +"_N(" + flag + ").txt")
                                    : filePathN;
                            String nFile="diff的N行：\r\n"+diffListOld.get(j).replaceAll("\n", "\r\n")+"\r\n//**************\r\n"+functionOld;
                            utils.writeText(nFile, filePathN, false);
                            if (!utils.fileExist(filePath2))
                                utils.writeText(functionCode.replaceAll("\n", "\r\n"), filePath2, false);

                            reuseCodeList.add(functionOld);
                            num++;
                            System.out.println(filePathN);
                            if (!reuseVersionList.contains(version)) {
                                reuseVersionList.add(version);
                            }
                            if (!ExecuteExcel.reuseToExcel.contains(version)) {
                                ExecuteExcel.reuseToExcel.add(funcFileName);
                            }                           
                        }
                    }
                }
            }
        }
        return reuseVersionList;
    }

    public static void main(String[] args) {
        // CodeReuse codeReuse = new CodeReuse();
        // String code = codeReuse
        // .getFunction(
        // "C:\\Users\\wt\\Desktop\\tyy\\software\\wireshark\\wireshark-1.12.2\\epan\\dissectors\\packet-wccp.c",
        // "dissect_wccp2r1_address_table_info");
        // System.out.println(code);

        CodeReuse codeReuse = new CodeReuse();
        HandDiff handDiff = new HandDiff();
        System.out.println("begin");
        String path = "C:\\Users\\wt\\Desktop\\tyy\\实验室work-tyy\\getContainVersion\\Wireshark\\";
        String fileName = "epan/dissectors/packet-ssl-utils.c";
        String functionName = "gif_decode_frame";
        String diffPath = "C:\\Users\\wt\\Desktop\\tyy\\实验室work-tyy\\Ffmpeg-1.1diff\\CVE-2013-3673.txt";
        String function = codeReuse.getFunction(path + fileName, functionName);
        System.out.println(function); // 获取diff文件
        ArrayList<String> diffList = handDiff.handleDiff(diffPath, fileName, functionName, true);

        // String returnStr = codeReuse.matchDiffLine(function,
        // diffList.get(0));
        System.out.println("begin2");
        codeReuse.matchDiffLine(function, diffList.get(0));
        // System.out.println(returnStr);
        // String writePath = "C:\\Users\\yytang\\Desktop\\CVE-2013-3673.txt";
        // codeReuse.utils.writeText(returnStr, writePath, false);
        System.out.println("end");

    }
}
