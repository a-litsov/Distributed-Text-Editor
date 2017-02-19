/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.model;

/**
 *
 * @author al1as
 */
public class Range {
    private int start;
    private int end;
    public Range(int start, int end)
    {
        this.start = start;
        this.end = end;
    }
    public int getStart()
    {
        return start;
    }
    public void setStart(int start)
    {
        this.start = start;
    }
    public int getEnd()
    {
        return end;
    }
    public void setEnd(int end)
    {
        this.end = end;
    }
}
