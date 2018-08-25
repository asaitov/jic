package org.jic;

import org.jic.core.Jic;

import javax.swing.*;

public class Start {
    public static void main(String[] args) {
        Jic jic = new Jic();
        SwingUtilities.invokeLater(jic);
    }
}
