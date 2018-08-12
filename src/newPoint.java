
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Hitesh Mohite
 */
public class newPoint {
    /**
     * @param args the command line arguments
     */
    //port for the server
    public static int connecting_port = 2000;
    //these two variable sets the no of clients playing the game
    public static int no_of_clients = 0, max_clients = 2;
    //creating a serversocket variable
    public static ServerSocket server;
    //this variable socket accepts clients
    public static Socket accept_client;
    //stores the clients threads into this array list
    public static ArrayList clients;
    //varible to hold the terminal condition
    public static String result;
    //creates a object of type newpoint
    public static newPoint np;
    
    public static void main(String[] args) throws IOException, InterruptedException {
        
        //object for server socket
        server = new ServerSocket(connecting_port);
        //instatiating the arraylist object
        clients = new ArrayList();
        //instatiating the newpoint object
        np = new newPoint();
        //this while loop will iterate till it accepts all the clients
        //required to play the game
        while(no_of_clients < max_clients)
        {
            //accepting individual client
            accept_client = server.accept();
            //creating the thread for ech client
            accept_Clients newClient = new accept_Clients(accept_client);
            //adding each client object to the arraylist
            clients.add(newClient);
            //calls the starts method for each client
            newClient.starts(max_clients, np, no_of_clients+1);
            //incrementing the no of clients variable
            no_of_clients++;
        }
        //closes the server
        server.close();
    }
    
    //this method terminates the game of other clients
    public static void send_final_status(int player_won) throws IOException
    {
        //initialising the string
        String final_message = "You lost...player "+(player_won+1)+" won the game";
        //this for loop iterates through out the arraylist
        //and sends the message for all the clients other than the 
        //winner
        for(int i = 0; i < clients.size(); i++)
        {
            if(i != player_won)
            {
                accept_Clients new_obj = (accept_Clients) clients.get(i);
                new_obj.send_data(final_message);
            }
        }
    }
}
//This class represents the each client
class accept_Clients extends Thread
{
    //creating an object for receiving the data
    DataInputStream getData;
    //creating an object for sending the data
    DataOutputStream sendData;
    //creates a socket variable
    Socket new_client;
    //creates the variable for newPoint
    newPoint obj;
    //stores the access of current player
    int current_player;
    //stores the no of players involved in this game
    int total_players;
    //variable to recieve data that is sent from client side 
    String data_received;
    
    //constructor to initaliase input,output
    //and socket objects
    accept_Clients(Socket S) throws IOException
    {
        new_client = S;
        sendData = new DataOutputStream(new_client.getOutputStream());
        getData = new DataInputStream(new_client.getInputStream());
    }
    
    //method to initialise players, newPoint and player variables
    //and also to invoke start method
    public void starts(int players, newPoint np, int player)
    {
        total_players = players;
        current_player = player;
        obj = np;
        start();
    }
    
    //run method to run the thread
    public void run()
    {
        try 
        {
            System.out.println("Server is connected to Client");
            //sends the data to the client
            send_data("You are now connected");
            //sends the data to the client
            send_data((total_players - 1)+" other player(s) joined\n");
            //sends the data
            send_data("exit");
            
            //this while loop iterates till any one of the player wins
            while(true)
            {
                System.out.println("in the run method");
                //stores the string that has been returned by the server
                String str = play_the_game();
                //condition for breaking out of the loop
                if(str.equals("completed"))
                {
                    break;
                }
                //sleeps the to make sure other thread gets hold on 
                //play the game method
                sleep(1000);
            }
            //sends the final message
            obj.send_final_status(current_player-1);
        } 
        catch (IOException ex) {
            ex.printStackTrace();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
    
    //this method has the access to all of the data that is sent from the clients
    String play_the_game() throws IOException
    {
        System.out.println("this is play the game ");
        //synchronized block on the newPoint object
        synchronized(obj)
        {
            //sends this data
            send_data("Your move:");
            //receives the data from the server
            data_received = read_client_data();
            System.out.println("data received is "+data_received+" from player "+(current_player));
            //if the game is completed by the client
            if(data_received.equals("completed"))
            {
                return data_received;
            }
            System.out.println("terminal condition not processed");
            System.out.println(current_player);
            //does the if condition if it is player 1
            if(current_player == 1)
            {
                System.out.println(current_player);
                //stores the reference of another client
                accept_Clients temp = (accept_Clients)obj.clients.get(1);
                System.out.println("********************************");
                //sends data to other client
                temp.send_data("Player 1 :");
                temp.send_data(data_received);
            }
            //does the if condition if it is player 1
            else if(current_player == 2)
            {
                System.out.println(current_player);
                //stores the reference of another client
                accept_Clients temp = (accept_Clients)obj.clients.get(0);
                //sends data to other client
                temp.send_data("Player 2 :");
                temp.send_data(data_received);
            }
        }
        //returns the data recieved from other client
        return data_received;
    }
    
    //reads data from the client
    String read_client_data() throws IOException
    {
        return getData.readUTF();
    }
    
    //sends data to the client
    void send_data(String data_to_send) throws IOException
    {
        System.out.println("The following data is to be sent a client");
        System.out.println(data_to_send);
        sendData.writeUTF(data_to_send);
        System.out.println("Data sent");
    }
}