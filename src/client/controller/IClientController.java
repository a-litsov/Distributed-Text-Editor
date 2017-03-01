/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client.controller;

/**
 *
 * @author al1as
 */
public interface IClientController {
    void loginUser(String name, String pass);
    void registerUser(String login, String pass);
    void sendFileListRequest();
    void sendFileContentRequest(String filename);
    void sendRangesAndLock(int startSymbol, int endSymbol);
    void sendUnlocking();
    void sendSaveRequest(String content);
//    void incEndLock(int value);
    int getStartSymbolRange();
    int getEndSymbolRange();
//    void incEndLineChanging(int value);
    void connect();
    void refreshFileContent();
}
