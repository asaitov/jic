package org.jic.util;

import org.jic.settings.Defaults;

import javax.swing.*;
import java.awt.*;
import java.util.Calendar;
import java.util.Locale;

public class Util {
    private static CharSequence[][] symbols = {
            {"\"", "&quot;"},
            {"&", "&amp;"},
            {"<", "&lt;"},
            {">", "&gt;"}
/*
            {"?", "&OElig;"},
            {"?", "&oelig;"},
            {"?", "&Scaron;"},
            {"?", "&scaron;"},
            {"?", "&Yuml;"},
            {"?", "&circ;"},
            {"?", "&tilde;"},
            {"�", "&ndash;"},
            {"�", "&mdash;"},
            {"�", "&lsquo;"},
            {"�", "&rsquo;"},
            {"�", "&sbquo;"},
            {"�", "&ldquo;"},
            {"�", "&rdquo;"},
            {"�", "&bdquo;"},
            {"�", "&dagger;"},
            {"�", "&Dagger;"},
            {"�", "&permil;"},
            {"�", "&lsaquo;"},
            {"�", "&rsaquo;"},
            {"�", "&euro;"}
*/
    };


    public static String escapeSpecialHTMLChars(String s) {
        String res = s;
        for (CharSequence[] sym: symbols)
            res = res.replace(sym[0], sym[1]);
        return res;
    }

    public static void setPrefferedWidth(JComponent c, int w) {
        c.setPreferredSize(new Dimension(w, c.getPreferredSize().height));
    }

    public static void setPrefferedHeight(JComponent c, int h) {
        c.setPreferredSize(new Dimension(c.getPreferredSize().width, h));
    }

    public static void suppress(JComponent c) {
        c.setMaximumSize(c.getPreferredSize());
    }

    public static boolean isSuitableImage(long size, int w, int h) {
        return (size <= Defaults.ICON_MAX_SIZE && w > 0 && h > 0 && w <= Defaults.ICON_WIDTH && h <= Defaults.ICON_HEIGHT);
    }

    public static String getTimeStamp(String format) {
        try {
            return String.format(format.replace("%", "%1$t"), Calendar.getInstance());
        } catch (Throwable e) {
            return format;
        }
    }

    public static boolean eq(String a, String b) {
        if (a == null)
            return b == null;
        else
            return a.equals(b);
    }

    public static String makeFirstCapital(String s, Locale l) {
        return s.substring(0, 1).toUpperCase(l) + s.substring(1).toLowerCase(l);
    }

    public static Color middleColor(Color c1, Color c2) {
        return new Color((c1.getRed() + c2.getRed()) / 2, (c1.getGreen() + c2.getGreen()) / 2, (c1.getBlue() + c2.getBlue()) / 2);
    }

}
