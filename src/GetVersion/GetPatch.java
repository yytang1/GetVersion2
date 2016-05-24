package GetVersion;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import GetVersion.VulnerabilityInfo.VulnerInfo;

public class GetPatch {
	Common common = new Common();
	Utils utils = new Utils();
	public static final ArrayList<String> reuseToExcel = new ArrayList<String>();
	public static final ArrayList<String> reuseNToExcel = new ArrayList<String>();
	public void executeExcel(String diffPath, String codePath, String excelPath)
			throws Exception {

		VulnerabilityInfo vulnerabilityInfo = new VulnerabilityInfo();
		CheckDiff checkDiff = new CheckDiff();
		HandleVersion handleVersion = new HandleVersion();
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

			 ArrayList<String> containVersions = checkDiff.getVersionContainDiff(diffFilePath, codePathTemp,versionPrefix, vulnerInfo, true);
			 if (containVersions == null) {
			 vulnerInfo.report += "diff文件读取函数出错，请检查diff文件;";
			 continue; }
			 Collections.reverse(containVersions);
				vulnerInfo.containVersions = containVersions;
			vulnerInfo.errorVersions = checkDiff.getVersionContainDiff(
					diffFilePath, codePathTemp, versionPrefix, vulnerInfo,
					false);

			vulnerInfosTemp.add(vulnerInfo);
			vulnerabilityInfo.writeResultToExcel(vulnerInfosTemp, excelPath);
		}
		vulnerabilityInfo.writeResultToExcel(vulnerInfos, excelPath);
	}

	void printResult(ArrayList<VulnerInfo> vulnerInfos) {
		for (VulnerInfo vulnerInfo : vulnerInfos) {
			System.out.println("cve:" + vulnerInfo.cve);
			System.out.println("versions:" + vulnerInfo.containVersions);
		}
		System.out.println(vulnerInfos.size());
	}

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		String diff="E:\\myWork\\实验室work-tyy\\findPatch\\diffs";
	    String excel1="E:\\myWork\\实验室work-tyy\\findPatch\\xen-qemu.xlsx";
	    String excel2="E:\\myWork\\实验室work-tyy\\findPatch\\libav-ffpmeg.xlsx";
	    String excel3="E:\\myWork\\实验室work-tyy\\findPatch\\firefox-thunderbird.xlsx";
	    String exceltest="E:\\myWork\\实验室work-tyy\\findPatch\\xen-qemu_test.xlsx";
	    String codePath="E:\\myWork\\实验室work-tyy\\findPatch";
		GetPatch getPatch = new GetPatch();
		getPatch.executeExcel(diff, codePath, excel3);
		System.out.println("end");
	}

}
