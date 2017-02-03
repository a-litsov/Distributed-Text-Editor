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
public class StatusLabelClass extends JLabel implements IObserver {

    @Override
    public void invalidUsername() {
        status = "Please, choose another username";
        this.setText(status);
    }

    @Override
    public void invalidRange() {
        status = "Invalid range, please, choose another range";
        this.setText(status);  
    }
    private String status;
    public StatusLabelClass() {
        IClientModel clientModel = BClientModel.build();
        clientModel.addObserver(this);
    }

    @Override
    public void updateId() {
        status = "Id successfully loaded";
        this.setText(status);
    }

    @Override
    public void updatePrevFilename() {
        status = "Logged in, previous filename successfully loaded";
        this.setText(status);
    }

    @Override
    public void updateFileList() {
        status = "List of files successfully loaded";
        this.setText(status);
    }

    @Override
    public void updateFileContent() {
        status = "File content loaded. Ready to lock";
        this.setText(status);
    }

    @Override
    public void updateSavingState() {
    }

    @Override
    public void updateRangesState() {
        status = "Ranges set successfully. Lock activated";
        this.setText(status);
    }

}
