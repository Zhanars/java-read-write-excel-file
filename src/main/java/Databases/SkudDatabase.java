package Database;


import Databases.DatabaseClass;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class SkudDatabase extends DatabaseClass {

    private String  EmployeeGroupFPP = "8716F8A3-0F50-4865-9850-4B1CA9B746D5";
    private String  EmployeeGroupFEIB = "98A648B7-78E0-4D04-9592-BC4B19082860";
    private String  EmployeeGroupFIIT = "28675B7D-F03E-4DF0-872C-26D8A172BC40";
    private String  EmployeeGroupFLPID = "BD322909-4AB3-48D4-A1EC-746263EAFA97";
    private String  EmployeeGroupDIS;


    public void setEmployeeGroupFPP(String employeeGroupFPP) {
        EmployeeGroupFPP = employeeGroupFPP;
    }

    public void setEmployeeGroupFEIB(String employeeGroupFEIB) {
        EmployeeGroupFEIB = employeeGroupFEIB;
    }

    public void setEmployeeGroupFIIT(String employeeGroupFIIT) {
        EmployeeGroupFIIT = employeeGroupFIIT;
    }

    public void setEmployeeGroupFLPID(String employeeGroupFLPID) {
        EmployeeGroupFLPID = employeeGroupFLPID;
    }

    public void setEmployeeGroupDIS(String employeeGroupDIS) {
        EmployeeGroupDIS = employeeGroupDIS;
    }

    public SkudDatabase() {
        super("jdbc:sqlserver://37.228.66.86\\RUSGUARD:49182;database=RusGuardDB",
                "joker",
                "Desant3205363",
                "mssql",
                "skud");
    }


    public void setEmployeeNewStudents(ArrayList<Students> list){
        String host = gethost();
        String password = getpassword();
        String username = getuser();
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").
                format(Calendar.getInstance().getTime());
        String EmployeeGroup = "";
        for (int i = 0; i < list.size(); i++) {
            Students students = list.get(i);
            if(checkIIN(students.getIIN())){
                if(students.getFacultet().equals("Пищевых производств"))
                    EmployeeGroup = EmployeeGroupFPP;
                if(students.getFacultet().equals("Экономики и бизнеса"))
                    EmployeeGroup = EmployeeGroupFEIB;
                if(students.getFacultet().equals("Инжиниринга и информационных технологий"))
                    EmployeeGroup = EmployeeGroupFIIT;
                if(students.getFacultet().equals("Лёгкой промышленности и дизайна"))
                    EmployeeGroup = EmployeeGroupFLPID;

                try (Connection conn = DriverManager.getConnection(host,username,password);
                     Statement stmt = conn.createStatement();) {
                    String SQL = "INSERT INTO [dbo].[Employee] ([SecondName],[FirstName]," +
                            "[LastName],[PassportNumber],[CreationDateTime],[ModificationDateTime],[EmployeeGroupID]," +
                            "[IsRemoved],[IsLocked]," +
                            "[IsAccessLevelsInherited],[IsWorkSchedulesInherited],[IsWorkZonesInherited]) " +
                            "VALUES " +
                            "('"+replaceStrKztoRu(students.getLastname().toLowerCase())+"', " +
                            "'"+replaceStrKztoRu(students.getFirstname().toLowerCase())+"', " +
                            "'"+replaceStrKztoRu(students.getPatronymic().toLowerCase())+"'," +
                            "'"+students.getIIN()+"'," +
                            "'"+timeStamp+"'," +
                            "'"+timeStamp+"'," +
                            "'"+EmployeeGroup+"'," +
                            "0,0,0,1,1) ";
                    stmt.executeUpdate(SQL);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else  {

            }
        }

    }

    public void setEmployeeAcsAccessLevel(){
        String host = gethost();
        String password = getpassword();
        String username = getuser();
        try (Connection conn = DriverManager.getConnection(host,username,password);
             Statement stmt = conn.createStatement();) {
            String SQL = " INSERT INTO [dbo].[EmployeeAcsAccessLevel] ([EmployeeID]" +
                    "      ,[AcsAccessLevelID]" +
                    "      ,[EndDate])" +
                    "  select [_id],'EE1CCAD9-DB66-4FBD-ACEA-6AED573863EF',null FROM [dbo].[Employee] where " +
                    "[EmployeeGroupID] = '%"+EmployeeGroupFPP+"%' or " +
                    "[EmployeeGroupID] ='%"+EmployeeGroupFEIB+"%' or " +
                    "[EmployeeGroupID] ='%"+EmployeeGroupFIIT+"%' or " +
                    "[EmployeeGroupID] ='%"+EmployeeGroupFLPID+"%'";
            stmt.executeUpdate(SQL);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    public String replaceStrKztoRu(String str){
        str =  str.replace("ә","а");
        str = str.replace("і","и");
        str = str.replace("ң","н");
        str = str.replace("ү","у");
        str = str.replace("ұ","у");
        str = str.replace("қ","к");
        str = str.replace("ө","о");
        str = str.replace("һ","х");
        return str;
    }


    public boolean checkIIN(String IIN) {
        boolean bool = true;
        String host = gethost();
        String password = getpassword();
        String username = getuser();
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").
                format(Calendar.getInstance().getTime());

        try (Connection conn = DriverManager.getConnection(host,username,password);
             Statement stmt = conn.createStatement();) {
            String SQL = "SELECT  [PassportNumber] " +
                    "FROM [RusGuardDB].[dbo].[Employee] where PassportNumber like '%"+IIN+"%'";
            ResultSet rs = stmt.executeQuery(SQL);
            if(rs.next()){
                bool=false;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bool;

    }
}
