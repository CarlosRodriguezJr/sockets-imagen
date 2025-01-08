package ipc;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class ImageClient {

    public static void main(String[] args) {
        String SERVER_IP = "localhost";
        int SERVER_PORT = 65489;

        try (Socket s = new Socket(SERVER_IP, SERVER_PORT);
             DataInputStream in = new DataInputStream(s.getInputStream());
             DataOutputStream out = new DataOutputStream(s.getOutputStream());
             Scanner scanner = new Scanner(System.in)) {

            System.out.print("Ingrese el nombre del Album: ");
            String nombreAlbum = scanner.nextLine();
            out.writeUTF(nombreAlbum);

            String response = in.readUTF();
            if ("OK".equals(response)) {
                String fileName = in.readUTF();
                long fileLength = in.readLong();

                System.out.println("Recibiendo imagen...");

                try (FileOutputStream fout = new FileOutputStream("imagen_recibida_" + fileName)) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    long totalBytesRead = 0;

                    while (totalBytesRead < fileLength) {
                        bytesRead = in.read(buffer, 0,
                                (int) Math.min(buffer.length, fileLength - totalBytesRead));
                        fout.write(buffer, 0, bytesRead);
                        totalBytesRead += bytesRead;
                    }
                    System.out.println("Transmisión completa.");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                System.out.println("Imagen del Album no encontrada.");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
