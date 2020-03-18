package ch.hevs.isi;

import ch.hevs.isi.utils.Utility;

public class MinecraftController {
    public static boolean USE_MODBUS4J = false;

    public static void usage() {
        System.out.println("Parameters: <InfluxDB Server> <Group Name> <MordbusTCP Server> <modbus TCP port> [-modbus4j]");
        System.exit(1);
    }

    public static void main(String[] args) {

        String dbHostName    = "localhost";
        String dbName        = "labo";
        String dbUserName    = "root";
        String dbPassword    = "root";

        String modbusTcpHost = "localhost";
        int    modbusTcpPort = 1502;

        // Check the number of arguments and show usage message if the number does not match.
        String[] parameters = null;

        // If there is only one number given as parameter, construct the parameters according the group number.
        if (args.length == 4 || args.length == 5) {
            parameters = args;

            // Decode parameters for influxDB
            dbHostName  = parameters[0];
            dbUserName  = parameters[1];
            dbName      = dbUserName;
            dbPassword  = Utility.md5sum(dbUserName);

            // Decode parameters for Modbus TCP
            modbusTcpHost = parameters[2];
            modbusTcpPort = Integer.parseInt(parameters[3]);

            if (args.length == 5) {
                USE_MODBUS4J = (parameters[4].compareToIgnoreCase("-modbus4j") == 0);
            }
        } else {
            usage();
        }

    }
}
