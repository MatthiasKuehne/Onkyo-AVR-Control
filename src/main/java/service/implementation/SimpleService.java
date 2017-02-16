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

package service.implementation;

import communication.Communication;
import dto.OnkyoDevice;
import dto.utils.EnumValueTranslation;
import gui.CallBackUI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import service.CallBackService;
import service.Service;

import java.net.InetAddress;
import java.net.InetSocketAddress;

public class SimpleService implements Service, CallBackService {
    private static final Logger LOGGER = LogManager.getLogger(SimpleService.class);

    Communication communication;
    // TODO List/queue? of UI callbacks...

    // just testing...
    CallBackUI callBackUI;

    public SimpleService() {}

    public SimpleService(Communication communication) {
        this.communication = communication;
    }

    @Override
    public void detectDevices() {
        String msg = buildEiscpMessage("ECNQSTN", "x").toString(); // TODO "outsource" to constant
        this.communication.detectDevicesUDP(msg);
    }

    @Override
    public void powerOn() {

    }

    @Override
    public void volumeUp() {

    }

    @Override
    public void volumeDown() {

    }

    @Override
    public void deviceDetectedCallBack(String message, InetAddress address) {
        OnkyoDevice onkyoDevice = this.getDeviceFromMessage(message, address);
        this.callBackUI.onkyoDeviceDetected(onkyoDevice);
    }

    /**
     * Wraps a command in a eiscp data message (data characters).
     * adapted method from Tom Gutwin, Eiscp.java in folder TomGutwin
     *
     * TODO outsource method
     *
     * @param command must be one of the Strings from the eiscp.Eiscp.Command class.
     * @param unitType the type of device, x for broadcast, 1 is receiver, 2 stereo
     *
     **/
    private StringBuilder buildEiscpMessage(String command, String unitType) {
        StringBuilder sb = new StringBuilder();
        int eiscpDataSize = command.length() + 2 ; // this is the eISCP data size
        int eiscpMsgSize = eiscpDataSize + 1 + 16 ; // this is the eISCP data size

        /* This is where I construct the entire message
        character by character. Each char is represented by a 2 digit hex value */
        sb.append("ISCP");
        // the following are all in HEX representing one char

        // 4 char Big Endian Header
        sb.append((char)Integer.parseInt("00", 16));
        sb.append((char)Integer.parseInt("00", 16));
        sb.append((char)Integer.parseInt("00", 16));
        sb.append((char)Integer.parseInt("10", 16));

        // 4 char  Big Endian data size
        sb.append((char)Integer.parseInt("00", 16));
        sb.append((char)Integer.parseInt("00", 16));
        sb.append((char)Integer.parseInt("00", 16));
        // the official ISCP docs say this is supposed to be just the data size  (eiscpDataSize)
        // ** BUT **
        // It only works if you send the size of the entire Message size (eiscpMsgSize)
        sb.append((char)Integer.parseInt(Integer.toHexString(eiscpMsgSize), 16));

        // eiscp_version = "01";
        sb.append((char)Integer.parseInt("01", 16));

        // 3 chars reserved = "00"+"00"+"00";
        sb.append((char)Integer.parseInt("00", 16));
        sb.append((char)Integer.parseInt("00", 16));
        sb.append((char)Integer.parseInt("00", 16));

        //  eISCP data
        // Start Character
        sb.append("!");

        // eISCP data - unittype char '1' is receiver
        sb.append(unitType);

        // eISCP data - 3 char command and param    ie PWR01
        sb.append(command);

        // msg end - EOF
        sb.append((char)Integer.parseInt("0D", 16));

        LOGGER.info("eISCP data size: "+eiscpDataSize +"(0x"+Integer.toHexString(eiscpDataSize) +") chars");
        LOGGER.info("eISCP msg size: "+sb.length() +"(0x"+Integer.toHexString(sb.length()) +") chars");

        return sb;
    }

    private String getIscpMessageFromRawMessage(String rawMessage) {
        // TODO parse header length...
        int headerLen = 16;
        String header = rawMessage.substring(0, headerLen);

        int eofIndex = rawMessage.indexOf("\u001a", headerLen);
        if (eofIndex < 0) {
            eofIndex = rawMessage.indexOf("\u0019", headerLen);
            if (eofIndex < 0) {
                eofIndex = rawMessage.indexOf("\r", headerLen);
                if (eofIndex < 0) {
                    eofIndex = rawMessage.indexOf("\n", headerLen);
                }
            }
        }

        String iscpMessage = rawMessage.substring(headerLen, eofIndex);
        return iscpMessage;
    }

    /**
     * maybe change to take not raw, but already iscp message...?
     * @param message
     * @return
     */
    private OnkyoDevice getDeviceFromMessage(String message, InetAddress address) {
        OnkyoDevice onkyoDevice = null;
        String iscpMessage = this.getIscpMessageFromRawMessage(message);
        String deviceCategoryStr = iscpMessage.substring(1,2);
        String[] messageSplit = iscpMessage.substring(5).split("/");
        if (messageSplit.length >= 3 ) {
            String modelName = messageSplit[0];
            int port = Integer.parseInt(messageSplit[1]);
            InetSocketAddress inetSocketAddress = new InetSocketAddress(address, port);
            String destinationAreaStr = messageSplit[2];
            onkyoDevice = new OnkyoDevice(EnumValueTranslation.convertToDeviceCategory(deviceCategoryStr), modelName,
                    inetSocketAddress, EnumValueTranslation.convertToDestinationArea(destinationAreaStr));

            if (messageSplit.length >= 4) {
                // also set mac address
                onkyoDevice.setMacAddress(messageSplit[3]);
            }

        } else {
            LOGGER.error("iscmp detection reply message is too short!");
            // TODO throw exception
        }
        return onkyoDevice;
    }

    public void setCommunication(Communication communication) {
        this.communication = communication;
    }

    public void setCallBackUI(CallBackUI callBackUI) {
        this.callBackUI = callBackUI;
    }
}
