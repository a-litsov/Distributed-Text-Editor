/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client.controller;

import client.model.BClientModel;
import client.model.IClientModel;

/**
 *
 * @author al1as
 */
public class ClientController implements IClientController {

    private IClientModel clientModel;
    
    @Override
    public void sendName(String name) {
        clientModel = BClientModel.build();
        clientModel.sendName(name);
    }

    @Override
    public void sendFileListRequest() {
        clientModel = BClientModel.build();
        clientModel.sendFileListRequest();
    }

    @Override
    public void sendFileContentRequest(String filename) {
        clientModel = BClientModel.build();
        clientModel.sendFileContentRequest(filename);
    }

    @Override
    public void sendRangesAndLock(String startLine, String endLine, int startSymbol, int endSymbol) {
        clientModel = BClientModel.build();
        clientModel.sendRangesAndLock(startLine, endLine, startSymbol, endSymbol);
    }

    @Override
    public void sendUnlocking() {
        clientModel = BClientModel.build();
        clientModel.sendUnlocking();
    }

    @Override
    public void sendSaveRequest(String content) {
        clientModel = BClientModel.build();
        clientModel.sendSaveRequest(content);
    }
    
    @Override
    public void connect() {
        clientModel = BClientModel.build();
        clientModel.connect();
    }
    
    @Override
    public void incEndLock(int value) {
        clientModel = BClientModel.build();
        clientModel.incEndLock(value);
    }

    @Override
    public int getStartSymbolRange() {
        clientModel = BClientModel.build();
        return clientModel.getStartSymbolRange();
    }

    @Override
    public int getEndSymbolRange() {
        clientModel = BClientModel.build();
        return clientModel.getEndSymbolRange();
    }

    @Override
    public void incEndLineChanging(int value) {
        clientModel = BClientModel.build();
        clientModel.incEndLineChanging(value);
    }
}
