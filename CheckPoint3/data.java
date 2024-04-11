package CheckPoint3;

import java.sql.*;
import java.util.Scanner;
import java.util.Arrays;



public class data {

    public static Scanner scanner = new Scanner(System.in);

    public static boolean input_Check(String type, String input){
        if(input.isEmpty()){
            return false;
        }
        boolean res = false;
        String[] semesters = {"spring", "summer", "fall", "winter"};
        switch(type){
            case "year":
                if(input.matches("\\d{4}")){
                    res = true;
                }
                break;
            case "semester":
                if(Arrays.asList(semesters).contains(input)){
                    res = true;
                }
                break;
            case "course":
                if(input.matches("\\d{3}")){
                    res = true;
                }
                break;   
            default:
                if(input.matches("\\d{1}")){
                    res = true;
                }     
        }
        if(!res){
            return false;
        }
        return true;
    }

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

    public static void JDBC_Capacity(Connection conn){
        String year = "";
        String semester = "";
        String course_id = "";
        String sec_id = "";
        PreparedStatement pstmt = null;
        while(true){
            System.out.println("Input data on the section whose classroom capacity you wish to check.");

            //Year
            System.out.print("Year(yyyy)or 0 to exit:");
            year = scanner.nextLine();
            if(!input_Check("year", year)){
                System.out.println("Please input an 4 digit integer.");
                continue;
            }
            try{
                pstmt = conn.prepareStatement("select * from section where year=?");
                pstmt.setString(1, year);
                if(JDBC_Check(pstmt) == 0){
                    System.out.println("Year not in database.");
                    continue;
                }
            }
            catch(SQLException e){
                System.out.println("SQLException: " + e);
            }

            //Semester
            System.out.print("Semester(string):");
            semester = scanner.nextLine();
            if(!input_Check("semester", semester)){
                System.out.println("Please input one of fall, winter, spring, summer.");
                continue;
            }
            semester = Character.toUpperCase(semester.charAt(0)) + semester.substring(1);
            
            //Course ID
            System.out.print("Input course ID as 3 digit integer:");
            course_id = scanner.nextLine();
            if(!input_Check("course", course_id)){
                System.out.println("Please input an 3 digit integer.");
                continue;
            }
            try{
                pstmt = conn.prepareStatement("select * from course where course_id=?");
                pstmt.setString(1, course_id);
                if(JDBC_Check(pstmt) == 0){
                    System.out.println("Course not in database.");
                    continue;
                }

                pstmt = conn.prepareStatement("select * from section where course_id=? and year=? and semester=?");
                pstmt.setString(1, course_id);
                pstmt.setString(2, year);
                pstmt.setString(3, semester);
                if(JDBC_Check(pstmt) == 0){
                    System.out.println("Course not offered in given year/semester.");
                    continue;
                }
            }
            catch(SQLException e){
                System.out.println("SQLException: " + e);
            }

            //Section ID
            System.out.print("Input section ID as integer:");
            sec_id = scanner.nextLine();
            if(!input_Check("section", sec_id)){
                System.out.println("Please input an integer.");
                continue;
            }
            try{
                pstmt = conn.prepareStatement("select * from section where course_id=? and year=? and semester=? and sec_id=?");
                pstmt.setString(1, course_id);
                pstmt.setString(2, year);
                pstmt.setString(3, semester);
                pstmt.setString(4, sec_id);
                if(JDBC_Check(pstmt) == 0){
                    System.out.println("Non-existent Section for given course offered in given year/semester.");
                    continue;
                }
            }
            catch(SQLException e){
                System.out.println("SQLException: " + e);
            }
            break;
        }

        try{
            pstmt = conn.prepareStatement("select capacity, count(*) as enroll from section natural join classroom natural join takes group by course_id, sec_id, semester, year, capacity having course_id=? and sec_id =? and semester=? and year =?");
            pstmt.setString(1, course_id);
            pstmt.setString(2, sec_id);
            pstmt.setString(3, semester);
            pstmt.setString(4, year);
            ResultSet res = pstmt.executeQuery();
            
            if (res.next()) {  // Move to the first row
                int capacity = Integer.parseInt(res.getString("capacity"));
                int enroll = Integer.parseInt(res.getString("enroll"));
                System.out.println("Capacity is " + capacity + ". Enrollment is " + enroll + ".");
                if(capacity >= enroll){
                    System.out.println("There are " + (capacity-enroll) + " open seats.");
                }
                else{
                    System.out.println("It is overenrolled by " + (enroll-capacity) + " seats.");
                }
            } else {
                System.out.println("No data found for the specified criteria.");
            }
            
            res.close();
            pstmt.close();
        }
        catch(SQLException e){
            System.out.println("SQLException: " + e);
        }
    }

    public static void JDBC_Connection(String userid, String pass){
        try(Connection conn = DriverManager.getConnection(
                "jdbc:oracle:thin:@edgar1.cse.lehigh.edu:1521:cse241",
                userid,
                pass
            );
            Statement stmt = conn.createStatement();
        ){
            JDBC_Capacity(conn);
            stmt.close();
            conn.close();
        }
        catch(SQLException e){
            System.out.println("SQLException: " + e);
        }
    }

    
    public static void main(String[] args){
        String userid = "";
        String pass = "";
        while(true){
            System.out.print("enter Oracle user id: ");
            userid = scanner.nextLine();
            if(!userid.isEmpty()){
                break;
            }
        }
        while(true){
            System.out.print("enter Oracle  password for " + userid + ": ");
            pass = scanner.nextLine();
            if(!pass.isEmpty()){
                break;
            }
        }

        JDBC_Connection(userid,pass);

    }
}