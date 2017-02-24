/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.view;

import java.util.ArrayList;
import server.model.Range;

/**
 *
 * @author al1as
 */
public interface IServerView {
    public int[] getRanges();
    public String getString();
    
    public void sendString(String str);
    public void SendFileContent(String content);
    public void SendFileContent(String content, ArrayList<Range> ranges);
}
