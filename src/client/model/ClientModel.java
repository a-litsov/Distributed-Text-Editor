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
 * @author evgen
 */
public class ClientModel implements IClientModel{   
    int port = 3125;
    InetAddress ip = null;
    Socket cs;
    InputStream is;
    OutputStream os;
    DataInputStream dis;
    DataOutputStream dos;
    String id;
    String username;
    String fileList = "";
    String fileContent = "";
    String filename = "";
    String short_filename = "";
    String prevFilename = "";
    String start, end;
    String lockedPart1, unlockedPart, lockedPart2;
    ArrayList<TextFragment> contentFragments = new ArrayList<TextFragment>();
    ArrayList<Range> symbolLocks = new ArrayList<Range>();
    int endLineChanging = 0;
    String message;//Строка, содержащая сообщение
    boolean f = true;
    boolean flag = false;//Указывает на то, была передача данных, или нет
    
    ArrayList<IObserver> observers = new ArrayList<>();
    
    public ClientModel() {
        Range tmp = new Range(0, 0);
        symbolLocks.add(tmp);
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
    public String getLockedPart1() {
        return lockedPart1;
    }
    
    @Override
    public String getUnlockedPart() {
        return unlockedPart;
    }
    
    @Override
    public String getLockedPart2() {
        return lockedPart2;
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
            
            //Получаем идентификатор
            id = dis.readUTF();
            updateIdObs(); // Обновляем view, подписанные на id
            System.out.println("Connected and ID loaded:" + id);
            
            //Создаем "обработчик" завершения сервера
            new Thread()
            {

                @Override
                public void run() { 
                    while(f)
                    {
                        try {
                            String s;
                            try{
                                s = dis.readUTF();
                            } catch(EOFException ex1) {
                                break;
                            }
                            if(s.equals("Server stopped"))
                            {
                                f = false;
                                sendStop();
                                cs.close();
                                System.exit(1);
                            }
                            
                            
                            if(s.equals("Ok!Previous filename sending.")) {
                                getPrevFilenameFromServer();
                                updatePrevFilenameObs();    
                            }    

                            if(s.equals("Error! Failed filename sending!")) {
                                invalidUsername();
                            }
                            
                            if(s.equals("Registration successful")) {
                                username = dis.readUTF();
                                updateRegistrationStatusObs();    
                            }
                            
                            if(s.equals("Error! Failed registration!")) {
                                invalidRegistration();
                            }
                            
                            
                            if(s.equals("File list sending")) {
                                getFileListFromServer();
                                updateFileListObs();
                            }
                                 
                            if(s.equals("File content sending.")) {
                                getFileContentFromServer();
                                updateFileContentObs();  
                            }  
                            
                            if (s.equals("File content with ranges sending.")) {
                                loadFileContent();
                                updateFileContentObs();
                            }
                            
                            
                            if(s.equals("File saved successfully")) {
                                updateSavingStateObs();
                            }
                              
                            if(s.equals("Ranges was set successfully")) {
                                loadFileSeparate();
                                updateRangesStateObs();   
                            }
                            if(s.equals("Error with setting ranges")) {
                                invalidRange();
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
    
    @Override
    public void loginUser(String name, String pass) {
        try {
            dos.writeUTF("User login");
            dos.writeUTF(name);
            dos.writeUTF(pass);
            username = name;
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
    
    private void loadFileSeparate() {
        int start_ = Integer.parseInt(start);
        int end_ = Integer.parseInt(end);
        String[] TextData = new String[3];
        String[] ArrayContent = fileContent.split("\n");
        for (int i = 0; i < 3; i++) {
            TextData[i] = "";
        }
        int switcher = 0;
        for (int i = 0; i < ArrayContent.length; i++) {
            if (i == start_ - 1) {
                switcher++;
            }
            if (i == end_) {
                switcher++;
            }
            TextData[switcher] += ArrayContent[i] + "\n";
        }
        if (!TextData[2].isEmpty())
            TextData[2] = TextData[2].substring(0, TextData[2].length() - 1);
        else
            TextData[1] = TextData[1].substring(0, TextData[1].length() - 1);
        lockedPart1 = TextData[0];
        unlockedPart = TextData[1];
        lockedPart2 = TextData[2];
        try {
            // new part, all above to delete
            loadFileWithMyLocks();
        } catch (BadLocationException ex) {
            Logger.getLogger(ClientModel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private ArrayList<Range> getSymbolRanges(String content, ArrayList<Range> lineRanges) {
        if (lineRanges.size() > 0 && content.length() != 0) {
            ArrayList<Range> symbolRanges = new ArrayList<Range>();
            Range tmp;
            int currentStartPosition = 0, currentEndPosition, i;
            int lineNumber = 0;
            for (int k = 0; k < lineRanges.size(); k++) {
                for (i = 0; lineNumber < lineRanges.get(k).getStart(); i = content.indexOf("\n", i + 1), lineNumber++) {
                    if (lineNumber == 0)
                        currentStartPosition = 0;
                    else
                        currentStartPosition = i + 1;
                }
                String tmpContent = content + "\n";
                for (i = currentStartPosition; lineNumber <= lineRanges.get(k).getEnd(); i = tmpContent.indexOf("\n", i + 1), lineNumber++);
                currentEndPosition = i - 1;
                tmp = new Range(currentStartPosition, currentEndPosition);
                symbolRanges.add(tmp);
            }
            return symbolRanges;
        } else {
            System.out.println("Something gone wrong in getSymbolRanges method");
            return null;
        }
    }

    private void loadFileWithMyLocks() throws BadLocationException {
        contentFragments.clear();
        int start_ = Integer.parseInt(start);
        int end_ = Integer.parseInt(end);
        Range tmp = new Range(start_, end_);
        ArrayList<Range> lineRanges = new ArrayList<Range>();
        lineRanges.add(tmp);
        System.out.println("loadFileContent begins");
        if (lineRanges.size() > 0) {
            int start, end;
            ArrayList<Range> symbolRanges = getSymbolRanges(fileContent, lineRanges);
            if (symbolRanges != null) {
                // adding first locked part
                end = symbolRanges.get(0).getStart();
                TextFragment tmpFragment = new TextFragment();
                tmpFragment.text = fileContent.substring(0, end);
                tmpFragment.isLocked = true;
                contentFragments.add(tmpFragment);
                // adding unlocked parts
                for (int i = 0; i < symbolRanges.size(); i++) {
                    start = symbolRanges.get(i).getStart();
                    end = symbolRanges.get(i).getEnd() + 1;
                    tmpFragment = new TextFragment();
                    tmpFragment.text = fileContent.substring(start, end);
                    tmpFragment.isLocked = false;
                    contentFragments.add(tmpFragment);
                    if (i + 1 < symbolRanges.size()) {
                        start = end;
                        end = symbolRanges.get(i).getStart() + 1;
                        tmpFragment = new TextFragment();
                        tmpFragment.text = fileContent.substring(start, end);
                        tmpFragment.isLocked = true;
                        contentFragments.add(tmpFragment);
                    } else {
                        // check if file end
                        tmpFragment = new TextFragment();
                        tmpFragment.text = fileContent.substring(end, fileContent.length());
                        tmpFragment.isLocked = true;
                        contentFragments.add(tmpFragment);
                    }
                }
            } else {
                System.out.println("symbolRanges is null, error");
            }
        } else {
            TextFragment tmpFragment = new TextFragment();
            tmpFragment.text = fileContent;
            tmpFragment.isLocked = true;
            contentFragments.add(tmpFragment);
            System.out.println("lineRanges is empty");
        }
        System.out.println("loadFileContent completed");       
    }
    
    // loads document with styles
    public void loadFileContent() {
        contentFragments.clear();
//        int start_ = Integer.parseInt(start);
//        int end_ = Integer.parseInt(end);
//        Range tmp = new Range(start_, end_);
//        ArrayList<Range> lineRanges = new ArrayList<Range>();
//        lineRanges.add(tmp);
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
            ArrayList<Range> symbolRanges = getSymbolRanges(fileContent, lineRanges);
            if (symbolRanges != null) {
                // adding first unlocked part
                end = symbolRanges.get(0).getStart();
                TextFragment tmpFragment = new TextFragment();
                tmpFragment.text = fileContent.substring(0, end);
                tmpFragment.isLocked = false;
                contentFragments.add(tmpFragment);
                // adding locked parts
                for (int i = 0; i < symbolRanges.size(); i++) {
                    start = symbolRanges.get(i).getStart();
                    end = symbolRanges.get(i).getEnd()+1;
                    tmpFragment = new TextFragment();
                    tmpFragment.text = fileContent.substring(start, end);
                    tmpFragment.isLocked = true;
                    contentFragments.add(tmpFragment);
                    if (i + 1 < symbolRanges.size()) {
                        start = end;
                        end = symbolRanges.get(i).getStart()+1;
                        tmpFragment = new TextFragment();
                        tmpFragment.text = fileContent.substring(start, end);
                        tmpFragment.isLocked = false;
                        contentFragments.add(tmpFragment);
                    } else {
                        // check if file end
                        tmpFragment = new TextFragment();
                        tmpFragment.text = fileContent.substring(end, fileContent.length());
                        tmpFragment.isLocked = false;
                        contentFragments.add(tmpFragment);
                    }
                }
            } else {
                System.out.println("symbolRanges is null, error");
            }
        } else {
            TextFragment tmpFragment = new TextFragment();
            tmpFragment.text = fileContent;
            tmpFragment.isLocked = false;
            contentFragments.add(tmpFragment);
            System.out.println("lineRanges is empty");
        }
        System.out.println("loadFileContent completed");
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
        } catch (IOException ex) {
            Logger.getLogger(ClientModel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private static int countLines(String str){
        String[] lines = str.split("\r\n|\r|\n");
        return  lines.length;
    }
    
    boolean tryParseInt(String value) {  
        try {  
            Integer.parseInt(value);  
            return true;  
        } catch (NumberFormatException e) {  
            return false;  
        }  
    }
    // Нумерация строк с единицы, ноль в стартовом диапазоне недопустим
    boolean testRange() {
        boolean parsed = true;
        int start_ = -1;
        int end_ = -1;
        if(tryParseInt(start) && tryParseInt(end)) {
            start_ = Integer.parseInt(start);
            end_ = Integer.parseInt(end);
        }

        int linesCount = countLines(fileContent);
        if(start_ <= 0 || end_ <= 0 || start_ > end_ || start_ > linesCount || end_ > linesCount ) {
            return false;
        } 
        return true;
    }
    
    @Override
    public void sendRangesAndLock(String startLine, String endLine, int startSymbol, int endSymbol) {
        try {
            this.start = startLine;
            this.end = endLine;
            if (!testRange()) {
                invalidRange();
                return;
            }        
            Range tmp = new Range(startSymbol, endSymbol);
            symbolLocks.clear();
            symbolLocks.add(tmp);
            //Отправляем выбранный диапазон
            dos.writeUTF("Ranges sending");
            dos.writeUTF(start);
            dos.writeUTF(end);
        } catch (IOException ex) {
            Logger.getLogger(ClientModel.class.getName()).log(Level.SEVERE, null, ex);

        }
    }
    
    @Override
    public void incEndLock(int value) {
        // extendable: if we need any lock, just pass it number by parameter
        Range current = symbolLocks.get(0);
        current.setEnd(current.getEnd()+value);
    }
    
    @Override
    public void sendUnlocking() {
        try {
            dos.writeUTF("Unlocking");
        }
        catch (IOException ex) {
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
    
    private void getFileListFromServer() {
        try {
            fileList = dis.readUTF();            
        } catch (IOException ex) {
               Logger.getLogger(ClientModel.class.getName()).log(Level.SEVERE, null, ex); 
        }
    }
    
    private void getFileContentFromServer() {
        try {
            // to delete
            fileContent = dis.readUTF();
            // part with fragments потом добавить диапазоны локов от серва (сейчас они и не приходят)
            TextFragment tmpFragment = new TextFragment();
            tmpFragment.text = fileContent;
            tmpFragment.isLocked = false;
            contentFragments.clear();
            contentFragments.add(tmpFragment);
            // to delete
            lockedPart1 = ""; lockedPart2 = "";
        } catch (IOException ex) {
            Logger.getLogger(ClientModel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    void invalidUsername() {
        for (IObserver o: observers) {
            o.invalidUsername();
        }
    }
    
    void invalidRegistration() {
        for (IObserver o: observers) {
            o.invalidRegistration();
        }
    }
    
    void invalidRange() {
        for (IObserver o: observers) {
            o.invalidRange();
        }
    }
    
    void updateIdObs() {
        for (IObserver o: observers) { 
            o.updateId();
        }
    }
    
    void updatePrevFilenameObs() {
        for (IObserver o: observers) { 
            o.updatePrevFilename();
        }
    }
    
    void updateFileListObs() {
        for (IObserver o: observers) { 
            o.updateFileList();
        }
    }
    
    void updateFileContentObs() {
        for (IObserver o: observers) { 
            o.updateFileContent();
        }
    }
    
    void updateSavingStateObs() {
        for (IObserver o: observers) { 
            o.updateSavingState();
        }
    }    
    
    void updateRangesStateObs() {
        for (IObserver o: observers) { 
            o.updateRangesState();
        }
    }            
    @Override
    public void addObserver(IObserver o)
    {
        observers.add(o);
    }
    
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
    public void incEndLineChanging(int value) {
        endLineChanging += value;
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
    public void updateRegistrationStatusObs() {
        for (IObserver o : observers) {
            o.updateRegistrationStatus();
        }
    }

    @Override
    public String getUsername() {
        return username;
    }
}
