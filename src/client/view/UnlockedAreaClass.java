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
public class UnlockedAreaClass extends JTextArea implements IObserver {

    @Override
    public void invalidUsername() {
    }

    @Override
    public void invalidRange() {
    }
    String fileList;
    String content;
    String unlockedPart;
    public UnlockedAreaClass() {
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
        IClientModel clientModel = BClientModel.build();
        fileList = clientModel.getFileList();
        this.setText(fileList);
        this.setEditable(false);
    }

    @Override
    public void updateFileContent() {
        IClientModel clientModel = BClientModel.build();
        content = clientModel.getFileContent();
        this.setText(content);
        this.setEditable(false);
    }

    @Override
    public void updateSavingState() {
    }

    @Override
    public void updateRangesState() {
        IClientModel clientModel = BClientModel.build();
        unlockedPart = clientModel.getUnlockedPart();
        this.setText(unlockedPart);
        this.setEditable(true);
    }

}
