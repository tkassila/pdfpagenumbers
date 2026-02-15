package com.metait.write.pagenumbers.pdf.pdfpagenumbers.cmdline;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class PrintLogicPosition {
    private final PRINTTYPE printType;
    private final PRINTPOSITION PRINTPOSITION;
    private String  strText = "";
    private final Calendar printCalendar;
    private final int iStartPage;
    private final int iEngPage;
    private final int iStartPageNumber;
    private final String cmdLineStart;
    private boolean bTimetextonly = false;

    public PrintLogicPosition(final PRINTTYPE p_printType,
                              final PRINTPOSITION p_PRINTPOSITION, final String  p_strText,
                              final  Calendar p_printCalendar,
                              final int p_iStartPage, final int p_iEngPage, final int p_iStartPageNumber,
                              final boolean p_bTimetextonly
    )
    {
        printType = p_printType;
        PRINTPOSITION = p_PRINTPOSITION;
        printCalendar = p_printCalendar;
        strText = p_strText;
        iStartPage = p_iStartPage;
        iEngPage = p_iEngPage;
        iStartPageNumber = p_iStartPageNumber;
        cmdLineStart = getCmdLineStart(p_printType);
        bTimetextonly = p_bTimetextonly;
    }

    public final boolean getTimeTextOnly()
    {
        return bTimetextonly;
    }

    public final Calendar getCalendar() {
        return printCalendar;
    }

    public String getTextAndCalendarString()
    {
        String ret = "";
        if (strText != null)
            ret = strText;
        if (printCalendar != null) {
            Locale current = new Locale("fi","FI");
            SimpleDateFormat sdf =new SimpleDateFormat("dd.MM.YYYY HH:MM", current);
            String dateString = sdf.format(printCalendar.getTime());
            ret += " " +dateString;
            ret = ret.trim();
        }
        return ret;
    }

    public static String getCmdLineStart(PRINTTYPE type)
    {
        String ret = "";
        if (type == PRINTTYPE.PRINTPAGENUMBERS)
            return "pagenumbers_";
        else
        if (type == PRINTTYPE.PRINTEXTRATXT)
            return "printtext_";
        else
        if (type == PRINTTYPE.PRINTTIME)
            return "printtime_";

        return ret;
    }

    public final String getCommandType(){
        return printType.toString().toLowerCase();
    }

    public final PRINTTYPE getPrintType() { return printType; }
    public final PRINTPOSITION getPrintPosition() { return PRINTPOSITION; }
    public final String getText() {
        if (strText == null || strText.trim().isEmpty())
            return "";
        return strText; }
    public final String getCmdLineStart() { return cmdLineStart; }

    public final String getCommand() {
        return cmdLineStart +getCommandType();
    }
    public final String getCommand(String strValue) {
        if (strValue == null || strValue.trim().isEmpty())
            return cmdLineStart +getCommandType();
        return cmdLineStart +getCommandType() +"=" +strValue;
    }
}


