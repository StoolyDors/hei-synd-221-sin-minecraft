package ch.hevs.isi.utils;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * This class contains some useful Java methods to manipulate Modbus data.
 *
 * @author Michael Clausen
 * @author Patrice Rudaz
 */
public class Utility {

    /**
     * Calculates and returns the CRC using the data passed in parameters.
     *
     * @param data a byte array containing the data to send
     * @param offset the offset
     * @param len the data length
     * @return byte[] the CRC
     */
    public static byte[] calculateCRC(byte[] data , int offset , int len)
    {
        int crc = 0x0000FFFF;
        for (int i = 0 ; i < len ; i++)
        {
            crc = crc ^ Utility.unsignedByteToSignedInt(data[i + offset]);
            for (int j = 0 ; j < 8 ; j++)
            {
                int tmp = crc;
                int carryFlag = tmp & 0x0001;
                crc = crc >> 1;
                if (carryFlag == 1)
                {
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
     * @return boolean true if the CRC is correct, otherwise false
     */
    public static boolean checkCRC(byte[] data , int offset , int len , byte[] crc)
    {
        byte[] calcCrc = Utility.calculateCRC(data , offset , len);
        if (calcCrc[0] == crc[0] && calcCrc[1] == crc[1])
            return true;
        else
            return false;
    }


    /**
     * Converts an unsigned byte to a signed integer.
     *
     * @param from an unsigned byte to convert to a signed integer
     * @return int a signed integer
     */
    public static int unsignedByteToSignedInt(byte from)
    {
        return 0x000000FF & (int)from;
    }

    /**
     * Utility method to convert a byte array in a string made up of hex (0,.. 9, a,..f)
     */
    public static String getHexString(byte[] b) throws Exception {
        return getHexString(b, 0, b.length);
    }

    public static String getHexString(byte[] b, int offset, int length) {
        String result = "";
        for (int i = offset ; i < offset+length ; i++) {
            result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1) ;
        }
        return result;
    }


    /**
     * To wait some times ...
     */
    public static void waitSomeTime(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            DEBUG("Utility", "waitSomeTime()", "Exception : " + e.getMessage());
        }
    }

    /**
     * Returns the md5 of any input...
     * @param msg The input string to process
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
     * @param bytes     The array of bytes to convert.
     * @param offset    The position where the method has to start to get the bytes from.
     * @param size      The amount of bytes to convert.
     * @return A <code>Float</code> value or <code>null</code> if the process failed.
     */
    public static Float bytesToFloat(byte[] bytes, int offset, int size) {

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

    // DEBUG System.out
    public static void DEBUG(String className, String method, String msg) {
        int millis = Calendar.getInstance().get(Calendar.MILLISECOND);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        System.out.println(sdf.format(Calendar.getInstance().getTime()) + "." + String.format("%3d", millis) +
                            " [" + className + "] " + method + " > " + msg);
    }
}
