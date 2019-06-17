package nl.ronaldteune.sss.controller;

import javax.servlet.ServletOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class SoundStream {
    public static byte[] getStream(String path) throws IOException {
        File file = new File(path);
        byte[] bytesArray = new byte[(int) file.length()];
        FileInputStream fis = new FileInputStream(file);
        fis.read(bytesArray);
        fis.close();
        return bytesArray;
    }
    public static byte[] getSoundStream(String path, ServletOutputStream outputStream) throws IOException, InterruptedException {
        if (path.endsWith("flac")) {
            ProcessBuilder pb = new ProcessBuilder("/usr/bin/ffmpeg",
                    "-nostdin", "-i", "pipe:0",
                    "-f", "ogg", "-acodec", "libvorbis",
                    "-maxrate", "256", "pipe:1")
                    .redirectErrorStream(false);
            pb.redirectInput(new File(path));
            final Process process = pb.start();
            try {
                byte[] buffer = new byte[10240];
                int bytesRead;
                while ((bytesRead = process.getInputStream().read(buffer)) != -1) {
                    System.out.print(".");
                    outputStream.write(buffer, 0, bytesRead);
                }
                System.out.println();
            } catch (Exception e) {
                e.printStackTrace();
            }
            process.waitFor();
            return new byte[0];
        } else {
            return getStream(path);
        }
    }
}
