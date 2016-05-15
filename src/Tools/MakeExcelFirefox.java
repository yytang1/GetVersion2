package Tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;

import GetVersion.ExcelUtil;
import GetVersion.Utils;
import GetVersion.VulnerabilityInfo.VulnerInfo;

public class MakeExcelFirefox {
	
	ExcelUtil excelUtil = new ExcelUtil();
	public static void main(String[] args) {
		
		MakeExcelFirefox makeExcelFirefox=new MakeExcelFirefox();
		// TODO Auto-generated method stub
		Utils utils = new Utils();
		String excelPath = "";
	    if (args.length >= 1) {
	    	excelPath = args[0];
	    } else {
	       System.out.println("请输入漏洞表格Excel的路径：");
	       excelPath = utils.getStringOfSystemIn();
	    }
	    try {
			ArrayList<VulnerInfo>  vulnerInfos=  makeExcelFirefox.readInfoFromExcel(excelPath);
			makeExcelFirefox.writeResultToExcel(vulnerInfos, excelPath);
		} catch (InvalidFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	}
	
	/**
     * 从excel中读取
     * 
     * @param filePath
     *            excel路径
     * @return 预置数据信息数组，不正确时返回{@code null}
     * @throws InvalidFormatException
     */
    public ArrayList<VulnerInfo> readInfoFromExcel(String filePath) throws InvalidFormatException {
        ArrayList<VulnerInfo> vulnerInfos = new ArrayList<VulnerInfo>();
        if (filePath == null) {
            return vulnerInfos;
        }
        ArrayList<Row> rows = excelUtil.readExcelHssfRows(filePath, 0);
        if (rows.size() == 0) {
            return vulnerInfos;
        }
        rows.remove(0);
        for (Row row : rows) {
            String temp = excelUtil.getCellStringValue(row, 0);
            if (temp == null || temp.length() < 1) {
                continue;
            }
            VulnerInfo vulnerInfo = new VulnerInfo();

            vulnerInfo.cve = excelUtil.getCellStringValue(row, 0).replaceAll("[ |　]", " ")
                    .replaceAll("\\u00A0", "").trim();
            vulnerInfo.softeware = excelUtil.getCellStringValue(row, 2).replaceAll("[ |　]", " ")
                    .replaceAll("\\u00A0", "").trim().toLowerCase();
            if (vulnerInfo.softeware.equals("linux_kernel")) {
                vulnerInfo.softeware = "linux";
            }
            if(vulnerInfo.softeware.contains("firefox")){
            	vulnerInfo.softeware="firefox";
            }
            String softwareVersion=excelUtil.getCellStringValue(row, 3)
                    .replaceAll("[ |　]", " ").replaceAll("\\u00A0", "").trim();
            String[] softwareVersionList=softwareVersion.split("[;；]");
            String version="";
            if (softwareVersionList.length>1) {
				for (int i = 0; i < softwareVersionList.length; i++) {
					if (softwareVersionList[i].contains("Mozilla Firefox")||softwareVersionList[i].contains("MozillaFirefox")) {
						version=softwareVersionList[i];
					}
					else if (!softwareVersion.contains("Mozilla Firefox")&&!softwareVersion.contains("MozillaFirefox")) {
						if (softwareVersionList[i].contains("Firefox")) {
							version=softwareVersionList[i];
						}
					}
				}
			}
            else {
            	version=softwareVersion;
			}
            version=version.replace("Mozilla Firefox","");
            version=version.replace("MozillaFirefox","");
            version=version.replace("Firefox","");
            vulnerInfo.softwareVersion = version.trim();

            String fileName = excelUtil.getCellStringValue(row, 4);
            if (fileName == null) {
                vulnerInfo.report += "漏洞文件名为空，请注意查看";
                vulnerInfos.add(vulnerInfo);
                continue;
            }
            fileName = fileName.replaceAll("[ |　]", " ").replaceAll("\\u00A0", "").trim();
            vulnerInfo.fileName = fileName;
            System.out.println(fileName);
            String[] fileList = fileName.split("[;；]");

            String functionName = excelUtil.getCellStringValue(row, 5);
            if (functionName != null) {
                String[] functionList = functionName.split("[;；]");
                if (functionList.length < fileList.length) {
                    vulnerInfo.report += "漏洞文件名和漏洞函数不匹配，请注意查看";
                    vulnerInfos.add(vulnerInfo);
                    continue;
                }
                vulnerInfo.functionName = functionName.replaceAll("\\u00A0", "").trim();
            } else {
                vulnerInfo.functionName = functionName;
            }
            vulnerInfos.add(vulnerInfo);
        }
        return vulnerInfos;
    }
    
    /**
     * 初始化一个结果展示的Excel,并创建一个单元格样式,初始化基本值
     * 
     * @param workbook
     *            初始化的Excel
     * @return 返回创建的单元格样式{@link HSSFCellStyle}
     */
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
        return cellStyle;
    }
    /***
     * 
     * @param vulnerInfos
     * @param excelPath
     *            结果表格路径地址
     */
    public void writeResultToExcel(ArrayList<VulnerInfo> vulnerInfos, String excelPath) {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFCellStyle cellStyle = initWorkbook(workbook);
        HSSFSheet sheet = workbook.getSheetAt(0);
        for (int rowIndex = 0; rowIndex < vulnerInfos.size() + 1; rowIndex++) {
            VulnerInfo vulnerInfo = rowIndex == 0 ? null : vulnerInfos.get(rowIndex - 1);
            HSSFRow row = sheet.createRow(rowIndex);
            HSSFCell cell = null;
            for (int i = 0; i < 6; i++) {
                cell = row.createCell(i);
                if (rowIndex == 0) {
                    cellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
                } else {
                    cellStyle.setAlignment(HSSFCellStyle.ALIGN_LEFT);
                }
                cell.setCellStyle(cellStyle);
                switch (i) {
                case 0:
                    cell.setCellValue(rowIndex == 0 ? "CVE ID" : vulnerInfo.cve);
                    break;
                case 1:
                    cell.setCellValue(rowIndex == 0 ? "CWE ID" : "unknown");
                    break;    
                case 2:
                    cell.setCellValue(rowIndex == 0 ? "软件名" : vulnerInfo.softeware);
                    break;
                case 3:
                    cell.setCellValue(rowIndex == 0 ? "含漏洞的软件版本"
                            : vulnerInfo.softwareVersion);
                    break;
                case 4:
                    cell.setCellValue(rowIndex == 0 ? "漏洞文件名" : vulnerInfo.fileName);
                    break;
                case 5:
                    cell.setCellValue(rowIndex == 0 ? "主要修补函数名" : vulnerInfo.functionName);
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
                name = name.replace(".xlsx", "Firefox.xls");
            } else {
                name = name.replace(".xls", "Firefox.xls");
            }
            new File(path).mkdirs();
            workbook.write(new FileOutputStream(path + name));
            workbook.close();
            System.out.println(path + name);
        } catch (IOException e) {
            System.out.println(e.toString());
        }
    }
}
