/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.view;

import java.net.Socket;

/**
 *
 * @author al1as
 */
public class BServerView {
    static public IServerView build(Socket s)
    {
        return new ServerView(s);
    }    
}
