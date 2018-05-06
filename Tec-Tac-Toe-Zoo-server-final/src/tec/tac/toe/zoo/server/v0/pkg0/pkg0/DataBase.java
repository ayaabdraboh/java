/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tec.tac.toe.zoo.server.v0.pkg0.pkg0;

/**
 *
 * @author lw
 */
public interface DataBase {
    
    
    public void DataBaseClass();
    public void closseConection();
   
    void insertPlayer( );//params
    // adding player
    
    void deletePlayer(); //userame pass
    // delete player
    
    void updateUserData(); //userame pass // updatedData
    // player update his data
    
    void setscore(); // userame
    // update the score of player
    
   
    void selectPlayers();//userame pass
    //get All Players From DataBase  
    //retur query result
    
    void insertGame();//params
    //adding game  
    //retur

    void selectGame();
    //accept player id 
    //retur all games 
    
    //showing    
    void getPlayerRecords();
    //accept player id return all player records
    void getRecords();// playerame 
    //accept Game
    
    //retur query result
    void deleteRecord();
    
    
    void addRequest();//addnew Request //update Request To add Pinding request 0 1 2
    //returquery result
    void deleterequest();
    //request id 
    //retur request result 
    void updateRequest();
     //accept request Id 
    //update oly i pinding state //0 refused 1 accepted 2 pinding 
    //retur query result
    void selectRequests();
    // accept player Userame 
    // select player Requests
    // it should retur the query result;
    
}
