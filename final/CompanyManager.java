import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedWriter;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.Scanner;

public class CompanyManager {

    private static final String[] STREET_NAMES = {
            "Maple", "Oak", "Pine", "Cedar", "Elm",
            "Ash", "Birch", "Cherry", "Chestnut", "Dogwood",
            "Fir", "Hickory", "Juniper", "Linden", "Magnolia",
            "Olive", "Peach", "Pear", "Plum", "Quince",
            "Redwood", "Sequoia", "Spruce", "Teak", "Walnut",
            "Willow", "Yew", "Acacia", "Alder", "Aspen",
            "Banyan", "Beech", "Cypress", "Ebony", "Hemlock",
            "Jasmine", "Koa", "Laurel", "Myrtle", "Nectarine",
            "Palm", "Poplar", "Sycamore", "Tulip", "Zelkova"
    };
    private static final Random RANDOM = new Random();

    // Private constructor to prevent instantiation
    private CompanyManager() {
        throw new AssertionError("Utility class cannot be instantiated");
    }

    /**
     * Main page for Company Manager
     * 
     * @param conn
     * @param scanner
     */
    public static void CompanyManagerPage(Connection conn, Scanner scanner) {
        int option;
        do {
            System.out.println();
            System.out.println();
            System.out.println();
            System.out.println("\n====================== EU: Company Manager Page =========================");
            System.out.println("1. View Property, Apartment, and Amenity Summary");
            System.out.println("2. Add New Property");
            System.out.println("3. Logout");
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
                    ViewCompany.ViewManual(conn, true, scanner);
                    break;
                case 2:
                    NewPropertyPage(conn, scanner);
                    break;
                case 3:
                    System.out.println("Logging out...");
                    break;
                default:
                    System.out.println("That's not a valid option! Please enter a number between 1 and 3.");
                    break;
            }
        } while (option != 3);
    }

    /**
     * New Property Page for Company Manager
     * 
     * @param conn
     * @param scanner
     */
    public static void NewPropertyPage(Connection conn, Scanner scanner) {
        while (true) {
            System.out.println();
            System.out.println();
            System.out.println();
            scanner.nextLine();
            System.out.println("\n====================== EU: Add Property Page =========================");
            String auto_generate = Customer.InfoInput(scanner, "Do you want to automatically generate data? (Yes/No)",
                    "confirm");
            if (auto_generate.equalsIgnoreCase("Yes")) {
                System.out.println("=====================================================================");
                System.out.println();
                boolean res = PropertyAuto(conn, scanner);
                if (res) {
                    System.out.println("Property is successfully added.");
                } else {
                    System.out.println("Property is not added.");
                }
                break;
            } else {
                System.out.println("=====================================================================");
                System.out.println();
                boolean res = PropertyManual(conn, scanner);
                if (res) {
                    System.out.println("Property is successfully added.");
                } else {
                    System.out.println("Property is not added.");
                }
                break;
            }
        }
        System.out.println("Back to Company Manager Page...");
    }

    /**
     * View Common Amenity
     * 
     * @param conn
     * @param scanner
     */
    public static void ViewCommonAmenity(Connection conn, Scanner scanner) {
        System.out.println();
        System.out.println();
        System.out.println();
        String sql = "select * from amenity natural join common_amenity";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            System.out.println("Common Amenity");
            System.out.println(ViewCompany.generateDashLine(5, 20, "-"));
            String header = String.format("%-20s %-20s %-20s %-20s %-20s %n",
                    "Amenity_ID", "Common_Amenity", "COST", "HOUR", "CAPACITY");
            System.out.println(header);
            System.out.println(ViewCompany.generateDashLine(5, 20, "-"));
            while (rs.next()) {
                String amenity_id = rs.getString("amenity_id");
                String amenity_name = rs.getString("description");
                String cost = rs.getString("cost");
                String hour = rs.getString("hour");
                String capacity = rs.getString("capacity");
                String row = String.format("%-20s %-20s %-20s %-20s %-20s %n",
                        amenity_id, amenity_name, cost, hour, capacity);
                System.out.println(row);
            }
            System.out.println(ViewCompany.generateDashLine(5, 20, "-"));
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /***
     * View Private Amenity
     * 
     * @param conn
     * @param scanner
     */
    public static void ViewPrivateAmenity(Connection conn, Scanner scanner) {
        System.out.println();
        System.out.println();
        System.out.println();
        String sql = "select * from amenity natural join private_amenity";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            System.out.println("Private Amenity");
            System.out.println(ViewCompany.generateDashLine(5, 20, "-"));
            String header = String.format("%-20s %-20s %-20s %-20s %-20s %n",
                    "Amenity_ID", "Private_Amenity", "COST", "FEATURE", "WARRANTY");
            System.out.println(header);
            System.out.println(ViewCompany.generateDashLine(5, 20, "-"));
            while (rs.next()) {
                String amenity_id = rs.getString("amenity_id");
                String amenity_name = rs.getString("description");
                String cost = rs.getString("cost");
                String hour = rs.getString("feature");
                String capacity = rs.getString("warranty");
                String row = String.format("%-20s %-20s %-20s %-20s %-20s %n",
                        amenity_id, amenity_name, cost, hour, capacity);
                System.out.println(row);
            }
            System.out.println(ViewCompany.generateDashLine(5, 20, "-"));
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /***
     * Auto generate property
     * 
     * @param conn
     * @param scanner
     */
    public static boolean PropertyAuto(Connection conn, Scanner scanner) {
        String street_address = "";
        String sql = "";
        while (true) {
            street_address = generateRandomStreetAddress();
            sql = "select * from property where address = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, street_address);
                ResultSet rs = pstmt.executeQuery();
                if (!rs.next()) {
                    break;
                }
            } catch (SQLException e) {
                System.out.println(e.getMessage());
                return false;
            }
        }
        // Insert Property
        System.out.println("\n====================== EU Add Property: Property =========================");
        int property_id = InsertProperty(conn, street_address);
        System.out.println("=====================================================================");
        if (property_id == 0) {
            return false;
        }
        // Insert Apartment
        System.out.println("\n====================== EU Add Property: Apartment =========================");
        int num_apartment = Integer
                .parseInt(Customer.InfoInput(scanner, "How many apartments does this property have?(1-99)", "number"));
        for (int i = 1; i <= num_apartment; i++) {
            float rent = 1000 + RANDOM.nextFloat() * (2000 - 1000);
            float apt_size = 600 + RANDOM.nextFloat() * (1600 - 500);
            int num_bedroom = RANDOM.nextInt(5);
            int num_bathroom = RANDOM.nextInt(5);
            int apt_id = i;
            int apartment_id = InsertApartment(conn, property_id, apt_id, rent, apt_size, num_bedroom, num_bathroom);
            if (apartment_id == 0) {
                return false;
            }
        }
        System.out.println("=====================================================================");
        // Common Amenity
        System.out.println("\n====================== EU Add Property: Common Amenity =========================");
        int num_common_amenity_available = NumAmenity(conn, "common");
        int num_common_amenity = InputNumAmenity(scanner, num_common_amenity_available, "common");
        if (num_common_amenity != 0) {
            int[] common_amenity = RandomAmenity(conn, num_common_amenity, "common");
            for (int i = 0; i < num_common_amenity; i++) {
                sql = "insert into prop_common values (?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(2, property_id);
                    pstmt.setInt(1, common_amenity[i]);
                    pstmt.executeUpdate();
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                    return false;
                }
                System.out.println("Property: " + property_id + " Common Amenity: " + common_amenity[i]
                        + " is successfully added.");
            }
        }
        System.out.println("=====================================================================");

        // Private Amenity
        for (int i = 1; i <= num_apartment; i++) {
            System.out.println("\n====================== EU Add Property: Private Amenity =========================");
            System.out.println("Property: " + property_id + ", Apartment: " + i);
            int num_private_amenity_available = NumAmenity(conn, "private");
            int num_private_amenity = InputNumAmenity(scanner, num_private_amenity_available, "private");
            if (num_private_amenity == 0) {
                continue;
            }
            int[] private_amenity = RandomAmenity(conn, num_private_amenity, "private");
            for (int j = 0; j < num_private_amenity; j++) {
                sql = "insert into apt_private values (?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(2, property_id);
                    pstmt.setInt(3, i);
                    pstmt.setInt(1, private_amenity[j]);
                    pstmt.executeUpdate();
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                    return false;
                }
                System.out.println("Property: " + property_id + ", Apartment: " + i + ", Private Amenity: "
                        + private_amenity[j] + " is successfully added.");
            }
            System.out.println("=====================================================================");
        }
        return true;
    }

    
    /***
     * Manual generate property
     * @param conn
     * @param scanner
     */
    public static boolean PropertyManual(Connection conn, Scanner scanner){
        String street_address = "";
        String sql = "";
        while (true) {
            street_address = ProperyInfoInput(scanner, "Please enter the address of the property in a format of 'Street_Number Street_Name St': ", "address");
            sql = "select * from property where address = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, street_address);
                ResultSet rs = pstmt.executeQuery();
                if (!rs.next()) {
                    break;
                }
                else{
                    System.out.println("This property already exists. Try different address.");
                    continue;
                }
            } catch (SQLException e) {
                System.out.println(e.getMessage());
                return false;
            }
        }
        // Insert Property
        System.out.println("\n====================== EU Add Property: Property =========================");
        int property_id = InsertProperty(conn, street_address);
        System.out.println("=====================================================================");
        if (property_id == 0) {
            return false;
        }
        // Insert Apartment
        System.out.println("\n====================== EU Add Property: Apartment =========================");
        int num_apartment = Integer
                .parseInt(ProperyInfoInput(scanner, "How many apartments does this property have?(1-99): ", "number_not_zero"));
        for (int i = 1; i <= num_apartment; i++) {
            System.out.println("Apartment: " + i);
            float rent = Float.parseFloat(ProperyInfoInput(scanner, "Please enter the rent of the apartment(float): ", "rent"));
            float apt_size = Float.parseFloat(ProperyInfoInput(scanner, "Please enter the size of the apartment(float): ", "rent"));
            int num_bedroom = Integer.parseInt(ProperyInfoInput(scanner, "Please enter the number of bedrooms of the apartment(0-99): ", "number_zero"));
            int num_bathroom = Integer.parseInt(ProperyInfoInput(scanner, "Please enter the number of bathrooms of the apartment(0-99): ", "number_zero"));
            int apt_id = i;
            int apartment_id = InsertApartment(conn, property_id, apt_id, rent, apt_size, num_bedroom, num_bathroom);
            if (apartment_id == 0) {
                return false;
            }
            System.out.println();
        }
        System.out.println("=====================================================================");

        // Common Amenity
        ViewCommonAmenity(conn, scanner);
        System.out.println("\n====================== EU Add Property: Common Amenity =========================");
        int num_common_amenity_available = NumAmenity(conn, "common");
        int num_common_amenity = InputNumAmenity(scanner, num_common_amenity_available, "common");
        scanner.nextLine();
        if (num_common_amenity != 0) {
            int[] common_amenity = ChooseAmenity(conn, scanner, "Common", num_common_amenity, property_id, 0);
            for (int i = 0; i < num_common_amenity; i++) {
                if(common_amenity[i] == 0){
                    continue;
                }
                sql = "insert into prop_common values (?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(2, property_id);
                    pstmt.setInt(1, common_amenity[i]);
                    pstmt.executeUpdate();
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                    return false;
                }
                System.out.println("Property: " + property_id + " Common Amenity: " + common_amenity[i]
                        + " is successfully added.");
            }
        }
        System.out.println("=====================================================================");


        // Private Amenity
        ViewPrivateAmenity(conn, scanner);
        for (int i = 1; i <= num_apartment; i++) {
            System.out.println("\n====================== EU Add Property: Private Amenity =========================");
            System.out.println("Property: " + property_id + ", Apartment: " + i);
            int num_private_amenity_available = NumAmenity(conn, "private");
            int num_private_amenity = InputNumAmenity(scanner, num_private_amenity_available, "private");
            if (num_private_amenity == 0) {
                continue;
            }
            scanner.nextLine();
            int[] private_amenity = ChooseAmenity(conn, scanner, "Private", num_private_amenity, property_id, i);
            for (int j = 0; j < num_private_amenity; j++) {
                if(private_amenity[j] == 0){
                    continue;
                }
                sql = "insert into apt_private values (?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(2, property_id);
                    pstmt.setInt(3, i);
                    pstmt.setInt(1, private_amenity[j]);
                    pstmt.executeUpdate();
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                    return false;
                }
                System.out.println("Property: " + property_id + ", Apartment: " + i + ", Private Amenity: "
                        + private_amenity[j] + " is successfully added.");
            }
            System.out.println("=====================================================================");
        }
        return false;
    }
    
    /**
     * Choose Amenity
     * @param conn
     * @param scanner
     * @param amenity_type
     * @param num_amenity
     * @return a list of amenity_id
     */
    public static int[] ChooseAmenity(Connection conn, Scanner scanner, String amenity_type, int num_amenity, int property_id, int apt_id){
        int[] amenity_id = new int[num_amenity];
        String sql = amenity_type.equals("Common") ? "select * from common_amenity where amenity_id=?" : "select * from private_amenity where amenity_id=?";
        String check_conflict_sql = amenity_type.equals("Common") ? "select * from prop_common where amenity_id=? and prop_id=?" : "select * from apt_private where amenity_id=? and prop_id=? and apt_id=?";
        for(int i = 0; i < num_amenity; i++){
            System.out.println(amenity_type + " Amenity: " + (i+1) + "/" + num_amenity);
            String choice = ProperyInfoInput(scanner, "Please enter the id of the amenity:(or 'q' to escape) ", "id");
            if(choice.equalsIgnoreCase("q")){
                continue;
            }
            //Check if amenity in array
            if(contains(amenity_id, Integer.parseInt(choice))){
                System.out.println("This amenity already selected. Try different amenity_id.");
                i--;
                continue;
            }
            //Check if amenity exists
            int amenity = Integer.parseInt(choice);
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, amenity);
                ResultSet rs = pstmt.executeQuery();
                if (!rs.next()) {
                    System.out.println("This amenity does not exist. Try different amenity_id.");
                    i--;
                    continue;
                }

            } catch (SQLException e) {
                System.out.println(e.getMessage());
                i--;
                continue;
            }
            //Check if amenity already selected
            if(amenity_type.equals("Common")){
                try (PreparedStatement pstmt = conn.prepareStatement(check_conflict_sql)) {
                    pstmt.setInt(1, amenity);
                    pstmt.setInt(2, property_id);
                    ResultSet rs = pstmt.executeQuery();
                    if (rs.next()) {
                        System.out.println("This amenity already selected. Try different amenity_id.");
                        i--;
                        continue;
                    }
                    else{
                        amenity_id[i] = amenity;
                    }
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                    i--;
                    continue;
                }
            }
            else{
                try (PreparedStatement pstmt = conn.prepareStatement(check_conflict_sql)) {
                    pstmt.setInt(1, amenity);
                    pstmt.setInt(2, property_id);
                    pstmt.setInt(3, apt_id);
                    ResultSet rs = pstmt.executeQuery();
                    if (rs.next()) {
                        System.out.println("This amenity already selected. Try different amenity_id.");
                        i--;
                        continue;
                    }
                    else{
                        amenity_id[i] = amenity;
                    }
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                    i--;
                    continue;
                }
            }
        }
        return amenity_id;
    }

    /**
     * Check if the array contains the value
     * @param array
     * @param value
     * @return
     */
    public static boolean contains(int[] array, int value) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == value) {
                return true; // Value found in the array
            }
        }
        return false; // Value not found in the array
    }




    /**
     * Generate Random Address
     */
    public static String generateRandomStreetAddress() {
        String street = STREET_NAMES[RANDOM.nextInt(STREET_NAMES.length)];
        int number = RANDOM.nextInt(999) + 1;

        return number + " " + street + " St";
    }

    /***
     * Insert Property
     * 
     * @param conn
     * @return property_id
     */
    public static int InsertProperty(Connection conn, String street_address) {
        int property_id = 0;
        String sql = "INSERT INTO property (address) VALUES (?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, street_address);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return 0;
        }
        String id_sql = "select prop_id from property where address = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(id_sql)) {
            pstmt.setString(1, street_address);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                property_id = rs.getInt("prop_id");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return 0;
        }
        System.out.println("Property: " + property_id + ", Adress: " + street_address + " is successfully added.");
        return property_id;
    }

    /***
     * Insert Apartment
     * 
     * @param conn
     * @param property_id
     * @param apt_id
     * @param apt_size
     * @param rent
     * @param num_bedroom
     * @param num_bathroom
     * @return apartment_id is successful, 0 otherwise
     */
    public static int InsertApartment(Connection conn, int property_id, int apt_id, float rent, float apt_size,
            int num_bedroom, int num_bathroom) {
        int apartment_id = 0;
        String sql = "INSERT INTO apartment (prop_id, apt_id, rent, apt_size, bedroom_num, bathroom_num) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, property_id);
            pstmt.setInt(2, apt_id);
            pstmt.setFloat(3, rent);
            pstmt.setFloat(4, apt_size);
            pstmt.setInt(5, num_bedroom);
            pstmt.setInt(6, num_bathroom);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            String delete_sql = "delete from property where prop_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(delete_sql)) {
                pstmt.setInt(1, property_id);
                pstmt.executeUpdate();
            } catch (SQLException e2) {
                System.out.println(e2.getMessage());
            }
            return 0;
        }
        String id_sql = "select apt_id from apartment where prop_id = ? and apt_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(id_sql)) {
            pstmt.setInt(1, property_id);
            pstmt.setInt(2, apt_id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                apartment_id = rs.getInt("apt_id");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return 0;
        }
        System.out.println("Property: " + property_id + ", Apartment: " + apartment_id + " is successfully added.");
        return apartment_id;
    }

    /***
     * Number of Amenity
     */
    public static int NumAmenity(Connection conn, String amenity_type) {
        int num_amenity = 0;
        String sql = amenity_type.equals("common") ? "select count(*) as num_amenity from common_amenity"
                : "select count(*) as num_amenity from private_amenity";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                num_amenity = rs.getInt("num_amenity");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return num_amenity;
    }

    /***
     * Input number of Amenity
     */
    public static int InputNumAmenity(Scanner scanner, int num_amenity, String amenity_type) {
        int num = 0;
        System.out.println(num_amenity + " " + amenity_type + " amenity are available.");
        while (true) {
            System.out.print("How many " + amenity_type + " amenity do you want to add? (0-" + num_amenity + "):");
            while (!scanner.hasNextInt()) {
                System.out
                        .println("That's not a valid option! Please enter a number between 0 and " + num_amenity + ".");
                scanner.next();
            }
            num = scanner.nextInt();
            if (num <= num_amenity) {
                break;
            } else {
                System.out
                        .println("That's not a valid option! Please enter a number between 1 and " + num_amenity + ".");
            }
        }
        return num;
    }

    /***
     * Randomly Select Amenity
     * 
     * @param conn
     * @param num_amenity
     * @param amenity_type
     * @return array of amenity_id
     */
    public static int[] RandomAmenity(Connection conn, int num_amenity, String amenity_type) {
        String common_random_sql = """
                SELECT amenity_id FROM (
                    SELECT amenity_id FROM common_amenity
                    ORDER BY DBMS_RANDOM.VALUE
                )
                WHERE ROWNUM <= ?
                """;
        String private_random_sql = """
                SELECT amenity_id FROM (
                    SELECT amenity_id FROM private_amenity
                    ORDER BY DBMS_RANDOM.VALUE
                )
                WHERE ROWNUM <= ?
                """;
        int[] amenity_id = new int[num_amenity];
        String sql = amenity_type.equals("common") ? common_random_sql : private_random_sql;
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, num_amenity);
            ResultSet rs = pstmt.executeQuery();
            int i = 0;
            while (rs.next()) {
                amenity_id[i] = rs.getInt("amenity_id");
                i++;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return amenity_id;
    }



    /**
     * Customer Input Validation
     * 
     * @param scanner
     * @param instruction
     * @param type
     * 
     */
    public static String ProperyInfoInput(Scanner scanner, String instruction, String type) {
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
                        System.out.println("Please enter a valid id");
                        continue;
                    }
                    break;
                case "rent":
                    if (!input.matches("\\d+(\\.\\d+)?")) {
                        System.out.println("Please enter a valid input");
                        continue;
                    }
                    break;
                case "confirm":
                    if (!input.equalsIgnoreCase("Yes") && !input.equalsIgnoreCase("No")) {
                        System.out.println("Please enter a valid option(Yes, No)");
                        continue;
                    }
                    break;
                case "number_not_zero":
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
                case "number_zero":
                    if (!input.matches("\\d{1,2}")) {
                        System.out.println("Please enter a valid 1 to 2-Digit number");
                        continue;
                    }
                    break;
                case "address":
                    if (!input.matches("\\d{1,5}\\s[A-Za-z]+\\sSt")) {
                        System.out.println("Please enter a valid address in a format of 'Street_Number Street_Name St'");
                        continue;
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
}
