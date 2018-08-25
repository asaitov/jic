package org.jic.ui;

import org.jic.core.Jic;
import org.jic.Message;
import org.jic.util.Util;
import org.jic.settings.Defaults;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Vector;

public class SettingsDialog extends JDialog implements ActionListener, PropertyChangeListener {
    private final static int BG = 10;
    private final static int SG = 5;
    private final static int TF_WIDTH = 150;
    private final static int TFS_WIDTH = 100;
    private final static int TFM_WIDTH = 50;

    private JLabel userIconPreview, channelIconPreview;
    private JFileChooser uifc, cifc;
    private JPanel contentPane;
    private JButton buttonOK, buttonCancel, buttonDefault;
    private JTabbedPane tabbedPane;
    private JPanel connectionPanel, userPanel, chatPanel, filesPanel;
    private JPanel buttonPanel;
    private JTextField labelTextField;
    private JTextField addressField;
    private JTextField userNameField;
    private JComboBox userStateField;
    private MainFrame frame;
    private Jic jic;
    private JLabel userIconLabel, channelIconLabel;
    private ImageIcon icon;
    private JLabel fontLabel, colorLabel;
    private JButton fontButton, colorButton, userIconButton, channelIconButton;

    private JSpinner portField, ttlField;
    private JSpinner fontSizeField;
    private JSpinner refreshIntervalField;

    private JComboBox fontFamilyField;
    private boolean defaultUserIcon, defaultChannelIcon;
    private JCheckBox logCheckBox;
    private JButton logButton;

    private JTextField timeStampField;
    private JLabel timeStampLabel;
    private JCheckBox timeStamping, smiling, showSystemMessages;

    private JButton addStateButton, removeStateButton;

    public SettingsDialog(MainFrame frame, Jic jic) {
        super(frame, JDialog.ModalityType.APPLICATION_MODAL);
        this.jic = jic;
        this.frame = frame;

        defaultUserIcon = false;
        defaultChannelIcon = false;

        contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout());
        setContentPane(contentPane);

        buttonOK = new JButton(getString("Settings.OK"));
        buttonCancel = new JButton(getString("Settings.Cancel"));
        buttonDefault = new JButton(getString("Settings.Default"));
        buttonOK.addActionListener(this);
        buttonCancel.addActionListener(this);
        buttonDefault.addActionListener(this);
        buttonPanel = new JPanel();
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(SG, SG, SG, SG));
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(buttonDefault);
        buttonPanel.add(Box.createRigidArea(new Dimension(SG, 0)));
        buttonPanel.add(buttonOK);
        buttonPanel.add(Box.createRigidArea(new Dimension(SG, 0)));
        buttonPanel.add(buttonCancel);

        connectionPanel = createConnectionPanel();
        userPanel = createUserPanel();
        chatPanel = createChatPanel();
        filesPanel = createFilesPanel();


        tabbedPane = new JTabbedPane();
        tabbedPane.addTab(getString("Settings.Connection.title"), connectionPanel);
        tabbedPane.addTab(getString("Settings.User.title"), userPanel);
        tabbedPane.addTab(getString("Settings.Chat.title"), chatPanel);
        tabbedPane.addTab(getString("Settings.Files.title"), filesPanel);


        contentPane.add(tabbedPane, BorderLayout.CENTER);
        contentPane.add(buttonPanel, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(buttonOK);


        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        contentPane.registerKeyboardAction(this, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setTitle(getString("Settings.title"));


        pack();
        setLocationRelativeTo(null);
        setIconImage(frame.getIconImage());
        setResizable(false);
    }

    private void onOK() {
        setVisible(false);
        if (jic.getFont() != fontLabel.getFont())
            jic.setFont(fontLabel.getFont());
        if (colorLabel.getForeground() != jic.getMe().getColor())
            jic.sendMessageToAll(Message.MA_SET_COLOR, colorLabel.getForeground());
        if (defaultUserIcon) {
            jic.setDefaultUserIcon();
            jic.sendMessageToAll(Message.MA_SET_ICON, null);
        } else if (uifc != null && !Util.eq(jic.getUserIconPath(), uifc.getSelectedFile().getAbsolutePath())) {
            jic.setUserIconPath(uifc.getSelectedFile().getAbsolutePath());
            jic.sendMessageToAll(Message.MA_SET_ICON, null);
        }
        String newNick = userNameField.getText();
        if (newNick != null && !newNick.trim().equals("") && !jic.getMe().getUsername().equals(newNick)) {
            if (newNick.length() > Defaults.MAX_USERNAME_LENGTH)
                newNick = newNick.substring(0, Defaults.MAX_USERNAME_LENGTH);
            jic.sendMessageToAll(Message.MA_SET_USERNAME, newNick);
        }
        jic.setTimeStamping(timeStamping.isSelected());
        jic.setTimeStampFormat(timeStampField.getText());
        jic.setSmiling(smiling.isSelected());
        jic.setShowSystemMessages(showSystemMessages.isSelected());
        jic.setRefreshInterval((Integer) refreshIntervalField.getValue());
        if (defaultChannelIcon) {
            jic.setChannelIcon(null);
            frame.updateUserList();
        } else if (cifc != null && !Util.eq(jic.getChannelIconPath(), cifc.getSelectedFile().getAbsolutePath())) {
            jic.setChannelIconPath(cifc.getSelectedFile().getAbsolutePath());
            jic.setChannelIcon((ImageIcon) channelIconLabel.getIcon());
            frame.updateUserList();
        }

        jic.setOptsAddress(addressField.getText());
        jic.setOptsPort((Integer) portField.getValue());
        jic.setOptsTtl((Integer) ttlField.getValue());

        boolean needRestart = !Util.eq(jic.getSocketGroup().getHostAddress(), addressField.getText().trim()) || (Integer) portField.getValue() != jic.getPort() || (Integer) ttlField.getValue() != jic.getTtl();
        if (needRestart)
            JOptionPane.showConfirmDialog(frame, getString("Settings.need_restart"), getString("Settings.attention"), JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);

        updateStates();

        String state = (String) userStateField.getSelectedItem();
        if (!Util.eq(state, jic.getMe().getState()))
            jic.sendMessageToAll(Message.MA_SET_STATE, state);

//        jic.saveSettings();
        dispose();
    }

    private void onCancel() {
        setVisible(false);
        dispose();
    }

    private void onDefault() {
        JPanel src = (JPanel) tabbedPane.getSelectedComponent();
        if (src == connectionPanel) {
            addressField.setText(Defaults.MULTICAST_ADDRESS);
            portField.setValue(Defaults.MULTICAST_PORT);
            ttlField.setValue(Defaults.MULTICAST_TTL);
        } else if (src == userPanel) {
            colorLabel.setForeground(new Color(Defaults.USER_COLOR));
            userIconLabel.setIcon(jic.getDefaultUserIcon());
            defaultUserIcon = true;
            userNameField.setText(jic.getMe().getAddress().getHostName());

        } else if (src == chatPanel) {
            fontFamilyField.setSelectedItem(Defaults.FONT_FAMILY);
            fontSizeField.setValue(Defaults.FONT_SIZE);
            channelIconLabel.setIcon(jic.getDefaultChannelIcon());
            defaultChannelIcon = true;
            timeStamping.setSelected(true);
            timeStampField.setText(Defaults.TIME_STAMP);
            refreshIntervalField.setValue(Defaults.REFRESH_INTERVAL);
            smiling.setSelected(true);
            showSystemMessages.setSelected(true);
        }
    }

    public String getString(String key) {
        return frame.getString(key);
    }

    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == buttonOK) {
            onOK();
        } else if (src == buttonCancel) {
            onCancel();
        } else if (src == contentPane) {
            onCancel();
        } else if (src == buttonDefault) {
            onDefault();
        } else if (src == userIconButton) {
            userIconPreview = new JLabel();
            uifc = createIconFileChooser(userIconPreview);
            uifc.showOpenDialog(this);
            File file = uifc.getSelectedFile();
            if (file == null || file.length() > Defaults.ICON_MAX_SIZE) {
                uifc = null;
                return;
            }
            ImageIcon icon = new ImageIcon(file.getAbsolutePath());
            if (!Util.isSuitableImage(file.length(), icon.getIconWidth(), icon.getIconHeight())) {
                uifc = null;
                return;
            }
            defaultUserIcon = false;
            userIconLabel.setIcon(icon);
        } else if (src == channelIconButton) {
            channelIconPreview = new JLabel();
            cifc = createIconFileChooser(channelIconPreview);
            cifc.showOpenDialog(this);
            File file = cifc.getSelectedFile();
            if (file == null) {
                cifc = null;
                return;
            }
            ImageIcon icon = new ImageIcon(file.getAbsolutePath());
            if (!Util.isSuitableImage(0, icon.getIconWidth(), icon.getIconHeight())) {
                cifc = null;
                return;
            }
            defaultChannelIcon = false;
            channelIconLabel.setIcon(icon);
        } else if (src == colorButton) {
            Color color = JColorChooser.showDialog(this, getString("Chat.input.change_color.title"), jic.getMe().getColor());
            if (color != null)
                colorLabel.setForeground(color);
        } else if (src == fontButton) {

        } else if (src == fontFamilyField) {
            String f = (String) fontFamilyField.getSelectedItem();
            fontLabel.setFont(new Font(f, Font.PLAIN, fontLabel.getFont().getSize()));
        } else if (src == addStateButton) {
            String newState = (String) JOptionPane.showInputDialog(this, getString("Settings.User.add_state.text"), getString("Settings.User.add_state.title"), JOptionPane.QUESTION_MESSAGE, null, null, "");
            if (newState != null && !newState.trim().equals("")) {
                if (newState.length() > Defaults.MAX_USER_STATE_LENGTH)
                    newState = newState.substring(0, Defaults.MAX_USER_STATE_LENGTH);
                userStateField.addItem(newState);
                userStateField.setSelectedIndex(userStateField.getItemCount() - 1);
            }
        } else if (src == removeStateButton) {
            if (userStateField.getSelectedIndex() != 0)
                userStateField.removeItemAt(userStateField.getSelectedIndex());
        }

    }

    private JPanel createConnectionPanel() {
        addressField = new JTextField();
        portField = new JSpinner(new SpinnerNumberModel(jic.getPort(), 0, 65535, 1));
        ttlField = new JSpinner(new SpinnerNumberModel(jic.getTtl(), 1, 10, 1));
        Util.setPrefferedWidth(addressField, TF_WIDTH);
        Util.setPrefferedWidth(portField, TFS_WIDTH);
        Util.setPrefferedWidth(ttlField, TFM_WIDTH);


        JPanel p, p1, panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(SG, SG, SG, SG));
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));


        p1 = new JPanel();
        p1.setBorder(BorderFactory.createTitledBorder((String) null));
        p1.setLayout(new BoxLayout(p1, BoxLayout.PAGE_AXIS));

        p = new JPanel();
        p.setLayout(new BorderLayout());
        p.add(new JLabel(getString("Settings.Connection.address")), BorderLayout.WEST);
        p.add(addressField, BorderLayout.EAST);
        addressField.setText(jic.getSocketGroup().getHostAddress());
        p1.add(p);

        p1.add(Box.createRigidArea(new Dimension(0, SG)));

        p = new JPanel();
        p.setLayout(new BorderLayout());
        p.add(new JLabel(getString("Settings.Connection.port")), BorderLayout.WEST);
        p.add(portField, BorderLayout.EAST);
        p1.add(p);

        p1.add(Box.createRigidArea(new Dimension(0, SG)));

        p = new JPanel();
        p.setLayout(new BorderLayout());
        p.add(new JLabel(getString("Settings.Connection.ttl")), BorderLayout.WEST);
        p.add(ttlField, BorderLayout.EAST);
        p1.add(p);


        panel.add(p1);


        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(panel, BorderLayout.NORTH);
        return mainPanel;
    }

    private JButton createMiniButton(String ni, String ri, String pi) {
        JButton button = new JButton(new ImageIcon(getClass().getResource(ni)));
        button.setRolloverIcon(new ImageIcon(getClass().getResource(ri)));
        button.setPressedIcon(new ImageIcon(getClass().getResource(pi)));
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.addActionListener(this);
        return button;
    }


    private JPanel createUserPanel() {
        userNameField = new JTextField();
        userStateField = new JComboBox(jic.getStates());
        userStateField.setSelectedItem(jic.getMe().getState());
        Util.setPrefferedWidth(userNameField, TF_WIDTH);
        Util.setPrefferedWidth(userStateField, TF_WIDTH);


        JPanel p, p1, panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(SG, SG, SG, SG));
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

        p = new JPanel();
        p.setLayout(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(getString("Settings.User.name")));
        p.add(userNameField, BorderLayout.SOUTH);
        userNameField.setText(jic.getMe().getUsername());
        panel.add(p);

        p = new JPanel();
        p.setLayout(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(getString("Settings.User.state")));
        p.add(userStateField, BorderLayout.CENTER);
        panel.add(p);
        p1 = new JPanel();
        p1.setLayout(new BorderLayout(0, 0));
        addStateButton = createMiniButton(Defaults.PLUS_NORMAL, Defaults.PLUS_ROLLOVER, Defaults.PLUS_DOWN);


        removeStateButton = createMiniButton(Defaults.MINUS_NORMAL, Defaults.MINUS_ROLLOVER, Defaults.MINUS_DOWN);
        p1.add(addStateButton, BorderLayout.WEST);
        p1.add(removeStateButton, BorderLayout.EAST);
//        p1.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        p.add(p1, BorderLayout.EAST);


        p1 = new JPanel();
        p1.setLayout(new BorderLayout());

        p = new JPanel();
        p.setBorder(BorderFactory.createTitledBorder(getString("Settings.User.icon")));
        p.setLayout(new BoxLayout(p, BoxLayout.LINE_AXIS));
        userIconLabel = new JLabel(jic.getMe().getIcon());
        p.add(userIconLabel);
        p.add(Box.createRigidArea(new Dimension(BG, 0)));
        p.add(Box.createHorizontalGlue());
        userIconButton = new JButton(getString("Settings.browse"));
        userIconButton.addActionListener(this);
        p.add(userIconButton);
        p1.add(p, BorderLayout.EAST);


        p = new JPanel();
        p.setBorder(BorderFactory.createTitledBorder(getString("Settings.User.color")));
        p.setLayout(new BoxLayout(p, BoxLayout.LINE_AXIS));
        colorLabel = new JLabel(getString("Settings.sample_text"));
        colorLabel.setOpaque(true);
        colorLabel.setBackground(Color.WHITE);
        colorLabel.setForeground(jic.getMe().getColor());
        p.add(colorLabel);
        p.add(Box.createRigidArea(new Dimension(BG, 0)));
        p.add(Box.createHorizontalGlue());
        colorButton = new JButton(getString("Settings.select"));
        colorButton.addActionListener(this);
        p.add(colorButton);
        p1.add(p, BorderLayout.WEST);

        panel.add(p1);


        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(panel, BorderLayout.NORTH);
        return mainPanel;
    }


    private JPanel createChatPanel() {

        JPanel p, p1, p2, panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(SG, SG, SG, SG));
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));


        p = new JPanel();
        p.setBorder(BorderFactory.createTitledBorder(getString("Settings.Chat.font")));
        p.setLayout(new BorderLayout(0, SG));

        p1 = new JPanel();
        p1.setLayout(new BorderLayout(0, SG));
        fontLabel = new JLabel(getString("Settings.sample_text"));
        fontLabel.setHorizontalAlignment(SwingConstants.CENTER);
        fontLabel.setOpaque(true);
        fontLabel.setBackground(Color.WHITE);
        fontLabel.setFont(jic.getFont());
        p1.add(fontLabel, BorderLayout.CENTER);
        p1.setPreferredSize(new Dimension(0, 30));
        p.add(p1);

        p1 = new JPanel();
        p1.setLayout(new BoxLayout(p1, BoxLayout.LINE_AXIS));
        fontFamilyField = new JComboBox(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
        fontFamilyField.setSelectedItem(jic.getFont().getFamily());
        fontFamilyField.addActionListener(this);
        p1.add(fontFamilyField);
        p1.add(Box.createRigidArea(new Dimension(BG, 0)));
        p1.add(Box.createHorizontalGlue());
        fontSizeField = new JSpinner(new SpinnerNumberModel(jic.getFont().getSize(), 5, 50, 1));
        Util.setPrefferedWidth(fontSizeField, TFM_WIDTH);
        fontSizeField.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                int i = (Integer) fontSizeField.getValue();
                fontLabel.setFont(new Font(fontLabel.getFont().getFamily(), Font.PLAIN, i));
            }
        });
        p1.add(fontSizeField);
        p.add(p1, BorderLayout.SOUTH);

        panel.add(p);


        p1 = new JPanel();
        p1.setLayout(new BorderLayout());

        p = new JPanel();
        p.setBorder(BorderFactory.createTitledBorder(getString("Settings.Chat.channel_icon")));
        p.setLayout(new BoxLayout(p, BoxLayout.LINE_AXIS));
        channelIconLabel = new JLabel(jic.getChannel().getIcon());
        p.add(channelIconLabel);
        p.add(Box.createRigidArea(new Dimension(BG, 0)));
        p.add(Box.createHorizontalGlue());
        channelIconButton = new JButton(getString("Settings.browse"));
        channelIconButton.addActionListener(this);
        p.add(channelIconButton);
        p1.add(p, BorderLayout.EAST);


        p = new JPanel();
        p.setLayout(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(getString("Settings.Chat.time_stamp")));

        p2 = new JPanel();
        p2.setLayout(new BoxLayout(p2, BoxLayout.LINE_AXIS));
        timeStamping = new JCheckBox(getString("Settings.show"), jic.isTimeStamping());
        p2.add(timeStamping);
        p2.add(Box.createRigidArea(new Dimension(BG, 0)));
        p2.add(Box.createHorizontalGlue());
        timeStampLabel = new JLabel(Util.getTimeStamp(jic.getTimeStampFormat()));
        p2.add(timeStampLabel);
        p.add(p2, BorderLayout.NORTH);
        p2 = new JPanel();
        p2.setLayout(new BoxLayout(p2, BoxLayout.LINE_AXIS));
        timeStampField = new JTextField(jic.getTimeStampFormat());
        timeStampField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                timeStampLabel.setText(Util.getTimeStamp(timeStampField.getText()));
            }

            public void removeUpdate(DocumentEvent e) {
                timeStampLabel.setText(Util.getTimeStamp(timeStampField.getText()));
            }

            public void changedUpdate(DocumentEvent e) {
            }
        });
        p2.add(timeStampField);
        p.add(p2, BorderLayout.SOUTH);


        p1.add(p, BorderLayout.CENTER);

        panel.add(p1);

        panel.add(Box.createRigidArea(new Dimension(0, SG)));

        p1 = new JPanel();
        p1.setLayout(new BoxLayout(p1, BoxLayout.PAGE_AXIS));
        p1.setBorder(BorderFactory.createTitledBorder((String) null));

        p = new JPanel();
        p.setLayout(new BorderLayout());
        p.add(new JLabel(getString("Settings.Chat.refresh_interval")), BorderLayout.WEST);
        refreshIntervalField = new JSpinner(new SpinnerNumberModel(jic.getRefreshInterval(), 1, 60, 1));
        Util.setPrefferedWidth(refreshIntervalField, TFM_WIDTH);
        p.add(refreshIntervalField, BorderLayout.EAST);
        p1.add(p);


        p = new JPanel();
        p.setLayout(new BorderLayout());
        smiling = new JCheckBox(getString("Settings.Chat.enable_smiles"), jic.isSmiling());
        p.add(smiling, BorderLayout.WEST);
        p1.add(p);

        p = new JPanel();
        p.setLayout(new BorderLayout());
        showSystemMessages = new JCheckBox(getString("Settings.Chat.show_system_messages"), jic.isShowSystemMessages());
        p.add(showSystemMessages, BorderLayout.WEST);
        p1.add(p);

        panel.add(p1);

        // TODO: Sounds checkbox

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(panel, BorderLayout.NORTH);
        return mainPanel;
    }

    private JFileChooser createIconFileChooser(JLabel preview) {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(SG, SG, SG, SG));
        panel.setLayout(new BorderLayout());
        preview.setPreferredSize(new Dimension(Defaults.ICON_WIDTH + BG, Defaults.ICON_HEIGHT + BG));
        panel.add(preview, BorderLayout.CENTER);
        JFileChooser fc = new JFileChooser(".");
        fc.setAcceptAllFileFilterUsed(false);
        fc.setApproveButtonText(getString("FileChooser.approveButtonText"));
        fc.setApproveButtonToolTipText(getString("FileChooser.approveButtonToolTipText"));
        fc.setFileFilter(new FileNameExtensionFilter(getString("Settings.image_files_desc"), "jpg", "gif"));
        fc.setMultiSelectionEnabled(false);
        fc.setDialogTitle(getString("Settings.Chat.select_icon"));
        fc.setAccessory(panel);
        fc.addPropertyChangeListener(this);

        return fc;
    }

    private JPanel createFilesPanel() {
        JPanel p, panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(SG, SG, SG, SG));
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));


        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(panel, BorderLayout.NORTH);
        return mainPanel;
    }

    public void propertyChange(PropertyChangeEvent e) {
        e.getSource();
        if (e.getSource() == uifc) {
            if (e.getPropertyName().equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY)) {
                File file = uifc.getSelectedFile();
                if (file == null || file.length() > Defaults.ICON_MAX_SIZE) {
                    userIconPreview.setIcon(null);
                    return;
                }
                ImageIcon icon = new ImageIcon(file.getAbsolutePath());
                if (!Util.isSuitableImage(file.length(), icon.getIconWidth(), icon.getIconHeight())) {
                    userIconPreview.setIcon(null);
                    return;
                }
                userIconPreview.setIcon(icon);
            }
        } else if (e.getSource() == cifc) {
            if (e.getPropertyName().equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY)) {
                File file = cifc.getSelectedFile();
                if (file == null) {
                    channelIconPreview.setIcon(null);
                    return;
                }
                ImageIcon icon = new ImageIcon(file.getAbsolutePath());
                if (!Util.isSuitableImage(0, icon.getIconWidth(), icon.getIconHeight())) {
                    channelIconPreview.setIcon(null);
                    return;
                }
                channelIconPreview.setIcon(icon);
            }
        }
    }

    public void updateStates() {
        Vector<String> states = new Vector<String>();
        for (int i = 0; i < userStateField.getItemCount(); i++)
            states.add((String) userStateField.getItemAt(i));
        jic.setStates(states);
    }

}
