import java.sql.*;
import java.util.Date;
import java.util.Scanner;
import java.text.SimpleDateFormat;

public class Customer {

    public static void customerLogin(Scanner sc, Connection conn) {
        String email;
        ResultSet r;
        String query = "select c_id from customer where email = ?";
        PreparedStatement smt;

        do {
            System.out.print("To go back, type 'back'. Otherwise, please enter your email: ");
            email = sc.nextLine();
            if (email.equals("back")) {
                Hurts.mainScreen(sc, conn);
                return;
            } else {
                try {
                    smt = conn.prepareStatement(query);
                    smt.setString(1, email);
                    r = smt.executeQuery();
                    if (r.next()) {
                        System.out.println("Login success!");
                        int id = r.getInt(1);
                        customerScreen(sc, conn, id);
                        return;
                    }
                } catch (SQLException e) {
                }
                System.out.println(
                        "Email not linked to a customer account. Make sure you spelled it correctly, otherwise type 'back' to create a new account.");
            }
        } while (true);
    }

    public static void customerScreen(Scanner sc, Connection conn, int c_id) {
		int choice;
		PreparedStatement smt;
		ResultSet r;

		try {
			smt = conn.prepareStatement("select first_name, last_name from customer where c_id = " + c_id);
			r = smt.executeQuery();
			r.next();
			System.out.println("Welcome back, " + r.getString(1) + " " + r.getString(2) + "!");
		} catch (Exception e) {
		}

		System.out.println("Here are your options.");
		System.out.println("1: View and request available cars");
		System.out.println("2: View past rentals and bills");
		System.out.println("3: Add a discount code.");
		System.out.println("4: Logout");

		while (true) {

			System.out.print("Which option would you like to do? ");
			try {
				choice = sc.nextInt();
				sc.nextLine();
				if (choice == 1) {
					pickACar(sc, conn, c_id);
					return;
				} else if (choice == 2) {
					viewBills(sc, conn, c_id);
					return;
				} else if (choice == 3) {
					addDiscount(sc, conn, c_id);
					return;
				} else if (choice == 4) {
					Hurts.mainScreen(sc, conn);
					return;
				}
			} catch (Exception e) {
			}
			System.out.println("Try again!");
		}
    }
    
    public static void newCustomer(Scanner sc, Connection conn) {
		String email;
		boolean valid = false;
		PreparedStatement smt, smt2;
		String query;
		ResultSet r;
		String input;
		SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");

		String[] fields = new String[] { "first name", "last name", "address line",
				"apartment line (hit enter to skip)", "city", "state (abbreviation)", "zip",
				"phone number (enter as a 10-digit number)", "date of birth (MM-DD-YYYY)", "driver's license" };
		int[] maxLengths = new int[] { 20, 20, 35, 20, 30, 20, 5, 10, 10, 15 };

		System.out.println("Welcome to Hurts! We're so happy to have you as a new customer.");
		while (!valid) {
			System.out.print(
					"Please enter your email to create an account. If you want to go back to the main menu, enter 'back': ");
			email = sc.nextLine();
			if (email.equals("back")) {
				Hurts.mainScreen(sc, conn);
				return;
			} else if (email.length() > 240) {
				System.out.println("Your email is too long! Try again.");

			} else {
				try {
					query = "select email from customer where email = '" + email + "'";
					smt = conn.prepareStatement(query);
					r = smt.executeQuery(query);
					if (r.next()) {
						System.out.println("You already have an account with that email!");
					} else {
						query = "insert into customer values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
						smt = conn.prepareStatement(query);
						smt.setString(12, email);
						for (int i = 0; i < fields.length; i++) {
							valid = false;
							while (!valid) {
								try {
									System.out.print("Enter your " + fields[i] + ": ");
									input = sc.nextLine();
									if (input.length() <= maxLengths[i]) {
										if (i < 6 || i == 9) {
											smt.setString(i + 2, input);
										} else if (i == 6 || i == 7) { // zip and phone number
											if (input.length() != maxLengths[i]) {
												throw new Exception();
											}
											smt.setLong(i + 2, Long.parseLong(input));
										} else if (i == 8) { // DOB
											if (input.length() != 10) {
												throw new Exception();
											}
											Date date = sdf.parse(input);
											long millis = date.getTime();
											smt.setDate(10, new java.sql.Date(millis));
										}
										valid = true;
									} else {
										System.out.println(
												"Too long. Try again! The max # of characters is " + maxLengths[i]);
									}
								} catch (NumberFormatException e) {
									System.out.println("Not a number. Try again.");
								} catch (Exception e) {
									System.out.println("Invalid format. Try again.");
								}
							}
						}
						smt2 = conn.prepareStatement("select max(c_id) from customer");
						r = smt2.executeQuery();
						if (r.next()) {
							smt.setInt(1, r.getInt(1) + 1);
						} else {
							smt.setInt(1, 1);
						}
						smt.executeUpdate();
						System.out.println("Account created successfully!");
						customerScreen(sc, conn, r.getInt(1) + 1);

					}
				} catch (SQLException e) {
					System.out.println(e.getErrorCode());
				}
			}
		}
    }

    public static void pickACar(Scanner sc, Connection conn, int c_id) {
		PreparedStatement smt, smt2;
		ResultSet r;
		String query = "select * from location";
		String input = "";
		int storeNum = 0;
		boolean valid = false;

		System.out.println("Here's a list of all our locations!");
		try {
			smt = conn.prepareStatement(query);
			r = smt.executeQuery();
			System.out.printf("%-13s %-35s %-20s %-30s %-5s %-5s %-15s\n", "Store Number", "Address", "Suite", "City",
					"State", "ZIP", "Phone Number");
			while (r.next()) {
				System.out.printf("%-13d %-35s %-20s %-30s %-5s %-5d %-15d\n", r.getInt(1), r.getString(2),
						r.getString(3), r.getString(4), r.getString(5), r.getInt(6), r.getLong(7));
			}
		} catch (SQLException e) {
			System.out.println(e.getErrorCode());
		}
		// Print out all locations with store number, address, city, state, zip order by
		// state, city, zip
		while (!valid) {
			System.out.print(
					"If you would like to go back, enter 'back'. Otherwise, enter the ID of the store whose cars you want to view: ");
			input = sc.nextLine();
			if (input.equals("back")) {
				customerScreen(sc, conn, c_id);
				return;
			}
			query = "select store_number from location where store_number = ?";
			try {
				storeNum = Integer.parseInt(input);
				smt = conn.prepareStatement(query);
				smt.setInt(1, storeNum);
				r = smt.executeQuery();
				if (r.next()) {
					valid = true;
				}
			} catch (Exception e) {
				System.out.println("Not a valid store number!");
			}
		}
		query = "select rv_id, type, make, model, year, to_char(hourly_rate, '99.00'), to_char(daily_rate, '999.00') from rental_vehicle natural join vehicle_type natural join vehicle_location where store_number = ? order by daily_rate";
		try {
			smt = conn.prepareStatement(query);
			smt.setInt(1, storeNum);
			r = smt.executeQuery();
			if (r.next()) {
				System.out.printf("%-5s %-15s %-15s %-23s %-6s %-12s %-12s \n", "ID", "Type", "Make", "Model", "Year",
						"Hourly Rate", "Daily Rate");
				do {
					System.out.printf("%-5d %-15s %-15s %-23s %-6d %-12s %-12s \n", r.getInt(1), r.getString(2),
							r.getString(3), r.getString(4), r.getInt(5), r.getString(6), r.getString(7));
				} while (r.next());
				valid = false;
				while (!valid) {
					System.out.print(
							"Enter 'back' to choose another store or go to the customer screen. Otherwise, enter the ID of the car you'd like to rent: ");
					input = sc.nextLine();
					if (input.equals("back")) {
						pickACar(sc, conn, c_id);
						return;
					} else {
						query = "select rv_id from vehicle_location where store_number = ? and rv_id = ?";
						try {
							smt = conn.prepareStatement(query);
							smt.setInt(1, storeNum);
							smt.setInt(2, Integer.parseInt(input));
							r = smt.executeQuery();
							if (r.next()) {
								valid = true;
							} else {
								System.out.println("Not a valid choice!");
							}
						} catch (Exception e) {
						}
					}
				}
				System.out.println("Request successfully recorded.");
				query = "insert into rental values (?, ?, ?, null, ?, null, null, null, null, null, null, null, null, null, null)";
				try {
					smt = conn.prepareStatement(query);
					smt2 = conn.prepareStatement("select max(r_id) from rental");
					r = smt2.executeQuery();
					if (r.next()) {
						smt.setInt(1, r.getInt(1) + 1);
					} else {
						smt.setInt(1, 1);
					}
					smt.setInt(2, c_id);
					smt.setInt(3, storeNum);
					smt.setInt(4, Integer.parseInt(input));
					smt.executeUpdate();
				} catch (SQLException e) {
				}
				customerScreen(sc, conn, c_id);
				return;
			} else {
				System.out.println("There are currently no vehicles at this location. Search again!");
				pickACar(sc, conn, c_id);
				return;
			}
		} catch (SQLException e) {
		}
    }
    
    public static void viewBills(Scanner sc, Connection conn, int c_id) {

		PreparedStatement smt;
		ResultSet r;
		String query = "select r_id, to_char(cast(start_date as date),'MM/DD/YYYY'), to_char(cast(end_date as date),'MM/DD/YYYY'), concat(concat(p.city, ', '), p.state), concat(concat(d.city, ', '), d.state),"
				+ "(general_charge + fuel_charge + insurance_charge + per_mile_charge + dropoff_charge + additional_fees - discount) as total from rental r "
				+ "join location p on r.pickup_loc=p.store_number join location d on r.dropoff_loc=d.store_number where c_id = ? order by r_id";

		try {
			smt = conn.prepareStatement(query);
			smt.setInt(1, c_id);
			r = smt.executeQuery();
			if (!r.next()) {
				System.out.println("You have no past bills.");
				customerScreen(sc, conn, c_id);
				return;
			}
			System.out.printf("%-7s %-17s %-17s %-22s %-22s %-8s\n", "R_ID", "Starting Date", "Ending Date",
					"Starting City", "Ending City", "Bill Total");
			do {
				System.out.printf("%-7d %-17s %-17s %-22s %-22s %-8.2f\n", r.getInt(1), r.getString(2), r.getString(3),
						r.getString(4), r.getString(5), r.getDouble(6));
			} while (r.next());

		} catch (SQLException e) {
			System.out.println(e.getErrorCode());
		}

		boolean valid = false;
		int r_id = 0;
		String input;
		query = "select r_id from rental where pickup_loc is not null and c_id = ? and r_id = ?";
		while (!valid) {
			System.out.print("Enter the R_ID of the bill you'd like to see in detail. Otherwise, enter back: ");
			try {
				input = sc.nextLine();
				if (input.equals("back")) {
					customerScreen(sc, conn, c_id);
					return;
				}
				r_id = Integer.parseInt(input);
				smt = conn.prepareStatement(query);
				smt.setInt(1, c_id);
				smt.setInt(2, r_id);
				r = smt.executeQuery();
				if (r.next()) {
					valid = true;
				} else {
					System.out.println("Not a valid choice.");
				}

			} catch (Exception e) {
				System.out.println("Not a number! Try again.");
			}
		}

		query = "select first_name, last_name, customer.address_line, customer.apt_line, customer.city, customer.state, customer.zip, to_char(cast(start_date as date),'MM-DD-YYYY'), to_char(cast(end_date as date),'MM-DD-YYYY'), concat(concat(p.city, ', '), p.state), concat(concat(d.city, ', '), d.state), make, model, year, general_charge, fuel_charge, insurance_charge, per_mile_charge, dropoff_charge, additional_fees, discount_amount, discount, (general_charge + fuel_charge + insurance_charge + per_mile_charge + dropoff_charge + additional_fees - discount) as total from rental r join rental_vehicle on r.rv_id = rental_vehicle.rv_id join vehicle_type on rental_vehicle.v_id = vehicle_type.v_id join customer on r.c_id = customer.c_id left join customer_discount on customer.c_id = customer_discount.c_id left join discount_group on customer_discount.code = discount_group.code join location p on r.pickup_loc=p.store_number join location d on r.dropoff_loc=d.store_number where r_id = ?";
		try {
			smt = conn.prepareStatement(query);
			smt.setInt(1, r_id);
			r = smt.executeQuery();
			r.next();

			System.out.println("\n\nBILL FOR RENTAL #" + r_id);
			System.out.println(r.getString(1) + " " + r.getString(2));
			System.out.println(r.getString(3));
			if (r.getString(4) != null) {
				System.out.println(r.getString(4));
			}
			System.out.println(r.getString(5) + ", " + r.getString(6) + " " + r.getString(7));
			System.out.println("\n");
			System.out.println("Trip dates: " + r.getString(8) + " to " + r.getString(9) + "(" + r.getString(10)
					+ " -> " + r.getString(11) + ")");
			System.out.println("Vehicle: " + r.getString(12) + " " + r.getString(13) + " " + r.getInt(14));
			System.out.println("\n");
			System.out.println("CHARGES");
			System.out.printf("%-20s $%9.2f\n", "General Charge", r.getDouble(15));
			System.out.printf("%-20s $%9.2f\n", "Fuel Charge", r.getDouble(16));
			System.out.printf("%-20s $%9.2f\n", "Insurance Charge", r.getDouble(17));
			System.out.printf("%-20s $%9.2f\n", "Per-Mile Charge", r.getDouble(18));
			System.out.printf("%-20s $%9.2f\n", "Dropoff Charge", r.getDouble(19));
			System.out.printf("%-20s $%9.2f\n", "Additional Charges", r.getDouble(20));

			if (r.getDouble(21) > 0) {
				System.out.printf("%-19s ($%9.2f)\n", "Discount (" + r.getDouble(21) + "%)", r.getDouble(22));
			}

			System.out.printf("\n%-20s $%9.2f\n", "GRAND TOTAL", r.getDouble(23));

		} catch (SQLException e) {
			System.out.println("OOPSIE!");
			System.out.println(e.getErrorCode());
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		System.out.print("Press enter to continue.");
		sc.nextLine();
		System.out.println();
		customerScreen(sc, conn, c_id);

	}

	public static void addDiscount(Scanner sc, Connection conn, int c_id) {
		PreparedStatement smt;
		ResultSet r;
		String query = "select code from discount_group where code = ?";
		double oldDiscount = 0;
		double newDiscount = 0;

		while (true) {

			System.out.print("To go back, type 'back'. Otherwise, enter your discount code: ");
			String code = sc.nextLine();
			if (code.equals("back")) {
				customerScreen(sc, conn, c_id);
				return;
			} else {
				code = code.toUpperCase();
				try {
					smt = conn.prepareStatement(query);
					smt.setString(1, code);
					r = smt.executeQuery();
					if (r.next()) {
						query = "select discount_amount from discount_group natural join customer_discount where c_id = ?";
						smt = conn.prepareStatement(query);
						smt.setInt(1, c_id);
						r = smt.executeQuery();
						if (r.next()) {
							oldDiscount = r.getDouble(1);
						}
						query = "select discount_amount from discount_group where code = ?";
						smt = conn.prepareStatement(query);
						smt.setString(1, code);
						r = smt.executeQuery();
						r.next();
						newDiscount = r.getDouble(1);
						if (newDiscount < oldDiscount) { // Worse Discount is not applied
							System.out.println("This is a worse discount than your old one. Discount not applied.");
							customerScreen(sc, conn, c_id);
							return;
						} else {
							if (oldDiscount > 0) { // replacing the old discount
								query = "update customer_discount set code = ? where c_id = ?";
								smt = conn.prepareStatement(query);
								smt.setString(1, code);
								smt.setInt(2, c_id);
								smt.executeUpdate();
							} else {
								query = "insert into customer_discount(c_id, code) values(?, ?)";
								smt = conn.prepareStatement(query);
								smt.setInt(1, c_id);
								smt.setString(2, code);
								smt.executeUpdate();
							}
							query = "select company_name from discount_group where code = ?";
							smt = conn.prepareStatement(query);
							smt.setString(1, code);
							r = smt.executeQuery();
							r.next();
							System.out.println("Your discount of " + newDiscount + "% from " + r.getString(1)
									+ " has now been applied.");
							customerScreen(sc, conn, c_id);
							return;
						}
					} else {
						System.out.println("Invalid code. Try again.");
					}
				} catch (SQLException e) {
					System.out.println(e.getErrorCode());
				}
			}

		}

	}

}
