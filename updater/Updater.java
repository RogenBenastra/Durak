import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.io.File.*;
import java.nio.file.Files;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.StandardCopyOption;

import java.net.Socket;

public class Updater {

//deleting folder
  public static boolean deleteFolder(File directory) {
    if (directory.exists()) {
      File[] files = directory.listFiles();
      if (files != null) {
        for (File file : files) {
          if (file.isDirectory()) {
            deleteFolder(file);
          } else {
            file.delete();
          }
        }
      }
    }
    if (directory.delete()) {
      System.out.println("Directory and its subdirectories and files have been deleted.");
      return true;
    } else {
      System.out.println("Failed to delete directory and its subdirectories and files.");
      return false;
    }
  }


  public static void main(String[] args) {

//проверяем условия начала работы программы
File file1 = new File("_temp\\Durak.jar");
File file2 = new File("Durak.jar");
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
//
                     break;
                  }
               }
            } catch (Exception ex) {
               //ex.printStackTrace();
            }

//сперва удаляем старую версию
    File oldFile = new File("Durak.jar");
        int retryCounter = 0;
        final int maxRetries = 10;
        boolean fileDeleted = false;
                while (!fileDeleted && retryCounter < maxRetries) {
            if (oldFile.delete()) {
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

//копируем durak.jar из временной папки в постоянную
    try {
      // source file
      File source = new File("_temp\\Durak.jar");
      // destination file
      File destination = new File("Durak.jar");
      // move the file from source to destination
      Files.move(source.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
      // delete the source folder "_temp"
File temp_folder = new File("_temp");      
temp_folder.delete();
      //System.out.println("File moved and source folder deleted successfully!");
    } catch (Exception e) {
      System.out.println("Error while moving the file: " + e.getMessage());
    }

//удаляем временную папку
deleteFolder(new File("_temp"));

// Launch file2.jar using Desktop
File newFile = new File("Durak.jar");    
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