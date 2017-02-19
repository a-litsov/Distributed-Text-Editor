/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client.model;

import client.view.IObserver;
import client.view.TextFragment;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import server.model.Range;
/**
 *
 * @author al1as
 */

class ClientModel implements IClientModel{   
    private int port = 3125;
    private InetAddress ip = null;
    private Socket cs;
    private InputStream is;
    private OutputStream os;
    private DataInputStream dis;
    private DataOutputStream dos;
    private String id, username, fileList = "", fileContent = "", filename = "",
        prevFilename = "";
    private int startLine, endLine;
    private ArrayList<TextFragment> contentFragments = new ArrayList<TextFragment>();
    private ArrayList<Range> symbolLocks = new ArrayList<Range>();
    private int endLineChanging = 0;
    private boolean f = true;
    
    ArrayList<IObserver> observers = new ArrayList<>();
    
    public ClientModel() {
        Range tmp = new Range(0, 0);
        symbolLocks.add(tmp);
    }
    
    @Override
    public void addObserver(IObserver o)
    {
        observers.add(o);
    }
    
    @Override
    public String getId() {
        return id;
    }
    
    @Override
    public String getPrevFilename() {
        return prevFilename;
    }
        
    @Override
    public String getFileList() {
        return fileList;
    }
    
    @Override
    public String getFileContent() {
        return fileContent;
    }
    
    @Override
    public ArrayList<TextFragment> getTextFragments() {
        return contentFragments;
    }
    
    @Override
    public int getStartSymbolRange() {
        return symbolLocks.get(0).getStart();
    }

    @Override
    public int getEndSymbolRange() {
        return symbolLocks.get(0).getEnd();
    }
    
    @Override
    public String getUsername() {
        return username;
    }
    
    @Override
    public void incEndLineChanging(int value) {
        endLineChanging += value;
    }
    
    @Override
    public void loginUser(String login, String pass) {
        try {
            dos.writeUTF("User login");
            dos.writeUTF(login);
            dos.writeUTF(pass);
            username = login;
        } catch (IOException ex) {
            Logger.getLogger(ClientModel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void registerUser(String login, String pass) {
        try {
            dos.writeUTF("User registration");
            dos.writeUTF(login);
            dos.writeUTF(pass);
            username = login;
        } catch (IOException ex) {
            Logger.getLogger(ClientModel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void sendFileListRequest() {
        try {
            dos.writeUTF("Get list of files.");
        } catch (IOException ex) {
            Logger.getLogger(ClientModel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void sendFileContentRequest(String filename) {
        try {
            dos.writeUTF("Get file content");
            dos.writeUTF(filename);
            this.filename = filename;
        } catch (IOException ex) {
            Logger.getLogger(ClientModel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void sendSaveRequest(String content) {
        try {
            dos.writeUTF("File saving");
            dos.writeUTF(content);
            String endLineChange = String.valueOf(endLineChanging);
            dos.writeUTF(endLineChange);
        } catch (IOException ex) {
            Logger.getLogger(ClientModel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void sendRangesAndLock(int startSymbol, int endSymbol) {
        try {
            this.startLine = caretPositionToLineNumber(startSymbol, 0);
            this.endLine = caretPositionToLineNumber(endSymbol, 0);
            symbolLocks.clear();
            symbolLocks.add(new Range(startSymbol, endSymbol));
            //Отправляем выбранный диапазон
            dos.writeUTF("Ranges sending");
            dos.writeInt(startLine);
            dos.writeInt(endLine);
        } catch (IOException ex) {
            Logger.getLogger(ClientModel.class.getName()).log(Level.SEVERE, null, ex);

        }
    }

    @Override
    public void incEndLock(int value) {
        // extendable: if we need any lock, just pass it number by parameter
        Range current = symbolLocks.get(0);
        current.setEnd(current.getEnd() + value);
    }

    @Override
    public void sendUnlocking() {
        try {
            dos.writeUTF("Unlocking");
        } catch (IOException ex) {
            Logger.getLogger(ClientModel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void sendStop() {
        try {
            dos.writeUTF("end");
        } catch (IOException ex) {
            Logger.getLogger(ClientModel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void refreshFileContent() {
        sendFileContentRequest(filename);
    }
    
    
    public void connect() {
        try {
            ip = InetAddress.getLocalHost();
        } catch (UnknownHostException ex) {
            Logger.getLogger(ClientModel.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            cs = new Socket(ip, port);
            System.out.println("Client start \n");
            is = cs.getInputStream();
            os = cs.getOutputStream();
            
            dis = new DataInputStream(is);
            dos = new DataOutputStream(os);
            
            // Starting connection via getting id from server
            id = dis.readUTF();
            updateIdObs(); 
            System.out.println("Connected and ID loaded:" + id);
            
            // Creating server message event handler
            new Thread()
            {
                @Override
                public void run() { 
                    while(f) {
                        try {
                            String s = dis.readUTF();          
                            switch(s) {
                                case "Server stopped":
                                    f = false;
                                    sendStop();
                                    cs.close();
                                    System.exit(1);
                                    break;
								case "Login successful":
                                    updateLoginStatusObs();
                                    break;
								case "Error! Failed login!":
									invalidLogin();
									break;
                                case "Registration successful":
                                    updateRegistrationStatusObs();
                                    break;
                                case "Error! Failed registration!":
                                    invalidRegistration();
                                    break;
                                case "File list sending":
                                    getFileListFromServer();
                                    updateFileListObs();
                                    break;
                                case "File content with ranges sending.":
                                    loadFileContent();
                                    updateFileContentObs();
                                    break;
                                case "File saved successfully":
                                    updateSavingStateObs();
                                    clearChanging();
                                    break;
                                case "Ranges was set successfully":
                                    loadFileWithMyLocks();
                                    updateRangesStateObs();
                                    break;
                                case "Error with setting ranges":
                                    invalidRange();
                                    break;
                                case "Successfully unlocked!":
                                    updateUnlockingStateObs();
                                    clearChanging();
                                    break;
                                default:
                                    System.out.println("Unexpected server message:" + s);
                            }
                        } catch (IOException ex) {
                            Logger.getLogger(ClientModel.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }.start();
           
        } catch (IOException ex) {
            Logger.getLogger(ClientModel.class.getName()).log(Level.SEVERE, null, ex);   
        } 
    }
    
    private void getPrevFilenameFromServer() {
        try {
            prevFilename = dis.readUTF();
        } catch (IOException ex) {
               Logger.getLogger(ClientModel.class.getName()).log(Level.SEVERE, null, ex); 
        }
    }
    
    private void getFileListFromServer() {
        try {
            fileList = dis.readUTF();            
        } catch (IOException ex) {
               Logger.getLogger(ClientModel.class.getName()).log(Level.SEVERE, null, ex); 
        }
    }
    
    private void loadFileWithMyLocks() {
        contentFragments.clear();
        Range tmp = new Range(startLine, endLine);
        ArrayList<Range> lineRanges = new ArrayList<Range>();
        lineRanges.add(tmp);
        System.out.println("loadFileContent begins");
        if (lineRanges.size() > 0) {
            int start, end;
            String tmpFileContent = fileContent + "\n";
            ArrayList<Range> symbolRanges = getSymbolRanges(tmpFileContent, lineRanges);
            if (symbolRanges != null) {
                // adding first locked part
                end = symbolRanges.get(0).getStart();
                TextFragment tmpFragment = new TextFragment();
                tmpFragment.setText(tmpFileContent.substring(0, end));
                tmpFragment.setLocked(true);
                contentFragments.add(tmpFragment);
                // adding unlocked parts
                for (int i = 0; i < symbolRanges.size(); i++) {
                    start = symbolRanges.get(i).getStart();
                    end = symbolRanges.get(i).getEnd() + 1;
                    tmpFragment = new TextFragment();
                    tmpFragment.setText(tmpFileContent.substring(start, end));
                    tmpFragment.setLocked(false);
                    contentFragments.add(tmpFragment);
                    if (i + 1 < symbolRanges.size()) {
                        start = end;
                        end = symbolRanges.get(i+1).getStart();
                        tmpFragment = new TextFragment();
                        tmpFragment.setText(tmpFileContent.substring(start, end));
                        tmpFragment.setLocked(true);
                        contentFragments.add(tmpFragment);
                    } else {
                        // check if file end
                        tmpFragment = new TextFragment();
                        tmpFragment.setText(fileContent.substring(end));
                        tmpFragment.setLocked(true);
                        contentFragments.add(tmpFragment);
                    }
                }
            } else {
                System.out.println("symbolRanges is null, error");
            }
        } else {
            TextFragment tmpFragment = new TextFragment();
            tmpFragment.setText(fileContent);
            tmpFragment.setLocked(true);
            contentFragments.add(tmpFragment);
            System.out.println("lineRanges is empty");
        }
        System.out.println("loadFileContent completed");       
    }
    
    private void loadFileContent() {
        contentFragments.clear();
        ArrayList<Range> lineRanges = new ArrayList<Range>();
    
        try {
            fileContent = dis.readUTF();
            int size = dis.readInt();
            for(int i = 0; i < size; i++) {
                int start = dis.readInt();
                int end = dis.readInt();
                Range tmpRange = new Range(start, end);
                lineRanges.add(tmpRange);
            }
        } catch (IOException ex) {
            Logger.getLogger(ClientModel.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        System.out.println("loadFileContent begins");
        if (lineRanges.size() > 0) {
            int start, end;
            String tmpFileContent = fileContent + "\n";
            ArrayList<Range> symbolRanges = getSymbolRanges(tmpFileContent, lineRanges);
            if (symbolRanges != null) {
                // adding first unlocked part
                end = symbolRanges.get(0).getStart();
                TextFragment tmpFragment = new TextFragment();
                tmpFragment.setText(tmpFileContent.substring(0, end));
                tmpFragment.setLocked(false);
                contentFragments.add(tmpFragment);
                // adding locked parts
                for (int i = 0; i < symbolRanges.size(); i++) {
                    start = symbolRanges.get(i).getStart();
                    end = symbolRanges.get(i).getEnd()+1;
                    tmpFragment = new TextFragment();
                    tmpFragment.setText(tmpFileContent.substring(start, end));
                    tmpFragment.setLocked(true);
                    contentFragments.add(tmpFragment);
                    if (i + 1 < symbolRanges.size()) {
                        start = end;
                        end = symbolRanges.get(i+1).getStart();
                        tmpFragment = new TextFragment();
                        tmpFragment.setText(tmpFileContent.substring(start, end));
                        tmpFragment.setLocked(false);
                        contentFragments.add(tmpFragment);
                    } else {
                        // check if file end
                        tmpFragment = new TextFragment();
                        tmpFragment.setText(fileContent.substring(end));
                        tmpFragment.setLocked(false);
                        contentFragments.add(tmpFragment);
                    }
                }
            } else {
                System.out.println("symbolRanges is null, error");
            }
        } else {
            TextFragment tmpFragment = new TextFragment();
            tmpFragment.setText(fileContent);
            tmpFragment.setLocked(false);
            contentFragments.add(tmpFragment);
            System.out.println("lineRanges is empty");
        }
        System.out.println("loadFileContent completed");
    }
    
    private int caretPositionToLineNumber(int caretPosition, int startPosition) {
        int i;
        int lineNumber = (caretPosition == 0) ? 1 : 0;
        String fileContent = this.fileContent + '\n';
        for(i = startPosition; i < Integer.min(caretPosition, fileContent.length()); i = fileContent.indexOf('\n', i+1))
            lineNumber++;
        System.out.println("Current line number: " + lineNumber);
        return lineNumber;
    }
    
    private ArrayList<Range> getSymbolRanges(String content, ArrayList<Range> lineRanges) {
        if (lineRanges.size() > 0 && content.length() != 0) {
            ArrayList<Range> symbolRanges = new ArrayList<Range>();
            Range tmp;
            int currentStartPosition = 0, currentEndPosition = -1, i;
            int lineNumber = 0;
            for (int k = 0; k < lineRanges.size(); k++) {
                // Finding start of range
                for (i = currentEndPosition; lineNumber < lineRanges.get(k).getStart() - 1; i = content.indexOf("\n", i + 1), lineNumber++);
                currentStartPosition = i + 1;
                // Finding end of range
                for (i = currentStartPosition - 1; lineNumber < lineRanges.get(k).getEnd(); i = content.indexOf("\n", i + 1), lineNumber++);
                currentEndPosition = i >= content.length() - 1 ? content.length() - 2 : i;
                tmp = new Range(currentStartPosition, currentEndPosition);
                symbolRanges.add(tmp);
            }
            return symbolRanges;
        } else {
            System.out.println("Something gone wrong in getSymbolRanges method");
            return null;
        }
    }
    
    private void clearChanging() {
        endLineChanging = 0;
        symbolLocks.clear();
    }

    
    private void invalidLogin() {
        for (IObserver o: observers) {
            o.invalidLogin();
        }
    }
    
    private void invalidRegistration() {
        for (IObserver o: observers) {
            o.invalidRegistration();
        }
    }
    
    private void invalidRange() {
        for (IObserver o: observers) {
            o.invalidRange();
        }
    }
    
    private void updateIdObs() {
        for (IObserver o: observers) { 
            o.updateId();
        }
    }
    
    private void updateFileListObs() {
        for (IObserver o: observers) { 
            o.updateFileList();
        }
    }
    
    private void updateFileContentObs() {
        for (IObserver o: observers) { 
            o.updateFileContent();
        }
    }
    
    private void updateSavingStateObs() {
        for (IObserver o: observers) { 
            o.updateSavingState();
        }
    }    
    
    private void updateRangesStateObs() {
        for (IObserver o: observers) { 
            o.updateRangesState();
        }
    }  
    
    private void updateUnlockingStateObs() {
        for (IObserver o : observers) {
            o.updateUnlockingState();
        }
    }

    private void updateRegistrationStatusObs() {
        for (IObserver o : observers) {
            o.updateRegistrationStatus();
        }
    }
	
	private void updateLoginStatusObs() {
        for (IObserver o: observers) { 
            o.updateLoginStatus();
        }
    }
}