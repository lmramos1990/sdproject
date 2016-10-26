import java.sql.*;

public class Database {
  private static String user = "bd";
  private static String pass = "oracle";
  private static String url = "jdbc:oracle:thin:@localhost:1521:XE";

  public Database() {

  }

  public static void main (String[] args){
    Connection myConnection = null;
    Statement stmt = null;
    try{
      Class.forName("oracle.jdbc.OracleDriver");

      System.out.println("Connecting to the database...");
      myConnection = DriverManager.getConnection(url, user, pass);
      System.out.println("Connected database successfully...");




      //STEP 4: Execute a query
      System.out.println("Creating statement...");
      stmt = myConnection.createStatement();

      String sql = "SELECT client_id, username, pass, status FROM Client";
      ResultSet rs = stmt.executeQuery(sql);
      //STEP 5: Extract data from result set
      while(rs.next()){
         //Retrieve by column name
         int client_id  = rs.getInt("client_id");
         String username = rs.getString("username");
         String pass = rs.getString("pass");
         int status  = rs.getInt("status");

         //Display values
         System.out.print("client_id: " + client_id);
         System.out.print(", username: " + username);
         System.out.print(", pass: " + pass);
         System.out.println(", status: " + status);
      }
      rs.close();

    }catch(SQLException se){
       //Handle errors for JDBC
       se.printStackTrace();
    }catch(Exception e){
       //Handle errors for Class.forName
       e.printStackTrace();
    }finally{
       //finally block used to close resources
       try{
          if(stmt!=null)
             myConnection.close();
       }catch(SQLException se){
       }// do nothing
       try{
          if(myConnection != null)
             myConnection.close();
       }catch(SQLException se){
          se.printStackTrace();
       }//end finally try
    }//end try
    System.out.println("Goodbye!");

  }

}
