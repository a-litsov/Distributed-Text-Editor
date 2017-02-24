/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.view;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import server.model.Range;

/**
 *
 * @author al1as
 */
public class ServerView implements IServerView {
    Socket cs;
    DataInputStream dis;
    DataOutputStream dos;

    public ServerView(Socket _cs) {
        try {
            cs = _cs;
            dis = new DataInputStream(cs.getInputStream());
            dos = new DataOutputStream(cs.getOutputStream());
        } catch (IOException ex) {
            Logger.getLogger(ServerView.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public int[] getRanges() {
        int[] ranges = new int[2];
        try
        {
            ranges[0] = dis.readInt(); // start
            ranges[1] = dis.readInt(); // end
            // В презентере сконструировать Range и добавить его в общую хэштаблицу
        } catch (IOException ex) {
                Logger.getLogger(ServerView.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ranges;
    }

    @Override
    public void sendString(String str) {
        try {
            dos.writeUTF(str);
        } catch (IOException ex) {
            Logger.getLogger(ServerView.class.getName()).log(Level.SEVERE, null, ex);
        }  
    }

    @Override
    public void SendFileContent(String content, ArrayList<Range> ranges) {
        try {
            dos.writeUTF("File content with ranges sending.");
            dos.writeUTF(content);
            // Sending ranges
            dos.writeInt(ranges.size());
            for(int i = 0; i < ranges.size(); i++) {
                dos.writeInt(ranges.get(i).getStart());
                dos.writeInt(ranges.get(i).getEnd());
            }
        } catch (IOException ex) {
            Logger.getLogger(ServerView.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void SendFileContent(String content) {
        try {
            dos.writeUTF("File content sending.");
            dos.writeUTF(content);
        } catch (IOException ex) {
            Logger.getLogger(ServerView.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public String getString() {
        String str = "null";
        try {
            str = dis.readUTF();
        } catch (IOException ex) {
            Logger.getLogger(ServerView.class.getName()).log(Level.SEVERE, null, ex);
        }
        return str;
    }
}
