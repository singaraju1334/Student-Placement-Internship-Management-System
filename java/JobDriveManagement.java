import java.sql.*;
import java.util.Scanner;

public class JobDriveManagement {

    static String url = "jdbc:mysql://localhost:3306/student_placement_system";
    static String username = "root";
    static String password = "";

    static Scanner sc = new Scanner(System.in);

    public static void viewJobDrives() {

        String query = """
                SELECT j.drive_id,
                       c.company_name,
                       j.job_role,
                       j.job_type,
                       j.minimum_cgpa,
                       j.package_lpa,
                       j.drive_date,
                       j.application_deadline,
                       j.vacancies
                FROM job_drive j
                JOIN company c
                ON j.company_id = c.company_id
                """;

        try (Connection con = DriverManager.getConnection(url, username, password);
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            System.out.println("\n========== JOB DRIVE LIST ==========");

            while (rs.next()) {

                System.out.println(
                    "ID: " + rs.getInt("drive_id") +
                    " | Company: " + rs.getString("company_name") +
                    " | Role: " + rs.getString("job_role") +
                    " | Type: " + rs.getString("job_type") +
                    " | Min CGPA: " + rs.getDouble("minimum_cgpa") +
                    " | Package: " + rs.getDouble("package_lpa") + " LPA" +
                    " | Drive Date: " + rs.getDate("drive_date") +
                    " | Deadline: " + rs.getDate("application_deadline") +
                    " | Vacancies: " + rs.getInt("vacancies")
                );
            }

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {

        while (true) {

            System.out.println("\n=================================");
            System.out.println(" JOB DRIVE MANAGEMENT");
            System.out.println("=================================");
            System.out.println("1. View Job Drives");
            System.out.println("2. Exit");
            System.out.print("Enter your choice: ");

            int choice = sc.nextInt();

            switch (choice) {

                case 1:
                    viewJobDrives();
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