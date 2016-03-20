package DatabaseMysql;

import java.sql.*;
import java.util.ArrayList;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import DatabaseMysql.GetDataFromExcel.versionInfo;

public class JDBCoption {

    public Connection getConnection() {
        Connection connection = null;
        try {
            Class.forName("com.mysql.jdbc.Driver"); // 加载MYSQL JDBC驱动程序
            // Class.forName("org.gjt.mm.mysql.Driver");
            System.out.println("Success loading Mysql Driver!");
        } catch (Exception e) {
            System.out.print("Error loading Mysql Driver!");
            e.printStackTrace();
        }
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/code_reuse",
                    "root", "321");
            // 连接URL为 jdbc:mysql//服务器地址/数据库名 ，后面的2个参数分别是登陆用户名和密码
            System.out.println("数据库连接成功");

        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return connection;

    }

    public void installPatch() throws SQLException, InvalidFormatException {

        GetDataFromExcel getDataFromExcel = new GetDataFromExcel();
        String path = "C:\\Users\\wt\\Desktop\\tyy\\实验室work-tyy\\getContainVersion\\versions2\\";
        ArrayList<String> filepaths = new ArrayList<String>();
        int name = 2002;
        for (int i = name; i <= 2016; i++) {
            String filePath = path + "nvdcve-" + String.valueOf(i) + ".xls";
            System.out.println(filePath);
            filepaths.add(filePath);
        }
        int size = 0;
        Connection conn = getConnection();
        conn.setAutoCommit(false);
        for (String string : filepaths) {
            String sql = "INSERT Contain_Verion_t(cve,software,versions)VALUES(?,?,?)";
            PreparedStatement prest = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_SENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            ArrayList<versionInfo> vInfosTemp = getDataFromExcel.readInfoFromExcel(string);
            for (int x = 0; x < vInfosTemp.size(); x++) {
                prest.setString(1, vInfosTemp.get(x).cve);
                prest.setString(2, vInfosTemp.get(x).softeware);
                prest.setString(3, vInfosTemp.get(x).versions);
                prest.addBatch();
            }
            System.out.println(prest.executeBatch());
            conn.commit();
            size += vInfosTemp.size();
            System.out.println(size);
        }
        closeConnection(null, null, conn);
        System.out.println("总数：" + size);
    }

    public void closeConnection(ResultSet resultSet, PreparedStatement preparedStatement,
            Connection connection) throws SQLException {

        if (resultSet != null)
            resultSet.close();
        if (preparedStatement != null)
            preparedStatement.close();
        if (connection != null && connection.isClosed() == false)
            connection.close();
        System.out.println("数据库关闭");
    }

    public static void main(String args[]) {
        JDBCoption jdbCoption = new JDBCoption();
        try {
            jdbCoption.installPatch();
            System.out.println("end");

        } catch (InvalidFormatException | SQLException e) {
            // TODO Auto-generated catch block
            System.out.println("error");
            e.printStackTrace();
        }
    }
}