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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class ListenerUDP implements Runnable, Closeable {
    private static final Logger LOGGER = LogManager.getLogger(ListenerUDP.class);

    private DatagramSocket datagramSocket;
    private CallBackCommunication callBackCommunication;

    public ListenerUDP(DatagramSocket datagramSocket, CallBackCommunication callBackCommunication) {
        this.datagramSocket = datagramSocket;
        this.callBackCommunication = callBackCommunication;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        byte[] data; // initialize here?
        DatagramPacket packet;
        while(!Thread.currentThread().isInterrupted()) {
            data = new byte[1024]; // should be more than enough space for the message
            packet = new DatagramPacket(data, data.length);
            try {
                this.datagramSocket.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength());
                this.callBackCommunication.deviceDetectedCallBack(message);
            } catch (IOException e) {
                // TODO handle exception -> callback in communication?
                if (!Thread.currentThread().isInterrupted()) {
                    LOGGER.error(e.getMessage());
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        if (this.datagramSocket != null) {
            this.datagramSocket.close();
        }
    }
}
