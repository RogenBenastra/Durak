public class MyClass {

public static native boolean JFWSayString(String lpszStringToSpeak, boolean bInterrupt);
public static native boolean JFWStopSpeech();
public static native boolean JFWRunScript(String lpszScriptName);

    static {
        System.loadLibrary("MyClass");
System.loadLibrary("jfwapi");
    }
}
        
