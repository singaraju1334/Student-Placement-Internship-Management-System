import java.sql.*;
import java.util.Scanner;

public class PlacementResultManagement {

    static String url = "jdbc:mysql://localhost:3306/student_placement_system";
    static String username = "root";
    static String password = "";

    static Scanner sc = new Scanner(System.in);

    public static void viewPlacementResults() {

        String query = """
                SELECT pr.result_id,
                       s.name AS student_name,
                       c.company_name,
                       j.job_role,
                       pr.final_status,
                       pr.joining_date,
                       pr.offered_package
                FROM placement_result pr
                JOIN application a
                    ON pr.application_id = a.application_id
                JOIN student s
                    ON a.student_id = s.student_id
                JOIN job_drive j
                    ON a.drive_id = j.drive_id
                JOIN company c
                    ON j.company_id = c.company_id
                """;

        try (Connection con = DriverManager.getConnection(url, username, password);
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            System.out.println("\n========== PLACEMENT RESULT LIST ==========");

            while (rs.next()) {

                System.out.println(
                    "ID: " + rs.getInt("result_id") +
                    " | Student: " + rs.getString("student_name") +
                    " | Company: " + rs.getString("company_name") +
                    " | Role: " + rs.getString("job_role") +
                    " | Status: " + rs.getString("final_status") +
                    " | Joining Date: " + rs.getDate("joining_date") +
                    " | Package: " + rs.getDouble("offered_package") + " LPA"
                );
            }

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {

        while (true) {

            System.out.println("\n=================================");
            System.out.println(" PLACEMENT RESULT MANAGEMENT");
            System.out.println("=================================");
            System.out.println("1. View Placement Results");
            System.out.println("2. Exit");
            System.out.print("Enter your choice: ");

            int choice = sc.nextInt();

            switch (choice) {

                case 1:
                    viewPlacementResults();
                    break;

                case 2:
                    System.out.println("Exiting application...");
                    return;

                default:
                    System.out.println("Invalid choice!");
            }
        }
    }
}