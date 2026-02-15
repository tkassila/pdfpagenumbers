package com.metait.write.pagenumbers.pdf.pdfpagenumbers;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.*;
import java.io.IOException;
import java.util.Optional;
import java.io.File;
import javafx.collections.FXCollections;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.metait.write.pagenumbers.pdf.pdfpagenumbers.cmdline.PdfPageNumbersCmdline;

public class PdfPageNumberController {
    @FXML
    private Button buttonInputFile;
    @FXML
    private Label labelMsg;
    @FXML
    protected Button buttonOutputDir;
    @FXML
    private Label labelInputPdf;
    @FXML
    private Label labelOutputDir;
    @FXML
    private TextField textFieldOutputFile;
    @FXML
    protected CheckBox checkBoxNumbering;
    @FXML
    protected CheckBox checkBoxPrintTime;
    @FXML
    protected CheckBox checkBoxPrintExtra;
    @FXML
    protected HBox hBoxNumbering;
    @FXML
    protected HBox hBoxNumbering2;
    @FXML
    protected VBox vboxPage;
    @FXML
    private TextField textFieldStartPage;
    @FXML
    private TextField textFieldEndPage;
    @FXML
    private TextField textFieldStartPageNumber;
    @FXML
    private TextField textFiledExtraText;
    @FXML
    protected Button buttonConvert;
    @FXML
    protected TextArea textAreaResult;
    @FXML
    private ComboBox<String> comboBoxExtraText;
    @FXML
    private HBox hBoxExtraText;
    @FXML
    protected ComboBox<String> comboBoxPrintTime;
    @FXML
    private HBox hBoxPrintTime;
    @FXML
    protected Label labelCommand;
    @FXML
    protected ComboBox<String> comboBoxPageNumbers;
    @FXML
    protected Button buttonOpenResult;
    @FXML
    protected TextField textFieldTimeText;
    @FXML
    protected TextField textFieldPageText;
    @FXML
    protected CheckBox checkPrintOnlyText;

    private FileChooser inputFileChooser = new FileChooser();
    private DirectoryChooser directoryOutChooser = new DirectoryChooser();
    private Stage parentStage;
    private File inputPdfFile;
    private File selectedDir;
    private File openResutlFile = null;
    private ObservableList<String> pdfTextPositions;

    private int iStartPage = -1, iEndPage = -1, iStartPageNumber = 1;

    @FXML
    protected void initialize()
    {
        //  textFieldUserWritePasswordTip.setStyle("-fx-font-weight: bold; -fx-text-fill: yellow; -fx-font-size: 14");
        buttonOpenResult.setDisable(true);
        staticLabelMsg = this.labelMsg;
        pdfTextPositions = FXCollections.observableArrayList();
        pdfTextPositions.add("LEFT");
        pdfTextPositions.add("CENTER");
        pdfTextPositions.add("RIGHT");

        inputFileChooser.setTitle("Open input pdf file");
        directoryOutChooser.setTitle("Select output directory for a output pdf file");
        checkBoxNumbering.setSelected(true);
        checkBoxPrintTime.setSelected(false);

        hBoxExtraText.setDisable(true);
        hBoxPrintTime.setDisable(true);
        hBoxExtraText.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, null , null)));
        hBoxPrintTime.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, null , null)));
        vboxPage.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, null , null)));
      //hBoxNumbering.setDisable(true);
        comboBoxPageNumbers.setItems(pdfTextPositions);
        comboBoxPrintTime.setItems(pdfTextPositions);
        comboBoxExtraText.setItems(pdfTextPositions);
        comboBoxPageNumbers.setValue("RIGHT");
        comboBoxExtraText.setValue("CENTER");
        comboBoxPrintTime.setValue("LEFT");
    }

    public void setState(Stage stage){ parentStage = stage; }

    private static Label staticLabelMsg;

    public static void openFile(File file) throws Exception {
        if (Desktop.isDesktopSupported()) {
            new Thread(() -> {
                try {
                    Desktop.getDesktop().open(file);
                } catch (IOException e) {
                    e.printStackTrace();
                    staticLabelMsg.setText(e.getMessage());
                }
            }).start();
        }
    }

    @FXML
    protected void buttonOpenResultPressed()
    {
        if (openResutlFile != null && openResutlFile.exists())
            try {
                PdfPageNumberController.openFile(openResutlFile);
            }catch (Exception e){
                textAreaResult.setText(e.getMessage());
            }
    }

    @FXML
    protected void buttonConvertPressed()
    {
        if (isAllowedToStartConversion())
            doConversionToOutputFile();
    }

    private void checkIsAllowedToStartConversion()
    {

    }

    private boolean isAllowedToStartConversion()
    {
        boolean ret = false;
        textAreaResult.setText("");

        if (labelInputPdf.getText().trim().isEmpty()) {
            textAreaResult.setText("Select input pdf file.");
            return ret;
        }
        else
        if (labelOutputDir.getText().trim().isEmpty()) {
            textAreaResult.setText("Select output directory.");
            return ret;
        }
        else
        if (textFieldOutputFile.getText().trim().isEmpty()) {
            textAreaResult.setText("Error: output file name is empty!");
            return ret;
        }

        String strValue = textFieldOutputFile.getText().trim();
        if (!strValue.endsWith(".pdf"))
        {
            textAreaResult.setText("The output file name must end with: .pdf");
            return ret;
        }

        if (!checkBoxNumbering.isSelected() && !checkBoxPrintExtra.isSelected() && !checkBoxPrintTime.isSelected())
        {
            textAreaResult.setText("At least one of 3 checkbox controls and its below controls must be " +
                    "selected and have right values, before to start the conversion.");
            return ret;
        }
        boolean isOk = false;
        iStartPage = -1;
        iEndPage = -1;
        iStartPageNumber = 1;

        // PAGE NUMBERING:
        if (checkBoxNumbering.isSelected())
        {
            if (textFieldEndPage.getText().trim().isEmpty()
                && textFieldStartPage.getText().trim().isEmpty()
                && textFieldStartPageNumber.getText().trim().isEmpty()
            ) {
                isOk = true;
            }
            else
            {
                String tmp_textFieldEndPage = textFieldEndPage.getText().trim();
                String tmp_textFieldStartPage = textFieldStartPage.getText().trim();
                String tmp_textFieldStartPageNumber = textFieldStartPageNumber.getText().trim();
                if (!tmp_textFieldStartPage.isEmpty())
                {
                    try {
                        int iValue = Integer.parseInt(tmp_textFieldStartPage);
                        if (iValue < 1)
                        {
                            textAreaResult.setText("The value of Start Page must 1 or greater or empty (= not set)!");
                            return ret;
                        }
                        iStartPage = iValue;
                    }catch (Exception e){
                        textAreaResult.setText("Error in the value of Start Page must 1 or greater or empty (= not set)!");
                        return ret;
                    }
                }

                if (!tmp_textFieldEndPage.isEmpty())
                {
                    try {
                        int iValue = Integer.parseInt(tmp_textFieldEndPage);
                        if (iValue < 1)
                        {
                            textAreaResult.setText("The value of End Page must 1 or greater than start page value or empty (= not set)!");
                            return ret;
                        }
                        iEndPage = iValue;
                    }catch (Exception e){
                        textAreaResult.setText("Error in the value of End Page must 1 or greater or empty (= not set)!");
                        return ret;
                    }
                }

                if (!tmp_textFieldStartPageNumber.isEmpty())
                {
                    try {
                        int iValue = Integer.parseInt(tmp_textFieldStartPageNumber);
                        if (iValue < 1)
                        {
                            textAreaResult.setText("The value of Start Page Number must 1 or greater or empty (= not set)!");
                            return ret;
                        }
                        iStartPageNumber = iValue;
                    }catch (Exception e){
                        textAreaResult.setText("Error in the value of Start Page Number must 1 or greater or empty (= not set)!");
                        return ret;
                    }
                }

                if (iStartPage > iEndPage)
                {
                    textAreaResult.setText("Start page (" +iStartPage +") value must equal or End page (" +iEndPage +") value or empty (= not set)!");
                    return ret;
                }

                if (iStartPageNumber > iEndPage)
                {
                    textAreaResult.setText("Start page number (" +iStartPageNumber +
                            ") value must equal or lower than End page (" +iEndPage +") value or empty (= not set)!");
                    return ret;
                }
                isOk = true;
            }
        }
        if (isOk)
            ret = true;

        return ret;
    }

    private void doConversionToOutputFile()
    {
        /*
          public String writePDFPageNumbers(int p_iStartPage, int p_iEndPage, int p_iStartPageNumber,
                                    File inputFile, File outputFile, String strCommand)
         */
        if (textFieldOutputFile.getText().trim().isEmpty())
        {
            setLabelMsg("Error: output pdf file name field is empty!");
            return;
        }

        buttonOpenResult.setDisable(true);
        PdfPageNumbersCmdline cmdline = new PdfPageNumbersCmdline();
        File outPdfFile = new File(selectedDir.getAbsolutePath() +File.separatorChar
                                    +textFieldOutputFile.getText().trim());
        if (inputPdfFile.exists() && outPdfFile.exists()
                && inputPdfFile.getAbsolutePath().equals(outPdfFile.getAbsolutePath()))
        {
            ButtonType okButton = new ButtonType("Ok", ButtonBar.ButtonData.OK_DONE);
            ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            Alert alert = new Alert(Alert.AlertType.WARNING,
                    "The input pdf file and the output pdf are the same!", okButton, cancelButton);

            alert.setTitle("The same files");
            Optional<ButtonType> result = alert.showAndWait();
            boolean okSelected = false;
            ButtonType resultButtonType = result.get();
            if (result.isPresent() && resultButtonType.getButtonData() == ButtonBar.ButtonData.OK_DONE){
                okSelected = true;
            }
            if (!okSelected) {
                textAreaResult.setText("The conversion has been canceled. The input and out put files are the same!");
                return;
            }
        }

        if (userSelectedOptionsNotOK())
            return;;

        String strCommand = getCurrentCommand();
    //    strCommand = "printtext_";
        labelCommand.setText(strCommand);
        openResutlFile = outPdfFile;
        /*
         writePDFPageNumbers(iStartPage, iEndPage, iStartPageNumber, inputPdfFile, outputPdfFile, null);

        String result = cmdline.writePDFPageNumbers(iStartPage, iEndPage, iStartPageNumber,
                                                    inputPdfFile, outPdfFile, strCommand);
        */
        String[] paramArgs = {inputPdfFile.getAbsolutePath(), outPdfFile.getAbsolutePath(), strCommand};
        String result = cmdline.writePDFPageNumbers(paramArgs);
        if (!result.trim().isEmpty())
            textAreaResult.setText(result);
        else {
            textAreaResult.setText("The conversion has successfully done into output pdf.");
            buttonOpenResult.setDisable(false);
        }

    }

    private void setLabelMsg(String txt)
    {
        Platform.runLater(() -> {
            try {
                //an event with a button maybe
                // System.out.println("button is clicked");
                labelMsg.setText(txt == null ? "" : txt);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private boolean userSelectedOptionsNotOK()
    {
        boolean ret = false;
        setLabelMsg("");
        if (checkBoxNumbering.isSelected() && checkBoxPrintTime.isSelected() && checkBoxPrintExtra.isSelected())
        {
            String strValueOfPageNumbers = comboBoxPageNumbers.getValue();
            String strValueOfPrintTime = comboBoxPrintTime.getValue();
            String strValueOfExtraText = comboBoxExtraText.getValue();
            if (strValueOfPageNumbers.equals(strValueOfPrintTime))
            {
                setLabelMsg("Selected combo values cannot be the same in 'Page numbers' and 'Print time' controls!");
                return true;
            }
            if (strValueOfPageNumbers.equals(strValueOfExtraText))
            {
                setLabelMsg("Selected combo values cannot be the same in 'Page numbers' and 'Extra text' controls!");
                return true;
            }
            if (strValueOfPrintTime.equals(strValueOfExtraText))
            {
                setLabelMsg("Selected combo values cannot be the same in 'Print time' and 'Extra text' controls!");
                return true;
            }

        }

        if (checkBoxNumbering.isSelected() && checkBoxPrintTime.isSelected())
        {
            String strValueOfPageNumbers = comboBoxPageNumbers.getValue();
            String strValueOfPrintTime = comboBoxPrintTime.getValue();
            if (strValueOfPageNumbers.equals(strValueOfPrintTime))
            {
                setLabelMsg("Selected combo values cannot be the same in 'Page numbers' and 'Print time' controls!");
                return true;
            }
        }

        if (checkBoxNumbering.isSelected() && checkBoxPrintExtra.isSelected())
        {
            String strValueOfPageNumbers = comboBoxPageNumbers.getValue();
            String strValueOfExtraText = comboBoxExtraText.getValue();
            if (strValueOfPageNumbers.equals(strValueOfExtraText))
            {
                setLabelMsg("Selected combo values cannot be the same in 'Page numbers' and 'Extra text' controls!");
                return true;
            }
        }

        if (checkBoxPrintTime.isSelected() && checkBoxPrintExtra.isSelected())
        {
            String strValueOfPrintTime = comboBoxPrintTime.getValue();
            String strValueOfExtraText = comboBoxExtraText.getValue();
            if (strValueOfPrintTime.equals(strValueOfExtraText))
            {
                setLabelMsg("Selected combo values cannot be the same in 'Print time' and 'Extra text' controls!");
                return true;
            }
        }

        if (checkBoxPrintExtra.isSelected()) {
            String strValueOfExtraText = textFiledExtraText.getText();
            if (strValueOfExtraText == null || strValueOfExtraText.isEmpty()) {
                setLabelMsg("'Extra text' control is empty! Give some value of it.");
                return true;
            }
            if (strValueOfExtraText != null && strValueOfExtraText.trim().isEmpty() && strValueOfExtraText.contains(" ")) {
                setLabelMsg("");
                ButtonType okButton = new ButtonType("Ok", ButtonBar.ButtonData.OK_DONE);
                ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
                Alert alert = new Alert(Alert.AlertType.WARNING,
                        "'Extra text' control is contains only space!\nDo you want to continue?", okButton, cancelButton);

                alert.setTitle("Contains space characters");
                Optional<ButtonType> result = alert.showAndWait();
                boolean okSelected = false;
                ButtonType resultButtonType = result.get();
                if (result.isPresent() && resultButtonType.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                    okSelected = true;
                }
                if (!okSelected)
                    return true;
            }

            /*
            if (checkBoxPrintTime.isSelected()) {
                String strValueOfPrintText = textFieldTimeText.getText();
                if (strValueOfPrintText == null || strValueOfPrintText.isEmpty()) {
                    setLabelMsg("'Print time text' control is empty! Give some value of it.");
                    return true;
                }
                if (strValueOfPrintText.trim().isEmpty() && strValueOfPrintText.contains(" ")) {
                    setLabelMsg("");
                    ButtonType okButton = new ButtonType("Ok", ButtonBar.ButtonData.OK_DONE);
                    ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
                    Alert alert = new Alert(Alert.AlertType.WARNING,
                            "'Print time text' control is contains only space!\nDo you want to continue?", okButton, cancelButton);

                    alert.setTitle("Contains space characters");
                    Optional<ButtonType> result = alert.showAndWait();
                    boolean okSelected = false;
                    ButtonType resultButtonType = result.get();
                    if (result.isPresent() && resultButtonType.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                        okSelected = true;
                    }
                    if (!okSelected)
                        return true;
                }
            }
             */
        }
        return ret;
    }

    /*
        comboBoxPageNumbers.setValue("RIGHT");
        comboBoxExtraText.setValue("CENTER");
        comboBoxPrintTime.setValue("LEFT");
     */

    private String getCurrentCommand()
    {
        String ret = "";
        StringBuffer sbCommand = new StringBuffer();
        if (checkBoxNumbering.isSelected())
        {
            if (comboBoxPageNumbers.getValue().equals("RIGHT"))
                sbCommand.append(" pagenumbers_right ");
            else
            if (comboBoxPageNumbers.getValue().equals("CENTER"))
                sbCommand.append(" pagenumbers_center ");
            else
            if (comboBoxPageNumbers.getValue().equals("LEFT"))
                sbCommand.append(" pagenumbers_left ");
            if (!textFieldPageText.getText().trim().isEmpty()) {
                sbCommand.append(" pagenumbers_text='" +textFieldPageText.getText().trim() +"' ");
            }
        }

        if (checkBoxPrintTime.isSelected())
        {
            if (checkPrintOnlyText.isSelected())
                sbCommand.append(" timetextonly ");
            if (comboBoxPrintTime.getValue().equals("RIGHT"))
                sbCommand.append(" printtime_right ");
            else
            if (comboBoxPrintTime.getValue().equals("CENTER"))
                sbCommand.append(" printtime_center ");
            else
            if (comboBoxPrintTime.getValue().equals("LEFT"))
                sbCommand.append(" printtime_left ");
            String strValue = textFieldTimeText.getText();
            if (strValue != null && !strValue.trim().isEmpty()) {
                sbCommand.append(" printtime_text='" +textFieldTimeText.getText().trim() +"' ");
            }
        }

        if (checkBoxPrintExtra.isSelected())
        {
            if (comboBoxExtraText.getValue().equals("RIGHT"))
                sbCommand.append(" printtext_right=");
            else
            if (comboBoxExtraText.getValue().equals("CENTER"))
                sbCommand.append(" printtext_center=");
            else
            if (comboBoxExtraText.getValue().equals("LEFT"))
                sbCommand.append(" printtext_left=");
            if (!textFiledExtraText.getText().trim().isEmpty()) {
                sbCommand.append("'" +textFiledExtraText.getText().trim() +"' ");
            }
        }
        if (sbCommand.length() > 0)
        {
            return " -command=\"" +sbCommand.toString().trim() +"\"";
        }
        return sbCommand.toString();
    }

    @FXML
    protected void checkBoxPrintExtraPressed()
    {
        if(checkBoxPrintExtra.isSelected())
            hBoxExtraText.setDisable(false);
        else
            hBoxExtraText.setDisable(true);
        checkIsAllowedToStartConversion();
    }

    @FXML
    protected void checkBoxNumberingPressed()
    {
        if (checkBoxNumbering.isSelected()) {
            hBoxNumbering.setDisable(false);
            hBoxNumbering2.setDisable(false);
        }
        else {
            hBoxNumbering.setDisable(true);
            hBoxNumbering2.setDisable(true);
        }
        checkIsAllowedToStartConversion();
    }

    @FXML
    protected void checkBoxPrintTimePressed()
    {
        if (checkBoxPrintTime.isSelected())
            hBoxPrintTime.setDisable(false);
        else
            hBoxPrintTime.setDisable(true);
        checkIsAllowedToStartConversion();
    }

    @FXML
    protected void buttonOutputDirPressed()
    {
        if (selectedDir != null)
            directoryOutChooser.setInitialDirectory(selectedDir);
        else
        if (inputPdfFile != null)
            directoryOutChooser.setInitialDirectory(new File(inputPdfFile.getParent()));

        File tmp_selectedDir = directoryOutChooser.showDialog(parentStage);
        if (tmp_selectedDir != null && tmp_selectedDir.exists() && tmp_selectedDir.isDirectory())
        {
            selectedDir = tmp_selectedDir;
            labelOutputDir.setText(selectedDir.getAbsolutePath());
        }
    }

    @FXML
    protected void buttonInputFilePressed() {

        setLabelMsg("");
        if (inputPdfFile != null)
            inputFileChooser.setInitialFileName(inputPdfFile.getAbsolutePath());
        File selectedPdf = inputFileChooser.showOpenDialog(parentStage);
        if (selectedPdf != null && selectedPdf.exists())
        {
            if (selectedPdf.getName().endsWith(".pdf")) {
                inputPdfFile = selectedPdf;
                labelInputPdf.setText(selectedPdf.getAbsolutePath());
                setLabelMsg("Input file had set: " +selectedPdf.getName());
            }
            else
                setLabelMsg("Input file must end with: .pdf!");
        }

    }
}