package GetVersion;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.ArrayList;

import org.apache.poi.POIXMLDocument;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelUtil {

	public Workbook create(InputStream inp) throws IOException,
			InvalidFormatException {
		if (!inp.markSupported()) {
			inp = new PushbackInputStream(inp, 8);
		}
		if (POIFSFileSystem.hasPOIFSHeader(inp)) {
			return new HSSFWorkbook(inp);
		}
		if (POIXMLDocument.hasOOXMLHeader(inp)) {
			return new XSSFWorkbook(OPCPackage.open(inp));
		}
		throw new IllegalArgumentException("你的excel版本目前poi解析不了");
	}

	/**
	 * 读取Excel中所有行
	 * 
	 * @param filePath
	 *            Excel的路径
	 * @param sheetnum
	 *            sheet页码
	 * @return 返回Excel行数组
	 * @throws InvalidFormatException
	 */
	public ArrayList<Row> readExcelHssfRows(String filePath, int sheetnum)
			throws InvalidFormatException {
		ArrayList<Row> rows = new ArrayList<Row>();
		int rowIndex = 0;
		try {
			InputStream in = new FileInputStream(filePath);
			Workbook workbook = null;
			workbook = create(in);
			// if (filePath.endsWith("xlsx")) {
			// workbook = new XSSFWorkbook(in);
			// } else {
			// workbook = new HSSFWorkbook(in);
			// }
			Sheet sheet = workbook.getSheetAt(sheetnum);
			while (true) {
				Row row = sheet.getRow(rowIndex);
				if (row == null) {
					break;
				}
				rows.add(row);
				rowIndex++;
			}
			workbook.close();
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return rows;
	}

	/**
	 * 返回Excel中单元格的{@link String}值
	 * 
	 * @param row
	 *            单元格所在行
	 * @param index
	 *            单元格所在列的索引
	 * @return 当所在行为空或类型不正确时返回{@code null}
	 */
	public String getCellStringValue(Row row, int index) {
		Cell cell = row.getCell(index);
		if (cell == null) {
			return null;
		}
		if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
			String string = cell.getStringCellValue().trim();
			return string == "" ? null : string;
		}
		if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
			return String.valueOf(cell.getNumericCellValue());
		}
		return null;
	}

	public static void main(String[] args) throws Exception {
	}
}