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

package communication.implementation;

import communication.CallBackCommunication;
import communication.Communication;
import service.CallBackService;

import java.net.InetAddress;

public class SimpleCommunication implements Communication, CallBackCommunication {

    private CallBackService callBackService;

    public SimpleCommunication(CallBackService callBackService) {
        this.callBackService = callBackService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void detectDevicesUDP(String message) {
        byte[] data = "!xECNQSTN".getBytes(); // TODO: outsource command
        // TODO get local network address
//        InetAddress.getLocalHost().get
//        InetAddress address = InetAddress.getByName();
    }

    @Override
    public void deviceDetectedCallBack(String message) {

    }
}
