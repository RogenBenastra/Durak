package classes;

import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class GetAppVersion
{

    public static String getVersion() {
        try {
            Manifest manifest = new Manifest(Durak.class.getResourceAsStream("/META-INF/MANIFEST.MF"));
            Attributes attributes = manifest.getMainAttributes();
            return attributes.getValue("Implementation-Version");
        } catch (IOException e) {
            return e.getMessage();
                   }
    }//fn
  
}//cl