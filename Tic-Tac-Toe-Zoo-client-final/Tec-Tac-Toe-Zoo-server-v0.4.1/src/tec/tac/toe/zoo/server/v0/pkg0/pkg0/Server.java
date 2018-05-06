/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tec.tac.toe.zoo.server.v0.pkg0.pkg0;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javax.print.attribute.standard.Severity;
import static tec.tac.toe.zoo.server.v0.pkg0.pkg0.DataBaseClass.rs;

/**
 *
 * @author Omnia
 */
public class Server extends Application {
    ServerSocket ss = null;
    int p =5555;
    Socket s = null;
    String ip = "";
    DataInputStream in = null;
    PrintWriter out = null;
    Thread startThread = null ;
    
    static Vector<LogedPlayer> currentLogedPlayers = new Vector<LogedPlayer>();
    static  DataBaseClass dataBase = null;
    Button stop ;
     Button start ;
    public void start(Stage primaryStage) {
    
        start = new Button();
        start.setText("start Server");
        start.setOnAction(new EventHandler<ActionEvent>() {            
        @Override
        public void handle(ActionEvent event) {
            try {
                ss = new ServerSocket(p);
                // start background serves which will call accept 
                startServer();
                dataBase = new DataBaseClass("tic-tac-tooe","root","");
                System.out.println("Server Started ");
                start.setVisible(false);
                stop.setVisible(true);
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
                }
            });
                
        stop = new Button();
        stop.setText("stop Server");
        stop.setOnAction(new EventHandler<ActionEvent>(){
            
            public void handle(ActionEvent event) {
                stopSErver();
                System.out.println("Server Stopped ");
                start.setVisible(true);
                stop.setVisible(false);
            }
        }
                
                
                
        );
        
        StackPane root = new StackPane();
        root.getChildren().add(start);
        root.getChildren().add(stop);
        stop.setVisible(false);
        Scene scene = new Scene(root, 300, 250);
        primaryStage.setTitle("Hello World!");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    public void startServer(){
        startThread =new Thread(){  
            public void run(){
               System.err.println("started");
               System.err.println("run");    
                while(true){
                    System.err.println("lise");    
                try {
                        s =  ss.accept();
                        System.err.println("some one");
                        new LogedPlayer(s);
                } catch (IOException ex) {
                        Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                }

                }
            }
               
        };
      
        startThread.start();
    }
    
    public void stopSErver(){
         //send message for each player that server closed 
         // and then clothe its streams, sockets and threads
         //slose server thread that accept sockets
         for(LogedPlayer i : currentLogedPlayers){
             try {
                 i.output.println("logout:server");
                 i.input.close();
                 i.output.close();
                 i.clientSocket.close();
                 //why it reads stopThread although it is private
                 i.stopThread();
             }
             
             catch (IOException ex) {
                 Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
             } catch (Throwable ex) {
                 Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
         startThread.stop();
    }
    
class LogedPlayer extends Thread{

    Socket clientSocket;
    DataInputStream input = null;
    PrintStream output = null;
    String userName = null;
    String userPass = null;
    String player = null;
    String playWith = "";
    boolean running =true;
    
        LogedPlayer(Socket clientSocket){
        try {
            this.clientSocket = clientSocket;
            input = new DataInputStream(clientSocket.getInputStream());
            output = new PrintStream(clientSocket.getOutputStream());
            Server.currentLogedPlayers.add(this);
            this.start();
        } catch (IOException ex) {
            Logger.getLogger(LogedPlayer.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
        
        public void run(){
            while(running){
                try {
                    System.out.println("liss ....");
                    String loginMsg = input.readLine();
                    if(!loginMsg.equals(""))
                    msgHendler(loginMsg);
                } catch (IOException ex) {
                   // Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                    stopThread();
                    running = false;
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
        public void stopThread(){ 
            try {
                this.input.close();
                this.output.close();
                this.clientSocket.close();
                this.finalize();
            } catch (Throwable ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        private String msgHendler(String message) {
            String replay = message.split("\\:")[0];
            switch(replay){
                case "login" : {
                    //login:user=name,pass=pass
                    String msg = message.split("\\:")[1];
                    String temp = msg.split("\\,")[0];
                    userName = temp.split("\\=")[1];
                    temp = msg.split("\\,")[1];
                    userPass = temp.split("\\=")[1];
                    getPlayerData(userName,userPass);
                    break;
                }
                case "online" :{
                    String players = getAllPlayers();
                    System.out.println("online:"+players);
                    output.println("online:"+players);
                    break;
                }
                case "signup":{
                    //request signip:user=name,pass=pass
                    String msg = message.split("\\:")[1];
                    String temp = msg.split("\\,")[0];
                    userName = temp.split("\\=")[1];
                    temp = msg.split("\\,")[1];
                    userPass = temp.split("\\=")[1];
                    insertPlayer(userName,userPass);
                    break;
                }
                case "logout":{
                      //logout:name,pass
                    String msg = message.split("\\:")[1];
                    String name = msg.split("\\,")[0];
                    String pass = msg.split("\\,")[1];
                    this.logOut(); 
                    break;
                }
                case "requestTo":{
                    //requestTo:playwith,mark
                    String msg = message.split("\\:")[1];
                    String playWith = msg.split("\\,")[0];
                    String mark = msg.split("\\,")[1];
                    sendRequest(playWith,mark);
                    this.playWith = playWith;
                    break;
                }
                case "requestReplay":{
                    //requestReplay:sender,state
                    String msg = message.split("\\:")[1];
                    String sender = msg.split("\\,")[0];
                    String state = msg.split("\\,")[1];                
                    sendRequestReplay(sender,state);
                    playWith = sender;
                    break;
                }
                
                case "cell":{
                    String index = message.split("\\:")[1];
                    for(LogedPlayer i : currentLogedPlayers){
                        if(i.userName.equals(this.playWith)){
                            i.output.println("cell:"+index);
                            break;
                        };
                    }
                    break;
                }
                
                case "score":{
                    
                    System.err.println(message);
                     String msg = message.split("\\:")[1];   
                     System.err.println(msg);
                     String player = msg.split("\\,")[0]; 
                     String score = msg.split("\\,")[1]; 
                     DataBaseClass.setscore(player,Integer.valueOf(score));
                     break;
                }
              
                
                default:
                    System.out.println(message);          
            }
            return "";
        }

        synchronized private void insertPlayer(String userName, String userPass) {
           if(dataBase != null){
               Boolean inserted = DataBaseClass.insertPlayer(userName,userPass);
               System.out.println("signup:"+inserted);
               output.println("signup:"+inserted);   
           }
        }

        synchronized private void getPlayerData(String userName , String userPass) {
            /*String loginMsg = input.readLine();
            megHendler(loginMsg);
            */
            if(userName != null && userPass != null){
                if(dataBase != null){
                       ResultSet rs = DataBaseClass.selectPlayers(userName,userPass);
                       String userdata = "";
                       if(rs != null){
                           try {
                               if(rs.next()){
                                   for(int i = 1 ; i < 4;i++){
                                       userdata += rs.getString(i)+",";
                                   }
                                   userdata +="1";//current user is active
                                   updatePlayerStatus();
                               }else{
                                   userdata ="false";
                                   System.out.println("no next");
                               }
                           } catch (SQLException ex) {
                               Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                           }
                           output.println("login:"+userdata);          
                        } 
                }else{  
                    System.out.println("data base null");
                        //faild to get user data from database
                }
                
            }else{
                System.out.println("fail to get data from client message");
                //faild to get User name and PAss
            }
      
        }
        synchronized private String  getAllPlayers(){
            String players = "";
               if(dataBase != null){
                       ResultSet rs = DataBaseClass.selectPlayers(null,null);
                       String userdata = "";
                       if(rs != null){
                           try {
                               while (rs.next()) { 
                                   if(rs.getString(1).equals(userName)) continue;
                                   userdata = "";
                                   for(int i = 1 ; i < 4;i++){
                                       if(i != 2)
                                       userdata += rs.getString(i)+"-";
                                   }
                                   //check if player is online
                                   userdata += 
                                           checkPlayerOnline(rs.getString(1)) ? "1":"0";
                                   players +=userdata+",";
                               }
                           } catch (SQLException ex) {
                               Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                           }
                        } 
                }else{  
                    System.out.println("data base null");
                        //faild to get user data from database
                }
            return players;
        }
        
        synchronized private boolean checkPlayerOnline(String name){
                for(LogedPlayer i : currentLogedPlayers){
                    if(i.userName.equals(name)){
                        return true;
                    }
                }
            return false;
        }

        synchronized private void logOut(){
            //logout this current player 
            //close thread
            if(Server.currentLogedPlayers.remove(this)){
                System.out.println("removed : "+userName);
                running = false;
            };
            updatePlayerStatus();
            stopThread();
        }
        
        synchronized private void updatePlayerStatus(){
            //send message for all player 
            //that user userName is loged out 
            for(LogedPlayer i : Server.currentLogedPlayers){
                    if(i.userName != this.userName){
                        System.out.println("send update to " + i.userName);
                        i.output.println("update:"+userName);
                    }
                }
        }
        
        private void sendRequest(String playWith, String mark) {
            for(LogedPlayer i : currentLogedPlayers){
                  if(i.userName.equals(playWith)){
                         System.out.println(userName + " send request to " + playWith);
                          i.output.println("requestFrom:"+userName+","+mark);
                  }
            
            }
        }

        private void sendRequestReplay(String sender, String state) {
            for(LogedPlayer i : currentLogedPlayers){
                if(i.userName.equals(sender)){
                    i.output.println("requestReplay:"+sender+","+state);
                    System.out.println("requestReplay to "+sender);
                }
            }
            
        }
        
    }



}

