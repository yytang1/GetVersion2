package GetVersion;

public class RunPatch {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub

		GetPatch getPatch = new GetPatch();
		Utils utils = new Utils();

		String excelPath = "";
		if (args.length >= 1) {
			excelPath = args[0];
		} else {
			System.out.println("请输入漏洞表格Excel的路径：");
			excelPath = utils.getStringOfSystemIn();
		}

		String diffPath = "";
		if (args.length >= 2) {
			diffPath = args[1];
		} else {
			System.out.println("请输入diff文件所在路径：");
			diffPath = utils.getStringOfSystemIn();
		}

		String codePath = "";
		if (args.length >= 3) {
			codePath = args[2];
		} else {
			System.out.println("请输入软件源码所在路径：");
			codePath = utils.getStringOfSystemIn();
		}

		System.out.println("开始");
		getPatch.executeExcel(diffPath, codePath, excelPath);
		System.out.println("完成");
	}
}
