/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tec.tac.toe.zoo.server.v0.pkg0.pkg0;

import java.util.List;

/**
 *
 * @author abdelmun3m
 */
     class Player {
    String userName ;
    String passWord ;
    List requestList ;
    List gameList ;
    boolean active;
    int score  = 0;
    
    
    public Player(String name, String pass,  boolean active , List request, List game,int score)
        {
            this.score = Integer.valueOf(pass);
            userName=name;
            requestList=request;
            gameList=game;
        }
     }

    