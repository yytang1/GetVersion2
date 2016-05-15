package GetVersion;

import java.io.File;
import java.util.ArrayList;

import GetVersion.VulnerabilityInfo.VulnerInfo;

public class CheckDiff {

	Utils utils = new Utils();
	HandDiff handDiff = new HandDiff();
	Common common = new Common();

	public ArrayList<String> getFileVersions(ArrayList<String> fileLists,
			String versionPrefix) {

		ArrayList<String> versionLists = new ArrayList<String>();
		for (int i = 0; i < fileLists.size(); i++) {
			versionLists.add(fileLists.get(i).replaceAll(versionPrefix, ""));
		}
		return versionLists;
	}

	public String getCodeFile(String versionPrefix, String codepath,
			String version, String fileName) {
		String content = "";
		String path = common.getCodefilePath(codepath, versionPrefix, version,
				fileName);
		content = utils.readText(path);
		content = handleCode(content);
		return content;
	}

	String handleCode(String code) {
		String temp = "";
		code = common.handleLineBreak(code);
		String[] codeList = code.trim().split("\n");
		for (String string : codeList) {
			String codesLine = string;
			if (string.length() > 0) {
				codesLine = string.trim();
			}
			codesLine = codesLine.replaceAll("\t", "    ");
			temp += (temp.length() > 0 ? "\n" + codesLine : codesLine);
		}
		return temp;
	}

	public ArrayList<String> getVersionFileExist(String codePath,
			String versionPrefix, String fileName, ArrayList<String> versionList) {
		ArrayList<String> versionsTrue = new ArrayList<String>();
		String[] fileList = fileName.split("[;；]");

		for (String version : versionList) {
			int i = 0;
			for (i = 0; i < fileList.length; i++) {
				fileList[i] = fileList[i].replaceAll("[ |　]", " ")
						.replaceAll("\\u00A0", "").trim();
				String filePath = common.getCodefilePath(codePath,
						versionPrefix, version, fileList[i]);
				if (filePath.length() < 1) {
					break;
				}
			}
			if (i == fileList.length) {
				versionsTrue.add(version);
			}
		}
		return versionsTrue;
	}

	/**
	 * 
	 * @param diffStr
	 *            diff文件内容
	 * @param codepath
	 *            源码路径
	 * @param versionPrefix
	 *            版本号前缀
	 * @param versionList
	 *            版本列
	 * @return 满足条件的版本列
	 */
	public ArrayList<String> getVersionContainDiff(String cve, String codepath,
			String versionPrefix, VulnerInfo vulnerInfo, boolean isOld) {
		String[] fileList = vulnerInfo.fileName.split("[;；]");
		int functionNameIsNull = 1;
		if (vulnerInfo.functionName == null
				|| vulnerInfo.functionName.length() < 1) {
			functionNameIsNull = 0;
		}
		String[] functionList = (functionNameIsNull > 0 ? vulnerInfo.functionName
				.split("[;；]") : null);
		ArrayList<String> versionsTrueFinal = new ArrayList<String>();
		versionsTrueFinal.addAll(vulnerInfo.existVersions);
		for (int i = 0; i < fileList.length; i++) {
			String fileName = fileList[i].replaceAll("[ |　]", " ")
					.replaceAll("\\u00A0", "").trim();
			String functionName = (functionNameIsNull > 0 ? functionList[i]
					.replaceAll("[ |　]", " ").replaceAll("\\u00A0", "").trim()
					: null);
			ArrayList<String> diffStrList = handDiff.handleDiff(cve, fileName,
					functionName, isOld);
			if (diffStrList == null || diffStrList.size() < 1) {
				return null;
			}
			ArrayList<String> versionsTrue = new ArrayList<String>();
			int flag = 1;
			for (String version : vulnerInfo.existVersions) {
				flag = 1;
				String codeStr = getCodeFile(versionPrefix, codepath, version,
						fileName);
				for (String diffStr : diffStrList)
					if (!codeStr.contains(diffStr)) {
						flag = 0;
						break;
					}
				if (flag > 0) {
					versionsTrue.add(version);
				}
			}
			versionsTrueFinal.retainAll(versionsTrue);
		}
		return versionsTrueFinal;
	}

	public static void main(String[] args) {
		DealSoftware dealSoftware = new DealSoftware();
		// TODO Auto-generated method stub
		// String codepath = "C:\\Users\\wt\\Desktop\\ffmpeg";
		String codepath2 = "C:\\Users\\wt\\Desktop\\tyy\\software\\ffmpeg";
		// excel 中 获取的函数文件名
		String filePath = "libavcodec\\huffyuv.c";
		// String diffPath =
		// "C:\\Users\\wt\\Desktop\\实验室work-tyy\\需完成的工作\\测试数据\\Ffmpeg\\Ffmpeg-1.1diff";
		String diffPath2 = "C:\\Users\\wt\\Desktop\\tyy\\实验室work-tyy\\Ffmpeg复用代码获取程序修改\\Ffmpeg补丁文件-新";
		// String cve = diffPath2 + File.separator + "CVE-2013-7015.txt";
		// 多个函数测试
		String cve2 = diffPath2 + File.separator + "CVE-2013-0848.txt";
		String versionPrefix = "ffmpeg-";
		System.out.println("begin");
		CheckDiff checkDiff = new CheckDiff();
		String software = "ffmpeg";
		ArrayList<String> fileList = dealSoftware.getFileName(codepath2,
				software);
		ArrayList<String> versionList = checkDiff.getFileVersions(fileList,
				versionPrefix);
		// 满足区间条件的版本列
		VulnerabilityInfo.VulnerInfo vulnerInfo = new VulnerInfo();
		vulnerInfo.existVersions = versionList;
		vulnerInfo.fileName = filePath;
		ArrayList<String> versions = checkDiff.getVersionContainDiff(cve2,
				codepath2, versionPrefix, vulnerInfo, false);
		System.out.println(versions.size() + "end");
		System.out.println(versions);
		ArrayList<String> versions2 = checkDiff.getVersionContainDiff(cve2,
				codepath2, versionPrefix, vulnerInfo, true);
		System.out.println(versions2.size() + "end");
		System.out.println(versions2);
		System.out.println(checkDiff.getCodeFile(versionPrefix, codepath2,
				"1.0", filePath));
	}
}
