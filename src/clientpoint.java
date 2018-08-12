/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Random;
import java.awt.*;
/**
 *
 * @author Hitesh Mohite
 */
public class clientpoint {

    /**
     * @param args the command line arguments
     */
    public static int connecting_port = 2000;
    public static InetAddress server_name;
    public static Socket client;
    public static DataInputStream getData;
    public static DataOutputStream sendData;
    public static String readData, game_status = "";
    public static boolean breakable = true;
    public static int strikes = 0, strikes_req = 5;
    public static int[][] player = new int[5][5];
    public static ArrayList grid = new ArrayList();
    static gameUI ui;
    public static void main(String[] args) throws UnknownHostException, IOException {
        // TODO code application logic here
        server_name = InetAddress.getLocalHost();
        client = new Socket(server_name, connecting_port);
        System.out.println("Welcome to game BINGO");
        System.out.println("Waiting for other player(s) to join the game");
        System.out.println("Wait for your turn to play when connected");
        getData = new DataInputStream(client.getInputStream());
        sendData = new DataOutputStream(client.getOutputStream());
        
        while(breakable)
        {
            breakable = read_server_data();
        }
        ui = new gameUI();
        ui.setVisible(true);
        game_on();
        write_client_data("Thanks for connecting");
    }
    
    public static boolean read_server_data() throws IOException
    {
        readData = getData.readUTF();
        if(readData.equals("exit"))
        {
            return false;
        }
        else
        {
            System.out.println(readData);
            return true;
        }
    }
    public static int get_server_data() throws IOException
    {
        int number;
        readData = getData.readUTF();
        number = Integer.parseInt(readData);
        return number;
    }
    public static void write_client_data(String send_message) throws IOException
    {
        sendData.writeUTF(send_message);
    }
    
    public static void game_on() throws IOException
    {
        int counter = 0, temp = 0, temp1 = 0, value = 0;
        String user_input = "";
        boolean flag = true;
        Random rand  = new Random();
        BufferedReader from_user;
        
        //Making elements for 5 by 5 grid for the user
        System.out.println("Game has begun");
        while(flag)
        {
            temp1 = rand.nextInt(25) + 1;
            counter++;
            if((grid.size() < 25) && (!grid.contains(temp1)))
            {
                grid.add(temp1);
            }
            if((grid.size() == 25))
            {
                flag = false;
            }
        }
        
        //Making a grid of 5 by 5 for the user
        for(int i = 0; i < 5; i++)
        {
            for(int j = 0; j < 5; j++)
            {
                player[i][j] = (Integer)grid.get(temp);
                temp++;
            }
        }
        
   
       //making of moves made by user
       //System.out.println("Enter numbers accordingly");
       from_user = new BufferedReader(new InputStreamReader(System.in));
       //System.out.println("\nEnter any number from grid(except -1)");
       display_grid();
       
       while(true)
       {
           String read = check_for_turn();
           System.out.println("Displaying the Grid:\n\n");
           display_grid();
           if(read.contains("Your move:"))
           {
               ui.label.setText("YOUR MOVE:  ");
               System.out.println("\n"+read);
               user_input = getInput();
               while((user_input == null) || user_input.equals(""))
               {
                   
                   //System.out.println("Re-enter:");
                   user_input = getInput();
               }
               value = Integer.parseInt(user_input);
               ui.nextInput = null;
               ui.input.setText("");
               ui.label.setText("WAIT FOR YOUR MOVE:  ");
               if(check(value))
               {
                   strike_the_value(value);
                   update_strikes();
                   if(check_strikes())
                   {
                        System.out.println("\n\nBingo!!!");
                        System.out.println("You have won the game");
                        game_status = "completed";
                        write_client_data(game_status);
                        break;
                   }
                   //System.out.println("Displaying the Grid:\n\n");
                   display_grid();
                   write_client_data(user_input);
               }
               else
               {
                   //System.out.println("Invalid input. Re-enter the input:");
                   continue;
               }
           }
           else if(read.contains("Player"))
           {
               //System.out.println("number sent by "+read);
               int number = get_server_data();
               //System.out.println(number);
               strike_the_value(number);
               update_strikes();
               if(check_strikes())
               {
                   //System.out.println("\n\nBingo");
                   //System.out.println("You have won the game");
                   //System.out.println("Thank you for participating");
                   game_status = "completed";
                   ui.result.setText("YOU HAVE WON THE GAME");
                   write_client_data(game_status);
                   break;
               }
               System.out.println("Displaying the Grid:\n\n");
               display_grid();
           }
           else if(read.contains("You lost"))
           {
               //System.out.println(read);
               ui.result.setText("YOU LOST");
               break;
           }
       }
    }
    
    //displays the grid
    public static void display_grid()
    {
        String outputString = "";
        for(int i = 0; i < 5; i++)
        {
            for(int j = 0; j < 5; j++)
            {
                outputString = outputString +player[i][j]+"     ";
                if(player[i][j]/10 == 0)
                    outputString = outputString +" ";
            }
            outputString = outputString + "\n\n";
        }
        ui.board.setText(outputString);
    }
    
    public static String getInput()
    {
        
        return ui.nextInput;
    }
    
    //this method checks if the entered number is valid or not
    public static boolean check(int value)
    {
        boolean result = false;
        for(int i = 0; i < 5; i++)
        {
            for(int j = 0; j < 5; j++)
            {
                if(player[i][j] == value)
                {
                    result = true;
                    return result;
                }
            }
        }
        return result;
    }
    
    //this method changes the grid after every move made by user
    public static void strike_the_value(int value)
    {
        for(int i = 0; i < 5; i++)
        {
            for(int j = 0; j < 5; j++)
            {
                if(player[i][j] == value)
                {
                    player[i][j] = -1;
                }
            }
        }
    }
    
    //this method returns the status of the game
    public static void update_strikes()
    {
        int number = -1;
        strikes = 0;
        
        //checks for the strikes through out the diagnols
        if((player[0][0] == number) && (player[1][1] == number) && (player[2][2] == number) && (player[3][3] == number) && (player[4][4] == number))
        {
            strikes++;
        }
        
        if((player[4][0] == number) && (player[3][1] == number) && (player[2][2] == number) && (player[1][3] == number) && (player[0][4] == number))
        {
            strikes++;
        }
        
        //this loop checks for the strikes among rows and colomns
        for(int i = 0; i < 5; i++)
        {
            //checks for the strikes through out the colomns
            if((player[i][0] == number) && (player[i][1] == number) && (player[i][2] == number) && (player[i][3] == number) && (player[i][4] == number))
            {
                strikes++;
            }
            
            //checks for the strikes through out the colomns
            if((player[0][i] == number) && (player[1][i] == number) && (player[2][i] == number) && (player[3][i] == number) && (player[4][i] == number))
            {
                strikes++;
            }
        }
    }
    
    
    public static boolean check_strikes()
    {
        //System.out.println("checking strikes");
        if(strikes >= strikes_req)
        {
            return true;
        }
        //System.out.println("Strike value:"+strikes);
        ui.strike.setText(Integer.toString(strikes));
        return false;
    }

    public static String check_for_turn() throws IOException 
    {
        readData = getData.readUTF();
        if(readData.equals("Your move:"))
        {
            return readData;
        }
        else if(readData.contains("Player"))
        {
            System.out.println(readData);
            return readData;
        }
        else
        {
            return readData;
        }
    }
}
