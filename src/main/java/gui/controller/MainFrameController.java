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

package gui.controller;

import dto.OnkyoDevice;
import gui.CallBackUI;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import service.Service;

import java.io.IOException;

/**
 *
 */
public class MainFrameController implements CallBackUI {
    private static final Logger LOGGER = LogManager.getLogger(MainFrameController.class);

    private Service service;

    @FXML
    private VBox onkyoDeviceListContainer;

    ///// methods and buttons mainly to test first functionalities ////

    @FXML
    private void detectDevicesButtonPressed() {
        if (this.service != null) {
            // first delete previously found devices from UI
            this.onkyoDeviceListContainer.getChildren().clear();

            this.service.detectDevices();
        }
    }

    @FXML
    private void powerOnButtonPressed() {
        if (this.service != null) {
            this.service.powerOn();
        }
    }

    @FXML
    private void volumeUpButtonPressed() {
        if (this.service != null) {
            this.service.volumeUp();
        }
    }

    @FXML
    private void volumeDownButtonPressed() {
        if (this.service != null) {
            this.service.volumeDown();
        }
    }

    //////////////////////////////////////7

    public void setService(Service service) {
        this.service = service;
    }



    // just UI testing //
    private void addOnkyoDeviceListItem(OnkyoDevice onkyoDevice) {

        // UI changes must run in JavaFX thread
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                FXMLLoader fxmlLoader2 = new FXMLLoader();
                Parent onkyoDeviceListitem;
                try {
                    onkyoDeviceListitem = fxmlLoader2.load(getClass().getClassLoader().getResource("gui/fxml/onkyoDeviceListItem.fxml").openStream());
                } catch (IOException e) {
                    LOGGER.error(e.getMessage());
                    return;
                }

                // set onkyoDevice in controller
                OnkyoDeviceListItemController onkyoDeviceListItemController = (OnkyoDeviceListItemController) fxmlLoader2.getController();
                onkyoDeviceListItemController.setOnkyoDevice(onkyoDevice);

                // show listItem in container
                onkyoDeviceListContainer.getChildren().add(onkyoDeviceListitem);
            }
        });
    }

    @Override
    public void onkyoDeviceDetected(OnkyoDevice onkyoDevice) {
        this.addOnkyoDeviceListItem(onkyoDevice);
    }
}
