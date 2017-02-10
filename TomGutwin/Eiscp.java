/*
 *  $Source: /cvsroot2/open/projects/WebARTS/ca/bc/webarts/widgets/Util.java,v $
 *  $Name:  $
 *
 *  $Revision: $
 *  $Date: 2010-07-18 20:44:20 -0700 (Sun, 18 Jul 2010) $
 *  $Locker:  $
 *
 *
 *  Written by Tom Gutwin - WebARTS Design.
 *  Copyright (C) 2010 WebARTS Design, North Vancouver Canada
 *  http://www.webarts.bc.ca
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without_ even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package ca.bc.webarts.tools.eiscp;

import java.io.*;
import java.net.*;

/**
 *  A class that wraps the coomunication to Onkyo/Integra devices using the
 *  ethernet Integra Serial Control Protocal (eISCP).
 *
 * @author     Tom Gutwin P.Eng
 */
public class Eiscp
{
  /**  A holder for this clients System File Separator.  */
  public final static String SYSTEM_FILE_SEPERATOR = File.separator;

  /**  A holder for this clients System line termination separator.  */
  public final static String SYSTEM_LINE_SEPERATOR =
                                           System.getProperty("line.separator");

  /**  The VM classpath (used in some methods)..  */
  public static String CLASSPATH = System.getProperty("class.path");

  /**  The users home ditrectory.  */
  public static String USERHOME = System.getProperty("user.home");

  /**  The users pwd ditrectory.  */
  public static String USERDIR = System.getProperty("user.dir");

  /**  A holder This classes name (used when logging).  */
  private static String CLASSNAME = "ca.bc.webarts.tools.Eiscp";

  /**  Class flag signifying if the initUtil method has been called  */
  private static boolean classInit = false;

  /** default receiver IP Address. **/
  private static final String DEFAULT_EISCP_IP = "10.0.0.203";

  /** default eISCP port. **/
  private static final int DEFAULT_EISCP_PORT = 60128;
  private static Socket eiscpSocket_ = null;
  private static ObjectOutputStream out_ = null;
  private static DataInputStream in_ = null;
  private static boolean connected_ = false;


  /**
   * Connects to the receiver by opening a socket connection through the DEFaULT IP and DEFAULT eISCP port.
   **/
   public boolean connectSocket() { return connectSocket(DEFAULT_EISCP_IP,DEFAULT_EISCP_PORT);}


  /**
   * Connects to the receiver by opening a socket connection through the DEFAULT eISCP port.
   **/
   public boolean connectSocket(String ip) { return connectSocket(ip,DEFAULT_EISCP_PORT);}


  /**
   * Connects to the receiver by opening a socket connection through the eISCP port.
   **/
  public boolean connectSocket(String ip, int eiscpPort)
  {
    if (ip==null || ip.equals("")) ip=DEFAULT_EISCP_IP;
    if (eiscpPort==0 ) eiscpPort=DEFAULT_EISCP_PORT;
    try
    {
      //1. creating a socket to connect to the server
      eiscpSocket_ = new Socket(ip, eiscpPort);
      System.out.println("Connected to "+ip+" on port "+eiscpPort);
      //2. get Input and Output streams
      out_ = new ObjectOutputStream(eiscpSocket_.getOutputStream());
      System.out.println("out_Init");
      out_.flush();
      // System.out.println("inInit");
      connected_ = true;
    }
    catch(UnknownHostException unknownHost)
    {
      System.err.println("You are trying to connect to an unknown host!");
    }
    catch(IOException ioException)
    {
      ioException.printStackTrace();
    }
    return connected_;
  }


  /**
   * Closes the socket connection.
   * @return true if the closed succesfully
   **/
  public boolean closeSocket()
  {
    //4: Closing connection
    try
    {
      if (in_!=null) in_.close();
      if (out_!=null) out_.close();
      if (eiscpSocket_!=null) eiscpSocket_.close();
      System.out.println("closed connections");
      connected_ = false;
    }
    catch(IOException ioException)
    {
      ioException.printStackTrace();
    }
    return connected_;
  }


  /** Converts an ascii String to a hex  String **/
  public static String convertStringToHex(String str)
  {
     return convertStringToHex( str, false);
  }


  /** Converts an ascii String to a hex  String **/
  public static String convertStringToHex(String str,  boolean dumpOut)
  {
    char[] chars = str.toCharArray();
    String out_put = "";

    if (dumpOut) System.out.println("Ascii: "+str);
    if (dumpOut) System.out.print("Hex: ");
    StringBuffer hex = new StringBuffer();
    for(int i = 0; i < chars.length; i++)
    {
      out_put = Integer.toHexString((int)chars[i]);
      if (out_put.length()==1) hex.append("0");
      hex.append(out_put);
      if (dumpOut) System.out.print("0x"+(out_put.length()==1?"0":"")+ out_put+" ");
    }
    if (dumpOut) System.out.println("");

    return hex.toString();
  }


  /** Converts a hex String to an ascii String **/
  public static String convertHexToString(String hex)
  {
    return convertHexToString( hex, false);
  }


  /** Converts a hex String to an ascii String **/
  public static String convertHexToString(String hex,  boolean dumpOut)
  {

    StringBuilder sb = new StringBuilder();
    StringBuilder temp = new StringBuilder();
    String out_put = "";

    if (dumpOut) System.out.print("Hex: ");
    //49204c6f7665204a617661 split into two characters 49, 20, 4c...
    for( int i=0; i<hex.length()-1; i+=2 ){

        //grab the hex in pairs
        out_put = hex.substring(i, (i + 2));
        if (dumpOut) System.out.print("0x"+out_put+" ");
        //convert hex to decimal
        int decimal = Integer.parseInt(out_put, 16);
        //convert the decimal to character
        sb.append((char)decimal);

        temp.append(decimal);
    }
    if (dumpOut) System.out.println("Decimal : " + temp.toString());

    return sb.toString();
  }


  /**
    * Wraps a command in a eiscp data message (data characters).
    *
    * @param command must be one of the Strings from the eiscp.Eiscp.Command class.
    **/
  public StringBuilder getEiscpMessage(String command)
  {
    StringBuilder sb = new StringBuilder();
    int eiscpDataSize = command.length() + 2 ; // this is the eISCP data size
    int eiscpMsgSize = eiscpDataSize + 1 + 16 ; // this is the eISCP data size

    /* This is where I construct the entire message
        character by character. Each char is represented by a 2 disgit hex value */
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
    sb.append("1");

    // eISCP data - 3 char command and param    ie PWR01
    sb.append( command);

    // msg end - EOF
    sb.append((char)Integer.parseInt("0D", 16));

    System.out.println("eISCP data size: "+eiscpDataSize +"(0x"+Integer.toHexString(eiscpDataSize) +") chars");
    System.out.println("eISCP msg size: "+sb.length() +"(0x"+Integer.toHexString(sb.length()) +") chars");


    return sb;
  }


  /**
    * Sends to command to the receiver and does not wait for a reply
    *
    * @param command must be one of the Strings from the eiscp.Eiscp.Command class.
    **/
  public void sendCommand(String command)
  {
    sendCommand(command, true);
  }


  /**
    * Sends to command to the receiver and does not wait for a reply
    *
    * @param command must be one of the Strings from the eiscp.Eiscp.Command class.
    * @param closeSocket flag to close the connection when done or leave it open.
    **/
  public void sendCommand(String command, boolean closeSocket)
  {
    StringBuilder sb = getEiscpMessage(command);

    if(connectSocket())
    {
      try
      {
        System.out.println("sending "+sb.length() +" chars: ");
        convertStringToHex(sb.toString(), true);
        //out_.writeObject(sb.toString());
        //out_.writeChars(sb.toString());
        out_.writeBytes(sb.toString());  // <--- This is the one that works
        //out_.writeBytes(convertStringToHex(sb.toString(), false));
        //out_.writeChars(convertStringToHex(sb.toString(), false));
        out_.flush();
        System.out.println("sent!" );
      }
      catch(IOException ioException)
      {
        ioException.printStackTrace();
      }
    }
    if (closeSocket) closeSocket();
  }


  /**
    * Sends to command to the receiver and waits for a reply
    *
    * @param command must be one of the Strings from the eiscp.Eiscp.Command class.
    * @return the response to the command
    **/
  public String queryCommand(String command)
  {
    return queryCommand(command, true);
  }


  /**
    * Sends to command to the receiver and waits for a reply
    *
    * @param command must be one of the Strings from the eiscp.Eiscp.Command class.
    * @param closeSocket flag to close the connection when done or leave it open.
    * @return the response to the command
    **/
  public String queryCommand(String command, boolean closeSocket)
  {
    String retVal = "";

    sendCommand(command,false);

    // now listen for the response
    if(connected_)
    {
      try
      {
        in_ = new DataInputStream(eiscpSocket_.getInputStream());
        byte [] responseBytes = new byte[32] ; 
        int numBytesReceived = 0;
        
        numBytesReceived = in_.read(responseBytes);
        retVal = new String(responseBytes);
        System.out.println("received "+numBytesReceived+" bytes: \""+retVal+"\"" );
      }
      catch(EOFException  eofException)
      {
        System.out.println("received: \""+retVal+"\"" );
      }
      catch(IOException ioException)
      {
        ioException.printStackTrace();
      }
    }
    else 
      System.out.println(" Not Connected to Receive ");
    if (closeSocket) closeSocket();

    return retVal ;
  }


  /**  Class main commandLine entry method.*/
  public static void main(String [] args)
  {
    final String methodName = CLASSNAME + ": main()";
    Eiscp instance = new Eiscp();

    StringBuffer helpMsg = new StringBuffer(SYSTEM_LINE_SEPERATOR);
    helpMsg.append("---  WebARTS Eiscp Class  --------------------------------");
    helpMsg.append("----------------------");
    helpMsg.append(SYSTEM_LINE_SEPERATOR);
    helpMsg.append("WebARTS Eiscp Class");
    helpMsg.append(SYSTEM_LINE_SEPERATOR);
    helpMsg.append("SYNTAX:");
    helpMsg.append(SYSTEM_LINE_SEPERATOR);
    helpMsg.append("   java ");
    helpMsg.append(CLASSNAME);
    helpMsg.append(" command methodArgs");
    helpMsg.append(SYSTEM_LINE_SEPERATOR);
    helpMsg.append(SYSTEM_LINE_SEPERATOR);
    helpMsg.append("Available Methods:");
    helpMsg.append(SYSTEM_LINE_SEPERATOR);
    helpMsg.append("-->   POWER_ON");
    helpMsg.append(SYSTEM_LINE_SEPERATOR);
    helpMsg.append("-->   MUTE");
    helpMsg.append(SYSTEM_LINE_SEPERATOR);
    helpMsg.append("-->   UNMUTE");
    helpMsg.append(SYSTEM_LINE_SEPERATOR);
    helpMsg.append("---------------------------------------------------------");
    helpMsg.append("----------------------");
    helpMsg.append(SYSTEM_LINE_SEPERATOR);

    if (args ==null || args.length<1)
      System.out.println(helpMsg.toString());
    else
    {
      if (args[0].equals("test"))
      {
        System.out.println("Testing Eiscp");
        instance.queryCommand(VOLUME_QUERY);
        instance.sendCommand(MUTE);
      }
      else
      {
        // Parse the command
        String command = "";
        if (args[0].equalsIgnoreCase("POWER_OFF")) command = POWER_OFF;
        else if (args[0].equalsIgnoreCase("POWER_ON")) command = POWER_ON;

        else if (args[0].equalsIgnoreCase("UNMUTE")) command = UNMUTE;
        else if (args[0].equalsIgnoreCase("MUTE")) command = MUTE;
        else if (args[0].equalsIgnoreCase("VOLUME_UP")) command = VOLUME_UP;
        else if (args[0].equalsIgnoreCase("VOLUME_DOWN")) command = VOLUME_DOWN;
        else if (args[0].equalsIgnoreCase("VOLUME_QUERY")) command = VOLUME_QUERY ;
        else if (args[0].equalsIgnoreCase("SETVOLUME")) command = setVolume(Integer.parseInt(args[1]));

        else if (args[0].equalsIgnoreCase("SOURCE_DVR")) command = SOURCE_DVR;
        else if (args[0].equalsIgnoreCase("SOURCE_SATELLITE")) command = SOURCE_SATELLITE;
        else if (args[0].equalsIgnoreCase("SOURCE_AUXILIARY")||
                 args[0].equalsIgnoreCase("SOURCE_AUX")) command = SOURCE_AUX;
        else if (args[0].equalsIgnoreCase("SOURCE_COMPUTER")||
                 args[0].equalsIgnoreCase("SOURCE_PC")) command = SOURCE_COMPUTER;
        else if (args[0].equalsIgnoreCase("SOURCE_BLURAY")) command = SOURCE_BLURAY;
        else if (args[0].equalsIgnoreCase("SOURCE_TAPE1")) command = SOURCE_TAPE1;
        else if (args[0].equalsIgnoreCase("SOURCE_TAPE2")) command = SOURCE_TAPE2;
        else if (args[0].equalsIgnoreCase("SOURCE_PHONO")) command = SOURCE_PHONO;
        else if (args[0].equalsIgnoreCase("SOURCE_CD")) command = SOURCE_CD;
        else if (args[0].equalsIgnoreCase("SOURCE_FM")) command = SOURCE_FM;
        else if (args[0].equalsIgnoreCase("SOURCE_AM")) command = SOURCE_AM;
        else if (args[0].equalsIgnoreCase("SOURCE_TUNER")) command = SOURCE_TUNER;
        else if (args[0].equalsIgnoreCase("SOURCE_MUSICSERVER")) command = SOURCE_MUSICSERVER;
        else if (args[0].equalsIgnoreCase("SOURCE_INTERETRADIO")) command = SOURCE_INTERETRADIO;
        else if (args[0].equalsIgnoreCase("SOURCE_USB")) command = SOURCE_USB;
        else if (args[0].equalsIgnoreCase("SOURCE_USB_BACK")) command = SOURCE_USB_BACK;
        else if (args[0].equalsIgnoreCase("SOURCE_NETWORK")) command = SOURCE_NETWORK;
        else if (args[0].equalsIgnoreCase("SOURCE_MULTICH")) command = SOURCE_MULTICH;
        else if (args[0].equalsIgnoreCase("SOURCE_SIRIUS")) command = SOURCE_SIRIUS;
        else if (args[0].equalsIgnoreCase("SOURCE_UP")) command = SOURCE_UP;
        else if (args[0].equalsIgnoreCase("SOURCE_DOWN")) command = SOURCE_DOWN;
        else if (args[0].equalsIgnoreCase("SOURCE_GAME")) command = SOURCE_GAME;
        else if (args[0].equalsIgnoreCase("SOURCE_QUERY")) command = SOURCE_QUERY;

        else if (args[0].equalsIgnoreCase("VIDEO_WIDE_AUTO")) command = VIDEO_WIDE_AUTO;
        else if (args[0].equalsIgnoreCase("VIDEO_WIDE_43")) command = VIDEO_WIDE_43;
        else if (args[0].equalsIgnoreCase("VIDEO_WIDE_FULL")) command = VIDEO_WIDE_FULL;
        else if (args[0].equalsIgnoreCase("VIDEO_WIDE_ZOOM")) command = VIDEO_WIDE_ZOOM;
        else if (args[0].equalsIgnoreCase("VIDEO_WIDE_WIDEZOOM")) command = VIDEO_WIDE_WIDEZOOM;
        else if (args[0].equalsIgnoreCase("VIDEO_WIDE_SMARTZOOM")) command = VIDEO_WIDE_SMARTZOOM;
        else if (args[0].equalsIgnoreCase("VIDEO_WIDE_NEXT")) command = VIDEO_WIDE_NEXT;
        else if (args[0].equalsIgnoreCase("VIDEO_WIDE_QUERY")) command = VIDEO_WIDE_QUERY;

        else if (args[0].equalsIgnoreCase("LISTEN_MODE_STEREO")) command = LISTEN_MODE_STEREO;
        else if (args[0].equalsIgnoreCase("LISTEN_MODE_ALCHANSTEREO")) command = LISTEN_MODE_ALCHANSTEREO;
        else if (args[0].equalsIgnoreCase("LISTEN_MODE_AUDYSSEY_DSX")) command = LISTEN_MODE_AUDYSSEY_DSX;
        else if (args[0].equalsIgnoreCase("LISTEN_MODE_PLII_MOVIE_DSX")) command = LISTEN_MODE_PLII_MOVIE_DSX;
        else if (args[0].equalsIgnoreCase("LISTEN_MODE_PLII_MUSIC_DSX")) command = LISTEN_MODE_PLII_MUSIC_DSX;
        else if (args[0].equalsIgnoreCase("LISTEN_MODE_PLII_GAME_DSX")) command = LISTEN_MODE_PLII_GAME_DSX;
        else if (args[0].equalsIgnoreCase("LISTEN_MODE_NEO_CINEMA_DSX")) command = LISTEN_MODE_NEO_CINEMA_DSX;
        else if (args[0].equalsIgnoreCase("LISTEN_MODE_NEO_MUSIC_DSX")) command = LISTEN_MODE_NEO_MUSIC_DSX;
        else if (args[0].equalsIgnoreCase("LISTEN_MODE_NEURAL_SURROUND_DSX")) command = LISTEN_MODE_NEURAL_SURROUND_DSX;
        else if (args[0].equalsIgnoreCase("LISTEN_MODE_NEURAL_DIGITAL_DSX")) command = LISTEN_MODE_NEURAL_DIGITAL_DSX;
        else if (args[0].equalsIgnoreCase("LISTEN_MODE_QUERY")) command = LISTEN_MODE_QUERY;

        
        //send the command
        String queryResponse = "";
        if (!command.equals(""))
          if(args[0].endsWith("QUERY") )
          {
            queryResponse = instance.queryCommand(command);
            System.out.println(command +" response:" +
              queryResponse.substring(18,(queryResponse.length()-9)));
          }
          else
            instance.sendCommand(command);
      }
    }
  } // main

      public static  final String POWER_OFF = "PWR00";
      public  static final String POWER_ON  = "PWR01";

      public static final String UNMUTE       = "AMT00";
      public static final String MUTE         = "AMT01";
      public static  final String VOLUME_UP    = "MVLUP";
      public static final String VOLUME_DOWN  = "MVLDOWN";
      public static  final String VOLUME_QUERY  = "MVLQSTN";

      public static  String setVolume(int vol){return "MVL"+Integer.toHexString(vol);}

      public static final String SOURCE_DVR  = "SLI00";
      public static final String SOURCE_SATELLITE  = "SLI01";
      public static final String SOURCE_GAME  = "SLI02";
      public static final String SOURCE_AUX = "SLI03";
      public static final String SOURCE_VIDEO5  = "SLI04";
      public static final String SOURCE_COMPUTER  = "SLI05";
      //public static final String SOURCE_VIDEO6    = "SLI05";
      //public static final String SOURCE_VIDEO7    = "SLI06";
      public static final String SOURCE_BLURAY    = "SLI10";
      public static final String SOURCE_TAPE1     = "SLI20";
      public static final String SOURCE_TAPE2     = "SLI21";
      public static final String SOURCE_PHONO     = "SLI22";
      public static final String SOURCE_CD        = "SLI23";
      public static final String SOURCE_FM     = "SLI24";
      public static final String SOURCE_AM     = "SLI25";
      public static final String SOURCE_TUNER     = "SLI26";
      public static final String SOURCE_MUSICSERVER     = "SLI27";
      public static final String SOURCE_INTERETRADIO    = "SLI28";
      public static final String SOURCE_USB    = "SLI29";
      public static final String SOURCE_USB_BACK    = "SLI2A";
      public static final String SOURCE_NETWORK    = "SLI2C";
      public static final String SOURCE_MULTICH     = "SLI30";
      //public static final String SOURCE_XM     = "SLI31";
      public static final String SOURCE_SIRIUS     = "SLI32";
      public static final String SOURCE_UP     = "SLIUP";
      public static final String SOURCE_DOWN     = "SLIDOWN";
      public static final String SOURCE_QUERY     = "SLIQSTN";

      public static final String VIDEO_WIDE_AUTO     = "VWM00";
      public static final String VIDEO_WIDE_43     = "VWM01";
      public static final String VIDEO_WIDE_FULL     = "VWM02";
      public static final String VIDEO_WIDE_ZOOM     = "VWM03";
      public static final String VIDEO_WIDE_WIDEZOOM     = "VWM04";
      public static final String VIDEO_WIDE_SMARTZOOM     = "VWM05";
      public static final String VIDEO_WIDE_NEXT     = "VWMUP";
      public static final String VIDEO_WIDE_QUERY     = "VWMQSTN";

      public static final String LISTEN_MODE_STEREO     = "LMD00";
      public static final String LISTEN_MODE_ALCHANSTEREO     = "LMD0C";

      public static final String LISTEN_MODE_AUDYSSEY_DSX     = "LMD16";
      public static final String LISTEN_MODE_PLII_MOVIE_DSX     = "LMDA0";
      public static final String LISTEN_MODE_PLII_MUSIC_DSX     = "LMDA1";
      public static final String LISTEN_MODE_PLII_GAME_DSX     = "LMDA2";
      public static final String LISTEN_MODE_NEO_CINEMA_DSX     = "LMDA3";
      public static final String LISTEN_MODE_NEO_MUSIC_DSX     = "LMDA4";
      public static final String LISTEN_MODE_NEURAL_SURROUND_DSX     = "LMDA5";
      public static final String LISTEN_MODE_NEURAL_DIGITAL_DSX     = "LMDA6";
      public static final String LISTEN_MODE_QUERY    = "LMDQSTN";

} // class
