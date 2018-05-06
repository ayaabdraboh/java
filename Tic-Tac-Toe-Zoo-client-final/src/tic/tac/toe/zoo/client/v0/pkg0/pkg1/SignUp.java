package tic.tac.toe.zoo.client.v0.pkg0.pkg1;

import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class SignUp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        Pane root = new Pane();

        Label userName = new Label("User Name:");
        root.getChildren().add(userName);

        TextField userTextField = new TextField();
        userTextField .setId("fild");
        root.getChildren().add(userTextField);

        Label pw = new Label("Password:");
        root.getChildren().add(pw);

        PasswordField pwBox = new PasswordField();
         pwBox.setId("fild");
        root.getChildren().add(pwBox);

        Button btn = new Button("Sign Up");
        HBox hbBtn = new HBox(10);
        hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
        hbBtn.getChildren().add(btn);
        root.getChildren().add(hbBtn);

        final Text actiontarget = new Text();
        root.getChildren().add(actiontarget);
        actiontarget.setId("actiontarget");

        btn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent e) {
                actiontarget.setText("Please Sign Up");
            }
        });

        Scene scene = new Scene(root, 800, 575);

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
        actiontarget.setLayoutX(330);
        actiontarget.setLayoutY(430);
        
        
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        scene.getStylesheets().add(SignUp.class.getResource("signup.css").toExternalForm());
        primaryStage.show();
    }
}
