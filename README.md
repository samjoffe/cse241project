# Hurts Rent-a-Lemon
## by Sam Joffe (slj222)

### Compilation Instructions
To compile the code from within the slj222 directory enter the following
```bash
javac Hurts.java
```

### Data Generation
The data was all generated using Mockaroo. I used some of the built-in fields as well as creating regular expressions for attributes such as suite number and driver's license and SQL expressions for random foreign keys.

### User Instructions
Please have the terminal in full-screen mode to view the formatting correctly.

Once connected to the database, you can "log in" as either a customer or employee. You also have the choice to create a new customer account.

#### Customer

##### Account Creation
When creating an account, you will be prompted to enter some information about yourself, including your email which you will need to log in. Note that the email does not explicitly have to be an email (any string can be used), however, emails are unique between accounts since you use them to log in.

##### Customer Login
To "log in", you must enter the email of the customer. If there is no customer account associated with the email you type in, then you won't be able to log in, and the interface will prompt you to try again. If you need to go back to the main screen, you can type 'back'.

In the "view and request available vehicles" screen, the customer can request the car they are looking for. First, all the store locations will display, and the user can type in the store number for the store whose cars they want to view. Then, all the cars which are currently at that location will be displayed, and the customer can choose the ID of the car they would like to request. This does not actually start a rental, as only an employee can do that, but they can view the requests that have been made at the store they are working at.

The customer can view any bills they may have had from completed rentals. Going to that screen will show a condensed version of all the bills they have, ordered by R_ID. The customer can then select the bill they want to see the full information for.

The customer can add a discount code from a company they work for, which will give them up to 25% off their final total for rentals. When you go to the add discount code screen, you can just type in the discount code you got, and it will be applied. If the customer already has a discount code, it will keep whichever one is a better discount. The discount code applies to all rentals the customer makes in the future.

#### Employee
The employee interface allows you to  check-out or check-in any vehicles. To "log in", just type in the store number. The employee can check out a vehicle to a customer who made a request at that store. If the customer's requested car is no longer at the store, the request is deleted and the customer is asked to pick a new car. Otherwise, the employee can select the request that should be checked out. The employee can "ask" the customer if they would like to purchase insurance, and also types in the date and time that the rental is beginning at. Then, the rental becomes active, and the car can no longer be requested or checked out of that location until it is returned.

To check in a car (which can happen at any location), the employee must enter the RV_ID (rental vehicle ID) to begin the return process. If it is a different location than the pickup location, a dropoff fee is charged to the customer, but the vehicle stays in the location it was dropped off at. The employee must enter the time of return (which must be after the checkout time) and record the odometer reading, which allows the program to determine how many miles were driven. They must also record how many gallons will be needed to refill the car, and assess the dollar amount of any damages to the car. After all of that, the car is officially returned, a grand total for the bill is displayed, and the customer can view the bill in their account!

#### Known Issues
If you try to return a car after a long period of time (ie the charge would be in the millions range which is too big for the database), the car will be returned, however, the bill will show up as $0. I felt that this wasn't worth fixing because it is an unrealistic scenario.

<i>Things to Try</i>

Customer Accounts: mdurj@nhs.uk, slj222@lehigh.edu

Discount Codes: UPKSXGN437 (6.16% discount), 0NE90M681W (23.09% discount)
