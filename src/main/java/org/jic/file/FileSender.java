package org.jic.file;

import org.jic.bean.User;
import org.jic.core.Jic;
import org.jic.settings.Defaults;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class FileSender extends Thread {
    private File[] files;
    private User user;
    private ServerSocket serverSocket;
    private JDialog frame;
    private Socket socket;
    private int id;
    private DataInputStream in;
    private DataOutputStream out;

    public FileSender(Jic jic, File[] files, User user) {
        this.files = files;
        this.user= user;
        try {
            serverSocket = new ServerSocket(0, 10, jic.getMe().getAddress());
        } catch (IOException e) {
            // TODO
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        frame = new JDialog();
    }


    public void run() {
        try {
            serverSocket.setSoTimeout(Defaults.CONNECTION_TIMEOUT);
        } catch (SocketException e) {
            // TODO
        }

        try {
//SocketTimeout
            Socket socket = serverSocket.accept();
            if (!socket.getInetAddress().equals(user.getAddress()))  {
                //TODO
                return;
            }
            try {
                in = new DataInputStream(socket.getInputStream());
                out = new DataOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                // TODO
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

            sendFileList();
        } catch (Exception e) {
            // TODO
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


        user.setFileSender(null);
    }

    private void sendFileList() {
        id = 0;
        for (File file: files) {
            go(file, -1);
        }
        try {
            out.writeInt(-1);
        } catch (IOException e) {
            // TODO
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private void go(File file, int parent) {
        int t = id;
        sendFileInfo(id++, parent, file.length(), file.getName());
        File files[] = file.listFiles();
        if (files == null)
            return;
        for (File f: files)
            go(f, t);
    }

    public int getPort() {
        return serverSocket.getLocalPort();
    }

    private void sendFileInfo(int id, int parent, long size, String name) {
        try {
            out.writeInt(id);
            out.writeInt(parent);
            out.writeLong(size);
            out.writeInt(name.length());
            out.write(name.getBytes());
        } catch (IOException e) {
            // TODO
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

}

