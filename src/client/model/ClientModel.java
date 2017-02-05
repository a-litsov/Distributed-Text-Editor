/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client.model;

import client.view.IObserver;
import client.MainClientFormMVC;
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
    String message;//Строка, содержащая сообщение
    boolean f = true;
    boolean flag = false;//Указывает на то, была передача данных, или нет
    
    ArrayList<IObserver> observers = new ArrayList<>();
    
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
            Logger.getLogger(MainClientFormMVC.class.getName()).log(Level.SEVERE, null, ex);
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
                            
                            
                            if(s.equals("File list sending")) {
                                getFileListFromServer();
                                updateFileListObs();
                            }
                                 
                            if(s.equals("File content sending.")) {
                                getFileContentFromServer();
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
            Logger.getLogger(MainClientFormMVC.class.getName()).log(Level.SEVERE, null, ex);   
        } 
    }
    
    @Override
    public void sendName(String name) {
        try {
            dos.writeUTF("Name sending");
            dos.writeUTF(name);
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
    public void sendRangesAndLock(String start, String end) {
        try {
            this.start = start;
            this.end = end;
            if (!testRange()) {
                invalidRange();
                return;
            }        
            //Отправляем выбранный диапазон
            dos.writeUTF("Ranges sending");
            dos.writeUTF(start);
            dos.writeUTF(end);
        } catch (IOException ex) {
            Logger.getLogger(MainClientFormMVC.class.getName()).log(Level.SEVERE, null, ex);

        }
    }
    
    @Override
    public void sendUnlocking() {
        try {
            dos.writeUTF("Unlocking");
        }
        catch (IOException ex) {
                Logger.getLogger(MainClientFormMVC.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void sendStop() {
        try {
            dos.writeUTF("end");
        } catch (IOException ex) {
            Logger.getLogger(MainClientFormMVC.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void sendSaveRequest(String content) {
        try {
            dos.writeUTF("File saving");
            dos.writeUTF(content);
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
            fileContent = dis.readUTF();
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
}
