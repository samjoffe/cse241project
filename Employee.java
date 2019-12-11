import java.util.Date;
import java.util.Scanner;
import java.sql.*;
import java.text.SimpleDateFormat;

public class Employee {

    public static void employeeLogin(Scanner sc, Connection conn) {
        String storeNum;
        PreparedStatement smt;
        ResultSet r;
        String query = "select store_number from location where store_number = ?";

        do {
            System.out.print("To go back, type 'back'. Otherwise, please enter your store number: ");
            storeNum = sc.next();
            if (storeNum.equals("back")) {
                sc.nextLine();
                Hurts.mainScreen(sc, conn);
                return;
            } else {
                try {
                    smt = conn.prepareStatement(query);
                    smt.setInt(1, Integer.parseInt(storeNum));
                    r = smt.executeQuery();
                    if (r.next()) {
                        System.out.println("Login success!");
                        employeeScreen(sc, conn, Integer.parseInt(storeNum));
                        return;
                    }
                } catch (SQLException e) {
                }
                System.out.println("Invalid store number. Please try again.");
            }
        } while (true);
    }

    public static void employeeScreen(Scanner sc, Connection conn, int storeNum) {
        int choice;

        System.out.println("Hello Store " + storeNum + " employee!");
        System.out.println("What would you like to do?");
        System.out.println("1: Rent a requested vehicle");
        System.out.println("2: Return a vehicle");
        System.out.println("3: Logout");

        while (true) {

            System.out.print("Which option would you like to do? ");
            try {
                choice = sc.nextInt();
                sc.nextLine();
                if (choice == 1) {
                    CarCheckout(sc, conn, storeNum);
                    return;
                } else if (choice == 2) {
                    returnCar(sc, conn, storeNum);
                    return;
                } else if (choice == 3) {
                    Hurts.mainScreen(sc, conn);
                    return;
                }
            } catch (Exception e) {
            }
            System.out.println("Try again!");
        }
    }

    public static void CarCheckout(Scanner sc, Connection conn, int storeNum) {

        PreparedStatement smt;
        ResultSet r;
        String query = "select r_id, first_name, last_name, email, rv_id, make, model, year from rental natural join customer natural join rental_vehicle natural join vehicle_type where pickup_loc = ? and start_date is null";
        boolean valid = false;
        int r_id = 0;
        boolean insurance = false;
        String response;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

        try {
            smt = conn.prepareStatement(query);
            smt.setInt(1, storeNum);
            r = smt.executeQuery();

            if (!r.next()) {
                System.out.println("There are no requests at this location.");
                employeeScreen(sc, conn, storeNum);
                return;
            }
            System.out.printf("%-6s %-20s %-20s %-30s %-7s %-15s %-23s %4s\n", "R_ID", "First Name", "Last Name",
                    "Email", "RV_ID", "Make", "Model", "Year");
            do {
                System.out.printf("%-6d %-20s %-20s %-30s %-7d %-15s %-23s %4d\n", r.getInt(1), r.getString(2),
                        r.getString(3), r.getString(4), r.getInt(5), r.getString(6), r.getString(7), r.getInt(8));
            } while (r.next());

        } catch (Exception e) {
        }
        while (!valid) { // Select the request that you'd like to process
            try {
                System.out.println("Select the R_ID  of the rental you'd like to proceed with: ");
                r_id = sc.nextInt();
                query = "select r_id from rental where pickup_loc = ? and r_id = ? and start_date is null"; // Must be
                                                                                                            // at that
                                                                                                            // location
                                                                                                            // and not
                                                                                                            // fulfilled
                                                                                                            // yet
                smt = conn.prepareStatement(query);
                smt.setInt(1, storeNum);
                smt.setInt(2, r_id);
                r = smt.executeQuery();
                sc.nextLine();
                if (r.next()) {
                    valid = true;
                } else {
                    System.out.println("Not a valid choice!");
                }
            } catch (Exception e) {
                System.out.println("Please enter a number!");
            }
        }
        // Next, check to make sure the car is still at that location. If it is not, the
        // request is deleted, otherwise, continue on to checkout.
        query = "select rv_id from rental natural join vehicle_location where store_number = pickup_loc and r_id = ?";
        try {
            smt = conn.prepareStatement(query);
            smt.setInt(1, r_id);
            r = smt.executeQuery();
            if (!r.next()) { // VEHICLE IS NO LONGER AT THAT LOCATION. DELETE RENTAL, MAKE NEW REQUEST
                System.out.println(
                        "Sorry, the vehicle that was requested is not here right now. Please have the customer fill out a new request.");
                System.out.println("The request is being deleted from the system.");
                query = "delete from rental where r_id = ?";
                smt = conn.prepareStatement(query);
                smt.setInt(1, r_id);
                smt.executeUpdate();
                employeeScreen(sc, conn, storeNum);
                return;
            } else { // Vehicle is still at that location, proceed to checkout
                valid = false;
                while (!valid) {
                    System.out.print("Would the customer like to purchase insurance for $10/day? (Y/N) ");
                    response = sc.nextLine();
                    if (response.toUpperCase().equals("Y")) {
                        valid = true;
                        insurance = true;
                    } else if (response.toUpperCase().equals("N")) {
                        valid = true;
                    } else {
                        System.out.println("Invalid response. Try again.");
                    }
                }
            }
        } catch (Exception e) {
        }
        query = "UPDATE rental SET start_date = ?, insurance_charge = ? WHERE r_id = ?";
        valid = false;
        while (!valid) {
            System.out.print(
                    "Please enter the date and time of the start of the rental in the form yyyy/MM/dd HH:mm:ss ");
            try {
                String myDate = sc.nextLine();
                Date date = sdf.parse(myDate);
                long millis = date.getTime();
                smt = conn.prepareStatement(query);
                smt.setTimestamp(1, new Timestamp(millis));
                if (insurance) {
                    smt.setDouble(2, 10);
                } else {
                    smt.setDouble(2, 0);
                }
                smt.setInt(3, r_id);
                valid = true;
                smt.executeUpdate();
                System.out.println("The car is ready to be driven!");
                // Remove car from vehicles that can be selected
                query = "select rv_id from rental where r_id = ?";
                smt = conn.prepareStatement(query);
                smt.setInt(1, r_id);
                r = smt.executeQuery();
                r.next();
                int rv_id = r.getInt(1);
                query = "delete from vehicle_location where rv_id = ?";
                smt = conn.prepareStatement(query);
                smt.setInt(1, rv_id);
                smt.executeUpdate();
            } catch (Exception e) {
                System.out.println("Invalid date. Try again!");
            }
        }
        employeeScreen(sc, conn, storeNum);
    }

    public static void returnCar(Scanner sc, Connection conn, int storeNum) {

        PreparedStatement smt;
        ResultSet r;
        String query = "select rv_id from rental where end_date is null and start_date is not null and rv_id = ?";
        boolean valid = false;
        String input = "";
        int r_id = 0;
        long start_millis = 0;
        long new_millis = 0;
        double hourly_rate = 0;
        int oldOdometer = 0;
        int newOdometer = 0;
        int milesDriven = 0;
        boolean insurance = false;
        boolean dropoff = false;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        int gallonsNeeded = 0;
        double additionalFees = 0;
        double discount_amount = 0;

        while (!valid) {
            System.out.print(
                    "To go back, type 'back'. Otherwise, please enter the RV_ID of the vehicle that is being returned: ");
            try {
                input = sc.nextLine();
                if (input.equals("back")) {
                    employeeScreen(sc, conn, storeNum);
                    return;
                }
                smt = conn.prepareStatement(query);
                smt.setInt(1, Integer.parseInt(input));
                r = smt.executeQuery();
                if (r.next()) {
                    valid = true;
                } else {
                    System.out.println("That is not an RV_ID for an active rental.");
                }
            } catch (Exception e) {
                System.out.println("Please enter an integer.");
            }
        }
        query = "select first_name, last_name, email, make, model, year, to_char(cast(start_date as date),'MM-DD-YYYY') from rental natural join rental_vehicle natural join customer natural join vehicle_type where rv_id = ? and end_date is null and start_date is not null";
        try {
            smt = conn.prepareStatement(query);
            smt.setInt(1, Integer.parseInt(input));
            r = smt.executeQuery();
            r.next();
            System.out.printf("%-20s %-20s %-40s %-15s %-15s %-4s %-12s\n", "First", "Last", "Email", "Make", "Model",
                    "Year", "Rental Start");
            System.out.printf("%-20s %-20s %-40s %-15s %-15s %-4d %-12s\n", r.getString(1), r.getString(2),
                    r.getString(3), r.getString(4), r.getString(5), r.getInt(6), r.getString(7));
        } catch (Exception e) {
        }
        valid = false;
        while (!valid) {
            System.out.print("Would you like to proceed with the above return? (Y/N) ");
            String response = sc.nextLine();
            if (response.toUpperCase().equals("Y")) {
                valid = true;
            } else if (response.toUpperCase().equals("N")) {
                CarCheckout(sc, conn, storeNum);
                return;
            } else {
                System.out.println("Invalid response. Try again.");
            }
        }

        query = "select r_id from rental where rv_id = ? and start_date is not null and end_date is null";
        try {
            smt = conn.prepareStatement(query);
            smt.setInt(1, Integer.parseInt(input));
            r = smt.executeQuery();
            r.next();
            r_id = r.getInt(1);
        } catch (Exception e) {
        }

        query = "select start_date, hourly_rate, odometer_reading, insurance_charge, pickup_loc, discount_amount from rental join rental_vehicle on rental.rv_id = rental_vehicle.rv_id join customer on rental.c_id = customer.c_id left join customer_discount on customer.c_id = customer_discount.c_id left join discount_group on customer_discount.code = discount_group.code where r_id = ?";
        try {
            smt = conn.prepareStatement(query);
            smt.setInt(1, r_id);
            r = smt.executeQuery();
            r.next();
            start_millis = r.getTimestamp(1).getTime();
            hourly_rate = r.getDouble(2);
            oldOdometer = r.getInt(3);
            insurance = (r.getInt(4) == 10);
            dropoff = !(r.getInt(5) == storeNum);
            discount_amount = r.getDouble(6);

        } catch (Exception e) {
        }

        System.out.println("The old odometer reading was " + oldOdometer + ".");
        valid = false;
        while (!valid) {
            System.out.print("Enter the new odometer reading: ");
            try {
                newOdometer = Integer.parseInt(sc.nextLine());
                if (newOdometer < oldOdometer) {
                    throw new Exception();
                }
                milesDriven = newOdometer - oldOdometer;
                valid = true;
            } catch (Exception e) {
                System.out.println("Invalid odometer reading. Try again.");
            }
        }

        query = "update rental_vehicle set odometer_reading = ? where rv_id = ?";
        try {
            smt = conn.prepareStatement(query);
            smt.setInt(1, newOdometer);
            smt.setInt(2, Integer.parseInt(input));
            smt.executeUpdate();
        } catch (Exception e) {
        }

        valid = false;
        while (!valid) {
            System.out.print("Enter the return time (yyyy/MM/dd HH:mm:ss) ");
            try {
                Date date = sdf.parse(sc.nextLine());
                new_millis = date.getTime();
                if (new_millis < start_millis) {
                    System.out.println("Make sure return time is after the start time!");
                } else {
                    valid = true;
                }
            } catch (Exception e) {
                System.out.println("Invalid date format.");
            }
        }

        query = "update rental set end_date = ?, dropoff_loc = ?, miles_driven = ? where r_id = ?";
        try {
            smt = conn.prepareStatement(query);
            smt.setTimestamp(1, new java.sql.Timestamp(new_millis));
            smt.setInt(2, storeNum);
            smt.setInt(3, milesDriven);
            smt.setInt(4, r_id);
            smt.executeUpdate();
        } catch (SQLException e) { // ORA-01747
            System.out.println("YO!");
            System.out.println(e.getErrorCode());
        }

        valid = false;
        while (!valid) {
            System.out.print("How many gallons of fuel needed? (Round to the nearest gallon, enter 0 if full) ");
            try {
                gallonsNeeded = Integer.parseInt(sc.nextLine());
                if (gallonsNeeded < 0 || gallonsNeeded > 50) {
                    throw new Exception();
                }
                valid = true;
            } catch (Exception e) {
                System.out.println("Invalid number of gallons.");
            }
        }

        valid = false;
        while (!valid) {
            System.out.print("How much charge for damages? (Enter 0 if there is no damage) ");
            try {
                additionalFees = Double.parseDouble(sc.nextLine());
                if (additionalFees < 0 || additionalFees > 1000000) {
                    throw new Exception();
                }
                additionalFees *= 100;
                additionalFees = (double) (Math.round(additionalFees)) / 100.0; // Rounds to 2 decimal places
                valid = true;
            } catch (Exception e) {
                System.out.println("Invalid damage amount.");
            }
        }

        // BILL CALCULATIONS
        double general_charge;
        long fuel_charge;
        int dropoff_charge = 0;
        long insurance_charge = 0;
        double per_mile_charge = 0;
        double subtotal;
        double discount = 0;
        double total;

        double hours = Math.ceil((new_millis - start_millis) / (1000.0 * 60 * 60));
        double days = Math.ceil((new_millis - start_millis) / (1000.0 * 60 * 60 * 24));

        if (hours <= 8) {
            general_charge = hours * hourly_rate;
        } else {
            general_charge = days * (8 * hourly_rate);
        }

        fuel_charge = 20 * gallonsNeeded;

        if (dropoff) {
            dropoff_charge = 200;
        }

        if (insurance) {
            insurance_charge = 10 * (long) days;
        }

        if (milesDriven > (200 * days)) {
            per_mile_charge = 0.50 * (milesDriven - (200 * days));
        }

        subtotal = general_charge + fuel_charge + insurance_charge + per_mile_charge + dropoff_charge + additionalFees;

        discount = subtotal * discount_amount * 0.01;

        total = subtotal - discount;

        try {
            query = "update rental set general_charge = ?, fuel_charge = ?, insurance_charge = ?, per_mile_charge = ?, dropoff_charge = ?, additional_fees = ?, discount = ? where r_id = ?";
            smt = conn.prepareStatement(query);
            smt.setDouble(1, general_charge);
            smt.setLong(2, fuel_charge);
            smt.setLong(3, insurance_charge);
            smt.setDouble(4, per_mile_charge);
            smt.setInt(5, dropoff_charge);
            smt.setDouble(6, additionalFees);
            smt.setDouble(7, discount);
            smt.setInt(8, r_id);
            smt.executeUpdate();

            query = "insert into vehicle_location values(?, ?)";
            smt = conn.prepareStatement(query);
            smt.setInt(1, Integer.parseInt(input));
            smt.setInt(2, storeNum);
            smt.executeUpdate();

            System.out.printf("Car successfully returned! The bill is $%-8.2f\n", total);

        } catch (Exception e) {

        }

        employeeScreen(sc, conn, storeNum);

    }

}