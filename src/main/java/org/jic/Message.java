package org.jic;

import org.jic.settings.Defaults;

import java.awt.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Date;

public class Message {

    public static final byte MT_COMMON = 0;
    public static final byte MT_PRIVATE = 1;

    public static final byte MA_NOTHING = 0;
    public static final byte MA_MESSAGE = 1;
    public static final byte MA_I_AM_HERE = 2;
    public static final byte MA_WHO_IS_HERE = 3;
    public static final byte MA_SET_USERNAME = 4;
    public static final byte MA_SET_COLOR = 5;
    public static final byte MA_SET_ICON = 6;
    public static final byte MA_INFO = 7;
    public static final byte MA_GET_INFO = 8;
    public static final byte MA_GET_USERNAME = 9;
    public static final byte MA_GET_COLOR = 10;
    public static final byte MA_GET_ICON = 11;
    public static final byte MA_LEFT = 12;
    public static final byte MA_SET_TOPIC = 13;
    public static final byte MA_SET_STATE = 14;
    public static final byte MA_GET_STATE = 15;
    public static final byte MA_FILE_PROPOSAL = 16;

    
    private String username;
    private InetAddress address;
    private String text;
    private byte type;
    private byte action;
    private Color color;
    private byte[] iconData;
    private Date date;
    private int port;


    public Message() {

    }

    public static Message createMessage(DatagramPacket packet) {
        Message message = new Message();
        message.setAddress(packet.getAddress());
        ByteBuffer buffer = ByteBuffer.wrap(packet.getData());
        message.setType(buffer.get());
        message.setAction(buffer.get());
        int len;
        byte buf[];
        switch (message.getAction()) {
            case MA_MESSAGE:
                len = buffer.getInt();
                buf = new byte[len];
                buffer.get(buf, 0, len);
                message.setText(new String(buf));
                break;
            case MA_INFO:
                message.setColor(new Color(buffer.getInt()));
                message.setDate(new Date(buffer.getLong()));
                len = buffer.getInt();
                buf = new byte[len];
                buffer.get(buf, 0, len);
                message.setUsername(new String(buf));
                len = buffer.getInt();
                buf = new byte[len];
                buffer.get(buf, 0, len);
                message.setText(new String(buf)); // user state
                len = buffer.getInt();
                buf = new byte[len];
                buffer.get(buf, 0, len);
                message.setIconData(buf);
                break;
            case MA_SET_COLOR:
                message.setColor(new Color(buffer.getInt()));
                break;
            case MA_SET_ICON:
                len = buffer.getInt();
                buf = new byte[len];
                buffer.get(buf, 0, len);
                message.setIconData(buf);
                break;
            case MA_SET_USERNAME:
                len = buffer.getInt();
                buf = new byte[len];
                buffer.get(buf, 0, len);
                message.setUsername(new String(buf));
                break;
            case MA_SET_TOPIC:
                len = buffer.getInt();
                buf = new byte[len];
                buffer.get(buf, 0, len);
                message.setText(new String(buf));
                break;
            case MA_SET_STATE:
                len = buffer.getInt();
                buf = new byte[len];
                buffer.get(buf, 0, len);
                message.setText(new String(buf));
                break;

            case MA_FILE_PROPOSAL:
                message.setPort(buffer.getInt());
                break;
            case MA_NOTHING:
                break;
            case MA_I_AM_HERE:
                break;
            case MA_WHO_IS_HERE:
                break;
            case MA_GET_COLOR:
                break;
            case MA_GET_STATE:
                break;
            case MA_GET_ICON:
                break;
            case MA_GET_USERNAME:
                break;
            case MA_GET_INFO:
                break;
            case MA_LEFT:
                break;
            default:
        }


        return message;
    }

    public DatagramPacket createPacket(InetAddress dst, int port) {
        ByteBuffer buffer = ByteBuffer.allocate(Defaults.BUFFER_LENGTH);
        buffer.put(type);
        buffer.put(action);
        switch (action) {
            case MA_MESSAGE:
                byte s[] = text.getBytes();
                buffer.putInt(s.length);
                buffer.put(s);
                break;
            case MA_INFO:
                buffer.putInt(color.getRGB());
                buffer.putLong(date.getTime());
                buffer.putInt(username.length());
                buffer.put(username.getBytes());
                buffer.putInt(text.length());
                buffer.put(text.getBytes());
                buffer.putInt(iconData.length);
                buffer.put(iconData);
                break;
            case MA_SET_COLOR:
                buffer.putInt(color.getRGB());
                break;
            case MA_SET_USERNAME:
                buffer.putInt(username.length());
                buffer.put(username.getBytes());
                break;
            case MA_SET_TOPIC:
                buffer.putInt(text.length());
                buffer.put(text.getBytes());
                break;
            case MA_SET_STATE:
                buffer.putInt(text.length());
                buffer.put(text.getBytes());
                break;
            case MA_SET_ICON:
                buffer.putInt(iconData.length);
                buffer.put(iconData);
                break;
            case MA_FILE_PROPOSAL:
                buffer.putInt(this.port);
                break;
            case MA_NOTHING:
                break;
            case MA_I_AM_HERE:
                break;
            case MA_WHO_IS_HERE:
                break;
            case MA_GET_ICON:
                break;
            case MA_GET_COLOR:
                break;
            case MA_GET_STATE:
                break;
            case MA_GET_INFO:
                break;
            case MA_GET_USERNAME:
                break;
            case MA_LEFT:
            default:
        }
        return new DatagramPacket(buffer.array(), buffer.position() + 1, dst, port);
    }


    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public InetAddress getAddress() {
        return address;
    }

    public void setAddress(InetAddress address) {
        this.address = address;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }


    public byte getAction() {
        return action;
    }

    public void setAction(byte action) {
        this.action = action;
    }


    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public byte[] getIconData() {
        return iconData;
    }

    public void setIconData(byte[] iconData) {
        this.iconData = iconData;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }


    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
