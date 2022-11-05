import java.sql.*;
import java.util.ArrayList;

import org.opencv.core.Point;

// JDBC implementation
public class MiddleTier {
	
	// Get connection to database
	public Connection getConnection() throws Exception{
		
		Class.forName("com.mysql.cj.jdbc.Driver");
		String URL = "jdbc:mysql://127.0.0.1:3306/detect_defect";
		Connection connection = DriverManager.getConnection(URL, "root", "root1234");
		
		return connection;
	}
	
	// To execute the sql String
	public void execute(String sql) throws Exception {
		
		Connection connection = getConnection();
		Statement stmt = connection.createStatement();
		stmt.executeUpdate(sql);
		
		stmt.close();
		connection.close();
	}
	
	// Create table with name "Contour(i)"
	public void create_table(int i) throws Exception {
		String sql = String.format("CREATE TABLE Contour%d" , i) + "(num INTEGER not NULL, " +
				" x DOUBLE not NULL, " + "y DOUBLE not NULL, " + " PRIMARY KEY (num))";
		execute(sql);
	}
	
	// Insert values to the tables
	public void insert_value(int i, int num, double x, double y) throws Exception {
		String sql = String.format("INSERT INTO Contour%d VALUES (%d, %f, %f )", i, num, x, y );
		execute(sql);
	}
	
	// Get the melt pool contour as an array of points from database
	public ArrayList<Point> get_Points(int i) throws Exception{
		
		ArrayList<Point> result = new ArrayList<Point>();
		
		Connection connection = getConnection();
		Statement stmt = connection.createStatement();
		String sql = String.format("SELECT x, y FROM Contour%d", i);
		ResultSet rs = stmt.executeQuery(sql);
		
		while(rs.next()) {
			double x = rs.getDouble("x");
			double y = rs.getDouble("y");
			Point p = new Point(x, y);
			result.add(p);
		}
		
		stmt.close();
		connection.close();
		
		return result;
	}
}
