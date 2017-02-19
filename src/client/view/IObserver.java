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
public interface IObserver {
    void updateId();
    void updateFileList();
    void updateFileContent();
    void updateSavingState();
    void updateRangesState();
    void updateUnlockingState();
    void updateRegistrationStatus();
    void updateLoginStatus();

    void invalidLogin();
    void invalidRegistration();
    void invalidRange();
}
