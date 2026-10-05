import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseConnection {

    public static void main(String[] args) {

        String url = "jdbc:mysql://localhost:3306/student_placement_system";
        String username = "root";
        String password = "";

        try {
            Connection connection = DriverManager.getConnection(
                url,
                username,
                password
            );

            System.out.println("=================================");
            System.out.println("JAVA JDBC CONNECTION SUCCESSFUL");
            System.out.println("Student Placement System");
            System.out.println("=================================");

            connection.close();

        } catch (Exception e) {
            System.out.println("Database connection failed!");
            e.printStackTrace();
        }
    }
}