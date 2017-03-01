/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import server.model.BServerModel;
import server.model.IServerModel;

import server.presenter.BServerPresenter;
import server.presenter.IServerPresenter;
import server.view.BServerView;
import server.view.IServerView;

/**
 *
 * @author al1as
 */
public class ServerThread extends Thread{
    JTable table;
    boolean f = true;
    int port = 3125;
    InetAddress ip = null;
    ServerSocket ss;   
    static IServerModel model;
    
    synchronized public void stopServer()
    {
        f = false;
        model.sendMessage("end");
        Stop();
    }
    
    private void Stop() {
     try {
          Socket s = new Socket(ip, port);
          s.getOutputStream().flush();
          s.close();
     } catch (IOException e) {
     }
    }
    
    public ServerThread(JTable table)
    {
        this.table = table;
        try {
            ip = InetAddress.getLocalHost();
        } catch (UnknownHostException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            ss = new ServerSocket(port, 0, ip);
        } catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Server start\n");
    }

    @Override
    public void run() {
        model = BServerModel.build((DefaultTableModel)table.getModel());
        while(f == true)
        {
            try {
                Socket cs = ss.accept();
                IServerView view = BServerView.build(cs);
                IServerPresenter presenter = BServerPresenter.build(model, view);
                System.out.println("connect" + presenter.getUUID().toString());
            } catch (IOException ex) {
                Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
