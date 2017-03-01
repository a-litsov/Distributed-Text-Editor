/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.presenter;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;
import server.model.FileElement;
import server.model.IServerModel;
import server.model.Range;
import server.view.IServerView;

/**
 *
 * @author al1as
 */
class ServerPresenter extends Thread implements IServerPresenter {
    IServerView v;
    IServerModel m;
    
    UUID id = UUID.randomUUID();
    
    ArrayList file_elements;  
    String username;
    String filename;
    int start;
    int end;
    FileElement element;
    boolean f = true;
    
    public UUID getUUID() {
        return id;
    }
    public String getFilename() {
        return filename;
    }
    
    public ServerPresenter(IServerModel _m, IServerView _v) {
        v = _v;
        m = _m;
        
        start();
        m.addPresenter(this);
    }
    
    private void GetRanges() {
        int[] ranges = v.getRanges();
        start = ranges[0];
        end = ranges[1];
        Range tmp_range = new Range(start, end);
        element = new FileElement(id, username, tmp_range);
        Boolean isOk = m.putFileElement(filename, element);
        if(isOk) 
            v.sendString("Ranges was set successfully");
        else {
            sendFileContent();
            v.sendString("Error with setting ranges");   
        }
    }
    
    private void Unlock() {
        m.extractFileElement(filename, element);
    }
    
    private void sendFileContent() {
        String content = m.getFileContent(filename);
        ArrayList<Range> ranges = m.getFileRanges(filename);
        v.SendFileContent(content, ranges);
    }
    
    @Override
    public void run() {
        v.sendString(id.toString());
        System.out.println("send ID");
        
        while(f)
        {
			String s = v.getString();
			switch(s) {
				case "Ranges sending":
					GetRanges();
					break;
				case "Unlocking":
					Unlock();
					sendFileContent();
					v.sendString("Successfully unlocked!");
					break;
				case "File saving":
					String LockedContent = v.getString();
					String endLineChanged = v.getString();
					int endLineChanging = Integer.parseInt(endLineChanged);
					if (endLineChanging != 0)
						m.updateRanges(endLineChanging, filename, end, id);
					m.Save(LockedContent, filename, id, element.getEnd() - endLineChanging);
					Unlock();
					sendFileContent();
					v.sendString("File saved successfully");
					break;
				case "User login":
					username = v.getString();
					String passHash = v.getString();
					boolean res = m.loginUser(username, passHash);
					if (res)
						v.sendString("Login successful");
					else
						v.sendString("Error! Failed login!");
					break;
				case "User registration":
					username = v.getString();
					passHash = v.getString();
					res = m.registerUser(username, passHash);
					if (res)
						v.sendString("Registration successful");
					 else
						v.sendString("Error! Failed registration!");
					break;
				case "Get list of files.":
					final File folder = new File("Shared");
					v.sendString("File list sending");
					v.sendString(m.listFilesForFolder(folder));
					break;
				case "Get file content":
					filename = v.getString();
					sendFileContent();
					break;
				case "end":
					f = false;
					break;
			}     
        }
    }
    
    public void update() {
        if(m.getMessage().equals("end"))
            v.sendString("Server stopped");
    }
}
