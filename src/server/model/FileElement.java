/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.model;

import java.util.UUID;

/**
 *
 * @author al1as
 */
public class FileElement {
    UUID Id;
    Range range;
    String username;
    
    
    public FileElement(UUID Id, String username, int start, int end)
    {
        this.Id = Id;
        this.username = username;
        range = new Range(start, end);
    }
    public FileElement(UUID Id, String username, Range range)
    {
        this.Id = Id;
        this.username = username;
        this.range = range;
    }
    public String getUsername() {
        return username;
    }
    public UUID getUUID()
    {
        return Id;
    }
    public void setUUID(UUID Id)
    {
        this.Id = Id;
    }
    public int getStart()
    {
        return range.getStart();
    }
    public void setStart(int start)
    {
        range.setStart(start);
    }
    public int getEnd()
    {
        return range.getEnd();
    }
    public void setEnd(int end)
    {
        range.setEnd(end); 
    }
}
