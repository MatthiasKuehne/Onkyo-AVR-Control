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
import dto.utils.EnumValueTranslation;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;


public class OnkyoDeviceListItemController {

    @FXML
    private Label deviceCategory;

    @FXML
    private Label deviceModelAndRegion;

    @FXML
    private Label deviceIp;

    private OnkyoDevice onkyoDevice;

    public OnkyoDeviceListItemController() {}

    public OnkyoDeviceListItemController(OnkyoDevice onkyoDevice) {
        this.onkyoDevice = onkyoDevice;
        this.setLabelText();
    }

    private void setLabelText() {
        if (this.onkyoDevice != null) {
            this.deviceCategory.setText(EnumValueTranslation.convertFromDeviceCategory(this.onkyoDevice.getDeviceCategory()));
            this.deviceModelAndRegion.setText(this.onkyoDevice.getModelName()
                    + " " + EnumValueTranslation.convertFromDestinationArea(this.onkyoDevice.getDestinationArea()));
            this.deviceIp.setText(this.onkyoDevice.getInetSocketAddress().toString());
        }
    }

    public OnkyoDevice getOnkyoDevice() {
        return onkyoDevice;
    }

    public void setOnkyoDevice(OnkyoDevice onkyoDevice) {
        this.onkyoDevice = onkyoDevice;
        this.setLabelText();
    }
}
