import java.sql.*;
import java.util.Scanner;

public class AdminLogin {

    static String url = "jdbc:mysql://localhost:3306/student_placement_system";
    static String username = "root";
    static String password = "";

    static Scanner sc = new Scanner(System.in);

    public static boolean login(String adminUsername, String adminPassword) {

        String query = """
                SELECT name
                FROM admin
                WHERE username = ?
                AND password = ?
                """;

        try (Connection con = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, adminUsername);
            ps.setString(2, adminPassword);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                System.out.println("\n=================================");
                System.out.println(" LOGIN SUCCESSFUL");
                System.out.println(" Welcome, " + rs.getString("name"));
                System.out.println("=================================");
                return true;
            } else {
                System.out.println("\nInvalid username or password!");
                return false;
            }

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        }
    }

    public static void main(String[] args) {

        System.out.println("\n=================================");
        System.out.println(" ADMIN LOGIN");
        System.out.println(" STUDENT PLACEMENT SYSTEM");
        System.out.println("=================================");

        System.out.print("Enter username: ");
        String adminUsername = sc.nextLine();

        System.out.print("Enter password: ");
        String adminPassword = sc.nextLine();

        login(adminUsername, adminPassword);
    }
}