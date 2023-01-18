import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import oracle.jdbc.driver.*;
import oracle.sql.*;
import oracle.sql.DATE;

public class G17 {

	static int MemberID;
	
	public static OracleConnection loginOracle() throws SQLException {
		Console console = System.console();
		System.out.print("Enter Oracle username: "); 
		String username = console.readLine(); 
		System.out.print("Enter Oracle password: "); 
		char[] password = console.readPassword();
		String pwd = String.valueOf(password);
		DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
		OracleConnection conn = (OracleConnection)DriverManager.getConnection(
				"jdbc:oracle:thin:@studora.comp.polyu.edu.hk:1521:dbms",username,pwd);
		return conn;
	} 
	
	private static void loginmenu(OracleConnection conn) throws SQLException{
		Console console = System.console();
		String chose = "";
		
		while (chose != "1" && chose != "2") {
			System.out.println("1:login\n"+ "2:exit");
			chose = console.readLine();
			
			switch (chose) {
			case "1":
				login(conn);
				break;
				
			case "2":
				conn.close();
				System.exit(0);
				break;
			default:
				System.out.println("wrong input");
				break;
			}
		}
	}
	
	private static void login(OracleConnection conn) throws SQLException{
		Statement stmt;
		ResultSet rset;
		Console console = System.console();
		System.out.print("Enter your Member ID: "); 
		String Mid = console.readLine();
		System.out.print("Enter your password: "); 
		char[] Mpw = console.readPassword();
		String Mpwd = String.valueOf(Mpw);

		//admin login
		if (Mid.matches("-1") && Mpwd.matches("-1")) {
			AdminMenu(conn);
			return;
		}else{
			int intMid = Integer.parseInt(Mid);

			//member login
			stmt = conn.createStatement();
			rset = stmt.executeQuery("SELECT Member_id FROM Member WHERE Member_id =" + "'" + intMid + "'" 
					+ " AND Member_pw =" + "'" + Mpwd + "'");
			if(rset.next()) {
				MemberID = Integer.parseInt(Mid);
				MemberMenu(conn);
				return;
			}else {
				System.out.println("incorrect Member ID or password");
				loginmenu(conn);
				return;
			}
		}
	}

	private static void MemberMenu(OracleConnection conn) throws SQLException {
		Notification(conn);
		Statement stmt;
		ResultSet rset;
		
		stmt = conn.createStatement();
		rset = stmt.executeQuery("SELECT status FROM member WHERE member_id = '" + MemberID + "'");
		if (rset.next()) {
			if (rset.getString(1).matches("deactive")) {
				System.out.println("Your account is deactivated, please return your books as soon as possible");
				loginmenu(conn);
				return;
			}
			Console console = System.console();
			System.out.println("================================"); 
			System.out.println("1:Search book\n"
					+ "2:Record\n"
					+ "3:Reserved Book\n"
					+ "4:Logout\n");
			System.out.print("Input: ");
			String chose = console.readLine();
			switch(chose) {
				case "1":
					memSearchMenu(conn);
					break;
				case "2":
					memRecord(conn);
					break;
				case "3":
					memReserveBook(conn);
				case "4":
					loginmenu(conn);
					break;
			default:
				MemberMenu(conn);	
			}
		}

	}
	
	private static void memReserveBook(OracleConnection conn) throws SQLException {
		Statement stmt;
		ResultSet rset;
		Console console = System.console();
		System.out.print("Enter the Book ID for the book you want to reserve: "); 
		String ReserveID = console.readLine();
		int Rid = Integer.parseInt(ReserveID);
		stmt = conn.createStatement();
		rset = stmt.executeQuery("SELECT status FROM book where book_id = " + "'" + Rid + "'");
		if (rset.next()) {
			if(rset.getString(1).matches("shelf")) {
				stmt.executeUpdate("update book set status = 'reserved' WHERE book_id = " + "'" + Rid + "'");
				stmt.executeUpdate("INSERT INTO reserved(member_id,book_id) VALUES(" + MemberID +"," + Rid + ")");
				System.out.println("Reserve successful");
				MemberMenu(conn);
			}else {
				System.out.println("Selected book is already been reserved");
				MemberMenu(conn);
				return;
			}
		}else {
			System.out.println("Wrong Book ID");
			MemberMenu(conn);
		}
	}

	private static void Notification(OracleConnection conn) throws SQLException {
		Statement stmt;
		ResultSet rset;
		
		stmt = conn.createStatement();
		rset = stmt.executeQuery("SELECT Book.Book_name, Book.storing_location FROM Book, Reserved WHERE Reserved.Member_ID ="
				+ "'" + MemberID + "'" + " AND Book.Book_id = Reserved.Book_id");
		if(rset.next()) {
			System.out.println("Your reserved book is available now ");
			System.out.flush();
			System.out.println(rset.getString(1) + " " +
					rset.getString(2));
		}
		while (rset.next())
		{
			System.out.flush();
			System.out.println(rset.getString(1) + " " +
					rset.getString(2));
		}
	}

	private static void AdminMenu(OracleConnection conn) throws SQLException {
		Console console = System.console();
		System.out.println("================================"); 
		System.out.println("1:Search book\n"
				+ "2:deactive/active Member\n"
				+ "3:show all member status\n"
				+ "4:Record\n"
				+ "5:insert book\n"
				+ "6:logout\n");
		System.out.print("Input: ");
		String chose = console.readLine();
		switch(chose) {
			case "1":
				AdminSearchMenu(conn);
				break;
			case "2":
				activationAccount(conn);
				break;
			case "3":
				showAllMember(conn);
				break;
			case "4":
				AdminRecord(conn);
				break;
			case "5":
				insertbook(conn);
				break;
			case "6":
				loginmenu(conn);
				break;
		default:
			AdminMenu(conn);
		}
	}
	private static void insertbook(OracleConnection conn) throws SQLException {
		Console console = System.console();
		System.out.print("Enter the author: "); 
		String author = console.readLine(); 
		System.out.print("Enter the publisher: "); 
		String publisher = console.readLine();
		System.out.print("Enter the book name: "); 
		String book_name = console.readLine(); 
		System.out.print("Enter the category: "); 
		String category = console.readLine();
		System.out.print("Enter the publish year: "); 
		String publish_year = console.readLine(); 
		System.out.print("Enter the storing location: "); 
		String storing_location = console.readLine();
		String status = "shelf";
		
		if (author.isBlank()||publisher.isBlank()||book_name.isBlank()||category.isBlank()||
				publish_year.isBlank()||storing_location.isBlank()) {
			System.out.println("missing information");
			AdminMenu(conn);
			return;
		}
		// insert the data
		Statement statement = conn.createStatement();
		statement.executeUpdate("INSERT INTO Book(Author, Status, Publisher, Book_name, Category, Publish_year, Storing_location)"
				+ " VALUES(" 
				+ "'" + author + "'" + ","
				+ "'" + status + "'" + "," 
				+ "'" + publisher + "'" + "," 
				+ "'" + book_name + "'" + "," 
				+ "'" + category + "'" + "," 
				+ "'" + publish_year + "'" + "," 
				+ "'" + storing_location + "'" + ")");
		System.out.println("Insert complete!");
		AdminMenu(conn);
	}

	private static void showAllMember(OracleConnection conn) throws SQLException {

		Statement stmt;
		ResultSet rset;
		
		stmt = conn.createStatement();
		rset = stmt.executeQuery("SELECT Member_id, Status FROM Member");
		while (rset.next())
		{
			System.out.flush();
			System.out.println(rset.getInt(1) + " " +
					rset.getString(2));
		}
		AdminMenu(conn);
	}

	private static void memSearchMenu(OracleConnection conn) throws SQLException {	
		String name = "";
		String author = "";
		String category = "";
		Console console = System.console();
		System.out.println("================================"); 
		System.out.println("input the name(leave blank if you don't have");
		name = console.readLine();
		if(name == "") 
			name = "%%";
		
		System.out.println("input the author(leave blank if you don't have");
		author = console.readLine();
		if(author == "") 
			author = "%%";
		
		System.out.println("input the category(leave blank if you don't have");
		category = console.readLine();
		if(category == "") 
			category = "%%";
		
		memSearch(conn, name, author, category);
	}
	
	
	private static void memSearch(OracleConnection conn, String name, String author, String category) throws SQLException {
		Statement stmt;
		ResultSet rset;

		stmt = conn.createStatement();
		String x = "SELECT * FROM Book Where " + 
		"BOOK_NAME like '%" + name + 
		"%' AND AUTHOR like '%" + author + 
		"%' AND CATEGORY like'%" + category +"%'";
		rset = stmt.executeQuery(x);
		while (rset.next())
		{
			System.out.flush();
			System.out.println(rset.getInt(1) + " " +
					rset.getString(2) + " " +
					rset.getString(3) + " " +
					rset.getString(4) + " " +
					rset.getString(5) + " " +
					rset.getString(6) + " " +
					rset.getString(7) + " " +
					rset.getString(8));
		}
		System.out.println("Enter 1 to continue search, other to exit to menu");
		Console console = System.console();
		String chose = console.readLine();
		switch(chose) {
			case "1":
				memSearchMenu(conn);
				break;
			default:
				MemberMenu(conn);
		}
	}
	
	private static void AdminSearchMenu(OracleConnection conn) throws SQLException {	
		String name = "";
		String author = "";
		String category = "";
		Console console = System.console();
		System.out.println("================================"); 
		System.out.println("input the name(leave blank if you don't have");
		name = console.readLine();
		if(name == "") 
			name = "%%";
		
		System.out.println("input the author(leave blank if you don't have");
		author = console.readLine();
		if(author == "") 
			author = "%%";
		
		System.out.println("input the category(leave blank if you don't have");
		category = console.readLine();
		if(category == "") 
			category = "%%";
		
		AdminSearch(conn, name, author, category);
	}
	
	
	private static void AdminSearch(OracleConnection conn, String name, String author, String category) throws SQLException {
		Statement stmt;
		ResultSet rset;

		// Prepare SQL for request
		stmt = conn.createStatement();
		String x = "SELECT * FROM Book Where " + 
		"BOOK_NAME like '%" + name + 
		"%' AND AUTHOR like '%" + author + 
		"%' AND CATEGORY like'%" + category +"%'";
		rset = stmt.executeQuery(x);
		while (rset.next())
		{
			System.out.flush();
			System.out.println(rset.getInt(1) + " " +
					rset.getString(2) + " " +
					rset.getString(3) + " " +
					rset.getString(4) + " " +
					rset.getString(5) + " " +
					rset.getString(6) + " " +
					rset.getString(7) + " " +
					rset.getString(8));
		}
		System.out.println("Enter 1 to continue search, other to exit to menu");
		Console console = System.console();
		String chose = console.readLine();
		switch(chose) {
			case "1":
				AdminSearchMenu(conn);
				break;
			default:
				AdminMenu(conn);
		}
	}
	private static void activationAccount(OracleConnection conn) throws SQLException {
		Statement stmt;
		Date today = new Date(System.currentTimeMillis());
		
		stmt = conn.createStatement();
		System.out.println(today);
		stmt.executeUpdate("update member set Status = 'deactive' where member.member_ID IN (select borrowed.member_ID From Borrowed where due_date < " 
				+ "date '" + today + "')");
		stmt.executeUpdate("update member set Status = 'active' where member.member_ID NOT IN (select borrowed.member_ID FROM Borrowed)");
		System.out.println("Update complete!");
		AdminMenu(conn);
	}
	
	private static void AdminRecord(OracleConnection conn) throws SQLException {
		Statement stmt;
		ResultSet rset;
		Console console = System.console();
		System.out.println("================================"); 
		System.out.println("Input 1 for search all book status");
		System.out.println("Input 2 for search for specific book"); 
		String chose = console.readLine();
		switch(chose) {
			case "1":
				stmt = conn.createStatement();
				rset = stmt.executeQuery("SELECT book_id, book_name, status FROM Book");
				while (rset.next())
				{
					System.out.flush();
					System.out.println(rset.getInt(1) + " " +
							rset.getString(2) + " " +
							rset.getString(3));
				};
				AdminMenu(conn);
				break;
				
			case "2":
				System.out.print("Enter the book name: "); 
				String book_name = console.readLine();
				stmt = conn.createStatement();
				rset = stmt.executeQuery("SELECT book_id, book_name, status FROM Book WHERE book_name LIKE " 
				+ "'%" + book_name + "%'");
				while (rset.next())
				{
					System.out.flush();
					System.out.println(rset.getInt(1) + " " +
							rset.getString(2) + " " +
							rset.getString(3));
				};
				AdminMenu(conn);
				break;
				
			default:
				AdminMenu(conn);
		}		
	}
	private static void memRecord(OracleConnection conn) throws SQLException {
		Statement stmt;
		ResultSet rset;
		Console console = System.console();
		System.out.println("================================"); 
		System.out.println("Input 1 for search all book status");
		System.out.println("Input 2 for search for specific book status by name"); 
		String chose = console.readLine();
		switch(chose) {
			case "1":
				stmt = conn.createStatement();
				rset = stmt.executeQuery("SELECT book_id, book_name, status FROM Book");
				while (rset.next())
				{
					System.out.flush();
					System.out.println(rset.getInt(1) + " " +
							rset.getString(2) + " " +
							rset.getString(3));
				};
				MemberMenu(conn);
				break;
				
			case "2":
				System.out.print("Enter the book name: "); 
				String book_name = console.readLine();
				stmt = conn.createStatement();
				rset = stmt.executeQuery("SELECT book_id, book_name, status FROM Book WHERE book_name LIKE " 
				+ "'%" + book_name + "%'");
				while (rset.next())
				{
					System.out.flush();
					System.out.println(rset.getInt(1) + " " +
							rset.getString(2) + " " +
							rset.getString(3));
				};
				MemberMenu(conn);
				break;
				
			default:
				MemberMenu(conn);
		}		
	}

	public static void main(String args[]) throws SQLException, IOException{
		// Connection
		OracleConnection conn = loginOracle();
		loginmenu(conn);
	}
}
