//TABLES SCHEMA
//
//FOR ALL TABLES CREATED, NONE OF THE FIELDS SHOULD ALLOW VALUES AS NULL. THIS IS TO PREVENT ANY DATABASE INCONSISTENCIES.
//
// DROP TABLE INVENTORY CASCADE CONSTRAINTS;
// CREATE TABLE INVENTORY (
// 	ProductID integer NOT NULL CHECK (ProductID > 0),
// 	ProductDesc varchar(30)	NOT NULL,
// 	ProductPrice numeric(8,2) NOT NULL,
// 	ProductStockAmount integer NOT NULL CHECK (ProductStockAmount > 0),
// 	primary key (ProductID)
// );
//
//THE PRODUCTID's ARE SET TO START FROM 1 ONWARDS (PREVENTS NEGATIVE IDs) FOR A MORE USER-FRIENDLY INTERFACE. EACH PRODUCT ID MUST BE UNIQUE SO WE SET IT TO BE THE TABLE's PRIMARY KEY
//THE PRODUCTSTOCKAMOUNT FOLLOWS THE SAME LOGIC SINCE YOU CAN'T HAVE NEGATIVE STOCK IN REAL LIFE
//
// DROP TABLE ORDERS CASCADE CONSTRAINTS;
// CREATE TABLE ORDERS (
// 	OrderID integer NOT NULL CHECK (OrderID > 0),
// 	OrderType varchar(30) CONSTRAINT check_OrderType CHECK (OrderType IN ('InStore', 'Collection', 'Delivery')),
// 	OrderCompleted integer CHECK (OrderCompleted = 1 OR OrderCompleted = 0),
// 	OrderPlaced date NOT NULL,
// 	primary key (OrderID)
// );
//
//THE ORDERID's ARE ALSO SET TO START FROM 1 ONWARDS. THE PRIMARY KEY IS SET ON THE ORDERID AS THESE HAVE TO BE UNIQUE
//THE ORDERTYPE WAS SPECIFIED IN THE COURSEWORK TO BE EITHER 'InStore', 'Collection' or 'Delivery' SO A CONSTRAINT IS CREATED TO CHECK THIS
//THE ORDERCOMPLETED FIELD MUST BE EITHER 1 OR 0 SINCE AN ORDER CAN'T BE "HALF-DONE"
//
// DROP TABLE ORDER_PRODUCTS CASCADE CONSTRAINTS;
// CREATE TABLE ORDER_PRODUCTS (
// 	OrderID integer,
// 	ProductID integer,
// 	ProductQuantity integer NOT NULL CHECK (ProductQuantity > 0),
// 	foreign key (OrderID) REFERENCES orders(OrderID),
// 	foreign key (ProductID) REFERENCES inventory(ProductID)
// );
//
//THE ORDERID AND THE PRODUCTID ARE NOT SPECIFIED TO BE NOT NULL NOR GREATER THAN 0 BECAUSE THEY HAVE ALREADY BEEN SPECIFIED IN
//TABLE INVENTORY AND ORDERS. BY SETTING THESE FIELDS AS FOREIGN KEYS THE CONSTRAINTS TO NOT NEED TO BE CLARIFIED TWICE
//The PRODUCTQUANTITY IS SET TO BE GREATER THAN 0 AS THERE WOULDN'T BE ANY POINT IN ORDERING "Nothing"
//
//
// DROP TABLE DELIVERIES CASCADE CONSTRAINTS;
// CREATE TABLE DELIVERIES (
// 	OrderID integer,
// 	FName varchar(30) NOT NULL,
// 	LName varchar(30) NOT NULL,
// 	House varchar(30) NOT NULL,
// 	Street varchar(30) NOT NULL,
// 	City varchar(30) NOT NULL,
// 	DeliveryDate date NOT NULL,
// 	foreign key (OrderID) REFERENCES orders(OrderID)
// );
//
//THE ORDERID FIELD IS SET AS THE FOREIGN KEY REFERENCING THE PARENT TABLE ORDERS IN ORDER TO CONNECT BOTH TABLES
//
// DROP TABLE COLLECTIONS CASCADE CONSTRAINTS;
// CREATE TABLE COLLECTIONS (
// 	OrderID integer,
// 	FName varchar(30) NOT NULL,
// 	LName varchar(30) NOT NULL,
// 	CollectionDate date NOT NULL,
// 	foreign key (OrderID) REFERENCES orders(OrderID)
// );
//
//THE ORDERID FIELD IS SET AS THE FOREIGN KEY REFERENCING THE PARENT TABLE ORDERS IN ORDER TO CONNECT BOTH TABLES
//
// DROP TABLE STAFF CASCADE CONSTRAINTS;
// CREATE TABLE STAFF (
// 	StaffID integer NOT NULL CHECK (StaffID > 0),
// 	FName varchar(30) NOT NULL,
// 	LName varchar(30) NOT NULL,
// 	primary key (StaffID)
// );
//
//THE STAFFID IS THE PRIMARY KEY SINCE THESE HAVE TO BE UNIQUE FOR EACH STAFF MEMBER
//
// DROP TABLE STAFF_ORDERS CASCADE CONSTRAINTS;
// CREATE TABLE STAFF_ORDERS (
// 	StaffID integer,
// 	OrderID integer,
// 	primary key (StaffID, OrderID),
// 	foreign key (StaffID) REFERENCES staff(StaffID),
// 	foreign key (OrderID) REFERENCES orders(OrderID)
// );
//
//HERE THE PRIMARY KEY IS SET AS A PAIR OF FIELDS: THE STAFFID AND THE ORDERID BECAUSE NO STAFFID SHOULD HAVE TWO OR MORE OF THE SAME ORDER


//Design Choices
//The biggest change that could be applied to the structure of the database schema is the removal of the table staff_orders.
//By adding the staffID of each order in the orders table one can reduce the size of the database which would simplify the SQL statements.
//Furthermore, it is unnecessary to have both FName and LName. By combining these two fields into one field (E.g CustomerName)
//the size of the database can be reduced whilst keeping the same constraints.
//A change to the fields OrderType is that it could hold more values other than 'InStore', 'Collection', 'Delivery'
//such as 'DISPATCHED' in order to prevent orders being cancelled and as such (although we do not have control over this)
//Similarly, the OrderCompleted section could include the value "2" to represent orders that have been dispatched or some other state.

import java.io.*;
import java.sql.*;
import java.util.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.IOException;
import java.util.ArrayList;

class Assignment {

  private static String readEntry(String prompt)
  {
    try
    {
      StringBuffer buffer = new StringBuffer();
      System.out.print(prompt);
      System.out.flush();
      int c = System.in.read();
      while(c != '\n' && c != -1) {
        buffer.append((char)c);
        c = System.in.read();
      }
      return buffer.toString().trim();
    }
    catch (IOException e)
    {
      return "";
    }
  }

  /**
  * @param conn An open database connection
  * @param productIDs An array of productIDs associated with an order
  * @param quantities An array of quantities of a product. The index of a quantity correspeonds with an index in productIDs
  * @param orderDate A string in the form of 'DD-Mon-YY' that represents the date the order was made
  * @param staffID The id of the staff member who sold the order
  */
  public static void option1(Connection conn, int[] productIDs, int[] quantities, String orderDate, int staffID) throws SQLException {//in-store purchases

    Statement stmt = conn.createStatement();
    Statement stmt2 = conn.createStatement();
    Statement stmt3 = conn.createStatement();
    ResultSet rset1;
    ResultSet rset2;
    int i;

    String sqlString1 = "SELECT MAX(OrderID) FROM orders";
    //select the id of the last order in the table
    try {
      rset1 = stmt.executeQuery(sqlString1);
      rset1.next();
      //check if there are any orders in the table orders, if there are then look at its last order otherwise make the orderID = 1
      i = rset1.getInt(1) + 1;
    } catch (SQLException e) {
      System.out.println("First order in the table");
      i = 1;
    }
    String sqlString2 = "INSERT INTO orders VALUES (" + i + ", 'InStore', 1, '" + orderDate + "')";
    //insert into the orders table the corresponding OrderID and the date specified by the customer.
    //The middle two columns are always set to InStore and 1 for option 1
    stmt.executeUpdate(sqlString2);
    String sqlString3 = "INSERT INTO staff_orders VALUES (" + staffID + ", " + i + ")";
    //insert into staff_orders table the corresponding OrderID and the staffID specified by the customer.
    stmt.executeUpdate(sqlString3);
    for (int j = 0; j < productIDs.length; j++) {
      String sqlString4 = "INSERT INTO order_products VALUES (" + i + ", " + productIDs[j] + ", " + quantities[j] + ")";
      //insert the productIDs, orderID and the desired quantity of each product for all products selected by the customer
      stmt.executeUpdate(sqlString4);
      String sqlString5 = "SELECT ProductStockAmount FROM inventory WHERE ProductID = " + productIDs[j];
      //for every product selected, look at its product stock amount
      try {
        rset2 = stmt2.executeQuery(sqlString5);
        rset2.next();
        int reducedStock = rset2.getInt(1) - quantities[j];
        //calculate the new stock after being reduced
        String sqlString6 = "UPDATE inventory SET ProductStockAmount = " + reducedStock + " WHERE ProductID = " + productIDs[j];
        //change the product's stock amount to the one we calculated earlier
        stmt2.executeUpdate(sqlString6);
      } catch (SQLException e) {
        System.out.println("No products available");
        stmt2.close();
        return;
      }
      j++;
    }
    stmt.close();
  }

  /**
  * @param conn An open database connection
  * @param productIDs An array of productIDs associated with an order
  * @param quantities An array of quantities of a product. The index of a quantity correspeonds with an index in productIDs
  * @param orderDate A string in the form of 'DD-Mon-YY' that represents the date the order was made
  * @param collectionDate A string in the form of 'DD-Mon-YY' that represents the date the order will be collected
  * @param fName The first name of the customer who will collect the order
  * @param LName The last name of the customer who will collect the order
  * @param staffID The id of the staff member who sold the order
  */
  public static void option2(Connection conn, int[] productIDs, int[] quantities, String orderDate, String collectionDate, String fName, String lName, int staffID) throws SQLException
  {
    Statement stmt = conn.createStatement();

    ResultSet rset1;
    ResultSet rset2;
    int i;

    String sqlString1 = "SELECT MAX(OrderID) FROM orders";
    //select the id of the last order in the table
    try {
      rset1 = stmt.executeQuery(sqlString1);
      rset1.next();
      //check if there are any orders in the table orders, if there are then look at its last order otherwise make the orderID = 1
      i = rset1.getInt(1) + 1;
    } catch (SQLException e) {
      System.out.println("First order in the table");
      i = 1;
    }
    String sqlString2 = "INSERT INTO orders VALUES (" + i + ", 'Collection', 0, '" + orderDate + "')";
    //Insert into table orders the corresponding OrderID and the orderDate specified by the customer
    //The two middle columns will always be Collection and 0 for option 2
    stmt.executeUpdate(sqlString2);
    String sqlString3 = "INSERT INTO staff_orders VALUES (" + staffID + ", " + i + ")";
    //Insert into table staff_orders the corresponding OrderID and the StaffID specified by the customer
    stmt.executeUpdate(sqlString3);
    String sqlString4 = "INSERT INTO collections VALUES (" + i + ", '" + fName + "', '" + lName + "', '" + collectionDate + "')";
    //Insert into table collections the corresponding OrderID and
    //the first name, last name and collection date specified by the customer
    stmt.executeUpdate(sqlString4);
    for (int j = 0; j < productIDs.length; j++) {
      String sqlString5 = "INSERT INTO order_products VALUES (" + i + ", " + productIDs[j] + ", " + quantities[j] + ")";
      //insert the productIDs, orderID and the desired quantity of each product for all products selected by the customer
      stmt.executeUpdate(sqlString5);
      String sqlString6 = "SELECT ProductStockAmount FROM inventory WHERE ProductID = " + productIDs[j];
      //for every product selected, look at its product stock amount
      try {
        rset2 = stmt.executeQuery(sqlString6);
        rset2.next();
        int reducedStock = rset2.getInt(1) - quantities[j];
        //calculate the new stock after being reduced
        String sqlString7 = "UPDATE inventory SET ProductStockAmount = " + reducedStock + " WHERE ProductID = " + productIDs[j];
        //change the product's stock amount to the one we calculated earlier
        stmt.executeUpdate(sqlString7);
      } catch (SQLException e) {
        System.out.println("No products available");
      }
      j++;
    }
  }

  /**
  * @param conn An open database connection
  * @param productIDs An array of productIDs associated with an order
  * @param quantities An array of quantities of a product. The index of a quantity correspeonds with an index in productIDs
  * @param orderDate A string in the form of 'DD-Mon-YY' that represents the date the order was made
  * @param deliveryDate A string in the form of 'DD-Mon-YY' that represents the date the order will be delivered
  * @param fName The first name of the customer who will receive the order
  * @param LName The last name of the customer who will receive the order
  * @param house The house name or number of the delivery address
  * @param street The street name of the delivery address
  * @param city The city name of the delivery address
  * @param staffID The id of the staff member who sold the order
  */
  public static void option3(Connection conn, int[] productIDs, int[] quantities, String orderDate, String deliveryDate, String fName, String lName,
  String house, String street, String city, int staffID) throws SQLException
  {
    Statement stmt = conn.createStatement();

    ResultSet rset1;
    ResultSet rset2;
    int i;

    String sqlString1 = "SELECT MAX(OrderID) FROM orders";
    //select the id of the last order in the table
    try {
      rset1 = stmt.executeQuery(sqlString1);
      rset1.next();
      //check if there are any orders in the table orders, if there are then look at its last order otherwise make the orderID = 1
      i = rset1.getInt(1) + 1;
    } catch (SQLException e) {
      System.out.println("First order in the table");
      i = 1;
    }
    String sqlString2 = "INSERT INTO orders VALUES (" + i + ", 'Delivery', 0, '" + orderDate + "')";
    //Insert into table orders the corresponding OrderID and the orderDate specified by the customer
    //The two middle columns will always be Delivery and 0 for option 3
    stmt.executeUpdate(sqlString2);
    String sqlString3 = "INSERT INTO staff_orders VALUES (" + staffID + ", " + i + ")";
    //Insert into table staff_orders the corresponding OrderID and the StaffID specified by the customer
    stmt.executeUpdate(sqlString3);
    String sqlString4 = "INSERT INTO deliveries VALUES (" + i + ", '" + fName + "', '" + lName + "', '" + house + "', '" + street + "', '" + city + "', '" + deliveryDate + "')";
    //Insert into table deliveries the corresponding OrderID and the first name,
    //last name, house name, street name, city name and the delivery date specified by the customer
    stmt.executeUpdate(sqlString4);
    for (int j = 0; j < productIDs.length; j++) {
      String sqlString5 = "INSERT INTO order_products VALUES (" + i + ", " + productIDs[j] + ", " + quantities[j] + ")";
      //insert the productIDs, orderID and the desired quantity of each product for all products selected by the customer
      stmt.executeUpdate(sqlString5);
      String sqlString6 = "SELECT ProductStockAmount FROM inventory WHERE ProductID = " + productIDs[j];
      //for every product selected, look at its product stock amount
      try {
        rset2 = stmt.executeQuery(sqlString6);
        rset2.next();
        int reducedStock = rset2.getInt(1) - quantities[j];
        //calculate the new stock after being reduced
        String sqlString7 = "UPDATE inventory SET ProductStockAmount = " + reducedStock + " WHERE ProductID = " + productIDs[j];
        //change the product's stock amount to the one we calculated earlier
        stmt.executeUpdate(sqlString7);
      } catch (SQLException e) {
        System.out.println("No products available");
      }
      j++;
    }
  }

  /**
  * @param conn An open database connection
  */
  public static void option4(Connection conn) throws SQLException
  {
    Statement stmt = conn.createStatement();
    ResultSet rset1;
    String sqlString1 = "SELECT i.ProductID, i.ProductDesc, SUM(op.ProductQuantity * i.ProductPrice) FROM inventory i JOIN order_products op ON i.productID = op.productID GROUP BY i.ProductID, i.ProductDesc ORDER BY SUM(op.ProductQuantity * i.ProductPrice) DESC";
    //Select the productID, product description and the total value sold of each product in descending order of total value sold
    rset1 = stmt.executeQuery(sqlString1);
    System.out.println("ProductID, ProductDesc, TotalValueSold");
    while (rset1.next()) {
      System.out.println(rset1.getInt(1) + ", " + rset1.getString(2) + ", £" + rset1.getDouble(3));
    }
  }

  /**
  * @param conn An open database connection
  * @param date The target date to test collection deliveries against
  */
  public static void option5(Connection conn, String date) throws SQLException
  {
    Statement stmt = conn.createStatement();
    Statement stmt2 = conn.createStatement();
    ResultSet rset1;
    ResultSet rset2;
    int currentStock;
    int reservedStock;
    int productID;
    int orderID;
    int i;

    String sqlString1 = "SELECT o.OrderID FROM orders o JOIN collections c ON o.OrderID = c.OrderID WHERE ((TO_DATE('" + date + "') - c.CollectionDate) >= 8 AND o.OrderCompleted = 0 AND o.OrderType = 'Collection')";
    //Select the order ID of all the orders that are for collection, have not yet been collected
    //and the collection date is at least 8 days before the date specified by the user
    try {
      rset1 = stmt.executeQuery(sqlString1);
      while(rset1.next()) {
        orderID = rset1.getInt(1);
        String sqlString2 = "SELECT op.ProductQuantity, op.ProductID, i.ProductStockAmount FROM inventory i JOIN order_products op ON i.productID = op.productID WHERE op.orderID = " + orderID + " GROUP BY op.ProductQuantity, op.ProductID, i.ProductStockAmount";
        //Select the product ID, the product quantity and the product stock amount
        //of the products that were bought by the orders selected in the previous query
        try {
          rset2 = stmt2.executeQuery(sqlString2);
          rset2.next();
          reservedStock = rset2.getInt(1);
          productID = rset2.getInt(2);
          currentStock = rset2.getInt(3);
          int refundedStock = currentStock + reservedStock;
          //calculate the new stock of each product by adding the current stock and the stock reserved by the order
          String sqlString3 = "UPDATE inventory SET ProductStockAmount =" + refundedStock + "WHERE productID = " + productID;
          //change the product stock amount to the value we calculated previously
          stmt2.executeUpdate(sqlString3);
          String sqlString4 = "DELETE FROM collections WHERE OrderID = " + orderID;
          //delete the order from the table collections
          stmt2.executeUpdate(sqlString4);
          String sqlString5 = "DELETE FROM staff_orders WHERE OrderID = " + orderID;
          //delete the order from the table staff_orders
          stmt2.executeUpdate(sqlString5);
          String sqlString6 = "DELETE FROM order_products WHERE OrderID = " + orderID;
          //delete the order from the table order_products
          stmt2.executeUpdate(sqlString6);
          String sqlString7 = "DELETE FROM orders WHERE OrderID = " + orderID;
          //delete the order from the table orders
          stmt2.executeUpdate(sqlString7);
          System.out.println("Order " + orderID + " has been cancelled");
        } catch (SQLException e) {
          System.out.println("No ProductQuantity or No productID");
          stmt2.close();
          return;
        }
      }
    } catch (SQLException e) {
      System.out.println("No orders out of date");
      stmt.close();
    }
  }

  /**
  * @param conn An open database connection
  */
  public static void option6(Connection conn) throws SQLException
  {
    Statement stmt = conn.createStatement();

    ResultSet rset1;
    String sqlString1 = "SELECT s.FName, s.LName, SUM(i.ProductPrice * op.ProductQuantity) FROM inventory i JOIN order_products op ON i.ProductID = op.ProductID JOIN staff_orders so ON so.OrderID = op.OrderID JOIN staff s ON s.StaffID = so.StaffID GROUP BY s.FName, s.LName HAVING SUM(i.ProductPrice * op.ProductQuantity) >= 50000 ORDER BY SUM(i.ProductPrice * op.ProductQuantity) DESC";
    //select the first name, last name and the total value sold by each staff member that has sold over 50000 worth of products
    try {
      rset1 = stmt.executeQuery(sqlString1);
      System.out.println("EmployeeName, TotalValueSold");
      while(rset1.next()) {
        System.out.println(rset1.getString(1) + " " + rset1.getString(2) + ", £" + rset1.getDouble(3));
      }
    } catch (SQLException e) {
      System.out.println("No employees have made over 50000");
      stmt.close();
      return;
    }
  }

  /**
  * @param conn An open database connection
  */
  public static void option7(Connection conn) throws SQLException
  {
    Statement stmt = conn.createStatement();
    Statement stmt2 = conn.createStatement();
    ResultSet rset1;
    ResultSet rset2;
    int productCount = 2;

    String sqlString1 = "SELECT i.ProductID, SUM(i.Productprice * op.ProductQuantity) FROM order_products op JOIN inventory i ON op.ProductID = i.ProductID GROUP BY i.ProductID HAVING SUM(i.Productprice * op.ProductQuantity) > 20000";
    //select the product ID and the total value sold by each product that has sold over 20000
    try {
      rset1 = stmt.executeQuery(sqlString1);
      System.out.print("EmployeeName");
      String sqlString2 = "SELECT s.fName, s.lName";
      String sqlString3 = " FROM inventory i JOIN order_products op ON i.productID = op.productID JOIN staff_orders so ON op.orderID = so.orderID JOIN staff s ON so.staffID = s.staffID WHERE 1=1 AND";
      while(rset1.next()) {
        int currentproductID = rset1.getInt(1);
        System.out.print(", Product " + currentproductID);
        sqlString2 += ", SUM(CASE op.ProductID WHEN " + currentproductID + " THEN op.ProductQuantity ELSE 0 END) AS Product" + currentproductID;
        sqlString3 += " op.productID = " + currentproductID + " OR";
        productCount++;
      }
      String sqlString4 = sqlString3.substring(0, sqlString3.lastIndexOf(" "));
      //removes the last order in sqlString3
      sqlString2 += sqlString4 + " GROUP BY s.fName, s.lName ORDER BY SUM(op.productQuantity * i.productPrice) DESC";
      //sqlString2 will select the quantities sold of each product by each staff member that
      //has sold at least 1 of the products selected in the first select statement
      try {
        rset2 = stmt2.executeQuery(sqlString2);
        while(rset2.next()) {
          System.out.println("");
          System.out.print(rset2.getString(1) + " " + rset2.getString(2));
          for(int i = 3; i < productCount+1; i++) {
            System.out.print(", " + rset2.getString(i));
          }
        }
        System.out.println("");
      }  catch (SQLException e) {
          System.out.println("");
          System.out.print("Big Query error");
          stmt2.close();
          return;
      }
    } catch (SQLException e) {
        System.out.println("No products profit are over 20000");
        stmt.close();
        return;
    }
  }

  /**
  * @param conn An open database connection
  * @param year The target year we match employee and product sales against
  */
  public static void option8(Connection conn, int year) throws SQLException
  {
    Statement stmt = conn.createStatement();
    Statement stmt2 = conn.createStatement();
    Statement stmt3 = conn.createStatement();
    Statement stmt4 = conn.createStatement();
    ResultSet rset1;
    ResultSet rset2;
    ResultSet rset3;
    ResultSet rset4;
    int i = 0;
    int j;

    String sqlString1 = "SELECT i.productID, SUM(i.ProductPrice * op.ProductQuantity) AS yearProfit FROM inventory i JOIN order_products op ON i.productID = op.productID JOIN orders o ON op.orderID = o.orderID WHERE EXTRACT(YEAR FROM o.OrderPlaced) = " + year + " GROUP BY i.productID HAVING SUM(i.productPrice * op.productQuantity) > 20000";
    //Selects the product IDs of the products that sold over 20000 in the year specified by the user
    try {
      rset1 = stmt.executeQuery(sqlString1);
      String sqlString2 = "CREATE VIEW table1 AS SELECT s.fName, s.lName";
      String sqlString3 = " FROM inventory i JOIN order_products op ON i.productID = op.productID JOIN orders o ON o.orderID = op.orderID JOIN staff_orders so ON op.orderID = so.orderID JOIN staff s ON so.staffID = s.staffID WHERE EXTRACT(YEAR FROM o.orderPlaced) = " + year;
      while(rset1.next()) {
          sqlString2 += ", SUM(CASE op.ProductID WHEN " + rset1.getInt(1) + " THEN op.ProductQuantity ELSE 0 END) AS Product" + i;
          sqlString3 += " OR op.productID = " + rset1.getInt(1);
          i++;
      }
      sqlString2 += sqlString3 + " GROUP BY s.fName, s.lName";
      //sqlString2 is the select statement as the sqlString2 in option7 but it will work on a different set of products
      stmt3.executeUpdate(sqlString2);
    } catch (SQLException e) {
        System.out.println("No products made over 20000 this year");
        stmt3.close();
        return;
    }

    String sqlString6 = "CREATE VIEW table2 AS SELECT s.FName, s.LName FROM inventory i JOIN order_products op ON i.ProductID = op.ProductID JOIN orders o ON op.orderID = o.orderID JOIN staff_orders so ON so.OrderID = op.OrderID JOIN staff s ON s.StaffID = so.StaffID WHERE EXTRACT(YEAR FROM o.orderPlaced) = " + year + " GROUP BY s.FName, s.LName HAVING SUM(i.ProductPrice * op.ProductQuantity) >= 30000";
    //A second view is created containing the first name and the last name of the staff members
    //that have sold at least 30000 in the year specified by the user
    stmt.executeUpdate(sqlString6);

    String sqlString5 = "CREATE VIEW table3 AS SELECT fname, lname FROM table1 WHERE 1=1 ";
    for (j = 0; j < i; j++) {
      sqlString5 += "AND Product" + j + " > 0 ";
    }
    sqlString5 += "GROUP BY fname, lname";
    //sqlString5 will only select the first name and the last name of the staff members
    //that have sold at least 1 of each product from the first view we created
    stmt.executeUpdate(sqlString5);

    String sqlString7 = "SELECT * from table3 INTERSECT SELECT * from table2";
    //By intersecting table3 and table2, we can obtain the staff members that exist in both tables thus picking out only the ones
    //that have sold at least 30000 and have sold at least 1 of each product from table1
    try {
      rset4 = stmt.executeQuery(sqlString7);
      while(rset4.next()) {
        System.out.println("");
        System.out.print(rset4.getString(1) + " " + rset4.getString(2));
      }
      System.out.println("");
    } catch (SQLException e) {
        System.out.println("Failed to merge");
        stmt.close();
    }

    String sqlString8 = "DROP view table1";
    //dropping the view is important so that the next view created when option8 is called again does not result in an error
    stmt2.executeUpdate(sqlString8);
    String sqlString9 = "DROP view table2";
    stmt.executeUpdate(sqlString9);
    String sqlString10 = "DROP view table3";
    stmt.executeUpdate(sqlString10);
  }

  public static Connection getConnection()
  {
    String user;
    String passwrd;
    Connection conn;

    try
    {
      Class.forName("oracle.jdbc.driver.OracleDriver");
    }
    catch (ClassNotFoundException x)
    {
      System.out.println ("Driver could not be loaded");
    }
    user = readEntry("Enter database account:");
    passwrd = readEntry("Enter a password:");
    try
    {
      conn = DriverManager.getConnection("jdbc:oracle:thin:@daisy.warwick.ac.uk:1521:daisy",user,passwrd);
      return conn;
    }
    catch(SQLException e)
    {
      System.out.println("Error retrieving connection");
      return null;
    }
  }

  public static void main(String args[]) throws SQLException, IOException
  {
    // You should only need to fetch the connection details once
    Connection conn = getConnection();

    boolean done = false;
    do {
      printMenu();
      String ch = readEntry("Enter your choice: ");
      switch (ch.charAt(0)) {
        //All the do(x) where x is the option number, we must first ask for the necessary input which is why we call another function first
        case '1': do123(conn, 1);
        break;
        case '2': do123(conn, 2);
        break;
        case '3': do123(conn, 3);
        break;
        case '4': option4(conn);
        break;
        case '5': do5(conn);
        break;
        case '6': option6(conn);
        break;
        case '7': option7(conn);
        break;
        case '8': do8(conn);
        break;
        case '0':done = true;
        break;
        default : System.out.println(" Not a valid option ");
      } //switch
    } while(!done);
    // Code to present a looping menu, read in input data and call the appropriate option menu goes here
    // You may use readEntry to retrieve input data

    conn.close();
  }

  private static void printMenu() {
    System.out.println("\n MENU:");
    System.out.println("(1) In-Store Purchases");
    System.out.println("(2) Collection");
    System.out.println("(3) Delivery");
    System.out.println("(4) Biggest Sellers");
    System.out.println("(5) Reserved Stock");
    System.out.println("(6) Staff Life-Time Success");
    System.out.println("(7) Staff Contribution");
    System.out.println("(8) Employees of the Year");
    System.out.println("(0) Quit. \n");
  }

  public static void do123 (Connection conn, int option) throws SQLException {
    ArrayList<Integer> productIDList = new ArrayList<Integer>();
    ArrayList<Integer> quantitiesList = new ArrayList<Integer>();
    //ArrayLists were chosen as the data structure for options 1-3 because they are single-dimensional structures
    //that have no set size thus allowing the customer to order however many products as he/she wants as long as there is enough stock
    char c = 'Y';
    int staffID;

    while (c != 'N') {
      int productID = Integer.parseInt(readEntry("Enter a product ID: "));
      //we must use the parseInt() function on the input because the readEntry() function returns a string and we need an integer
      productIDList.add(productID);
      //add the input to the productID ArrayList
      int quantity = Integer.parseInt(readEntry("Enter the quantity sold: "));
      quantitiesList.add(quantity);
      //add the input to the quantities ArrayList
      String answer = readEntry("Is there another product in the order?: ");
      c = answer.charAt(0);
      //charAt() function is used to obtain the character at the specified index of a string
      //in this case, we store the input of the user into a char variable, if Y keep running while loop otherwise continue
    }
    sameProduct(productIDList, quantitiesList);
    int[] productIDs = new int[productIDList.size()];
    int[] quantities = new int[quantitiesList.size()];
    //create arrays of size equal to the size of the ArrayLists
    for(int i = 0; i < productIDList.size(); i++) {
      productIDs[i] = productIDList.get(i);
      quantities[i] = quantitiesList.get(i);
    }
    //copy over all the values in the ArrayLists to the arrays

    String orderDate = readEntry("Enter date sold: ");

    switch (option) {
      case 1: staffID = Integer.parseInt(readEntry("Enter your staff ID: "));
              if (checkIDandQuantity(conn, productIDList, quantitiesList) && checkDateFormat(orderDate)) {
                //check if productID's exist, there's enough stock and the date inputted is in the correct format
                //only run option1 if all the input is correct
                option1(conn, productIDs, quantities, orderDate, staffID);
              }
              break;
              //The main difference is that for case 2 and case 3 there is an extra check for the date of collection and the date of delivery
              //to see if the either of them are earlier than the order date which if it did, the order would not make sense
      case 2: String dateCollection = readEntry("Enter date of collection: ");
              String fnameCollector = readEntry("Enter the first name of collector: ");
              String lnameCollector = readEntry("Enter the last name of collector: ");
              staffID = Integer.parseInt(readEntry("Enter your staff ID: "));

              if (checkIDandQuantity(conn, productIDList, quantitiesList) && checkDateFormat(orderDate) && checkDateFormat(dateCollection) && checkDeliveryCollection(orderDate, dateCollection)) {
                //only run option2 if all the input is correct
                option2(conn, productIDs, quantities, orderDate, dateCollection, fnameCollector, lnameCollector, staffID);
              }
              break;
      case 3: String dateDelivery = readEntry("Enter date of collection: ");
              String fnameRecipient = readEntry("Enter the first name of recipient: ");
              String lnameRecipient = readEntry("Enter the last name of recipient: ");
              String housename = readEntry("Enter the house name/no: ");
              String streetname = readEntry("Enter the street: ");
              String cityname = readEntry("Enter the City: ");
              staffID = Integer.parseInt(readEntry("Enter your staff ID: "));

              if(checkIDandQuantity(conn, productIDList, quantitiesList) && checkDateFormat(orderDate) && checkDateFormat(dateDelivery) && checkDeliveryCollection(orderDate, dateDelivery)) {
                //only run option3 if all the input is correct
                option3(conn, productIDs, quantities, orderDate, dateDelivery, fnameRecipient, lnameRecipient, housename, streetname, cityname, staffID);
              }
              break;
      default : System.out.println(" Not a valid option ");
    }
  }

  public static void do5 (Connection conn) throws SQLException {
    String theDate = readEntry("Enter the date: ");
    if (checkDateFormat(theDate)) {
      option5(conn, theDate);
    }
  }

  public static void do8 (Connection conn) throws SQLException {
    int year = Integer.parseInt(readEntry("Enter the year: "));
    option8(conn, year);
  }

  public static boolean checkIDandQuantity (Connection conn, ArrayList<Integer> productIDs, ArrayList<Integer> quantities) throws SQLException {
    Statement stmt = conn.createStatement();
    Statement stmt2 = conn.createStatement();
    ResultSet rset1;
    ResultSet rset2;

    for (int i = 0; i < productIDs.size(); i++) {
      String checkID = "SELECT productID from inventory WHERE productID = " + productIDs.get(i);
      //for each productID inputted by the customer, we select the same productID from the inventory table
      try {
        rset1 = stmt.executeQuery(checkID);
        if (rset1.next()) {
          //if there is a match then continue without doing anything
        } else {
          //else return false since productID inserted by customer does not exist in our inventory
          System.out.println("No product with ID " + productIDs.get(i));
          return false;
        }
      } catch (SQLException e) {
          System.out.println("Error in ID");
          return false;
      }
      String checkQuantity = "SELECT ProductStockAmount FROM inventory WHERE productID = " + productIDs.get(i);
      //for each quantity inputted by the customer, check the current product stock amount in the inventory for that particular product
      try {
          rset2 = stmt2.executeQuery(checkQuantity);
          rset2.next();
          if (rset2.getInt(1) < quantities.get(i)) {
              //if the current stock amount is less than the quantity desired by the customer, return false
              System.out.println("Not enough in stock");
              return false;
          } //otherwise continue
      } catch (SQLException e) {
          System.out.println("Error in quantity");
          return false;
      }
    }
    return true;
    //if all checks have resulted in true, then return true
  }

  public static boolean checkDateFormat (String date) {
    if (date == null) {
      //if the date inputted by the customer is blank (null) then return false
      System.out.println("Date is required");
      return false;
    }

    String dateFormat = "dd-MMM-yy";
    //specify the format desired of the date
    SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);

    try {
      sdf.parse(date);
      //parse the date introduced by the customer and check if it's in the same format as the one specified earlier
    } catch (ParseException e) {
        System.out.println("Date must be in format 'DD-Mon-YY'");
        //if not, then tell the user the right format, and return false
        return false;
    }
    return true;
  }

  public static boolean checkDeliveryCollection (String orderPlaced, String checkingDate) {
    if (orderPlaced == null || checkingDate == null) {
      //if any of the dates passed as parameters are null, return false
      return false;
    }
    SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");
    //create our template format
    Date orderDate;
    Date checkDate;
    try {
      orderDate = sdf.parse(orderPlaced);
      //parse the first date to check if it is in the correct format
    } catch (ParseException e) {
        //if not return false
        System.out.println("Error creating order date");
        return false;
    }

    try {
      checkDate = sdf.parse(checkingDate);
      //parse the second date to check if it is in the correct format
    } catch (ParseException e) {
        //if not return false
        System.out.println("Error creating check date");
        return false;
    }

    if (checkDate.after(orderDate)) {
      //use in-built .after() function to check if the delivery/collection date(second date) is later than the order date (first date)
      return true;
      //if true return true
    } else {
      System.out.println("Delivery or Collection date is earlier than order date");
      //else tell the customer the error and return false
      return false;
    }
  }

  public static void sameProduct (ArrayList<Integer> productIDs, ArrayList<Integer> quantities) {
    for (int i = 0; i < productIDs.size()-1; i++) {
      for (int j = i+1; j < productIDs.size(); j++) {
        if(productIDs.get(i) == productIDs.get(j)) {
          int totalquantity = quantities.get(i) + quantities.get(j);
          quantities.set(i, totalquantity);
          productIDs.remove(j);
          quantities.remove(j);
        }
      }
    }
    //this method checks for duplicate product IDs within the productIDs ArrayList, if there are any then add their quantities
    //and store in the smaller index out of the two. Then remove the productID and the quantity from the space in the bigger index
  }
}
