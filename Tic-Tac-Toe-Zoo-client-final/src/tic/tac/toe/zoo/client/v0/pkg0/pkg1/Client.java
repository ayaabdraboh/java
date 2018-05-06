/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tic.tac.toe.zoo.client.v0.pkg0.pkg1;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 *
 * @author abdelmun3m
 */
public class Client extends Application {

    //"logout:name="+userName+",pass="+userPass
    //"logout:name="+userName+",pass="+userPass
    //player Data
    String userName;
    String userPass;
    Stage stage;
    boolean vaildUser = false;
    boolean userSigned = false;
    //network Data
    public static final int port = 5555;
    public static final String ip = "10.140.200.142";
    Socket socket = null;
    DataInputStream inStream = null;
    PrintStream outStream = null;
    Boolean serverAvilability = false;
    Thread serverListener;
    boolean runnig = false;
    String mark = "x";
    Boolean[] a;
    boolean turn = true;
    Label a0, a1, a2, a3, a4, a5, a6, a7, a8;
    Pane root;
    String playWith = "";
    Label loginValid = null;
    Button btn = null;
    String active;
    Label player1;
    Label player2;
    int score = 0;
    boolean get;
    
    boolean Active;
    static List<Player> client = new Vector<Player>();
    static List<String> records = new Vector<String>();
    static List<Game> games = new Vector<Game>();

    //String record = "0-x,1-o,2-x,3-o,4-x,5-o,6-x,7-o,8-x";
    String record = "";
    Boolean recording = false;
    
    public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) throws SocketException {
        stage = primaryStage;
        createMainScene();
    }
    
    public void createMainScene() {
        stage.setTitle("TicTacToeZoe");
        Pane root = new Pane();
        Label login = new Label("Login");
        login.setId("login");
        login.setOnMouseClicked(new EventHandler<javafx.scene.input.MouseEvent>() {
            @Override
            public void handle(javafx.scene.input.MouseEvent event) {
                createLoginScene();
            }
            
        });
        root.getChildren().add(login);
        
        Label signup = new Label("Sign Up");
        signup.setId("sign");
        signup.setOnMouseClicked(new EventHandler<javafx.scene.input.MouseEvent>() {
            @Override
            public void handle(javafx.scene.input.MouseEvent event) {
                createSignUpScene();
            }
            
        });
        
        root.getChildren().add(signup);
        
        login.setLayoutX(50);
        login.setLayoutY(448);
        
        signup.setLayoutX(570);
        signup.setLayoutY(448);
        
        Scene scene = new Scene(root, 700, 570);
        stage.setResizable(false);
        stage.setScene(scene);
        scene.getStylesheets().add(Client.class.getResource("css/style.css").toExternalForm());
        stage.show();
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                logOut("", "");
            }
        });
        
    }
    
    public void startServerConnection(String mode, String name, String pass) {
        if (!serverAvilability) {
            new Thread() {
                //this thread to try to connect to server 
                //it will connect to server and close in case of connection succesed or faild
                //thread should wait a replay
                public void run() {
                    String responseMsg = null;
                    try {
                        
                        socket = new Socket(ip, port);
                        inStream = new DataInputStream(socket.getInputStream());
                        outStream = new PrintStream(socket.getOutputStream());
                        serverAvilability = true;
                        
                        if (mode.equals("login")) {
                            //send login message
                            final String loginMsg = "login:name=" + name + ",pass=" + pass;
                            outStream.println(loginMsg);
                        } else if (mode.equals("signup")) {
                            //send signup message
                            final String loginMsg = "signup:name=" + name + ",pass=" + pass;
                            outStream.println(loginMsg);
                        }

                        //reade server stream 
                        System.out.println("start read " + mode);
                        String reply = inStream.readLine();

                        //handel server replay message 
                        String msg = serverMessageHandler(reply);
                        
                        if (mode.equals("login")) {
                            if (msg.equals("false")) {
                                vaildUser = false;
                                userSigned = false;
                                serverAvilability = false;
                                userName = null;
                                userPass = null;
                                responseMsg = "invalid user";
                            } else {
                                vaildUser = true;
                                userSigned = false;
                                userName = name;
                                userPass = pass;
                                responseMsg = msg;
                            }
                        } else if (mode.equals("signup")) {
                            if (msg.equals("false")) {
                                //  faild to  sign up 
                                serverAvilability = false;
                                userSigned = false;
                                userName = null;
                                userPass = null;
                                responseMsg = "Faild To Sign up username is used Before";
                            } else if (msg.equals("true")) {
                                userSigned = true;
                                vaildUser = false;
                                Client.this.userName = name;
                                Client.this.userPass = pass;
                                //data inserted
                            }
                        }
                    } catch (IOException ex) {
                        serverAvilability = false;
                        vaildUser = false;
                        userSigned = false;
                        responseMsg = "Faild to connect to server " + ex.getMessage().toString();
                    } finally {
                        try {
                            this.finalize();
                            Client.this.runtimeUIUpdates(responseMsg);
                            System.out.println("connection thread finalized");
                        } catch (Throwable ex2) {
                            System.err.println("Faild to close connection thread " + ex2.getMessage());
                        }
                    }
                }
            }.start();
        }
        
    }
    
    public void createLoginScene() {
        
        GridPane loginScene = new GridPane();
        loginScene.setAlignment(Pos.CENTER);
        
        loginScene.setHgap(10);
        loginScene.setVgap(10);
        loginScene.setPadding(new Insets(25, 25, 25, 25));
        
        Text scenetitle = new Text("Welcome");
        scenetitle.setId("welcome-text");
        loginScene.add(scenetitle, 0, 0, 2, 1);
        
        Label userNameLable = new Label("User Name:");
        userNameLable.setId("labeluser");
        loginScene.add(userNameLable, 0, 1);
        
        TextField userNameField = new TextField();
        loginScene.add(userNameField, 1, 1);
        
        Label userPassLable = new Label("Password:");
        userPassLable.setId("labelpass");
        loginScene.add(userPassLable, 0, 2);
        
        PasswordField userPassField = new PasswordField();
        loginScene.add(userPassField, 1, 2);
        
        btn = new Button("Login");
        HBox hbBtn = new HBox(10);
        hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
        hbBtn.getChildren().add(btn);
        loginScene.add(hbBtn, 1, 4);
        
        loginValid = new Label();
        loginValid.setId("log");
        loginScene.add(loginValid, 1, 6);
        
        btn.setOnAction(new EventHandler<ActionEvent>() {
            
            @Override
            public void handle(ActionEvent event) {
                String name = userNameField.getText();
                String pass = userPassField.getText();
                btn.setVisible(false);
                if (!(name.equals("") && pass.equals(""))) {
                    loginValid.setText("Connecting...");
                    startServerConnection("login", name, pass);
//                    AudioClip sound = new AudioClip("s.mp3");
//                    sound.setVolume(100);
//                    sound.play();
                } else {
                    loginValid.setText("Please Enter user Name And Pass");
                    btn.setVisible(true);
                }
                
            }
            
        });
        
        Scene scene = new Scene(loginScene, 700, 575);
        scene.getStylesheets().add(Client.class.getResource("css/Login.css").toExternalForm());
        stage.close();
        stage = new Stage();
        stage.setTitle("Tic-Tac-Toe-Zoo!");
        stage.setScene(scene);
        stage.show();
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                logOut("", "");
            }
        });
    }
    
    public void creratePlayerChoice() {
        Pane root = new Pane();
        root.setId("pane");
        Scene scene = new Scene(root, 785, 610);
        
        FlowPane flowbg = new FlowPane();
        flowbg.setId("flowbg");
        
        int width = 150;
        
        for (int i = 0; i < client.size(); i++) {
            
            FlowPane temp = new FlowPane();
            temp.setPadding(new Insets(0, 3, 0, 3));
            temp.setAlignment(Pos.CENTER);
            
            final Text actiontarget = new Text();
            root.getChildren().add(actiontarget);
            actiontarget.setId("actiontarget");
            
            Boolean online = client.get(i).active;
            temp.setId("flow");
            
            Label label3 = new Label("" + client.get(i).userName);
            label3.setId("label3");
            
            Label label4 = new Label();
            label4.setId("label4");
            label4.setMaxWidth(Double.MAX_VALUE);
            label4.setAlignment(Pos.CENTER);
            label4.setText("" + client.get(i).score);
            
            Label label5 = new Label("" + i);
            label5.setId("label5");
            
            Button btnBack = new Button("Back");
            btnBack.setId("btnBack");
            root.getChildren().add(btnBack);
            btnBack.setLayoutX(700);
            btnBack.setLayoutY(40);
            btnBack.setMaxWidth(100);
            btnBack.setMaxHeight(50);
            
            
            
            if (online) {
                temp.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                       String  with = label3.getText();
                       playWith = with;
                        // createGameStage(playWith);
                        requestPlay(with, mark);
                    }
                });
                label5.setText("Online");
            } else {
                
                label5.setText("ofline");
                
            }
            
             btnBack.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                       createprofileScene();
                    }
             });
            
            label5.setMaxWidth(Double.MAX_VALUE);
            label5.setAlignment(Pos.CENTER);
            
            temp.getChildren().add(label3);
            temp.getChildren().add(label4);
            temp.getChildren().add(label5);
            
            temp.setMaxWidth(100);
            temp.setMaxHeight(100);
            temp.setHgap(5);
            
            actiontarget.setLayoutX(327);
            actiontarget.setLayoutY(570);
            
            flowbg.getChildren().add(temp);
            
            temp.setOrientation(Orientation.VERTICAL);
            
        }
        flowbg.setLayoutX(170);
        flowbg.setLayoutY(340);
        flowbg.setMaxWidth(550);
        flowbg.setMaxHeight(200);
        root.getChildren().add(flowbg);
        scene.getStylesheets().add(Client.class.getResource("css/chooseplayers.css").toExternalForm());        
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                logOut("", "");
            }
        });
        
    }

    public void createGameStage(String u) {
        
        a = new Boolean[]{true, true, true, true, true, true, true, true, true};
        a0 = new Label();
        a1 = new Label();
        a2 = new Label();
        a3 = new Label();
        a4 = new Label();
        a5 = new Label();
        a6 = new Label();
        a7 = new Label();
        a8 = new Label();
        root = new Pane();
        
        player1 = new Label(userName);
        player1.setId("p1");
        root.getChildren().add(player1);
        
        player2 = new Label(u);
        
        player2.setId("p2");
        root.getChildren().add(player2);
        
        a0.setId("a0");
        a0.setAlignment(Pos.CENTER);
        a0.setOnMouseClicked(new EventHandler<javafx.scene.input.MouseEvent>() {
            @Override
            public void handle(javafx.scene.input.MouseEvent event) {
                System.out.println("turn : " + turn + "a[0] : " + a[0]);
                if (a[0] && turn) {
                    sendCell(0);
                    //match();
                }
            }
            
        });
        root.getChildren().add(a0);
        
        a1.setId("a1");
        a1.setAlignment(Pos.CENTER);
        a1.setOnMouseClicked(new EventHandler<javafx.scene.input.MouseEvent>() {
            @Override
            public void handle(javafx.scene.input.MouseEvent event) {
                 System.out.println("turn : " + turn + "a[0] : " + a[1]);
                if (a[1] && turn) {
                    
                    sendCell(1);

                    //  match();
                }
            }
            
        });
        root.getChildren().add(a1);
        
        a2.setId("a2");
        a2.setAlignment(Pos.CENTER);
        a2.setOnMouseClicked(new EventHandler<javafx.scene.input.MouseEvent>() {
            @Override
            public void handle(javafx.scene.input.MouseEvent event) {
                if (a[2] && turn) {
                    
                    sendCell(2);
                    
                }
            }
            
        });
        root.getChildren().add(a2);
        
        a3.setId("a3");
        a3.setAlignment(Pos.CENTER);
        a3.setOnMouseClicked(new EventHandler<javafx.scene.input.MouseEvent>() {
            @Override
            public void handle(javafx.scene.input.MouseEvent event) {
                if (a[3] && turn) {
                    
                    sendCell(3);
                    
                }
            }
            
        });
        root.getChildren().add(a3);
        
        a4.setId("a4");
        a4.setAlignment(Pos.CENTER);
        a4.setOnMouseClicked(new EventHandler<javafx.scene.input.MouseEvent>() {
            @Override
            public void handle(javafx.scene.input.MouseEvent event) {
                if (a[4] && turn) {
                    
                    sendCell(4);
                    
                }
            }
            
        });
        
        root.getChildren().add(a4);
        
        a5.setId("a5");
        a5.setAlignment(Pos.CENTER);
        a5.setOnMouseClicked(new EventHandler<javafx.scene.input.MouseEvent>() {
            @Override
            public void handle(javafx.scene.input.MouseEvent event) {
                if (a[5] && turn) {
                    
                    sendCell(5);
                    
                }
            }
            
        });
        root.getChildren().add(a5);
        
        a6.setId("a6");
        a6.setAlignment(Pos.CENTER);
        a6.setOnMouseClicked(new EventHandler<javafx.scene.input.MouseEvent>() {
            @Override
            public void handle(javafx.scene.input.MouseEvent event) {
                if (a[6] && turn) {
                    
                    sendCell(6);
                    
                }
            }
            
        });
        root.getChildren().add(a6);
        
        a7.setId("a7");
        a7.setAlignment(Pos.CENTER);
        a7.setOnMouseClicked(new EventHandler<javafx.scene.input.MouseEvent>() {
            @Override
            public void handle(javafx.scene.input.MouseEvent event) {
                if (a[7] && turn) {
                    sendCell(7);
                    
                }
            }
            
        });
        
        root.getChildren().add(a7);
        
        a8.setId("a8");
        a8.setAlignment(Pos.CENTER);
        a8.setOnMouseClicked(new EventHandler<javafx.scene.input.MouseEvent>() {
            @Override
            public void handle(javafx.scene.input.MouseEvent event) {
                if (a[8] && turn) {
                    sendCell(8);
                    
                }
            }
            
        });
        root.getChildren().add(a8);
        
        Button btn = new Button(" ");
        btn.setShape(new Circle(1.5));
        btn.setMaxSize(10, 6);
        btn.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                record = "";
                recording = true;
                // playRecord(record);
            }
        });
        
        root.getChildren().add(btn);
        
        Label exit = new Label(" Exit ");
        exit.setId("ex");
        root.getChildren().add(exit);
        exit.setOnMouseClicked(new EventHandler<javafx.scene.input.MouseEvent>() {
            @Override
            public void handle(javafx.scene.input.MouseEvent event) {
                
                playWith = "";
                turn = true;
                createprofileScene();
                
            }
            
        });
        Scene scene = new Scene(root, 800, 570);
        scene.getStylesheets().add(Client.class.getResource("css/appStyle.css").toExternalForm());
        
        a0.setLayoutX(230);
        a0.setLayoutY(200);
        
        a1.setLayoutX(350);
        a1.setLayoutY(198);
        
        a2.setLayoutX(490);
        a2.setLayoutY(200);
        
        a3.setLayoutX(230);
        a3.setLayoutY(300);
        
        a4.setLayoutX(350);
        a4.setLayoutY(300);
        
        a5.setLayoutX(490);
        a5.setLayoutY(300);
        
        a6.setLayoutX(230);
        a6.setLayoutY(410);
        
        a7.setLayoutX(350);
        a7.setLayoutY(410);
        
        a8.setLayoutX(480);
        a8.setLayoutY(410);
        btn.setLayoutX(339);
        btn.setLayoutY(45);
        
        exit.setLayoutX(60);
        exit.setLayoutY(512);
        
        player1.setLayoutX(75);
        player1.setLayoutY(75);
        player2.setLayoutX(665);
        player2.setLayoutY(75);
        
        stage.close();
        stage = new Stage();
        stage.setTitle("Tic-Tac-Toe-Zoo!");
        stage.setScene(scene);
        stage.show();
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                logOut("", "");
            }
        });
    }
    
    public void createSignUpScene() {
        
        Pane root = new Pane();
        Label userName = new Label("User Name:");
        root.getChildren().add(userName);
        
        TextField userTextField = new TextField();
        userTextField.setId("fild");
        root.getChildren().add(userTextField);
        
        Label pw = new Label("Password:");
        root.getChildren().add(pw);
        
        PasswordField pwBox = new PasswordField();
        pwBox.setId("fild");
        root.getChildren().add(pwBox);
        
        btn = new Button("Sign Up");
        HBox hbBtn = new HBox(10);
        hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
        hbBtn.getChildren().add(btn);
        root.getChildren().add(hbBtn);
        
        loginValid = new Label();
        loginValid.setId("signLable");
        root.getChildren().add(loginValid);
        loginValid.setId("actiontarget");
        
        btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                String name = userTextField.getText();
                String pass = pwBox.getText();
                btn.setVisible(false);
                if (!(name.equals("") && pass.equals(""))) {
                    startServerConnection("signup", name, pass);
                } else {
                    btn.setVisible(true);
                    loginValid.setText("User Name and Pass must not be empty");
                }
            }
        });
        
        Scene scene = new Scene(root, 800, 575);
        scene.getStylesheets().add(SignUp.class.getResource("css/signup.css").toExternalForm());
        userName.setLayoutX(320);
        userName.setLayoutY(140);
        userTextField.setLayoutX(340);
        userTextField.setLayoutY(170);
        pw.setLayoutX(320);
        pw.setLayoutY(230);
        pwBox.setLayoutX(340);
        pwBox.setLayoutY(260);
        hbBtn.setLayoutX(330);
        hbBtn.setLayoutY(330);
        loginValid.setLayoutX(330);
        loginValid.setLayoutY(430);
        
        stage.close();
        stage = new Stage();
        stage.setTitle("Tic-Tac-Toe-Zoo!");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                logOut( "" , "");
            }
        });
    }
    
    public void createprofileScene() {
        
        getPlayers();
        //if(!runnig){
        startServerListener();
        //}
        
        Pane root = new Pane();
        
        Label score = new Label();
        score.setId("score");
        root.getChildren().add(score);
        score.setText("" + this.score);
        Label user = new Label(userName);
        user.setId("login");
        root.getChildren().add(user);
        Button btn = new Button("Play Now ");
        btn.setId("btn");
        
        btn.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                choice();
                // playWith = "duaa";
                //requestPlay(playWith, mark);
            }
        });
        
        Button play = new Button("Play Record ");
        play.setId("play");
        play.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(!record.equals("")){
                    playRecord(record);
                }else{
                    Alert a = new Alert(Alert.AlertType.ERROR);
                    a.setContentText("No Records");
                    a.showAndWait();
                }
                
                // playRecord(record);
            }
        });
        
        
        Button log = new Button("Log Out");
        log.setId("log");
        log.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                logOut("", "");               // playRecord(record);
            }
        });
                root.getChildren().add(log);
        root.getChildren().add(play);
        
        root.getChildren().add(btn);
        
        score.setLayoutX(300);
        
        score.setLayoutY(220);
        
        user.setLayoutX(350);
        user.setLayoutY(50);
        
        btn.setLayoutX(90);
        btn.setLayoutY(458);
        
        play.setLayoutX(490);
        play.setLayoutY(458);
        
        log.setLayoutX(510);
        log.setLayoutY(358);
        
        Scene scene = new Scene(root, 700, 570);
        stage.setResizable(false);
        stage.setScene(scene);
        scene.getStylesheets().add(Client.class.getResource("css/pro.css").toExternalForm());
        stage.show();
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                logOut("", "");
            }
        });
        
    }
    
    synchronized public void startServerListener() {
        
        if (!runnig) {
            
            runnig = true;
            serverListener = new Thread() {
                public void run() {
                    while (runnig) {
                        try {
                            String message = "";
                            System.out.println("lisin  ....");
                            message = inStream.readLine();
                            if(message != null){
                                serverMessageHandler(message);
                            }
                            
                        } catch (IOException ex) {
                            runnig = false;
                            try {
                                socket.close();
                                outStream.close();
                                inStream.close();
                                
                            } catch (IOException ex1) {
                                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex1);
                            }
                            
                            serverListener.stop();
                        }
                    }
                }
            };
            
            serverListener.start();
        }
        
    }
    
    synchronized public String serverMessageHandler(String msg) {
        System.err.println(msg);
        String message = msg.split("\\:")[0];
        switch (message) {
            case "login":
                //login:name=username,pass=pass
                //login:false
                String replay = msg.split("\\:")[1];
                return replay;
            
            case "signup":
                //signup:true|false
                replay = msg.split("\\:")[1];
                return replay;

            //logout:server
            //logout:name=uername,pass=pass
            case "online": {
                replay = msg.split("\\:")[1];
                String[] players = replay.split("\\,");
                for (int i = 0; i < players.length; i++) {
                    if (!players[i].equals("")) {
                        String[] player = players[i].split("\\-");
                        Player p = new Player(player[0], player[1], Boolean.parseBoolean(player[2]), null, null, 1);
                        p.active = player[2].equals("0") ? false : true;
                        client.add(p);
                    }
                }
                break;
            }
            
            case "update": {
                replay = msg.split("\\:")[1];
                for (Player p : client) {
                    if (p.userName.equals(replay)) {
                        System.out.println("ssssss1 " + p.active);
                        p.active = !p.active;
                        System.out.println("ssssss2 " + p.active);
                        //break;
                    }
                    System.out.println("Player " + p.userName + " is " + p.active);
                }
                //update UI
                break;
            }
            
            case "record": {
                replay = msg.split("\\:")[1];
                for (int i = 0; i <= records.size(); i++) {
                    records.add(replay.split("\\,")[i]);
                }
                break;
                
            }
            
            case "game": {
                replay = msg.split("\\:")[1];
                String[] gm = replay.split("\\,");
                
                for (int i = 0; i <= gm.length; i++) {
                    String[] game = gm[i].split("\\-");
                    Game g = new Game(game[0], game[1], game[2], Integer.parseInt(game[3]));
                    games.add(g);
                }
                
                break;
                
            }
            
            case "requestFrom": {

                //requestFrom:userame,mark
                replay = msg.split("\\:")[1];
                String requestPlayer = replay.split("\\,")[0];
                String Tmark = replay.split("\\,")[1];
                
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        String requestReplay = "no";
                        if(playWith.equals("")){
                             requestReplay = showRequestotificatio(requestPlayer, mark);
                                mark = Tmark.equals("x") ? "o" : "x";
                        }
                        outStream.println("requestReplay:" + requestPlayer + "," + requestReplay);
                        if (requestReplay.equals("yes")) {
                            turn = false;
                            playWith = requestReplay;
                            Client.this.createGameStage(requestPlayer);
                            
                        } else {
                          //  playWith = "";
                            System.out.println("game refused   ");
                        }
                    }
                });
                break;
                
            }
            case "requestReplay": {
                //requestReplay:my name,state
                replay = msg.split("\\:")[1];
                String user = replay.split("\\,")[0];
                String acceptace = replay.split("\\,")[1];
                if (acceptace.equals("yes")) {
                    //start game
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            turn = true;
                            Client.this.createGameStage(playWith);
                        }
                    });
                    System.out.println("Game Accepted " + mark);
                } else if (acceptace.equals("no")) {
                    // acel request
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            turn = true;
                            playWith = "";
                            Alert a = new Alert(Alert.AlertType.ERROR);
                            a.setTitle("Message");
                            a.setContentText("request Refused user may be ofline or bussy");
                            a.showAndWait();
                            Client.this.createprofileScene();
                        }
                    });
                    System.out.println("Game refused ");
                }
                break;
            }
            case "cell": {
                replay = msg.split("\\:")[1];
                String otherMark = mark.equals("x") ? "o" : "x";
                System.err.println("i msg  " + msg);
                setCell(replay, otherMark);
                
            }
            
            case "logout": {
                replay = msg.split("\\:")[1];
                
                if(replay.equals("server")){
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            logOut("", "");
                            loginValid.setText("server down ! try to connect again... ");
                        }
                    });
                    
                    //runtimeUIUpdates("server down ! try to connect again... ");
                }
            }
            
            default:
                System.out.println(msg);
        }
        return "";
    }
    
    synchronized private void runtimeUIUpdates(String errorMsg) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (errorMsg != null) {
                    loginValid.setText(errorMsg);
                    btn.setVisible(true);
                }
                
                if (Client.this.vaildUser) {
                    Client.this.createprofileScene();
                    score = Integer.valueOf(errorMsg.split("\\,")[2]);
                } else if (Client.this.userSigned) {
                    
                    Client.this.createprofileScene();
                    score = Integer.valueOf(errorMsg.split("\\,")[2]);
                    
                }
            }
        });
        
    }
    
    synchronized private void logOut(String userName, String userPass) {
        if (serverAvilability) {
            final String loOutMsg = "logout:name=" + this.userName + ",pass=" + this.userPass;
            outStream.println(loOutMsg);
            try {
                socket.close();
                inStream.close();
                outStream.close();
                runnig = false;
                serverAvilability = false;
                userName = null;
                userPass = null;
                vaildUser = false;
                record = "";
            } catch (IOException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                createLoginScene();
            }
        }
    }
    
    synchronized public void requestPlay(String name, String mark) {
        final String loginMsg = "requestTo:" + name + "," + mark;
        System.out.println("requestTo " + name + "," + mark);
        outStream.println(loginMsg);
    }
    
    synchronized public void getPlayers() {
        if (!get) {
            System.out.println("try to get");
            try {
                final String loginMsg = "online:players";
                outStream.println(loginMsg);
                String reply = inStream.readLine();
                String msg = serverMessageHandler(reply);
                
            } catch (IOException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
            get = true;
        }
        
    }
    
    synchronized public void getRecords() {
        try {
            final String loginMsg = "records:";
            outStream.println(loginMsg);
            String reply = inStream.readLine();
            String msg = serverMessageHandler(reply);
            for (int i = 0; i <= records.size(); i++) {
                records.get(i);//print result in screen     
            }
            
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    synchronized public void getgames() {
        try {
            final String loginMsg = "games:";
            outStream.println(loginMsg);
            String reply = inStream.readLine();
            String msg = serverMessageHandler(reply);
            for (int i = 0; i <= games.size(); i++) {
                games.get(i);//print result in screen
            }
            
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    synchronized void sendCell(int index) {
        String loginMsg = "cell:" + index;
        setCell("" + index, mark);
        outStream.println(loginMsg);
    }
    
    synchronized private void setCell(String index, String otherMark) {
        if(recording){
            record += index + "-" + otherMark + ",";
        }
        turn = !turn;
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                a[Integer.valueOf(index)] = false;
                switch (index) {
                    case "0":
                        a0.setText(otherMark);
                        break;
                    case "1":
                        a1.setText(otherMark);
                        break;
                    case "2":
                        a2.setText(otherMark);
                        break;
                    case "3":
                        a3.setText(otherMark);
                        break;
                    case "4":
                        a4.setText(otherMark);
                        break;
                    case "5":
                        a5.setText(otherMark);
                        break;
                    case "6":
                        a6.setText(otherMark);
                        break;
                    case "7":
                        a7.setText(otherMark);
                        break;
                    case "8":
                        a8.setText(otherMark);
                        break;
                    
                }
                
                if (turn) {
                    // userName.
                    player1.setStyle("-fx-text-fill: #C11B17;");
                    player2.setStyle("-fx-text-fill: #4CC552;");
                } else {
                    player2.setStyle("-fx-text-fill: #C11B17;");
                    player1.setStyle("-fx-text-fill: #4CC552;");
                    
                }
                match();
            }
        });
        
    }
    
    synchronized void sendScore(int score) {
        String loginMsg = "score:" + userName + "," + this.score;
        outStream.println(loginMsg);
    }
    
    synchronized public void choice() {
        
        ArrayList<String> choices = new ArrayList<String>();
        choices.add("Two Player");
        choices.add("Computer");
        ChoiceDialog<String> dialog = new ChoiceDialog<>("Two Player", choices);
        dialog.setTitle("Choice Dialog");
        dialog.setHeaderText("select Player");
        dialog.setContentText("Choose your letter:");
        
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {

            //System.out.println("Your choice: " + result.get());
            if (result.get().equals("Two Player")) {
                
                System.out.println("tooooooo");
                creratePlayerChoice();
            }
        }

// The Java 8 way to get the response value (with lambda expression).
        result.ifPresent(letter -> System.out.println("Your choice: " + letter));
        
    }
    
    synchronized private String showRequestotificatio(String replayPlay, String mark) {
        
        String rep;        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Request for play game");
        alert.setHeaderText(replayPlay + " want to play game with you");
        alert.setContentText("Choose your option.");
        
        ButtonType buttonTypeOne = new ButtonType("yes");
        
        ButtonType buttonTypeCancel = new ButtonType("no", ButtonBar.ButtonData.CANCEL_CLOSE);
        
        alert.getButtonTypes().setAll(buttonTypeOne, buttonTypeCancel);
        
        Optional<ButtonType> result = alert.showAndWait();
        
        System.out.println(result.get().getText());
        rep = result.get().getText();
        return rep;
    }
    
    synchronized public void match() {
        
        String otherMark = mark.equals("x") ? "o" : "x";
        
        System.out.println(mark + "" + otherMark);
        if (a0.getText() == a1.getText() && a0.getText() == a2.getText() && !a0.getText().equals("")) {
            if (a0.getText() == mark) {
                ++score;
                Line line = new Line();
                line.setStartX(230.230f);
                line.setStartY(240.400f);
                line.setEndX(580.230f);
                line.setEndY(240.400f);
                line.setStrokeWidth(8);
                line.setStroke(Color.LIGHTGREY);
                
                root.getChildren().add(line);
                
                Label win = new Label("WIN");
                win.setId("win");
                win.setAlignment(Pos.CENTER);
                root.getChildren().add(win);
                win.setLayoutX(260);
                win.setLayoutY(300);
                sendScore(score);
            } else if (a0.getText() == otherMark) {
                Label lose = new Label("LOSE");
                lose.setId("lose");
                lose.setAlignment(Pos.CENTER);
                root.getChildren().add(lose);
                lose.setLayoutX(260);
                lose.setLayoutY(300);
                
            }
        } else if (a3.getText() == a4.getText() && a4.getText() == a5.getText() && !a3.getText().equals("")) {
            if (a3.getText() == mark) {
                ++score;
                Line line = new Line();
                line.setStartX(230.230f);
                line.setStartY(340.400f);
                line.setEndX(580.230f);
                line.setEndY(340.400f);
                line.setStrokeWidth(8);
                line.setStroke(Color.LIGHTGREY);
                
                root.getChildren().add(line);
                
                Label win = new Label("WIN");
                win.setId("win");
                win.setAlignment(Pos.CENTER);
                root.getChildren().add(win);
                win.setLayoutX(260);
                win.setLayoutY(300);
                sendScore(score);
            } else if (a3.getText() == otherMark) {
                Label lose = new Label("LOSE");
                lose.setId("lose");
                lose.setAlignment(Pos.CENTER);
                root.getChildren().add(lose);
                lose.setLayoutX(260);
                lose.setLayoutY(300);
                
            }
        } else if (a6.getText() == a7.getText() && a7.getText() == a8.getText() && !a6.getText().equals("")) {
            if (a6.getText() == mark) {
                ++score;
                Line line = new Line();
                line.setStartX(230.230f);
                line.setStartY(450.400f);
                line.setEndX(580.230f);
                line.setEndY(450.400f);
                line.setStrokeWidth(8);
                line.setStroke(Color.LIGHTGREY);
                
                root.getChildren().add(line);
                
                Label win = new Label("WIN");
                win.setId("win");
                win.setAlignment(Pos.CENTER);
                root.getChildren().add(win);
                win.setLayoutX(260);
                win.setLayoutY(300);
                sendScore(score);
            } else if (a6.getText() == otherMark) {
                Label lose = new Label("LOSE");
                lose.setId("lose");
                lose.setAlignment(Pos.CENTER);
                root.getChildren().add(lose);
                lose.setLayoutX(260);
                lose.setLayoutY(300);
                
            }
        } else if (a0.getText() == a4.getText() && a4.getText() == a8.getText() && !a8.getText().equals("")) {
            if (a0.getText() == mark) {
                ++score;
                Line line = new Line();
                line.setStartX(250.230f);
                line.setStartY(230.400f);
                line.setEndX(580.230f);
                line.setEndY(490.400f);
                line.setStrokeWidth(8);
                line.setStroke(Color.LIGHTGREY);
                
                root.getChildren().add(line);
                
                Label win = new Label("WIN");
                win.setId("win");
                win.setAlignment(Pos.CENTER);
                root.getChildren().add(win);
                win.setLayoutX(260);
                win.setLayoutY(300);
                sendScore(score);
            } else if (a0.getText() == otherMark) {
                Label lose = new Label("LOSE");
                lose.setId("lose");
                lose.setAlignment(Pos.CENTER);
                root.getChildren().add(lose);
                lose.setLayoutX(260);
                lose.setLayoutY(300);
                
            }
            
        } else if (a2.getText() == a4.getText() && a4.getText() == a6.getText() && !a2.getText().equals("")) {
            if (a2.getText() == mark) {
                ++score;
                Line line = new Line();
                line.setStartX(570.200f);
                line.setStartY(230.400f);
                line.setEndX(230.200f);
                line.setEndY(490.400f);
                line.setStrokeWidth(8);
                line.setStroke(Color.LIGHTGREY);
                
                root.getChildren().add(line);
                
                Label win = new Label("WIN");
                win.setId("win");
                win.setAlignment(Pos.CENTER);
                root.getChildren().add(win);
                win.setLayoutX(260);
                win.setLayoutY(300);
                sendScore(score);
            } else if (a2.getText() == otherMark) {
                Label lose = new Label("LOSE");
                lose.setId("lose");
                lose.setAlignment(Pos.CENTER);
                root.getChildren().add(lose);
                lose.setLayoutX(260);
                lose.setLayoutY(300);
                
            }
        } else if (a0.getText() == a3.getText() && a3.getText() == a6.getText() && !a0.getText().equals("")) {
            if (a0.getText() == mark) {
                ++score;
                Line line = new Line();
                line.setStartX(280.200f);
                line.setStartY(230.400f);
                line.setEndX(280.200f);
                line.setEndY(470.400f);
                line.setStrokeWidth(8);
                line.setStroke(Color.LIGHTGREY);
                
                root.getChildren().add(line);
                
                Label win = new Label("WIN");
                win.setId("win");
                win.setAlignment(Pos.CENTER);
                root.getChildren().add(win);
                win.setLayoutX(260);
                win.setLayoutY(300);
                sendScore(score);
            } else if (a0.getText() == otherMark) {
                Label lose = new Label("LOSE");
                lose.setId("lose");
                lose.setAlignment(Pos.CENTER);
                root.getChildren().add(lose);
                lose.setLayoutX(260);
                lose.setLayoutY(300);
                
            }
        } else if (a1.getText() == a4.getText() && a4.getText() == a7.getText() && !a1.getText().equals("")) {
            if (a1.getText() == mark) {
                ++score;
                Line line = new Line();
                line.setStartX(410.200f);
                line.setStartY(230.400f);
                line.setEndX(410.200f);
                line.setEndY(470.400f);
                line.setStrokeWidth(8);
                line.setStroke(Color.LIGHTGREY);
                
                root.getChildren().add(line);
                
                Label win = new Label("WIN");
                win.setId("win");
                win.setAlignment(Pos.CENTER);
                root.getChildren().add(win);
                win.setLayoutX(260);
                win.setLayoutY(300);
                sendScore(score);
            } else if (a1.getText() == otherMark) {
                Label lose = new Label("LOSE");
                lose.setId("lose");
                lose.setAlignment(Pos.CENTER);
                root.getChildren().add(lose);
                lose.setLayoutX(260);
                lose.setLayoutY(300);
                
            }
        } else if (a2.getText() == a5.getText() && a5.getText() == a8.getText() && !a2.getText().equals("")) {
            if (a2.getText() == mark) {
                ++score;
                Line line = new Line();
                line.setStartX(540.200f);
                line.setStartY(230.400f);
                line.setEndX(540.200f);
                line.setEndY(470.400f);
                line.setStrokeWidth(8);
                line.setStroke(Color.LIGHTGREY);
                
                root.getChildren().add(line);
                
                Label win = new Label("WIN");
                win.setId("win");
                win.setAlignment(Pos.CENTER);
                root.getChildren().add(win);
                win.setLayoutX(260);
                win.setLayoutY(300);
                sendScore(score);
            } else if (a2.getText() == otherMark) {
                Label lose = new Label("LOSE");
                lose.setId("lose");
                lose.setAlignment(Pos.CENTER);
                root.getChildren().add(lose);
                lose.setLayoutX(260);
                lose.setLayoutY(300);
                
            }
        }
        
    }
    
    synchronized public void playRecord(String record) {
        
        createGameStage("");
        String[] cells = record.split("\\,");
        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("in record play ");
                
                for (String i : cells) {
                    System.out.println("record  " + i);
                    if (!i.equals("")) {
                        
                        String index = i.split("\\-")[0];
                        String mark = i.split("\\-")[1];
                        setCell(index, mark);
                        turn = false;
                        this.myWait();
                    }
                    
                }
                turn = true;
                try {
                    this.finalize();
                    System.out.println("finalis");
                } catch (Throwable ex) {
                    Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            synchronized public void myWait() {
                try {
                    this.wait(1000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
        }).start();
        
    }
    
}
