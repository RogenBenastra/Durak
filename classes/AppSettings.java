package classes;

import java.util.Properties;
import java.io.*;

public  class AppSettings
{
private Boolean intro;
private int soundMode;
private int sortingType;
private Boolean fullPack;
private Boolean checkUpdates;
private Boolean rightNow;
private String scores;
private Properties properties;

private int propertyToInt(String str)
{
try
{
int i = Integer.valueOf(str);
if (i<0) i=0;
if(i>3) i=3;
return i;
}
catch (Exception ex)
{
return 0;
}
}//fn

private Boolean propertyToBoolean(String str)
{
try
{
if(str.equals("true"))
return true;
else if(str.equals("false"))
return false;
else return true;
}
catch(Exception ex)
{
return false;
}
}//fn

//гетеры/сеттеры для приватных переменных
public  Boolean getIntro()
{
prepare();
this.intro= propertyToBoolean(properties.getProperty("intro"));
return this.intro;
}

public   void setIntro(Boolean b)
{
prepare();
properties.setProperty("intro", String.valueOf(b));
SavePropertiesToINI();
}

public  int getSoundMode()
{
prepare();
//this.soundMode=Integer.valueOf(properties.getProperty("soundMode"));
this.soundMode= propertyToInt(properties.getProperty("soundMode"));
return this.soundMode;
}

public   void setSoundMode(int i)
{
prepare();
properties.setProperty("soundMode", String.valueOf(i));
SavePropertiesToINI();
}

public  int getSortingType()
{
prepare();
this.sortingType=propertyToInt(properties.getProperty("sortingType"));
return this.sortingType;
}

public   void setSortingType(int i)
{
prepare();
properties.setProperty("sortingType", String.valueOf(i));
SavePropertiesToINI();
}

public  Boolean getFullPack()
{
prepare();
this.fullPack=propertyToBoolean(properties.getProperty("fullPack"));
return this.fullPack;
}

public   void setFullPack(Boolean b)
{
prepare();
properties.setProperty("fullPack", String.valueOf(b));
SavePropertiesToINI();
}

public  Boolean getCheckUpdates()
{
prepare();
this.checkUpdates=propertyToBoolean(properties.getProperty("checkUpdates"));
return this.checkUpdates;
}

public   void setCheckUpdates(Boolean b)
{
prepare();
properties.setProperty("checkUpdates", String.valueOf(b));
SavePropertiesToINI();
}

public  String getScores()
{
prepare();
this.scores=properties.getProperty("scores");
return this.scores;
}

public   void setScores(String s)
{
prepare();
properties.setProperty("scores", s);
SavePropertiesToINI();
}

public  Boolean getRightNow()
{
prepare();
this.rightNow=propertyToBoolean(properties.getProperty("rightNow"));
return this.rightNow;
}

public   void setRightNow(Boolean b)
{
prepare();
properties.setProperty("rightNow", String.valueOf(b));
SavePropertiesToINI();
}

//вспомогательный метод для подгрузки значений
 void prepare()
{
properties = new Properties();
try
{
properties.load(new FileInputStream(new File("settings.ini")));
}catch(Exception ex)
{
}
}//fn

//вспомогательный метод для сохранения значений
private void SavePropertiesToINI()
{
try
{
FileOutputStream outStream = new FileOutputStream("settings.ini");
properties.store(outStream,"settings storage file");
} catch(Exception ex){}
}//fn

}//cl