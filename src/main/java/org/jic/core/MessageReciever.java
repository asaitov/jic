package org.jic.core;

import org.jic.core.Jic;
import org.jic.settings.Defaults;
import org.jic.Message;

import java.net.DatagramPacket;
import java.net.MulticastSocket;

public class MessageReciever extends Thread {

    private Jic jic;
    private boolean listening;
    private MulticastSocket socket;
    final private DatagramPacket packet;

    public MessageReciever(Jic jic) {
        this.jic = jic;
        byte[] buf = new byte[Defaults.BUFFER_LENGTH];
        packet = new DatagramPacket(buf, buf.length);
        socket = jic.getSocket();
        listening = true;
    }

    public boolean isListening() {
        return listening;
    }

    // TODO: sync???
    synchronized public void setListening(boolean listening) {
        this.listening = listening;
    }

    public void run() {
        Message msg;
        while (isListening()) {
            try {
                if (socket != null) {
                    // TODO: sync???
                    synchronized (packet) {
                        socket.receive(packet);
                        msg = Message.createMessage(packet);
                    }
                    jic.dispatchMessage(msg);
                } else
                    break;
            } catch (Throwable e) {
                e.printStackTrace();
                // TODO
                // jic.dispatchMessage(null);
            }
        }
    }

}
