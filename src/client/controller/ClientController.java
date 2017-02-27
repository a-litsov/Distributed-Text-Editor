/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client.controller;

import client.model.BClientModel;
import client.model.IClientModel;
import client.view.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *
 * @author al1as
 */
public class ClientController implements IClientController {

    private IClientModel clientModel;
	private IClientView clientView;
	
	public ClientController() {
		clientModel = BClientModel.build();
		clientView = BClientView.build();
		clientView.addOpenListener(new OpenListener());
		clientView.addSaveListener(new SaveListener());
		clientView.addRefreshListener(new RefreshListener());
		clientView.addLockListener(new LockListener());
		clientView.addUnlockListener(new UnlockListener());
		connect();
	}
	
	class OpenListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			clientModel.sendFileListRequest();
		}
	}
	
	class SaveListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String content = clientView.getSavingContent();
			sendSaveRequest(content);
			System.out.println("Modified content sent, here it is:\n" + content);
		}
	}
	
	class RefreshListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			refreshFileContent();
			System.out.println("Content refresh request was sent.\n");
		}
	}
	
	class LockListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			int startSymbolNumber = clientView.getStartLockPos();
			int endSymbolNumber = clientView.getEndLockPos();
			sendRangesAndLock(startSymbolNumber, endSymbolNumber);
			System.out.println("Lock was parsed and sended to server.");
		}
	}
	
	class UnlockListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			sendUnlocking();
			System.out.println("Unlocking message was sent and file content in Text pane updated.");
		}
	}
	  
    @Override
    public void loginUser(String name, String pass) {
        clientModel = BClientModel.build();
        clientModel.loginUser(name, pass);
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
    public void sendRangesAndLock(int startSymbol, int endSymbol) {
        clientModel = BClientModel.build();
        clientModel.sendRangesAndLock(startSymbol, endSymbol);
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

    @Override
    public void registerUser(String login, String pass) {
        clientModel = BClientModel.build();
        clientModel.registerUser(login, pass);
    }

    @Override
    public void refreshFileContent() {
        clientModel = BClientModel.build();
        clientModel.refreshFileContent();
    }
}
