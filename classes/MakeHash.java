package classes;

import java.io.*;
import java.security.MessageDigest;

public class MakeHash
{

  public static String getHash(String fileName)
{
try{    
MessageDigest digest = MessageDigest.getInstance("SHA-256");
    FileInputStream inputStream = new FileInputStream(fileName);
    byte[] data = new byte[1024];
    int read = 0;
    while ((read = inputStream.read(data)) != -1)
{
      digest.update(data, 0, read);
    }
    byte[] hash = digest.digest();
    StringBuilder hexString = new StringBuilder();
    for (byte b : hash)
{
      String hex = Integer.toHexString(0xff & b);
      if (hex.length() == 1)
{
        hexString.append('0');
}
            hexString.append(hex);
}
        return hexString.toString();
} catch (Exception e) {
        return e.getMessage();
    }
 }//fn

}//cl