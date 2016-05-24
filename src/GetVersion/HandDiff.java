package GetVersion;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HandDiff {
	Utils utils = new Utils();
	Common common = new Common();

	boolean matchCode(String diffPath, String codePath, String fileName) {
		ArrayList<String> diffStrList = handleDiff(diffPath, fileName, null,
				false);
		String codeStr = utils.readText(codePath);
		// TODO check是否包含该漏洞
		for (String string : diffStrList) {
			if (!codeStr.contains(string))
				return false;
		}
		return true;
	}

	/**
	 * 
	 * @param filePath
	 *            diff文件路径
	 * @param fileName
	 *            文件名
	 * @param isOld
	 *            是否获取修改前代码，{@code false}则获取修改后代码
	 * @return 文件名对应下的函数列
	 */
	// diff文件多函数处理，提取代码列
	public ArrayList<String> handleDiff(String filePath, String fileName,
			String functionName, boolean isOld) {
		// 临时测试使用，用后即删除。
		if (fileName.contains("tools\\qemu-xen\\")) {
			fileName = fileName.replace("tools\\qemu-xen\\", "");
		}
		if (fileName.contains("tools\\qemu-xen-traditional\\")) {
			fileName = fileName.replace("tools\\qemu-xen-traditional\\", "");
		}
		if (fileName.contains("mozilla/")) {
			fileName = fileName.replace("mozilla/", "");
		}
		if (fileName.contains("mozilla\\")) {
			fileName = fileName.replace("mozilla\\", "");
		}
		ArrayList<String> strFilterList = new ArrayList<String>();
		// 文件名处理：
		fileName = fileName.replace("\\", "/");
		System.out.println(fileName);
		// 如果isOld，则去掉"+"行
		String plus = (isOld ? "+" : "-");
		String strOriginal = utils.readText(filePath);
		strOriginal = common.handleLineBreak(strOriginal);
		String[] strs = strOriginal.split("\n");
		for (int i = 0; i < strs.length; i++) {
			// 一个diff匹配 @@ 划分
			if (strs[i].contains("Index:") || strs[i].contains(fileName)) {
				// 去掉index之下几行无用代码
				while (!strs[++i].contains("@@")) {
				}
				while (i < strs.length && strs[i].contains("@@")) {
					String strFilter = "";
					i++;

					// if (strs[i].contains("}")) {
					// i++;
					// }
					while (i < strs.length && !strs[i].contains("@@")) {
						String temp = strs[i];

						if (strs[i].contains("Index:")
								|| strs[i].contains("@@")
								|| strs[i].contains("diff --git"))
							break;
						if (temp.length() > 0) {
							if (temp.subSequence(0, 1).equals(plus)) {
								i++;
								continue;
							}
							if (temp.charAt(0) == '-' || temp.charAt(0) == '+') {
								temp = temp.substring(1);
							}
						}
						temp = temp.replaceAll("\t", "    ");
						strFilter += (strFilter.length() > 0 ? "\n"
								+ temp.trim() : temp.trim());
						i++;
					}
					strFilter = handleFunction(strFilter, functionName);
					strFilterList.add(strFilter);
					System.out.println(strFilter);
				}
			}
		}
		return strFilterList;
	}

	// 处理 diff中修改函数参数或者函数返回值的情况
	// 修改 进一步，functionName是一个列
	String handleFunction(String function, String functionName) {
		if (functionName == null || functionName.contains(",|，")
				|| functionName.length() <= 0) {
			return function;
		}
		String re = "^[\\w\\s]+[\\s\\*]+" + functionName
				+ "\\s*\\([\\s\\S]*?\\)\\s*\\{";
		Pattern pattern = Pattern.compile(re, Pattern.MULTILINE);
		Matcher matcher = pattern.matcher(function);
		if (matcher.find()) {
			function = function.substring(matcher.start()).trim();
		}
		return function;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		HandDiff handDiff = new HandDiff();
		;
		String diffPath2 = "C:\\Users\\wt\\Desktop\\tyy\\实验室work-tyy\\Ffmpeg复用代码获取程序修改\\Ffmpeg补丁文件-新";
		String diffPath3 = "C:\\Users\\wt\\Desktop\\tyy\\实验室work-tyy\\getContainVersion\\Wireshark\\Wireshark补丁文件-2016.3.7-邓之珺修改diff";
		// String diffPath1 =
		// "C:\\Users\\wt\\Desktop\\实验室work-tyy\\Ffmpeg-1.1diff";
		// 多个函数
		// String cve2 = "CVE-2013-7023.txt";
		// 多个函数
		// String cve3 = "CVE-2014-2099.txt";
		// 多个文件CVE-2011-1691;CVE-2011-1202
		// String cve4 = "CVE-2013-2912.txt";
		// String cve5 = "CVE-2013-2911.txt";
		// String cve = "CVE-2013-7015.txt";
		String cve6 = "CVE-2015-3811.txt";
		String fileName6 = "epan/dissectors/packet-wcp.c";
		// String fileName = "libavcodec/flashsv.c";
		String functionName = "";
		// String fileName2 = "Source/core/xml/XSLStyleSheetLibxslt.cpp";
		// String fileName3 =
		// "content\\renderer\\pepper\\pepper_in_process_router.cc";
		ArrayList<String> diffList = handDiff.handleDiff(diffPath3
				+ File.separator + cve6, fileName6, functionName, true);
		System.out.println(diffList.size());
		int i = 0;
		for (String string : diffList) {

			System.out.println(i + "个函数：\n" + string);
			i++;
		}
		ArrayList<String> diffList2 = handDiff.handleDiff(diffPath3
				+ File.separator + cve6, fileName6, functionName, false);
		System.out.println(diffList2.size());
		i = 0;
		for (String string : diffList2) {
			System.out.println(i + "个函数：\n" + string);
			i++;
		}

	}
}
