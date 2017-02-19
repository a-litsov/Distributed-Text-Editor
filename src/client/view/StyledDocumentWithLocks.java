/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client.view;

import client.controller.BClientController;
import client.controller.IClientController;
import java.awt.Color;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.DocumentFilter;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.Utilities;

import server.model.Range;

/**
 *
 * @author al1as
 */
public class StyledDocumentWithLocks extends DefaultStyledDocument
{
    SimpleAttributeSet lockAttributeSet = new SimpleAttributeSet();
    boolean isActivated = false;
    
    public StyledDocumentWithLocks() 
    {
        StyleConstants.setForeground(lockAttributeSet, Color.GRAY);
    }
    
    @Override
    public void remove(final int offset, final int length) throws BadLocationException 
    {
        IClientController controller = BClientController.build();
        if (offset >= controller.getStartSymbolRange() &&  offset + length <= controller.getEndSymbolRange()+1) {
                controller.incEndLock(-length);
                String deletedContent = this.getText(offset, length);
                
                for(int i = 0; i < length; i++) {
                    if(deletedContent.charAt(i) == '\n')
                        controller.incEndLineChanging(-1);
                }
                super.remove(offset, length);
        }
    }
    
    @Override
    public void insertString(int offset, String str, AttributeSet a) throws BadLocationException {
        IClientController controller = BClientController.build();
        int end = controller.getEndSymbolRange();
        if (end == this.getLength() - 1) {
            end++;
        }
        int length = str.length();
        if (offset >= controller.getStartSymbolRange() && offset <= end) {
            controller.incEndLock(length);
            for (int i = 0; i < length; i++) 
                if (str.charAt(i) == '\n') 
                    controller.incEndLineChanging(1);
            super.insertString(offset, str, a);
        }
    }
    
    // loads document with styles
    public void loadFileContent(ArrayList<TextFragment> fragments) throws BadLocationException 
    {
        int start = 0;
        int end = 0;
        super.remove(0, this.getLength());
        for(int i = 0; i < fragments.size(); i++) {
            TextFragment curFragment = fragments.get(i);
            if(curFragment.isLocked) {
                end = start + curFragment.text.length() - 1;
                super.insertString(start, curFragment.text, lockAttributeSet);
                start = end + 1;
            } else {
                end = start + curFragment.text.length() - 1;
                super.insertString(start, curFragment.text, null);
                start = end + 1; 
            }
        }
    }
}
