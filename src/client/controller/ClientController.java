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
public class ClientController implements IClientController, ILockObserver {

    private IClientModel clientModel;
	private IClientView clientView;
	
	public ClientController(IClientModel clientModel) {
		this.clientModel = clientModel;
		clientView = BClientView.build(clientModel, this);

		clientView.createNumberedTextPane();
        clientView.createMenu();
        clientView.createBottomLabels();
		clientView.showForm();
		
		clientView.addOpenListener(new OpenListener());
		clientView.addSaveListener(new SaveListener());
		clientView.addRefreshListener(new RefreshListener());
		clientView.addLockListener(new LockListener());
		clientView.addUnlockListener(new UnlockListener());
		clientView.addLockObserver(this);
		
		clientView.setStatus("Connecting to server..");
		connect();
	}

	@Override
	public void updateEndSymbol(int value) {
		clientModel.incEndLock(value);
	}

	@Override
	public void updateEndLineChanging(int value) {
		clientModel.incEndLineChanging(value);
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
    public void loginUser(String login, String pass) {
		String passHash = MD5(pass);
        clientModel.loginUser(login, passHash);
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
    public void registerUser(String login, String pass) {
		String passHash = MD5(pass);
        clientModel.registerUser(login, passHash);
    }

    @Override
    public void refreshFileContent() {
        clientModel = BClientModel.build();
        clientModel.refreshFileContent();
    }
	
	private String MD5(String md5) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] array = md.digest(md5.getBytes());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            System.out.println("Problem with md5 alghorithm occured. Null string returned.");
        }
        return null;
    }
}