import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedWriter;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.Scanner;

import javax.swing.text.View;


public class PropertyManager {
    // Private constructor to prevent instantiation
    private PropertyManager() {
        throw new AssertionError("Utility class cannot be instantiated");
    }

    /***
     * Main page for the property manager
     * @param conn
     * @param scanner
     */
    public static void PropertyManagerPage(Connection conn, Scanner scanner){
        int option;
        do {
            System.out.println();
            System.out.println();
            System.out.println();
            System.out.println("\n====================== EU: Property Manager Page =========================");
            System.out.println("1. View Visit Data");
            System.out.println("2. View Lease Data");
            System.out.println("3. Add New Tenants or Pets");
            System.out.println("4. Move Out Tenants");
            System.out.println("5. Logout");
            System.out.println("=====================================================================");
            System.out.print("Please enter your choice (1-5): ");

            while (!scanner.hasNextInt()) {
                System.out.println("That's not a valid option! Please enter a number between 1 and 5.");
                scanner.next();
                System.out.print("Please enter your choice (1-5): ");
            }

            option = scanner.nextInt();

            switch (option) {
                case 1:
                    ViewVisit(conn, scanner);
                    break;
                case 2:
                    ViewLease(conn, scanner);
                    break;
                case 3:
                    scanner.nextLine();
                    String lease_id = "";
                    while(true){
                        lease_id = Customer.InfoInput(scanner, "Please Enter a Lease ID(or 'q' to quit)", "id");
                        String sql = "SELECT * FROM Lease WHERE lease_id = ?";
                        try(PreparedStatement ps = conn.prepareStatement(sql)){
                            ps.setString(1, lease_id);
                            ResultSet rs = ps.executeQuery();
                            if(!rs.next()){
                                System.out.println("Lease ID does not exist!");
                                continue;
                            }
                        }catch(SQLException e){
                            System.out.println(e.getMessage());
                        }
                        break;
                    }
                    Tenant.AddRoommate(conn, scanner, Integer.parseInt(lease_id));
                    break;
                case 4:
                    Moveout(conn, scanner);
                    break;
                case 5:
                    System.out.println("Logging out...");
                    break;
                default:
                    System.out.println("That's not a valid option! Please enter a number between 1 and 5.");
                    break;
            }
        } while (option != 5);
    }


    /***
     * Visit data
     * @param conn
     * @param scanner
     */
    public static void ViewVisit(Connection conn, Scanner scanner){
        ViewCompany.ViewSummary(conn, new StringBuilder(), null, null, null,true);
        scanner.nextLine();
        System.out.println("\n====================== EU: View Visit Page =========================");
        System.out.println("Do you want to check visit data for a specific apartment?");
        System.out.println("Yes: It will output the visit data for a specific apartment");
        System.out.println("No: It will output the visit data for all apartments");
        String option = Customer.InfoInput(scanner, "Enter Yes/No: ", "confirm");
        if(option.equalsIgnoreCase("Yes")){
            String propID = "";
            String aptID = "";
            while(true){
                propID = ViewCompany.IDInput("Enter a Property ID", scanner);
                try{
                    PreparedStatement pstmt = conn.prepareStatement("select * from property where prop_id = ?");
                    pstmt.setString(1, propID);
                    if(ViewCompany.JDBC_Check(pstmt) == 0){
                        System.out.println("Property ID Not Found! Please Try Again.");
                        continue;
                    }
                    pstmt.close();
                }
                catch(SQLException e){
                    System.out.println("SQLException: " + e);
                }
                aptID = ViewCompany.IDInput("Enter a Apartment ID", scanner);
                try{
                    PreparedStatement pstmt = conn.prepareStatement("select * from apartment where prop_id = ? and apt_id = ?");
                    pstmt.setString(1, propID);
                    pstmt.setString(2, aptID);
                    if(ViewCompany.JDBC_Check(pstmt) == 0){
                        System.out.println("Apartment ID Not Found! Please Try Again.");
                        continue;
                    }
                    pstmt.close();
                }
                catch(SQLException e){
                    System.out.println("SQLException: " + e);
                }
                break;
            }
            System.out.println();
            System.out.println("Visit Data for Property ID: " + propID + " Apartment ID: " + aptID);
            System.out.println(ViewCompany.generateDashLine(4,20,"-"));
            String header = String.format("%-20s %-20s %-20s %-20s %n",
            "PROPERTY_ID", "APARTMENT_ID", "TENANT_ID", "TENANT_NAME");
            System.out.println(header);
            System.out.println(ViewCompany.generateDashLine(4,20,"-"));
            String sql = """
                    select prop_id, apt_id, id, name from visits join prospect_tenant on tenent_id = id where prop_id = ? and apt_id = ?
                    order by prop_id, apt_id
                    """;
            try(PreparedStatement pstmt = conn.prepareStatement(sql)){
                pstmt.setString(1, propID);
                pstmt.setString(2, aptID);
                ResultSet rs = pstmt.executeQuery();
                while(rs.next()){
                    String prop_id = rs.getString("prop_id");
                    String apt_id = rs.getString("apt_id");
                    String id = rs.getString("id");
                    String name = rs.getString("name");
                    String row = String.format("%-20s %-20s %-20s %-20s %n",
                    prop_id, apt_id, id, name);
                    System.out.println(row);
                }
            }catch(SQLException e){
                System.out.println(e.getMessage());
            }
            System.out.println(ViewCompany.generateDashLine(4,20,"-"));
        }
        else{
            System.out.println();
            System.out.println("Visit Data for All Apartments");
            System.out.println(ViewCompany.generateDashLine(4,20,"-"));
            String header = String.format("%-20s %-20s %-20s %-20s %n",
            "PROPERTY_ID", "APARTMENT_ID", "TENANT_ID", "TENANT_NAME");
            System.out.println(header);
            System.out.println(ViewCompany.generateDashLine(4,20,"-"));
            String sql = """
                    select prop_id, apt_id, id, name from visits join prospect_tenant on tenent_id = id order by prop_id, apt_id
                    """;
            try(PreparedStatement pstmt = conn.prepareStatement(sql)){
                ResultSet rs = pstmt.executeQuery();
                while(rs.next()){
                    String prop_id = rs.getString("prop_id");
                    String apt_id = rs.getString("apt_id");
                    String id = rs.getString("id");
                    String name = rs.getString("name");
                    String row = String.format("%-20s %-20s %-20s %-20s %n",
                    prop_id, apt_id, id, name);
                    System.out.println(row);
                }
            }catch(SQLException e){
                System.out.println(e.getMessage());
            }
            System.out.println(ViewCompany.generateDashLine(4,20,"-"));
        }
    }


    /**
     * View lease data
     * @param conn
     * @param scanner
     */
    public static void ViewLease(Connection conn, Scanner scanner){
        ViewCompany.ViewSummary(conn, new StringBuilder(), null, null, null,true);
        scanner.nextLine();
        System.out.println("\n====================== EU: View Lease Page =========================");
        System.out.println("Do you want to check detail data for a specific lease?(Property, Apartment, Lease, Amenity, Cost)");
        String option = Customer.InfoInput(scanner, "Enter Yes/No: ", "confirm");
        String lease_id = "";
        if(option.equalsIgnoreCase("No")){
            System.out.println("=====================================================================");
            return;
        }
        while(true){
            lease_id = Customer.InfoInput(scanner, "Please Enter a Lease ID(or 'q' to quit)", "id");
            if (lease_id.equalsIgnoreCase("q")){
                System.out.println("=====================================================================");
                return;
            }
            String sql = "SELECT * FROM Lease WHERE lease_id = ?";
            try(PreparedStatement ps = conn.prepareStatement(sql)){
                ps.setString(1, lease_id);
                ResultSet rs = ps.executeQuery();
                if(!rs.next()){
                    System.out.println("Lease ID does not exist!");
                    continue;
                }
            }catch(SQLException e){
                System.out.println(e.getMessage());
            }
            break;
        }
        ViewCompany.ViewSummary(conn, new StringBuilder(), null, null, lease_id,true);
        Tenant.ViewSelectedAmenity(conn, scanner, Integer.parseInt(lease_id));
        ViewLeaseDetial(conn, scanner, Integer.parseInt(lease_id));
        ViewTenant(conn, scanner, Integer.parseInt(lease_id));
        ViewPet(conn, scanner, Integer.parseInt(lease_id));
        System.out.println("TOTAL MONTHLY COST: " + Tenant.ViewBill(conn, scanner, Integer.parseInt(lease_id)) + "\n");
        System.out.println("=====================================================================");
    }

    /***
     * View Lease detail by lease id
     * @param conn
     * @param scanner
     * @param lease_id
     */
    public static void ViewLeaseDetial(Connection conn, Scanner scanner, int lease_id){
        System.out.println();
        System.out.println();
        System.out.println();
        String sql = "select * from lease where lease_id = ?";
        String header = String.format("%-20s %-20s %-20s %-20s %n",
        "LEASE_ID", "TERM", "START_DATE", "DEPOSIT");
        System.out.println("Lease Detail for Lease ID: " + lease_id);
        System.out.println(ViewCompany.generateDashLine(4,20,"-"));
        System.out.println(header);
        System.out.println(ViewCompany.generateDashLine(4,20,"-"));
        try(PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setInt(1, lease_id);
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()){
                String lease_id_ = rs.getString("lease_id");
                String term = rs.getString("term");
                Date dbDate = rs.getDate("start_date");
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String start_date = dateFormat.format(dbDate);
                String deposit = rs.getString("deposit");
                String row = String.format("%-20s %-20s %-20s %-20s %n",
                lease_id_, term, start_date, deposit);
                System.out.println(row);
            }
        }catch(SQLException e){
            System.out.println(e.getMessage());
        }
        System.out.println(ViewCompany.generateDashLine(4,20,"-"));
    }
    
    /***
     * View Tenant under a lease
     * @param conn
     * @param scanner
     * @param lease_id
     */
    public static void ViewTenant(Connection connm, Scanner scanner, int lease_id){
        System.out.println();
        System.out.println();
        System.out.println();
        String sql = """
            select * from prospect_tenant natural join person natural join tenant where lease_id = ?
                """;
        System.out.println("Tenant Detail for Lease ID: " + lease_id );
        System.out.println(ViewCompany.generateDashLine(7, 20, "-"));
        String header = String.format("%-20s %-20s %-20s %-20s %-20s %-20s %-20s %n",
        "TENANT_ID", "NAME", "DATE_OF_BIRTH", "MONTH_INCOME", "RENTAL_HISTORY", "CRIME_HISTORY", "SSN");
        System.out.println(header);
        System.out.println(ViewCompany.generateDashLine(7,20,"-"));
        try(PreparedStatement pstmt = connm.prepareStatement(sql)){
            pstmt.setInt(1, lease_id);
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()){
                String id = rs.getString("id");
                String name = rs.getString("name");
                Date dbDate = rs.getDate("date_of_birth");
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String date_of_birth = dateFormat.format(dbDate);
                String month_income = rs.getString("month_income");
                String rental_history = rs.getString("rental_history");
                String crime_history = rs.getString("crime_history");
                String ssn = rs.getString("ssn");
                String row = String.format("%-20s %-20s %-20s %-20s %-20s %-20s %-20s %n",
                id, name, date_of_birth, month_income, rental_history, crime_history, ssn);
                System.out.println(row);
            }
        }catch(SQLException e){
            System.out.println(e.getMessage());
        }
        System.out.println(ViewCompany.generateDashLine(7,20,"-"));
    }



    /***
     * View Pet under a lease
     * @param conn
     * @param scanner
     * @param lease_id
     */
    public static void ViewPet(Connection conn, Scanner scanner, int lease_id){
        System.out.println();
        System.out.println();
        System.out.println();
        String sql = """
            select * from prospect_tenant natural join pet natural join lease_pet where lease_id = ?
                """;
        System.out.println("Pet Detail for Lease ID: " + lease_id);
        System.out.println(ViewCompany.generateDashLine(5, 20, "-"));
        String header = String.format("%-20s %-20s %-20s %-20s %-20s %n",
        "PET_ID", "PET_NAME", "DATE_OF_BIRTH", "SPECIES", "VACCINATION");
        System.out.println(header);
        System.out.println(ViewCompany.generateDashLine(5,20,"-"));
        try(PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setInt(1, lease_id);
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()){
                String pet_id = rs.getString("id");
                String pet_name = rs.getString("name");
                Date dbDate = rs.getDate("date_of_birth");
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String date_of_birth = dateFormat.format(dbDate);
                String pet_breed = rs.getString("species");
                String pet_age = rs.getString("vaccination");
                String row = String.format("%-20s %-20s %-20s %-20s %-20s %n",
                pet_id, pet_name, date_of_birth, pet_breed, pet_age);
                System.out.println(row);
            }
        }catch(SQLException e){
            System.out.println(e.getMessage());
        }
        System.out.println(ViewCompany.generateDashLine(5,20,"-"));
    }


    /***
     * Move out tenant
     * @param conn
     * @param scanner
     */
    public static void Moveout(Connection conn, Scanner scanner){
        System.out.println();
        System.out.println();
        System.out.println();
        ViewAllLease(conn, scanner);
        ViewExpiredLease(conn, scanner);
        String lease_id = "";
        int prop_id = 0;
        int apt_id = 0;
        float deposit = 0;
        String check_sql = """
            select * from lease where lease_id = ?
                """;
        String moveout_sql = """
            delete from lease where lease_id = ?
                """;
        System.out.println("\n====================== EU: Move out Page =========================");
        System.out.println("NOTE: We are a brutal company, Proprty Manager has the right to move out any lease(including not expired lease)");
        System.out.println("NOTE: Moveout a lease will delete all the data related to this lease, including tenant, pet, selected amenity");
        System.out.println("NOTE: Moveout a lease will not delete payment history");
        System.out.println("NOTE: Moveout a lease will refund the deposit to the tenant");
        scanner.nextLine();
        //Check lease id
        while(true){
            lease_id = Customer.InfoInput(scanner, "Please Enter a Lease ID(or 'q' to quit)", "id");
            if (lease_id.equalsIgnoreCase("q")){
                System.out.println("=====================================================================");
                return;
            }
            try(PreparedStatement pstmt = conn.prepareStatement(check_sql)){
                pstmt.setString(1, lease_id);
                ResultSet rs = pstmt.executeQuery();
                if(!rs.next()){
                    System.out.println("Lease ID does not exist!");
                    continue;
                }
                prop_id = rs.getInt("prop_id");
                apt_id = rs.getInt("apt_id");
                deposit = rs.getFloat("deposit");
                deposit = -1 * deposit;
            }catch(SQLException e){
                System.out.println(e.getMessage());
            }
            break;
        }
        //Confirm moveout
        ViewLeaseDetial(conn, scanner, Integer.parseInt(lease_id));
        String confirm = Customer.InfoInput(scanner, "Are you sure to move out this lease? Enter Yes/No: ", "confirm");
        if(confirm.equalsIgnoreCase("No")){
            System.out.println("=====================================================================");
            return;
        }
        // Refund deposit
        String cardNumber = "1000000000000000";
        String cardName = "EU";
        String cardExpire = "2044/12/31";
        String bill_address = "1234 EU Street";
        System.out.println("Refund deposit with Card");
        System.out.println("Card Number: " + cardNumber);
        System.out.println("Card Name: " + cardName);
        System.out.println("Card Expire Date: " + cardExpire);
        System.out.println("Billing Address: " + bill_address);
        confirm = Customer.InfoInput(scanner, "Confirm to Refund? Enter Yes/No: ", "confirm");
        if(confirm.equalsIgnoreCase("No")){
            System.out.println("=====================================================================");
            return;
        }
        int payment_id = Tenant.InsertPayment(conn, scanner, Integer.parseInt(lease_id), deposit);
        if(payment_id == 0){
            System.out.println("Refund Failed!");
            System.out.println("=====================================================================");
            return;
        }
        String card_sql = """
            INSERT INTO credit_debit (payment_id, card_number, card_name, expire_date, bill_address) 
            VALUES (?, ?, ?, TO_DATE(?, 'yyyy/mm/dd'), ?)
                """;
        try(PreparedStatement pstmt = conn.prepareStatement(card_sql)){
            pstmt.setInt(1, payment_id);
            pstmt.setString(2, cardNumber);
            pstmt.setString(3, cardName);
            pstmt.setString(4, cardExpire);
            pstmt.setString(5, bill_address);
            pstmt.executeUpdate();
        }catch(SQLException e){
            System.out.println("Refund Failed!");
            System.out.println("=====================================================================");
            String delete_sql = """
                delete from payment where payment_id = ?
                    """;
            try(PreparedStatement pstmt = conn.prepareStatement(delete_sql)){
                pstmt.setInt(1, payment_id);
                pstmt.executeUpdate();
            }catch(SQLException e1){
                System.out.println(e1.getMessage());
            }
            return;
        }
        System.out.println("Refund Success!");
        ViewRefound(conn, scanner, payment_id);

        //Delete Lease
        try(PreparedStatement pstmt = conn.prepareStatement(moveout_sql)){
            pstmt.setString(1, lease_id);
            pstmt.executeUpdate();
        }catch(SQLException e){
            System.out.println(e.getMessage());
        }
        System.out.println("Moveout Success!");
        System.out.println("Property ID: " + prop_id + " Apartment ID: " + apt_id + " Lease ID: " + lease_id + " has been moved out!");
        System.out.println("=====================================================================");
    }

    /***
     * Check Expired Lease
     * @param conn
     * @param scanner
     */
    public static void ViewExpiredLease(Connection conn, Scanner scanner){
        System.out.println();
        System.out.println();
        System.out.println();
        String sql = """
            SELECT *
            FROM lease
            WHERE ADD_MONTHS(start_date, 12) < CURRENT_DATE
                """;
        System.out.println("Expired Lease");
        System.out.println(ViewCompany.generateDashLine(4, 20, "-"));
        String header = String.format("%-20s %-20s %-20s %-20s %n",
        "LEASE_ID", "TERM", "START_DATE", "DEPOSIT");
        System.out.println(header);
        System.out.println(ViewCompany.generateDashLine(4,20,"-"));
        try(PreparedStatement pstmt = conn.prepareStatement(sql)){
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()){
                String lease_id = rs.getString("lease_id");
                String term = rs.getString("term");
                Date dbDate = rs.getDate("start_date");
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String start_date = dateFormat.format(dbDate);
                String deposit = rs.getString("deposit");
                String row = String.format("%-20s %-20s %-20s %-20s %n",
                lease_id, term, start_date, deposit);
                System.out.println(row);
            }
        }catch(SQLException e){
            System.out.println(e.getMessage());
        }
        System.out.println(ViewCompany.generateDashLine(4,20,"-"));
    }

    /***
     * Check All Lease
     * @param conn
     * @param scanner
     */
    public static void ViewAllLease(Connection conn, Scanner scanner){
        System.out.println();
        System.out.println();
        System.out.println();
        String sql = """
            SELECT *
            FROM lease
                """;
        System.out.println("All Lease");
        System.out.println(ViewCompany.generateDashLine(4, 20, "-"));
        String header = String.format("%-20s %-20s %-20s %-20s %n",
        "LEASE_ID", "TERM", "START_DATE", "DEPOSIT");
        System.out.println(header);
        System.out.println(ViewCompany.generateDashLine(4,20,"-"));
        try(PreparedStatement pstmt = conn.prepareStatement(sql)){
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()){
                String lease_id = rs.getString("lease_id");
                String term = rs.getString("term");
                Date dbDate = rs.getDate("start_date");
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String start_date = dateFormat.format(dbDate);
                String deposit = rs.getString("deposit");
                String row = String.format("%-20s %-20s %-20s %-20s %n",
                lease_id, term, start_date, deposit);
                System.out.println(row);
            }
        }catch(SQLException e){
            System.out.println(e.getMessage());
        }
        System.out.println(ViewCompany.generateDashLine(4,20,"-"));
    }

    /***
     * Check payment history
     * @param conn
     * @param scanner
     * @param lease_id
     */
    public static void ViewRefound(Connection conn, Scanner scanner, int payment_id){
        System.out.println();
        System.out.println();
        System.out.println();
        String sql = """
            select * from payment natural join credit_debit where payment_id = ?
                """;
        System.out.println("Refound Detail for Payment ID: " + payment_id);
        System.out.println(ViewCompany.generateDashLine(8, 20, "-"));
        String header = String.format("%-20s %-20s %-20s %-20s %-20s %-20s %-20s %-20s %n",
        "PAYMENT_ID", "LEASE_ID", "AMOUNT", "DATE", "CARD_NUMBER", "CARD_NAME", "EXPIRE_DATE", "BILL_ADDRESS");
        System.out.println(header);
        System.out.println(ViewCompany.generateDashLine(8,20,"-"));
        try(PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setInt(1, payment_id);
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()){
                String payment_id_ = rs.getString("payment_id");
                String lease_id = rs.getString("lease_id");
                String amount = rs.getString("amount");
                Date dbDate = rs.getDate("payment_date");
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String date = dateFormat.format(dbDate);
                String card_number = rs.getString("card_number");
                String card_name = rs.getString("card_name");
                dbDate = rs.getDate("expire_date");
                dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String expire_date = dateFormat.format(dbDate);
                String bill_address = rs.getString("bill_address");
                String row = String.format("%-20s %-20s %-20s %-20s %-20s %-20s %-20s %-20s %n",
                payment_id_, lease_id, amount, date, card_number, card_name, expire_date, bill_address);
                System.out.println(row);
            }
        }catch(SQLException e){
            System.out.println(e.getMessage());
        }
        System.out.println(ViewCompany.generateDashLine(8,20,"-"));
    }


}   
