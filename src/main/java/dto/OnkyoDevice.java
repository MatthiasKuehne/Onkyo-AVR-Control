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

package dto;

import dto.enums.DestinationArea;
import dto.enums.DeviceCategory;

import java.net.InetSocketAddress;

/**
 * encapsulates all relevant data of an onkyo device
 */
public class OnkyoDevice {

    private DeviceCategory deviceCategory;
    private String modelName;
    private InetSocketAddress inetSocketAddress; // IP and port number of device
    private DestinationArea destinationArea; // not really needed
    private String macAddress; // not really needed I guess...

    public OnkyoDevice(DeviceCategory deviceCategory, String modelName, InetSocketAddress inetSocketAddress, DestinationArea destinationArea) {
        this.deviceCategory = deviceCategory;
        this.modelName = modelName;
        this.inetSocketAddress = inetSocketAddress;
        this.destinationArea = destinationArea;
    }

    public DeviceCategory getDeviceCategory() {
        return deviceCategory;
    }

    public String getModelName() {
        return modelName;
    }

    public InetSocketAddress getInetSocketAddress() {
        return inetSocketAddress;
    }

    public DestinationArea getDestinationArea() {
        return destinationArea;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setInetSocketAddress(InetSocketAddress inetSocketAddress) {
        this.inetSocketAddress = inetSocketAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }
}
