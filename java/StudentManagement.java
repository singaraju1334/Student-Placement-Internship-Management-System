import java.sql.*;
import java.util.Scanner;

public class StudentManagement {

    static String url = "jdbc:mysql://localhost:3306/student_placement_system";
    static String username = "root";
    static String password = "";

    static Scanner sc = new Scanner(System.in);

    public static void viewStudents() {

        String query = """
                SELECT s.student_id, s.roll_no, s.name,
                       d.department_name, s.cgpa
                FROM student s
                JOIN department d
                ON s.department_id = d.department_id
                """;

        try (Connection con = DriverManager.getConnection(url, username, password);
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            System.out.println("\n========== STUDENT LIST ==========");

            while (rs.next()) {
                System.out.println(
                    "ID: " + rs.getInt("student_id") +
                    " | Roll No: " + rs.getString("roll_no") +
                    " | Name: " + rs.getString("name") +
                    " | Department: " + rs.getString("department_name") +
                    " | CGPA: " + rs.getDouble("cgpa")
                );
            }

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void searchStudent() {

        System.out.print("\nEnter student name: ");
        String name = sc.next();

        String query = """
                SELECT s.student_id, s.roll_no, s.name,
                       d.department_name, s.cgpa
                FROM student s
                JOIN department d
                ON s.department_id = d.department_id
                WHERE s.name LIKE ?
                """;

        try (Connection con = DriverManager.getConnection(url, username, password);
             PreparedStatement pstmt = con.prepareStatement(query)) {

            pstmt.setString(1, "%" + name + "%");

            ResultSet rs = pstmt.executeQuery();

            boolean found = false;

            System.out.println("\n========== SEARCH RESULT ==========");

            while (rs.next()) {

                found = true;

                System.out.println(
                    "ID: " + rs.getInt("student_id") +
                    " | Roll No: " + rs.getString("roll_no") +
                    " | Name: " + rs.getString("name") +
                    " | Department: " + rs.getString("department_name") +
                    " | CGPA: " + rs.getDouble("cgpa")
                );
            }

            if (!found) {
                System.out.println("No student found.");
            }

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {

        while (true) {

            System.out.println("\n=================================");
            System.out.println(" STUDENT PLACEMENT MANAGEMENT");
            System.out.println("=================================");
            System.out.println("1. View Students");
            System.out.println("2. Search Student");
            System.out.println("3. Exit");
            System.out.print("Enter your choice: ");

            int choice = sc.nextInt();

            switch (choice) {

                case 1:
                    viewStudents();
                    break;

                case 2:
                    searchStudent();
                    break;

                case 3:
                    System.out.println("Exiting application...");
                    return;

                default:
                    System.out.println("Invalid choice!");
            }
        }
    }
}