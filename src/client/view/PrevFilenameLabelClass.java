/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client.view;

import client.model.BClientModel;
import client.model.IClientModel;
import javax.swing.JLabel;

/**
 *
 * @author al1as
 */
public class PrevFilenameLabelClass extends JLabel implements IObserver {
    
    private String prevFilename;
    
    @Override
    public void invalidUsername() {
        prevFilename = "Invalid username";
        this.setText(prevFilename);
    }

    @Override
    public void invalidRange() {
    }
    
    public PrevFilenameLabelClass() {
        IClientModel clientModel = BClientModel.build();
        clientModel.addObserver(this);
    }

    @Override
    public void updateId() {
    }

    @Override
    public void updatePrevFilename() {
        IClientModel clientModel = BClientModel.build();
        prevFilename = clientModel.getPrevFilename();
        this.setText(prevFilename);
    }

    @Override
    public void updateFileList() {
    }

    @Override
    public void updateFileContent() {
    }

    @Override
    public void updateSavingState() {
    }

    @Override
    public void updateRangesState() {
    }
    
}
