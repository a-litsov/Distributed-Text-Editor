/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client.view;

import client.model.BClientModel;
import client.model.IClientModel;
import javax.swing.JTextArea;

/**
 *
 * @author al1as
 */
public class LockedArea1Class extends JTextArea implements IObserver {

    @Override
    public void invalidUsername() {
    }

    @Override
    public void invalidRange() {
    }
    String lockedPart1;
    public LockedArea1Class() {
        IClientModel clientModel = BClientModel.build();
        clientModel.addObserver(this);
    }

    @Override
    public void updateId() {
    }

    @Override
    public void updatePrevFilename() {
    }

    @Override
    public void updateFileList() {
    }

    @Override
    public void updateFileContent() {
        this.setText("");
    }

    @Override
    public void updateSavingState() {
    }

    @Override
    public void updateRangesState() {
        IClientModel clientModel = BClientModel.build();
        lockedPart1 = clientModel.getLockedPart1();
        this.setText(lockedPart1);
    }
    
}
