import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.Random;


//CSCE 3613 Operating Systems: HW9 Sleeping Barber Problem
//November 8th, 2021
//Diego Castro
//010873014

public class sleepingBarber
{
    static int sleepTimeNumber;
    static int numSeats;

    public static void main (String[] args)
    {   
        try
        {
            sleepTimeNumber = Integer.valueOf(args[0]);
            numSeats = Integer.valueOf(args[1]);
        }
        catch(Exception e)
        {
            System.out.println("\n");
            System.out.println("** Error, next time please enter \"java sleepingBarber x y\" **");
            System.out.println("Where x is the number of seconds the barber sleeps, and y is the number of chairs");
            System.out.println("DEFAULTING:");
            //default
            sleepTimeNumber = 5;
            numSeats = 3;
        }
        
        System.out.print("\n");
        System.out.print("The barber will sleep for " + sleepTimeNumber + " seconds, and will have " + numSeats + " chairs in the waiting room.\n\n");
        System.out.println("DIEGO'S BARBER SHOP IS OPEN FOR BUSINESS!");
        System.out.println("*****************************************");

        barberShop shop = new barberShop(numSeats);
        Barber barber = new Barber(shop);
        CustomerGenerator custGen = new CustomerGenerator(shop);
        Thread oneBarber = new Thread(barber);
        Thread multipleCustGen = new Thread(custGen);
        oneBarber.start();
        multipleCustGen.start();

    }

}

class barberShop
{
    public int seats;
    public int custWaiting;
    public long customerName;

    Semaphore customersReady;
    Semaphore barberReady;
    Semaphore mutex;
    
    Random rand = new Random();
    LinkedList<Customer> customerList;

    public barberShop(int seats)
    {
        //Initialize the barbershop with # of seats (chairs) as params.
        this.seats = seats;
        custWaiting = 0;
        customersReady = new Semaphore(0);
        barberReady = new Semaphore(0);
        mutex = new Semaphore(1);
        customerList = new LinkedList<Customer>();
    }

    public void cutHair()
    {
        Customer customer;
        //Wait on customer
        //Update number of customers waiting, signal to wake up barber, etc
        //Simulate cutting hair with sleep
        try
        {
            //If there are no customers waiting, barber goes back to sleep
            if(custWaiting == 0)
            {
                System.out.println("The waiting room is currently empty. Sleeping...");
                try
                {
                    Thread.sleep(sleepingBarber.sleepTimeNumber * 1000);
                }
                catch(Exception e)
                {
                    System.out.println("Error... (cutHair)");
                }
                
            }

            customersReady.acquire();
            mutex.acquire();

            //Implement customer queue as LL
            customer = (Customer) ((LinkedList<?>)customerList).poll();
            System.out.println("Barber started...");
            System.out.println("Barber found a customer in the queue.");
            System.out.println("Cutting hair of Customer : Customer Thread " + customer.getCustomerName());

            //Simulate cutting hair
            Thread.sleep(sleepingBarber.sleepTimeNumber * 1000);
            System.out.println("Finished cutting hair of Customer Thread " + customer.getCustomerName());

            barberReady.release();

            custWaiting--;

            mutex.release();
        }
        catch(Exception e)
        {
            System.out.println("Error... (cutHair)");
        }

    }

    public void add(Customer customer)
    {
        //DO THINGS HERE like determine if there are enough chairs in the waiting room. Leave if waiting room is full. If waiting room is not full, things must happen

        try
        {   
            
            //Customer walks in to an empty chair, sits down and gets hair cut.
            if(seats > custWaiting)
            {
                mutex.acquire();
                custWaiting++;
                
                customerName = customer.getCustomerName();
                //add new customer to end of queue if there's a seat
                customerList.addLast(customer);
                System.out.println("Customer : Customer Thread " + customerName + " got a chair");
                //DEBUG System.out.println("People in waiting room: " + custWaiting);
                
                //release customer and mutex semaphore
                customersReady.release();
                mutex.release();
                
                //aquire barber so he can start cutting
                barberReady.acquire();
            }
            else
            {
                //All seats are taken, customer leaves.
                System.out.println("*** There are no chairs available for Customer Thread " + customer.getCustomerName());
                System.out.println("*** Customer Customer Thread " + customer.getCustomerName() + " Exits...");
            }

        }
        catch(Exception e)
        {
            System.out.println("Error... (add)");
        }
    }
}

class Barber implements Runnable
{
    barberShop shop;
    
    public Barber(barberShop shop)
    {
        this.shop = shop;
    }

    public void run()
    {
        //Simulate sleep by putting thread to sleep
        while(true)
        {
            try
            {   
                Thread.sleep(sleepingBarber.sleepTimeNumber * 1000);
                System.out.println("Barber is waiting...");
                System.out.println("Barber waiting for lock");
                shop.cutHair();
            }
            catch(Exception e)
            {
                System.out.println("Error... (barberRun)");
            }
            
        }
    }

}

class Customer implements Runnable
{
    barberShop shop;
    long customerName;

    public Customer(barberShop shop)
    {
        this.shop = shop;
    }

    //Getter for customer name
    public long getCustomerName()
    {
        return this.customerName;
    }

    //Setter for customer name
    public void setCustomerName(long customerName)
    {
        this.customerName = customerName;
    }

    public void run()
    {
        try
        {
            goForHairCut();
        }
        catch(Exception e)
        {
            System.out.println("Error... (customerRun)");
        }
        
    }

    private void goForHairCut()
    {
        shop.add(this);
    }
}

class CustomerGenerator implements Runnable
{
    barberShop shop;

    Date dateStamp;

    Random randomSecs = new Random(); 
    Customer customer;
    Thread thread;

    long customerName;

    public CustomerGenerator(barberShop shop)
    {
        this.shop = shop;
    }

    public void run()
    {
        while(true)
        {
            //Create customers and pass object "shop"
            //Create thread
            //Start threads
            //Sleep random amount of time

            

            customer = new Customer(shop);

            thread = new Thread(customer);

            customerName = thread.getId();

            dateStamp = new Date();
            
            customer.setCustomerName(customerName);

            System.out.println("Customer : Customer Thread " + customerName + " enters the shop " + dateStamp);

            thread.start();
            try
            {
                //Sleep randomly with at least 1 second in between
                int mSeconds = randomSecs.nextInt(sleepingBarber.sleepTimeNumber * 1000) + 1000;
                //System.out.println("Sleeping for " + mSeconds + " seconds");
                Thread.sleep(mSeconds);

            }
            catch(Exception e)
            {
                System.out.println("Error... (customerRun)");
            }
        }
    }
    
    //Getter for customer name.
    public long getCustomerName()
    {
        return customerName;
    }
}



