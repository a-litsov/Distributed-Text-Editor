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
public class IdLabelClass  extends JLabel implements IObserver {

    @Override
    public void invalidUsername() {
    }

    @Override
    public void invalidRange() {
    }

    public IdLabelClass() {
        IClientModel clientModel = BClientModel.build();
        clientModel.addObserver(this);
    }

    @Override
    public void updateId() {
        IClientModel clientModel = BClientModel.build();
        this.setText(clientModel.getId());
    }

    @Override
    public void updatePrevFilename() {
    }

    @Override
    public void updateFileList() {    }

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
