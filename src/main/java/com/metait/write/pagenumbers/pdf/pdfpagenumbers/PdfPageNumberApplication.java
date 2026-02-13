package com.metait.write.pagenumbers.pdf.pdfpagenumbers;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class PdfPageNumberApplication extends Application {
    static boolean bDebug = true;
    @Override
    public void start(Stage stage) throws IOException {
        PdfPageNumberController controller = new PdfPageNumberController();
        controller.setState(stage);
        FXMLLoader fxmlLoader = new FXMLLoader(PdfPageNumberApplication.class.getResource("pdfpagenumbers-view.fxml"));
        fxmlLoader.setController(controller);
        Scene scene = new Scene(fxmlLoader.load(), 920, 940);
        stage.setTitle("Set pdf page margin");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}