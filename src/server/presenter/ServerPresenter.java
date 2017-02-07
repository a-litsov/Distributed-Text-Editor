/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.presenter;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    String start;
    String end;
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
        String[] ranges = v.getRanges();
        start = ranges[0];
        end = ranges[1];
        Range tmp_range = new Range(Integer.parseInt(start), Integer.parseInt(end));
        element = new FileElement(id, username, tmp_range);
        String answer = m.putFileElement(filename, element);
        v.sendMes(answer);
    }
    
    private void Unlock() {
        m.extractFileElement(filename, element);
    }
    
    private void sendFileContent(String filename) {
        String content = m.getFileContent(filename);
        v.SendFileContent(content);
    }
    
    @Override
    public void run() {
        v.sendMes(id.toString());
        System.out.println("send ID");
        
        while(f)
        {
                String s = v.getString();

                if(s.equals("Ranges sending")) {
//                    sendFileContent(filename);
//                  Здесь не имеет смысла посылать контент, т.к. нужно сделать онлайн-обновление локов и контента у всех
                    GetRanges();
                    m.SendToDB(element.getUsername(), filename);
                }
               
                if(s.equals("Unlocking")) {
                    Unlock();
                    sendFileContent(filename);
                }
//                
//                
                if(s.equals("File saving")) {
                    Unlock();
                    String LockedContent = v.getString();
                    m.Save(LockedContent, filename, element);
                    sendFileContent(filename);
                    v.sendMes("File saved successfully");
                }
                
                
                if(s.equals("Name sending")) {
                    username = v.getName();
                    boolean res = m.addName(username);
                    if(res) {
                        v.sendMes("Ok!Previous filename sending.");
                        String prev_filename = m.GetFromDB(username);
                        v.sendMes(prev_filename);
                    } else {
                        v.sendMes("Error! Failed filename sending!");
                    }
                }
                
                
                if(s.equals("Get list of files.")) {
                    final File folder = new File("Shared");
                    v.sendMes("File list sending");
                    v.sendMes(m.listFilesForFolder(folder));
                }
                if(s.equals("Get file content")) {
                    filename = v.getString();
                    String content = m.getFileContent(filename);
                    v.SendFileContent(content);
                }
                if(s.equals("Remove name"))
                    m.removeName(username);
                if(s.equals("end"))
                    f = false;         
        }
    }
    
    public void update() {
        if(m.getMessage().equals("end"))
            v.sendMes("Server stopped");
    }
}
