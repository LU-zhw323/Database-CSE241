import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.Scanner;
import java.util.Arrays;


/*** ViewCompany.java
 * @Function ViewManual: Main View Page of the Company: Property, Apartment, Lease, Common Amenity, Private Amenity
 * @Function ViewSummary: View Summary of Property and Apartment
 */
public final class ViewCompany {

    // Private constructor to prevent instantiation
    private ViewCompany() {
        throw new AssertionError("Utility class cannot be instantiated");
    }

    /***
     * Utility method to Generate a dash line of the same length as the input string
     * @param numberOfFields
     * @param fieldWidth
     * @param dashChar
     */
    public static String generateDashLine(int numberOfFields, int fieldWidth, String dashChar) {
        int totalLength = numberOfFields * fieldWidth - 1;
        return String.format("%0" + totalLength + "d", 0).replace("0", dashChar);
    }


    /***
     * Utility method to export summary of properties and apartments to a text file
     * @param conn
     */
    public static void OutputTable(StringBuilder sb){
        // Specify the file path
        String filePath = "PropertySummary.txt";

        // Use try-with-resources for automatic resource management
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            // Write the contents of StringBuilder to the file
            writer.write(sb.toString());
        } catch (IOException e) {
            System.out.println("IOException: " + e);
        }
        System.out.println("The report has been exported to " + filePath);
    }
    

    /***
     * Utility method to print a summary table of all properties and apartments and their availabilities
     * @param conn
     */
    public static void ViewSummary(Connection conn, StringBuilder sb, String prop, String apt, String lease, boolean showlease){
        sb.setLength(0);
        sb.append("\nPROPERTY & APARTMENT SUMMARY TABLE\n");
        String sql = """
            select * from (view_apartment_lease(?,?,?))
                """;
        try(PreparedStatement pstmt = conn.prepareStatement(sql);
        ){
            pstmt.setString(1, prop);
            pstmt.setString(2, apt);
            pstmt.setString(3, lease);
            ResultSet rs = pstmt.executeQuery();
            // Print table header
            String header = String.format("%-20s %-20s %-20s %-20s %-20s %-20s %-20s %-20s %n",
            "PROPERTY_ID", "ADDRESS", "APARTMENT_ID", "RENT",
            "SIZE", "BEDROOMS", "BATHROOMS", showlease? "LEASE_ID":"AVAILABLE");
            sb.append(generateDashLine(8,20,"-")).append("\n");
            sb.append(header);
            // Iterate through the ResultSet
            String currentAddress = "";
            while (rs.next()) {
                // Retrieve values from the ResultSet
                String propAddress = rs.getString("ADDRESS");
                String propID = rs.getString("PROP_ID");
                if(!propAddress.equals(currentAddress)){
                    currentAddress = propAddress;
                    sb.append(generateDashLine(8,20,"-")).append("\n");
                }
                else{
                    propAddress = "";
                    propID = "";
                }
                String aptID = rs.getString("APT_ID");
                double rent = rs.getDouble("RENT");
                int size = rs.getInt("APT_SIZE");
                int bedrooms = rs.getInt("BEDROOM_NUM");
                int bathrooms = rs.getInt("BATHROOM_NUM");
                String leaseID = rs.getString("LEASE_ID");
                String available = "NO";
                if(leaseID == null){
                    available = "YES";
                }
                
                // Print formatted result
                sb.append(String.format("%-20s %-20s %-20s %-20.2f %-20d %-20d %-20d %-20s %n",
                        propID,
                        propAddress,
                        aptID,
                        rent,
                        size,
                        bedrooms,
                        bathrooms,
                        showlease? leaseID :available));
            }
            sb.append(generateDashLine(8,20,"-")).append("\n");
            sb.append("\n");
            System.out.println();
            System.out.println();
            System.out.println();
            System.out.println(sb.toString());
            pstmt.close();
        }
        catch(SQLException e){
            System.out.println("SQLException: " + e + "\n");
        }  
    }

    
    /***
     * Utility method to print a summary table of all properties and their common amenities
     * @param conn
     */
    public static void ViewPropertyCommon(Connection conn, StringBuilder sb, String prop){
        sb.setLength(0);
        sb.append("\nPROPERTY & Common Amenities SUMMARY TABLE\n");
        String sql = """
            select * from ((view_property_common(?)))
                """;
        try(PreparedStatement stmt = conn.prepareStatement(sql);
        ){
            stmt.setString(1, prop);
            ResultSet rs = stmt.executeQuery();
            // Print table header
            String header = String.format("%-20s %-20s %-20s %-20s %-20s %-20s %-20s %n",
                    "PROPERTY_ID","ADDRESS", "AMENITY_ID", "COMMON_AMENITY",
                    "COST","HOUR", "CAPACITY");
            sb.append(generateDashLine(7,20,"-")).append("\n");
            sb.append(header);
            
            // Iterate through the ResultSet
            String currentAddress = "";
            while (rs.next()) {
                // Retrieve values from the ResultSet
                String propAddress = rs.getString("ADDRESS");
                String propID = rs.getString("PROP_ID");
                if(!propAddress.equals(currentAddress)){
                    currentAddress = propAddress;
                    sb.append(generateDashLine(7,20,"-")).append("\n");
                }
                else{
                    propAddress = "";
                    propID = "";
                }
                String commonAmenityID = rs.getString("AMENITY_ID");
                String commonAmenity = rs.getString("COMMON_AMENITY");
                int commonAmenityHour = rs.getInt("COMMON_AMENITY_HOUR");
                double commonAmenityCost = rs.getDouble("COMMON_AMENITY_COST");
                int commonAmenityCapacity = rs.getInt("COMMON_AMENITY_CAPACITY");
                
                // Print formatted result
                sb.append(String.format("%-20s %-20s %-20s %-20s %-20.2f %-20d %-20d %n",
                        propID,
                        propAddress,
                        commonAmenityID,
                        commonAmenity,
                        commonAmenityCost,
                        commonAmenityHour,
                        commonAmenityCapacity));
            }
            sb.append(generateDashLine(7,20,"-")).append("\n");
            sb.append("\n");
            System.out.println(sb.toString());
            stmt.close();
        }
        catch(SQLException e){
            System.out.println("SQLException: " + e + "\n");
        }  
            
    }

    /**
     * Utility method to print a summary table of all apartments and their private amenities
     * @param conn
     */
    public static void ViewApartmentPrivate(Connection conn, StringBuilder sb, String prop, String apt){
        sb.setLength(0);
        sb.append("\nAPARTMENT & PRIVATE Amenities SUMMARY TABLE\n");
        String sql = """
            select * from (view_apartment_private(?,?))
                    """;
        try(PreparedStatement stmt = conn.prepareStatement(sql);
        ){
            stmt.setString(1, prop);
            stmt.setString(2, apt);
            ResultSet rs = stmt.executeQuery();
            // Print table header
            String header = String.format("%-20s %-20s %-20s %-20s %-20s %-20s %-20s %-20s %n",
                    "PROPERTY_ID","ADDRESS", "APARTMENT_ID","AMENITY_ID","PRIVATE_AMENITY", "COST",
                    "FEATURE", "WARRENTY");
            sb.append(generateDashLine(8,20,"-")).append("\n");
            sb.append(header);
            
            // Iterate through the ResultSet
            String currentAddress = "";
            String currentApt = "";
            while (rs.next()) {
                // Retrieve values from the ResultSet
                String propAddress = rs.getString("ADDRESS");
                String aptID = rs.getString("APT_ID");
                if(aptID.equals(currentApt) && propAddress.equals(currentAddress)){
                    aptID = "";
                }
                else{
                    currentApt = aptID;
                }
                String propID = rs.getString("PROP_ID");
                if(!propAddress.equals(currentAddress)){
                    currentAddress = propAddress;
                    sb.append(generateDashLine(8,20,"-")).append("\n");
                }
                else{
                    propAddress = "";
                    propID = "";
                }
                String privateAmenityID = rs.getString("AMENITY_ID");
                String privateAmenity = rs.getString("DESCRIPTION");
                double privateAmenityCost = rs.getDouble("COST");
                String privateAmenityFeature = rs.getString("FEATURE");
                String privateAmenityWarranty = rs.getString("WARRANTY");
                
                // Print formatted result
                sb.append(String.format("%-20s %-20s %-20s %-20s %-20s %-20.2f %-20s %-20s %n",
                        propID,
                        propAddress,
                        aptID,
                        privateAmenityID,
                        privateAmenity,
                        privateAmenityCost,
                        privateAmenityFeature,
                        privateAmenityWarranty));
            }
            sb.append(generateDashLine(8,20,"-")).append("\n");
            sb.append("\n");
            System.out.println(sb.toString());
            stmt.close();
        }
        catch(SQLException e){
            System.out.println("SQLException: " + e + "\n");
        }  
    }

    /**
     * Utility method for viewing the manual
     * @param conn
     */
    public static void ViewManual(Connection conn, boolean isManager, Scanner scanner){
        StringBuilder sb_summary = new StringBuilder();
        StringBuilder sb_property = new StringBuilder();
        StringBuilder sb_apartment = new StringBuilder();
        ViewSummary(conn, sb_summary, null, null, null, false);
        int option;
        int max_option = isManager ? 4 : 3;

        do {
            System.out.println();
            System.out.println();
            System.out.println();
            System.out.println("\n==================== EU: Property/Apartment Page ====================");
            System.out.println("1. View Property & Apartment Amenities");
            System.out.println("2. Search Property & Apartment");
            if(isManager){
                System.out.printf("%1d. Output All Summary to File \n", max_option-1);
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
                    System.out.println("Viewing Property & Apartment Amenities...");
                    ViewPropertyCommon(conn, sb_property, null);
                    ViewApartmentPrivate(conn, sb_apartment, null, null);
                    // loginAsPropertyManager();
                    break;
                case 2:
                    // Call the login function for Tenant
                    SearchManual(conn, scanner);
                    // loginAsTenant();
                    break;
                case 3:
                    if(isManager){
                        System.out.println("Outputing All Summary to File...");
                        ViewSummary(conn, sb_summary, null, null, null, false);
                        ViewPropertyCommon(conn, sb_property, null);
                        ViewApartmentPrivate(conn, sb_apartment, null, null);
                        sb_summary.append(sb_property).append(sb_apartment);
                        OutputTable(sb_summary);
                        System.out.println("All Above Summary Outputed to File.");
                    }
                    else{
                        System.out.println("Quitting...");
                        sb_summary.setLength(0);
                        sb_property.setLength(0);
                        sb_apartment.setLength(0);
                        System.out.println("Going back to previous...");
                    }
                    break;
                case 4:
                    sb_summary.setLength(0);
                    sb_property.setLength(0);
                    sb_apartment.setLength(0);
                    System.out.println("Going back to previous...");
                    // loginAsCompanyManager();
                    break;
    
                default:
                    System.out.printf("That's not a valid option! Please enter a number between 1 and %1d.\n", max_option);
                    break;
            }
        } while (option != max_option);
    }


    /**
     * Utility method for searching manual
     * @param conn
     * @param scanner
     */
    public static void SearchManual(Connection conn, Scanner scanner){
        int option;
        do {
            System.out.println();
            System.out.println();
            System.out.println();
            System.out.println("\n==================== EU: Property/Apartment Search ====================");
            System.out.println("1. Search by Property ID & Apartment ID");
            System.out.println("2. Search by Rent, Address, Amenities");
            System.out.println("3. Back");
            System.out.println("=====================================================================");
            System.out.println("Please enter your choice (1-3): ");

            while (!scanner.hasNextInt()) {
                System.out.println("That's not a valid option! Please enter a number between 1 and 3.");
                scanner.next(); 
                System.out.println("Please enter your choice (1-3): ");
            }

            option = scanner.nextInt();

            switch (option) {
                case 1:
                    System.out.println();
                    SearchByID(conn, scanner);
                    break;
                case 2:
                    System.out.println();
                    SearchByAttributes(conn, scanner);
                    break;
                case 3:
                    System.out.println("Going back to previous...");
                    // loginAsCompanyManager();
                    break;
    
                default:
                    System.out.println("Invalid option. Please enter a number between 1 and 3.");
                    break;
            }
        } while (option != 3);

    }

    
    /**
     * Utility method for Check ID input
     * @param instruction
     * @param scanner
     * @return ID
     */
    public static String IDInput(String instruction, Scanner scanner) {
        String ID = "";
        while (true) {
            System.out.println(instruction);
            ID = scanner.nextLine();
            if (ID.matches("\\d{1,5}")) {
                break;
            } else {
                System.out.println("Invalid ID! ID is a 1 to 5 digit number, Please try again.");
            }
        }
        return ID;
    }

    /**
     * Utility method for Check input
     * @param pstmt
     * @return size of result
     */
    public static int JDBC_Check(PreparedStatement pstmt){
        ResultSet res = null;
        int size = 0;
        try{
            res = pstmt.executeQuery();
            while (res.next()) {
                size++;
            }
        }
        catch(SQLException e){
            System.out.println("SQLException: " + e);
        }
        finally {
            try {
                if (res != null) {
                    res.close();
                }
                if (pstmt != null) {
                    pstmt.close();
                }
            } catch (SQLException e) {
                System.out.println("SQLException on closing: " + e);
            }
        }
        return size;
    }

    /**
     * Utility method for searching by ID
     * @param conn
     * @param scanner
     */
    public static void SearchByID(Connection conn, Scanner scanner){
        System.out.println();
        System.out.println();
        System.out.println();
        StringBuilder sb_summary = new StringBuilder();
        StringBuilder sb_property = new StringBuilder();
        StringBuilder sb_apartment = new StringBuilder();
        String propID = "";
        String aptID = "";
        System.out.println("\n==================== EU: Property/Apartment Search By ID ====================");
        scanner.nextLine();
        while(true){
            propID = IDInput("Enter a Property ID", scanner);
            try{
                PreparedStatement pstmt = conn.prepareStatement("select * from property where prop_id = ?");
                pstmt.setString(1, propID);
                if(JDBC_Check(pstmt) == 0){
                    System.out.println("Property ID Not Found! Please Try Again.");
                    continue;
                }
                pstmt.close();
            }
            catch(SQLException e){
                System.out.println("SQLException: " + e);
            }
            aptID = IDInput("Enter a Apartment ID", scanner);
            try{
                PreparedStatement pstmt = conn.prepareStatement("select * from apartment where prop_id = ? and apt_id = ?");
                pstmt.setString(1, propID);
                pstmt.setString(2, aptID);
                if(JDBC_Check(pstmt) == 0){
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
        System.out.println("=====================================================================");
        System.out.println("Searching Property & Apartment...");
        ViewSummary(conn, sb_summary, propID, aptID, null, false);
        ViewPropertyCommon(conn, sb_property, propID);
        ViewApartmentPrivate(conn, sb_apartment, propID, aptID);
        System.out.printf("Above Summary for Property: %5s, Apartment: %5s\n", propID, aptID);
    }


    /**
     * Utility method for searching by attributes
     * @param conn
     * @param scanner
     */
    public static void SearchByAttributes(Connection conn, Scanner scanner){
        System.out.println();
        System.out.println();
        System.out.println();


        scanner.nextLine();
        String sql = """
            with amenity_summary as (
                select prop_id, address, apt_id, rent, amenity_id from (select * from property natural join apartment) natural join apt_private
                union
                select prop_id, address, apt_id, rent, amenity_id from (select * from property natural join apartment) natural join prop_common
            )
            select distinct prop_id, address, apt_id from amenity_summary where
                """;
        String target_rent = "";
        String target_address = "";
        String target_amenity = "";
        System.out.println("\n==================== EU: Property/Apartment Search By Attribute ====================");
        System.out.println("System will search for apartments based on address, rent, and amenities.");
        System.out.println("If you don't want to search by a certain attribute, just press 'q' to continue.");
        int currentPostion = 1;
        int rentPosition = 0;
        int addressPosition = 0;
        int commonPosition = 0;
        while(true){
            System.out.println("Enter the target address:(or press 'q' to continue) ");
            String temp_adress = scanner.nextLine();
            if(!temp_adress.isBlank() || temp_adress.equalsIgnoreCase("q")){
                target_address = temp_adress.equalsIgnoreCase("q")? "" : temp_adress;
                if(!target_address.isEmpty()){
                    try{
                        PreparedStatement pstmt = conn.prepareStatement("select * from property where address = ?");
                        pstmt.setString(1, target_address);
                        if(JDBC_Check(pstmt) == 0){
                            System.out.println("Address Not Found! Please Try Again.");
                            continue;
                        }
                        pstmt.close();
                        sql += "address = ?";
                        addressPosition = currentPostion;
                        currentPostion++;
                    }
                    catch(SQLException e){
                        System.out.println("SQLException: " + e);
                    }
                }
                break;
            }
            else{
                System.out.println("Invalid Address! Please try again.");
            }
        }
        while(true){
            System.out.println("Enter the target rent:(or press 'q' to continue) ");
            String temp_rent = scanner.nextLine();
            if(temp_rent.matches("\\d+") || temp_rent.equalsIgnoreCase("q")){
                target_rent = temp_rent.equalsIgnoreCase("q")? "" : temp_rent;
                if(!target_rent.isEmpty()){
                    try{
                        String temp_sql = "select * from property natural join apartment where rent = ?";
                        if(!target_address.isEmpty()){
                            temp_sql += " and address = ?";
                        }
                        PreparedStatement pstmt = conn.prepareStatement(temp_sql);
                        pstmt.setString(1, target_rent);
                        if(!target_address.isEmpty()){
                            pstmt.setString(2, target_address);
                        }
                        if(JDBC_Check(pstmt) == 0){
                            System.out.println("Rent Not Found! Please Try Again.");
                            continue;
                        }
                        pstmt.close();
                        sql += target_address.isEmpty() ? "" : " and ";
                        sql += "rent = ?";
                        rentPosition = currentPostion;
                        currentPostion++;
                    }
                    catch(SQLException e){
                        System.out.println("SQLException: " + e);
                    }
                }
                break;
            }
            else{
                System.out.println("Invalid rent! Please try again.");
            }
        }
        while(true){
            System.out.println("Enter the target Amenity ID:(or press 'q' to continue) ");
            String temp_amenity = scanner.nextLine();
            if(temp_amenity.matches("\\d{1,5}") || temp_amenity.equalsIgnoreCase("q")){
                target_amenity = temp_amenity.equalsIgnoreCase("q")? "" : temp_amenity;
                if(!target_amenity.isEmpty()){
                    if(!target_address.isEmpty() || !target_rent.isEmpty()){
                        sql += " and ";
                    }
                    sql += "amenity_id = ?";
                    commonPosition = currentPostion;
                }
                break;
            }
            else{
                System.out.println("Invalid amenity! Please try again.");
            }
        }
        System.out.println("=====================================================================");
        if(target_address.isEmpty() && target_rent.isEmpty() && target_amenity.isEmpty()){
            System.out.println("No attribute is specified. Please try again.");
            return;
        }
        System.out.println("Searching Property & Apartment...\n");
        sql += " order by prop_id, apt_id";
       
        try(PreparedStatement pstmt = conn.prepareStatement(sql)){
            if(!target_address.isEmpty()){
                pstmt.setString(addressPosition, target_address);
            }
            if(!target_rent.isEmpty()){
                pstmt.setString(rentPosition, target_rent);
            }
            if(!target_amenity.isEmpty()){
                pstmt.setString(commonPosition, target_amenity);
            }
            ResultSet rs = pstmt.executeQuery();
            // Print table header
            System.out.println(generateDashLine(3,20,"-"));
            String header = String.format("%-20s %-20s %-20s  %n", "PROPERTY_ID", "ADDRESS", "APARTMENT_ID");
            System.out.println(header);
            System.out.println(generateDashLine(3,20,"-"));
            // Iterate through the ResultSet
            while(rs.next()){
                String propID = rs.getString("PROP_ID");
                String address = rs.getString("ADDRESS");
                String aptID = rs.getString("APT_ID");
                System.out.printf("%-20s %-20s %-20s  %n", propID, address, aptID);
            }
            System.out.println(generateDashLine(3,20,"-"));
        }catch(SQLException e){
            System.out.println("SQLException: " + e);
        }
        
    }


    


}

