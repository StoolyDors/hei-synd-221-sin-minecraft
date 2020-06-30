package ch.hevs.isi.utils;

import java.io.*;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;

/**
 * This class contains some useful Java methods to manipulate data.
 *
 * @author Michael Clausen
 * @author Patrice Rudaz
 */
public class Utility {

    /** Default size for the TCP input stream */
    public static final int TCP_BUFFER_SIZE = 4096;

    /** Object to get some random values... */
    public static Random rnd = new Random(1);

    /**
     * Calculates and returns the CRC using the data passed in parameters.
     *
     * @param data a byte array containing the data to send
     * @param offset the offset
     * @param len the data length
     *
     * @return byte[] the CRC
     */
    public static byte[] calculateCRC(byte[] data, int offset, int len)
    {
        int crc = 0x0000FFFF;
        for (int i = 0; i < len; i++) {
            crc = crc ^ Utility.unsignedByteToSignedInt(data[i + offset]);
            for (int j = 0; j < 8; j++) {
                int tmp = crc;
                int carryFlag = tmp & 0x0001;
                crc = crc >> 1;
                if (carryFlag == 1) {
                    crc = crc ^ 0xA001;
                }
            }
        }

        byte[] result = new byte[2];
        result[0] = (byte)(crc & 0xFF);
        result[1] = (byte)((crc & 0xFF00) >> 8);

        return result;
    }

    /**
     * Checks the CRC and returns true if it is correct, otherwise false.
     *
     * @param data a byte array containing the data to send
     * @param offset the offset
     * @param len the data length
     * @param crc a byte array containing the CRC to check
     *
     * @return boolean true if the CRC is correct, otherwise false
     */
    public static boolean checkCRC(byte[] data, int offset, int len, byte[] crc)
    {
        byte[] calcCrc = Utility.calculateCRC(data, offset, len);
        return (calcCrc[0] == crc[0] && calcCrc[1] == crc[1]);
    }


    /**
     * Converts an unsigned byte to a signed integer.
     *
     * @param from an unsigned byte to convert to a signed integer
     *
     * @return int a signed integer
     */
    public static int unsignedByteToSignedInt(byte from)
    {
        return 0x000000FF & (int)from;
    }

    /**
     * Utility method to convert a byte array in a string made up of hex (0,.. 9, a,..f). The array of bytes is 
     * converted from offset 0 to its end.
     * 
     * @param b Array of bytes to be converted in HEX string.
     * 
     * @return A String representing the HEX values of the given array of bytes.
     */
    public static String getHexString(byte[] b)
    {
        return getHexString(b, 0, b.length);
    }

    /**
     * Utility method to convert a byte array in a string made up of hex (0,.. 9, a,..f)
     *
     * @param b         The byte array to convert in HEX string
     * @param offset    The index where we start to convert from.
     * @param length    The amount of bytes to convert in HEX string
     * @return A String representing the HEX values of the selected bytes of the array.
     */
    public static String getHexString(byte[] b, int offset, int length)
    {
        String result = "";
        for (int i = offset; i < offset+length; i++) {
            result = result.concat(Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1));
        }
        return result;
    }

    /**
     * Retrieves a random value rounded to 2 decimal...
     *
     * @param factor A coefficient which the random value is multiplied with.
     *
     * @return A random value rounded to 2 decimal converted to a String.
     */
    public static String getStringRndVal(int factor)
    {
        float val = (float) rnd.nextDouble()*factor*10;
        return String.format("%.2f", val).replace(",", ".");
    }


    /**
     * Reads the incoming data from an input stream as long as there is something to read and saved the data in an array
     * of bytes.
     *
     * The method is blocking ! This method blocks until input data is available, end of file is detected, or an 
     * exception is thrown.
     *
     * If the length of of read data is zero, then no bytes are read and an empty array of bytes is returned; otherwise,
     * there is an attempt to read at least one byte. If no byte is available because the stream is at the end of the
     * file, the value -1 is returned; otherwise, at least one byte is read and returned as an array of bytes.
     *
     * @param  in    The input Stream where to read the data coming from.
     *
     * @return  The read data as an <code>array of bytes</code>. Or null if the has been closed by the peer while
     *          waiting for incoming data.
     * @throws  IOException If the first byte cannot be read for any reason other than the end of the file, if the input
     *                      stream has been closed, or if some other I/O error occurs.
     */
    public static byte[] readBytes(InputStream in) throws IOException
    {
        byte[] buffer = new byte[ TCP_BUFFER_SIZE ];

        // Read the incoming data
        int b = in.read(buffer);

        // Creates an array of bytes with the right size
        if(b > 0) {
            byte[] rBytes = new byte[b];
            System.arraycopy(buffer, 0, rBytes, 0, b);
            return rBytes;    // The received values
        } else if (b == 0)
            return new byte[0];

        return null;
    }


    /**
     * Reads from the given input stream an amount of bytes and retrieves these data as an array of bytes.
     *
     * The method is blocking !
     *
     * @param in    The input stream where to read the data coming from.
     * @param len   The amount of data to read
     *
     * @return  The read data as an <code>array of bytes</code>.
     *
     * @throws IOException  If the first byte cannot be read for any reason other
     *                      than the end of the file, if the input stream has been
     *                      closed, or if some other I/O error occurs.
     */
    public static byte[] readNBytes(InputStream in, int len) throws IOException
    {
        byte[] buffer = new byte[len];

        // Read the incoming data
        int b = 0;
        while (b < len) {
            b += in.read(buffer, b, len-b);
        }

        // Creates an array of bytes with the right size
        if (b == -1)       return null;         // the connection has been canceled by the peer
        else if (b == 0)   return new byte[0];  // empty data
        else               return buffer;       // The received data
    }

    /**
     * Reads a line of text. A line is considered to be terminated by any one of a
     * line feed ('\n'), a carriage return ('\r'), or a carriage return followed
     * immediately by a line feed.
     *
     * @param in The Input Stream to read from.
     *
     * @return An array of bytes containing the contents of the line, not including any
     *            line-termination characters, or null if the end of the stream has
     *         been reached.
     *
     * @throws IOException    If an I/O error occurs
     */
    public static byte[] readLine(InputStream in) throws IOException
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));

        // Read a complete line with Cariage Return and/or Line Feed
        String line = reader.readLine();

        // Handle the value to return
        if(line != null)
            return line.replace("\n", "").replace("\r", "").getBytes();
        else
            return null;
    }

    /**
     * Send the data contained in the given array of bytes through the output
     * stream. If the data to send do not end with a carriage return, the method adds '\r\n' at the end.
     *
     * @param out       The Output Stream to send the data to.
     * @param toSend    The data to send
     *
     * @throws IOException    If an I/O error occurs
     */
    public static void writeLine(OutputStream out, byte[] toSend) throws IOException
    {
        
        if (toSend[toSend.length - 1] == '\n' || toSend[toSend.length - 1] == '\r') {
            sendBytes(out, toSend, 0, toSend.length);
            return;
        }

        byte[] buffer = new byte[toSend.length + 2];
        System.arraycopy(toSend, 0, buffer, 0, toSend.length);
        buffer[toSend.length]     = '\r';
        buffer[toSend.length + 1] = '\n';
        sendBytes(out, buffer, 0, buffer.length);
    }

    /**
     * This method sends the content of the <code>buffer</code> to the given <code>OutputStream</code>. This content is
     * specified by the starting position, defined by <code>offset</code>, and the amount of bytes,defined by
     * <code>length</code>. If an error occurs during this process, a exception will be raised.
     *
     * @param out       The Output Stream to send the data to.
     * @param toSend    The data to send
     * @param offset    The starting position in the buffer of data
     * @param length    The amount of bytes to be sent.
     *
     * @throws IOException    If an I/O error occurs
     */
    public static void sendBytes (OutputStream out, byte[] toSend, int offset, int length) throws IOException
    {
        out.write(toSend, offset, length);
        out.flush();
    }

    /**
     * To wait some times ... Simply give the delay you want to wait for and that's it !
     *
     * @param ms    The amount of time to wait expressed in milli-seconds [ms].
     */
    public static void waitSomeTime(int ms)
    {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            DEBUG("Utility", "waitSomeTime()", "Exception : " + e.getMessage());
        }
    }

    /**
     * Returns the md5 of any input...
     *
     * @param msg The input string to process
     *
     * @return  The md5 of the input string.
     */
    public static String md5sum(String msg)
    {
        try {
            MessageDigest md = MessageDigest.getInstance("md5");
            return String.format("%032x", new BigInteger(1, md.digest(msg.getBytes("UTF-8"))));
        } catch (UnsupportedEncodingException e) {
            DEBUG("Utility", "md5sum()", "UnsupportedEncodingException");
        } catch (NoSuchAlgorithmException e) {
            DEBUG("Utility", "md5sum()", "NoSuchAlgorithmException");
        }
        return null;
    }

    /**
     * Returns a <code>float</code> value from array of bytes. This byte's array can only be 2 or 4 bytes long.
     *
     * @param bytes     The array of bytes to convert.
     * @param offset    The position where the method has to start to get the bytes from.
     * @param size      The amount of bytes to convert.
     *
     * @return A <code>Float</code> value or <code>null</code> if the process failed.
     */
    public static Float bytesToFloat(byte[] bytes, int offset, int size)
    {
        if (size == 2 || size == 4) {
            byte[] tmp = new byte[4];
            System.arraycopy(bytes, offset, tmp, 0, size);
            try {
                return ByteBuffer.allocate(4).wrap(tmp).order(ByteOrder.BIG_ENDIAN).getFloat();
            } catch (Exception e) {
                DEBUG("Utility", "bytesToFloat()", "ByteBufferException: " + e.getLocalizedMessage());
            }
        } else {
            DEBUG("Utility", "bytesToFloat()", "ERROR: size MUST be 2 or 4 !!!");
        }
        return null;
    }

    /**
     * Method to use to access a file in your resources folder... The file you want to access has to be under the
     * `resources` folder of your project. You can create as many sub-folders as you want, they will become the path of
     * the file to work with. This path is given to the method by the parameter `path` and the file is identified by its
     * name and extension.
     *
     * @param path      folder's hierarchy (if exists) from `resources`
     * @param fileName  Name of the file to access
     *
     * @return  A <code>BufferedReader</code> related to the file you want to work with, or null if the file could not
     *          be reached.
     */
    public static BufferedReader fileParser(String path, String fileName)
    {
        // set relative path
        InputStream is = null;
        if (path == null) {
            is = ClassLoader.getSystemClassLoader().getResourceAsStream(fileName);
        } else {
            is = ClassLoader.getSystemClassLoader().getResourceAsStream(path + "/" + fileName);
        }

        if (is != null) {
            return new BufferedReader(new InputStreamReader(is));
        }
        return null;
    }

    // DEBUG System.out
    public static void DEBUG(String className, String method, String msg) {
        int millis = Calendar.getInstance().get(Calendar.MILLISECOND);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        System.out.println(sdf.format(Calendar.getInstance().getTime()) + "." + String.format("%3d", millis) +
                            " [" + className + "] " + method + " > " + msg);
    }
}
