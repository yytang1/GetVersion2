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

public class HandleDiffCodeSeg {
	ExcelUtil excelUtil = new ExcelUtil();
	ArrayList<String> diffs = new ArrayList<String>();
	ArrayList<String> funcs = new ArrayList<String>();

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		HandleDiffCodeSeg handleDiffCodeSeg=new HandleDiffCodeSeg();
		// TODO Auto-generated method stub
		Utils utils = new Utils();
		String excelPath = "";
		excelPath="D:\\我的文档\\我的桌面\\linux\\linux_kernelReuseResultSummary.xls";
//	    if (args.length >= 1) {
//	    	excelPath = args[0];
//	    } else {
//	       System.out.println("请输入漏洞表格Excel的路径：");
//	       excelPath = utils.getStringOfSystemIn();
//	    }
			handleDiffCodeSeg.readInfoFromExcel(excelPath);
			handleDiffCodeSeg.writeResultToExcel(excelPath);
		
	}

	/**
	 * 从excel中读取
	 * 
	 * @param filePath
	 *            excel路径
	 * @return 预置数据信息数组，不正确时返回{@code null}
	 * @throws InvalidFormatException
	 */
	public void readInfoFromExcel(String filePath) {
		ArrayList<Row> rows;
		try {
			rows = excelUtil.readExcelHssfRows(filePath, 0);
			rows.remove(0);
			for (Row row : rows) {
				String temp = excelUtil.getCellStringValue(row, 0);
				if (temp == null || temp.length() < 1) {
					continue;
				}
				String diff = temp.substring(0, 13)+"_VULN_"+temp.charAt(temp.length()-1);
				String func = temp.substring(0, temp.length() - 2);
				diffs.add(diff);
				funcs.add(func);
			}
		} catch (InvalidFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		sheet.setColumnWidth(0, 40 * 256);
		sheet.setColumnWidth(1, 60 * 256);
		return cellStyle;
	}

	/***
	 * 
	 * @param vulnerInfos
	 * @param excelPath
	 *            结果表格路径地址
	 */
	public void writeResultToExcel(String excelPath) {
		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFCellStyle cellStyle = initWorkbook(workbook);
		HSSFSheet sheet = workbook.getSheetAt(0);
		for (int rowIndex = 0; rowIndex < funcs.size() + 1; rowIndex++) {
			HSSFRow row = sheet.createRow(rowIndex);
			HSSFCell cell = null;
			for (int i = 0; i < 2; i++) {
				cell = row.createCell(i);
				if (rowIndex == 0) {
					cellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
				} else {
					cellStyle.setAlignment(HSSFCellStyle.ALIGN_LEFT);
				}
				cell.setCellStyle(cellStyle);
				switch (i) {
				case 0:
					cell.setCellValue(rowIndex == 0 ? "diff段" : diffs
							.get(rowIndex - 1));
					break;
				case 1:
					cell.setCellValue(rowIndex == 0 ? "CVE号_软件名版本号_函数名" : funcs
							.get(rowIndex - 1));
					break;
				default:
					break;
				}
			}
		}
		try {
			String path = excelPath.substring(0,
					excelPath.lastIndexOf("\\") + 1);
			String name = excelPath.substring(excelPath.lastIndexOf("\\") + 1);
			if (name.contains(".xlsx")) {
				name = name.replace(".xlsx", "Change.xls");
			} else {
				name = name.replace(".xls", "Change.xls");
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
