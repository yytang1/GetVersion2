package GetVersion;

public class Run {

    public static void main(String[] args) throws Exception {
        // TODO Auto-generated method stub
        ExecuteExcel executeExcel = new ExecuteExcel();
        Utils utils = new Utils();

        System.out.println("请输入漏洞表格Excel的路径：");
        String excelPath = utils.getStringOfSystemIn();

        System.out.println("请输入diff文件所在路径：");
        String diffPath = utils.getStringOfSystemIn();

        System.out.println("请输入软件源码所在路径：");
        String codePath = utils.getStringOfSystemIn();
        System.out.println("开始");
        executeExcel.executeExcel(diffPath, codePath, excelPath);
        System.out.println("完成");
    }

}
