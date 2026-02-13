module com.metait.write.pagenumbers.pdf.pdfpagenumbers {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.github.librepdf.openpdf;
    requires java.desktop;


    opens com.metait.write.pagenumbers.pdf.pdfpagenumbers to javafx.fxml;
    exports com.metait.write.pagenumbers.pdf.pdfpagenumbers;
}