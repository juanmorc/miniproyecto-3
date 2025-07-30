package controller;


import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.animation.ScaleTransition;
import javafx.util.Duration;
import javafx.scene.control.Label;

import java.io.IOException;


public class InicioController {

    @FXML private Button playButton, outButton;

    @FXML private Label navalLabel, battleLabel, furyLabel;

    @FXML
    public void initialize() {
        aplicarLatido(navalLabel);
        aplicarLatido(battleLabel);
        aplicarLatido(furyLabel);

        playButton.setOnAction(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/stage.fxml"));
                Parent root = loader.load();
                Stage newStage = new Stage();
                newStage.setTitle("Stage");
                newStage.setScene(new Scene(root));
                newStage.setResizable(false);
                newStage.show();

                Stage currentStage = (Stage) playButton.getScene().getWindow();
                currentStage.close();

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        outButton.setOnAction(e -> System.exit(0));
    }

    private void aplicarLatido(Label label) {
        ScaleTransition latido = new ScaleTransition(Duration.seconds(1), label);
        latido.setFromX(1.0);
        latido.setFromY(1.0);
        latido.setToX(1.1);
        latido.setToY(1.1);
        latido.setCycleCount(ScaleTransition.INDEFINITE);
        latido.setAutoReverse(true);
        latido.play();
    }
}