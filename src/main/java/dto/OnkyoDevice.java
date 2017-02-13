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

import dto.enums.DeviceCategory;

import java.net.InetSocketAddress;

public class OnkyoDevice {

    private DeviceCategory deviceCategory;
    private String modelName;
    private InetSocketAddress inetSocketAddress; // IP and port number of device
    private String destinationArea; // not really needed, fixed 2 characters
    private String macAddress; // not really needed I guess...

}
