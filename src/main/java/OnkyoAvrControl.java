/*
Onkyo AVR Control - Java Program to control Onkyo AVRs over a local network
Copyright (C) 2017 Matthias Mitter

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import communication.Communication;
import communication.implementation.SimpleCommunication;
import gui.controller.MainFrameController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import service.CallBackService;
import service.Service;
import service.implementation.SimpleService;

import java.io.IOException;

import static java.lang.Math.min;

/**
 * Main class to launch the OnkyoAvrControl application
 */
public class OnkyoAvrControl extends Application {
    private static final Logger LOGGER = LogManager.getLogger(OnkyoAvrControl.class);

    private Service service;
    private Communication communication;

    /**
     * The main method.
     *
     * @param args the arguments
     */
    public static void main(String[] args) {
        Application.launch(args);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(final Stage primaryStage) {
        LOGGER.info("Starting Onkyo AVR Control.");
        Parent root;
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            root = fxmlLoader.load(getClass().getResource("gui/fxml/mainFrame.fxml").openStream());
            Scene scene = new Scene(root);

            MainFrameController mainFrameController = (MainFrameController) fxmlLoader.getController();

            if (mainFrameController == null) {
                LOGGER.error("mainFramController is null, program can't be initialized");
                return;
            }

            SimpleService simpleService = new SimpleService();
            Communication communication = new SimpleCommunication(simpleService);
            simpleService.setCommunication(communication);
            mainFrameController.setService(simpleService);

            this.service = simpleService;
            this.communication = communication;

            primaryStage.setScene(scene);
            primaryStage.show();
            // TODO delete this after scaling option integrated into UI
            // used for dev on highdpi linux (ubuntu gnome) system (4k)
            // where javafx doesn't automatically scale
            String osName = System.getProperty("os.name");
            if (osName.contains("Linux")) {
                this.scaleUI(primaryStage, 2);
            }

        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            // show error message?
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() throws Exception {
        super.stop();
        // TODO cleanup everything, threads and so on
        this.communication.close();
    }

    /**
     * private method here for now, gets moved to a ui controller late to be set by user
     * maybe return boolean -> successful or not
     * @param scalingFacator to factor to scale to UI
     *                       must be > 0
     */
    private void scaleUI(Stage primaryStage, int scalingFacator) {
        if (scalingFacator <= 0) {
            LOGGER.error("Scaling factor must be bigger than zero.");
            return;
        }
        LOGGER.info("Setting scaling factor to " + scalingFacator);
        Scale scale = new Scale(scalingFacator, scalingFacator);
        scale.setPivotX(0);
        scale.setPivotY(0);
        primaryStage.getScene().getRoot().getTransforms().setAll(scale);
        primaryStage.setHeight(min(primaryStage.getHeight() * scalingFacator, primaryStage.getMaxHeight()));
        primaryStage.setWidth(min(primaryStage.getWidth() * scalingFacator, primaryStage.getMaxWidth()));
        primaryStage.show();
    }
}