import geneticrasp.GeneticPerson;
import geneticrasp.GeneticRooms;
import geneticrasp.Start;
import org.ini4j.Wini;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Properties;

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
                    "  FROM [atu_univer].[dbo].[univer_schedule_time] where status = 1 and schedule_time_type_id = 1 and schedule_time_id < 7" +
                    "  order by schedule_time_begin";
            String SQL1 = "SELECT g.[group_id]" +
                    "      ,[educ_type_id]" +
                    "      ,[teacher_id]" +
                    "  ,eps.subject_id" +
                    "  ,eps.faculty_id" +
                    "  ,count(gs.student_id) as studcount" +
                    "  FROM [atu_univer].[dbo].[univer_group] g " +
                    "  join univer_educ_plan_pos eps on eps.educ_plan_pos_id = g.educ_plan_pos_id" +
                    "  join univer_group_student gs on gs.group_id = g.group_id" +
                    "  group by g.[group_id]" +
                    "      ,[educ_type_id]" +
                    "      ,[teacher_id]" +
                    "  ,eps.subject_id" +
                    "  ,eps.faculty_id" +
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
            count = getRScount(groups1);
            int[] teachers = new int[count];
            GeneticPerson[] persons = new GeneticPerson[count];
            ResultSet groups = stmt.executeQuery(SQL1);
            int n = 0;
            while (groups.next()) {
                persons[n] = new GeneticPerson(
                        groups.getInt("group_id"),
                        groups.getInt("subject_id"),
                        groups.getInt("educ_type_id"),
                        groups.getInt("teacher_id"),
                        groups.getInt("faculty_id"),
                        groups.getInt("studcount")
                );
                teachers[n] = groups.getInt("teacher_id");
                n++;
            }

            ResultSet rs1 = stmt.executeQuery(SQL2);
            count = getRScount(rs1);
            i = 0;
            GeneticRooms[] auditors = new GeneticRooms[count];
            ResultSet auditorsfromuniver = stmt.executeQuery(SQL2);
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
            }
            new Start(persons, teachers, times, auditors);
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
}
