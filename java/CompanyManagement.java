import java.sql.*;
import java.util.Scanner;

public class CompanyManagement {

    static String url = "jdbc:mysql://localhost:3306/student_placement_system";
    static String username = "root";
    static String password = "";

    static Scanner sc = new Scanner(System.in);

    public static void viewCompanies() {

        String query = """
                SELECT company_id, company_name, industry,
                       location, contact_email
                FROM company
                """;

        try (Connection con = DriverManager.getConnection(url, username, password);
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            System.out.println("\n========== COMPANY LIST ==========");

            while (rs.next()) {

                System.out.println(
                    "ID: " + rs.getInt("company_id") +
                    " | Company: " + rs.getString("company_name") +
                    " | Industry: " + rs.getString("industry") +
                    " | Location: " + rs.getString("location") +
                    " | Email: " + rs.getString("contact_email")
                );
            }

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {

        while (true) {

            System.out.println("\n=================================");
            System.out.println(" COMPANY MANAGEMENT");
            System.out.println("=================================");
            System.out.println("1. View Companies");
            System.out.println("2. Exit");
            System.out.print("Enter your choice: ");

            int choice = sc.nextInt();

            switch (choice) {

                case 1:
                    viewCompanies();
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