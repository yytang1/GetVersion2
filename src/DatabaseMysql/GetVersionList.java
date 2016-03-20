package DatabaseMysql;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;

import GetVersion.ExcelUtil;

/**
 * 
 * @author tyy
 * 
 *         读取excel和数据库，获取包含漏洞的版本列，写入excel 中version used列中，结果表格添加后缀“=GetVersion”
 *
 */
public class GetVersionList {

    ExcelUtil excelUtil = new ExcelUtil();
    JDBCoption jdbCoption = new JDBCoption();

    public class VulnerAllInfo {
        public String cve = ""; // cve号
        public String cwe = ""; // cve号
        public String softeware = ""; // 软件名
        public String softwareVersion = ""; // 含漏洞软件版本
        public String fileName = ""; // 漏洞文件名
        public String functionName = ""; // 漏洞函数名
        public String type = "";// 类型
        public String patchLink = ""; // 补丁链接
        public String versionUsed = ""; // version used 版本，从数据库获取
    }

    /**
     * 从excel中读取
     * 
     * @param filePath
     *            excel路径
     * @return 预置数据信息数组，不正确时返回{@code null}
     * @throws InvalidFormatException
     */
    public ArrayList<VulnerAllInfo> readInfoFromExcel(String filePath)
            throws InvalidFormatException {
        ArrayList<VulnerAllInfo> vulnerAllInfos = new ArrayList<GetVersionList.VulnerAllInfo>();

        if (filePath == null) {
            return vulnerAllInfos;
        }
        ArrayList<Row> rows = excelUtil.readExcelHssfRows(filePath, 0);
        if (rows.size() == 0) {
            return vulnerAllInfos;
        }
        for (Row row : rows) {
            VulnerAllInfo vulnerAllInfo = new VulnerAllInfo();
            String cve = excelUtil.getCellStringValue(row, 0);
            if (cve == null || cve.length() < 1) {
                continue;
            }
            vulnerAllInfo.cve = cve;
            vulnerAllInfo.cwe = excelUtil.getCellStringValue(row, 1);
            vulnerAllInfo.softeware = excelUtil.getCellStringValue(row, 2);
            vulnerAllInfo.softwareVersion = excelUtil.getCellStringValue(row, 3);
            vulnerAllInfo.fileName = excelUtil.getCellStringValue(row, 4);
            vulnerAllInfo.functionName = excelUtil.getCellStringValue(row, 5);
            vulnerAllInfo.type = excelUtil.getCellStringValue(row, 6);
            System.out.println(vulnerAllInfo.type);
            vulnerAllInfo.patchLink = excelUtil.getCellStringValue(row, 7);
            vulnerAllInfo.versionUsed = excelUtil.getCellStringValue(row, 8);

            vulnerAllInfos.add(vulnerAllInfo);
        }
        Connection conn = jdbCoption.getConnection();
        Statement stmt = null;
        for (VulnerAllInfo vulnerAllInfo : vulnerAllInfos) {
            String sqlStr = "select * from contain_verion_t where cve=\"" + vulnerAllInfo.cve
                    + "\" and software=\"" + vulnerAllInfo.softeware + "\";";
            System.out.println(sqlStr);
            try {
                stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sqlStr);
                if (rs.next()) {
                    vulnerAllInfo.versionUsed = rs.getString("versions");
                }
                rs.close();
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
        return vulnerAllInfos;
    }

    public void sqlQuery() {
        String cve = "CVE-2015-6249";
        String software = "wireshark";
        String sqlStr = "select * from contain_verion_t where cve=\"" + cve + "\" and software=\""
                + software + "\";";
        System.out.println(sqlStr);
        Connection conn = jdbCoption.getConnection();
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sqlStr);
            while (rs.next()) {
                System.out.println(rs.getString("cve"));
                System.out.println(rs.getString("software"));
                System.out.println(rs.getString("versions") + "\n");
            }
            rs.close();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private HSSFCellStyle initWorkbook(HSSFWorkbook workbook) {
        HSSFCellStyle cellStyle = workbook.createCellStyle();
        HSSFSheet sheet = workbook.createSheet();
        // 初始化纵向对齐方式为居中
        cellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
        // 初始化单元格大小为包含文本
        cellStyle.setWrapText(true);
        // 初始化工作表的列宽
        sheet.setColumnWidth(0, 15 * 256);
        sheet.setColumnWidth(1, 15 * 256);
        sheet.setColumnWidth(2, 15 * 256);
        sheet.setColumnWidth(3, 25 * 256);
        sheet.setColumnWidth(4, 25 * 256);
        sheet.setColumnWidth(5, 40 * 256);
        sheet.setColumnWidth(6, 40 * 256);
        sheet.setColumnWidth(7, 30 * 256);
        sheet.setColumnWidth(8, 30 * 256);
        return cellStyle;
    }

    public void writeToExcel(ArrayList<VulnerAllInfo> vulnerAllInfos, String excelPath) {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFCellStyle cellStyle = initWorkbook(workbook);
        HSSFSheet sheet = workbook.getSheetAt(0);
        int temp = -1;
        for (int rowIndex = 0; rowIndex < vulnerAllInfos.size(); rowIndex++) {
            VulnerAllInfo vulnerAllInfo = vulnerAllInfos.get(rowIndex);
            HSSFRow row = sheet.createRow(rowIndex);
            HSSFCell cell = null;
            for (int i = 0; i < 9; i++) {
                cell = row.createCell(i);
                if (rowIndex == 0) {
                    cellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
                } else {
                    cellStyle.setAlignment(HSSFCellStyle.ALIGN_LEFT);
                }
                cell.setCellStyle(cellStyle);
                switch (i) {
                case 0:
                    cell.setCellValue(rowIndex == temp ? "CVE ID" : vulnerAllInfo.cve);
                    break;
                case 1:
                    cell.setCellValue(rowIndex == temp ? "CWE ID" : vulnerAllInfo.cwe);
                    break;
                case 2:
                    cell.setCellValue(rowIndex == temp ? "Software" : vulnerAllInfo.softeware);
                    break;
                case 3:
                    cell.setCellValue(rowIndex == temp ? "Software version"
                            : vulnerAllInfo.softwareVersion);
                    break;
                case 4:
                    cell.setCellValue(rowIndex == temp ? "漏洞文件名" : vulnerAllInfo.fileName);
                    break;
                case 5:
                    cell.setCellValue(rowIndex == temp ? "主要修补函数名" : vulnerAllInfo.functionName);
                    break;
                case 6:
                    cell.setCellValue(rowIndex == temp ? "类型" : vulnerAllInfo.type);
                    break;
                case 7:
                    cell.setCellValue(rowIndex == temp ? "补丁链接" : vulnerAllInfo.patchLink);
                    break;
                case 8:
                    cell.setCellValue(rowIndex == temp ? "Version used" : vulnerAllInfo.versionUsed);
                    break;
                default:
                    break;
                }
            }
        }
        try {
            String path = excelPath.substring(0, excelPath.lastIndexOf("\\") + 1);
            String name = excelPath.substring(excelPath.lastIndexOf("\\") + 1);
            if (name.contains(".xlsx")) {
                name = name.replace(".xlsx", "-GetVersion.xls");
            } else {
                name = name.replace(".xls", "-GetVersion.xls");
            }
            new File(path).mkdirs();
            workbook.write(new FileOutputStream(path + name));
            workbook.close();
            System.out.println(path + name);
        } catch (IOException e) {
            System.out.println(e.toString());
        }
    }

    public static void main(String[] args) {
        try {
            GetVersionList getVersionList = new GetVersionList();
            // getVersionList.sqlQuery();
            String filePath = "C:\\Users\\wt\\Desktop\\tyy\\实验室work-tyy\\getContainVersion\\Wireshark\\Wireshark漏洞信息.xls";
            String filePath2 = "C:\\Users\\wt\\Desktop\\tyy\\实验室work-tyy\\getContainVersion\\2016.1.26-Ffmpeg漏洞信息-上传服务器.xls";
            ArrayList<VulnerAllInfo> vulnerAllInfos = getVersionList.readInfoFromExcel(filePath2);
            getVersionList.writeToExcel(vulnerAllInfos, filePath2);
            System.out.println("end");
        } catch (InvalidFormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
