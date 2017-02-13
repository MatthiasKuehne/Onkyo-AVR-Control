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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import service.CallBackService;

import java.io.Closeable;
import java.io.IOException;
import java.net.*;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SimpleCommunication implements Communication, CallBackCommunication {
    private static final Logger LOGGER = LogManager.getLogger(SimpleCommunication.class);

    private CallBackService callBackService;
    private ExecutorService threadPool;
    private ListenerUDP listenerUDP;

    public SimpleCommunication(CallBackService callBackService) {
        this.callBackService = callBackService;
        this.threadPool = Executors.newCachedThreadPool();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void detectDevicesUDP(String message) {
        byte[] data = message.getBytes();
        InetAddress broadCastAddress = null;
        try {
            broadCastAddress = this.getLocalBroadcastIp();
        } catch (SocketException e) {
            LOGGER.error(e.getMessage());
        } catch (UnknownHostException e) {
            LOGGER.error(e.getMessage());
        }

        DatagramPacket packet = new DatagramPacket(data, data.length, broadCastAddress, 60128); // TODO outsource port to constant
        LOGGER.info("UDP packet sent.");
        try {
            DatagramSocket socket = new DatagramSocket();
            socket.send(packet);
            if (this.listenerUDP != null) {
                // TODO first close open listener and thread
            }
            this.listenerUDP = new ListenerUDP(socket, this);
            this.threadPool.execute(this.listenerUDP);
            LOGGER.info("UDP Listener started");
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
    }

    @Override
    public void deviceDetectedCallBack(String message) {
        LOGGER.info("Detected device: " + message);
        // TODO call back service
        this.callBackService.deviceDetectedCallBack(message);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        // TODO close everything
        this.listenerUDP.close();
        this.threadPool.shutdown();
        this.threadPool.shutdownNow();
    }

    private InetAddress getLocalBroadcastIp() throws UnknownHostException, SocketException {
        InetAddress localAddress = InetAddress.getLocalHost();
        LOGGER.info("Local Address from java API: " + localAddress.getHostAddress());

        String localAddressString = null;

        // API not always returning correct ip, this works better from
        // http://stackoverflow.com/questions/9481865/getting-the-ip-address-of-the-current-machine-using-java
        try (final DatagramSocket socket = new DatagramSocket()){
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            localAddressString = socket.getLocalAddress().getHostAddress();
            LOGGER.info("UDP socket worked: local address: " + localAddressString);
        }

        InetAddress broadCastAddress = null;
        Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
        for (NetworkInterface net : Collections.list(nets)) {
            List<InterfaceAddress> interfaceAddressList = net.getInterfaceAddresses();
            for (InterfaceAddress interfaceAddress : interfaceAddressList) {
                if (localAddressString != null) {
                    // udp socket "trick" did work
                    if (interfaceAddress.getAddress().getHostAddress().equals(localAddressString)) {
                        broadCastAddress = interfaceAddress.getBroadcast();
                        LOGGER.info("Found broadcast address: " + interfaceAddress.getBroadcast());
                    }
                } else {
                    // well maybe the java api returned the correct ip address
                    if (interfaceAddress.getAddress().equals(localAddress)) {
                        broadCastAddress = interfaceAddress.getBroadcast();
                        LOGGER.info("Found broadcast address: " + interfaceAddress.getBroadcast());
                    }
                }
            }
        }
        return broadCastAddress;
    }
}
