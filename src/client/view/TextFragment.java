/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client.view;

/**
 *
 * @author al1as
 */
public class TextFragment {
    private String text;
    private boolean isLocked = false;
    
    public String getText() {
        return text;
    }
    
    public boolean isLocked() {
        return isLocked;
    }
    
    public void setText(String text) {
        this.text = text;
    }
    
    public void setLocked(boolean isLocked) {
        this.isLocked = isLocked;
    }
}
