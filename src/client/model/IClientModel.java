/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client.model;

import client.view.IObserver;
import client.view.TextFragment;
import java.util.ArrayList;

/**
 *
 * @author al1as
 */
public interface IClientModel {
    public void connect();
    
    
    void loginUser(String name, String pass);
    void registerUser(String login, String pass);
    void updateRegistrationStatusObs(); 
    void sendFileListRequest();
    void sendFileContentRequest(String filename);
    void sendRangesAndLock(int startSymbol, int endSymbol);
    void sendUnlocking();
    void sendSaveRequest(String content);
    void incEndLock(int value);
    void incEndLineChanging(int value);
    
    
    String getId();
    String getUsername();
    String getPrevFilename();
    String getFileList();
    String getFileContent();
    String getLockedPart1();
    String getUnlockedPart();
    String getLockedPart2();
    int getStartSymbolRange();
    int getEndSymbolRange();
    ArrayList<TextFragment> getTextFragments();
    void addObserver(IObserver o);
}
