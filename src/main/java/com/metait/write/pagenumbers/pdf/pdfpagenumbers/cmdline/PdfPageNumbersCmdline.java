package com.metait.write.pagenumbers.pdf.pdfpagenumbers.cmdline;

import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.text.pdf.PdfReader;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PdfPageNumbersCmdline {
    private File inputPdfFile;
    private File outputPdfFile;
    private int iStartPage = -1;
    private int iEndPage = -1;
    private int iStartPageNumber = 1;
    private String strCommand = "";
    private String strText = "";
    private PrintLogicPosition printPrintTime = null;
    private PrintLogicPosition printExtraText = null;
    private PrintLogicPosition printPageNumbers = getDefaultPageNumberPosition();
    private boolean bNoPageNumbers = false;
    private boolean bCalledFromGui = false;
    private int iSystemExit = 0;
    private boolean bIsErrorOccured = false;

    public static void main(String[] args) {
        PdfPageNumbersCmdline cmdline = new PdfPageNumbersCmdline();
        if (args == null || (args.length != 2 && args.length != 3))
            cmdline.printHelp(args);
        else
            cmdline.writePDFPageNumbers(args);
    }

    public void printHelp(String[] args)
    {
        systemErr("Usage: " +this.getClass().getSimpleName() +" input.pdf output.pdf" );
        systemErr("Usage: " +this.getClass().getSimpleName() +" input.pdf output.pdf -command=\"pagenumbers_left|pagenumbers_center|pagenumbers_right\"");
        systemErr("Usage: " +this.getClass().getSimpleName() +" input.pdf output.pdf -command=\"printtime_left|printtime_center|printtime_right [printtime_text='ddd ddd dddd']\"");
        systemErr("Usage: " +this.getClass().getSimpleName() +" input.pdf output.pdf -command=\"printtext_left='ddfd'|printtext_center='eee'|printtext_right='eee' [timetextonly] startpage=2 [endpage=24] startpagenumber=1\" " );
        systemErr("");
        systemErr("  where almost all above -commmand options can used the same, except the same logical position in side of command!");
        systemErr("  where | character means logical or. And it can't be part of command.");
        systemErr("");
        systemErr("  where startpagenumber means that with that *page number* are starting from numbering pages.");
        systemErr("  where startpage means that with that *page* are starting from numbering pages.");
        systemErr("  where endpage means that with after that *page* and later are stopped from numbering pages.");

        systemErr("  where center means the center marginal position to printed.");
        systemErr("  where left means the left marginal position to printed.");
        systemErr("  where right means the right marginal position to printed.");

        systemErr("  where printtime means print time to printed value in the form dd.mm.yyyy hh:mm");
        systemErr("  where printtext means string value to printed.");
        systemErr("  where pagenumbers means string value to printed.");
        systemErr("  where printtime_text means string value to printed with printtime_left etc.");

        systemErr("  and where values and variables for -command are optional => [].");
        systemExit(1);
    }

    private boolean numberVariablesAreNotOK(int iDocPages)
    {
        // check int variables for page are ok:
        boolean ret = false;
        systemOut("Pdf document has " +iDocPages + " pages.");
        if (iDocPages > 0)
        {
            int tmp_endPage = iDocPages;
            if (iStartPage > 1)
                tmp_endPage = iDocPages - iStartPage;
            if (tmp_endPage < 0)
            {
                systemErr("Start page variable: has too high value: " +iStartPage);
                return true;
            }

            int tmp_endPage2 = iDocPages;
            if (iEndPage > 1)
                tmp_endPage2 = iDocPages - iEndPage;
            if (tmp_endPage2 < 0)
            {
                systemErr("End page variable: has too high value: " +iEndPage);
                return true;
            }

            int tmp_endPage3 = iDocPages;
            if (iEndPage > 1 && iEndPage < iDocPages && iStartPage > 0)
                tmp_endPage3 = iDocPages - iEndPage -iStartPage;
            if (tmp_endPage3 < 0)
            {
                systemErr("End page (" +iEndPage+ ") and Start page (" +iStartPage+") has too high values");
                return true;
            }

            if (iStartPage > 0 && iDocPages < iStartPage)
            {
                systemErr("Start page variable: has too high value: " +iStartPage);
                return true;
            }

            if (iEndPage > 0 && iDocPages < iEndPage)
            {
                systemErr("End page variable: has too high value: " +iEndPage);
                return true;
            }

            if (iStartPageNumber > 0 && iDocPages < iStartPageNumber)
            {
                systemErr("Start page number variable: has too high value: " +iStartPageNumber);
                return true;
            }

            if (iEndPage > 0 && iEndPage < iStartPage)
            {
                systemErr("Start page (" +iStartPage+
                        "): has too high value compared into end page number " +iEndPage);
                return true;
            }

            if (iEndPage > 0 && iEndPage < iStartPageNumber)
            {
                systemErr("Start page number variable (" +iStartPageNumber +
                        "): has too high value compared into end page number " +iEndPage);
                return true;
            }

            if (iStartPageNumber > 0 && iStartPage > 0 && iStartPage > iStartPageNumber)
            {
                systemErr("Start page number variable (" +iStartPageNumber +
                        "): has too low value compared into start page (" +iStartPage);
                return true;
            }

        }
        return ret;
    }

    private void writePDFPageNumbers(String strText, File inputFile, File outputFile,
                                     PrintLogicPosition printPageNumbers,
                                     PrintLogicPosition printExtraText,
                                     PrintLogicPosition printExtraPrintTime)
    {
        try {
            PdfReader reader = new PdfReader(inputFile.getAbsolutePath());
            PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(outputFile.getAbsolutePath()));
            stamper.setRotateContents(false);
            Phrase t = null;
            String strPrefix = (strText != null && !strText.trim().isEmpty() ? strText +" " : "");
            int iDocPages = reader.getNumberOfPages();
            if (numberVariablesAreNotOK(iDocPages))
                return;

            String strPrintTimeText = null;
            for (int i = 1; i <= reader.getNumberOfPages(); i++) {
                if (iStartPage > 0 && i < iStartPage) {
                    systemOut("Skipped pdf page: " +i +" because of " +i +" is lower than: " +iStartPage);
                    continue;
                }
                else
                if (iEndPage > 0 && i > iEndPage) {
                    systemOut("Skipped pdf page: " +i +" because of " +i +" is bigger than: " +iEndPage);
                    continue;
                }

                float xt = reader.getPageSize(i).getWidth()-50;
                float yt = reader.getPageSize(i).getBottom(15);

                if (!bNoPageNumbers) {
                    if (printPageNumbers.getPrintPosition() == PRINTPOSITION.RIGHT) {
                        String strValue = printPageNumbers.getText();
                        if (strValue != null && !strValue.isEmpty())
                            strValue = strValue + " ";
                        t = new Phrase( strValue+ +iStartPageNumber + "/" + reader.getNumberOfPages(), new Font(Font.HELVETICA, 9));
                        ColumnText.showTextAligned(
                                stamper.getOverContent(i), Element.ALIGN_RIGHT,
                                t, xt, yt, 0);
                    }
                    else
                    {
                        if (printPageNumbers.getPrintPosition() == PRINTPOSITION.CENTER) {
                            xt = reader.getPageSize(i).getWidth() - (reader.getPageSize(i).getWidth() / 2);
                            String strValue = printPageNumbers.getText();
                            if (strValue != null && !strValue.isEmpty())
                                strValue = strValue + " ";
                            t = new Phrase(strValue + +iStartPageNumber + "/" + reader.getNumberOfPages(), new Font(Font.HELVETICA, 9));
                            ColumnText.showTextAligned(
                                    stamper.getOverContent(i), Element.ALIGN_RIGHT,
                                    t, xt, yt, 0);
                        }
                        else
                        {
                            if (printPageNumbers.getPrintPosition() == PRINTPOSITION.LEFT) {
                                xt = 50;
                                String strValue = printPageNumbers.getText();
                                if (strValue != null && !strValue.isEmpty())
                                    strValue = strValue + " ";
                                t = new Phrase(strValue + +iStartPageNumber + "/" + reader.getNumberOfPages(), new Font(Font.HELVETICA, 9));
                                ColumnText.showTextAligned(
                                        stamper.getOverContent(i), Element.ALIGN_RIGHT,
                                        t, xt, yt, 0);
                            }
                        }
                    }
                }

                if (printExtraPrintTime != null) {
                    if (printExtraPrintTime.getPrintPosition() == PRINTPOSITION.CENTER) {
                        xt = reader.getPageSize(i).getWidth() - (reader.getPageSize(i).getWidth() / 2);
                        strPrintTimeText = printExtraPrintTime.getText();
                        if (printExtraPrintTime.getCalendar() != null
                                && !printExtraPrintTime.getTimeTextOnly()) {
                            strPrintTimeText = printExtraPrintTime.getTextAndCalendarString();
                        }
                        t = new Phrase(strPrintTimeText, new Font(Font.HELVETICA, 9));
                        ColumnText.showTextAligned(
                                stamper.getOverContent(i), Element.ALIGN_RIGHT,
                                t, xt, yt, 0);
                    } else if (printExtraPrintTime.getPrintPosition() == PRINTPOSITION.LEFT) {
                        xt = 120;
                        strPrintTimeText = printExtraPrintTime.getText();
                        if (printExtraPrintTime.getCalendar() != null
                                && !printExtraPrintTime.getTimeTextOnly()) {
                            strPrintTimeText = printExtraPrintTime.getTextAndCalendarString();
                           // DO THIS!;
                        }
                        t = new Phrase(strPrintTimeText, new Font(Font.HELVETICA, 9));
                        ColumnText.showTextAligned(
                                stamper.getOverContent(i), Element.ALIGN_RIGHT,
                                t, xt, yt, 0);

                    }
                    else if (printExtraPrintTime.getPrintPosition() == PRINTPOSITION.RIGHT) {
                        xt = reader.getPageSize(i).getWidth()-50;
                        strPrintTimeText = printExtraPrintTime.getText();
                        if (printExtraPrintTime.getCalendar() != null
                                && !printExtraPrintTime.getTimeTextOnly()) {
                            strPrintTimeText = printExtraPrintTime.getTextAndCalendarString();
                            // DO THIS!;
                        }
                        t = new Phrase(strPrintTimeText, new Font(Font.HELVETICA, 9));
                        ColumnText.showTextAligned(
                                stamper.getOverContent(i), Element.ALIGN_RIGHT,
                                t, xt, yt, 0);

                    }
                }

                if (printExtraText != null) {
                    if (printExtraText.getPrintPosition() == PRINTPOSITION.CENTER) {
                        xt = reader.getPageSize(i).getWidth() - (reader.getPageSize(i).getWidth() / 2);
                        strPrintTimeText = printExtraText.getText();
                        t = new Phrase(strPrintTimeText, new Font(Font.HELVETICA, 9));
                        ColumnText.showTextAligned(
                                stamper.getOverContent(i), Element.ALIGN_RIGHT,
                                t, xt, yt, 0);
                    } else if (printExtraText.getPrintPosition() == PRINTPOSITION.LEFT) {
                        xt = 120;
                        strPrintTimeText = printExtraText.getText();
                        t = new Phrase(strPrintTimeText, new Font(Font.HELVETICA, 9));
                        ColumnText.showTextAligned(
                                stamper.getOverContent(i), Element.ALIGN_RIGHT,
                                t, xt, yt, 0);

                    }
                    else if (printExtraText.getPrintPosition() == PRINTPOSITION.RIGHT) {
                        xt = reader.getPageSize(i).getWidth()-50;
                        strPrintTimeText = printExtraText.getText();
                        t = new Phrase(strPrintTimeText, new Font(Font.HELVETICA, 9));
                        ColumnText.showTextAligned(
                                stamper.getOverContent(i), Element.ALIGN_RIGHT,
                                t, xt, yt, 0);
                    }
                }

                iStartPageNumber++;
            }
            stamper.close();
            reader.close();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private File getTempPdfCopyOf(File inputFile)
    {
        String tmpFileName = "";
        String tmpSuffix = ".pdf";
        File temp = null;
        try{
            tmpFileName = "tmp_" +inputFile.getName();
            temp = File.createTempFile(tmpFileName, tmpSuffix);
        }catch(IOException e){
            e.printStackTrace();
            systemErr("Cannot create a tmp file: " +tmpFileName +tmpSuffix );
            systemExit(2);
        }

        try {
            Files.copy(inputFile.toPath(), temp.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }catch (Exception e){
            e.printStackTrace();
            systemErr("Cannot copy data into a temp file!");
            systemExit(3);
        }
        return temp;
    }

    private boolean systemExit(int iExit)
    {
        iSystemExit = iExit;
        if (!bCalledFromGui) {
            System.exit(iExit);
        }
        return true;
    }

    private String getTextFromCommand(String strCommnad)
    {
        String ret = "";
        if (strCommnad != null && !strCommnad.isEmpty() && !strCommnad.trim().isEmpty())
        {
            ret = strCommnad.trim();
        }
        return ret;
    }

    public String writePDFPageNumbers(int p_iStartPage, int p_iEndPage, int p_iStartPageNumber,
                                    File inputFile, File outputFile, String strCommand)
    {
        iStartPage = p_iStartPage;
        iEndPage = p_iEndPage;
        iStartPageNumber = p_iStartPageNumber;
        bCalledFromGui = true;
        String earlierInputFileName = inputFile.getAbsolutePath();
        inputPdfFile = getTempPdfCopyOf(inputFile);
        if (strCommand != null && !strCommand.trim().isEmpty()
           && (printPageNumbers == null || printExtraText == null || printPrintTime == null)) {
            if (!checkStrCommandParameter(strCommand, null)) {
                if (!spScreen.toString().isEmpty()) {
                    spScreen.append("\n");
                    spScreen.append("\n");
                    printHelp(null);

                }
                if (spScreen.toString().isEmpty())
                    return "There is an error of -command= value!";
                return spScreen.toString();
            }
        }
        writePDFPageNumbers(getTextFromCommand(strCommand), inputPdfFile, outputFile,
                printPageNumbers, printExtraText, printPrintTime);
        String tmpFileName = inputPdfFile.getAbsolutePath();
        if (!earlierInputFileName.equals(tmpFileName))
            if (!inputPdfFile.delete())
                systemErr("Cannot delete a tmp file: " +tmpFileName);
        if (bIsErrorOccured)
        {
            spScreen.append("\n");
            spScreen.append("\n");
            printHelp(null);
        }
        return spScreen.toString();
    }

    private void systemErr(String msg)
    {
        System.err.println(msg);
        bIsErrorOccured = true;
        spScreen.append("Error: " +msg +"\n");
    }

    private void systemOut(String msg)
    {
        System.out.println(msg);
        spScreen.append(msg).append("\n");
    }


    private StringBuffer spScreen = new StringBuffer();

    public String writePDFPageNumbers(String[] args)
    {
        if (!checkCmdLineOptions(args)) {
            String msg = "Error in command line options!";
            systemErr(msg);
            return msg;
        }
        if (!isFilenameValid(outputPdfFile.getAbsolutePath()))
        {
            String msg = "The output file name is not valid file name!";
            systemErr(msg);
            return msg;
        }

        return writePDFPageNumbers(iStartPage, iEndPage, iStartPageNumber, inputPdfFile, outputPdfFile, strCommand);
    }

    private boolean getNoPageNumbers(boolean bNoPageNumbers, String strCommand)
            throws TooMuchValuesInTheSameVariable
    {
        boolean ret = bNoPageNumbers;
        if (strCommand == null || strCommand.isEmpty())
            return ret;
        int ind = strCommand.indexOf("nopagenumbers");
        if (ind > -1)
        {
            int len = strCommand.length();
            if ((ind+1) < len) {
                int ind2 = strCommand.indexOf("nopagenumbers", ind + 1);
                if (ind2 > -1)
                    throw new TooMuchValuesInTheSameVariable("nopagenumbers is defined too much times!");
            }
            ret = true;
        }
        return ret;
    }

    private PrintLogicPosition getPrintPageNumberPosition(String strCommand)
            throws TooMuchValuesInTheSameVariable
    {
        PrintLogicPosition ret = null;
        if (strCommand == null || strCommand.isEmpty())
            return printPageNumbers;
        String strCmd = new String(strCommand.replace("pagenumbers_text=", ""));
        int ind2 = strCommand.indexOf("pagenumbers_text");
        String strPreFixText = "";
        if (ind2 > -1) {
            strPreFixText = strCommand.substring(ind2 + 1);
            if (strPreFixText != null && !strPreFixText.trim().isEmpty())
                strPreFixText = strPreFixText.trim().replaceAll("'","");
        }
        strCmd = strCmd.trim();

        int ind = strCmd.indexOf("pagenumbers_");
        if (ind == -1)
            return printPageNumbers;
        StringBuffer sp = new StringBuffer();
        final String cnsSeek = "pagenumbers_";
        sp.append(cnsSeek);
        int i = ind +cnsSeek.length();
        int len = strCommand.length();
        char [] chArrayCommand = strCommand.toCharArray();
        while (i < len && chArrayCommand[i] != ' ')
        {
            if (chArrayCommand[i] != '_')
             sp.append(chArrayCommand[i]);
            i++;
        }
        String strValue = sp.toString();
        if (strValue != null && !strValue.isEmpty())
        {
            if (strValue.equals("pagenumbers_center"))
                ret = new PrintLogicPosition(PRINTTYPE.PRINTPAGENUMBERS,
                        PRINTPOSITION.CENTER, strPreFixText, null,
                        -1,-1,-1, false);
            else
            if (strValue.equals("pagenumbers_right"))
                ret = new PrintLogicPosition(PRINTTYPE.PRINTPAGENUMBERS,
                        PRINTPOSITION.RIGHT, strPreFixText, null,
                        -1,-1,-1, false);
            else
            if (strValue.equals("pagenumbers_left"))
                ret = new PrintLogicPosition(PRINTTYPE.PRINTPAGENUMBERS,
                        PRINTPOSITION.LEFT, strPreFixText, null,
                        -1,-1,-1, false);
            else
                return printPageNumbers;
        }
        return ret;
    }

    private static PrintLogicPosition getDefaultPageNumberPosition()
    {
        return new PrintLogicPosition(PRINTTYPE.PRINTPAGENUMBERS,
                PRINTPOSITION.RIGHT, "", null,
                -1,-1,-1, false);
    }

    private void check2OfTooManyTheSameKindCmdLineOptions(PrintLogicPosition pos1, PrintLogicPosition pos2)
    {
        if (pos1 != null && pos2 != null)
        {
            if (pos1.getPrintPosition() == pos2.getPrintPosition())
            {
                if (pos1.getPrintPosition() == PRINTPOSITION.LEFT) {
                    systemErr( pos1.getCmdLineStart() +"left and " +pos2.getCmdLineStart()
                            +"left are defined in the same position, which is not allowed!");
                    if (systemExit(7))
                        return;
                }
                else
                if (pos1.getPrintPosition() == PRINTPOSITION.CENTER) {
                    systemErr(pos1.getCmdLineStart() +"center and " +pos2.getCmdLineStart()
                            +"center are defined in the same position, which is not allowed!");
                    if (systemExit(7))
                        return;
                }
                else
                if (pos1.getPrintPosition() == PRINTPOSITION.RIGHT) {
                    systemErr(pos1.getCmdLineStart() +"right and " +pos2.getCmdLineStart()
                            +"right are defined in the same position, which is not allowed!");
                    if (systemExit(7))
                        return;
                }
            }
        }
    }

    private void checkTooManyTheSameKindCmdLineOptions()
    {

        // printExtraText, printPageNumbers, printExtraPrintTime
        if (!bNoPageNumbers)
            check2OfTooManyTheSameKindCmdLineOptions(printPageNumbers, printExtraText);
        if (!bNoPageNumbers)
            check2OfTooManyTheSameKindCmdLineOptions(printPageNumbers, printPrintTime);
        check2OfTooManyTheSameKindCmdLineOptions(printExtraText, printPrintTime);
        /*
        if (printExtraText != null && printExtraPrintTime != null)
        {
            if (printExtraPrintTime.getPrintPosition() == printExtraText.getPrintPosition())
            {
                if (printExtraPrintTime.getPrintPosition() == PRINTPOSITION.LEFT) {
                    systemErr("printtext_left and printtime_left are defined in the same position, which is not allowed!");
                    if (systemExit(7))
                        return;
                }
                else
                if (printExtraPrintTime.getPrintPosition() == PRINTPOSITION.CENTER) {
                    systemErr("printtext_center and printtime_center are defined in the same position, which is not allowed!");
                    if (systemExit(7))
                        return;
                }
            }

            if (printPageNumbers.getPrintPosition() == printExtraText.getPrintPosition())
            {
                if (printPageNumbers.getPrintPosition() == PRINTPOSITION.LEFT) {
                    systemErr("printtext_left and pagenumber_left are defined in the same position, which is not allowed!");
                    if (systemExit(7))
                        return;
                }
                else
                if (printPageNumbers.getPrintPosition() == PRINTPOSITION.CENTER) {
                    systemErr("printtext_center and pagenumber_center are defined in the same position, which is not allowed!");
                    if (systemExit(7))
                        return;
                }
            }

            if (printPageNumbers.getPrintPosition() == printExtraPrintTime.getPrintPosition())
            {
                if (printPageNumbers.getPrintPosition() == PRINTPOSITION.LEFT) {
                    systemErr("printtime_left and pagenumber_left are defined in the same position, which is not allowed!");
                    if (systemExit(7))
                        return;
                }
                else
                if (printPageNumbers.getPrintPosition() == PRINTPOSITION.CENTER) {
                    systemErr("printtime_center and pagenumber_center are defined in the same position, which is not allowed!");
                    if (systemExit(7))
                        return;
                }
            }
        }
        else
        {
            if (bNoPageNumbers) {
                systemErr("-command: nopagenumbers and any printtime_... and printtext_... are defined, which is not allowed!");
                if (systemExit(8))
                    return;
            }
        }
        */

    }

    private boolean checkStrCommandParameter(String strCommand, String [] args)
    {
        boolean ret = false;
        boolean bCommandIsOk = true;
        boolean bCommandExtraPrintTimeOk = true;
        boolean bCommandExtraTextOk = true;

        if (strCommand == null || strCommand.trim().isEmpty()) {
            systemErr("-command=\"something\" parameter is wrong!");
            bCommandIsOk = false;
            if (args != null)
                printHelp(args);
        }
        try {
            iStartPage = getStartPageFrom(iStartPage, strCommand);
        } catch (TooMuchValuesInTheSameVariable tme)
        {
            systemErr(tme.getMessage());
            if (systemExit(6))
                return false;
        }
        try {
            iEndPage = getEndPageFrom(iEndPage, strCommand);
        } catch (TooMuchValuesInTheSameVariable tme)
        {
            systemErr(tme.getMessage());
            if (systemExit(6))
                return false;
        }
        try {
            iStartPageNumber = getStartPageNumberFrom(iStartPageNumber, strCommand);
        } catch (TooMuchValuesInTheSameVariable tme)
        {
            systemErr(tme.getMessage());
            if (systemExit(6))
                return false;
        }
        try {
            bNoPageNumbers = getNoPageNumbers(bNoPageNumbers, strCommand);
        } catch (TooMuchValuesInTheSameVariable tme)
        {
            systemErr(tme.getMessage());
            if (systemExit(6))
                return false;
        }

        /*
        try {
            printPageNumbers = getPrintPageNumberPosition(strCommand);
        }catch (TooMuchValuesInTheSameVariable tme){
            systemErr(tme.getMessage());
            if (systemExit(6))
                return false;
        }
        try {
            printPrintTime = getPrintTime(strCommand);
            bCommandExtraPrintTimeOk = true;
        } catch (TooMuchValuesInTheSameVariable tme)
        {
            systemErr(tme.getMessage());
            if (systemExit(6))
                return false;
        }
        try {
            printExtraText = getExtraText(strCommand);
            bCommandExtraTextOk = true;
        } catch (TooMuchValuesInTheSameVariable tme)
        {
            systemErr(tme.getMessage());
            if (systemExit(6))
                return false;
        }
         */

        try {
            printPageNumbers = getPrintLogicPosition(PRINTTYPE.PRINTPAGENUMBERS, strCommand);
        }catch (Exception tme){
            systemErr(tme.getMessage());
            if (systemExit(6))
                return false;
        }

        try {
            printPrintTime = getPrintLogicPosition(PRINTTYPE.PRINTTIME, strCommand);
            bCommandExtraPrintTimeOk = true;
        } catch (Exception tme)
        {
            systemErr(tme.getMessage());
            if (systemExit(6))
                return false;
        }
        try {
                printExtraText = getPrintLogicPosition(PRINTTYPE.PRINTEXTRATXT, strCommand);
                bCommandExtraTextOk = true;
            } catch (Exception tme)
            {
                systemErr(tme.getMessage());
                if (systemExit(6))
                    return false;
            }

        checkTooManyTheSameKindCmdLineOptions();
        if (bNoPageNumbers && printPrintTime != null && printExtraText == null) {
            systemErr("-command: nopagenumbers and any printtime_... and printtext_... are defined, which is not allowed!");
            if (systemExit(8))
                return false;
        }
        if (bCommandIsOk && bCommandExtraPrintTimeOk && bCommandExtraTextOk)
            ret = true;

        return ret;
    }

    public static boolean isFilenameValid(String file) {
        File f = new File(file);
        try {
            f.getCanonicalPath();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public boolean checkCmdLineOptions(String[] args)
    {
        boolean ret = false;
        if (args == null || args.length == 0)
        {
            systemErr("commandline parameters are missing!");
            printHelp(args);
        }
        else
        {
            if (args.length != 2 && args.length != 3 )
            {
                systemErr("commandline parameters are wrong!");
                printHelp(args);
            }
            else
            {
                boolean inputFileOk = false;
                boolean outputFileOk = false;
                boolean bCommandIsOk = true;
                boolean bCommandExtraTextOk = true;
                boolean bCommandExtraPrintTimeOk = true;
                File tmpInput = new File(args[0]);
                if (tmpInput.exists() && tmpInput.canRead() && tmpInput.getName().endsWith(".pdf"))
                {
                    inputPdfFile = tmpInput;
                    inputFileOk = true;
                }
                File tmpOutput = new File(args[1]);
                if (!tmpOutput.exists() ||
                        (tmpOutput.exists() && tmpOutput.canWrite() && tmpOutput.getName().endsWith(".pdf")))
                {
                    outputPdfFile = tmpOutput;
                    outputFileOk = true;
                }
                if (args.length == 3 )
                {
                    strCommand = args[2];
                    if (!checkStrCommandParameter(strCommand, args))
                        return false;
                    bCommandExtraPrintTimeOk = true;
                    bCommandExtraTextOk = true;
                    bCommandIsOk = true;
                }
                if (bCommandExtraPrintTimeOk && bCommandExtraTextOk && bCommandIsOk && inputFileOk && outputFileOk)
                    ret = true;
            }
        }
        return ret;
    }

    private PrintLogicPosition getPrintTime(String strCommand)
            throws TooMuchValuesInTheSameVariable
    {
        PrintLogicPosition ret = null;
        final String cnstSeekVar = "printtime_";
        String strC = strCommand.trim();
        int len = strC.length();
        if (len == 0)
            return null;
        int ind = strC.indexOf(cnstSeekVar); // printtime_left|printtime_center
        if (ind == -1)
            return null;
        int ind2 = -1;
        if (len >= (ind+1))
           ind2 = strC.indexOf(cnstSeekVar, ind+1);
        if (ind2 == -1)
            return null;
        String strSecondValue = strC.substring(ind2); // printtime_left|printtime_center
        if (ind2 != -1 && strSecondValue != null && (strSecondValue.contains("left")
                && strSecondValue.contains("center") || strSecondValue.contains("right")))
            throw new TooMuchValuesInTheSameVariable("Values of " +cnstSeekVar
                    +" are too much, only one is allowed.");
        StringBuffer sp = new StringBuffer();
        sp.append(cnstSeekVar);
        char [] chArrayC = strC.toCharArray();
        int i = ind;
        while(i < len && chArrayC[i+cnstSeekVar.length()] != ' ') {
            sp.append(chArrayC[i + cnstSeekVar.length()]);
            i++;
        }
        String founded = sp.toString();
        if (founded.startsWith(cnstSeekVar) && (founded.endsWith("left") || founded.endsWith("center"))) {
            PRINTTYPE type = PRINTTYPE.PRINTTIME;
            PRINTPOSITION position = null;
            if (founded.endsWith("center"))
                position = PRINTPOSITION.CENTER;
            else
            if (founded.endsWith("left"))
                position = PRINTPOSITION.LEFT;
            else
            if (founded.endsWith("right"))
                position = PRINTPOSITION.RIGHT;
            Calendar cal = Calendar.getInstance();
            Locale current = new Locale("fi","FI");
            SimpleDateFormat sdf=new SimpleDateFormat("dd.MM.YYYY HH:MM", current);
            String dateString = sdf.format(cal.getTime());
            if (strSecondValue != null && strSecondValue.contains("printtime_text"))
            {
                int ind3 = strSecondValue.indexOf('=');
                if (ind3 > -1)
                    dateString = strSecondValue.substring(ind3+1).replaceAll("'", "");
            }
            ret = new PrintLogicPosition(type, position, dateString, cal,
                    -1, -1, -1, false);
        }
        else
        {
            throw new TooMuchValuesInTheSameVariable(cnstSeekVar +" does have a wrong end value!");
        }
        return ret;
    }

    String getPrintExtraTextOfPrintType(PRINTTYPE printtype, int indLeft, int indCenter, int indRight)
    {
        String ret = "";
        if (printtype != PRINTTYPE.PRINTEXTRATXT) {
            ret = "text";
        }
        else
        {
            if (indLeft > -1)
                ret = "left";
            else
            if (indCenter > -1)
                ret = "center";
            else
            if (indRight > -1)
                ret = "right";
        }
//            if printtype == PRINTTYPE.PRINTPAGENUMBERS
        return ret;
    }

    private PrintLogicPosition getPrintLogicPosition(final  PRINTTYPE printtype,
                                                     final String strCommand)
            throws TooMuchValuesInTheSameVariable
    {
        PrintLogicPosition ret = null;
        if (strCommand == null || strCommand.trim().isEmpty())
            return ret;

        String printExtraText = null;
        Calendar printCalender = null;
        PRINTPOSITION printposition = null;
        String strText = null;
        String strPreFixText = null;
        boolean bValueAfterSearch = false;
        String strSearch = PrintLogicPosition.getCmdLineStart(printtype);
        if (strSearch == null || strSearch.trim().isEmpty())
            return ret;
        int ind = strCommand.indexOf(strSearch);
        if (ind == -1)
            return ret;

        boolean bTimeTextOnly = false;
        String strIndexOfSearch = strSearch +"left";
        int indLeftSearch = strIndexOfSearch.length();
        int indLeft = strCommand.indexOf(strIndexOfSearch);
        strIndexOfSearch = strSearch +"center";
        int indCenterSearch = strIndexOfSearch.length();
        int indCenter = strCommand.indexOf(strIndexOfSearch);
        strIndexOfSearch = strSearch +"right";
        int indRightSearch = strIndexOfSearch.length();
        int indRight = strCommand.indexOf(strIndexOfSearch);
        strIndexOfSearch = strSearch +getPrintExtraTextOfPrintType(printtype,
                indLeft, indCenter, indRight);
        int indTextSearch = strIndexOfSearch.length();
        int indText = strCommand.indexOf(strIndexOfSearch);
        if (indLeft == -1 && indCenter == -1 && indRight == -1)
            return ret;

        int indSearchTextStart = -1;
        if (indLeft > -1)
        {
            printposition = PRINTPOSITION.LEFT;
            indSearchTextStart = indLeft +indLeftSearch;
        }
        else
        if (indCenter > -1)
        {
            printposition = PRINTPOSITION.CENTER;
            indSearchTextStart = indCenter +indCenterSearch;
        }
        else
        if (indRight > -1)
        {
            printposition = PRINTPOSITION.RIGHT;
            indSearchTextStart = indRight +indRightSearch;
        }

        if(printtype == PRINTTYPE.PRINTEXTRATXT)
            bValueAfterSearch = true;

        if (bValueAfterSearch && indSearchTextStart > -1)
        {
            strText = strCommand.substring(indSearchTextStart+2);
            if (strText != null && !strText.trim().isEmpty())
            {
                int ind2 = strText.indexOf("'");
                if (ind2 > -1)
                    strText = strText.substring(0, ind2);
                strText = strText.replaceAll("=", " ")
                        .replaceAll("'","").trim();
                strPreFixText = strText;
            }
        }
        else
        {
            if (!bValueAfterSearch && indText > -1)
            {
                strText = strCommand.substring(indText+indTextSearch+2);
                if (strText != null && !strText.trim().isEmpty())
                {
                    int ind2 = strText.indexOf("'");
                    if (ind2 > -1)
                        strText = strText.substring(0, ind2);
                    strText = strText.replaceAll("=", " ")
                            .replaceAll("'","").trim();
                    strPreFixText = strText;
                }
            }
        }

        if (printtype == PRINTTYPE.PRINTPAGENUMBERS) {
            iStartPage = getStartPageFrom(iStartPage, strCommand);
            iEndPage = getEndPageFrom(iEndPage, strCommand);
            iStartPageNumber = getStartPageNumberFrom(iStartPageNumber, strCommand);
            //        private int iStartPageNumber = 1;
         }
        /*
         else
         if (printtype == PRINTTYPE.PRINTEXTRATXT)
         {
             ret = getExtraText(strCommand);
             return ret;
         }
         */
         else
         if (printtype == PRINTTYPE.PRINTTIME)
         {
            // ret = getPrintTime(strCommand);
            // return ret;
             Calendar cal = Calendar.getInstance();
             Locale current = new Locale("fi","FI");
             SimpleDateFormat sdf=new SimpleDateFormat("dd.MM.YYYY HH:MM", current);
             String strCalText = sdf.format(cal.getTime());
             printCalender = cal;
             int ind3 = strCommand.indexOf("timetextonly");
             if (ind3 > -1)
                 bTimeTextOnly = true;
             /*
             if (strPreFixText != null && !strPreFixText.trim().isEmpty())
                 strPreFixText = strCalText +" " +strPreFixText;
             else
                 strPreFixText = strCalText;
              */
             /*
             if (strText != null && !strText.trim().isEmpty())
             {
                 strText = strText +" " +strCalText;
             }
              */
         }

         ret = new PrintLogicPosition(printtype,
                printposition, strPreFixText, printCalender,
                 iStartPage, iEndPage, iStartPageNumber, bTimeTextOnly);

        return ret;
    }

    private PrintLogicPosition getExtraText(String strCommand)
            throws TooMuchValuesInTheSameVariable
    {
        PrintLogicPosition ret = null;
        final String cnstSeekVar = "printtext_";
        String strC = strCommand.trim();
        int len = strC.length();
        if (len == 0)
            return null;
        int ind = strC.indexOf(cnstSeekVar); // printtext_left|printtext_center
        if (ind == -1)
            return null;
        int ind2 = -1;
        if (len >= (ind+1))
            ind2 = strC.indexOf(cnstSeekVar, ind+1); // printtext_left|printtext_center
        if (ind2 != -1)
            throw new TooMuchValuesInTheSameVariable("Values of " +cnstSeekVar
                    +" are too much, only one is allowed.");

        StringBuffer sp = new StringBuffer();
        sp.append(cnstSeekVar);
        char [] chArrayC = strC.toCharArray();
        int i = ind;
        char chLast = ' ';
        i = i+cnstSeekVar.length();
        while(i < len && chArrayC[i] != ' '
            && chArrayC[i] != '=' ) {
            sp.append(chArrayC[i]);
            chLast = chArrayC[i];
            i++;
        }
        if (i < len) {
            chLast = chArrayC[i];
            if (chLast == '=')
                i++;
        }

        String founded = sp.toString();
        if (founded.startsWith(cnstSeekVar) && (founded.endsWith("left") || founded.endsWith("center")
           || founded.endsWith("right"))) {
            PRINTTYPE type = PRINTTYPE.PRINTEXTRATXT;
            PRINTPOSITION position = null;
            if (founded.endsWith("center"))
                position = PRINTPOSITION.CENTER;
            else
            if (founded.endsWith("left"))
                position = PRINTPOSITION.LEFT;
            else
            if (founded.endsWith("right"))
                position = PRINTPOSITION.RIGHT;
            Calendar cal = Calendar.getInstance();
            Locale current = new Locale("fi","FI");
            SimpleDateFormat sdf=new SimpleDateFormat("dd.MM.YYYY HH:MM", current);
            String dateString = sdf.format(cal.getTime());

            StringBuffer sp2 = new StringBuffer();
            boolean secondStringConstrainCharFounded = false;
            int iConstrainCounter = 0;
            while(i < len && (!secondStringConstrainCharFounded ||
                  (secondStringConstrainCharFounded && chArrayC[i] != ' ')))
            {
                chLast = chArrayC[i];
                if (iConstrainCounter == 1 && chLast != '\'')
                    sp2.append(chLast);
                if (chLast == '\'')
                    iConstrainCounter++;
                if (iConstrainCounter > 1)
                    secondStringConstrainCharFounded = true;
                i++;
            }

            String srtValue = sp2.toString();
            ret = new PrintLogicPosition(type, position, srtValue, cal,
                    -1,-1,-1, false);
        }
        else
        {
            throw new TooMuchValuesInTheSameVariable(cnstSeekVar +" does have a wrong end value!");
        }

        return ret;
    }

    private int getStartPageFrom(int iStartPage, String strCommand)
            throws TooMuchValuesInTheSameVariable
    {
        int ret = iStartPage;
        int tmp = getIntValueAfter("startpage", strCommand);
        if (tmp < 1)
            return ret;
        ret = tmp;
        return ret;
    }

    private int getEndPageFrom(int iEndPage, String strCommand)
            throws TooMuchValuesInTheSameVariable
    {
        int ret = iEndPage;
        int tmp = getIntValueAfter("endpage", strCommand);
        if (tmp < 1)
            return ret;
        ret = tmp;
        return ret;
    }

    private int getStartPageNumberFrom(int iStartPageNumber, String strCommand)
            throws TooMuchValuesInTheSameVariable
    {
        int ret = iStartPageNumber;
        int tmp = getIntValueAfter("startpagenumber", strCommand);
        if (tmp < 1)
            return ret;
        ret = tmp;
        return ret;
    }

    private static class TooMuchValuesInTheSameVariable extends Exception
    {
        public TooMuchValuesInTheSameVariable(String msg)
        {
            super((msg));
        }
    }

    private int getIntValueAfter(String strName, String strCommand)
            throws TooMuchValuesInTheSameVariable
    {
        int ret = -1;
        String removeString = "-command=";
        int indCommand = strCommand.indexOf(removeString);
        String dropedCommand = new String(strCommand);
        if (indCommand > -1)
            dropedCommand = dropedCommand.substring(indCommand +removeString.length());
        String strRegex = "(?ism)" +strName +"\\s*=\\s*(?<value>\\d+)";
        Pattern p = Pattern.compile(strRegex, Pattern.MULTILINE);
        Matcher m = p.matcher(dropedCommand);
        int groupCount = m.groupCount();
        boolean matchFounded = false;
        final String cnstValue = "value";
        int iValue = -1, iStart, iEnd;
        String strGroup;
        int iGroup;
        String strValue;
        int iCounter = 0;

        while (m.find()) {
            iGroup = 1;
            groupCount = m.groupCount();
            iStart = m.start();
            iEnd = m.end();

            strValue = m.group(cnstValue).trim();
            try {
                iValue = Integer.valueOf(strValue);
                matchFounded = true;
                iCounter++;
             //   break;
            }catch (Exception e){
                systemErr("Cannot read variable '" +strName +"' ant int value: '" +strValue +"'");
                // e.printStackTrace();
                systemExit(4);
            }
            /*
            systemOut("Founded:");
            systemOut(matchFounded);
             */
            strGroup = m.group(iGroup++);
        }

        if (iCounter > 1)
            throw new TooMuchValuesInTheSameVariable("Too much values for parameter: " + strCommand);
        if (matchFounded)
            ret = iValue;
        return ret;
    }
}
