import java.sql.*;
import java.util.ArrayList;

public class app {
    public static void main(String[] args) {
        // Create a variable for the connection string.
        String connectionUrl = "jdbc:sqlserver://univer.atu.kz;databaseName=atu_univer;user=joker;password=Univer2019#@";
        try (Connection con = DriverManager.getConnection(connectionUrl); Statement stmt = con.createStatement();) {
            String SQL = "SELECT [schedule_time_id]" +
                    "      ,[schedule_time_begin]" +
                    "      ,[schedule_time_end]" +
                    "  FROM [atu_univer].[dbo].[univer_schedule_time] where status = 1 and schedule_time_type_id = 1 " +
                    "  order by schedule_time_begin";
            ResultSet rs = stmt.executeQuery(SQL);
            ArrayList maintimes = new ArrayList();
            // Iterate through the data in the result set and display it.
            while (rs.next()) {
                maintimes.add(rs.getString("schedule_time_begin"));
                //System.out.println(rs.getString("schedule_time_id") + " " + rs.getString("schedule_time_begin"));
            }


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
            rs = stmt.executeQuery(SQL1);
            ArrayList maingroups = new ArrayList();
            // Iterate through the data in the result set and display it.
            while (rs.next()) {
                maingroups.add(rs.getString("group_id"));
                System.out.println(rs.getString("group_id")
                        + " " + rs.getString("educ_type_id")
                        + " " + rs.getString("teacher_id")
                        + " " + rs.getString("subject_id")
                        + " " + rs.getString("faculty_id")
                        + " " + rs.getString("studcount"));
            }
        }
        // Handle any errors that may have occurred.
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
