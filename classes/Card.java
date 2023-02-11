package classes;
import java.util.*;

public class Card
{
private String suit;
private String face;
private int rank;

//геттер suit
public String getSuit()
{
return this.suit;
}

public String getFace()
{
return this.face;
}

public int getRank()
{
return this.rank;
}

//сеттер suit
public void setSuit( String suit)
{
this.suit=suit;
}

public void setFace(String face)
{
this.face=face;
}

public void setRank(int rank)
{
this.rank=rank;
}

public Card()
{
}

public Card(String face, String suit, int rank)
{
this.face=face;
this.suit=suit;
this.rank=rank;
}

@Override
public String toString()
{
return this.face+" "+this.suit;
}

//масти
static List<String> suitArray = Arrays.asList(
new String ("Бубны"),
new String ("Пики"),
new String ("Трефы"),
new String ("Червы")
);

//Достоинства
static List<String> faceArray = Arrays.asList(
new String ("Двойка"),
new String ("Тройка"),
new String ("Четверка"),
new String ("Пятерка"),
new String ("Шестерка"),
new String ("Семерка"),
new String ("Восьмерка"),
new String ("Девятка"),
new String ("Десятка"),
new String ("Валет"),
new String ("Дама"),
new String ("Король"),
new String ("Туз")
);

//формирование колоды
public static ArrayList<Card> rafCard()
{
int i=2;
ArrayList<Card> arr = new ArrayList<Card>();
for (String suit: suitArray)
{
for (String face: faceArray)
{
arr.add(new Card(face,suit, i++));
}
i=2;
}

return arr;
}//fn

}//cl