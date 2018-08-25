package org.jic.core;

import org.jic.ui.MainFrame;
import org.jic.bean.User;
import org.jic.util.Util;
import org.jic.core.MessageReciever;
import org.jic.settings.Defaults;
import org.jic.Message;
import org.jic.file.FileReciever;

import javax.swing.*;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;
import java.util.Timer;
import java.text.MessageFormat;

public class Jic implements Runnable {

    // Settings
    private boolean scrolling;
    private boolean timeStamping;
    private boolean smiling;
    private boolean logging;
    private boolean showSystemMessages;
    private String timeStampFormat;
    private int refreshInterval;
    private int localeIndex;
    private Vector<String> states;
    // Icons
    private byte[] iconData;
    private ImageIcon channelIcon;
    private String userIconPath;
    private String channelIconPath;

    private MulticastSocket socket;
    private Font font;
    // Real connection settings
    private InetAddress socketGroup;
    private int port;
    private int ttl;
    // Temporary connection settings
    private String optsAddress;
    private int optsPort;
    private int optsTtl;

    // Main window
    private MainFrame frame;
    // Message messageReciever
    private MessageReciever messageReciever;
    // Me
    private User me;
    // Channel PUBLIC
    private User channel;
    // Default icon for channels
    private ImageIcon defaultChannelIcon;
    private ImageIcon defaultUserIcon;
    private byte[] defaultUserIconData;
    private Hashtable<InetAddress, User> userlist;
    // User list refresher
    private Timer refresher;
    // Channel topic
    private String topic;

    private Properties properties;
    private ResourceBundle bundle;

    Hashtable<String, ImageIcon> smilesTable;
    Vector<String> smilesVector;

    // Default styles for styled document
    StyleContext styles;


    public Jic() {
        loadSettings();
        socket = createMulticastSocket();
        if (socket == null)
            die("Couldn't create socket");
        messageReciever = new MessageReciever(this);
        userlist = new Hashtable<InetAddress, User>();
        channel = createChannel(Defaults.CHANNEL_NAME, socketGroup);
        frame = new MainFrame(this);
        frame.setCurrentUser(channel);
        addUser(me);
        addUser(channel);
        refresher = new Timer();
    }

    private MulticastSocket createMulticastSocket() {
        MulticastSocket socket;

        // Creating socket
        try {
            socket = new MulticastSocket(port);
        } catch (IOException e) {
            try {
                port = Defaults.MULTICAST_PORT;
                socket = new MulticastSocket(port);
            } catch (IOException e1) {
                return null;
            }
        }

        // Setting options
        try {
            socket.setLoopbackMode(false);
            socket.setReuseAddress(true);
            socket.setInterface(me.getAddress());
            socket.setSoTimeout(0);//(Defaults.NETWORK_DELAY);
            socket.setTimeToLive(ttl);
        } catch (IOException e) {
            return null;
        }

        // Joining multicast group
        try {
            socket.joinGroup(socketGroup);
        } catch (IOException e) {
            try {
                socketGroup = InetAddress.getByName(Defaults.MULTICAST_ADDRESS);
                socket.joinGroup(socketGroup);
            } catch (IOException e1) {
                return null;
            }
        }
        return socket;
    }

    public MulticastSocket getSocket() {
        return socket;
    }

    public void run() {
        me.setConnectedSince(Calendar.getInstance().getTime());
        me.setLastActivity(System.currentTimeMillis());
        messageReciever.start();
        sendMessage(socketGroup, Message.MA_INFO, null);
        sendMessage(socketGroup, Message.MA_GET_INFO, null);
        frame.setVisible(true);
        frame.setTrayNormal();
        refresher.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                refreshUserList();
            }
        }, refreshInterval * 1000, refreshInterval * 1000);
    }

    public void dispatchMessage(Message message) {
        if (message == null)
            return;
        User user;
        if (message.getAction() == Message.MA_GET_INFO) {
            sendMessage(message.getAddress(), Message.MA_INFO, null);
            return;
        } else if (message.getAction() == Message.MA_INFO) {
            if (!userlist.containsKey(message.getAddress())) {
                user = new User(new DefaultStyledDocument(styles));
                user.setAddress(message.getAddress());
                user.setColor(message.getColor());
                user.setIcon(new ImageIcon(message.getIconData()));
                user.setUsername(message.getUsername());
                user.setLastActivity(System.currentTimeMillis());
                user.setConnectedSince(message.getDate());
                addUser(user);
            }
            return;
        }

        if (!userlist.containsKey(message.getAddress())) {
            sendMessage(message.getAddress(), Message.MA_GET_INFO, null);
            return;
        }

        user = userlist.get(message.getAddress());
        user.setLastActivity(System.currentTimeMillis());
        switch (message.getAction()) {
            case Message.MA_MESSAGE:
                if (message.getType() == Message.MT_COMMON)
                    frame.addText(user, message.getText(), userlist.get(socketGroup));
                else
                    frame.addText(user, message.getText(), user);
                break;
            case Message.MA_LEFT:
                removeUser(user);
                break;
            case Message.MA_GET_INFO:
                sendMessage(message.getAddress(), Message.MA_INFO, null);
                break;
            case Message.MA_SET_USERNAME:
                frame.addSystemText(userlist.get(socketGroup), MessageFormat.format(getString("Chat.message.change_nick"), user.getUsername(), message.getUsername()));
                user.setUsername(message.getUsername());
                frame.updateUserList();
                break;
            case Message.MA_SET_TOPIC:
                frame.addSystemText(userlist.get(socketGroup), MessageFormat.format(getString("Chat.message.change_topic"), user.getUsername(), message.getText()));
                setTopic(message.getText());
                frame.setTopic();
                break;
            case Message.MA_SET_STATE:
                frame.addSystemText(userlist.get(socketGroup), MessageFormat.format(getString("Chat.message.change_state"), user.getUsername(), message.getText()));
                user.setState(message.getText());
                frame.updateUserList();
                break;
            case Message.MA_SET_COLOR:
                user.setColor(message.getColor());
                updateStyle(user);
                frame.updateUserList();
                frame.addSystemText(userlist.get(socketGroup), MessageFormat.format(getString("Chat.message.change_color"), user.getUsername()));
                break;
            case Message.MA_SET_ICON:
                user.setIcon(new ImageIcon(message.getIconData()));
                frame.updateUserList();
                break;
            case Message.MA_WHO_IS_HERE:
                sendMessage(message.getAddress(), Message.MA_I_AM_HERE, null);
                break;
            case Message.MA_FILE_PROPOSAL:
                if (user.getFileReciever() != null) {
                    // TODO
                    break;
                }
                Socket socket = null;
                try {
                    socket = new Socket(user.getAddress(), message.getPort());
                    user.setFileReciever(new FileReciever(this, user, socket));
                    user.getFileReciever().start();
                } catch (IOException e) {
                    // TODO
                    break;
                }
                break;

            default:
                break;
        }
    }

    public void sendMessageToAll(byte action, Object param) {
        sendMessage(socketGroup, action, param);
    }

    public void sendMessage(InetAddress dst, byte action, Object param) {
        Message msg = new Message();
        msg.setAction(action);

        if (dst == socketGroup)
            msg.setType(Message.MT_COMMON);
        else
            msg.setType(Message.MT_PRIVATE);

        switch (action) {
            case Message.MA_INFO:
                msg.setDate(me.getConnectedSince());
                msg.setText(me.getState());
                msg.setColor(me.getColor());
                msg.setIconData(iconData);
                msg.setUsername(me.getUsername());
                break;
            case Message.MA_SET_COLOR:
                msg.setColor((Color) param);
                break;
            case Message.MA_SET_USERNAME:
                msg.setUsername((String) param);
                break;
            case Message.MA_SET_TOPIC:
                msg.setText((String) param);
                break;
            case Message.MA_SET_STATE:
                msg.setText((String) param);
                break;
            case Message.MA_SET_ICON:
                msg.setIconData(iconData);
                break;
            case Message.MA_FILE_PROPOSAL:
                msg.setPort((Integer)param);
                break;
            case Message.MA_MESSAGE:
                msg.setText((String) param);
                break;
            case Message.MA_GET_INFO:
                break;
            case Message.MA_GET_COLOR:
                break;
            case Message.MA_GET_USERNAME:
                break;
            case Message.MA_GET_ICON:
                break;
            case Message.MA_WHO_IS_HERE:
                break;
            case Message.MA_I_AM_HERE:
                break;
            case Message.MA_LEFT:
                break;
            case Message.MA_NOTHING:
                break;
            default:
        }

        DatagramPacket packet = msg.createPacket(dst, port);
        try {
            socket.send(packet);
        } catch (IOException e) {
            // TODO
            e.printStackTrace();
        }
    }

    public void sendText(String text, User dst) {
        if (!text.equals(""))
            sendMessage(dst.getAddress(), Message.MA_MESSAGE, text);

    }

    public void destroy() {
        sendMessage(socketGroup, Message.MA_LEFT, null);
        saveSettings();
        messageReciever.setListening(false);
        refresher.cancel();
        refresher.purge();
    }

    public void addUser(User user) {
        if (user.getType() == User.TYPE_USER && user != me)
            if (!userlist.containsKey(user.getAddress()))
                frame.addSystemText(userlist.get(socketGroup), MessageFormat.format(getString("Chat.message.come"), user.getUsername()));
        userlist.put(user.getAddress(), user);
        frame.addUser(user);
//        frame.addStyles(user.getDoc());
        updateStyle(user);
    }

    public void updateStyle(User user) {
        if (user.getType() == User.TYPE_CHANNEL)
            for (User u : userlist.values())
                frame.addUserStyle(u, user.getDoc());
        else {
            frame.addUserStyle(me, user.getDoc());
            for (User u : userlist.values())
                if (u.getType() == User.TYPE_CHANNEL || u == user)
                    frame.addUserStyle(user, u.getDoc());
        }
    }


    public void removeUser(User user) {
        frame.addSystemText(userlist.get(socketGroup), MessageFormat.format(getString("Chat.message.left"), user.getUsername()));
        userlist.remove(user.getAddress());
        frame.removeUser(user);
    }

    public void setLocaleIndex(int localeIndex) {
        this.localeIndex = localeIndex;
        if (localeIndex < Defaults.SUPPORTED_LANGUAGES.length) {
            Locale.setDefault(new Locale(Defaults.SUPPORTED_LANGUAGES[localeIndex]));
            bundle = ResourceBundle.getBundle(Defaults.BUNDLE_NAME, Locale.getDefault());
        } else
            bundle = null;
        setColorChooserStrings();
        setFileChooserStrings();
    }

    public String getString(String key) {
        if (bundle == null)
            return "";
        else
            try {
                return new String(bundle.getString(key).getBytes("ISO-8859-1"));
            } catch (UnsupportedEncodingException e) {
                return "";
            }
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public User createChannel(String name, InetAddress address) {
        User channel = new User(new DefaultStyledDocument(styles));
        channel.setType(User.TYPE_CHANNEL);
        channel.setAddress(address);
        channel.setUsername(name);
        channel.setState("channel");
        channel.setIcon(channelIcon);
        return channel;
    }

    public User getMe() {
        return me;
    }

    private void loadSmiles() {
        smilesVector = new Vector<String>();
        BufferedReader in = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(Defaults.SMILES_DESC_FILE)));
        String line;
        smilesTable = new Hashtable<String, ImageIcon>();
        try {
            while ((line = in.readLine()) != null) {
                String tokens[] = line.split("\\s+");
                if (tokens.length < 2)
                    continue;
                URL url = getClass().getResource(Defaults.SMILES_PATH + tokens[0]);
                if (url == null)
                    continue;
                ImageIcon icon = new ImageIcon(url);
                smilesVector.add(tokens[1]);
                for (int i = 1; i < tokens.length; i++) {
                    smilesTable.put(tokens[i], icon);
                }
            }
        } catch (IOException e) {
            // TODO
        }

        Style s;
        Set<String> smileKeys = smilesTable.keySet();
        for (String key : smileKeys) {
            s = styles.addStyle(key, null);
            StyleConstants.setIcon(s, smilesTable.get(key));
        }
    }


    public void loadSettings() {
        ImageIcon icon;

        properties = new Properties();
        try {
            properties.load(new FileInputStream(Defaults.SETTINGS_FILE));
        } catch (IOException e) {
            // TODO
        }

        // Loading port
        port = getInt("Connection.port", Defaults.MULTICAST_PORT);
        if (port < 0 || port > 0xffff)
            port = Defaults.MULTICAST_PORT;

        // Loading multicast socket group
        try {
            socketGroup = InetAddress.getByName(properties.getProperty("Connection.address", Defaults.MULTICAST_ADDRESS));
        } catch (UnknownHostException e) {
            die("Can't resolve host");
        }

        // Loading Connection TTL
        ttl = getInt("Connection.ttl", Defaults.MULTICAST_TTL);
        if (ttl < 1 || ttl > Defaults.MAX_TTL)
            ttl = Defaults.MULTICAST_TTL;

        // Saving temporary connection settings
        optsAddress = socketGroup.getHostAddress();
        optsPort = port;
        optsTtl = ttl;

        // Loading chat font
        String fontFamily = properties.getProperty("Chat.font.name", Defaults.FONT_FAMILY);
        int fontSize = getInt("Chat.font.size", Defaults.FONT_SIZE);
        int fontStyle = getInt("Chat.font.style", Defaults.FONT_STYLE);
        font = new Font(fontFamily, fontStyle, fontSize);

        // Making default styles
        makeStyles();

        // Loading smiles and putting them in styles
        loadSmiles();

        // Loading user states
        states = new Vector<String>();
        states.add("");
        int i = 1;
        String state;
        while (true) {
            state = properties.getProperty("User.state." + i);
            if (state == null)
                break;
            states.add(state);
            i++;
        }

        // Loading user settings
        me = new User(new DefaultStyledDocument(styles));

        // Loading user localhost
        try {
            LinkedList<InetAddress> list = new LinkedList<>();
            for (NetworkInterface nif : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                for (InetAddress inetAddress : Collections.list(nif.getInetAddresses())) {
                    if (inetAddress.isSiteLocalAddress()) {
                        list.add(inetAddress);
                    }
                }
            }


            me.setAddress(list.getLast());
        } catch (Exception e) {
            die(e.toString());
        }

        // Loading user name
        me.setUsername(properties.getProperty("User.name", me.getAddress().getHostName()));

        // Loading user color
        me.setColor(new Color(getInt("User.color", Defaults.USER_COLOR)));

        // Loading user state
        me.setState(properties.getProperty("User.state", ""));

        // Loading default channel and user icons
        try {
            defaultChannelIcon = new ImageIcon(getClass().getResource(Defaults.ICON_CHANNEL));
            if (!Util.isSuitableImage(0, defaultChannelIcon.getIconWidth(), defaultChannelIcon.getIconHeight()))
                die("Too big default channel icon size");
            defaultUserIconData = new byte[Defaults.BUFFER_LENGTH];
            int len = getClass().getResourceAsStream(Defaults.ICON_USER).read(defaultUserIconData);
            defaultUserIconData = Arrays.copyOf(defaultUserIconData, len);

            defaultUserIcon = new ImageIcon(defaultUserIconData);
            if (!Util.isSuitableImage(len, defaultUserIcon.getIconWidth(), defaultUserIcon.getIconHeight()))
                die("Too big default user icon size");
        } catch (Throwable e) {
            die(e.toString());
        }

        // Loading user icon
        userIconPath = properties.getProperty("User.icon_path", "");
        iconData = new byte[Defaults.BUFFER_LENGTH];
        try {
            FileInputStream is = new FileInputStream(userIconPath);
            int len = is.read(iconData);
            iconData = Arrays.copyOf(iconData, len);
            icon = new ImageIcon(iconData);
            if (Util.isSuitableImage(len, icon.getIconWidth(), icon.getIconHeight()))
                me.setIcon(icon);
            else
                setDefaultUserIcon();
        } catch (Throwable e) {
            setDefaultUserIcon();
        }

        //Loading channel icon
        channelIconPath = properties.getProperty("Chat.channel.icon_path", "");
        try {
            channelIcon = new ImageIcon(channelIconPath);
            if (!Util.isSuitableImage(0, channelIcon.getIconWidth(), channelIcon.getIconHeight())) {
                channelIcon = defaultChannelIcon;
                channelIconPath = "";
            }
        } catch (Throwable e) {
            channelIcon = defaultChannelIcon;
            channelIconPath = "";
        }

        // Loading booleans
        logging = Boolean.parseBoolean(properties.getProperty("Files.logging", Defaults.FILES_LOGGING));
        timeStamping = Boolean.parseBoolean(properties.getProperty("Chat.time_stamping", Defaults.CHAT_TIME_STAMPING));
        smiling = Boolean.parseBoolean(properties.getProperty("Chat.smiling", Defaults.CHAT_SMILING));
        scrolling = Boolean.parseBoolean(properties.getProperty("Chat.scrolling", Defaults.CHAT_SCROLLING));
        showSystemMessages = Boolean.parseBoolean(properties.getProperty("Chat.show_system_messages", Defaults.CHAT_SHOW_SYSTEM_MESSAGES));

        // Loading time stamp format
        timeStampFormat = properties.getProperty("Chat.time_stamp", Defaults.TIME_STAMP);

        // Loading refresh user list interval
        refreshInterval = getInt("Chat.refresh_interval", Defaults.REFRESH_INTERVAL);

        // Loading locale index
        localeIndex = getInt("Locale", 0);
        if (localeIndex < 0 || localeIndex >= Defaults.SUPPORTED_LANGUAGES.length)
            localeIndex = 0;
        setLocaleIndex(localeIndex);

        // TODO: anything else?
    }

    private void makeStyles() {
        styles = new StyleContext();
        Style basic = styles.addStyle("basic", StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE));
        StyleConstants.setFontFamily(basic, font.getFamily());
        StyleConstants.setFontSize(basic, font.getSize());
        if (font.isItalic())
            StyleConstants.setItalic(basic, true);
        if (font.isBold())
            StyleConstants.setBold(basic, true);
        Style regular = styles.addStyle("regular", basic);
        Style s;
        s = styles.addStyle("system", regular);
        StyleConstants.setItalic(s, true);
        StyleConstants.setForeground(s, Defaults.SYSTEM_FONT_COLOR);
        s = styles.addStyle("timestamp", regular);
        StyleConstants.setForeground(s, Defaults.SYSTEM_FONT_COLOR);
    }


    private int getInt(String key, int def) {
        try {
            return Integer.parseInt(properties.getProperty(key, String.valueOf(def)));
        }
        catch (NumberFormatException e) {
            return def;
        }
    }

    private void die(String error) {
        System.out.println(error);
        System.exit(0);
    }


    public void saveSettings() {

        properties = new Properties();

        // Connection settings
        properties.put("Connection.port", String.valueOf(optsPort));
        properties.put("Connection.address", optsAddress);
        properties.put("Connection.ttl", String.valueOf(optsTtl));

        // User settings
        properties.put("User.name", me.getUsername());
        properties.put("User.color", String.valueOf(me.getColor().getRGB()));
        properties.put("User.icon_path", userIconPath);
        for (int i = 1; i < states.size(); i++) {
            properties.put("User.state." + i, states.get(i));
        }
        properties.put("User.state", me.getState());

        // Chat settings
        properties.put("Chat.channel.icon_path", channelIconPath);
        properties.put("Chat.font.name", font.getFamily());
        properties.put("Chat.font.size", String.valueOf(font.getSize()));
        properties.put("Chat.font.style", String.valueOf(font.getStyle()));
        properties.put("Chat.time_stamp", String.valueOf(timeStampFormat));
        properties.put("Chat.refresh_interval", String.valueOf(refreshInterval));

        // Booleans
        properties.put("Chat.time_stamping", String.valueOf(timeStamping));
        properties.put("Chat.smiling", String.valueOf(smiling));
        properties.put("Chat.scrolling", String.valueOf(scrolling));
        properties.put("Chat.show_system_messages", String.valueOf(showSystemMessages));
        properties.put("Files.logging", String.valueOf(showSystemMessages));

        // Locale
        properties.put("Locale", String.valueOf(localeIndex));

        try {
            properties.store(new FileOutputStream(Defaults.SETTINGS_FILE), "Jic Chat settings");
        } catch (Throwable e) {
            // TODO
        }

    }

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        this.font = font;
        for (User user : userlist.values()) {
            frame.updateStyle(user.getDoc());
//            frame.updateUserStyle(user, user.getDoc());
        }
    }


    public int getRefreshInterval() {
        return refreshInterval;
    }

    public void setRefreshInterval(int refreshInterval) {
        this.refreshInterval = refreshInterval;
        if (refresher != null) {
            refresher.cancel();
            refresher.purge();
        }
        refresher = new Timer();
        refresher.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                refreshUserList();
            }
        }, refreshInterval * 1000, refreshInterval * 1000);
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public InetAddress getSocketGroup() {
        return socketGroup;
    }

    public void setSocketGroup(InetAddress socketGroup) {
        this.socketGroup = socketGroup;
    }

    public int getTtl() {
        return ttl;
    }

    public void setTtl(int ttl) {
        this.ttl = ttl;
    }

    public User getChannel() {
        return channel;
    }

    public void setChannel(User channel) {
        this.channel = channel;
    }


    public String getUserIconPath() {
        return userIconPath;
    }

    public void setUserIconPath(String userIconPath) {
        FileInputStream is;
        try {
            is = new FileInputStream(userIconPath);
            iconData = new byte[Defaults.BUFFER_LENGTH];
            int len = is.read(iconData);
            iconData = Arrays.copyOf(iconData, len);
            this.userIconPath = userIconPath;
        } catch (Throwable e) {
            // TODO
        }
    }

    public void setDefaultUserIcon() {
        userIconPath = "";
        iconData = defaultUserIconData;
        me.setIcon(defaultUserIcon);
    }

    public void setChannelIcon(ImageIcon icon) {
        if (icon == null)
            channelIconPath = "";
        channel.setIcon(icon == null ? defaultChannelIcon : icon);
    }

    private void setFileChooserStrings() {
        UIDefaults def = UIManager.getDefaults();
        String[] strings = {
                "FileChooser.lookInLabelText", "FileChooser.fileNameLabelText",
                "FileChooser.filesOfTypeLabelText",
                "FileChooser.upFolderToolTipText", "FileChooser.homeFolderToolTipText",
                "FileChooser.newFolderToolTipText", "FileChooser.listViewButtonToolTipText",
                "FileChooser.detailsViewButtonToolTipText", "FileChooser.directoryOpenButtonText",
                "FileChooser.directoryOpenButtonToolTipText", "FileChooser.cancelButtonText",
                "FileChooser.cancelButtonToolTipText", "FileChooser.approveButtonText",
                "FileChooser.approveButtonToolTipText", "FileChooser.newFolderErrorText",
                "FileChooser.openButtonText", "FileChooser.openButtonToolTipText"
        };
        for (String s : strings)
            def.put(s, getString(s));
    }

    private void setColorChooserStrings() {
        UIDefaults def = UIManager.getDefaults();
        String[] strings = {
                "ColorChooser.previewText", "ColorChooser.sampleText",
                "ColorChooser.swatchesNameText", "ColorChooser.swatchesRecentText",
                "ColorChooser.rgbNameText", "ColorChooser.rgbRedText",
                "ColorChooser.rgbGreenText", "ColorChooser.rgbBlueText",
                "ColorChooser.hsbRedText", "ColorChooser.hsbGreenText",
                "ColorChooser.hsbBlueText", "ColorChooser.hsbHueText",
                "ColorChooser.hsbSaturationText", "ColorChooser.hsbBrightnessText",
                "ColorChooser.hsbNameText", "ColorChooser.okText",
                "ColorChooser.cancelText", "ColorChooser.resetText"
        };
        for (String s : strings)
            def.put(s, getString(s));
    }


    public boolean isScrolling() {
        return scrolling;
    }

    public void setScrolling(boolean scrolling) {
        this.scrolling = scrolling;
    }

    public boolean isTimeStamping() {
        return timeStamping;
    }

    public void setTimeStamping(boolean timeStamping) {
        this.timeStamping = timeStamping;
    }

    public boolean isSmiling() {
        return smiling;
    }

    public void setSmiling(boolean smiling) {
        this.smiling = smiling;
    }

    public String getTimeStampFormat() {
        return timeStampFormat;
    }

    public void setTimeStampFormat(String timeStampFormat) {
        this.timeStampFormat = timeStampFormat;
    }

    public ImageIcon getDefaultChannelIcon() {
        return defaultChannelIcon;
    }


    public ImageIcon getDefaultUserIcon() {
        return defaultUserIcon;
    }


    public String getChannelIconPath() {
        return channelIconPath;
    }

    public void setChannelIconPath(String channelIconPath) {
        this.channelIconPath = channelIconPath;
    }


    public int getLocaleIndex() {
        return localeIndex;
    }

    synchronized public void refreshUserList() {
        long time = System.currentTimeMillis();
// ConcurrentModification
        
        for (User user : userlist.values())
            if (user.getType() == User.TYPE_USER && time - user.getLastActivity() > Defaults.TIME_OUT + Defaults.REFRESH_INTERVAL)
                removeUser(user);
        sendMessage(socketGroup, Message.MA_WHO_IS_HERE, null);
    }


    public boolean isShowSystemMessages() {
        return showSystemMessages;
    }

    public void setShowSystemMessages(boolean showSystemMessages) {
        this.showSystemMessages = showSystemMessages;
    }

    public boolean isLogging() {
        return logging;
    }

    public void setLogging(boolean logging) {
        this.logging = logging;
    }


    public void setOptsTtl(int optsTtl) {
        this.optsTtl = optsTtl;
    }

    public void setOptsPort(int optsPort) {
        this.optsPort = optsPort;
    }

    public void setOptsAddress(String optsAddress) {
        this.optsAddress = optsAddress;
    }

    public Hashtable<String, ImageIcon> getSmilesTable() {
        return smilesTable;
    }

    public Vector<String> getSmilesVector() {
        return smilesVector;
    }

    public Vector<String> getStates() {
        return states;
    }

    public void setStates(Vector<String> states) {
        this.states = states;
    }
}

