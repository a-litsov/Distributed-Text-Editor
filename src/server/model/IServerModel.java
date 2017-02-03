/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.model;

import java.io.File;
import server.presenter.IServerPresenter;

/**
 *
 * @author al1as
 */
public interface IServerModel {
    public void addToTable(String filename, FileElement element);
    public void delFromTable(String filename, FileElement element);
    public String putFileElement(String filename, FileElement element);
    public void extractFileElement(String filename, FileElement element);
    public String GetFromDB(String Name);
    public void SendToDB(String username, String filename);
    public boolean addName(String username);
    public String listFilesForFolder(final File folder);
    public String getFileContent(String FileName);
    public void removeName(String username);
    public void Save(String LockedContent, String filename, FileElement element);
    public String getMessage();
    public void sendMessage(String message);
    public void refresh();
    public void addPresenter(IServerPresenter p);
}
