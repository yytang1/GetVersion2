package Tools;

import java.io.File;
import java.util.ArrayList;

import CodeReuse.CodeReuse;
import GetVersion.CheckDiff;
import GetVersion.Common;
import GetVersion.DealSoftware;
import GetVersion.HandleVersion;
import GetVersion.Utils;
import GetVersion.VulnerabilityInfo;
import GetVersion.VulnerabilityInfo.VulnerInfo;

public class GetRuse {
	Common common = new Common();
	Utils utils = new Utils();
	public static final ArrayList<String> reuseToExcel = new ArrayList<String>();
	public static final ArrayList<String> reuseNToExcel = new ArrayList<String>();

	public void executeExcel(String diffPath, String codePath, String excelPath)
			throws Exception {

		VulnerabilityInfo vulnerabilityInfo = new VulnerabilityInfo();
		CheckDiff checkDiff = new CheckDiff();
		HandleVersion handleVersion = new HandleVersion();
		CodeReuse codeReuse = new CodeReuse();
		DealSoftware dealSoftware = new DealSoftware();

		ArrayList<VulnerInfo> vulnerInfos = vulnerabilityInfo
				.readInfoFromExcel(excelPath);
		// 复用实例代码存放路径
		ArrayList<VulnerInfo> vulnerInfosTemp = new ArrayList<VulnerabilityInfo.VulnerInfo>();
		String resultPath = "";
		if (excelPath.contains("\\")) {
			resultPath = excelPath.substring(0, excelPath.lastIndexOf("\\"));
		} else if (excelPath.contains("/")) {
			resultPath = excelPath.substring(0, excelPath.lastIndexOf("/"));
		}
		System.out.println(excelPath);
		System.out.println(utils.deleteFileOrDir(resultPath + File.separator
				+ Common.reuseFunctiionFolder));

		System.out.println(utils.deleteFileOrDir(resultPath + File.separator
				+ Common.reuseN_LinesFolder));

		for (VulnerInfo vulnerInfo : vulnerInfos) {
			if (vulnerInfo.report.length() > 0) {
				continue;
			}
			String versionPrefix = vulnerInfo.softeware + "-";
			System.out.println(vulnerInfo.cve);
			String diffFilePath = common.getDifftxtPath(diffPath,
					vulnerInfo.cve);
			if (!utils.fileExist(diffFilePath)) {
				vulnerInfo.report += "\t该diff文件不存在";
				continue;
			}
			String codePathTemp = codePath + File.separator
					+ vulnerInfo.softeware;
			// 源码所有版本文件名列
			ArrayList<String> fileList = dealSoftware.getFileName(codePath,
					vulnerInfo.softeware);
			// 源码所有版本列
			ArrayList<String> versionList = checkDiff.getFileVersions(fileList,
					versionPrefix);
			// 满足区间条件的版本列
			ArrayList<String> versions = handleVersion.getCodeVersion(
					versionList, vulnerInfo.softwareVersion);
			if (versions == null) {
				vulnerInfo.report += "Software version出错，请注意检查";
				continue;
			}
			// 获取文件存在的版本列
			vulnerInfo.existVersions = checkDiff.getVersionFileExist(
					codePathTemp, versionPrefix, vulnerInfo.fileName, versions);
			System.out.println("文件存在的版本列：" + vulnerInfo.existVersions);
			// 测试

			// 针对同一漏洞的代码复用实例的获取
			System.out.println(versions + "end");

			vulnerInfo.reuseVersionsMost = codeReuse.getMostMatch2(
					diffFilePath, codePathTemp, versionPrefix, vulnerInfo,
					versions, resultPath);
			vulnerInfosTemp.add(vulnerInfo);
			vulnerabilityInfo.writeResultToExcel(vulnerInfosTemp, excelPath);
			vulnerabilityInfo.reuseResultToExcel(excelPath, reuseToExcel);
			vulnerabilityInfo.reuseResultToExcelN(excelPath, reuseNToExcel);
		}
		System.out.println("同一漏洞的代码复用实例存放路径：" + resultPath + "\n");
		vulnerabilityInfo.writeResultToExcel(vulnerInfos, excelPath);
		vulnerabilityInfo.reuseResultToExcel(excelPath, reuseToExcel);
		vulnerabilityInfo.reuseResultToExcelN(excelPath, reuseNToExcel);
	}

	void printResult(ArrayList<VulnerInfo> vulnerInfos) {
		for (VulnerInfo vulnerInfo : vulnerInfos) {
			System.out.println("cve:" + vulnerInfo.cve);
		}
		System.out.println(vulnerInfos.size());
	}

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		String path = "E:\\myWork\\实验室work-tyy\\5.20\\";
		String diffL = path + "diffs";
		String excelL = path + "test2.xlsx";
		String codePath1 = path + "software";
		GetRuse getRuse = new GetRuse();
		getRuse.executeExcel(diffL, codePath1, excelL);
		System.out.println("end");
	}
}
