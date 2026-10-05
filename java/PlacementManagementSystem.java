import java.util.Scanner;

public class PlacementManagementSystem {

    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {

        System.out.println("\n========================================");
        System.out.println(" STUDENT PLACEMENT MANAGEMENT SYSTEM");
        System.out.println("========================================");

        System.out.println("\n----------- ADMIN LOGIN -----------");

        System.out.print("Enter username: ");
        String username = sc.nextLine();

        System.out.print("Enter password: ");
        String password = sc.nextLine();

        if (!AdminLogin.login(username, password)) {
            System.out.println("\nAccess denied!");
            System.out.println("Exiting system...");
            return;
        }

        int choice;

        do {
            System.out.println("\n========================================");
            System.out.println(" STUDENT PLACEMENT MANAGEMENT SYSTEM");
            System.out.println("========================================");
            System.out.println("1. Student Management");
            System.out.println("2. Company Management");
            System.out.println("3. Job Drive Management");
            System.out.println("4. Application Management");
            System.out.println("5. Interview Round Management");
            System.out.println("6. Placement Results");
            System.out.println("7. Internship Management");
            System.out.println("8. Exit");
            System.out.println("========================================");

            System.out.print("Enter your choice: ");
            choice = sc.nextInt();

            switch (choice) {

                case 1:
                    StudentManagement.viewStudents();
                    break;

                case 2:
                    CompanyManagement.viewCompanies();
                    break;

                case 3:
                    JobDriveManagement.viewJobDrives();
                    break;

                case 4:
                    ApplicationManagement.viewApplications();
                    break;

                case 5:
                    InterviewRoundManagement.viewInterviewRounds();
                    break;

                case 6:
                    PlacementResultManagement.viewPlacementResults();
                    break;

                case 7:
                    InternshipManagement.viewInternships();
                    break;

                case 8:
                    System.out.println("\nThank you for using");
                    System.out.println("Student Placement Management System!");
                    break;

                default:
                    System.out.println("\nInvalid choice!");
            }

        } while (choice != 8);
    }
}