import java.sql.*;
import java.util.Scanner;

public class ApplicationManagement {

    static String url = "jdbc:mysql://localhost:3306/student_placement_system";
    static String username = "root";
    static String password = "";

    static Scanner sc = new Scanner(System.in);

    public static void viewApplications() {

        String query = """
                SELECT a.application_id,
                       s.name AS student_name,
                       c.company_name,
                       j.job_role,
                       a.application_date,
                       a.status
                FROM application a
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

            System.out.println("\n========== APPLICATION LIST ==========");

            while (rs.next()) {

                System.out.println(
                    "ID: " + rs.getInt("application_id") +
                    " | Student: " + rs.getString("student_name") +
                    " | Company: " + rs.getString("company_name") +
                    " | Role: " + rs.getString("job_role") +
                    " | Date: " + rs.getDate("application_date") +
                    " | Status: " + rs.getString("status")
                );
            }

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {

        while (true) {

            System.out.println("\n=================================");
            System.out.println(" APPLICATION MANAGEMENT");
            System.out.println("=================================");
            System.out.println("1. View Applications");
            System.out.println("2. Exit");
            System.out.print("Enter your choice: ");

            int choice = sc.nextInt();

            switch (choice) {

                case 1:
                    viewApplications();
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