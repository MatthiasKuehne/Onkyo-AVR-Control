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

import gui.CallBackUI;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import service.Service;

/**
 *
 */
public class MainFrameController implements CallBackUI {
    private static final Logger LOGGER = LogManager.getLogger(MainFrameController.class);

    private Service service;

    ///// methods and buttons mainly to test first functionalities ////

    @FXML
    private void detectDevicesButtonPressed() {
        if (this.service != null) {
            this.service.detectDevices();
        }
    }

    @FXML
    private void powerOnButtonPressed() {
        if (this.service != null) {
            this.service.detectDevices();
        }
    }

    @FXML
    private void volumeUpButtonPressed() {
        if (this.service != null) {
            this.service.detectDevices();
        }
    }

    @FXML
    private void volumeDownButtonPressed() {
        if (this.service != null) {
            this.service.detectDevices();
        }
    }

    //////////////////////////////////////7

    public void setService(Service service) {
        this.service = service;
    }
}
