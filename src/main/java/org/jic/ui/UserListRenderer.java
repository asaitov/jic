package org.jic.ui;

import org.jic.bean.User;
import org.jic.util.Util;
import org.jic.settings.Defaults;

import javax.swing.*;
import java.awt.*;

class UserListRenderer extends UserListCell implements ListCellRenderer {
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        User user = (User) value;
        setName(user.getUsername());
        setState(user.getState());
        setIcon(user.getIcon());
        if (isSelected) {
            if (user.isNotifying())
                setColor(Util.middleColor(Defaults.NOTIFYING_COLOR, list.getSelectionBackground()));
            else
                setColor(list.getSelectionBackground());
        } else {
            if (user.isNotifying())
                setColor(Defaults.NOTIFYING_COLOR);
            else {
                if ((index & 1) == (list.getModel().getSize() & 1))
                    setColor(list.getBackground());
                else
                    setColor(new Color(245, 245, 245));

            }
        }
        setUserColor(user.getColor());
        setEnabled(list.isEnabled());
        if (user.getType() == User.TYPE_CHANNEL)
            italize();
        else
            normalize();
        setToolTipText(user.getToolTip());
        return this;
    }
}
/*
class UserListRenderer extends JLabel implements ListCellRenderer {
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        User user = (User) value;
        setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
        setText(user.getUsername());
        setIcon(user.getIcon());
        if (isSelected) {
            if (user.isNotifying())
//                setBackground(Defaults.NOTIFYING_SELECTED_COLOR);
                setBackground(Util.middleColor(Defaults.NOTIFYING_COLOR, list.getSelectionBackground()));
            else
                setBackground(list.getSelectionBackground());
        } else {
            if (user.isNotifying())
                setBackground(Defaults.NOTIFYING_COLOR);
            else
                setBackground(list.getBackground());
        }
        setForeground(user.getColor());
        setEnabled(list.isEnabled());
        Font font = list.getFont();
        if (user.getType() == User.TYPE_USER)
            setFont(font);
        else
            setFont(new Font(font.getName(), font.getStyle() | Font.ITALIC, font.getSize()));
        setOpaque(true);
        setToolTipText(user.getToolTip());
        return this;
    }
}
*/
