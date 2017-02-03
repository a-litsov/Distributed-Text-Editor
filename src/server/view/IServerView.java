/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.view;

/**
 *
 * @author al1as
 */
public interface IServerView {
    public String[] getRanges();
    public String getName();
    public String getString();
    
    public void sendMes(String mes);
    public void sendPrevFilename(String prevFilename);
    public void SendFileContent(String content);
}
