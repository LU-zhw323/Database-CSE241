import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedWriter;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.text.View;




public class FinancialManager {
    // Private constructor to prevent instantiation
    private FinancialManager() {
        throw new AssertionError("Utility class cannot be instantiated");
    }

    /***
     * Financial Manager Page
     * @param conn
     * @param scanner
     */
    public static void FinancialManagerPage(Connection conn, Scanner scanner){
        int option;
        do {
            System.out.println();
            System.out.println();
            System.out.println();
            System.out.println("\n==================== EU: Finiancial Manager Page ====================");
            System.out.println("Please select one of the following options:");
            System.out.println("1. View Annual Financial Reports of All Years");
            System.out.println("2. View Annual Financial Reports of Specific Years");
            System.out.println("3. Quit");
            System.out.println("=====================================================================");
            System.out.print("Please enter your choice (1-3): ");

            while (!scanner.hasNextInt()) {
                System.out.println("That's not a valid option! Please enter a number between 1 and 3.");
                scanner.next();
                System.out.print("Please enter your choice (1-3): ");
            }

            option = scanner.nextInt();

            switch (option) {
                case 1:
                    ViewPage(conn, scanner, false);
                    break;
                case 2:
                    ViewPage(conn, scanner, true);
                    break;
                case 3:
                    System.out.println("Exiting...");
                    break;
                default:
                    System.out.println("That's not a valid option! Please enter a number between 1 and 3.");
                    break;
            }
        } while (option != 3);
    }

    /***
     * Annual Financial Reports
     * @param conn
     * @param sb
     * @param scanner
     * @param year
     */
    public static void ViewFinancialReports(Connection conn, StringBuilder sb, Scanner scanner, int year){
        sb.setLength(0);
        System.out.println();
        System.out.println();
        System.out.println();
        String sql = year != 0 ? """
                SELECT
                EXTRACT(YEAR FROM payment_date) AS year,
                EXTRACT(MONTH FROM payment_date) AS month,
                SUM(amount) as amount
            FROM
                payment
            GROUP BY
                ROLLUP(EXTRACT(YEAR FROM payment_date), EXTRACT(MONTH FROM payment_date))
            having EXTRACT(YEAR FROM payment_date) = ?
                """ : """
                    SELECT
                    EXTRACT(YEAR FROM payment_date) AS year,
                    EXTRACT(MONTH FROM payment_date) AS month,
                    SUM(amount) as amount
                FROM
                    payment
                GROUP BY
                    ROLLUP(EXTRACT(YEAR FROM payment_date), EXTRACT(MONTH FROM payment_date))
                        """;
        sb.append("Annual Financial Reports\n");
        System.out.println("Annual Financial Reports\n");
        String header = String.format("%-20s %-20s %-20s%n",
                    "YEAR", "MONTH", "AMOUNT");
        sb.append(header);
        System.out.println(header);
        System.out.println(ViewCompany.generateDashLine(3, 20, "-"));
        sb.append(ViewCompany.generateDashLine(3, 20, "-") + "\n");
        try(PreparedStatement ps = conn.prepareStatement(sql)){
            if(year != 0){
                ps.setInt(1, year);
            }
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                int year1 = rs.getInt("year");
                int month = rs.getInt("month");
                double amount = rs.getDouble("amount");
                String row = String.format("%-20s %-20s %-20.2f%n",
                        year1, month, amount);
                System.out.println(row);
                sb.append(row);
            }
        }catch(SQLException e){
            System.out.println("Error: " + e.getMessage());
        }
        System.out.println(ViewCompany.generateDashLine(3, 20, "-"));
        sb.append(ViewCompany.generateDashLine(3, 20, "-") + "\n");
    }


    /***
     * Annual Revenue Reports
     * @param conn
     * @param sb
     * @param scanner
     * @param year
     */
    public static void ViewRevenueReports(Connection conn, StringBuilder sb, Scanner scanner, int year){
        sb.setLength(0);
        System.out.println();
        System.out.println();
        System.out.println();
        String sql = year != 0 ? """
            SELECT
            EXTRACT(YEAR FROM payment_date) AS year,
            EXTRACT(MONTH FROM payment_date) AS month,
            SUM(amount) as amount
            FROM
                payment
            where
                payment.amount > 0
            GROUP BY
                ROLLUP(EXTRACT(YEAR FROM payment_date), EXTRACT(MONTH FROM payment_date))
            having EXTRACT(YEAR FROM payment_date) = ?
                """ : """
                    SELECT
                        EXTRACT(YEAR FROM payment_date) AS year,
                        EXTRACT(MONTH FROM payment_date) AS month,
                        SUM(amount) as amount
                    FROM
                        payment
                    where
                        payment.amount > 0
                    GROUP BY
                        ROLLUP(EXTRACT(YEAR FROM payment_date), EXTRACT(MONTH FROM payment_date))
                        """;
        sb.append("Annual Financial Reports\n");
        System.out.println("Annual Financial Reports\n");
        String header = String.format("%-20s %-20s %-20s%n",
                    "YEAR", "MONTH", "AMOUNT");
        sb.append(header);
        System.out.println(header);
        System.out.println(ViewCompany.generateDashLine(3, 20, "-"));
        sb.append(ViewCompany.generateDashLine(3, 20, "-") + "\n");
        try(PreparedStatement ps = conn.prepareStatement(sql)){
            if(year != 0){
                ps.setInt(1, year);
            }
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                int year1 = rs.getInt("year");
                int month = rs.getInt("month");
                double amount = rs.getDouble("amount");
                String row = String.format("%-20s %-20s %-20.2f%n",
                        year1, month, amount);
                System.out.println(row);
                sb.append(row);
            }
        }catch(SQLException e){
            System.out.println("Error: " + e.getMessage());
        }
        System.out.println(ViewCompany.generateDashLine(3, 20, "-"));
        sb.append(ViewCompany.generateDashLine(3, 20, "-") + "\n"); 
    }



    /***
     * Annual Expense Reports
     * @param conn
     * @param sb
     * @param scanner
     * @param year
     */
    public static void ViewExpenseReports(Connection conn, StringBuilder sb, Scanner scanner, int year){
        sb.setLength(0);
        System.out.println();
        System.out.println();
        System.out.println();
        String sql = year != 0 ? """
            SELECT
            EXTRACT(YEAR FROM payment_date) AS year,
            EXTRACT(MONTH FROM payment_date) AS month,
            SUM(amount) as amount
            FROM
                payment
            where
                payment.amount < 0
            GROUP BY
                ROLLUP(EXTRACT(YEAR FROM payment_date), EXTRACT(MONTH FROM payment_date))
            having EXTRACT(YEAR FROM payment_date) = ?
                """ : """
                    SELECT
                        EXTRACT(YEAR FROM payment_date) AS year,
                        EXTRACT(MONTH FROM payment_date) AS month,
                        SUM(amount) as amount
                    FROM
                        payment
                    where
                        payment.amount < 0
                    GROUP BY
                        ROLLUP(EXTRACT(YEAR FROM payment_date), EXTRACT(MONTH FROM payment_date))
                        """;
        sb.append("Annual Financial Reports\n");
        System.out.println("Annual Financial Reports\n");
        String header = String.format("%-20s %-20s %-20s%n",
                    "YEAR", "MONTH", "AMOUNT");
        sb.append(header);
        System.out.println(header);
        System.out.println(ViewCompany.generateDashLine(3, 20, "-"));
        sb.append(ViewCompany.generateDashLine(3, 20, "-") + "\n");
        try(PreparedStatement ps = conn.prepareStatement(sql)){
            if(year != 0){
                ps.setInt(1, year);
            }
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                int year1 = rs.getInt("year");
                int month = rs.getInt("month");
                double amount = rs.getDouble("amount");
                String row = String.format("%-20s %-20s %-20.2f%n",
                        year1, month, amount);
                System.out.println(row);
                sb.append(row);
            }
        }catch(SQLException e){
            System.out.println("Error: " + e.getMessage());
        }
        System.out.println(ViewCompany.generateDashLine(3, 20, "-"));
        sb.append(ViewCompany.generateDashLine(3, 20, "-") + "\n"); 
    }



    /***
     * View Page
     * @param conn
     * @param scanner
     * @param isYear
     */
    public static void ViewPage(Connection conn, Scanner scanner, boolean isYear){
        ArrayList<Integer> years = new ArrayList<>();
        int year = 0;
        StringBuilder annual = new StringBuilder();
        StringBuilder revenue = new StringBuilder();
        StringBuilder expense = new StringBuilder();
        years = ValidYear(conn);
        if(years.size() == 0){
            System.out.println("We don't have any payment records yet.");
            return;
        }
        if(isYear){
            System.out.println("\n==================== EU: Finiancial Manager Page ====================");
            System.out.println("Please select a year from the following list:");
            for(int i = 0; i < years.size(); i++){
                System.out.println("* " + years.get(i));
            }
            while(true){
                String temp = Customer.InfoInput(scanner, "Please Enter a Year", "year");
                year = Integer.parseInt(temp);
                if(years.contains(year)){
                    break;
                }
                System.out.println("Please enter a valid year within the above list.");
            }
            System.out.println("=====================================================================");
        }
        ViewFinancialReports(conn, annual, scanner, year);
        System.out.println("\n==================== EU: Finiancial Manager Page ====================");
        scanner.nextLine();
        String revenue_option = Customer.InfoInput(scanner, "Do you want to view revenue and expense reports? (Yes/No):", "confirm");
        System.out.println("=====================================================================");
        if(revenue_option.equalsIgnoreCase("Yes")){
            ViewRevenueReports(conn, revenue, scanner, year);
            ViewExpenseReports(conn, expense, scanner, year);
        }
        System.out.println("\n==================== EU: Finiancial Manager Page ====================");
        String putput_option = Customer.InfoInput(scanner, "Do you want to export all above reports? (Yes/No):", "confirm");
        if(putput_option.equalsIgnoreCase("Yes")){
            annual.append(revenue).append(expense);
            OutputReports(annual);
        }
        System.out.println("=====================================================================");
    }

    /***
     * Check which year is valid
     * @param conn
     * @return a list of valid years
     */
    public static ArrayList<Integer> ValidYear(Connection conn){
        ArrayList<Integer> years = new ArrayList<>();
        String sql = """
            SELECT distinct EXTRACT(YEAR FROM payment_date) AS year from payment
                """;
        try(PreparedStatement ps = conn.prepareStatement(sql)){
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                int year = rs.getInt("year");
                years.add(year);
            }
        }catch(SQLException e){
            System.out.println("Error: " + e.getMessage());
        }
        return years;
    }


    /***
     * Utility method to export summary of properties and apartments to a text file
     * @param conn
     */
    public static void OutputReports(StringBuilder sb){
        // Specify the file path
        String filePath = "FinancialReports.txt";

        // Use try-with-resources for automatic resource management
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            // Write the contents of StringBuilder to the file
            writer.write(sb.toString());
        } catch (IOException e) {
            System.out.println("IOException: " + e);
        }
        System.out.println("The report has been exported to " + filePath);
    }
}

