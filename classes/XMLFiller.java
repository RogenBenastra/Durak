import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.io.*;
import java.security.MessageDigest;

public class XMLFiller {

static String ver = "1.0.211";

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

    public static void main(String[] args) {
        
try {
            // Load the XML file into a Document object
            File xmlFile = new File("..\\Updates\\version.xml");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);

            // Find the "hash" node and update its value
            Node hashNode = doc.getElementsByTagName("hash").item(0);
            hashNode.setTextContent(getHash("..\\DN\\Durak.jar"));

Node verNode = doc.getElementsByTagName("version").item(0);
verNode.setTextContent(ver);

            // Save the updated XML file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(xmlFile);
            transformer.transform(source, result);
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }
}
