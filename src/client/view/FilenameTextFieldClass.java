/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client.view;

import client.model.BClientModel;
import client.model.IClientModel;
import client.controller.BClientController;
import client.controller.IClientController;
import javax.swing.JTextField;

/**
 *
 * @author al1as
 */
public class FilenameTextFieldClass extends JTextField implements IObserver{

    @Override
    public void invalidUsername() {
    }

    @Override
    public void invalidRange() {
    }

    public FilenameTextFieldClass() {
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
        this.setEnabled(true);
    }

    @Override
    public void updateFileContent() {
        this.setEnabled(true);
    }

    @Override
    public void updateSavingState() {
    }

    @Override
    public void updateRangesState() {
        this.setEnabled(false);
    }
    
}
