package org.jic.bean;

import org.jic.file.FileReciever;
import org.jic.file.FileSender;

import javax.swing.*;
import javax.swing.text.DefaultStyledDocument;
import java.awt.*;
import java.net.InetAddress;
import java.util.Date;

public class User implements Comparable<User> {

    public static final int TYPE_USER = 0;
    public static final int TYPE_CHANNEL = 1;

    private InetAddress address;
    private String username;
    private Color color;
    private ImageIcon icon;
    private DefaultStyledDocument doc;
    private int type;
    private boolean notifying;
    private String toolTip;
    private String state;
    private Date connectedSince;
    private long lastActivity;
    private FileReciever fileReciever;
    private FileSender fileSender;

    public User(DefaultStyledDocument doc) {
        address = null;
        username = null;
        color = Color.BLACK;
        icon = null;
        this.doc = doc;
        type = TYPE_USER;
        notifying = false;
        state = "";
        updateToolTip();
    }

    public InetAddress getAddress() {
        return address;
    }

    public void setAddress(InetAddress address) {
        this.address = address;
        updateToolTip();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
        updateToolTip();
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
        updateToolTip();
    }

    synchronized public ImageIcon getIcon() {
        return icon;
    }

    synchronized public void setIcon(Icon icon) {
        this.icon = (ImageIcon) icon;
        updateToolTip();
    }


    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
        updateToolTip();
    }


    public DefaultStyledDocument getDoc() {
        return doc;
    }


    public boolean isNotifying() {
        return notifying;
    }

    public void setNotifying(boolean notifying) {
        this.notifying = notifying;
    }


    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int compareTo(User u) {
        if (getType() == TYPE_CHANNEL && u.getType() != TYPE_CHANNEL)
            return 1;
        if (u.getType() == TYPE_CHANNEL && getType() != TYPE_CHANNEL)
            return -1;
        return getUsername().compareTo(u.getUsername());
    }

    public Date getConnectedSince() {
        return connectedSince;
    }

    public void setConnectedSince(Date connectedSince) {
        this.connectedSince = connectedSince;
    }

    public long getLastActivity() {
        return lastActivity;
    }

    public void setLastActivity(long lastActivity) {
        this.lastActivity = lastActivity;
    }

    public String getToolTip() {
        return toolTip;
    }


    public FileReciever getFileReciever() {
        return fileReciever;
    }

    public void setFileReciever(FileReciever fileReciever) {
        this.fileReciever = fileReciever;
    }

    public FileSender getFileSender() {
        return fileSender;
    }

    public void setFileSender(FileSender fileSender) {
        this.fileSender = fileSender;
    }

    public void updateToolTip() {
        // TODO
        toolTip = "";
/*
        if (address == null || color == null || username == null)
            toolTip = "";
        else
            toolTip = String.format(
                    "<html>" +
                            "<b>Name: </b><span color=#%4$02x%5$02x%6$02x>%1$s</span><br>" +
                            "<b>State: </b>blablabla<br>" +
                            "<b>Host name: </b>%2$s<br>" +
                            "<b>Host address: </b>%3$s<br>" +
                            "<b>Connected since: </b>blablabla<br>" +
                            "<html>"
                    , Util.escapeSpecialHTMLChars(username), address.getHostName(), address.getHostAddress(), color.getRed(), color.getGreen(), color.getBlue());
*/
        // TODO: Escape html characters and remove the fucking string away
    }
}
