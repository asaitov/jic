package org.jic.settings;

import java.awt.*;

public class Defaults {

    public static final String VERSION = "0.01";

    public static final String PACKAGE_BASE = "/jic/";

    public static final String URL_BASE = "http://10.6.6.213/";

    public static final String URL_VERSION = URL_BASE + "version.php";
    public static final String URL_HELP = URL_BASE + "help.php";

    public static final String[] SUPPORTED_LANGUAGES = {"en", "ru"};
    public static final String BUNDLE_NAME = "jic";

    public static final int MULTICAST_PORT = 0xface;
    public static final int MULTICAST_TTL = 3;
    public static final int MAX_TTL = 10;
    public static final int MAX_USERNAME_LENGTH = 32;
    public static final int MAX_TOPIC_LENGTH = 32;
    public static final int MAX_USER_STATE_LENGTH = 32;
    public static final int MAX_MESSAGE_LENGTH = 10240;
    public static final int MAX_FILENAME_LENGTH = 1024;
    public static final int CONNECTION_TIMEOUT = 60000;

    public static final int USER_COLOR = new Color(255, 0, 0).getRGB();
    public static final Color NOTIFYING_COLOR = new Color(255, 247, 153);
    public static final Color NOTIFYING_SELECTED_COLOR = new Color(130, 202, 156);
    public static final int ICON_MAX_SIZE = 10240;
    public static final int ICON_WIDTH = 32;
    public static final int ICON_HEIGHT = 32;
    public static final String ICON_CHANNEL = "/images/i.gif";

    //    public static final String FONT_NAME = "Arial";
    public static final String FONT_FAMILY = "Tahoma";
    public static final int FONT_STYLE = 0;
    public static final int FONT_SIZE = 12;

    //    public static final String SYSTEM_FONT_NAME = "Arial";
    //    public static final String SYSTEM_FONT_FAMILY = "Arial";
    public static final int SYSTEM_FONT_STYLE = 0;
    public static final int SYSTEM_FONT_SIZE = 12;
    public static final Color SYSTEM_FONT_COLOR = new Color(192, 192, 192);

    public static final String MULTICAST_ADDRESS = "234.0.0.1";
    public static final String TITLE = "Jic";
    public static final String ACTION_BREAK = "ACTION_BREAK";
    public static final String ACTION_SEND = "ACTION_SEND";
    public static final int NETWORK_DELAY = 50;
    public static final int BUFFER_LENGTH = 60000;
    public static final int USERLIST_WIDTH = 200;
    public static final String ICON_USER = "/images/k.gif";
    public static final String SMILES_PATH = "/images/smiles/";
    public static final String SMILES_DESC_FILE = "/images/smiles/smiles.properties";

    public static final String CHANNEL_NAME = "PUBLIC";

    public static final String ICON_SMILES_NORMAL = "/images/e.gif";
    public static final String ICON_SMILES_ROLLOVER = "/images/d.gif";
    public static final String ICON_SMILES_DOWN = "/images/h.gif";

    public static final String ICON_NICK_MENU = "/images/menu-nick3.gif";
    public static final String ICON_NICK_NORMAL = "/images/nick_n.gif";
    public static final String ICON_NICK_ROLLOVER = "/images/nick_r.gif";
    public static final String ICON_NICK_DOWN = "/images/nick_d.gif";

    public static final String ICON_COLOR_MENU = "/images/menu-color.gif";
    public static final String ICON_COLOR_NORMAL = "/images/color_n2.gif";
    public static final String ICON_COLOR_ROLLOVER = "/images/color_r2.gif";
    public static final String ICON_COLOR_DOWN = "/images/color_d2.gif";

    public static final String ICON_TOPIC_MENU = "/images/menu-topic.gif";
    public static final String ICON_TOPIC_NORMAL = "/images/topic_n4.gif";
    public static final String ICON_TOPIC_ROLLOVER = "/images/topic_r4.gif";
    public static final String ICON_TOPIC_DOWN = "/images/topic_d4.gif";

    public static final String ICON_SETTINGS_MENU = "/images/menu-settings3.gif";
    public static final String ICON_SETTINGS_NORMAL = "/images/settings-n2.gif";
    public static final String ICON_SETTINGS_ROLLOVER = "/images/settings-r2.gif";
    public static final String ICON_SETTINGS_DOWN = "/images/settings-d2.gif";

    public static final String ICON_CLEAR_NORMAL = "/images/button.gif";
    public static final String ICON_CLEAR_ROLLOVER = "/images/button.gif";
    public static final String ICON_CLEAR_DOWN = "/images/buttondown.gif";

    public static final String ICON_NORMAL = "/images/button.gif";
    public static final String ICON_ROLLOVER = "/images/button.gif";
    public static final String ICON_DOWN = "/images/buttondown.gif";

    public static final String PLUS_NORMAL = "/images/button_plus_n.gif";
    public static final String PLUS_ROLLOVER = "/images/button_plus_r.gif";
    public static final String PLUS_DOWN = "/images/button_plus_d.gif";

    public static final String MINUS_NORMAL = "/images/button_minus_n.gif";
    public static final String MINUS_ROLLOVER = "/images/button_minus_r.gif";
    public static final String MINUS_DOWN = "/images/button_minus_d.gif";

    public static final String ICON_HELP_MENU = "/images/menu-help.gif";



    public static final int SOCKET_TTL = 3;
    public static final int REFRESH_INTERVAL = 5;

    public static final String SETTINGS_FILE = "settings.properties";

    public static String CHAT_SCROLLING = "true";
    public static String CHAT_TIME_STAMPING = "true";
    public static String CHAT_SMILING = "true";
    public static String CHAT_SHOW_SYSTEM_MESSAGES = "true";

    public static String FILES_LOGGING = "true";

    public static String TIME_STAMP = "[%T]";

    public static long TIME_OUT = 10000;
    public static String APP_ICON = "/images/mainicon.gif";
    public static String APP_ICON_BLINK = "/images/mainiconblink.gif";


}
