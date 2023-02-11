import classes.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing .*;
import javax.swing.event.*;
import javax.swing.tree.*;

import javax.sound.sampled.*;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import java.io.*;

import org.w3c.dom.*;
import javax.xml.parsers.*;

import java.net.*;

public class Durak extends JFrame
{

//version app
final String appVersion="1.0.2.11";

//an object of AppSettings class
AppSettings apps = new AppSettings();

//menu items
JCheckBoxMenuItem orig, rsg, sounds_off, bySuit, byValue, bySuitAndValue, sorting_off,fullPack, checkUpdatesOnStart, rightNow;

//root nodes on hands and desk
DefaultMutableTreeNode hands1 = new DefaultMutableTreeNode("Карты на руках");
DefaultMutableTreeNode desk = new DefaultMutableTreeNode("Карты на столе");

//tree models
DefaultTreeModel model1 = new DefaultTreeModel(hands1);
DefaultTreeModel model2 = new DefaultTreeModel(desk);

//trees of hands and desk
JTree tree1 = new JTree(model1);
JTree tree2 = new JTree(model2);

//a button
JButton button=new JButton("Начать Игру");

//playing objects
Card oneGo, twoGo, trump;

//when trump is taken
boolean trumpIsTaken = false;

//scores of both players
int scores1, scores2;

//working pack
ArrayList<Card> wPack = new ArrayList<Card>();

//cards on computer's hands
ArrayList<Card> hands2 = new ArrayList<Card>();

//cards in drawn game
        ArrayList<Card> cardsInGame = new ArrayList<Card>();
        
//whose turn, true - me, false - him
        boolean turn;

//methods

//raise the closing event
void raiseClosingEvent()
{
this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
}

//remove node under cursor
void removeCardUnderCursor()
{
DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree1.getSelectionPath().getLastPathComponent();
DefaultTreeModel model = (DefaultTreeModel) tree1.getModel();
model.removeNodeFromParent(selectedNode);
}//fn

//очищает нужное дерево от узлов
void clearTree(DefaultMutableTreeNode n, JTree t)
{
        DefaultTreeModel model = (DefaultTreeModel) t.getModel();
        n.removeAllChildren();
        model.reload();
    }//fn

//возвращает индекс активного элемента
int getIndex()
{
DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree1.getSelectionPath().getLastPathComponent();
return hands1.getIndex(node);
}//fn

//возвращает активный элемент
DefaultMutableTreeNode getActiveNode()
{
DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree1.getSelectionPath().getLastPathComponent();
return node;
}//fn

//возвращает объект в виде карты по индексу
Card getCardByIndex(int i)
{
return (Card) ((DefaultMutableTreeNode) hands1.getChildAt(i)).getUserObject();
}//fn

//возвращает объект карту под курсором
Card getCardUnderCursor()
{
return  (Card) ((DefaultMutableTreeNode) hands1.getChildAt(getIndex())).getUserObject();
}//fn

//добавить карту на стол
void addToDesk(String s)
{
DefaultTreeModel model = (DefaultTreeModel) tree2.getModel();
if (desk.getChildCount()>0)
model.insertNodeInto(new DefaultMutableTreeNode(s), desk, desk.getChildCount());
else
{
desk.add(new DefaultMutableTreeNode(s));
model.reload();
}
}//fn

//добавление карты ко мне
void addCard(Card c, JTree t)
{
DefaultTreeModel model = (DefaultTreeModel) t.getModel();
if(hands1.getChildCount() == 0)
{
hands1.add(new DefaultMutableTreeNode(c));
model.reload();
}
else
model.insertNodeInto(new DefaultMutableTreeNode(c), hands1, hands1.getChildCount());
}//fn

//удаление последнего элемента со стола
void removeLastItemFromDesk()
{
DefaultMutableTreeNode node = (DefaultMutableTreeNode) desk.getChildAt(desk.getChildCount()-1);
 DefaultTreeModel model = (DefaultTreeModel) tree2.getModel();
model.removeNodeFromParent(node);
}//fn

//курсор на стол
void moveToDesk()
{
var selectedPath = tree2.getPathForRow(desk.getChildCount()-1);
tree2.setSelectionPath(selectedPath);
tree2.requestFocus();
}//fn

//курсор ко мне
void moveToMyHands()
{
var selectedPath = tree1.getPathForRow(0);
tree1.setSelectionPath(selectedPath);
tree1.requestFocus();
}//fn

//сохранение результатов в файл
void saveScores()
{
apps.setScores(scores1+"x"+scores2);
}//fn

//держим младшие козыри
boolean youngTrumpsBlocker()
{
if(wPack.size()>18 && !apps.getFullPack())
return false;
if(wPack.size()>26 && apps.getFullPack())
return false;
return true;
}//fn

//держим старшие козыри
boolean highTrumpsBlocker(Card input)
{
int i= rnd();
if (input.getRank() > 11 && input.getSuit().equals(trump.getSuit()) && wPack.size() != 0&&i==0)
return false;
else
return true;
}//fn

//держит карты старшего достоинства
Boolean highRankBlocker(Card input)
{
int i= rnd();
boolean b1=false, b2=false;
if(input.getRank() > 12 && input.getSuit().equals(oneGo.getSuit()) && i ==0)
b1=false;
else b1= true;

if(wPack.size() > 18 && !apps.getFullPack())
b2=false;
else b2=true;

if(wPack.size() > 26 && apps.getFullPack())
b2=false;
else b2=true;

if(b1==false&&b2==false)
return false;
else
return true;
}//fn

//результаты игры
boolean winner()
{
//моя победа
if(win()==1)
{
if(desk.getChildCount()>1&&turn)
{
removeLastItemFromDesk();
hands2.add(oneGo);
addToDesk(oneGo.toString());
}
moveToDesk();
playSound("you-win");
trump=null;
scores1++;
saveScores();
button.setEnabled(true);
button.setText("Новая игра");
return true;
}
//победа соперника
if(win()==2)
{
moveToDesk();
playSound("pc-win");
trump=null;
scores2++;
saveScores();
button.setEnabled(true);
button.setText("Новая игра");
return true;
}
//взаимно отбились
if (win() == 3)
{
nullRound();
playSound("bito2");
serving();
if(turn)
{
turn=false;
if(winner())
return true;
attack();
return true;
}
if(!turn)
{
turn=true;
moveToMyHands();
if(winner())
return true;
return true;
}
}
//противник победил
if(win()==4)
{
nullRound();
playSound("DEAL");
playSound("lastcard2");
addCard(trump,tree1);
trumpIsTaken=true;
turn=false;
attack();
moveToDesk();
return true;
}
//он ходил, я отбил, он взял козырь, я сходил
if(win()==5)
{
nullRound();
playSound("DEAL");
playSound("lastcard1");
hands2.add(trump);
trumpIsTaken=true;
turn=true;
return true;
}
// я отбился
if(win()==6)
{
nullRound();
playSound("bito2");
clearTree(desk,tree2);
serving();
moveToMyHands();
turn=true;
//противник берет козырь, в колоде 0, мой ход и я победил, т.к. у меня 0 карт
if(winner())
{
return true;
}
return true;
}
//противник отбился
if(win()==7)
{
nullRound();
playSound("allcards");
clearTree(desk,tree2);
serving();
turn=false;
attack();
moveToDesk();
return true;
}
//
if (win()==8)
{
nullRound();
addCard(trump,tree1);
trumpIsTaken=true;
turn=false;
attack();
return true;
}
return false;
}//fn

//сортировка
void sorting()
{
ArrayList<Card> tmp_card = new ArrayList<Card>();
for (int i=0;i<=hands1.getChildCount()-1;i++)
tmp_card.add(getCardByIndex(i));
clearTree(hands1,tree1);

        hands1.removeAllChildren();
        model1.reload();
    
//сортировка по масти
if(apps.getSortingType()==1)
{
tmp_card.sort(Comparator.comparing(Card::getSuit));
for (Card c:tmp_card)
addCard(c,tree1);
return;
}

//по достоинству
if(apps.getSortingType()==2)
{
tmp_card.sort(Comparator.comparing(Card::getRank));
for (Card c:tmp_card)
addCard(c,tree1);
return;
}

//по масти и достоинству
if(apps.getSortingType()==3)
{
tmp_card.sort(Comparator.comparing(Card::getRank));
for (Card c:tmp_card)
{
if(c.getSuit().equals("Пики"))
addCard(c,tree1);
}
for (Card c:tmp_card)
{
if(c.getSuit().equals("Трефы"))
addCard(c,tree1);
}
for (Card c:tmp_card)
{
if(c.getSuit().equals("Червы"))
addCard(c,tree1);
}
for (Card c:tmp_card)
{
if(c.getSuit().equals("Бубны"))
addCard(c,tree1);
}
}

if(apps.getSortingType()==0)
{
for (Card c:tmp_card)
addCard(c,tree1);
return;
}
}//fn

//раздача слонов
void serving()
{
int g1= hands1.getChildCount();
int g2= hands2.size();
if (g1 > 6) g1 = 6;
if (g2 > 6) g2 = 6;
int summ = (6 - g1) + (6 - g2);

//локальная инфа о ходе
boolean localTurn=turn;
//определение кому сдавать кз, если в колоде карты кончились
boolean hasPack = false;
//пока не кончилась колода
for (int i = 0; i <= summ; i++)
{

if (turn)
{
if(hands1.getChildCount()<6 && wPack.size()>0)
{
addCard(wPack.get(0),tree1);
wPack.remove(0);
playSound("DEAL");
localTurn=true;
hasPack=true;
}
if(hands2.size()<6 && wPack.size()>0)
{
hands2.add(wPack.get(0));
wPack.remove(0);
playSound("DEAL");
localTurn=false;
hasPack=true;
}
}//turn

if (!turn)
{
if(hands2.size()<6 && wPack.size()>0)
{
hands2.add(wPack.get(0));
wPack.remove(0);
playSound("DEAL");
localTurn=false;
hasPack=true;
}
if(hands1.getChildCount()<6&&wPack.size()>0)
{
addCard(wPack.get(0),tree1);
wPack.remove(0);
playSound("DEAL");
localTurn=true;
hasPack=true;
}
}//!turn
}//for

g1=hands1.getChildCount();
g2=hands2.size();
if (g1 > 6) g1 = 6;
if (g2 > 6) g2 = 6;
summ = (6 - g1) + (6 - g2);

//выдача козыря
for (int i = 0; i <= summ; i++)
{
//пришло время выдавать
if(wPack.size()==0&&!trumpIsTaken&&summ!=0)
{
//просто мне, потому что я ходил
if (!localTurn&&hands1.getChildCount()<6&&hasPack)
{
addCard(trump,tree1);
playSound("DEAL");
playSound("lastcard2");
trumpIsTaken=true;
break;
}
//мне, потому что противник подзавязку
if (hands2.size()>=6&&hands1.getChildCount()<6)
{
addCard(trump,tree1);
playSound("DEAL");
playSound("lastcard2");
trumpIsTaken=true;
break;
}
//мне, потому что противник отбился, у него нет карт, а колода кончилась
if (localTurn && !hasPack&&hands1.getChildCount()<6)
{
addCard(trump,tree1);
playSound("DEAL");
playSound("lastcard2");
trumpIsTaken=true;
break;
}
//просто противнику, потому что ход был его
if (!localTurn && hands2.size() < 6&&hasPack)
{
hands2.add(trump);
playSound("DEAL");
playSound("lastcard1");
trumpIsTaken=true;
break;
}
//противнику,когда колода пустая и ход был его
if (!localTurn && !hasPack&&hands2.size()<6)
{
hands2.add(trump);
playSound("DEAL");
playSound("lastcard1");
trumpIsTaken=true;
break;
}
//уже потерял мысль
if (localTurn&&hands2.size()<6)
{
hands2.add(trump);
playSound("DEAL");
playSound("lastcard1");
trumpIsTaken=true;
break;
}
//я забит под завязку
if (hands1.getChildCount()>=6&&hands2.size()<6)
{
hands2.add(trump);
playSound("DEAL");
playSound("lastcard1");
trumpIsTaken=true;
break;
}
//если он ходил, а я отбился
if (!localTurn && !hasPack)
{
hands2.add(trump);
playSound("DEAL");
playSound("lastcard1");
trumpIsTaken=true;
break;
}
}//if
}//for

sorting();
}//fn

//могу ли я докинуть
Boolean can1Add()
{
int i = getCardUnderCursor().getRank();
 for(Card c:cardsInGame)
{
if(c.getRank()==i)
return true;
}
return false;
}//fn

//может ли противник докинуть
boolean can2Add(Card input)
{
for (Card c:cardsInGame)
{
if(input.getRank()==c.getRank())
return true;
}
return false;
}//fn

//метод для сокращения объема кода
void addOps(Card input)
{
twoGo=input;
addToDesk(twoGo.toString());
cardsInGame.add(twoGo);
hands2.remove(twoGo);
}//fn

//компьютер атакует или докидывает
void attack()
{
button.setEnabled(true);
button.setText("Взять");
//сортируем противника по достоинству
hands2.sort(Comparator.comparing(Card::getRank));

//первый ход
if(cardsInGame.size()==0)
{
for (int i1 = 0; i1 <= hands2.size() - 1; i1++)
{
//ходим некозырем
if (trump.getSuit().equals(hands2.get(i1).getSuit())==false)
{
addOps(hands2.get(i1));

if (win() == -1)
{
playSound("distrib");
moveToDesk();
return;
}
else if(winner())
return;
}//if
}//for

//ходит козырем
for (int i2=0;i2<=hands2.size()-1;i2++)
{
if (trump.getSuit().equals(hands2.get(i2).getSuit())
/*&& youngTrumpsBlocker() && highTrumpsBlocker(hands2.get(i2))*/)
{
addOps(hands2.get(i2));

if(win()==-1)
{
playSound("distrib");
moveToDesk();
return;
}
else if(winner())
return;
}
}//for
}//if первый ход

//карты в розыгрыше уже есть
if(cardsInGame.size()>0)
{
//пробуем атаковать  некозырями
for (int i3 = 0; i3 <= hands2.size() - 1; i3++)
{
if (can2Add(hands2.get(i3)) && hands2.get(i3).getSuit().equals(trump.getSuit())==false)
{
addOps(hands2.get(i3));

if (win() == -1)
{
moveToDesk();
playSound("takemore");
playSound("distrib");
return;
}
else if (winner())
return;
}
}//for

//атакуем козырями
for (int i4 = 0; i4 <= hands2.size() - 1; i4++)
{
if (can2Add(hands2.get(i4)) && hands2.get(i4).getSuit().equals(trump.getSuit())
/*&& youngTrumpsBlocker() && highTrumpsBlocker(hands2.get(i4))*/)
{
addOps(hands2.get(i4));

if (win() == -1)
{
moveToDesk();
playSound("takemore");
playSound("distrib");
return;
}
else if(winner())
return;
}
}//for
}//if карты в розыгрыше уже есть

if(winner())
return;

//а если дошли сюда, то уже нечем ходить. говорим бито
playSound("bito2");
button.setEnabled(false);

if(winner())
return;

nullRound();
serving();
turn=true;
moveToMyHands();
}//fn

//вспомогательный метод для защиты
void defOps(Card input)
{
twoGo=input;
hands2.remove(twoGo);
cardsInGame.add(twoGo);
removeLastItemFromDesk();
addToDesk(oneGo.getFace() + " "+ oneGo.getSuit()+" побита "+twoGo.getFace()+" "+twoGo.getSuit());
moveToDesk();
}//fn

//противник отбивается
void defend()
{
button.setEnabled(true);
button.setText("Бито");
hands2.sort(Comparator.comparing(Card::getRank));

//отбиваемся некозырем, если есть
for (int i1 = 0; i1 <= hands2.size() - 1; i1++)
{
if(oneGo.getSuit().equals(hands2.get(i1).getSuit())
 && oneGo.getRank() < hands2.get(i1).getRank()
 && oneGo.getSuit().equals(trump.getSuit()) == false
/* && highRankBlocker(hands2.get(i1))*/)
{
defOps(hands2.get(i1));

if(win() == -1)
{
playSound("distrib");
playSound("covered");
return;
}
else if(winner())
return;
}//первый if
}//for

//отбиваемся козырем, если есть
for (int i2 = 0; i2 <= hands2.size() - 1; i2++)
{
if((oneGo.getSuit().equals(hands2.get(i2).getSuit())
&&oneGo.getRank()<hands2.get(i2).getRank())
| (hands2.get(i2).getSuit().equals(trump.getSuit())&&!oneGo.getSuit().equals(trump.getSuit())))
{
defOps(hands2.get(i2));

if(win()==-1)
{
playSound("distrib");
playSound("covered");
return;
}
else if (winner())
return;
}
}//for

//не смогли отбиться - забираем или проигрываем
button.setEnabled(false);
takeCards();

if(winner())
return;
nullRound();
serving();
moveToMyHands();
}//fn

//проигравший забирает карты
void takeCards()
{
clearTree(desk,tree2);

if(turn)
{
for (Card c:cardsInGame)
hands2.add(c);
playSound("i-take");
playSound("take");
hands2.sort(Comparator.comparing(Card::getRank));
}//if

if(!turn)
{
for (Card c:cardsInGame)
addCard(c,tree1);
playSound("take-it");
}//if

cardsInGame.clear();
}//fn

//сброс результатов раунда
void nullRound()
{
clearTree(desk,tree2);
cardsInGame.clear();
oneGo=null;
twoGo=null;
}//fn

//начинаем игру
void startGame()
{
cardsInGame.clear();
wPack.clear();
hands2.clear();
clearTree(hands1, tree1);
clearTree(desk,tree2);
oneGo=null; twoGo=null; trump=null;
trumpIsTaken=false;
loadScores();

//генерируем колоду
playSound("kn");
packGenerator();

//раздача слонов
serving();

//назначаем козырь
int j = rndMax(wPack.size()-1);
trump=wPack.get(j);
wPack.remove(j);

JOptionPane.showMessageDialog(this,"", "козырь "+trump.toString(),1);

//чей ход
turn=whoseTurn();

if(turn)
{
playSound("you-go");
button.setEnabled(false);
moveToMyHands();
return;
}

if(!turn)
{
playSound("i-go");
attack();
return;
}

}//fn

//генерируем колоду
void packGenerator()
{
ArrayList<Card> tmp_pack = new ArrayList<Card>();
for (Card c:Card.rafCard())
tmp_pack.add(c);

int cnt=0;
while (tmp_pack.size()>0)
{
cnt = rndMax(tmp_pack.size()-1);
wPack.add(tmp_pack.get(cnt));
tmp_pack.remove(cnt);
}

//делаем усеченную колоду
if(apps.getFullPack()==false)
{
for(int i=0;i<=wPack.size()-1;i++)
{
if(wPack.get(i).getRank()<6)
{
wPack.remove(i);
i--;
}
}
}

}//fn

//ходит первым тот, у кого меньший козырь
boolean whoseTurn()
{
ArrayList<Card> koz1 =new ArrayList<Card>();
ArrayList<Card> koz2 =new ArrayList<Card>();

for (int i2 = 0; i2 <= hands2.size()-1; i2++)
{
if(trump.getSuit().equals(hands2.get(i2).getSuit()))
{
koz2.add(hands2.get(i2));
continue;
}
}//for

for (int i1 = 0; i1 <= hands1.getChildCount()-1; i1++)
{
if(trump.getSuit().equals(getCardByIndex(i1).getSuit()))
{
koz1.add(getCardByIndex(i1));
continue;
}
}//for

//нет и нет
if(koz1.size()==0 && koz2.size()==0)
{
int z = rnd();
if(z==0) return false;
else return true;
}

//да и да
if(koz1.size()>0&&koz2.size()>0)
{
if(koz1.get(0).getRank() > koz2.get(0).getRank())
return false;
else return true;
}

//нет и да
if(koz1.size()==0&&koz2.size()!=0) return true;
//да и нет
if(koz2.size()==0&&koz1.size()!=0) return false;

return false;
}//fn

//результаты игры
int win()
{
//я победил
                if (hands1.getChildCount() == 0 && hands2.size() > 0 && trumpIsTaken && (turn | !turn))
return 1;
//противник победил
            if (hands2.size() == 0 && hands1.getChildCount() > 0 && trumpIsTaken && (!turn|turn))
            return 2;
//взаимно отбились
            if (hands1.getChildCount() == 0 && hands2.size() == 0 && !trumpIsTaken)
            return 3;
            //я хожу, противник отбивает, мне выдаётся козырь, он ходит и побеждает
if (hands1.getChildCount() == 0 && trumpIsTaken==false && turn&&hands2.size()==1)
return 4;
            //я отбился
            if (hands1.getChildCount() == 0 && hands2.size() > 0 && !trumpIsTaken && !turn)
return 6;
            //противник отбился
            if (hands2.size() == 0 && hands1.getChildCount() > 0 && !trumpIsTaken && turn)
return 7;
//если не совпала никакая ситуация            
return -1;
}//fn

//генерирует числа от 0 до 1, включая обадо максимального значения
int rndMax(int n)
{
if (n<0) n=0;
return ThreadLocalRandom.current().nextInt(0, n+1);
}//fn

//генерирует числа от 0 до 1, включая оба
public int rnd()
{
int n = 100000;
int res = ThreadLocalRandom.current().nextInt(0, n+1);
if(res<=(n/2)) return 0;
else return 1;
}//fn

//сохранение очков
      void savescoress()
{
apps.setScores(scores1+":"+scores2);
}//fn
	  
	  //загрузка очков
	  void loadScores()
	  {
	  String [] a= apps.getScores().split("[x]");
	  scores1 = Integer.parseInt(a[0]);
	  scores2 = Integer.parseInt(a[1]);
	  }//fn

//проигрывание звуков
void playSound(String s)
{

//если звуки выключены
if(apps.getSoundMode()==0)
{
if(s.equals("i-take"))
{
JOptionPane.showMessageDialog(null,"", "Забираю!",1);
return;
}
if(s.equals("bito2"))
{
JOptionPane.showMessageDialog(null,"", "бито!",1);
return;
}
if(s.equals("you-go"))
{
JOptionPane.showMessageDialog(null,"", "ходи первый!",1);
return;
}
if(s.equals("i-go"))
{
JOptionPane.showMessageDialog(null,"", "Я захожу!",1);
return;
}
if(s.equals("allcards"))
{
JOptionPane.showMessageDialog(null,"", "я отбился!",1);
return;
}
if(s.equals("lastcard2"))
{
JOptionPane.showMessageDialog(null,"", "я забрал козырь!",1);
return;
}
if(s.equals("lastcard1"))
{
JOptionPane.showMessageDialog(null,"", "ты забрал козырь!",1);
return;
}
if(s.equals("you-win"))
JOptionPane.showMessageDialog(null,"", "Ты победил!",1);
if(s.equals("you-win"))
JOptionPane.showMessageDialog(null,"", "я победил!",1);
return;
}
//если используются оригинальные звуки
if(apps.getSoundMode()==1)
{
return;
}
//звуки Red Storm Group
if(apps.getSoundMode()==2)
{
Sound snd=new Sound("Sounds\\"+s+".wav");
snd.play();
if(s.equals("covered")|s.equals("takemore")|s.equals("distrib"))
{
Long div= snd.getLength();
div=div/2;
while(snd.getPos()<div)
{
try
{
Thread.sleep(1);
}catch(Exception x){}
}
return;
}
while(snd.getPos()<snd.getLength())
{
try
{
Thread.sleep(1);
}catch(Exception x){}
}
snd.stop();
}//if
}//fn

public void checkSettingsFile()
{
File file=new File("settings.ini");
if (!file.exists())
{
try
{       
FileWriter writer = new FileWriter(file, false);
String text = "sortingType=3\r\nscores=0x0\r\nsoundMode=2\r\nintro=false\r\ncheckUpdates=false\r\nfullPack=false\r\nrightNow=false";
            writer.write(text);
            writer.flush();
}
catch (IOException ex){}
}
        }//fn

//метод для установки флажков на звуки и сортировку
Boolean checkFlagState(String s)
{

//звуки
if(s.equals("orig")&&apps.getSoundMode()==1)
return true;
else if(s.equals("rsg")&&apps.getSoundMode()==2)
return true;
else if(s.equals("sounds_off ")&&apps.getSoundMode()==0)
return true;
//сортировка
else if(s.equals("bySuit")&&apps.getSortingType()==1)
return true;
else if(s.equals("byValue")&&apps.getSortingType()==2)
return true;
else if(s.equals("bySuitAndValue")&&apps.getSortingType()==3)
return true;
else if(s.equals("sorting_off")&&apps.getSortingType()==0)
return true;

return false;
}//fn

//метод для создания системы меню
void createMenu()
{
JMenuBar menuBar = new JMenuBar();

JMenu settingsMenu = new JMenu("Настройки");

//встроенное менюЗвуки
JMenu soundsMenu = new JMenu("Звуки");
orig = new JCheckBoxMenuItem("Оригинальные",checkFlagState("orig"));
rsg = new JCheckBoxMenuItem("Red Storm Group",checkFlagState("rsg"));
sounds_off = new JCheckBoxMenuItem("НЕТ",checkFlagState("sounds_off "));
//добавляем
soundsMenu.add(orig);
soundsMenu.add(rsg);
soundsMenu.add(sounds_off);

    //встроенное меню Сортировка
JMenu sortingMenu = new JMenu("Сортировка");
bySuit = new JCheckBoxMenuItem("По масти",checkFlagState("bySuit"));
byValue = new JCheckBoxMenuItem("По достоинству",checkFlagState("byValue"));
bySuitAndValue = new JCheckBoxMenuItem("По масти и достоинству",checkFlagState("bySuitAndValue"));
sorting_off = new JCheckBoxMenuItem("НЕТ",checkFlagState("sorting_off"));
//добавляем
sortingMenu.add(bySuit);
sortingMenu.add(byValue);
sortingMenu.add(bySuitAndValue);
sortingMenu.add(sorting_off);

//остальные
fullPack = new JCheckBoxMenuItem("Полная колода", apps.getFullPack());
checkUpdatesOnStart = new JCheckBoxMenuItem("Проверять обновления при старте", apps.getCheckUpdates());
rightNow = new JCheckBoxMenuItem("Сразу к делу", apps.getRightNow());
JMenuItem checkUpdates = new JMenuItem(new CheckUpdates());
      JMenuItem exit = new JMenuItem(new ExitAction());
//добавляем
settingsMenu.add(soundsMenu);
settingsMenu.add(sortingMenu);
settingsMenu.add(fullPack);
settingsMenu.add(rightNow);
settingsMenu.add(checkUpdates);
    settingsMenu.add(exit);        

      menuBar.add(settingsMenu);
setJMenuBar(menuBar);
}//fn

//метод для разблокирования клавиши alt
private static void setupMenuKey(final JFrame frame) {
Action menuAction = new AbstractAction() {
@Override
public void actionPerformed(ActionEvent e) {
JRootPane rootPane = frame.getRootPane();
JMenuBar jMenuBar = rootPane.getJMenuBar();
JMenu menu = jMenuBar.getMenu(0);
menu.doClick();
}
};
JRootPane rootPane = frame.getRootPane();
ActionMap actionMap = rootPane.getActionMap();
final String MENU_ACTION_KEY = "expand_that_first_menu_please";
actionMap.put(MENU_ACTION_KEY, menuAction);
InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ALT, 0, true), MENU_ACTION_KEY);
}//fn

//getting version and hash from remote version.xml file
public String getInfoFromXML(String incomingString, String nodeName) {
    try {
        InputStream stream = new ByteArrayInputStream(incomingString.getBytes());
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(stream);
        Element e = doc.getDocumentElement();
        NodeList nodeList = doc.getElementsByTagName("root");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                NodeList nodelist = element.getElementsByTagName(nodeName);
                Element element1 = (Element) nodelist.item(0);
                NodeList fstNm = element1.getChildNodes();
                return (fstNm.item(0)).getNodeValue();
            }
        }
    } catch (Exception x) {
        return x.getMessage();
    }
    return null;
}//fn

//downloading into a String var
public static String DownloadVersionFileToString(String url) {
    try {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
                int responseCode = con.getResponseCode();
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return response.toString();
    } catch (Exception e) {
        e.printStackTrace();
        return null;
    }
}//fn

//получение информации о номере версии из jar
public static String getVersionFromManifest() {
        try {
            Manifest manifest = new Manifest(Durak.class.getResourceAsStream("/META-INF/MANIFEST.MF"));
            Attributes attributes = manifest.getMainAttributes();
            return attributes.getValue("Implementation-Version");
        } catch (IOException e) {
            return e.getMessage();
                   }
    }//fn

//получение информации из xml
public String getInfoFromXML(String nodeName)
{
try{
File file = new File("Updates\\version.xml");
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(file);
       Element e = doc.getDocumentElement();
        NodeList nodeList = doc.getElementsByTagName("root");
        for (int i = 0; i < nodeList.getLength(); i++)
{
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE)
{
                Element element = (Element) node;
                NodeList nodelist = element.getElementsByTagName(nodeName);
                Element element1 = (Element) nodelist.item(0);
                NodeList fstNm = element1.getChildNodes();
                              return (fstNm.item(0)).getNodeValue();
}
        }
}catch(Exception x){return x.getMessage();}
return null;
}//fn

//download file
public static Boolean downloadFile(String url) {
    try {
        URL link = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) link.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
return false;
        }
        String[] split = link.getFile().split("/");
        String fileName = split[split.length - 1] + ".tmp";
        try (FileOutputStream outputStream = new FileOutputStream(fileName)) {
            byte[] buffer = new byte[4096];
            int bytesRead = -1;
            while ((bytesRead = connection.getInputStream().read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
        connection.disconnect();
//JOptionPane.showMessageDialog(null,"", "загрузка окончена",1);        
return true;
    } catch (IOException e) {
//JOptionPane.showMessageDialog(null,"", e.getMessage(),1);                
return false;
    }
}//fn

//запуск кода в отдельном потоке
void newThread(int i)
{
Runnable task = () ->
{
//to do
};
 Thread t1=new Thread (task);
t1.start();
}//fn

//конструктор
    public Durak()
{
        super("Карточная игра «ДУРАК»");
setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

//проверка наличия файла настроек
checkSettingsFile();

//создаем меню
createMenu();

//делаем корни невидимыми
tree1.setRootVisible(false);
tree2.setRootVisible(false);

//события и обработчики

//сортировка по масти
bySuit.addItemListener(new ItemListener() {
@Override
public void itemStateChanged(ItemEvent e) {
           if(bySuit.getState()){
               bySuit.setState(true);
byValue.setState(false);
bySuitAndValue.setState(false);
sorting_off.setState(false);
apps.setSortingType(1);
sorting();
           } else {
               bySuit.setState(false);
apps.setSortingType(0);
sorting();
               }
}
});

//по достоинству
byValue.addItemListener(new ItemListener() {
@Override
public void itemStateChanged(ItemEvent e) {
           if(byValue.getState()){
               bySuit.setState(false);
byValue.setState(true);
bySuitAndValue.setState(false);
sorting_off.setState(false);
apps.setSortingType(2);
sorting();
           } else {
               byValue.setState(false);
apps.setSortingType(0);
sorting();
               }
}
});

//по масти и достоинству
bySuitAndValue.addItemListener(new ItemListener() {
@Override
public void itemStateChanged(ItemEvent e) {
           if(bySuitAndValue.getState()){
               bySuit.setState(false);
byValue.setState(false);
bySuitAndValue.setState(true);
sorting_off.setState(false);
apps.setSortingType(3);
sorting();
           } else {
               bySuitAndValue.setState(false);
apps.setSortingType(0);
sorting();
               }
}
});

//сортировка выкл
sorting_off.addItemListener(new ItemListener() {
@Override
public void itemStateChanged(ItemEvent e) {
           if(sorting_off.getState()){
               bySuit.setState(false);
byValue.setState(false);
bySuitAndValue.setState(false);
sorting_off.setState(true);
apps.setSortingType(0);
sorting();           
} else {
               sorting_off.setState(false);
sorting();
               }
}
});

//оригинальные звуки

orig.addItemListener(new ItemListener() {
@Override
public void itemStateChanged(ItemEvent e) {
           if(orig.getState()){
               orig.setState(true);
rsg.setState(false);
sounds_off.setState(false);
apps.setSoundMode(1);
           } else {
               orig.setState(false);
}
}
});

//звуки Red Storm Group
rsg.addItemListener(new ItemListener() {
@Override
public void itemStateChanged(ItemEvent e) {
           if(rsg.getState()){
               rsg.setState(true);
orig.setState(false);
sounds_off.setState(false);
apps.setSoundMode(2);
           } else {
               rsg.setState(false);
}
}
});

//звуки выкл
sounds_off.addItemListener(new ItemListener() {
@Override
public void itemStateChanged(ItemEvent e) {
           if(sounds_off.getState()){
               sounds_off.setState(true);
orig.setState(false);
rsg.setState(false);
apps.setSoundMode(0);
           } else {
               sounds_off.setState(false);
               }
}
});

//обработчик нажатия пункта меню проверка обновлений при старте

checkUpdatesOnStart.addItemListener(new ItemListener() {
@Override
public void itemStateChanged(ItemEvent e) {
           if(checkUpdatesOnStart.getState()){
               checkUpdatesOnStart.setState(true);
apps.setCheckUpdates(true);
           } else {
               checkUpdatesOnStart.setState(false);
apps.setCheckUpdates(false);
}
}
});

//обработчик нажатия пункта полная или сокращенная колода

fullPack.addItemListener(new ItemListener() {
@Override
public void itemStateChanged(ItemEvent e) {
           if(fullPack.getState()){
               fullPack.setState(true);
apps.setFullPack(true);
           } else {
               fullPack.setState(false);
apps.setFullPack(false);
}
}
});

//обработчик нажатия пункта меню rightNow

rightNow.addItemListener(new ItemListener() {
@Override
public void itemStateChanged(ItemEvent e) {
           if(rightNow.getState()){
               rightNow.setState(true);
apps.setRightNow(true);
           } else {
               rightNow.setState(false);
apps.setRightNow(false);
}
}
});

//события

//обработчик нажатия enter на дереве1
tree1.addKeyListener(new KeyAdapter()
{
@Override
            public void keyPressed(KeyEvent e)
            {
    if(e.getKeyCode()==KeyEvent.VK_ENTER)
    {

//я хожу
if(turn)
{
//первый ход
if(cardsInGame.size()==0)
{
oneGo=getCardUnderCursor();
addToDesk(oneGo.toString());
removeCardUnderCursor();
cardsInGame.add(oneGo);

if(winner())
return;

defend();
return;
}//первый ход

//второй и последующие ходы
if(cardsInGame.size()>0)
{
//можно ли докинуть
Boolean b = can1Add();
if(b)
{
oneGo= getCardUnderCursor();
 addToDesk(oneGo.toString());
removeCardUnderCursor();
cardsInGame.add(oneGo);

if(winner())
return;

defend();
}//я докидывал
}//второй ход
}//turn

//отбиваюсь
if(!turn)
{
oneGo= getCardUnderCursor();
 //если масти совпадают и моя карта старше или у меня козырь
if(
(oneGo.getSuit().equals(twoGo.getSuit())
&& oneGo.getRank() > twoGo.getRank())
||(oneGo.getSuit().equals(twoGo.getSuit())==false
&&oneGo.getSuit().equals(trump.getSuit()))
)
{
cardsInGame.add(getCardUnderCursor());
removeLastItemFromDesk();
removeCardUnderCursor();
addToDesk(twoGo+" побита "+oneGo);

if(winner())
return;

attack();
}
}//!turn

//    
}
}
});

//обработчик нажатия кнопки

button.addActionListener(new ActionListener()
{
@Override
    public void actionPerformed(ActionEvent e)
    {

if(trump==null)
{
startGame();
return;
}

//я отходился, больше не хочу, нажимаем "бито", передаем ход
if(turn)
{
desk.removeAllChildren();
nullRound();
serving();
turn=false;
attack();
return;
}

//не могу отбить, беру, нажимаю "взять", ход противника
if(!turn)
{
if(hands2.size()==0 && !trumpIsTaken)
{
button.setEnabled(false);
takeCards();
serving();
nullRound();
attack();
button.setEnabled(true);
button.setText("Взять");
return;
}

if(winner())
return;

takeCards();
serving();
nullRound();
attack();
}
//    
}
    });

//однокнопочные и двукнопочные нажатия
KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher()
{
@Override
public boolean dispatchKeyEvent(KeyEvent e)
{

//сюда добавляем одиночные клавиши

if((e.getKeyCode() == KeyEvent.VK_N && e.getID() == KeyEvent.KEY_PRESSED) & e.isControlDown() )
{
startGame();
return true;
}

/*
//движение по shift+tab
if((e.getKeyCode() == KeyEvent.VK_TAB && e.getID() == KeyEvent.KEY_PRESSED) & e.isShiftDown() )
{
return true;
}
*/

if(e.getKeyCode() == KeyEvent.VK_F2 && e.getID() == KeyEvent.KEY_PRESSED )
{

return true;
}//f2

//козырь
if(e.getKeyCode() == KeyEvent.VK_F5 && e.getID() == KeyEvent.KEY_PRESSED )
{
if(trump==null)
{
JOptionPane.showMessageDialog(null,"","козырь не назначен",1);
return true;
}
if(!trumpIsTaken)
{
JOptionPane.showMessageDialog(null,"",trump.getFace()+" "+trump.getSuit(),1);
}
if (trumpIsTaken)
{
JOptionPane.showMessageDialog(null,"","Козырь "+trump.getFace()+" "+trump.getSuit()+" взят",1);
}
}//f5

//остаток карт в колоде
if(e.getKeyCode() == KeyEvent.VK_F6 && e.getID() == KeyEvent.KEY_PRESSED )
{
if(wPack==null)
{
JOptionPane.showMessageDialog(null,"","колода отсутствует",1);
return true;
}
else
{
JOptionPane.showMessageDialog(null,"",wPack.size()+" карт в колоде",1);
}
}//f6

//карты у противника
if(e.getKeyCode() == KeyEvent.VK_F7 && e.getID() == KeyEvent.KEY_PRESSED )
{
if (hands2==null)
{
JOptionPane.showMessageDialog(null,"","у противника нет карт",1);
return true;
}
else
{
JOptionPane.showMessageDialog(null,"",hands2.size()+" карт у противника",1);
}
}//f7

//счет игры
if(e.getKeyCode() == KeyEvent.VK_F8 && e.getID() == KeyEvent.KEY_PRESSED )
{
if (scores1==0&&scores2==0)
{
JOptionPane.showMessageDialog(null,"","счёт не открыт",1);
return true;
}
if (scores2>scores1)
{
JOptionPane.showMessageDialog(null,"",scores2+":"+scores1+" в пользу противника",1);
}
if (scores1>scores2)
{
JOptionPane.showMessageDialog(null,"",scores1+":"+scores2+" в вашу пользу",1);
}
if (scores1==scores2)
{
JOptionPane.showMessageDialog(null,"",scores1+":"+scores2+" ничья",1);
}
}//f8

return false;
}
});

//some actions while app is closing
addWindowListener(new WindowAdapter()
       {
@Override
            public void windowClosed(WindowEvent event)
{

}
});

//интерфейс
JPanel mainPanel = new JPanel();
mainPanel.setLayout(new BorderLayout());//говорим что основная панель будет 2 строки по одной колонки
        JPanel panelTree = new JPanel();
panelTree.setLayout(new GridLayout(1,2));
panelTree.add(new JScrollPane(tree1), BorderLayout.WEST);
panelTree.add(new JScrollPane(tree2), BorderLayout.EAST);
mainPanel.add(panelTree,BorderLayout.NORTH);
mainPanel.add(new JScrollPane(button), BorderLayout.SOUTH);
getContentPane().add(mainPanel);
        setPreferredSize(new Dimension(800, 600));
        pack();
        setLocationRelativeTo(null);
       setVisible(true);

//фокус на кнопку начать при старте
button.requestFocus();

//активация верхнего меню нажатием alt
setupMenuKey(this);

//запускаем игру, если параметр rightNow=true
if(apps.getRightNow())
startGame();

 }//конструктор
   
//класс для нажатия пункта меню Выход
class ExitAction extends AbstractAction
{
public ExitAction()
{
     putValue(NAME, "Выход");
}
   public void actionPerformed(ActionEvent e)
{
      System.exit(0);
}
}//cl

//класс для проверки обновлений
class CheckUpdates extends AbstractAction
{
public CheckUpdates()
{
     putValue(NAME, "Проверить обновления");
}
   public void actionPerformed(ActionEvent e)
{
      
Runnable task = () ->
{

String res = DownloadVersionFileToString("https://raw.github.com/RogenBenastra/Durak/main/Updates/version.xml");
if(res==null)
{
JOptionPane.showMessageDialog(null, "", "Не получилось загрузить version.xml. Попробуйте позднее.",1);
return;
}

int app_version_remote = Integer.parseInt(getInfoFromXML(res, "version").replace(".",""));
int app_version_local = Integer.parseInt(getVersionFromManifest().replace(".",""));
//String hash_remote_txt = getInfoFromXML( res, "hash");

if(app_version_local==app_version_remote)
{
JOptionPane.showMessageDialog(null, "", "У вас самая свежая версия! Обновление не требуется.",1);
return;
}

Boolean completed = downloadFile("https://raw.github.com/RogenBenastra/Durak/main/DN/Durak.jar");
if(!completed)
{
JOptionPane.showMessageDialog(null, "", "Не удалось загрузить обновление. Попробуйте позднее.",1);
return;
}

//String hash_downloaded_file = MakeHash.getHash("Durak.jar.tmp");
JOptionPane.showMessageDialog(null, "", "Обновление выполнено. Приложение будет перезагружено.",1);
try{
Desktop desktop = Desktop.getDesktop();
desktop.open(new File("Updater.jar"));
}catch(Exception ex){}

};//runnable
 Thread t1=new Thread (task);
t1.start();

}
}//cl

    public static void main(String[] args) {
 EventQueue.invokeLater(
new Runnable()
{
public void run()
{
            try {
Durak durak = new Durak();

               new Thread(new Runnable() {
                  public void run() {
                     try {
                        ServerSocket serverSocket = new ServerSocket(9090);
                        Socket socket = serverSocket.accept();

                        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        String request = in.readLine();
                        if (request.equals("close")) {
                           socket.close();
                           serverSocket.close();
                           System.exit(0);
                        }
                     } catch (Exception e) {
                        //e.printStackTrace();
                     }
                  }
               }).start();
           
} catch (Exception e) {
               //e.printStackTrace();
            }

}
});
}
}
//конец фильма