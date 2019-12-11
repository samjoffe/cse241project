import java.io.*;
import java.sql.*;
import java.util.Date;
import java.util.Scanner;
import java.text.SimpleDateFormat;

public class Hurts {

	public static void main(String[] args) throws IOException, SQLException, java.lang.ClassNotFoundException {

		String user, pwd;
		Scanner sc = new Scanner(System.in);
		Connection conn = null;
		boolean valid = false;
		Statement smt;
		Console sys = System.console();

		System.out.println("Please sign in to the Oracle database.");
		while (!valid) {
			try {
				System.out.print("Please enter your username: ");
				user = sc.next().trim();
				System.out.print("Please enter your password: ");
				pwd = String.valueOf(sys.readPassword()); // Hide password
				sc.nextLine();
				conn = DriverManager.getConnection("jdbc:oracle:thin:@edgar1.cse.lehigh.edu:1521:cse241", user, pwd);
				smt = conn.createStatement();
				valid = true;

			} catch (SQLException sqle) {
				System.out.println("Username or password was invalid. Please try again.");
			}
		}

		mainScreen(sc, conn);

		conn.close();
		sc.close();

	}

	public static void mainScreen(Scanner sc, Connection conn) {
		int choice;
		boolean valid = false;

		System.out.println("Welcome to Hurts Rent-A-Lemon!");

		while (!valid) {
			System.out.println("Please select an option below.");
			System.out.println("1: Customer Login");
			System.out.println("2: Employee Login");
			System.out.println("3: Create Customer Account");
			System.out.println("4: Quit");

			try {
				choice = Integer.parseInt(sc.nextLine());
				if (choice == 1) {
					Customer.customerLogin(sc, conn);
					valid = true;
				} else if (choice == 2) {
					Employee.employeeLogin(sc, conn);
					valid = true;
				} else if (choice == 3) {
					Customer.newCustomer(sc, conn);
					valid = true;
				} else if (choice == 4) {
					return;
				} else {
					throw new Exception();
				}
			} catch (Exception e) {
				System.out.println("Not a valid choice! Try again.");
			}
		}
	}
}