import java.sql.*;
import java.util.Scanner;

public class InternshipManagement {

    static String url = "jdbc:mysql://localhost:3306/student_placement_system";
    static String username = "root";
    static String password = "";

    static Scanner sc = new Scanner(System.in);

    public static void viewInternships() {

        String query = """
                SELECT i.internship_id,
                       s.name AS student_name,
                       c.company_name,
                       i.internship_role,
                       i.start_date,
                       i.end_date,
                       i.stipend,
                       i.status
                FROM internship i
                JOIN student s
                    ON i.student_id = s.student_id
                JOIN company c
                    ON i.company_id = c.company_id
                """;

        try (Connection con = DriverManager.getConnection(url, username, password);
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            System.out.println("\n========== INTERNSHIP LIST ==========");

            while (rs.next()) {

                System.out.println(
                    "ID: " + rs.getInt("internship_id") +
                    " | Student: " + rs.getString("student_name") +
                    " | Company: " + rs.getString("company_name") +
                    " | Role: " + rs.getString("internship_role") +
                    " | Start: " + rs.getDate("start_date") +
                    " | End: " + rs.getDate("end_date") +
                    " | Stipend: ₹" + rs.getDouble("stipend") +
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
            System.out.println(" INTERNSHIP MANAGEMENT");
            System.out.println("=================================");
            System.out.println("1. View Internships");
            System.out.println("2. Exit");
            System.out.print("Enter your choice: ");

            int choice = sc.nextInt();

            switch (choice) {

                case 1:
                    viewInternships();
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