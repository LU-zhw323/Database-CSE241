import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedWriter;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

import javax.swing.text.View;

public final class Customer {

    // Private constructor to prevent instantiation
    private Customer() {
        throw new AssertionError("Utility class cannot be instantiated");
    }

    /**
     * Customer Page
     * 
     * @param conn
     * @param scanner
     * @param notNewCustomer True if the customer is not a new customer
     */
    public static void CustomerPage(Connection conn, Scanner scanner, boolean NewCustomer) {
        int option;
        int max_option = NewCustomer ? 3 : 2;
        do {
            System.out.println();
            System.out.println();
            System.out.println();
            System.out.println("\n==================== EU: Customer Login/Signup Page ====================");
            System.out.println("1. Login in with ID(Pet cannot login)");
            if (NewCustomer) {
                System.out.println("2. Signup as Prosepctive Tenant");
            }
            System.out.printf("%1d. Quit \n", max_option);
            System.out.println("=====================================================================");
            System.out.printf("Please enter your choice (1-%1d): \n", max_option);

            while (!scanner.hasNextInt()) {
                System.out.printf("That's not a valid option! Please enter a number between 1 and %1d.\n", max_option);
                scanner.next();
                System.out.printf("Please enter your choice (1-%1d): \n", max_option);
            }

            option = scanner.nextInt();

            switch (option) {
                case 1:
                    int target_id = LoginPage(conn, scanner);
                    if (target_id != 0) {
                        boolean isTenant = IfTenant(conn, target_id);
                        if (isTenant == true && NewCustomer == true) {
                            System.out.println("Please go back and login as Tenant");
                        } else if (isTenant == false && NewCustomer == false) {
                            System.out.println("Please go back and login as Prosepctive Tenant");
                        } else {
                            if (isTenant) {
                                Tenant.TenantPage(conn, scanner, Integer.toString(target_id));
                            } else {
                                PerspectiveTenantPage(conn, scanner, target_id);
                            }
                        }
                    }
                    break;
                case 2:
                    if (NewCustomer) {
                        CustomerInfoInput(conn, scanner, "", false);
                    } else {
                        System.out.println("Goodbye!");
                    }
                    break;
                case 3:
                    System.out.println("Goodbye!");
                    break;
                default:
                    System.out.printf("That's not a valid option! Please enter a number between 1 and %1d.\n", max_option);
                    break;
            }
        } while (option != max_option);
    }

    /***
     *  View Prospect Tenant
     * @param conn
     * @param scanner
     */
    public static void ViewProspectTenant(Connection conn, Scanner scanner){
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println("Prospect Tenant List");
        String person_sql = """
            SELECT pt.*, p.month_income, p.rental_history, p.crime_history
            FROM prospect_tenant pt
            JOIN person p ON pt.id = p.id
            LEFT JOIN tenant t ON p.id = t.id
            WHERE t.id IS NULL ORDER BY pt.id
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
    }
    
    
    /***
     * View Tenant
     * @param conn
     * @param scanner
     * @return
     */
    public static void ViewTenantList(Connection conn, Scanner scanner){
        System.out.println();
        System.out.println();
        System.out.println();
        String sql = """
            select * from prospect_tenant natural join person natural join tenant order by id
                """;
        System.out.println("Tenant List");
        System.out.println(ViewCompany.generateDashLine(7, 20, "-"));
        String header = String.format("%-20s %-20s %-20s %-20s %-20s %-20s %-20s %n",
        "TENANT_ID", "NAME", "DATE_OF_BIRTH", "MONTH_INCOME", "RENTAL_HISTORY", "CRIME_HISTORY", "SSN");
        System.out.println(header);
        System.out.println(ViewCompany.generateDashLine(7,20,"-"));
        try(PreparedStatement pstmt = conn.prepareStatement(sql)){
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
     * View Pet
     * @param conn
     * @param scanner
     * @return
     */
    public static void ViewPets(Connection conn, Scanner scanner){
        System.out.println();
        System.out.println();
        System.out.println();
        String sql = """
            select * from prospect_tenant natural join pet order by id
                """;
        System.out.println("Pet List");
        System.out.println(ViewCompany.generateDashLine(5, 20, "-"));
        String header = String.format("%-20s %-20s %-20s %-20s %-20s %n",
        "PET_ID", "PET_NAME", "DATE_OF_BIRTH", "SPECIES", "VACCINATION");
        System.out.println(header);
        System.out.println(ViewCompany.generateDashLine(5,20,"-"));
        try(PreparedStatement pstmt = conn.prepareStatement(sql)){
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
    
    /**
     * Customer Login
     * 
     * @param conn
     * @param scanner
     * 
     */
    public static int LoginPage(Connection conn, Scanner scanner) {
        ViewProspectTenant(conn, scanner);
        ViewTenantList(conn, scanner);
        ViewPets(conn, scanner);
        System.out.println();
        System.out.println();
        System.out.println();
        scanner.nextLine();
        int target_id = 0;
        System.out.println("\n==================== EU: Customer Login Page ====================");
        while (true) {
            String id = InfoInput(scanner, "Enter ID:(or 'q' to quit) ", "id");
            String sql = """
                        select * from prospect_tenant where id = ?
                    """;
            if (id.equalsIgnoreCase("q")) {
                System.out.println("Goodbye!");
                return 0;
            } else {
                try (PreparedStatement pstm = conn.prepareStatement(sql)) {
                    pstm.setInt(1, Integer.parseInt(id));
                    ResultSet rs = pstm.executeQuery();
                    if (rs.next()) {
                        target_id = Integer.parseInt(id);
                        break;
                    } else {
                        System.out.println("ID not found");
                        continue;
                    }
                } catch (SQLException e) {
                    System.out.println("Error: " + e.getMessage());
                }
            }
        }
        System.out.println("=====================================================================");
        System.out.println();
        if (IfPet(conn, target_id)) {
            System.out.println("Please go back, Pet cannot login");
            return 0;
        } else {
            return target_id;
        }
    }

    /**
     * Perspective Tenant Page
     * 
     * @param conn
     * @param scanner
     * @param id
     *
     */
    public static void PerspectiveTenantPage(Connection conn, Scanner scanner, int id) {
        int option;
        do {
            System.out.println();
            boolean isTenant = IfTenant(conn, id);
            if (isTenant) {
                System.out.println("Please go back and login as Tenant");
                break;
            }
            PrintCustomerSummary(conn, id);
            System.out.println("\n==================== EU: Prospective Tenant Page ====================");
            System.out.println("1. Update Customer Info");
            System.out.println("2. View Property & Apartment Info");
            System.out.println("3. Sechedule a Visit");
            System.out.println("4. Sign Lease and Select Amenity");
            System.out.println("5. Quit");
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
                    CustomerInfoInput(conn, scanner, Integer.toString(id), false);
                    break;
                case 2:
                    ViewCompany.ViewManual(conn, false, scanner);
                    break;
                case 3:
                    ViewCompany.ViewSummary(conn, new StringBuilder(), null, null, null, false);
                    ViewVisitedProperty(conn, scanner, id);
                    SecheduleVisit(conn, scanner, id);
                    break;
                case 4:
                    ViewCompany.ViewSummary(conn, new StringBuilder(), null, null, null, false);
                    ViewVisitedProperty(conn, scanner, id);
                    int lease_id = SignLease(conn, scanner, id);
                    if(lease_id != 0){
                        SelectAmenity(conn, scanner, lease_id);
                    }
                    break;
                case 5:
                    System.out.println("Goodbye!");
                    break;
                default:
                    System.out.println("That's not a valid option! Please enter a number between 1 and 5.");
                    break;
            }
        } while (option != 5);
    }

    /***
     * Check if a customer is a tenant or not
     * 
     * @param conn
     * @param scanner
     * @param id
     * @return true or false
     */
    public static boolean IfTenant(Connection conn, int id) {
        String sql = "select * from tenant where id = ?";
        Boolean isTenant = false;
        try (PreparedStatement pstm = conn.prepareStatement(sql)) {
            pstm.setInt(1, id);
            ResultSet res = pstm.executeQuery();
            if (res.next()) {
                isTenant = true;
            }

        } catch (SQLException e) {
            System.out.println("Error" + e);
        }
        return isTenant;
    }

    /***
     * Check if a customer is a pet or not
     * 
     * @param conn
     * @param id
     * @return
     */
    public static boolean IfPet(Connection conn, int id) {
        String sql = "select * from pet where id = ?";
        Boolean isPet = false;
        try (PreparedStatement pstm = conn.prepareStatement(sql)) {
            pstm.setInt(1, id);
            ResultSet res = pstm.executeQuery();
            if (res.next()) {
                isPet = true;
            }

        } catch (SQLException e) {
            System.out.println("Error" + e);
        }
        return isPet;

    }

    /**
     * Customer Input Validation
     * 
     * @param scanner
     * @param instruction
     * @param type
     * 
     */
    public static String InfoInput(Scanner scanner, String instruction, String type) {
        String res = "";
        while (true) {
            System.out.print(instruction);
            String input = scanner.nextLine();
            if (input.isEmpty()) {
                System.out.println("Please enter a valid info");
                continue;
            }
            switch (type) {
                case "id":
                    if (!input.matches("\\d{1,5}") && !input.equalsIgnoreCase("q")) {
                        System.out.println("Please enter a valid id. ID is a 1 to 5 digit number, Please try again.");
                        continue;
                    }
                    break;
                case "birth":
                    if (!input.matches("\\d{4}/\\d{2}/\\d{2}")) {
                        System.out.println("Please enter a valid date");
                        continue;
                    } else {
                        String year = input.substring(0, 4);
                        String month = input.substring(5, 7);
                        String day = input.substring(8, 10);
                        if (Integer.parseInt(year) < 1900 || Integer.parseInt(year) > 2023) {
                            System.out.println("Please enter a valid year");
                            continue;
                        }
                        if (Integer.parseInt(month) < 1 || Integer.parseInt(month) > 12) {
                            System.out.println("Please enter a valid month");
                            continue;
                        }
                        if (Integer.parseInt(day) < 1 || Integer.parseInt(day) > 31) {
                            System.out.println("Please enter a valid day");
                            continue;
                        }
                    }
                    break;
                case "income":
                    if (!input.matches("\\d+(\\.\\d+)?")) {
                        System.out.println("Please enter a valid income");
                        continue;
                    }
                    break;
                case "rental_history":
                    if (!input.equalsIgnoreCase("Good") && !input.equalsIgnoreCase("Average")
                            && !input.equalsIgnoreCase("Poor")) {
                        System.out.println("Please enter a valid rental history(Good. Average, Poor)");
                        continue;
                    }
                    break;
                case "crime_hisitory":
                    if (!input.equalsIgnoreCase("None") && !input.equalsIgnoreCase("Minor")
                            && !input.equalsIgnoreCase("Severe")) {
                        System.out.println("Please enter a valid crime history(Severe, Minor, None)");
                        continue;
                    }
                    break;
                case "vaccination":
                    if (!input.equalsIgnoreCase("Yes") && !input.equalsIgnoreCase("No")) {
                        System.out.println("Please enter a valid vaccination(Yes, No)");
                        continue;
                    }
                    break;
                case "ssn":
                    if (!input.matches("\\d{9}")) {
                        System.out.println("Please enter a valid ssn");
                        continue;
                    }
                    break;
                case "confirm":
                    if (!input.equalsIgnoreCase("Yes") && !input.equalsIgnoreCase("No")) {
                        System.out.println("Please enter a valid option(Yes, No)");
                        continue;
                    }
                    break;
                case "card_number":
                    if (!input.matches("\\d{16}")) {
                        System.out.println("Please enter a valid card number");
                        continue;
                    }
                    break;
                case "account_number":
                    if (!input.matches("\\d{12}")) {
                        System.out.println("Please enter a valid account number");
                        continue;
                    }
                    break;
                case "routing_number":
                    if (!input.matches("\\d{9}")) {
                        System.out.println("Please enter a valid routing number");
                        continue;
                    }
                    break;
                case "month":
                    if (!input.matches("\\d{2}")) {
                        System.out.println("Please enter a valid month");
                        continue;
                    }
                    else{
                        if(Integer.parseInt(input) < 1 || Integer.parseInt(input) > 12){
                            System.out.println("Please enter a valid month");
                            continue;
                        }
                    }
                    break;
                case "day":
                    if (!input.matches("\\d{2}")) {
                        System.out.println("Please enter a valid day");
                        continue;
                    }
                    else{
                        if(Integer.parseInt(input) < 1 || Integer.parseInt(input) > 31){
                            System.out.println("Please enter a valid day");
                            continue;
                        }
                    }
                    break;
                case "year":
                    if (!input.matches("\\d{4}")) {
                        System.out.println("Please enter a valid year");
                        continue;
                    }
                    else{
                        if(Integer.parseInt(input) < 1800 || Integer.parseInt(input) > 2023){
                            System.out.println("Please enter a valid year");
                            continue;
                        }
                    }
                    break;
                case "reference_number":
                    if (!input.matches("^[A-Z0-9]{8,20}$")) {
                        System.out.println("Please enter a valid reference number");
                        continue;
                    }
                    break;
                case "number":
                    if (!input.matches("\\d{1,2}")) {
                        System.out.println("Please enter a valid 1 to 2-Digit number");
                        continue;
                    }
                    else{
                        if(Integer.parseInt(input) < 1){
                            System.out.println("Please enter a valid 1 to 2-Digit number");
                            continue;
                        }
                    }
                    break;
                default:
                    break;
            }
            res = input;
            break;
        }
        return res;
    }

    /**
     * Check if the ssn is valid or not
     * @param conn
     * @param ssn
     */
    public static boolean IfValidSSN(Connection conn, String ssn) {
        String sql = """
                    select * from tenant where ssn = ?
                """;
        try (PreparedStatement pstm = conn.prepareStatement(sql)) {
            pstm.setInt(1, Integer.parseInt(ssn));
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) {
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return true;
    }

    /***
     * View Visited Property
     * @param conn
     * @param scanner
     * @param id
     */
    public static void ViewVisitedProperty(Connection conn, Scanner scanner, int id){
        System.out.println();
        System.out.println();
        System.out.println();
        String sql = """
            select prop_id, apt_id, id, name from visits join prospect_tenant on tenent_id = id where id = ? order by prop_id, apt_id
                """;
        System.out.println("Your Visited Property List");
        System.out.println(ViewCompany.generateDashLine(4, 20, "-"));
        String header = String.format("%-20s %-20s %-20s %-20s %n",
        "PROPERTY_ID", "APARTMENT_ID", "TENANT_ID", "TENANT_NAME");
        System.out.println(header);
        System.out.println(ViewCompany.generateDashLine(4,20,"-"));
        try(PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()){
                String prop_id = rs.getString("prop_id");
                String apt_id = rs.getString("apt_id");
                String tenant_id = rs.getString("id");
                String tenant_name = rs.getString("name");
                String row = String.format("%-20s %-20s %-20s %-20s %n",
                prop_id, apt_id, tenant_id, tenant_name);
                System.out.println(row);
            }
        }catch(SQLException e){
            System.out.println(e.getMessage());
        }
        System.out.println(ViewCompany.generateDashLine(4, 20, "-"));
    }

    /**
     * Customer Signup or Update
     * 
     * @param conn
     * @param scanner
     * @param target_id The id of the customer if the customer is not a new customer
     * @param isTenant  True if the customer is a tenant
     */
    public static void CustomerInfoInput(Connection conn, Scanner scanner, String target_id, boolean isTenant) {
        scanner.nextLine();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println("\n==================== EU: Customer Information Input ====================");
        String name = InfoInput(scanner, "Enter Name: ", "name");
        String birth = InfoInput(scanner, "Enter Birth Date(YYYY/MM/DD): ", "birth");
        int option = 0;
        if(!target_id.isEmpty()){
            if(IfPet(conn, Integer.parseInt(target_id))){
                option = 2;
            }else{
                option = 1;
            }
        }
        else{
            while (true) {
                System.out.println("Please Identify Yourself as Either");
                System.out.println("1. Customer");
                System.out.println("2. Pet");
                System.out.println("Please enter your choice (1-2): ");
                while (!scanner.hasNextInt()) {
                    System.out.println("That's not a valid option! Please enter a number between 1 and 2.");
                    scanner.next();
                    System.out.println("Please enter your choice (1-2): ");
                }
                option = scanner.nextInt();
                if (option != 1 && option != 2) {
                    System.out.println("That's not a valid option! Please enter a number between 1 and 2.");
                    continue;
                } else {
                    scanner.nextLine();
                    break;
                }
            }
        }
        String income = "";
        String rental_history = "";
        String crime_history = "";
        String vaccination = "";
        String species = "";
        String ssn = "0";
        if (option == 1) {
            income = InfoInput(scanner, "Enter Income: ", "income");
            rental_history = InfoInput(scanner, "Enter Rental History(Good, Average, Poor): ", "rental_history");
            crime_history = InfoInput(scanner, "Enter Crime History(Severe, Minor, None): ", "crime_history");
            rental_history = rental_history.substring(0, 1).toUpperCase() + rental_history.substring(1).toLowerCase();
            crime_history = crime_history.substring(0, 1).toUpperCase() + crime_history.substring(1).toLowerCase();
            if (isTenant) {
                while(true){
                    ssn = InfoInput(scanner, "Enter SSN(9-Digit): ", "ssn");
                    if(IfValidSSN(conn, ssn)){
                        break;
                    }else{
                        System.out.println("SSN already exists, please enter a valid SSN");
                    }
                }
            }
        } else {
            species = InfoInput(scanner, "Enter Species: ", "species");
            vaccination = InfoInput(scanner, "Enter Vaccination(Yes, No): ", "vaccination");
            species = species.substring(0, 1).toUpperCase() + species.substring(1).toLowerCase();
            vaccination = vaccination.substring(0, 1).toUpperCase() + vaccination.substring(1).toLowerCase();
        }
        System.out.println("=====================================================================");

        // Signup new Prospect Tenant
        if (target_id.isEmpty()) {
            int id = InsertPerspectiveTenant(conn, name, birth, income, rental_history, crime_history, species,
                    vaccination, option);
            if (id == 0) {
                System.out.println("Failed to add new customer");
            } else {
                PrintCustomerSummary(conn, id);
            }
        }
        // Update existing customer
        else {
            UpdateCustomer(conn, name, birth, income, rental_history, crime_history, species, vaccination, ssn, option,
                    Integer.parseInt(target_id), isTenant);
            //PrintCustomerSummary(conn, Integer.parseInt(target_id));
        }

    }

    /**
     * Insert a new customer into the database
     * 
     * @param conn
     * @param name
     * @param birth
     * @param income
     * @param rental_history
     * @param crime_history
     * @param species
     * @param vaccination
     * @param option         1 for customer, 2 for pet
     */
    public static int InsertPerspectiveTenant(Connection conn, String name, String birth, String income,
        String rental_history, String crime_history, String species, String vaccination, int option) {
        String perspective_sql = "INSERT INTO prospect_tenant (name, date_of_birth) VALUES (?, TO_DATE(?, 'yyyy/mm/dd'))";
        try (PreparedStatement pstm = conn.prepareStatement(perspective_sql)) {
            pstm.setString(1, name);
            pstm.setString(2, birth);
            int res = pstm.executeUpdate();
            if (res == 0) {
                System.out.println("Failed to add new customer");
                return 0;
            }

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }

        String id_sql = "SELECT id FROM prospect_tenant WHERE name = ? AND date_of_birth = TO_DATE(?, 'yyyy/mm/dd')";
        int id = 0;
        try (PreparedStatement pstm = conn.prepareStatement(id_sql)) {
            pstm.setString(1, name);
            pstm.setString(2, birth);
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) {
                id = Integer.parseInt(rs.getString("id"));
            } else {
                System.out.println("Failed to get id");
                return 0;
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }

        String customer_sql = "INSERT INTO person (id, month_income, rental_history, crime_history) VALUES (?, ?, ?, ?)";
        String pet_sql = "INSERT INTO pet (id, species, vaccination) VALUES (?, ?, ?)";
        try (PreparedStatement pstm = conn.prepareStatement(option == 1 ? customer_sql : pet_sql)) {
            pstm.setInt(1, id);
            if (option == 1) {
                pstm.setFloat(2, Float.parseFloat(income));
                pstm.setString(3, rental_history);
                pstm.setString(4, crime_history);
            } else {
                pstm.setString(2, species);
                pstm.setString(3, vaccination);
            }
            int res = pstm.executeUpdate();
            if (res == 0) {
                System.out.println("Failed to add new customer");
                return 0;
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return id;
    }

    /***
     * Update the existing customer by id: Perspective Tenant as well as Tenant
     * 
     * @param conn
     * @param name
     * @param birth
     * @param income
     * @param rental_history
     * @param crime_history
     * @param species
     * @param vaccination
     * @param id
     * @param isTenant
     * @return
     */
    public static void UpdateCustomer(Connection conn, String name, String birth, String income, String rental_history,
        String crime_history, String species, String vaccination, String ssn, int option, int id,
        boolean isTenant) {
        String update_prospect_sql = "UPDATE prospect_tenant SET name = ?, date_of_birth = TO_DATE(?, 'yyyy/mm/dd') WHERE id = ?";
        try (PreparedStatement pstm = conn.prepareStatement(update_prospect_sql)) {
            pstm.setString(1, name);
            pstm.setString(2, birth);
            pstm.setInt(3, id);
            int res = pstm.executeUpdate();
            if (res == 0) {
                System.out.println("Failed to update customer");
                return;
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
        String customer_sql = "UPDATE person SET month_income = ?, rental_history = ?, crime_history = ? WHERE id = ?";
        String pet_sql = "UPDATE pet SET species = ?, vaccination = ? WHERE id = ?";
        try (PreparedStatement pstm = conn.prepareStatement(option == 1 ? customer_sql : pet_sql)) {
            if (option == 1) {
                pstm.setFloat(1, Float.parseFloat(income));
                pstm.setString(2, rental_history);
                pstm.setString(3, crime_history);
                pstm.setInt(4, id);
            } else {
                pstm.setString(1, species);
                pstm.setString(2, vaccination);
                pstm.setInt(3, id);
            }
            int res = pstm.executeUpdate();
            if (res == 0) {
                System.out.println("Failed to update customer");
                return;
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
        // Update Tenant
        if (isTenant) {
            String update_tenant_sql = "UPDATE tenant SET ssn = ? WHERE id = ?";
            try (PreparedStatement pstm = conn.prepareStatement(update_tenant_sql)) {
                pstm.setString(1, ssn);
                pstm.setInt(2, id);
                int res = pstm.executeUpdate();
                if (res == 0) {
                    System.out.println("Failed to update customer");
                    return;
                }
            } catch (SQLException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }

        //PrintCustomerSummary(conn, id);
    }

    /**
     * Print the summary of the customer
     * 
     * @param conn
     * @param id
     */
    public static void PrintCustomerSummary(Connection conn, int id) {
        String sql = """
                    select prospect_tenant.id, name, date_of_birth, month_income, rental_history,
                    crime_history, species, vaccination, lease_id, ssn
                    from prospect_tenant
                    left outer join person on prospect_tenant.id = person.id
                    left outer join pet on prospect_tenant.id = pet.id
                    left outer join tenant on prospect_tenant.id = tenant.id
                    where prospect_tenant.id = ?
                """;
        try (PreparedStatement pstm = conn.prepareStatement(sql)) {
            pstm.setInt(1, id);
            ResultSet rs = pstm.executeQuery();
            int numberOfFields = 0;
            String userid = "";
            String name = "";
            String date_of_birth = "";
            String month_income = "";
            String rental_history = "";
            String crime_history = "";
            String species = "";
            String vaccination = "";
            String lease_id = "";
            String ssn = "";
            if (rs.next()) {
                userid = rs.getString("id");
                name = rs.getString("name");
                Date dbDate = rs.getDate("date_of_birth");
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                date_of_birth = dateFormat.format(dbDate);
                month_income = rs.getString("month_income");
                rental_history = rs.getString("rental_history");
                crime_history = rs.getString("crime_history");
                species = rs.getString("species");
                vaccination = rs.getString("vaccination");
                lease_id = rs.getString("lease_id");
                ssn = rs.getString("ssn");
            } else {
                System.out.println("Failed to get customer summary");
                return;
            }
            String header = "";
            String content = "";
            // Person
            if (month_income != null) {
                // Tenant
                if (lease_id != null) {
                    header = String.format(" %-20s %-20s %-20s %-20s %-20s %-20s %-20s %-20s %n",
                            "ID", "NAME", "DATE OF BIRTH", "INCOME", "RENTAL HISTORY", "CRIME HISTORY", "LEASE ID",
                            "SSN");
                    content = String.format(" %-20s %-20s %-20s %-20s %-20s %-20s %-20s %-20s %n",
                            userid, name, date_of_birth, month_income, rental_history, crime_history, lease_id, ssn);
                    numberOfFields = 8;
                }
                // Not Tenant
                else {
                    header = String.format(" %-20s %-20s %-20s %-20s %-20s %-20s %n",
                            "ID", "NAME", "DATE OF BIRTH", "INCOME", "RENTAL HISTORY", "CRIME HISTORY");
                    content = String.format(" %-20s %-20s %-20s %-20s %-20s %-20s %n",
                            userid, name, date_of_birth, month_income, rental_history, crime_history);
                    numberOfFields = 6;
                }
            }
            // Pet
            else {
                // Pet with Tenant
                if (lease_id != null) {
                    header = String.format(" %-20s %-20s %-20s %-20s %-20s %-20s %-20s %n",
                            "ID", "NAME", "DATE OF BIRTH", "SPECIES", "VACCINATION", "LEASE ID", "SSN");
                    content = String.format(" %-20s %-20s %-20s %-20s %-20s %-20s %-20s %n",
                            userid, name, date_of_birth, species, vaccination, lease_id, ssn);
                    numberOfFields = 7;
                }
                // Pet without Tenant
                else {
                    header = String.format(" %-20s %-20s %-20s %-20s %-20s %n",
                            "ID", "NAME", "DATE OF BIRTH", "SPECIES", "VACCINATION");
                    content = String.format(" %-20s %-20s %-20s %-20s %-20s %n",
                            userid, name, date_of_birth, species, vaccination);
                    numberOfFields = 5;
                }
            }
            System.out.println();
            System.out.println();
            System.out.println();
            System.out.println("\nCustomer Summary");
            System.out.println(ViewCompany.generateDashLine(numberOfFields, 20, "-"));
            System.out.println(header);
            System.out.println(ViewCompany.generateDashLine(numberOfFields, 20, "-"));
            System.out.println(content);
            System.out.println(ViewCompany.generateDashLine(numberOfFields, 20, "-"));
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Sechedule a visit
     * 
     * @param conn
     * @param scanner
     * @param id
     */
    public static void SecheduleVisit(Connection conn, Scanner scanner, int id) {
        System.out.println();
        System.out.println();
        System.out.println();
        scanner.nextLine();
        System.out.println("\n==================== EU: Visit A Apartment ========================");
        System.out.println("NOTE: We allow you to sechedule a visit for an apartment is leased");
        System.out.println("NOTE: You can only sechedule a visit for an apartment that you have not visited");
        while (true) {
            String prop_id = InfoInput(scanner, "Enter Property ID:(or 'q' to quit) ", "id");
            if (prop_id.equalsIgnoreCase("q")) {
                break;
            }
            String apt_id = InfoInput(scanner, "Enter Apartment ID:(or 'q' to quit) ", "id");
            if (apt_id.equalsIgnoreCase("q")) {
                break;
            }
            String sql = """
                        INSERT INTO visits (prop_id, apt_id, tenent_id) VALUES (?, ?, ?)
                    """;
            try (PreparedStatement pstm = conn.prepareStatement(sql)) {
                pstm.setInt(1, Integer.parseInt(prop_id));
                pstm.setInt(2, Integer.parseInt(apt_id));
                pstm.setInt(3, id);
                int res = pstm.executeUpdate();
                if (res == 0) {
                    System.out.println("Failed to sechedule a visit");
                    break;
                } else {
                    System.out.println("Sechedule a visit successfully");
                    break;
                }

            } catch (SQLException e) {
                System.out.println("Please enter a valid property id and apartment id, or you have already secheduled a visit for this apartment");
                continue;
            }
        }
        System.out.println("=====================================================================");
        System.out.println();
    }

    /**
     * Sign a Lease
     * @param conn
     * @param scanner
     * @param id
     */
    public static int SignLease(Connection conn, Scanner scanner, int id) {
        System.out.println();
        System.out.println();
        System.out.println();
        scanner.nextLine();
        float deposit = 0;
        System.out.println("\n==================== EU: Sign A Lease =======================");
        while (true) {
            String prop_id = InfoInput(scanner, "Enter Property ID:(or 'q' to quit) ", "id");
            if (prop_id.equalsIgnoreCase("q")) {
                break;
            }
            String apt_id = InfoInput(scanner, "Enter Apartment ID:(or 'q' to quit) ", "id");
            if (apt_id.equalsIgnoreCase("q")) {
                break;
            }
            String check_sql = """
                        select * from apartment where prop_id = ? and apt_id = ?
                    """;
            try (PreparedStatement pstm = conn.prepareStatement(check_sql)) {
                pstm.setInt(1, Integer.parseInt(prop_id));
                pstm.setInt(2, Integer.parseInt(apt_id));
                ResultSet rs = pstm.executeQuery();
                if (!rs.next()) {
                    System.out.println("Apartment not found");
                    continue;
                } else {
                    deposit = rs.getFloat("rent");
                }
            } catch (SQLException e) {
                System.out.println("Error: " + e.getMessage());
            }
            check_sql = """
                        select * from lease where prop_id = ? and apt_id = ?
                    """;
            try (PreparedStatement pstm = conn.prepareStatement(check_sql)) {
                pstm.setInt(1, Integer.parseInt(prop_id));
                pstm.setInt(2, Integer.parseInt(apt_id));
                ResultSet rs = pstm.executeQuery();
                if (rs.next()) {
                    System.out.println("Apartment already leased");
                    deposit = 0;
                    continue;
                }
            } catch (SQLException e) {
                System.out.println("Error: " + e.getMessage());
            }
            ViewCompany.ViewSummary(conn, new StringBuilder(), prop_id, apt_id, null, false);
            System.out.println();
            System.out.println("Term: 12 Month");
            System.out.println("Deposit: " + deposit);
            String date_option = InfoInput(scanner, "Do you want to input the Start Date? If not System will use today's date as the start date(Yes, No): ", "confirm");
            String start_date = "";
            if(date_option.equalsIgnoreCase("Yes")){
                start_date = InfoInput(scanner, "Enter Start Date(YYYY/MM/DD): ", "birth");
            }
            else{
                LocalDate currentDate = LocalDate.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
                start_date = currentDate.format(formatter);
            }

            String confirm = InfoInput(scanner, "Confirm to sign the lease?(Yes, No): ", "confirm");
            if (confirm.equalsIgnoreCase("No")) {
                break;
            }
            String ssn = "";
            while(true){
                ssn = InfoInput(scanner, "Enter SSN(9-Digit): ", "ssn");
                if(IfValidSSN(conn, ssn)){
                    break;
                }else{
                    System.out.println("SSN already exists, please enter a valid SSN");
                }
            }
            //Start Insertion
            int lease_id = 0;
            String call = "{ ? = call sign_lease(?, ?, ?, ?, ?, ?) }";
            try(CallableStatement stmt = conn.prepareCall(call)){
                stmt.registerOutParameter(1, Types.INTEGER);
                stmt.setInt(2, id);
                stmt.setInt(3, Integer.parseInt(prop_id));
                stmt.setInt(4, Integer.parseInt(apt_id));
                stmt.setString(5, start_date);
                stmt.setFloat(6, deposit);
                stmt.setInt(7, Integer.parseInt(ssn));
                stmt.execute();
                lease_id = stmt.getInt(1);
                if(lease_id == 0){
                    System.out.println("Failed to sign the lease, Please visit the apartment first");
                    System.out.println("=====================================================================");
                    System.out.println();
                    return 0;
                }
                else{
                    System.out.println("Sign the lease successfully");
                    System.out.println("Your lease id is: " + lease_id);
                    System.out.println("=====================================================================");
                    System.out.println();
                    return lease_id;
                }

            }catch(SQLException e){
                System.out.println("Error: " + e.getMessage());
            }
            
        }
        System.out.println("=====================================================================");
        System.out.println();
        return 0;
    }


   /**
    * Select Amenity
    * @param conn
    * @param scanner
    * @param lease_id
    */
    public static void SelectAmenity(Connection conn, Scanner scanner, int lease_id){
        String sql = """
                    select * from lease where lease_id = ?
                """;
        int prop_id = 0;
        int apt_id = 0;
        try(PreparedStatement pstm = conn.prepareStatement(sql)){
            pstm.setInt(1, lease_id);
            ResultSet rs = pstm.executeQuery();
            if(rs.next()){
                prop_id = rs.getInt("prop_id");
                apt_id = rs.getInt("apt_id");
            }else{
                System.out.println("Lease not found");
                return;
            }
        }catch(SQLException e){
            System.out.println("Error: " + e.getMessage());
        }
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println("\n==================== EU: Amenity Summary =======================");
        System.out.println("Common Amenity Available ");
        ViewCompany.ViewPropertyCommon(conn, new StringBuilder(), Integer.toString(prop_id));
        ViewCompany.ViewApartmentPrivate(conn, new StringBuilder(), Integer.toString(prop_id),Integer.toString(apt_id));
        System.out.println("=====================================================================");
        System.out.println();
        while(true){
            String amenity_id = InfoInput(scanner, "Enter Amenity ID:(or 'q' to quit) ", "id");
            if(amenity_id.equalsIgnoreCase("q")){
                break;
            }
            String check_availble_sql = """
                with amenity_summary as (
                    select prop_id, address, apt_id, rent, amenity_id from (select * from property natural join apartment) natural join apt_private
                    union
                    select prop_id, address, apt_id, rent, amenity_id from (select * from property natural join apartment) natural join prop_common
                   )
                select * from amenity_summary where prop_id = ? and apt_id = ? and amenity_id = ?
                """;
            try(PreparedStatement pstm = conn.prepareStatement(check_availble_sql)){
                pstm.setInt(1, prop_id);
                pstm.setInt(2, apt_id);
                pstm.setInt(3, Integer.parseInt(amenity_id));
                ResultSet rs = pstm.executeQuery();
                if(rs.next()){
                    String insert_sql = """
                        insert into select_amenity (amenity_id, lease_id) values (?, ?)
                    """;
                    try(PreparedStatement pstm2 = conn.prepareStatement(insert_sql)){
                        pstm2.setInt(2, lease_id);
                        pstm2.setInt(1, Integer.parseInt(amenity_id));
                        int res = pstm2.executeUpdate();
                        if(res == 0){
                            System.out.println("Failed to add amenity");
                            continue;
                        }else{
                            System.out.println("Add amenity successfully");
                            continue;
                        }
                    }catch(SQLException e){
                        System.out.println("Amenity already added");
                    }
                }else{
                    System.out.println("Amenity not Available at this apartment"); 
                    continue;
                }
            }catch(SQLException e){
                System.out.println("Error: " + e.getMessage());
            }
        }

    }
}
