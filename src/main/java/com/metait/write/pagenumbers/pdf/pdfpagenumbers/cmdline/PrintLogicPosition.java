package com.metait.write.pagenumbers.pdf.pdfpagenumbers.cmdline;

import java.util.Calendar;

public class PrintLogicPosition {
    private final PRINTTYPE printType;
    private final PRINTPOSITION PRINTPOSITION;
    private final  String  strText;
    private final Calendar printCalendar;
    private final int iStartPage;
    private final int iEngPage;
    private final int iStartPageNumber;
    private final String cmdLineStart;

    public PrintLogicPosition(final PRINTTYPE p_printType,
                              final PRINTPOSITION p_PRINTPOSITION, final String  p_strText,
                              final  Calendar p_printCalendar,
                              final int p_iStartPage, final int p_iEngPage, final int p_iStartPageNumber
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
    }

    private String getCmdLineStart(PRINTTYPE type)
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
    public final String getText() { return strText; }
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


