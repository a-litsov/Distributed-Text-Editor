/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client.view;

/**
 *
 * @author evgen
 */
public interface IObserver {
    void updateId();
    void updatePrevFilename();
    void updateFileList();
    void updateFileContent();
    void updateSavingState();
    void updateRangesState();
    void invalidUsername();
    void invalidRange();
}
