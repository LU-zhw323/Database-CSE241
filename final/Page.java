import java.sql.*;
import java.util.Scanner;
import java.util.Arrays;

public class Page {

    public static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        String userid = "";
        String pass = "";
        System.out.println("\n==================== Welcome to EU Interface ====================");
        while (true) {
            System.out.print("enter Oracle user id: ");
            userid = scanner.nextLine();
            if (!userid.isEmpty()) {
                break;
            }
        }
        while (true) {
            System.out.print("enter Oracle  password for " + userid + ": ");
            pass = scanner.nextLine();
            if (!pass.isEmpty()) {
                break;
            }
        }

        JDBC_Connection(userid, pass);
        System.out.println("==================================END================================\n");

    }

    public static void MainPage(Connection conn) {
        Scanner scanner = new Scanner(System.in);
        int option;

        do {
            System.out.println();
            System.out.println();
            System.out.println();
            System.out.println("\n==================== EU Command Line Interface ====================");
            System.out.println("Please select one of the following Login options:");
            System.out.println("1. Tenant");
            System.out.println("2. New Customer or Prosepctive Tenant");
            System.out.println("3. Property Manager");
            System.out.println("4. Company Manager");
            System.out.println("5. Financial Manager");
            System.out.println("6. Quit");
            System.out.println("=====================================================================");
            System.out.print("Please enter your choice (1-6): ");

            while (!scanner.hasNextInt()) {
                System.out.println("That's not a valid option! Please enter a number between 1 and 6.");
                scanner.next();
                System.out.print("Please enter your choice (1-6): ");
            }

            option = scanner.nextInt();

            switch (option) {
                case 1:
                    System.out.println("Logging in as Tenant...");
                    Customer.CustomerPage(conn, scanner, false);
                    break;
                case 2:
                    System.out.println("Logging as New Customer or Prosepctive Tenant...");
                    Customer.CustomerPage(conn, scanner, true);
                    break;
                case 3:
                    // Call the login function for Company Manager
                    System.out.println("Logging in as Property Manager...");
                    PropertyManager.PropertyManagerPage(conn, scanner);
                    // loginAsCompanyManager();
                    break;
                case 4:
                    // Call the login function for Financial Manager
                    System.out.println("Logging in as Company Manager...");
                    CompanyManager.CompanyManagerPage(conn, scanner);
                    // loginAsFinancialManager();
                    break;
                case 5:
                    System.out.println("Logging in as Financial Manager...");
                    FinancialManager.FinancialManagerPage(conn, scanner);
                    // Call the registration function for New Customer
                    break;
                case 6:
                    System.out.println("Quitting the application... Have a good day!");
                    break;
                default:
                    System.out.println("Invalid option. Please enter a number between 1 and 6.");
                    break;
            }
        } while (option != 6);

        scanner.close();
    }

    /***
     * This method is used to connect to the database
     * 
     * @param userid
     * @param pass
     * @throws ClassNotFoundException
     */
    public static boolean JDBC_Connection(String userid, String pass) {
        if (userid.isEmpty() || pass.isEmpty()) {
            return false;
        }

        try (Connection conn = DriverManager.getConnection(
                "jdbc:oracle:thin:@edgar1.cse.lehigh.edu:1521:cse241",
                userid,
                pass);
                Statement stmt = conn.createStatement();) {
            System.out.println("=====================================================================\n");
            MainPage(conn);
            stmt.close();
            conn.close();
            return true;
        } catch (SQLException e) {
            System.out.println("SQLException: " + e);
            return false;
        }
    }

}
