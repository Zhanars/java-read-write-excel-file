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
        String connectionUrl = "jdbc:postgresql://" + server + "/" + dbname ;
        try (Connection con = DriverManager.getConnection(connectionUrl,username,password); Statement stmt = con.createStatement();) {
            String SQL = "SELECT schedule_time_id, schedule_time_begin, schedule_time_end" +
                    " FROM schedule_time where  schedule_time_id < 12" +
                    "  order by schedule_time_begin";
            String SQL1 = " SELECT id, group_id, educ_type_id, teacher_id, subject_id, hours_educ, students_count, audience_id, day_of_week_id, time_id, faculty_id, chair_id" +
                    " FROM new_group";
            String SQL2 = "SELECT audience_id, faculty_id, building_id, chair_id, audience_type_id, audience_floor, audience_size, audience_number_ru" +
                    " FROM audience  where status = 1 order by faculty_id";
            
            
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
            int[][] faculty2 = new int[6][5];
            while (groups.next()) {
                persons[n] = new GeneticPerson(
                        groups.getInt("group_id"),
                        groups.getInt("subject_id"),
                        groups.getInt("educ_type_id"),
                        groups.getInt("teacher_id"),
                        groups.getInt("faculty_id"),
                        groups.getInt("studcount"),
                        groups.getInt("educ_plan_pos_credit")
                );
                teachers[n] = groups.getInt("teacher_id");
                n++;
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
