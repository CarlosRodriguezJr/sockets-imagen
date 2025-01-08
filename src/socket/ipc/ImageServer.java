package ipc;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ImageServer implements Runnable {

    private final Socket ch;
    private final String imageDirectory;

    public ImageServer(Socket clientSocket, String imageDirectory) {
        this.ch = clientSocket;
        this.imageDirectory = imageDirectory;
        System.out.println("Cliente conectado desde " +
                clientSocket.getInetAddress().getHostAddress());
    }

    @Override
    public void run() {
        try (DataInputStream in = new DataInputStream(ch.getInputStream());
             DataOutputStream out = new DataOutputStream(ch.getOutputStream())) {

            String albumName = in.readUTF();
            System.out.println("Cliente solicitó el Album: " + albumName);

            File imageFile = new File(imageDirectory + File.separator + albumName.toLowerCase() + ".png");

            if (imageFile.exists()) {
                out.writeUTF("OK");
                out.writeUTF(imageFile.getName());
                out.writeLong(imageFile.length());

                try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(imageFile))) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = bis.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                    System.out.println("Transmisión de imagen completa.");
                }
            } else {
                out.writeUTF("NOT_FOUND");
                System.out.println("Imagen no encontrada para el album: " + albumName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeClient();
        }
    }

    private void closeClient() {
        try {
            ch.close();
        } catch (IOException ex) {
        }
    }

    public static void main(String[] args) {
        int PORT = 65489;
        String imageDirectory = "D:\\Users\\carlo\\Documents\\NetBeansProjects\\socket-imagen\\imagenes";

        try (ServerSocket ss = new ServerSocket(PORT)) {
            System.out.println("Servidor esperando conexiones en puerto " + PORT);
            while (true) {
                Socket clientSocket = ss.accept();
                Thread t = new Thread(new ImageServer(clientSocket, imageDirectory));
                t.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
