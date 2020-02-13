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
            String SQL1 = " SELECT id, group_id, educ_type_id, teacher_id, subject_id, hours_educ, students_count, " +
                    "audience_id, day_of_week_id, time_id, faculty_id, chair_id, status, students_arr " +
                    " FROM new_group where deleted_date is null";
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
            count = getRScount1(groups1);
            int[] teachers = new int[count];
            GeneticPerson[] persons = new GeneticPerson[count];
            ResultSet groups = stmt.executeQuery(SQL1);
            int n = 0, n_t = 0;
            int[][] faculty2 = new int[6][5];
            int[][] chair2 = new int[26][5];
            while (groups.next()) {
                Array buf1 = groups.getArray("group_id");
                Integer[] group_id = (Integer[])buf1.getArray();
                buf1 = groups.getArray("subject_id");
                Integer[] subject_id = (Integer[])buf1.getArray();
                buf1 = groups.getArray("teacher_id");
                Integer[] teacher_id = (Integer[])buf1.getArray();
                buf1 = groups.getArray("students_arr");
                Integer[] students_arr = (Integer[])buf1.getArray();
                buf1 = groups.getArray("time_id");
                Integer[] bind_times = (Integer[])buf1.getArray();
                int status = (groups.getInt("status") == -1) ? 7 : 1;
                if (groups.getInt("audience_id") != 0) {
                    status *= 2;
                }
                if (groups.getInt("day_of_week_id") != 0) {
                    status *= 3;
                }
                if (bind_times.length > 0) {
                    status *= 5;
                } else {
                    bind_times = new Integer[]{0};
                }
                for(int time_id : bind_times) {
                    persons[n] = new GeneticPerson(
                            group_id,
                            subject_id,
                            teacher_id,
                            groups.getInt("educ_type_id"),
                            groups.getInt("faculty_id"),
                            groups.getInt("chair_id"),
                            groups.getInt("students_count"),
                            groups.getInt("hours_educ"),
                            groups.getInt("audience_id"),
                            time_id,
                            groups.getInt("day_of_week_id"),
                            status,
                            students_arr
                    );
                    for (int teach_id : teacher_id) {
                        if (Arrays.asList(teachers).contains(teach_id)) {
                            teachers[n_t] = groups.getInt("teacher_id");
                            n_t++;
                        }
                    }
                    n++;
                }
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
                chair2[groups.getInt("chair_id")][id]++;
            }
            ResultSet rs1 = stmt.executeQuery(SQL2);
            count = getRScount(rs1);
            i = 0;
            GeneticRooms[] auditors = new GeneticRooms[count];
            ResultSet auditorsfromuniver = stmt.executeQuery(SQL2);
            int[][] faculty = new int[6][5];
            int[][] chair = new int[26][5];
            int a = 0;
            while (auditorsfromuniver.next()) {
                auditors[a] = new GeneticRooms(
                        auditorsfromuniver.getInt("audience_id"),
                        auditorsfromuniver.getInt("faculty_id"),
                        auditorsfromuniver.getInt("chair_id"),
                        auditorsfromuniver.getInt("building_id"),
                        auditorsfromuniver.getInt("audience_type_id"),
                        auditorsfromuniver.getInt("audience_floor"),
                        auditorsfromuniver.getInt("audience_size"),
                        auditorsfromuniver.getString("audience_number_ru")
                );
                a++;
                faculty[auditorsfromuniver.getInt("faculty_id")][auditorsfromuniver.getInt("audience_type_id")]++;
                chair[auditorsfromuniver.getInt("chair_id")][auditorsfromuniver.getInt("audience_type_id")]++;
            }
            new Start(persons, teachers, times, auditors, faculty, faculty2, chair, chair2);
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
    public static int getRScount1(ResultSet rs) throws SQLException {
        int result = 0;
        while (rs.next()){
            Array buf1 = rs.getArray("time_id");
            Integer[] bind_times = (Integer[])buf1.getArray();
            if (bind_times.length == 0){
                bind_times = new Integer[]{0};
            }
            result += bind_times.length;
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
