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
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javax.print.attribute.standard.Severity;
import static tec.tac.toe.zoo.server.v0.pkg0.pkg0.DataBaseClass.rs;

/**
 *
 * @author Omnia
 */

public class Server extends Application {
    ServerSocket myServer = null;
    int p =5555;
    
   
    String ip = "";
    Thread startThread = null ;
    boolean started = false;
    static Vector<LogedPlayer> currentLogedPlayers = new Vector<LogedPlayer>();
     static List<Player> client = new Vector<Player>();
    static  DataBaseClass dataBase = null;
     Button stop ;
     Button start ;
     volatile static ObservableList<String> data ; 
    public void start(Stage primaryStage) {
    
         Stage stage = primaryStage;
         stage.setTitle("TicTacToeZoe Server");
       // Pane root = new Pane();
       
         Label server = new Label("Server Tic Tac Toe");
            server.setId("server");
        
        start = new Button();
        start.setText("start Server");
        start.setId("start");
        data = FXCollections.observableArrayList();
        start.setOnAction(new EventHandler<ActionEvent>() {            
        @Override
        public void handle(ActionEvent event) {
            try {
                myServer = new ServerSocket(p);
                // start background serves which will call accept 
                startServer();
                dataBase = new DataBaseClass("tic-tac-tooe","root","");
                String allPlayers = getAllPlayers("");
            
                 System.out.println("Server Started ");
                 String[] players = allPlayers.split("\\,");
                 System.out.println("allll" + allPlayers);
                for (int i = 0; i < players.length; i++) {
                    if (!players[i].equals("")) {
                        String[] player = players[i].split("\\-");
                        Player p = new Player(player[0], player[1], Boolean.parseBoolean(player[2]), null, null, 1);
                        p.active = player[2].equals("0") ? false : true;
                        client.add(p);
                        data.add(p.userName + "   " + (p.active ? "online": "ofline")+"   " + p.score);
                    }
                }
                start.setVisible(false);
                stop.setVisible(true);
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
                }
            });
        
        ListView<String> listView = new ListView<String>(data);
        listView.setPrefSize(200, 250);
  


         listView.getSelectionModel().selectedItemProperty().addListener(
            (ObservableValue<? extends String> ov, String old_val, 
                String new_val) -> {
                    System.out.println(new_val);

        });
        
        
        stop = new Button();
        stop.setId("stop");
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
        
          
        
        server.setLayoutX(190);
        server.setLayoutY(120);
        
        listView.setLayoutX(250);
        listView.setLayoutY(170);
        
        stop.setLayoutX(250);
        stop.setLayoutY(458);
        
         start.setLayoutX(250);
        start.setLayoutY(458);
        
        
        Pane root = new Pane();
        root.getChildren().add(start);
        root.getChildren().add(stop);
        root.getChildren().add(server);

         root.getChildren().add(listView);
        stop.setVisible(false);
            stage.setResizable(false);
            
        Scene scene = new Scene(root, 700, 570);
        scene.getStylesheets().add(Server.class.getResource("serverdesig.css").toExternalForm());
        primaryStage.setTitle("Hello World!");
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
             @Override
             public void handle(WindowEvent event) {
            stopSErver();
             }
         });
    }
    
    public void startServer(){
        
        
        if(!started){
            started = true;
        startThread =new Thread(){  
            public void run(){
               System.err.println("started");
               System.err.println("run");    
                while(started){
                    System.err.println("lise");    
                try {
                       
                       Socket s =  myServer.accept();
                       
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
        
    }
    
    public void stopSErver(){
         //send message for each player that server closed 
         // and then clothe its streams, sockets and threads
         //slose server thread that accept sockets
         if(started){
             data.clear();
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
         currentLogedPlayers.clear();
        try {
            myServer.close();
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
         
         startThread.stop();
         started = false;
         }
         
    }
    
    synchronized public static String  getAllPlayers(String userName){
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
        
    synchronized public static boolean checkPlayerOnline(String name){
                for(LogedPlayer i : currentLogedPlayers){
                    if(i.userName.equals(name)){
                        return true;
                    }
                }
            return false;
        }    
    
    synchronized public static void updatePlayerStatus(String userName){
            //send message for all player 
            //that user userName is loged out 
            data.clear();
            for(LogedPlayer i : Server.currentLogedPlayers){
                    if(i.userName != userName){
                        System.out.println("send update to " + i.userName);
                        i.output.println("update:"+userName);
                    }
                }
            
            for(Player i : client){
                if(i.userName.equals(userName)){
                    System.out.println(i.userName+  " brfore : " + i.active);
                    i.active = !i.active ;
                    System.out.println(i.userName + "after : " + i.active);
                }
            }
            
            for(Player p : client){
                 data.add(p.userName + "   " + (p.active ? "online": "ofline") + "   " + p.score);
            }
        }
        
         synchronized public static void updateScore(String userName , int score){
         
             for(Player i : client){
                 if(i.userName.equals(userName)){
                     i.score = score;
                     break;
                 }
             
             }
         
         }


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
                    if(!loginMsg.equals("") && loginMsg != null){
                        msgHendler(loginMsg);
                    }
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
        
        synchronized private String msgHendler(String message) {
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
                    String players = Server.getAllPlayers(userName);
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
                    if(state.equals("yes")){
                        playWith = sender;
                    }
                    break;
                }
                
                case "cell":{
                    String index = message.split("\\:")[1];
                    for(LogedPlayer i : Server.currentLogedPlayers){
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
                     Server.updateScore(player,Integer.valueOf(score));
                     DataBaseClass.setscore(player,Integer.valueOf(score));
                     break;
                }
              
                
                default:
                    System.out.println(message);          
            }
            return "";
        }

        synchronized private void insertPlayer(String userName, String userPass) {
           if(Server.dataBase != null){
               Boolean inserted = DataBaseClass.insertPlayer(userName,userPass);
               System.out.println("signup:"+inserted);
               if(inserted){
                   Server.currentLogedPlayers.add(this);
               }
               output.println("signup:"+inserted);   
           }
        }

        synchronized private void getPlayerData(String userName , String userPass) {
            if(userName != null && userPass != null){
                if(Server.dataBase != null){
                       ResultSet rs = DataBaseClass.selectPlayers(userName,userPass);
                       String userdata = "";
                       if(rs != null){
                           try {
                               if(rs.next()){
                                   if(!Server.checkPlayerOnline(rs.getString(1))){
                                   Server.currentLogedPlayers.add(this);
                                   for(int i = 1 ; i < 4;i++){
                                       userdata += rs.getString(i)+",";
                                   }
                                   userdata +="1";//current user is active
                                    Platform.runLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            Server.updatePlayerStatus(userName);
                                        }
                                    });
                                   }else{
                                       userdata ="false";
                                   }
                               }else{
                                    userdata ="false";
                                   System.out.println("no next");
                                   this.stopThread();
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
        

        synchronized private void logOut(){
            //logout this current player 
            //close thread
            if(Server.currentLogedPlayers.remove(this)){
                System.out.println("removed : "+userName);
                running = false;
            };
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    Server.updatePlayerStatus(userName);
                }
            });
           
            stopThread();
        }
        
       
        
        private void sendRequest(String playWith, String mark) {
            for(LogedPlayer i : Server.currentLogedPlayers){
                  if(i.userName.equals(playWith)){
                         System.out.println(userName + " send request to " + playWith);
                          i.output.println("requestFrom:"+this.userName+","+mark);
                  }
            
            }
        }

        private void sendRequestReplay(String sender, String state) {
            for(LogedPlayer i : Server.currentLogedPlayers){
                if(i.userName.equals(sender)){
                    i.output.println("requestReplay:"+sender+","+state);
                    System.out.println("requestReplay to "+sender);
                }
            }
        }        
    }



