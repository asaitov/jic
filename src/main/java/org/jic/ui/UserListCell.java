package org.jic.ui;

import javax.swing.*;
import java.awt.*;


public class UserListCell extends JPanel {
    private JLabel iconLabel, nameLabel, stateLabel;

    public UserListCell() {
        super();
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));


        iconLabel = new JLabel();
        nameLabel = new JLabel();
        stateLabel = new JLabel();

        Font font = iconLabel.getFont();

        iconLabel.setOpaque(true);
        iconLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));

        nameLabel.setOpaque(true);
        nameLabel.setHorizontalAlignment(SwingConstants.LEADING);
//        nameLabel.setFont(font.deriveFont(Font.BOLD, 14));

        stateLabel.setOpaque(true);
        stateLabel.setHorizontalAlignment(SwingConstants.TRAILING);
        stateLabel.setFont(font.deriveFont(Font.PLAIN, 12));
        stateLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(nameLabel, BorderLayout.CENTER);
        panel.add(stateLabel, BorderLayout.SOUTH);
        add(iconLabel, BorderLayout.WEST);
        add(panel, BorderLayout.CENTER);
    }


    public void setIcon(ImageIcon icon) {
        iconLabel.setIcon(icon);
    }

    public void setName(String name) {
        nameLabel.setText(name);
    }

    public void setState(String state) {
        stateLabel.setText(state);
    }

    public void setFont(Font font) {
//        nameLabel.setFont(font);
//        stateLabel.setFont(new Font(Defaults.FONT_FAMILY, Defaults.SYSTEM_FONT_STYLE, Defaults.SYSTEM_FONT_SIZE - 2));
    }

    public void setColor(Color color) {
        if (iconLabel == null || nameLabel == null || stateLabel == null)
            return;
        iconLabel.setBackground(color);
        nameLabel.setBackground(color);
        stateLabel.setBackground(color);
        setBackground(color);
    }

    public void setUserColor(Color color) {
        nameLabel.setForeground(color);
    }

    public void italize() {
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.ITALIC | Font.BOLD));
    }

    public void normalize() {
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD));
    }
}
