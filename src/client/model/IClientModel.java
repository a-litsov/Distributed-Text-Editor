/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client.model;

import client.view.IObserver;

/**
 *
 * @author al1as
 */
public interface IClientModel {
    public void connect();
    
    
    void sendName(String name);
    void sendFileListRequest();
    void sendFileContentRequest(String filename);
    void sendRangesAndLock(String start, String end);
    void sendUnlocking();
    void sendSaveRequest(String content);
    
    
    String getId();
    String getPrevFilename();
    String getFileList();
    String getFileContent();
    String getLockedPart1();
    String getUnlockedPart();
    String getLockedPart2();
    void addObserver(IObserver o);
}
