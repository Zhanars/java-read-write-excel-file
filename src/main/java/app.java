import geneticrasp.GeneticPerson;
import geneticrasp.GeneticRooms;
import geneticrasp.Start;
import org.ini4j.Wini;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.*;

public class app {
    public static void main(String[] args) throws IOException {
        // Create a variable for the connection string.

        Wini ini = new Wini(new File("src\\main\\resources\\config.ini"));
        String server = ini.get("database", "server");
        String dbname = ini.get("database", "dbname");
        String username = ini.get("database", "username");
        String password = ini.get("database", "password");
        String connectionUrl = "jdbc:sqlserver://" + server + ";databaseName=" + dbname + ";user=" + username + ";password=" + password;
        try (Connection con = DriverManager.getConnection(connectionUrl); Statement stmt = con.createStatement();) {
            String SQL = "SELECT [schedule_time_id]" +
                    "      ,[schedule_time_begin]" +
                    "      ,[schedule_time_end]" +
                    "  FROM [atu_univer].[dbo].[univer_schedule_time] where status = 1 and schedule_time_type_id = 1 and schedule_time_id < 12" +
                    "  order by schedule_time_begin";
            String SQL1 = "SELECT g.[group_id]" +
                    "      ,[educ_type_id]" +
                    "      ,[teacher_id]" +
                    "  ,eps.subject_id" +
                    "  ,sp.faculty_id" +
                    "  ,eps.educ_plan_pos_credit  educ_plan_pos_credit" +
                    "  ,count(gs.student_id) studcount" +
                    "  FROM [atu_univer].[dbo].[univer_group] g " +
                    "  join univer_educ_plan_pos eps on eps.educ_plan_pos_id = g.educ_plan_pos_id" +
                    "  join univer_group_student gs on gs.group_id = g.group_id" +
                    "  join univer_educ_plan ep on ep.educ_plan_id = eps.educ_plan_id" +
                    "  JOIN univer_speciality sp on sp.speciality_id = ep.speciality_id" +
                    "   join univer_academ_calendar_pos acc on acc.educ_plan_id = eps.educ_plan_id and acc.acpos_semester = eps.educ_plan_pos_semestr" +
                    "   where acc.control_id = 0 and (acc.acpos_date_start < GETDATE() and acc.acpos_date_end >GETDATE()) " +
                    "and sp.faculty_id < 5 and educ_type_id != 4 and educ_type_id!= 8 and educ_type_id!= 9 and educ_type_id!= 0 " +
                    "and educ_type_id != 1 and (educ_type_id != 2 and sp.faculty_id != 4)" +
                    "  group by g.[group_id]" +
                    "      ,[educ_type_id]" +
                    "      ,[teacher_id]" +
                    " ,eps.subject_id" +
                    "  ,sp.faculty_id" +
                    "  ,eps.educ_plan_pos_credit" +
                    "  order by g.group_id desc";
            String SQL2 = "SELECT [audience_id]" +
                    "      ,[faculty_id]" +
                    "      ,[building_id]" +
                    "      ,[audience_type_id]" +
                    "      ,[audience_floor]" +
                    "      ,[audience_size]" +
                    "      ,[audience_number_ru]  " +
                    "  FROM [atu_univer].[dbo].[univer_audience] where status = 1 order by faculty_id";
            
            
            ResultSet timesfromuniver1 = stmt.executeQuery(SQL);
            int count = getRScount(timesfromuniver1);
            int i = 0;
            String[] times = new String[count];
            ResultSet timesfromuniver = stmt.executeQuery(SQL);
            while (timesfromuniver.next()) {
                times[i] = timesfromuniver.getString("schedule_time_id");
                i++;
            }

            ResultSet groups1 = stmt.executeQuery(SQL1);
            count = getCreditcount(groups1);
            int[] teachers = new int[count];
            GeneticPerson[] persons = new GeneticPerson[count];
            ResultSet groups = stmt.executeQuery(SQL1);
            int n = 0;
            int[][] faculty2 = new int[6][5];
            while (groups.next()) {
                int creditCount = 0;
                while (creditCount < groups.getInt("educ_plan_pos_credit")) {
                    persons[n] = new GeneticPerson(
                            groups.getInt("group_id"),
                            groups.getInt("subject_id"),
                            groups.getInt("educ_type_id"),
                            groups.getInt("teacher_id"),
                            groups.getInt("faculty_id"),
                            groups.getInt("studcount"),
                            creditCount
                    );
                    teachers[n] = groups.getInt("teacher_id");
                    n++;
                    creditCount++;
                    int id;
                    switch (groups.getInt("educ_type_id")) {
                        case 1:
                            id = 2;
                            break;
                        case 2:
                            id = 3;
                            break;
                        case 3:
                            id = 4;
                            break;
                        case 7:
                            id = 3;
                            break;
                        default:
                            id = 2;
                            break;
                    }
                    faculty2[groups.getInt("faculty_id")][id]++;
                }
            }
            ResultSet rs1 = stmt.executeQuery(SQL2);
            count = getRScount(rs1);
            i = 0;
            GeneticRooms[] auditors = new GeneticRooms[count];
            ResultSet auditorsfromuniver = stmt.executeQuery(SQL2);
            int[][] faculty = new int[6][5];
            int a = 0;
            while (auditorsfromuniver.next()) {
                auditors[a] = new GeneticRooms(
                        auditorsfromuniver.getInt("audience_id"),
                        auditorsfromuniver.getInt("faculty_id"),
                        auditorsfromuniver.getInt("building_id"),
                        auditorsfromuniver.getInt("audience_type_id"),
                        auditorsfromuniver.getInt("audience_floor"),
                        auditorsfromuniver.getInt("audience_size"),
                        auditorsfromuniver.getString("audience_number_ru")
                );
                a++;

                faculty[auditorsfromuniver.getInt("faculty_id")][auditorsfromuniver.getInt("audience_type_id")]++;
            }
            for (i = 1; i < 6; i++){
                for (int j = 1; j < 5; j++){
                    System.out.println(i + " " + j + " " + faculty[i][j] + " " + faculty2[i][j]);
                }
            }
            new Start(persons, teachers, times, auditors, faculty, faculty2);
        }
        // Handle any errors that may have occurred.
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static int getRScount(ResultSet rs) throws SQLException {
        int result = 0;
        while (rs.next()){
            result++;
        }
        return result;
    }
    public static int getCreditcount(ResultSet rs) throws SQLException {
        int result = 0;
        while (rs.next()){
            result += rs.getInt("educ_plan_pos_credit");
        }
        return result;
    }
}
