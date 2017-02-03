/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client.controller;

import client.view.IObserver;

/**
 *
 * @author al1as
 */
public interface IClientController {
    void sendName(String name);
    void sendFileListRequest();
    void sendFileContentRequest(String filename);
    void sendRangesAndLock(String start, String end);
    void sendUnlocking();
    void sendSaveRequest(String content);
        
    void connect();
}
