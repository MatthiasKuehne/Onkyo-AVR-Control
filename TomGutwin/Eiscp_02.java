/*
 *  $URL: svn://svn.webarts.bc.ca/open/trunk/projects/WebARTS/ca/bc/webarts/tools/eiscp/Eiscp.java $
 *  $Author: tgutwin $
 *  $Revision: 624 $
 *  $Date: 2014-04-08 20:28:58 -0700 (Tue, 08 Apr 2014) $
 */
/*
 *
 *  Written by Tom Gutwin - WebARTS Design.
 *  Copyright (C) 2012-2014 WebARTS Design, North Vancouver Canada
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
import java.util.HashMap;
import java.util.TreeSet;
import java.util.Iterator;
import java.util.Vector;

/**
 *  A class that wraps the comunication to Onkyo/Integra devices using the
 *  ethernet Integra Serial Control Protocal (eISCP). This class uses class
 *  constants and commandMaps to help handling of the many iscp Commands.
 *  <br />
 *  The Message packet looks like:<br />
 *  <img src="http://tom.webarts.ca/_/rsrc/1320209141605/Blog/new-blog-items/javaeiscp-integraserialcontrolprotocol/eISCP-Packet.png" border="1"/>
 *  <br /> See also <a href="http://tom.webarts.ca/Blog/new-blog-items/javaeiscp-integraserialcontrolprotocol" > tom.webarts.ca</a> writeup.
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
  private static String CLASSNAME = "ca.bc.webarts.tools.eiscp.Eiscp";

  /**  Class flag signifying if the initUtil method has been called  */
  private static boolean classInit = false;

  /**  Class flag signifying if debugging_ messages are ptinted */
  private static boolean debugging_ = false;

  /** default receiver IP Address. **/
  private static final String DEFAULT_EISCP_IP = "10.0.0.203";
  /** Instantiated class IP for the receiver to communicate with. **/
  private String receiverIP_ = DEFAULT_EISCP_IP;

  /** default eISCP port. **/
  private static final int DEFAULT_EISCP_PORT = 60128;
  /** Instantiated class Port for the receiver to communicate with. **/
  private int receiverPort_ = DEFAULT_EISCP_PORT;

  /** the socket for communication - the protocol spec says to use one socket connection AND HOLD ONTO IT for re-use. **/
  private static Socket eiscpSocket_ = null;
  /** the timeout in ms for socket reads. **/
  private static int socketTimeOut_ = 500;
  private static ObjectOutputStream out_ = null;
  private static DataInputStream in_ = null;
  private static boolean connected_ = false;

  private static IscpCommands iscp_ = IscpCommands.getInstance();

  /** Maps the class contant vars to the eiscp command string. **/
  private static HashMap<Integer, String> commandMap_ = null;

  /** Maps a Readable string to a corresponding class var. **/
  private static HashMap<String, Integer> commandNameMap_ = null;

  /** Var to hold the volume level to or from a message. **/
  private static int volume_ = 32;

  private static StringBuffer helpMsg_ = new StringBuffer(SYSTEM_LINE_SEPERATOR);

  /** Simple class Constructor (using deafult IP and port) that gets all the class command constants set-up along with their command lookup maps (commandNameMap_ and commandMap_) . **/
  public Eiscp()
  {
    //initCommandMap();
  }


  /** Constructor that takes your receivers ip and default port, gets all the class command
   * constants set-up along with their command lookup maps (commandNameMap_ and commandMap_) .
   **/
  public Eiscp(String ip)
  {
    //initCommandMap();
    if (ip==null || ip.equals(""))
      receiverIP_=DEFAULT_EISCP_IP;
    else
      receiverIP_=ip;
    receiverPort_=DEFAULT_EISCP_PORT;
  }


  /** Constructor that takes your receivers ip and port,  gets all the class command
   * constants set-up along with their command lookup maps (commandNameMap_ and commandMap_) .
   **/
  public Eiscp(String ip, int eiscpPort)
  {
    //initCommandMap();
    if (ip==null || ip.equals(""))
      receiverIP_=DEFAULT_EISCP_IP;
    else
      receiverIP_=ip;
    if (eiscpPort<1 )
      receiverPort_=DEFAULT_EISCP_PORT;
    else
      receiverPort_=eiscpPort;
  }

  /** Makes Chocolate glazed doughnuts. **/
  public void setReceiverIP( String ip) { receiverIP_ = ip;}
  /** Makes Sprinkle doughnuts. **/
  public String getReceiverIP() {return receiverIP_;}
  /** Makes mini doughnuts. **/
  public void setReceiverPort( int port) { receiverPort_ = port;}
  /** Makes glazed doughnuts. **/
  public int getReceiverPort() {return receiverPort_;}

  /**
   * Connects to the receiver by opening a socket connection through the DEFaULT IP and DEFAULT eISCP port.
   **/
   public boolean connectSocket() { return connectSocket(null, -1);}


  /**
   * Connects to the receiver by opening a socket connection through the DEFAULT eISCP port.
   **/
   public boolean connectSocket(String ip) { return connectSocket(ip,-1);}


  /**
   * Connects to the receiver by opening a socket connection through the eISCP port.
   **/
  public boolean connectSocket(String ip, int eiscpPort)
  {
    if (ip==null || ip.equals("")) ip=receiverIP_;
    if (eiscpPort<1 ) eiscpPort=receiverPort_;

    if (eiscpSocket_==null || !connected_ || !eiscpSocket_.isConnected())
    try
    {
      //1. creating a socket to connect to the server
      eiscpSocket_ = new Socket(ip, eiscpPort);
      System.out.println("Connected to "+ip+" on port "+eiscpPort);
      //2. get Input and Output streams
      out_ = new ObjectOutputStream(eiscpSocket_.getOutputStream());
      in_ = new DataInputStream(eiscpSocket_.getInputStream());

      //System.out.println("out_Init");
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
      System.err.println("Can't Connect: "+ioException.getMessage());
    }
    return connected_;
  }


  /**
   * Tests the Connection to the receiver by opening a socket connection through the DEFaULT IP and DEFAULT eISCP port.
   * @return true if already connected or can connect, and false if can't connect
   **/
   public boolean testConnection() { return testConnection(DEFAULT_EISCP_IP,DEFAULT_EISCP_PORT);}


  /**
   * Tests the Connection to the receiver by opening a socket connection through the specified IP and DEFAULT eISCP port.
   * @param ip is the ip address (as a String) of the AV receiver to connect
   * @return true if already connected or can connect, and false if can't connect
   **/
   public boolean testConnection(String ip) { return testConnection(DEFAULT_EISCP_IP,DEFAULT_EISCP_PORT);}


  /**
   * test the connection to the receiver by opening a socket connection through the eISCP port AND THEN CLOSES it if it was not already open.
   * this method can be used when you need to specificly specify the IP and PORT. If the default port is used then you could also use the
   * {@link #testConnection(String) testConnection} method (that used the default port) or the {@link #testConnection() testConnection}
   * method (that used the default IP and port).
   *
   * @param ip is the ip address (as a String) of the AV receiver to connect
   * @param eiscpPort is the IP Port of the AV receiver to connect with (Onkyo's default is 60128)
   * @return true if already connected or can connect, and false if can't connect
   **/
  public boolean testConnection(String ip, int eiscpPort)
  {
    boolean retVal = false;
    if (ip==null || ip.equals("")) ip=DEFAULT_EISCP_IP;
    if (eiscpPort==0 ) eiscpPort=DEFAULT_EISCP_PORT;

    if (connected_)
    {
      // test existing connection
      if (eiscpSocket_.isConnected()) retVal = true;
    }
    else
    {
      // test a new connection
      try
      {
        //1. creating a socket to connect to the server
        eiscpSocket_ = new Socket(ip, eiscpPort);
        if (eiscpSocket_!=null) eiscpSocket_.close();
        retVal = true;
      }
      catch(UnknownHostException unknownHost)
      {
        System.err.println("You are trying to connect to an unknown host!");
      }
      catch(IOException ioException)
      {
        System.err.println("Can't Connect: "+ioException.getMessage());
      }
    }
    return retVal;
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
      boolean acted = false;
      if (in_!=null) {in_.close();in_=null;acted = true;}
      if (out_!=null) {out_.close();out_=null;acted = true;}
      if (eiscpSocket_!=null) {eiscpSocket_.close();eiscpSocket_=null;acted = true;}
      if (acted) System.out.println("closed connections");
      connected_ = false;
    }
    catch(IOException ioException)
    {
      ioException.printStackTrace();
    }
    return connected_;
  }


  public static String convertAsciiToBase10(String str)  {return convertAsciiToBase10( str, false);}
  public static String convertAsciiToBase10(String str,  boolean dumpOut)
  {
    char[] chars = str.toCharArray();
    StringBuffer base10 = new StringBuffer();
    if (dumpOut) System.out.print(" Base10: ");
    for(int i = 0; i < chars.length; i++)
    {
      base10.append( (int)chars[i]);
      if(i+1<chars.length) base10.append( ";" );
      if (dumpOut) System.out.print("  "+((""+(int)chars[i]).length()==1?"0":"")+(int)chars[i] + " ");
    }
   if (dumpOut) System.out.println("");
    return base10.toString();
  }


  /** Converts an ascii decimal String to a hex  String.
   * @param str holding the string to convert to HEX
   * @return a string holding the HEX representation of the passed in decimal str.
   **/
  public static String convertStringToHex(String str)
  {
     return convertStringToHex( str, false);
  }


  /** Converts an ascii decimal String to a hex  String.
   * @param str holding the string to convert to HEX
   * @param dumpOut flag to turn some debug output on/off
   * @return a string holding the HEX representation of the passed in str.
   **/
  public static String convertStringToHex(String str,  boolean dumpOut)
  {
    char[] chars = str.toCharArray();
    String out_put = "";

    if (dumpOut) System.out.println("    Ascii: "+str);
    if (dumpOut) System.out.print("    Hex: ");
    StringBuffer hex = new StringBuffer();
    for(int i = 0; i < chars.length; i++)
    {
      out_put = Integer.toHexString((int)chars[i]);
      if (out_put.length()==1) hex.append("0");
      hex.append(out_put);
      if (dumpOut) System.out.print("0x"+(out_put.length()==1?"0":"")+ out_put+" ");
    }
    if (dumpOut) System.out.println("");
    if (dumpOut) convertAsciiToBase10(str,dumpOut);
    /*
    if (dumpOut) System.out.print(" Base10: ");
    for(int i = 0; i < chars.length; i++)
    {
      System.out.print("   "+convertHexNumberStringToDecimal(chars[i]));
    }
    if (dumpOut) System.out.println("");
    */

    return hex.toString();
  }


  /** Converts an HEX number String to its decimal equivalent.
   * @param str holding the Hex Number string to convert to decimal
   * @return an int holding the decimal equivalent of the passed in HEX numberStr.
   **/
  public static int convertHexNumberStringToDecimal(String str)
  {
    return convertHexNumberStringToDecimal(str, false);
  }


  /** Converts an HEX number String to its decimal equivalent.
   * @param str holding the Hex Number string to convert to decimal
   * @param dumpOut boolean flag to turn some debug output on/off
   * @return an int holding the decimal equivalent of the passed in HEX numberStr.
   **/
  public static int convertHexNumberStringToDecimal(String str,  boolean dumpOut)
  {
    char[] chars = str.toCharArray();
    String out_put = "";

    if (dumpOut) System.out.println("\n      AsciiHex: 0x"+str);
    if (dumpOut) System.out.print(  "       Decimal: ");

    StringBuffer hex = new StringBuffer();
    String hexInt = new String();
    for(int i = 0; i < chars.length; i++)
    {
      out_put = Integer.toHexString((int)chars[i]);
      if (out_put.length()==1) hex.append("0");
      hex.append(out_put);
      if (dumpOut) System.out.print((out_put.length()==1?"0":"")+ out_put);
    }
    hexInt = ""+(Integer.parseInt( hex.toString(), 16));
    if (dumpOut) System.out.println("");
    if (dumpOut) System.out.println( "      Decimal: "+hexInt.toString());

    return Integer.parseInt(hexInt.toString());

    //return Integer.decode("0x"+str);
  }


  /** Converts a hex byte to an ascii String.
   * @param hex byte holding the HEX string to convert back to decimal
   * @return a string holding the HEX representation of the passed in str.
   **/
  public static String convertHexToString(byte hex)
  {
    byte [] bytes = {hex};
    return convertHexToString( new String(bytes), false);
  }


  /** Converts a hex String to an ascii String.
   * @param hex the HEX string to convert back to decimal
   * @return a string holding the HEX representation of the passed in str.
   **/
  public static String convertHexToString(String hex)
  {
    return convertHexToString( hex, false);
  }


  /** Converts a hex String to an ascii String.
   * @param hex the HEX string to convert backk to decimal
   * @param dumpOut boolean flag to turn some debug output on/off
   * @return a string holding the HEX representation of the passed in str.
   **/
  public static String convertHexToString(String hex,  boolean dumpOut)
  {

    StringBuilder sb = new StringBuilder();
    StringBuilder temp = new StringBuilder();
    String out_put = "";

    if (dumpOut) System.out.print("    Hex: ");
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
    if (dumpOut) System.out.println("    Decimal : " + temp.toString());

    return sb.toString();
  }


  /**
    * Wraps a command in a eiscp data message (data characters).
    *
    * @param command must be one of the Command Class Constants from the eiscp.Eiscp.Command class.
    * @return StringBuffer holing the full iscp message packet
    **/
  public StringBuilder getEiscpMessage(int command)
  {
    String cmdStr = "";
    if (command==iscp_.VOLUME_SET)
      cmdStr = getVolumeCmdStr();
    else
      cmdStr = iscp_.getCommandStr(command);

    StringBuilder sb = new StringBuilder();
    int eiscpDataSize = iscp_.getCommandStr(command).length() + 2 ; // this is the eISCP data size
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
    sb.append(cmdStr);

    // msg end - EOF
    sb.append((char)Integer.parseInt("0D", 16));

    //System.out.println("  eISCP data size: "+eiscpDataSize +"(0x"+Integer.toHexString(eiscpDataSize) +") chars");
    //System.out.println("  eISCP msg size: "+sb.length() +"(0x"+Integer.toHexString(sb.length()) +") chars");

    return sb;
  }  //getEiscpMessage


  /** dumps all the commands to System.out along with its associated eIscp message string in BASE10 numbers.
    * For example:< br />LISTEN_MODE_THEATER_DIMENSIONAL:
    **/
  public void dumpBinaryMessages()
  {
    StringBuilder sb = null;
    Iterator<String> it =iscp_.getIterator();
    String currCommandName = null;
    while( it.hasNext())
    {
      currCommandName = it.next();
      sb = getEiscpMessage(iscp_.getCommand(currCommandName));
      System.out.println(currCommandName+": "+convertAsciiToBase10(sb.toString()));
    }
  }

  /**
    * Sends to command to the receiver and does not wait for a reply.
    *
    * @param command must be one of the Command Class Constants from the eiscp.Eiscp.Command class.
    **/
  public void sendCommand(int command)
  {
    sendCommand(command, false);
  }


  /**
    * Sends to command to the receiver and does not wait for a reply.
    *
    * @param command must be one of the Command Class Constants from the eiscp.Eiscp.Command class.
    * @param closeSocket flag to close the connection when done or leave it open.
    **/
  public void sendCommand(int command, boolean closeSocket)
  {
    StringBuilder sb = getEiscpMessage(command);

    if(connectSocket())
    {
      try
      {
        System.out.println("  sending "+sb.length() +" chars: ");
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
    * Sends to command to the receiver and then waits for the response(s) <br />and returns only the response packetMessages related to the command requested<br />and closes the socket.
    * if you want to see ALL the responses use the sendQueryCommand(int command, boolean closeSocket, boolean returnAll) method.
    *
    * @param command must be one of the Command Class Constants from the eiscp.Eiscp.Command class.
    * @return the response to the command
    **/
  public String sendQueryCommand(int command)
  {
    return sendQueryCommand( command,  false);
  }


  /**
    * Sends to command to the receiver and then waits for the response(s) <br />and returns only the response packetMessages related to the command requested.
    * if you want to see ALL the responses use the sendQueryCommand(int command, boolean closeSocket, boolean returnAll) method.
    *
    * @param command must be one of the Command Class Constants from the eiscp.Eiscp.Command class.
    * @param closeSocket flag to close the connection when done or leave it open.
    * @return the response to the command
    **/
  public String sendQueryCommand(int command, boolean closeSocket)
  {
    return sendQueryCommand( command,  closeSocket, false);
  }


  /**
    * Sends to command to the receiver and then waits for the response(s). The responses often have nothing to do with the command sent
    * so this method can filter them to return only the responses related to the command sent.
    *
    * @param command must be one of the Command Class Constants from the eiscp.Eiscp.Command class.
    * @param closeSocket flag to close the connection when done or leave it open.
    * @param returnAll flags if all response packetMessages are returned, if no then ONLY the ones related to the command requested
    * @return the response to the command
    **/
  public String sendQueryCommand(int command, boolean closeSocket, boolean returnAll)
  {
    String retVal = "";

    /* Send The Command and then... */
    sendCommand(command,false);
    //sleep(50); // docs say so

    /* now listen for the response. */
    Vector <String> rv = null;
    if(returnAll)
      rv = readQueryResponses();
    else
      rv = readQueryResponses(command);
    String currResponse = "";
    for (int i=0; i < rv.size(); i++)
    {
      currResponse = (String) rv.elementAt(i);
      /* Send ALL responses OR just the one related to the commad sent??? */
      if (returnAll || currResponse.startsWith(iscp_.getCommandStr(command).substring(0,3)))
        retVal+= currResponse+"\n";
    }

    if (closeSocket) closeSocket();

    return retVal ;
  }


  /**
   * This method reads ALL responses (possibly more than one) after a query command.
   * @return an array of the data portion of the response messages only - There might be more than one response message received.
   **/
  public Vector <String> readQueryResponses() { return readQueryResponses(-1);}
  /**
   * This method reads responses (possibly more than one) after a query command. It can end
   * early when it finds the respose you are waitng for by passing in the command you called.
   * @param command is used to end the response processing early when it finds the command - if you want all responses processed pass in -1
   * @return an array of the data portion of the response messages only - There might be more than one response message received.
   **/
  public Vector <String> readQueryResponses(int command)
  {
    //boolean debugging = debugging_;
    boolean foundCommand = false;
    Vector <String> retVal = new Vector <String> ();
    byte [] responseBytes = new byte[32] ;
    String currResponse = "";
    int numBytesReceived = 0;
    int totBytesReceived = 0;
    int i=0;
    int packetCounter=0;
    int headerSizeDecimal = 0;
    int dataSizeDecimal = 0;
    char endChar1 ='!';// NR-5008 response sends 3 chars to terminate the packet - 0x1a 0x0d 0x0a
    char endChar2 ='!';
    char endChar3 ='!';

    if(connected_)
    {
      try
      {
        if (debugging_) System.out.println("\nReading Response Packet");
        eiscpSocket_.setSoTimeout(socketTimeOut_); // this must be set or the following read will BLOCK / hang the method when the messages are done

        while(!foundCommand && ((numBytesReceived = in_.read(responseBytes))>0) )
        {
          totBytesReceived = 0;
          StringBuilder msgBuffer = new StringBuilder("");
          if (debugging_) System.out.println("\n*\n*\n*\n*Buffering bytes: "+numBytesReceived);
          if (debugging_) System.out.print( " Packet"+"["+packetCounter+"]:");

          /* Read ALL the incoming Bytes and buffer them */
          // *******************************************
          while(numBytesReceived>0 )
          {
            totBytesReceived+=numBytesReceived;
            msgBuffer.append(new String(responseBytes));
            responseBytes = new byte[32];
            numBytesReceived = 0;
            if (in_.available()>0)
              numBytesReceived = in_.read(responseBytes);
            if (debugging_) System.out.print(" "+numBytesReceived);
          }
          if (debugging_) System.out.println();
          convertStringToHex(msgBuffer.toString(), debugging_);

          /* Response is done... process it into dataMessages */
          // *******************************************
          char [] responseChars = msgBuffer.toString().toCharArray(); // use the charArray to step through
          msgBuffer = null;// clear for garbageCollection

          if (debugging_) System.out.println("responseChars.length="+responseChars.length);
          int responseByteCnt = 0;
          char versionChar = '1';
          char dataStartChar = '!';
          char dataUnitChar = '1';
          char [] headerSizeBytes = new char[4];
          char [] dataSizeBytes  = new char[4];
          char [] dataMessage = null ; //init dynamically
          int dataByteCnt = 0;
          String dataMsgStr = "";

          // loop through all the chars and split out the dataMessages
          while (!foundCommand && (responseByteCnt< totBytesReceived))
          {
            /* read Header */
            // 1st 4 chars are the leadIn
            responseByteCnt+=4;

            // read headerSize
            headerSizeBytes[0]=responseChars[responseByteCnt++];
            headerSizeBytes[1]=responseChars[responseByteCnt++];
            headerSizeBytes[2]=responseChars[responseByteCnt++];
            headerSizeBytes[3]=responseChars[responseByteCnt++];

            // 4 char Big Endian data size;
            dataSizeBytes[0]=responseChars[responseByteCnt++];
            dataSizeBytes[1]=responseChars[responseByteCnt++];
            dataSizeBytes[2]=responseChars[responseByteCnt++];
            dataSizeBytes[3]=responseChars[responseByteCnt++];

            if (debugging_) System.out.println(" -HeaderSize-");
            headerSizeDecimal = convertHexNumberStringToDecimal(new String(headerSizeBytes),debugging_);
            if (debugging_) System.out.println(" -DataSize-");
            dataSizeDecimal = convertHexNumberStringToDecimal(new String(dataSizeBytes),true);//debugging_);

            // version
            versionChar = responseChars[responseByteCnt++];

            // 3 reserved bytes
            responseByteCnt+=3;
            dataByteCnt = 0;

            // Now the data message
            dataStartChar = responseChars[responseByteCnt++]; // parse and throw away (like parsley)
            dataUnitChar = responseChars[responseByteCnt++]; // dito
            dataMessage = null;
            System.out.println("new dataMessage["+dataSizeDecimal+"]");
            if(dataSizeDecimal<4096)
              dataMessage = new char [dataSizeDecimal];
            else
            {
              dataMessage= new char[0];
              System.out.println("error data message size hexVal="+dataSizeBytes);
              break;
            }

            /* Get the dataMessage from this response */
            // NR-5008 response sends 3 chars to terminate the packet - so DON't include them in the message
            while( dataByteCnt < (dataSizeDecimal-5) && responseByteCnt< (totBytesReceived-3))
            {
              dataMessage[dataByteCnt++] = responseChars[responseByteCnt++];
            }
            dataMsgStr = new String(dataMessage);
            if (debugging_) System.out.println("dataMessage:\n~~~~~~~~~~~~~");
            if (debugging_) System.out.println(dataMsgStr);
            retVal.addElement(dataMsgStr);

            // Read the end packet char(s) "[EOF]"
            // [EOF]			End of File		ASCII Code 0x1A
            // NOTE: the end of packet char (0x1A) for a response message is DIFFERENT that the sent message
            // NOTE: ITs also different than what is in the Onkyo eISCP docs
            // NR-5008 sends 3 chars to terminate the packet - 0x1a 0x0d 0x0a
            endChar1 = responseChars[responseByteCnt++];
            endChar2 = responseChars[responseByteCnt++];
            endChar3 = responseChars[responseByteCnt++];
            if (endChar1 == (char)Integer.parseInt("1A", 16) &&
                endChar2 == (char)Integer.parseInt("0D", 16) &&
                endChar3 == (char)Integer.parseInt("0A", 16)
               ) if (debugging_) System.out.println(" EndOfPacket["+packetCounter+"]\n");
            packetCounter++;
            // Now check if we end early
            if (command!=-1 && dataMsgStr.startsWith(iscp_.getCommandStr(command).substring(0,3)))
            {
              foundCommand = true;
              if (debugging_) System.out.println("Found Response:"+iscp_.getCommandStr(command));
            }
          }// done packet

        } // check for more data

      }
      catch( java.net.SocketTimeoutException  noMoreDataException)
      {
        if (debugging_) System.out.println("Response Done: " );
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
      System.out.println("!!Not Connected to Receive ");
    return retVal;
  }


  /** This method creates the set volume command based on the passed value. **/
  public static  String getVolumeCmdStr(){return iscp_.getVolumeCmdStr(volume_);}


  /** Converts the VOLUME_QUERY response into ascii decimal result. **/
  public static  String decipherVolumeResponse(String queryResponses)
  {
    String [] responses = queryResponses.split("[\n]");
    String retVal = "VOLUME_QUERY response: ";
    String queryResponse = "";
    String volVal = "00";
    Integer IntVal = Integer.decode("0x"+volVal);
    for (int i=0; i< responses.length; i++)
    {
      queryResponse = responses[i];
      if (queryResponse.startsWith(iscp_.getCommandStr(iscp_.VOLUME_SET)))
      {
        try
        {
          volVal = queryResponse.trim().substring(3);
          IntVal = Integer.decode("0x"+volVal);
          retVal +="\n   Volume=" + IntVal;
          retVal +=" (0x" + volVal +")\n";
        }
        catch (Exception ex)
        {
          // Ignore for now
        }
    }
    }
    return retVal;
  }


  /** This method takes the  3 character response from the USB Play status query (NETUSB_PLAY_STATUS_QUERY) and creates a human readable String.
   * NET/USB Play Status QUERY returns one of 3 letters - PRS.<oL>
   * <LI>p -> Play Status<ul><li>"S": STOP</li><li>"P": Play</li><li>"p": Pause</li><li>"F": FF</li><li>"R": FastREW</li></ul></LI>
   * <LI>r -> Repeat Status<ul><li>"-": Off</li><li>"R": All</li><li>"F": Folder</li><li>"1": Repeat 1</li></ul></LI>
   * <LI>s -> Shuffle Status<ul><li>"-": Off</li><li>"S": All</li><li>"A": Album</li><li>"F": Folder</li></ul></LI></oL>
   * @param queryResponses is the entire response packet with the oneOf3 char reply embedded in it.
  **/
  public   String decipherUsbPlayStatusResponse(String queryResponses)
  {
    String [] responses = queryResponses.split("[\n]");
    String retVal = "NETUSB_PLAY_STATUS_QUERY response: "+ queryResponses.trim();
    String queryResponse = "";
    for (int i=0; i< responses.length; i++)
    {
      queryResponse = responses[i];
      if (queryResponse.substring(3,4).equals("P") )
      {
        retVal += "\n  Play Status: ";
        if (queryResponse.substring(5).equals("S") )
          retVal +="Stop";
        else if (queryResponse.substring(5).equals("P") )
          retVal +="Play";
        else if (queryResponse.substring(5).equals("p") )
          retVal +="Pause";
        else if (queryResponse.substring(5).equals("F") )
          retVal +="FastForward";
        else if (queryResponse.substring(5).equals("R") )
          retVal +="FastRewind";
        else retVal+= "NotSpecified";
      }

      if (queryResponse.substring(3,4).equals("R") )
      {
        retVal += "\n  Repeat Status: ";
        if (queryResponse.substring(5).equals("-") )
          retVal +="Off";
        else if (queryResponse.substring(5).equals("R") )
          retVal +="All";
        else if (queryResponse.substring(5).equals("F") )
          retVal +="Folder";
        else if (queryResponse.substring(5).equals("1") )
          retVal +="1 song";
        else retVal+= "NotSpecified";
      }

      if (queryResponse.substring(3,4).equals("S") )
      {
        retVal += "\n  Schuffle Status: ";
        if (queryResponse.trim().substring(5).equals("-") )
          retVal +="Off";
        else if (queryResponse.trim().substring(5).equals("S") )
          retVal +="All";
        else if (queryResponse.trim().substring(5).equals("A") )
          retVal +="Album";
        else if (queryResponse.trim().substring(5).equals("F") )
          retVal +="Folder";
        else retVal+= "NotSpecified";
      }
    }

    return retVal;
  }


  /**
   *  A method to simply abstract the Try/Catch required to put the current
   *  thread to sleep for the specified time in ms.
   *
   * @param  waitTime  the sleep time in milli seconds (ms).
   * @return           boolean value specifying if the sleep completed (true) or was interupted (false).
   */
  public boolean sleep(long waitTime)
  {
    boolean retVal = true;
    /*
     *  BLOCK for the spec'd time
     */
    try
    {
      Thread.sleep(waitTime);
    }
    catch (InterruptedException iex)
    {
      retVal = false;
    }
    return retVal;
  }


  /** gets the help as a String.
   * @return the helpMsg in String form
   **/
  private static String getHelpMsgStr() {return getHelpMsg().toString();}


  /** initializes and gets the helpMsg_
  class var.
   * @return the class var helpMsg_
   **/
  private static StringBuffer getHelpMsg()
  {
    helpMsg_ = new StringBuffer(SYSTEM_LINE_SEPERATOR);
    helpMsg_.append("---  WebARTS Eiscp Class  -----------------------------------------------------");
    helpMsg_.append(SYSTEM_LINE_SEPERATOR);
    helpMsg_.append("---  $Revision: 624 $ $Date: 2014-04-08 20:28:58 -0700 (Tue, 08 Apr 2014) $ ---");
    helpMsg_.append(SYSTEM_LINE_SEPERATOR);
    helpMsg_.append("-------------------------------------------------------------------------------");
    helpMsg_.append(SYSTEM_LINE_SEPERATOR);
    helpMsg_.append("WebARTS Eiscp Class");
    helpMsg_.append(SYSTEM_LINE_SEPERATOR);
    helpMsg_.append("SYNTAX:");
    helpMsg_.append(SYSTEM_LINE_SEPERATOR);
    helpMsg_.append("   java ");
    helpMsg_.append(CLASSNAME);
    helpMsg_.append(" [hostIP] command [commandArgs]");
    helpMsg_.append(SYSTEM_LINE_SEPERATOR);
    helpMsg_.append("      - hostIP is optional and defaults to 10.0.0.203");
    helpMsg_.append(SYSTEM_LINE_SEPERATOR);
    helpMsg_.append("      - command is NOT optional");
    helpMsg_.append(SYSTEM_LINE_SEPERATOR);
    helpMsg_.append(SYSTEM_LINE_SEPERATOR);
    helpMsg_.append("Available Commands:");
    /* now add all the commands available */
    Iterator<String> it =iscp_.getIterator();
    while( it.hasNext())
    {
      helpMsg_.append(SYSTEM_LINE_SEPERATOR);
      helpMsg_.append("-->   "+it.next());
    }
    helpMsg_.append(SYSTEM_LINE_SEPERATOR);
    helpMsg_.append("---------------------------------------------------------");
    helpMsg_.append("----------------------");
    helpMsg_.append(SYSTEM_LINE_SEPERATOR);

    return helpMsg_;
  }


  /**
   * Class main commandLine entry method.
   **/
  public static void main(String [] args)
  {
    final String methodName = CLASSNAME + ": main()";
    Eiscp instance = new Eiscp(DEFAULT_EISCP_IP, DEFAULT_EISCP_PORT);
    int commandArg = 0;

    /* Simple way af parsing the args */
    if (args ==null || args.length<1)
      System.out.println(getHelpMsgStr());
    else
    {
      if (args[0].equals("test"))
      {
        System.out.println("Testing Eiscp");
        String queryResponse =  instance.sendQueryCommand(iscp_.VOLUME_QUERY);
        String volumeResponse = instance.decipherVolumeResponse(queryResponse);
        System.out.println(volumeResponse);
        System.out.println();

        instance.toggleMute();
        System.out.println();
        instance.sleep(750);
        instance.toggleMute();
      }
      else if (args[0].equals("dump"))
      {
        System.out.println("Dumping Eiscp Messages as binary values.");
        instance.dumpBinaryMessages();
      }
      else
      {
        if (args.length>1)  instance.setReceiverIP(args[commandArg++]);
        // Parse the command
        int command = -1;
        String commandStr = "";
        // TODO: Set up a loop to handle multiple commands/args in one run with one socket connection
        command =iscp_.getCommand(args[commandArg].toUpperCase());  //returns -1 if not found
        commandStr=iscp_.getCommandStr(command);

        /* Special case VOLUME_SET command needs to parse a parameter. */
        if ( command == iscp_.VOLUME_SET ) instance.setVolume(Integer.parseInt(args[commandArg+1]));
        System.out.println("command: "+commandStr);

        String queryResponse = "";
        if (args[commandArg].toUpperCase().equals("DUMP"))
        {
          System.out.println("Dumping eIscp Messages as binary values.\n----------------------------------------------------");
          instance.dumpBinaryMessages();
        }
        else if (command!=-1)
        {
          /* It is a query command so send AND parse response */
          if(args[commandArg].toUpperCase().endsWith("QUERY") )
          {
            //send the command and get the response
            queryResponse = instance.sendQueryCommand(command, true, false);
            System.out.print("Responses: \n  " +queryResponse);
            if (queryResponse!=null && !queryResponse.equals(""))
            {
              if (command==iscp_.NETUSB_PLAY_STATUS_QUERY)
              {
                System.out.println(instance.decipherUsbPlayStatusResponse(queryResponse));
              }
              else if (command==iscp_.VOLUME_QUERY)
              {
                System.out.println(instance.decipherVolumeResponse(queryResponse));
              }
              else
                System.out.println("   ="+ iscp_.getCommandName(queryResponse.trim()));
            }
            else
              System.out.println("\n"+ args[commandArg]+"("+commandStr +") response: EMPTY");
          }

          /* It is a basic change setting command (with no response) */
          else
          {
            instance.sendCommand(command); //send the command
          }
        }
        else
        {
          System.out.println(getHelpMsgStr()+"\n *!*!*!*! --> ");
          System.out.println(args[commandArg].toUpperCase() + " not found");
        }
      }
      instance.closeSocket();
    }
  } // main


  /**
   * get the class volume_.
   * @return the volume_
   **/
  public int getVolume()
  {
    return volume_;
  }


  /** sets the class volume_.
   * @param volume the value to set the class volume_
   **/
  public void setVolume(int volume)
  {
    volume_ = volume;
  }


  /** Toggles the MUTE setting..
   **/
  public void toggleMute()
  {
    String queryResponse =  sendQueryCommand(IscpCommands.MUTE_QUERY);
    if (true || debugging_) System.out.print("Responses: \n  " +queryResponse.trim() + "("+iscp_.getCommandName(queryResponse.trim())+")");
    if (  iscp_.getCommandName(queryResponse.trim()).equals("UNMUTE"))
    {
      if (true || debugging_) System.out.println("\nMuting .");
      sendCommand(IscpCommands.MUTE);
    }
    else
    {
      if (true || debugging_) System.out.println("\nUN-Muting .");
      sendCommand(IscpCommands.UNMUTE);
    }
  }

} // class
