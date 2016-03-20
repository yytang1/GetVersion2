package DatabaseMysql;

import java.util.ArrayList;
import java.util.HashSet;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;

import GetVersion.ExcelUtil;

public class GetDataFromExcel {
    ExcelUtil excelUtil = new ExcelUtil();

    public static class versionInfo {
        public String cve = ""; // cve号
        public String softeware = ""; // 软件名
        public String versions = ""; // 含漏洞软件版本
    }

    /**
     * 从excel中读取
     * 
     * @param filePath
     *            excel路径
     * @return 预置数据信息数组，不正确时返回{@code null}
     * @throws InvalidFormatException
     */
    public ArrayList<versionInfo> readInfoFromExcel(String filePath) throws InvalidFormatException {
        ArrayList<versionInfo> verInfos = new ArrayList<GetDataFromExcel.versionInfo>();

        if (filePath == null) {
            return verInfos;
        }
        ArrayList<Row> rows = excelUtil.readExcelHssfRows(filePath, 0);
        if (rows.size() == 0) {
            return verInfos;
        }
        for (Row row : rows) {
            versionInfo verInfo = new versionInfo();
            verInfo.cve = excelUtil.getCellStringValue(row, 0).replaceAll("[ |　]", " ")
                    .replaceAll("\\u00A0", "").trim();
            verInfo.softeware = excelUtil.getCellStringValue(row, 1).replaceAll("[ |　]", " ")
                    .replaceAll("\\u00A0", "").trim();
            verInfo.softeware = verInfo.softeware.toLowerCase();
            verInfo.versions = excelUtil.getCellStringValue(row, 2);
            if (!verInfo.softeware.equals("nn")) {
                continue;
            }
            if (verInfo.versions == null || verInfo.versions.length() < 1) {
                continue;
            }
            verInfos.add(verInfo);
        }
        return (new ArrayList<versionInfo>(new HashSet<versionInfo>(verInfos)));
    }

    public static void main(String[] args) {
        GetDataFromExcel getDataFromExcel = new GetDataFromExcel();
        String path = "C:\\Users\\wt\\Desktop\\tyy\\实验室work-tyy\\getContainVersion\\versions2\\";
        String filePath2015 = path + "nvdcve-2015.xls";
        ArrayList<versionInfo> versionInfos;
        try {
            versionInfos = getDataFromExcel.readInfoFromExcel(filePath2015);
            for (versionInfo versionInfo : versionInfos) {
                System.out.println(versionInfo.cve + "\t" + versionInfo.softeware + "\t"
                        + versionInfo.versions);
            }
            System.out.println(versionInfos.size());
        } catch (InvalidFormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
