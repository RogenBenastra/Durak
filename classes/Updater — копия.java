import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class Updater {
  public static void main(String[] args) {

//проверяем условия начала работы программы
File file1 = new File("Durak.jar");
File file2 = new File("Durak.jar.tmp");
if(!file1.exists()|!file2.exists())
return;

//закрываем активное приложение
            try {
               Socket socket = new Socket("localhost", 9090);
               PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
               out.println("close");
               socket.close();
              int count = 0;
               while (count < 15) {
                  try {
                     Socket checkSocket = new Socket("localhost", 9009);
                     checkSocket.close();
                     count++;
                     Thread.sleep(1000);
                  } catch (Exception ex) {
                     //JOptionPane.showMessageDialog(null, "App1 is closed");
                     break;
                  }
               }
            } catch (Exception ex) {
               //ex.printStackTrace();
            }

//сперва удаляем старую версию
    File file_delete = new File("Durak.jar");
        int retryCounter = 0;
        final int maxRetries = 10;
        boolean fileDeleted = false;
                while (!fileDeleted && retryCounter < maxRetries) {
            if (file_delete.delete()) {
                fileDeleted = true;
            } else {
                retryCounter++;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // ignore exception
                }
            }
}

//избавляемся от левого расширения
            File oldFile = new File("Durak.jar.tmp");
        File newFile = new File("Durak.jar");
if (oldFile.exists()) {
       boolean success = oldFile.renameTo(newFile);
        if (!success) {
//            System.out.println("File renaming failed.");
        }
}

// Launch file2.jar using Desktop
    if (newFile.exists()) {
      try {
        Desktop.getDesktop().open(newFile);
        System.exit(0);
      } catch (IOException e) {
        //System.err.println("Error launching Durak.jar");
        //e.printStackTrace();
      }
}

}//main
}//cl