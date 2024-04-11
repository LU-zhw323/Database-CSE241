import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Random;
import java.util.Scanner;




public class Tenant {

    // Private constructor to prevent instantiation
    private Tenant() {
        throw new AssertionError("Utility class cannot be instantiated");
    }

    /**
     * Check date
     * @param conn
     * @param scanner
     * @param lease_id
     */
    public static boolean IfExpire(Connection conn, Scanner scanner, int lease_id){
        boolean isExpire = false;
        String check_valid = """
            select start_date from lease where lease_id = ?
                """;
        try(PreparedStatement ps = conn.prepareStatement(check_valid);) {
            ps.setInt(1, lease_id);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                Date dbSqlDate = rs.getDate("start_date");
                LocalDate dbDate = dbSqlDate.toLocalDate(); // Convert to LocalDate
                LocalDate currentDate = LocalDate.now(); // Get current date

                // Calculate period between current date and dbDate
                Period period = Period.between(dbDate, currentDate);
                int years = period.getYears();
                if (years >= 1) {
                    isExpire = true;
                } else {
                    isExpire = false;
                }
                
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return isExpire;
    }

    /**
     * Tenant Main Page
     * @param conn
     * @param tenant_id
     */
    public static void TenantPage(Connection conn, Scanner scanner,String tenant_id) {
        int lease_id = 0;
        String lease_sql = "SELECT lease_id FROM tenant WHERE id = ?";
        try(PreparedStatement ps = conn.prepareStatement(lease_sql);) {
            ps.setString(1, tenant_id);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                lease_id = rs.getInt("lease_id");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        
        boolean isExpire = IfExpire(conn, scanner, lease_id);
        if(isExpire){
            System.out.println("\n====================== EU: Tenant Page =========================");
            System.out.println("Your lease is expired! Please contact your property manager!");
            System.out.println("Property Manager will move out and refund you as soon as possible!");
            System.out.println("If you wish to renew your lease, please sign a new lease once again!");
            System.out.println("Before you move out, please pay your due and clean your apartment!");
            while(true){
                String confirm = Customer.InfoInput(scanner, "Enter 'yes' to confirm: ", "confirm");
                if(confirm.equalsIgnoreCase("yes")){
                    ViewCompany.ViewSummary(conn,new StringBuilder(), null, null, Integer.toString(lease_id), false);
                    ViewSelectedAmenity(conn, scanner, lease_id);
                    PropertyManager.ViewLeaseDetial(conn, scanner, lease_id);
                    PropertyManager.ViewTenant(conn, scanner, lease_id);
                    PropertyManager.ViewPet(conn, scanner, lease_id);
                    float monthly_bill = ViewBill(conn, scanner, lease_id);
                    PayBill(conn, scanner, lease_id, monthly_bill);
                    break;
                }
            }
            System.out.println("=====================================================================");
            return;
        }
        
        int option;
        do {
            Customer.PrintCustomerSummary(conn, Integer.parseInt(tenant_id));
            System.out.println();
            System.out.println();
            System.out.println();
            System.out.println("\n====================== EU: Tenant Page =========================");
            System.out.println("1. Update My Profile");
            System.out.println("2. View My Apartment & Pay My Bill");
            System.out.println("3. Add Roommate or Pet");
            System.out.println("4. Logout");
            System.out.println("=====================================================================");
            System.out.print("Please enter your choice (1-4): ");

            while (!scanner.hasNextInt()) {
                System.out.println("That's not a valid option! Please enter a number between 1 and 4.");
                scanner.next();
                System.out.print("Please enter your choice (1-4): ");
            }

            option = scanner.nextInt();

            switch (option) {
                case 1:
                    Customer.CustomerInfoInput(conn, scanner, tenant_id, true);
                    break;
                case 2:
                    ViewCompany.ViewSummary(conn,new StringBuilder(), null, null, Integer.toString(lease_id), false);
                    ViewSelectedAmenity(conn, scanner, lease_id);
                    PropertyManager.ViewLeaseDetial(conn, scanner, lease_id);
                    PropertyManager.ViewTenant(conn, scanner, lease_id);
                    PropertyManager.ViewPet(conn, scanner, lease_id);
                    float monthly_bill = ViewBill(conn, scanner, lease_id);
                    PayBill(conn, scanner, lease_id, monthly_bill);
                    break;
                case 3:
                    scanner.nextLine();
                    AddRoommate(conn, scanner, lease_id);
                    break;
                case 4:
                    System.out.println("Logging out...");
                    break;
                default:
                    System.out.println("That's not a valid option! Please enter a number between 1 and 4.");
                    break;
            }
        } while (option != 4);
    }

    /***
     * Add Roommate
     * @param conn
     * @param scanner
     * @param lease_id
     */
    public static void AddRoommate(Connection conn, Scanner scanner, int lease_id){
        //Person
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println("Available Roommates");
        String person_sql = """
            SELECT pt.*, p.month_income, p.rental_history, p.crime_history
            FROM prospect_tenant pt
            JOIN person p ON pt.id = p.id
            LEFT JOIN tenant t ON p.id = t.id
            WHERE t.id IS NULL
                """;
        System.out.println(ViewCompany.generateDashLine(6, 20, "-"));
        String header = String.format(" %-20s %-20s %-20s %-20s %-20s %-20s %n",
        "ID", "NAME", "DATE OF BIRTH", "INCOME", "RENTAL HISTORY", "CRIME HISTORY");
        System.out.println(header);
        System.out.println(ViewCompany.generateDashLine(6, 20, "-"));
        try(PreparedStatement ps = conn.prepareStatement(person_sql);) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String id = rs.getString("id");
                String name = rs.getString("name");
                Date dbDate = rs.getDate("date_of_birth");
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String birth = dateFormat.format(dbDate);
                String month_income = rs.getString("month_income");
                String rental_history = rs.getString("rental_history");
                String crime_history = rs.getString("crime_history");
                String row = String.format(" %-20s %-20s %-20s %-20s %-20s %-20s %n",
                id, name, birth, month_income, rental_history, crime_history);
                System.out.println(row);
                System.out.println(ViewCompany.generateDashLine(6, 20, "-"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        //Pet
        System.out.println();
        System.out.println();
        System.out.println("Available Pets");
        String pets_sql = """
            SELECT pt.*, p.species, p.vaccination
            FROM prospect_tenant pt
            JOIN pet p ON pt.id = p.id
            LEFT JOIN lease_pet t ON p.id = t.id
            WHERE t.id IS NULL
                """;
        System.out.println(ViewCompany.generateDashLine(5, 20, "-"));
        header = String.format(" %-20s %-20s %-20s %-20s %-20s %n",
                            "ID", "NAME", "DATE OF BIRTH", "SPECIES", "VACCINATION");
        System.out.println(header);
        System.out.println(ViewCompany.generateDashLine(5, 20, "-"));
        try(PreparedStatement ps = conn.prepareStatement(pets_sql);) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String id = rs.getString("id");
                String name = rs.getString("name");
                Date dbDate = rs.getDate("date_of_birth");
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String birth = dateFormat.format(dbDate);
                String species = rs.getString("species");
                String vaccination = rs.getString("vaccination");
                String row = String.format(" %-20s %-20s %-20s %-20s %-20s %n",
                id, name, birth, species, vaccination);
                System.out.println(row);
                System.out.println(ViewCompany.generateDashLine(5, 20, "-"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        System.out.println();
        System.out.println();


        //Add Roommate
        System.out.println("\n====================== EU: Add Roomate/Pet Page =========================");
        System.out.println("Note: You can only have one pet");
        //scanner.nextLine();
        while (true) {
            String id = Customer.InfoInput(scanner, "Enter a ID of a customer or a pet:(or 'q' to quit) ", "id");
            if(id.equalsIgnoreCase("q")){
                break;
            }
            String check_sql = """
                select * from prospect_tenant where id = ?
                    """;
            try(PreparedStatement ps = conn.prepareStatement(check_sql);) {
                ps.setString(1, id);
                ResultSet rs = ps.executeQuery();
                if(!rs.next()) {
                    System.out.println("We dont have record on this ID, Please go back and sign him/her up!");
                    continue;
                }
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
            boolean isPet = Customer.IfPet(conn, Integer.parseInt(id));
            String availble_sql = "";
            //Person
            if(!isPet){
                availble_sql = """
                        SELECT * from tenant where id = ?
                        """;
                try(PreparedStatement ps = conn.prepareStatement(availble_sql);) {
                    ps.setString(1, id);
                    ResultSet rs = ps.executeQuery();
                    if(rs.next()) {
                        System.out.println("This person is already a tenant!");
                        continue;
                    }
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                }
                String ssn = Customer.InfoInput(scanner, "Enter a SSN of a customer: ", "ssn");
                String add_tenant_sql = """
                    INSERT INTO tenant (id, lease_id, ssn) VALUES (?, ?, ?)
                        """;
                try(PreparedStatement ps = conn.prepareStatement(add_tenant_sql);) {
                    ps.setString(1, id);
                    ps.setInt(2, lease_id);
                    ps.setString(3, ssn);
                    ps.executeUpdate();
                    System.out.println("Add tenant successfully!");
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                } 
            }
            else{
                availble_sql = """
                        SELECT * from lease_pet where id = ?
                        """;
                try(PreparedStatement ps = conn.prepareStatement(availble_sql);) {
                    ps.setString(1, id);
                    ResultSet rs = ps.executeQuery();
                    if(rs.next()) {
                        System.out.println("This pet is already added!");
                        continue;
                    }
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                }
                String add_pet_sql = """
                    INSERT INTO lease_pet (lease_id, id) VALUES (?, ?)
                        """;
                try(PreparedStatement ps = conn.prepareStatement(add_pet_sql);) {
                    ps.setString(2, id);
                    ps.setInt(1, lease_id);
                    ps.executeUpdate();
                    System.out.println("Add pet successfully!");
                } catch (SQLException e) {
                    System.out.println("You can only have one pet!");
                } 
            }


        }


        
        
    }



    /***
     * View Apartment Information
     * 
     */
    public static void ViewSelectedAmenity(Connection conn, Scanner scanner, int lease_id){
        //Common Amenities
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println("Selected Common Amenities");
        String sql = """
            select amenity_id, description, cost, hour, capacity from select_amenity natural join amenity natural join common_amenity where lease_id = ?
                """;
        try(PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setInt(1, lease_id);
            ResultSet rs = ps.executeQuery();
            System.out.println(ViewCompany.generateDashLine(5,20,"-"));
            String header = String.format("%-20s %-20s %-20s %-20s %-20s %n",
            "AMENITY_ID", "COMMON_AMENITY",
            "COST","HOUR", "CAPACITY");
            System.out.println(header);
            System.out.println(ViewCompany.generateDashLine(5,20,"-"));
            while (rs.next()) {
                String amenity_id = rs.getString("amenity_id");
                String description = rs.getString("description");
                String cost = rs.getString("cost");
                String hour = rs.getString("hour");
                String capacity = rs.getString("capacity");
                String row = String.format("%-20s %-20s %-20s %-20s %-20s %n",
                amenity_id, description,
                cost,hour, capacity);
                System.out.println(row);
            }
            System.out.println(ViewCompany.generateDashLine(5,20,"-"));
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }


        //Private Amenities
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println("Selected Private Amenities");
        sql = """
            select amenity_id, description, cost, feature, warranty from select_amenity natural join amenity natural join private_amenity where lease_id = ?
                """;
        try(PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setInt(1, lease_id);
            ResultSet rs = ps.executeQuery();
            System.out.println(ViewCompany.generateDashLine(5,20,"-"));
            String header = String.format("%-20s %-20s %-20s %-20s %-20s %n",
            "AMENITY_ID", "COMMON_AMENITY",
            "COST","FEATURE", "WARRANTY");
            System.out.println(header);
            System.out.println(ViewCompany.generateDashLine(5,20,"-"));
            while (rs.next()) {
                String amenity_id = rs.getString("amenity_id");
                String description = rs.getString("description");
                String cost = rs.getString("cost");
                String hour = rs.getString("feature");
                String capacity = rs.getString("warranty");
                String row = String.format("%-20s %-20s %-20s %-20s %-20s %n",
                amenity_id, description,
                cost,hour, capacity);
                System.out.println(row);
            }
            System.out.println(ViewCompany.generateDashLine(5,20,"-"));
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /***
     * View Monthly Bill
     * @param conn
     * @param scanner
     * @param tenant_id
     */
    public static float ViewBill(Connection conn, Scanner scanner, int lease_id){
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println("Monthly Bill");
        String sql = """
            select * from calculate_total_bill(?)
                """;
        float monthly_bill = 0;
        try(PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setInt(1, lease_id);
            ResultSet rs = ps.executeQuery();
            System.out.println(ViewCompany.generateDashLine(3,20,"-"));
            String header = String.format("%-20s %-20s %-20s %n",
            "RENT", "TOTAL_AMENITY", "TOTAL_MONTHLY_BILL");
            System.out.println(header);
            System.out.println(ViewCompany.generateDashLine(3,20,"-"));
            while (rs.next()) {
                float rent = rs.getFloat("apartment_rent");
                float total_amenity = rs.getFloat("total_amenity_cost");
                float total_bill = rs.getFloat("total_monthly_bill");
                monthly_bill = total_bill;
                String row = String.format("%-20.2f %-20.2f %-20.2f %n",
                rent, total_amenity, total_bill);
                System.out.println(row);
            }
            System.out.println(ViewCompany.generateDashLine(3,20,"-"));
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return monthly_bill;
    }


    /***
     * Pay Monthly Bill
     * @param conn
     * @param scanner
     * @param lease_id
     * @param monthly_bill
     */
    public static void PayBill(Connection conn, Scanner scanner, int lease_id, float monthly_bill){
        System.out.println();
        System.out.println();
        System.out.println();
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        String formattedDate = currentDate.format(formatter);
        int month = 0;
        String due_sql = "SELECT calculate_due_rent_payments(?) FROM dual";
        try(PreparedStatement ps = conn.prepareStatement(due_sql);) {
            ps.setInt(1, lease_id);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                month = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        float due = month * monthly_bill;
        int option = 0;
        do{
            int payment_id = 0;
            System.out.println("\n====================== EU: Bill & Pay Page =========================");
            System.out.println("Your monthly bill is: " + monthly_bill);
            System.out.println("You have "+ month + " unpaid payment at "+ formattedDate);
            System.out.println("Your due is: " + due + " at " + formattedDate);
            if(due == 0){
                System.out.println("You have no due. Thank you for your payment!");
                System.out.println("=====================================================================");
                break;
            }
            System.out.println("1. Pay By Credit/Debit Card");
            System.out.println("2. Pay By Transfer");
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
                    payment_id = PayByCard(conn, scanner, lease_id, due);
                    if(payment_id != 0){
                        System.out.println("Your Payment ID is: " + payment_id);
                        System.out.println("Thank you for your payment!");
                        return;
                    }
                    break;
                case 2:
                    payment_id = PayByTransfer(conn, scanner, lease_id, due);
                    if(payment_id != 0){
                        System.out.println("Your Payment ID is: " + payment_id);
                        System.out.println("Thank you for your payment!");
                        return;
                    }
                    break;
                case 3:
                    System.out.println("Returning to main page...");
                    break;
                default:
                    System.out.println("That's not a valid option! Please enter a number between 1 and 3.");
                    break;
            }
        }while(option != 3);
    }


    /***
     * Insert Payment
     * @param conn
     * @param scanner
     * @param lease_id
     * @param total_bill
     * @return
     */
    public static int InsertPayment(Connection conn, Scanner scanner, int lease_id, float total_bill){
        int payment_id = 0;
        String sql = """
            INSERT INTO payment (lease_id, payment_date, amount) 
            VALUES (?, TO_DATE(?, 'yyyy/mm/dd'), ?)
                """;
        try(PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setInt(1, lease_id);
            LocalDate currentDate = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
            String formattedDate = currentDate.format(formatter);
            ps.setFloat(3, total_bill);
            ps.setString(2, formattedDate);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        
        String payment_id_sql = """
            select payment_id from payment where lease_id = ? and payment_date = TO_DATE(?, 'yyyy/mm/dd') and amount = ?
                """;
        try(PreparedStatement ps = conn.prepareStatement(payment_id_sql);) {
            ps.setInt(1, lease_id);
            LocalDate currentDate = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
            String formattedDate = currentDate.format(formatter);
            ps.setString(2, formattedDate);
            ps.setFloat(3, total_bill);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                payment_id = rs.getInt("payment_id");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return payment_id;
    }


    /***
     * Pay Monthly Bill By Transfer
     * @param conn
     * @param scanner
     * @param lease_id
     * @param total_bill
     * @return
     */
    public static int PayByTransfer(Connection conn, Scanner scanner, int lease_id, float total_bill){
        System.out.println();
        System.out.println();
        System.out.println();

        String account_number = "";
        String bank_name = "";
        String routing_number = "";
        String reference_number = "";
        String sql = """
            INSERT INTO transfer (payment_id, account_number, bank_name, routing_number, reference_number)
            VALUES (?, ?, ?, ?, ?)
                        """;
            
        int option = 0;
        do{
            System.out.println("\n====================== EU: Pay By Transfer Page =========================");
            System.out.println("Do you want to automatically generate transfer information?");
            System.out.println("1. Yes");
            System.out.println("2. No");
            System.out.println("3. Quit");
            System.out.println("=====================================================================");
            System.out.print("Please enter your choice (1-3): ");

            while (!scanner.hasNextInt()) {
                System.out.println("That's not a valid option! Please enter a number between 1 and 3.");
                scanner.next();
                System.out.print("Please enter your choice (1-3): ");
            }

            option = scanner.nextInt();
            int payment_id = 0;

            switch (option) {
                case 1:
                    payment_id = InsertPayment(conn, scanner, lease_id, total_bill);
                    if(payment_id == 0){
                        System.out.println("Payment failed!");
                        return 0;
                    }
                    account_number = generateRandomNumber(12);
                    routing_number = generateRandomNumber(9);
                    reference_number = "REF" + generateRandomNumber(7);
                    bank_name = generateRandomBank();
                    try(PreparedStatement ps = conn.prepareStatement(sql);) {
                        ps.setInt(1, payment_id);
                        ps.setString(2, account_number);
                        ps.setString(3, bank_name);
                        ps.setString(4, routing_number);
                        ps.setString(5, reference_number);
                        ps.executeUpdate();
                        System.out.println("Your payment is successful!");
                        return payment_id;
                    } catch (SQLException e) {
                        String delete_sql = """
                            delete from payment where payment_id = ?
                                """;
                        try(PreparedStatement ps = conn.prepareStatement(delete_sql);) {
                            ps.setInt(1, payment_id);
                            ps.executeUpdate();
                        } catch (SQLException e1) {
                            System.out.println(e1.getMessage());
                        }
                        System.out.println(e.getMessage());
                    }
                    break;
                case 2:
                    scanner.nextLine();
                    bank_name = Customer.InfoInput(scanner, "Enter a Bank Name: ", "bank_name");
                    account_number = Customer.InfoInput(scanner, "Enter a 12-Digit Account Number: ", "account_number");
                    routing_number = Customer.InfoInput(scanner, "Enter a 9-Digit Routing Number: ", "routing_number");
                    reference_number = Customer.InfoInput(scanner, "Enter a Reference Number(8-20 digit, contain only (A-Z) or (0-9) ): ", "reference_number");
                    payment_id = InsertPayment(conn, scanner, lease_id, total_bill);
                    if(payment_id == 0){
                        System.out.println("Payment failed!");
                        return 0;
                    }
                    try(PreparedStatement ps = conn.prepareStatement(sql);) {
                        ps.setInt(1, payment_id);
                        ps.setString(2, account_number);
                        ps.setString(3, bank_name);
                        ps.setString(4, routing_number);
                        ps.setString(5, reference_number);
                        ps.executeUpdate();
                        System.out.println("Your payment is successful!");
                        return payment_id;
                    } catch (SQLException e) {
                        String delete_sql = """
                            delete from payment where payment_id = ?
                                """;
                        try(PreparedStatement ps = conn.prepareStatement(delete_sql);) {
                            ps.setInt(1, payment_id);
                            ps.executeUpdate();
                        } catch (SQLException e1) {
                            System.out.println(e1.getMessage());
                        }
                        System.out.println(e.getMessage());
                    }
                    break;
                case 3:
                    System.out.println("Returning to previous page...");
                    break;
                default:
                    System.out.println("That's not a valid option! Please enter a number between 1 and 3.");
                    break;
            }
        }while(option != 3);
        return 0;
    }

    /***
     * Pay Monthly Bill By Credit/Debit Card
     * @param conn
     * @param scanner
     * @param lease_id
     * @param total_bill
     */
    public static int PayByCard(Connection conn, Scanner scanner, int lease_id, float total_bill){
        System.out.println();
        System.out.println();
        System.out.println();

        String name = "";
        String card_number = "";
        String bill_address = "";
        String exp_date = "";
         String sql = """
                        INSERT INTO credit_debit (payment_id, card_number, card_name, expire_date, bill_address) 
                        VALUES (?, ?, ?, TO_DATE(?, 'yyyy/mm/dd'), ?)
                            """;

        
        String name_sql = """
                select name from lease natural join tenant natural join prospect_tenant where lease_id = ?
                """;
        try(PreparedStatement ps = conn.prepareStatement(name_sql);) {
            ps.setInt(1, lease_id);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                name = rs.getString("name");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        String address_sql = """
            select address from lease natural join property where lease_id = ?
                """;
        try(PreparedStatement ps = conn.prepareStatement(address_sql);) {
            ps.setInt(1, lease_id);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                bill_address = rs.getString("address");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        int option = 0;
        do{
            System.out.println("\n====================== EU: Pay By Card Page =========================");
            System.out.println("Do you want to automatically generate card information?");
            System.out.println("1. Yes");
            System.out.println("2. No");
            System.out.println("3. Quit");
            System.out.println("=====================================================================");
            System.out.print("Please enter your choice (1-3): ");

            while (!scanner.hasNextInt()) {
                System.out.println("That's not a valid option! Please enter a number between 1 and 3.");
                scanner.next();
                System.out.print("Please enter your choice (1-3): ");
            }

            option = scanner.nextInt();
            int payment_id = 0;

            switch (option) {
                case 1:
                    payment_id = InsertPayment(conn, scanner, lease_id, total_bill);
                    if(payment_id == 0){
                        System.out.println("Payment failed!");
                        return 0;
                    }
                    exp_date = generateRandomDate(2021, 2023);
                    card_number = generateRandomNumber(16);
                    try(PreparedStatement ps = conn.prepareStatement(sql);) {
                        ps.setInt(1, payment_id);
                        ps.setString(2, card_number);
                        ps.setString(3, name);
                        ps.setString(4, exp_date);
                        ps.setString(5, bill_address);
                        ps.executeUpdate();
                        System.out.println("Your payment is successful!");
                        return payment_id;
                    } catch (SQLException e) {
                        String delete_sql = """
                            delete from payment where payment_id = ?
                                """;
                        try(PreparedStatement ps = conn.prepareStatement(delete_sql);) {
                            ps.setInt(1, payment_id);
                            ps.executeUpdate();
                        } catch (SQLException e1) {
                            System.out.println(e1.getMessage());
                        }
                        System.out.println(e.getMessage());
                    }
                    break;
                case 2:
                    scanner.nextLine();
                    card_number = Customer.InfoInput(scanner, "Enter a 16-Digit Card Number: ", "card_number");
                    exp_date = Customer.InfoInput(scanner, "Enter a Expire Date (yyyy/mm/dd): ", "birth");
                    payment_id = InsertPayment(conn, scanner, lease_id, total_bill);
                    if(payment_id == 0){
                        System.out.println("Payment failed!");
                        return 0;
                    }
                    try(PreparedStatement ps = conn.prepareStatement(sql);) {
                        ps.setInt(1, payment_id);
                        ps.setString(2, card_number);
                        ps.setString(3, name);
                        ps.setString(4, exp_date);
                        ps.setString(5, bill_address);
                        ps.executeUpdate();
                        System.out.println("Your payment is successful!");
                        return payment_id;
                    } catch (SQLException e) {
                        String delete_sql = """
                            delete from payment where payment_id = ?
                                """;
                        try(PreparedStatement ps = conn.prepareStatement(delete_sql);) {
                            ps.setInt(1, payment_id);
                            ps.executeUpdate();
                        } catch (SQLException e1) {
                            System.out.println(e1.getMessage());
                        }
                        System.out.println(e.getMessage());
                    }
                    break;
                case 3:
                    System.out.println("Returning to previous page...");
                    break;
                default:
                    System.out.println("That's not a valid option! Please enter a number between 1 and 3.");
                    break;
            }
        }while(option != 3);
        return 0;
    }

    /**
     * Randomly generate a date between 2000/01/01 and 2023/01/01
     * @return a random date
     */
    public static String generateRandomDate(int startYear, int endYear) {
        // Convert the years to dates
        Calendar calendar = Calendar.getInstance();
        calendar.set(startYear, 0, 1);
        long startMillis = calendar.getTimeInMillis(); // January 1, startYear

        calendar.set(endYear, 0, 1);
        long endMillis = calendar.getTimeInMillis(); // January 1, endYear

        // Generate a random date between the range
        Random random = new Random();
        long randomMillisSinceEpoch = startMillis + (long) (random.nextDouble() * (endMillis - startMillis));

        // Format the random date
        Date randomDate = new Date(randomMillisSinceEpoch);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        return sdf.format(randomDate);
    }


    /**
     * Randomly generate a card number
     */
    public static String generateRandomNumber(int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();

        // First digit should be non-zero
        sb.append(random.nextInt(9) + 1);

        // Generate the remaining 15 digits
        for (int i = 0; i < length -1 ; i++) {
            sb.append(random.nextInt(10));
        }

        return sb.toString();
    }

    /**
     * Randomly generate a bank name
     */
    public static String generateRandomBank(){
        String[] bank = {"Bank of America", "Chase", "Wells Fargo", "Citi Bank", "US Bank", "PNC Bank", "TD Bank", "Capital One", "BB&T", "SunTrust"};
        Random random = new Random();
        return bank[random.nextInt(bank.length)];
    }
}



