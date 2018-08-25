package org.jic.ui;

import org.jic.*;
import org.jic.settings.Defaults;
import org.jic.file.FileSender;
import org.jic.core.Jic;
import org.jic.util.Util;
import org.jic.bean.User;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.MessageFormat;
import java.util.*;

public class MainFrame extends JFrame implements KeyListener, WindowListener, ActionListener, ComponentListener, MouseListener {

    private SystemTray tray;


    private Jic jic;

    private JList userList;
    private JTextArea messageField;
    private JTextPane messageArea;
    private JPanel mainPanel;
    private JPanel lowPanel;
    private JPanel toolsPanel;
    private JPanel topicPanel;
    private JScrollPane scrollAreaPanel;
    private JScrollPane scrollFieldPanel;
    private JScrollPane scrollUserListPanel;
    private JSplitPane vsplitter;
    private JSplitPane hsplitter;
    private JLabel topicLabel;
    private TitledBorder topicPanelBorder;
    private JDialog smilesDialog;
    private SettingsDialog settings;


    private JButton buttonSmiles, buttonNick, buttonColor, buttonTopic, buttonClear, buttonSettings, buttonFileTransfer;

    private JMenuBar menu;
    private JMenu menuFile, menuChat, menuLanguage, menuHelp;
    private JMenuItem menuFileClose, menuFileSettings, menuFileHide;
    private JMenuItem menuHelpContents, menuHelpAbout, menuHelpCheck;
    private JMenuItem menuChatClear, menuChatNick, menuChatColor, menuChatTopic;

    private DefaultStyledDocument currentDoc;
    private User currentUser;

    private JScrollBar messageSB;
    private DefaultListModel userListModel;
    private Hashtable<String, ImageIcon> smiles;
    private Hashtable<String, ImageIcon> smilesTable;
    private Set<String> smileKeys;

    private PopupMenu trayPopup;

    private JPopupMenu userListPopup, chatPopup;

    private TrayIcon trayIcon;

    private Image trayBlinkImage;

    private ArrayList<String> messageHistory;
    private int currentMessage;


    public MainFrame(Jic jic) {
        this.jic = jic;

/*
        try {
            UIManager.setLookAndFeel("com.incors.plaf.alloy.AlloyLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }
*/

        createFrame();
    }

    private void createFrame() {

        messageHistory = new ArrayList<String>();
        currentMessage = 0;

        setIconImage(new ImageIcon(getClass().getResource(Defaults.APP_ICON)).getImage());
        trayBlinkImage = new ImageIcon(getClass().getResource(Defaults.APP_ICON_BLINK)).getImage();

        try {
            tray = SystemTray.getSystemTray();
            trayIcon = new TrayIcon(getIconImage(), null, trayPopup);
            trayIcon.setImageAutoSize(true);
            trayIcon.addActionListener(this);
        }
        catch (Throwable e) {
            tray = null;
        }
        createSmilesDialog();
        mainPanel = new JPanel();

        topicPanel = new JPanel();
        messageField = new JTextArea();
        messageArea = new JTextPane();
        userList = new JList();
        topicLabel = new JLabel();
        lowPanel = new JPanel();
        toolsPanel = new JPanel();

        scrollAreaPanel = new JScrollPane(messageArea);
        scrollAreaPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollAreaPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollFieldPanel = new JScrollPane(messageField);
        scrollFieldPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollFieldPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollUserListPanel = new JScrollPane(userList);
        scrollUserListPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollUserListPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        topicPanel.setLayout(new BorderLayout());
        topicPanelBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        topicPanel.setBorder(topicPanelBorder);
        topicLabel.setHorizontalAlignment(SwingConstants.CENTER);
        topicPanel.add(topicLabel, BorderLayout.CENTER);


        createToolsPanel();

        lowPanel.setLayout(new BorderLayout());
        lowPanel.add(toolsPanel, BorderLayout.NORTH);
        lowPanel.add(scrollFieldPanel, BorderLayout.CENTER);

        vsplitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollAreaPanel, scrollUserListPanel);
        vsplitter.setResizeWeight(1.0);
        hsplitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, vsplitter, lowPanel);
        hsplitter.setResizeWeight(1.0);
        scrollUserListPanel.setPreferredSize(new Dimension(Defaults.USERLIST_WIDTH, scrollUserListPanel.getPreferredSize().height));
        scrollFieldPanel.setMinimumSize(scrollFieldPanel.getPreferredSize());


        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));


        mainPanel.add(topicPanel, BorderLayout.NORTH);
        mainPanel.add(hsplitter, BorderLayout.CENTER);


        userListModel = new DefaultListModel();
        userList.setCellRenderer(new UserListRenderer());
        userList.setModel(userListModel);
        userList.addMouseListener(this);

        currentDoc = (DefaultStyledDocument) messageArea.getStyledDocument();
        messageArea.setEditable(false);
        messageArea.addComponentListener(this);
        messageSB = scrollAreaPanel.getVerticalScrollBar();
        messageField.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.CTRL_DOWN_MASK), Defaults.ACTION_BREAK);
        messageField.getActionMap().put(Defaults.ACTION_BREAK, new DefaultEditorKit.InsertBreakAction());
        messageField.addKeyListener(this);


        createMenus();

        setJMenuBar(menu);
        setContentPane(mainPanel);
        currentUser = jic.getChannel();
        setStrings();
        setTopic();

        pack();
        setExtendedState(MAXIMIZED_BOTH);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        messageField.requestFocus();
        addWindowListener(this);
    }

    private void createToolsPanel() {
        toolsPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        toolsPanel.setLayout(new BoxLayout(toolsPanel, BoxLayout.X_AXIS));
        buttonSmiles = createToolButton(Defaults.ICON_SMILES_NORMAL, Defaults.ICON_SMILES_ROLLOVER, Defaults.ICON_SMILES_DOWN);
        buttonNick = createToolButton(Defaults.ICON_NICK_NORMAL, Defaults.ICON_NICK_ROLLOVER, Defaults.ICON_NICK_DOWN);
        buttonColor = createToolButton(Defaults.ICON_COLOR_NORMAL, Defaults.ICON_COLOR_ROLLOVER, Defaults.ICON_COLOR_DOWN);
        buttonTopic = createToolButton(Defaults.ICON_TOPIC_NORMAL, Defaults.ICON_TOPIC_ROLLOVER, Defaults.ICON_TOPIC_DOWN);
        buttonClear = createToolButton(Defaults.ICON_NORMAL, Defaults.ICON_ROLLOVER, Defaults.ICON_DOWN);
        buttonSettings = createToolButton(Defaults.ICON_SETTINGS_NORMAL, Defaults.ICON_SETTINGS_ROLLOVER, Defaults.ICON_SETTINGS_DOWN);
        buttonFileTransfer = createToolButton(Defaults.ICON_NORMAL, Defaults.ICON_ROLLOVER, Defaults.ICON_DOWN);

    }

    private JButton createToolButton(String ni, String ri, String pi) {
        JButton button = new JButton(new ImageIcon(getClass().getResource(ni)));
        button.setRolloverIcon(new ImageIcon(getClass().getResource(ri)));
        button.setPressedIcon(new ImageIcon(getClass().getResource(pi)));
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.addActionListener(this);

        toolsPanel.add(button);
        return button;
    }

    private void createMenus() {
        JMenuItem item;
        menu = new JMenuBar();
        menuFile = new JMenu();
        menuChat = new JMenu();
        menuLanguage = new JMenu();
        menuHelp = new JMenu();

        menuFileSettings = createMenuItem(getString("Menu.File.settings"), Defaults.ICON_SETTINGS_MENU, getString("Menu.File.settings_desc"), KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.ALT_MASK));
        menuFile.add(menuFileSettings);
        menuFileHide = createMenuItem(getString("Menu.File.hide"), null, getString("Menu.File.hide_desc"), KeyStroke.getKeyStroke(KeyEvent.VK_H, ActionEvent.ALT_MASK));
        menuFile.add(menuFileHide);
        menuFile.add(new JSeparator());
        menuFileClose = createMenuItem(getString("Menu.File.close"), null, getString("Menu.File.close_desc"), KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.ALT_MASK));
        menuFile.add(menuFileClose);

        menuChatClear = createMenuItem(getString("Menu.Chat.clear"), null, getString("Menu.Chat.clear_desc"), null);
        menuChat.add(menuChatClear);
        menuChatNick = createMenuItem(getString("Menu.Chat.change_nick"), Defaults.ICON_NICK_MENU, getString("Menu.Chat.change_nick_desc"), null);
        menuChat.add(menuChatNick);
        menuChatColor = createMenuItem(getString("Menu.Chat.choose_color"), Defaults.ICON_COLOR_MENU, getString("Menu.Chat.choose_color_desc"), null);
        menuChat.add(menuChatColor);
        menuChatTopic = createMenuItem(getString("Menu.Chat.set_topic"), Defaults.ICON_TOPIC_MENU, getString("Menu.Chat.set_topic_desc"), null);
        menuChat.add(menuChatTopic);

        ButtonGroup langGroup = new ButtonGroup();
        for (int i = 0; i < Defaults.SUPPORTED_LANGUAGES.length; i++) {
            item = new JRadioButtonMenuItem(new LanguageAction(i));
            langGroup.add(item);
            menuLanguage.add(item);
            if (i == jic.getLocaleIndex())
                item.setSelected(true);
        }

        menuHelpContents = createMenuItem(getString("Menu.Help.contents"),Defaults.ICON_HELP_MENU, getString("Menu.Help.contents_desc"), KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
        menuHelp.add(menuHelpContents);
        menuHelpCheck = createMenuItem(getString("Menu.Help.check_for_updates"), null, getString("Menu.Help.check_for_updates_desc"), null);
        menuHelp.add(menuHelpCheck);
        menuHelp.add(new JSeparator());
        menuHelpAbout = createMenuItem(getString("Menu.Help.about"), null, getString("Menu.Help.about_desc"), null);
        menuHelp.add(menuHelpAbout);

        menu.add(menuFile);
        menu.add(menuChat);
        menu.add(menuLanguage);
        menu.add(menuHelp);
    }

    private void createSmilesDialog() {
        Hashtable<String, ImageIcon> smilesTable = jic.getSmilesTable();
        Vector<String> smilesVector = jic.getSmilesVector();
        smilesDialog = new JDialog(this);

        JPanel smilesPanel = new JPanel();
        smilesPanel.setLayout(new BoxLayout(smilesPanel, BoxLayout.PAGE_AXIS));
        smilesPanel.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                smilesDialog.setVisible(false);
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        smilesDialog.setContentPane(smilesPanel);
        smilesPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        int i = 0;
        ImageIcon icon;
        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        smilesPanel.setLayout(gbl);
        for (String s : smilesVector) {
            gbc.gridx = i % 10;
            gbc.gridy = i / 10;
            i++;
            icon = smilesTable.get(s);
            Toolkit.getDefaultToolkit().prepareImage(icon.getImage(), -1, -1, null);
            JButton button = new JButton(new SmileAction(icon, s));
            button.setRolloverIcon(icon);
            button.setPressedIcon(icon);
            button.setMargin(new Insets(0, 0, 0, 0));
            button.setContentAreaFilled(false);
            button.setBorderPainted(false);
            button.setFocusPainted(false);
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            gbl.setConstraints(button, gbc);
            smilesPanel.add(button);

        }
        smilesDialog.setUndecorated(true);
        smilesDialog.pack();
        smilesDialog.addWindowListener(this);
    }

    public void addStyles(DefaultStyledDocument doc) {
        Style basic = doc.addStyle("basic", StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE));
        Font font = jic.getFont();
        StyleConstants.setFontFamily(basic, font.getFamily());
        StyleConstants.setFontSize(basic, font.getSize());
        if (font.isItalic())
            StyleConstants.setItalic(basic, true);
        if (font.isBold())
            StyleConstants.setBold(basic, true);
        Style regular = doc.addStyle("regular", basic);
        Style s;
        s = doc.addStyle("system", regular);
        StyleConstants.setItalic(s, true);
        StyleConstants.setForeground(s, Defaults.SYSTEM_FONT_COLOR);
        s = doc.addStyle("timestamp", regular);
        StyleConstants.setForeground(s, Defaults.SYSTEM_FONT_COLOR);

        for (String key : smileKeys) {
            s = doc.addStyle(key, regular);
            StyleConstants.setIcon(s, smiles.get(key));
        }
    }

    public void updateStyle(DefaultStyledDocument doc) {
        Style basic = doc.getStyle("basic");
        Font font = jic.getFont();
        StyleConstants.setFontFamily(basic, font.getFamily());
        StyleConstants.setFontSize(basic, font.getSize());
        if (font.isItalic())
            StyleConstants.setItalic(basic, true);
        if (font.isBold())
            StyleConstants.setBold(basic, true);
    }

    // Adds text said by user to dst
    public void addText(User user, String text, User dst) {
        try {
            DefaultStyledDocument doc = dst.getDoc();
            if (jic.isTimeStamping())
                doc.insertString(doc.getLength(), Util.getTimeStamp(jic.getTimeStampFormat()) + " ", doc.getStyle("timestamp"));
            doc.insertString(doc.getLength(), user.getUsername(), doc.getStyle("color_" + user.toString()));
            doc.insertString(doc.getLength(), ": ", doc.getStyle("regular"));
            addSmiledString(text, doc);
            doc.insertString(doc.getLength(), "\n", doc.getStyle("regular"));
            // Notify
            if (currentUser != dst) {
                dst.setNotifying(true);
                updateUserList();
            }
            if (((getExtendedState() & JFrame.ICONIFIED) != 0) || !isActive())
                setTrayBlink();
        } catch (BadLocationException e) {
            // TODO
            // This can not occur at all
        }
    }

    public void addUserStyle(User user, DefaultStyledDocument doc) {
        Style s = doc.addStyle("color_" + user, doc.getStyle("regular"));
        StyleConstants.setForeground(s, user.getColor());
    }

    public void updateUserStyle(User user, DefaultStyledDocument doc) {
        Style s = doc.getStyle("color_" + user);
        StyleConstants.setForeground(s, user.getColor());
    }

    public void addUser(User user) {
        insertUser(user);

    }

    public void removeUser(User user) {
        userListModel.removeElement(user);
    }


    private void addSmiledString(String text, DefaultStyledDocument doc) {
        Set<String> smileKeys = jic.getSmilesTable().keySet();
        try {
            if (!jic.isSmiling()) {
                doc.insertString(doc.getLength(), text, doc.getStyle("regular"));
                return;
            }
            int cur = 0;
            for (int i = 0; i < text.length(); i++) {
                for (String key : smileKeys) {
                    if (text.startsWith(key, i)) {
                        if (cur < i) {
                            doc.insertString(doc.getLength(), text.substring(cur, i), doc.getStyle("regular"));
                            cur = i;
                        }
                        doc.insertString(doc.getLength(), key, doc.getStyle(key));
                        cur += key.length();
                        i += key.length() - 1;
                        break;
                    }
                }
            }
            if (cur < text.length())
                doc.insertString(doc.getLength(), text.substring(cur, text.length()), doc.getStyle("regular"));
        } catch (BadLocationException e) {
            //TODO
        }
    }

    private void setStrings() {
        // Main title
        setTitle(getString("Frame.title"));

        // Topic of the channel
        setTopic();

        // Tool bar tooltips
        buttonClear.setToolTipText(getString("Menu.Chat.clear_desc"));
        buttonColor.setToolTipText(getString("Menu.Chat.choose_color_desc"));
        buttonNick.setToolTipText(getString("Menu.Chat.change_nick_desc"));
        buttonSmiles.setToolTipText(getString("Menu.Chat.insert_smile_desc"));
        buttonTopic.setToolTipText(getString("Menu.Chat.set_topic_desc"));

        // Menus
        menuFile.setText(getString("Menu.File"));
        menuFileClose.setText(getString("Menu.File.close"));
        menuFileClose.setToolTipText(getString("Menu.File.close_desc"));
        menuFileHide.setText(getString("Menu.File.hide"));
        menuFileHide.setToolTipText(getString("Menu.File.hide_desc"));
        menuFileSettings.setText(getString("Menu.File.settings"));
        menuFileSettings.setToolTipText(getString("Menu.File.settings_desc"));
        menuChat.setText(getString("Menu.Chat"));
        menuChatClear.setText(getString("Menu.Chat.clear"));
        menuChatClear.setToolTipText(getString("Menu.Chat.clear_desc"));
        menuChatColor.setText(getString("Menu.Chat.choose_color"));
        menuChatColor.setToolTipText(getString("Menu.Chat.choose_color_desc"));
        menuChatNick.setText(getString("Menu.Chat.change_nick"));
        menuChatNick.setToolTipText(getString("Menu.Chat.change_nick_desc"));
        menuChatTopic.setText(getString("Menu.Chat.set_topic"));
        menuChatTopic.setToolTipText(getString("Menu.Chat.set_topic_desc"));
        menuLanguage.setText(getString("Menu.Language"));
        menuHelp.setText(getString("Menu.Help"));
        menuHelpContents.setText(getString("Menu.Help.contents"));
        menuHelpContents.setToolTipText(getString("Menu.Help.contents_desc"));

    }

    public void setTopic() {
        if (currentUser == jic.getChannel()) {
            topicPanelBorder.setTitle(MessageFormat.format(getString("Frame.topic_is_set"), currentUser.getUsername()));
            String topic = jic.getTopic();
            topicLabel.setText(topic == null ? getString("Frame.no_topic") : topic);
        } else {
            topicPanelBorder.setTitle(getString("Frame.talking_to"));
            topicLabel.setText(currentUser.getUsername());
        }
        topicPanel.repaint();

    }

    private void insertText(String text) {
        messageField.insert(text, messageField.getCaretPosition());
    }

    public String getString(String key) {
        return jic.getString(key);
    }

    public void windowOpened(WindowEvent e) {
    }

    public void windowClosing(WindowEvent e) {
        onClose();
    }

    public void windowClosed(WindowEvent e) {
    }

    public void windowIconified(WindowEvent e) {
        if (tray != null && trayIcon != null)
            setVisible(false);
    }

    public void windowDeiconified(WindowEvent e) {
        setTrayNormal();
    }

    public void windowActivated(WindowEvent e) {
        setTrayNormal();
    }

    public void windowDeactivated(WindowEvent e) {
        if (e.getWindow() == smilesDialog)
            smilesDialog.setVisible(false);
    }

    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == buttonSmiles) {
            Point p = getMousePosition();
            smilesDialog.setLocation(p.x, p.y - smilesDialog.getHeight());
            smilesDialog.setVisible(true);
        } else if (src == buttonNick || src == menuChatNick) {
            changeNick();
        } else if (src == buttonColor || src == menuChatColor) {
            changeColor();
        } else if (src == menuChatClear || src == buttonClear) {
            clearChatScreen();
        } else if (src == buttonTopic || src == menuChatTopic) {
            changeTopic();
        } else if (src == buttonSettings || src == menuFileSettings) {
            settings = new SettingsDialog(this, jic);
            settings.setVisible(true);
        } else if (src == menuFileClose) {
            onClose();
        } else if (src == menuFileHide) {
            setExtendedState(getExtendedState() | JFrame.ICONIFIED);
        } else if (src == menuHelpAbout) {
            JOptionPane.showConfirmDialog(this, "Jic Chat\nVersion: " + Defaults.VERSION, getString("Menu.Help.about"), JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, new ImageIcon(getIconImage()));
        } else if (src == menuHelpCheck) {
            checkVersion();
        } else if (src == menuHelpContents) {
            showHelp();
        } else if (src == buttonFileTransfer) {
            prepareFileTtransfer();

        } else if (src == trayIcon) {
            setVisible(true);
            setState(JFrame.NORMAL);
        }


    }

    private void prepareFileTtransfer() {
        // TODO: Channel case
        if (currentUser.getType() == User.TYPE_CHANNEL)
            return;
        // TODO
        if (currentUser.getFileSender() != null)
            return;
        JFileChooser fc = new JFileChooser();
        fc.setMultiSelectionEnabled(true);
        fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fc.showDialog(this, "Send");
        File[] files = fc.getSelectedFiles();
        currentUser.setFileSender(new FileSender(jic, files, currentUser));
        jic.sendMessage(currentUser.getAddress(), Message.MA_FILE_PROPOSAL, currentUser.getFileSender().getPort());
        currentUser.getFileSender().start();

    }

    private void showHelp() {
        try {
            Desktop.getDesktop().browse(new URL(Defaults.URL_HELP).toURI());
        } catch (Throwable e) {
            // TODO
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private void checkVersion() {
        try {
            HttpURLConnection huc = (HttpURLConnection)new URL(Defaults.URL_VERSION).openConnection();
            if (huc.getResponseCode() != HttpURLConnection.HTTP_OK)
                JOptionPane.showConfirmDialog(this, "ERROR " + huc.getResponseCode(), getString("Menu.Help.about"), JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, new ImageIcon(getIconImage()));
            byte[] buf = new byte[10];
            int len = huc.getInputStream().read(buf);
            buf = Arrays.copyOf(buf, len);
            String s = new String(buf);
            JOptionPane.showConfirmDialog(this, "Current version: " + Defaults.VERSION + "\nLatest version: " + s, getString("Menu.Help.about"), JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, new ImageIcon(getIconImage()));
        } catch (Throwable e) {
            JOptionPane.showConfirmDialog(this, "ERROR. " + e.getMessage(), getString("Menu.Help.about"), JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, new ImageIcon(getIconImage()));
        }
    }

    public void componentResized(ComponentEvent e) {
        if (e.getComponent() == messageArea) {
            if (jic.isScrolling()) {
                messageSB.setValue(messageSB.getMaximum());
            }
        }
    }

    public void componentMoved(ComponentEvent e) {
    }

    public void componentShown(ComponentEvent e) {
    }

    public void componentHidden(ComponentEvent e) {
    }

    public void keyTyped(KeyEvent e) {
        if (e.getComponent() == messageField) {
            if (e.getKeyChar() == '\n' && e.getModifiers() == 0) {
                messageField.setText("");
            }
        }
    }

    public void keyPressed(KeyEvent e) {
        if (e.getComponent() == messageField) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER && e.getModifiersEx() == 0 && !messageField.getText().equals("")) {
                if (currentUser.getType() == User.TYPE_USER)
                    addText(jic.getMe(), messageField.getText(), currentUser);
                jic.sendText(messageField.getText(), currentUser);
                messageHistory.add(messageField.getText());
                currentMessage = messageHistory.size();
            } else
            if (e.getKeyCode() == KeyEvent.VK_UP && ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) == KeyEvent.CTRL_DOWN_MASK)) {
                currentMessage--;
                setCurrentMessage();
            } else
            if (e.getKeyCode() == KeyEvent.VK_DOWN && ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) == KeyEvent.CTRL_DOWN_MASK)) {
                currentMessage++;
                setCurrentMessage();
            }
        }
    }

    public void keyReleased(KeyEvent e) {
        if (e.getComponent() == messageField) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            }
        }
    }

    public void mouseClicked(MouseEvent e) {
        if (e.getComponent() == userList) {
            if (e.getClickCount() == 2) {
                setCurrentUser((User) userList.getSelectedValue());
            }
        }
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    private JMenuItem createMenuItem(String text, String icon, String desc, KeyStroke key) {
        JMenuItem mi = new JMenuItem(text);
        if (icon != null)
            mi.setIcon(new ImageIcon(getClass().getResource(icon)));
        mi.setToolTipText(desc);
        mi.setAccelerator(key);
        mi.addActionListener(this);
        return mi;
    }

    public class SmileAction extends AbstractAction {
        private String desc;

        public SmileAction(ImageIcon icon, String desc) {
            super(null, icon);
            this.desc = desc;
            putValue(SHORT_DESCRIPTION, desc);
        }

        public void actionPerformed(ActionEvent e) {
            insertText(desc);
            smilesDialog.setVisible(false);
            messageField.requestFocus();
        }
    }

    public class LanguageAction extends AbstractAction {
        int index;

        public LanguageAction(int index) {
            super();
            Locale loc = new Locale(Defaults.SUPPORTED_LANGUAGES[index]);

            putValue(NAME, Util.makeFirstCapital(loc.getDisplayLanguage(loc), loc));
            this.index = index;
        }

        public void actionPerformed(ActionEvent e) {
            jic.setLocaleIndex(index);
            setStrings();
            repaint();
        }
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
        if (currentUser.isNotifying()) {
            currentUser.setNotifying(false);
            updateUserList();
        }
        currentDoc = currentUser.getDoc();
        messageArea.setStyledDocument(currentDoc);
        userList.setSelectedValue(currentUser, true);
//        jic.setTopic(currentUser.getUsername());
        setTopic();
        messageField.requestFocus();
    }

    public void insertUser(User user) {
        int l = 0, r = userListModel.getSize() - 1, m;
        if (r < 0 || user.compareTo((User) userListModel.getElementAt(r)) < 0) {
            userListModel.addElement(user);
            return;
        }
        while (l < r - 1) {
            m = (l + r) >> 1;
            if (user.compareTo((User) userListModel.getElementAt(m)) > 0)
                l = m;
            else
                r = m;
        }
        userListModel.insertElementAt(user, r);
    }

    public void addSystemText(User dst, String text) {
        if (!jic.isShowSystemMessages())
            return;
        DefaultStyledDocument doc = dst.getDoc();
        try {
            if (jic.isTimeStamping())
                doc.insertString(doc.getLength(), Util.getTimeStamp(jic.getTimeStampFormat()) + " ", doc.getStyle("timestamp"));
            doc.insertString(doc.getLength(), text + "\n", doc.getStyle("system"));
        } catch (BadLocationException e) {
            // TODO
        }
    }

    public void updateUserList() {
        userList.repaint();
    }

    private void changeNick() {
        String newNick = (String) JOptionPane.showInputDialog(this, getString("Chat.input.change_nick.text"), getString("Chat.input.change_nick.title"), JOptionPane.QUESTION_MESSAGE, null, null, jic.getMe().getUsername());
        if (newNick != null && !newNick.trim().equals("")) {
            if (newNick.length() > Defaults.MAX_USERNAME_LENGTH)
                newNick = newNick.substring(0, Defaults.MAX_USERNAME_LENGTH);
            jic.sendMessageToAll(Message.MA_SET_USERNAME, newNick);
        }
        messageField.requestFocus();

    }

    private void changeColor() {
        Color color = JColorChooser.showDialog(this, getString("Chat.input.change_color.title"), jic.getMe().getColor());
        if (color != null)
            jic.sendMessageToAll(Message.MA_SET_COLOR, color);
        messageField.requestFocus();
    }

    private void clearChatScreen() {
        try {
            currentDoc.remove(0, currentDoc.getLength());
        } catch (BadLocationException e1) {
            //TODO
        }
    }

    public void setTrayIcon(Image icon) {
        if (tray == null || trayIcon == null)
            return;
//        tray.remove(trayIcon);
        trayIcon.setImage(icon);
        try {
            tray.add(trayIcon);
        } catch (Throwable e) {
            // TODO:
        }
    }

    public void setTrayNormal() {
        setTrayIcon(getIconImage());
    }

    public void setTrayBlink() {
        setTrayIcon(trayBlinkImage);
    }

    public void onClose() {
        int result = JOptionPane.showConfirmDialog(this, new JLabel("Do you really want to quit?"), "Confirm your choice", JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            if (settings != null)
                settings.dispose();
            jic.destroy();
            if (tray != null) {
                tray.remove(trayIcon);
            }
            dispose();
            System.exit(0);
        }
    }

    public void changeTopic() {
        String newTopic = (String) JOptionPane.showInputDialog(this, getString("Chat.input.change_topic.text"), getString("Chat.input.change_topic.title"), JOptionPane.QUESTION_MESSAGE, null, null, jic.getTopic());
        if (newTopic != null && !newTopic.trim().equals("")) {
            if (newTopic.length() > Defaults.MAX_TOPIC_LENGTH)
                newTopic = newTopic.substring(0, Defaults.MAX_TOPIC_LENGTH);
            jic.sendMessageToAll(Message.MA_SET_TOPIC, newTopic);
        }
        messageField.requestFocus();
    }

    private void setCurrentMessage() {
        int size = messageHistory.size();
        if (currentMessage < 0)
            currentMessage = 0;
        if (currentMessage > size)
            currentMessage = size;
        if (currentMessage == size)
            messageField.setText("");
        else
            messageField.setText(messageHistory.get(currentMessage));
    }


}