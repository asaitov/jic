package org.jic.file;

import org.jic.bean.User;
import org.jic.bean.FileDescription;
import org.jic.core.Jic;
import org.jic.settings.Defaults;

import javax.swing.*;
import java.net.Socket;
import java.io.*;
import java.util.ArrayList;


public class FileReciever extends Thread{
    private Jic jic;
    private User user;
    private Socket socket;
    private JDialog frame;
    private DataInputStream in;
    private DataOutputStream out;
    private ArrayList<FileDescription> filelist;


    public FileReciever(Jic jic, User user, Socket socket) {
        this.jic = jic;
        this.user = user;
        this.socket = socket;
        JDialog frame = new JDialog();
        filelist = new ArrayList<FileDescription>();
        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            // TODO
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public void run() {
        recieveFileList();
    }

    private void recieveFileList() {
        long size;
        int id, parent, len;
        String name;
        FileDescription file;
        byte[] buf = new byte[Defaults.MAX_FILENAME_LENGTH];
        while (true){
            try {
                id = in.readInt();
                if (id < 0)
                    break;
                parent = in.readInt();
                size = in.readLong();
                len = in.readInt();
                if (len > Defaults.MAX_FILENAME_LENGTH) {
                    // TODO
                }
                in.readFully(buf, 0, len);
                name = new String(buf, 0, len);
                file = new FileDescription(name, size, id, parent);
                filelist.add(file);
            } catch (IOException e) {
                // TODO
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        filelist.size();

    }


}
