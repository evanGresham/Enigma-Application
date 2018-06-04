import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class enigmaDraft extends PApplet {

Light[] letters = new Light[26];
String letterOrder =          "QWERTYUIOPASDFGHJKLZXCVBNM";
String letterOrderLowerCase = "qwertyuiopasdfghjklzxcvbnm";
PImage lightOnSprite;
boolean keyIsDown = false;
char keyDown;
char keyLight;
PImage blackground;

Enigma enigma;
public void setup() {
  frameRate(30);
  
  blackground = loadImage("blacktexture.jpg");
  for (int i = 0; i< letters.length; i++) {
    letters[i] = new Light(letterOrder.charAt(i), i);
  }
  lightOnSprite = loadImage("light.png");

  enigma = new Enigma();
  enigma.setRotors(3,2,1);
  enigma.randomPositions();
}



public void draw() {
  background(0);
  imageMode(CORNER);
  image(blackground,0,0, width,height);
  enigma.show();
}

public void mousePressed() {
  enigma.click(mouseX,mouseY);
}


public void keyPressed() {
  if (letterOrderLowerCase.indexOf(key) != -1 && !keyIsDown && !enigma.showPlugs) {
    char output = enigma.runMachine(key);
    if(output == '1'){
      return;
      
    }
    keyLight = output;
    letters[letterOrderLowerCase.indexOf(output)].lightUp = true;
    keyIsDown = true;
    keyDown = key;
  }
}


public void keyReleased() {
  if (letterOrderLowerCase.indexOf(key) != -1 && key == keyDown) {
    letters[letterOrderLowerCase.indexOf(keyLight)].lightUp = false;
    keyIsDown = false;
  }
}
class EndThing {
  int[][] wiring;


  EndThing() {
    wiring = new int[][] {{0, 21}, {1, 10}, {2, 22}, {3, 17}, {4, 6}, {5, 8}, {6, 4}, {7, 19}, {8, 5}, {9, 25}, {10, 1}, {11, 20}, {12, 18}, {13, 15}, {14, 16}, {15, 13}, {16, 14}, {17, 3}, {18, 12}, {19, 7}, {20, 11}, {21, 0}, {22, 2}, {23, 24}, {24, 23}, {25, 9}};
  }


  public int runThrough(int input, boolean forward) {

    input = (input) % 26;
    if (forward) {
      return wiring[input][1];
    } else {
      return wiring[input][0];
    }




    //    for (int i = 0; i< 26; i++) {
    //      if (forward) {
    //        if (input == wiring[i][0]) {
    //          return wiring[i][1];
    //        }
    //      } else {
    //        if (input == wiring[i][1]) {
    //          return wiring[i][0];
    //        }
    //      }
    //    }

    //    return -1;
  }
}
class Enigma {
  Rotor rotor1;
  Rotor rotor2;
  Rotor rotor3;
  EndThing end;
  PlugBoard plugBoard;
  boolean showPlugs = false;

  //---------------------------------------------------------------------------------------------------------------------------------------------------------------
  Enigma() {
    end = new EndThing();
    plugBoard = new PlugBoard();
  }
  //---------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void setRotors(int first, int second, int third) {

    if (first != second && first != third && second != third) {
      rotor1 = new Rotor(first, 1);
      rotor2 = new Rotor(second, 2);
      rotor3 = new Rotor(third, 3);
    }
  }
  //---------------------------------------------------------------------------------------------------------------------------------------------------------------


  public void setRotorPositions(int first, int second, int third) {
    rotor1.position = first;
    rotor2.position = second;
    rotor3.position = third;
  }
  //---------------------------------------------------------------------------------------------------------------------------------------------------------------

  public char runMachine(char inputChar) {
    if (rotor1.rotorNo == rotor2.rotorNo || rotor3.rotorNo == rotor2.rotorNo  || rotor1.rotorNo == rotor3.rotorNo ) {
      println("Error rotors cannot have the same number"); 
      return '1';
    }
    int inputNo = letterOrderLowerCase.indexOf(inputChar); 

    int currentNo = inputNo;
    currentNo = plugBoard.runThrough(currentNo);
    currentNo = rotor1.runThrough(currentNo, true);
    currentNo = rotor2.runThrough(currentNo, true);
    currentNo = rotor3.runThrough(currentNo, true);
    currentNo = end.runThrough(currentNo, true);
    currentNo = rotor3.runThrough(currentNo, false);
    currentNo = rotor2.runThrough(currentNo, false);
    currentNo = rotor1.runThrough(currentNo, false);
    currentNo = plugBoard.runThrough(currentNo);
    if (currentNo == -1) {
      println(rotor1.position, rotor2.position, rotor3.position);
    }

    if (currentNo == inputNo) {
      println(inputNo, rotor1.position, rotor2.position, rotor3.position);
    }
    moveRotors();

    return letterOrderLowerCase.charAt(currentNo);
  }

  //---------------------------------------------------------------------------------------------------------------------------------------------------------------


  public void moveRotors() {
    rotor1.position +=1;
    if (rotor1.position == 26) {
      rotor1.position = 0;
      rotor2.position+=1;
      if (rotor2.position == 26) {
        rotor2.position = 0;
        rotor3.position+=1;
        if (rotor3.position == 26) {
          rotor3.position = 0;
        }
      }
    }
  }

  //---------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void show() {
    if (!showPlugs) {
      stroke(0);
      for (int i = 0; i< letters.length; i++) {
        letters[i].show();
      }
      rotor1.show();
      rotor2.show();
      rotor3.show();
      if (rotor1.rotorNo == rotor2.rotorNo || rotor3.rotorNo == rotor2.rotorNo  || rotor1.rotorNo == rotor3.rotorNo ) {
        fill(255,0,0);
        text("Cannot use the same rotor twice", width/2,50);
      }
    } else {
      plugBoard.show();
    }
  }

  //-----------------------------------------------------------------------------------------------------------------------------------------------------------------
  public void randomRotors() {
    int rand1 = floor(random(5));
    int rand2 = floor(random(5));
    while (rand1 == rand2) {
      rand2 = floor(random(5));
    }

    int rand3 = floor(random(5));
    while (rand1 == rand3 || rand2 == rand3) {
      rand3 = floor(random(5));
    }
    setRotors(rand1, rand2, rand3);
  }


  public void randomPositions() {
    setRotorPositions(floor(random(26)), floor(random(26)), floor(random(26)));
  }

  //--------------------------------------------------------------------------------------------------------------------------------------------------

  public void click(int x, int y) {
    if (y > height*(9.0f/10.0f) && !enigma.plugBoard.movingPlug) {//if clicking the bottom of the screen then switch between plugs anad lamps
      enigma.showPlugs = !enigma.showPlugs;
    } else {

      enigma.rotor1.click(x, y);
      enigma.rotor2.click(x, y);
      enigma.rotor3.click(x, y);
      enigma.plugBoard.click(x, y);
    }
  }
}
class Light {
  char letter;
  int number;
  PVector pos;
  boolean lightUp  = false;

  Light(char let, int numb) {
    letter = let;
    number = numb;
    int level;
    int rowPos;
    float x;
    float y;
    if (numb < 10) {
      level = 1;
      rowPos = numb;
      x = (rowPos+1.0f)*width/11;
    } else if (numb < 19) {
      level = 2;
      rowPos = numb - 10;
      x = (rowPos+1.5f)*width/11;
    } else {

      level = 3;
      rowPos = numb - 19;
      x = (rowPos+2.0f)*width/11;
    }
    y  = height/3 + level*(height*2/3)/4;
    pos = new PVector(x, y);
  }





  public void show() {
    if (lightUp) {
      imageMode(CENTER);
      image(lightOnSprite, pos.x, pos.y);
      fill(200,100,0);
    } else {
      strokeWeight(5);
      fill(150);
      ellipse(pos.x, pos.y, 80, 80);
      fill(50);
    }
    textAlign(CENTER, CENTER);
    textSize(20);
    text(letter, pos.x, pos.y);
  }
}
class Plug {
  int connection1;
  int connection2;
  PlugPoint point1;
  PlugPoint point2;
  boolean move1 = false;
  boolean move2 = false;

  Plug(int c1, int c2, PlugPoint p1, PlugPoint p2 ) {
    connection1 = c1; 
    connection2 = c2;  
    point1 = p1;
    point2 = p2;
  }



  public void showLines() {

    stroke(100, 100, 200, 150);
    strokeWeight(3);

    if (move1) {
      line(mouseX, mouseY, point2.pos.x, point2.pos.y +15);
    } else if (move2) {
      line(point1.pos.x, point1.pos.y +15, mouseX, mouseY);
    } else {
      line(point1.pos.x, point1.pos.y +15, point2.pos.x, point2.pos.y +15);
    }
  }


  public void showPlugs() {
    stroke(200);
    fill(40);
    rectMode(CENTER);

    if (move1) {
      rect(mouseX, mouseY, 30, 70); 

      rect(point2.pos.x, point2.pos.y + 15, 30, 70);
    } else if (move2) {
      rect(point1.pos.x, point1.pos.y + 15, 30, 70); 
      rect(mouseX, mouseY, 30, 70);
    } else {
      rect(point1.pos.x, point1.pos.y + 15, 30, 70); 

      rect(point2.pos.x, point2.pos.y + 15, 30, 70);     
      fill(255);
      textSize(10);
      text(point2.letter, point1.pos.x, point1.pos.y + 15);
      text(point1.letter, point2.pos.x, point2.pos.y + 15);
    }
  }


  public boolean click(int x, int y) {
    if (x < point1.pos.x +15 && x > point1.pos.x - 15 && y < point1.pos.y +50 && y > point1.pos.y - 20) {
      move1 = true;
      point1.occupied = false;
      return true;
    } else if (x < point2.pos.x +15 && x > point2.pos.x - 15 && y < point2.pos.y +50 && y > point2.pos.y - 20) {
      move2 = true;
      point2.occupied = false;

      return true;
    }
    return false;
  }

  public void setPlugPoint(int plugPointNo, PlugPoint newPoint, int connectionNo) {
    newPoint.occupied = true;
    switch(connectionNo) {
      case(1):
      point1 = newPoint;
      connection1 = plugPointNo;
      break;
      case(2):
      point2 = newPoint;
      connection2 = plugPointNo;
      break;
    }
  }
}
class PlugBoard {
  Plug[] plugs = new Plug[10];
  PlugPoint[] plugPoints = new PlugPoint[26];
  boolean showing = false;
  boolean movingPlug = false;
  int movingPlugNo =0;

  PlugBoard() {
    for (int i = 0; i< plugPoints.length; i++) {
      plugPoints[i] = new PlugPoint(i);
    }
    randomisePlugs();
  }


  public void randomisePlugs() {
    ArrayList<Integer> chosen = new  ArrayList<Integer>();
    for (int i = 0; i< 10; i++) {
      int rand1 = floor(random(26));
      while (chosen.contains(rand1)) {
        rand1 = floor(random(26));
      }
      chosen.add(rand1);
      int rand2 = floor(random(26));
      while (chosen.contains(rand2)) {
        rand2 = floor(random(26));
      }
      chosen.add(rand2);
      plugs[i] = new Plug(rand1, rand2, plugPoints[rand1], plugPoints[rand2] );
      plugPoints[rand1].occupied = true;
      plugPoints[rand2].occupied = true;
    }
  }

  public void show() {
    for (int i = 0; i< 26; i++) {
      plugPoints[i].show();
    }

    for (int i= 0; i< plugs.length; i++) {
      plugs[i].showPlugs();
    }

    for (int i= 0; i< plugs.length; i++) {
      plugs[i].showLines();
    }
  }

  public int runThrough(int input) {
    for (int  i = 0; i< plugs.length; i++) {
      if (plugs[i].connection1 == input) {
        return plugs[i].connection2;
      } else if (plugs[i].connection2 == input) {
        return plugs[i].connection1;
      }
    }

    return input;//if no plugs on that letter then just return the input
  }



  public void click(int x, int y) {
    if (!movingPlug) {
      for (int i = 0; i< plugs.length; i++) {
        if (plugs[i].click(x, y)) {
          movingPlug = true;
          movingPlugNo = i;
          return;
        }
      }
    } else {
      for (int i = 0; i< plugPoints.length; i++) {
        if (plugPoints[i].click(x, y)) {
          if (!plugPoints[i].occupied) {
            movingPlug = false;
            if (plugs[movingPlugNo].move1) {
              plugs[movingPlugNo].setPlugPoint(i, plugPoints[i], 1);
              plugs[movingPlugNo].move1 = false;
            } else {
              plugs[movingPlugNo].setPlugPoint(i, plugPoints[i], 2);
              plugs[movingPlugNo].move2 = false;
            }
          }
          return;
        }
      }
    }
  }
}
class PlugPoint {
  PVector pos = new PVector();
  char letter;
  int letterNo;
  boolean occupied = false;
  PlugPoint(int no) {
    letterNo = no;
    letter = letterOrder.charAt(no);
    int level;
    int rowPos;
    float x;
    float y;
    if (no < 10) {
      level = 1;
      rowPos = no;
      x = (rowPos+1.0f)*width/11;
    } else if (no < 19) {
      level = 2;
      rowPos = no - 10;
      x = (rowPos+1.5f)*width/11;
    } else {

      level = 3;
      rowPos = no - 19;
      x = (rowPos+2.0f)*width/11;
    }
    y  = height/3 + level*(height*2/3)/4;
    if(no%3 ==0){
     y += 15; 
      
    }
    pos = new PVector(x, y);
  }


  public void show() {
    textAlign(CENTER,CENTER);
    textSize(20);
    fill(255);
    text(letter, pos.x, pos.y-40);
    fill(20);
    stroke(255);
    
    ellipse(pos.x, pos.y, 20, 20);
    ellipse(pos.x, pos.y+30, 20, 20);
  }
  
  public boolean click(int x, int y){
    if (x < pos.x +15 && x > pos.x - 15 && y < pos.y +35 && y > pos.y - 35) {
      return true;
    }
    return false;    
  }
}
class Rotor {
  int[][] wiring;
  int position = 0;
  int rotorNo;
  int rotorPos;

  Rotor(int rotorNumber, int rotorPosition) {
    rotorNo = rotorNumber;
    rotorPos = rotorPosition;
    switch(rotorNo) {
    case 0:
      wiring = new int[][] {{0, 15 }, {1, 4 }, {2, 25 }, {3, 20 }, {4, 14 }, {5, 7 }, {6, 23 }, {7, 18 }, {8, 2 }, {9, 21 }, {10, 5 }, {11, 12 }, {12, 19 }, {13, 1 }, {14, 6 }, {15, 11 }, {16, 17 }, {17, 8 }, {18, 13 }, {19, 16 }, {20, 9 }, {21, 22 }, {22, 0 }, {23, 24 }, {24, 3 }, {25, 10 }};  
      break;
    case 1:
      wiring = new int[][] {{0, 25 }, {1, 14 }, {2, 20 }, {3, 4 }, {4, 18 }, {5, 24 }, {6, 3 }, {7, 10 }, {8, 5 }, {9, 22 }, {10, 15 }, {11, 2 }, {12, 8 }, {13, 16 }, {14, 23 }, {15, 7 }, {16, 12 }, {17, 21 }, {18, 1 }, {19, 11 }, {20, 6 }, {21, 13 }, {22, 9 }, {23, 17 }, {24, 0 }, {25, 19 }};
      break;
    case 2:
      wiring = new int[][] {{0, 4 }, {1, 7 }, {2, 17 }, {3, 21 }, {4, 23 }, {5, 6 }, {6, 0 }, {7, 14 }, {8, 1 }, {9, 16 }, {10, 20 }, {11, 18 }, {12, 8 }, {13, 12 }, {14, 25 }, {15, 5 }, {16, 11 }, {17, 24 }, {18, 13 }, {19, 22 }, {20, 10 }, {21, 19 }, {22, 15 }, {23, 3 }, {24, 9 }, {25, 2 }};
      break;
    case 3:
      wiring = new int[][] {{0, 8 }, {1, 12 }, {2, 4 }, {3, 19 }, {4, 2 }, {5, 6 }, {6, 5 }, {7, 17 }, {8, 0 }, {9, 24 }, {10, 18 }, {11, 16 }, {12, 1 }, {13, 25 }, {14, 23 }, {15, 22 }, {16, 11 }, {17, 7 }, {18, 10 }, {19, 3 }, {20, 21 }, {21, 20 }, {22, 15 }, {23, 14 }, {24, 9 }, {25, 13 }};
      break;
    case 4:
      wiring = new int[][] {{0, 16 }, {1, 22 }, {2, 4 }, {3, 17 }, {4, 19 }, {5, 25 }, {6, 20 }, {7, 8 }, {8, 14 }, {9, 0 }, {10, 18 }, {11, 3 }, {12, 5 }, {13, 6 }, {14, 7 }, {15, 9 }, {16, 10 }, {17, 15 }, {18, 24 }, {19, 23 }, {20, 2 }, {21, 21 }, {22, 1 }, {23, 13 }, {24, 12 }, {25, 11 }};
      break;
    }
  }

  public int runThrough(int input, boolean forward) {

    if (forward) {
      input = (input+position) % 26;

      return wiring[input][1];
    } else {
      for (int i = 0; i< 26; i++) {
        if (input == wiring[i][1]) {
          int output = (wiring[i][0]-position);
          while (output<0) {
            output = 26+output;
          }
          output = output % 26;

          return output;
        }
      }
    }




    //for (int i = 0; i< 26; i++) {
    //  if (forward) {
    //    if (input == wiring[i][0]) {
    //      return wiring[i][1];
    //    }
    //  } else {
    //    if (input == wiring[i][1]) {
    //      return wiring[i][0];
    //    }
    //  }
    //}

    return -1;
  }


  public void show() {
    int x = width/2 - ((rotorPos-2)*200);
    rectMode(CENTER);
    fill(255);
    rect(x, 200, 50, 120);
    fill(230);
    rect(x, 160, 50, 40);
    rect(x, 240, 50, 40);
    fill(0);
    textSize(20);
    if (position == 0) {
      text(1, x, 160);
      text(26, x, 200);
      text(25, x, 240);
    } else if (position == 1) {
      text(position+1, x, 160);
      text(position, x, 200);
      text((26), x, 240);
    } else {
      text(position+1, x, 160);
      text(position, x, 200);
      text((position-1), x, 240);
    }
    fill(255);
    textSize(30);
    text(rotorNo+1, x, 100);
  }

  public void click(int x, int y) {
    int posX = width/2 - ((rotorPos-2)*200);
    if (x < posX + 25 && x > posX - 25 && y >160 && y < 240) {
      position +=1;
      position = position % 26;
    } else if (x < posX + 25 && x > posX - 25 && y >70 && y < 130) {
      nextRotor();
    }
  }

  public void nextRotor() {
    rotorNo = (rotorNo + 1)%5;
    switch(rotorNo) {
    case 0:
      wiring = new int[][] {{0, 15 }, {1, 4 }, {2, 25 }, {3, 20 }, {4, 14 }, {5, 7 }, {6, 23 }, {7, 18 }, {8, 2 }, {9, 21 }, {10, 5 }, {11, 12 }, {12, 19 }, {13, 1 }, {14, 6 }, {15, 11 }, {16, 17 }, {17, 8 }, {18, 13 }, {19, 16 }, {20, 9 }, {21, 22 }, {22, 0 }, {23, 24 }, {24, 3 }, {25, 10 }};  
      break;
    case 1:
      wiring = new int[][] {{0, 25 }, {1, 14 }, {2, 20 }, {3, 4 }, {4, 18 }, {5, 24 }, {6, 3 }, {7, 10 }, {8, 5 }, {9, 22 }, {10, 15 }, {11, 2 }, {12, 8 }, {13, 16 }, {14, 23 }, {15, 7 }, {16, 12 }, {17, 21 }, {18, 1 }, {19, 11 }, {20, 6 }, {21, 13 }, {22, 9 }, {23, 17 }, {24, 0 }, {25, 19 }};
      break;
    case 2:
      wiring = new int[][] {{0, 4 }, {1, 7 }, {2, 17 }, {3, 21 }, {4, 23 }, {5, 6 }, {6, 0 }, {7, 14 }, {8, 1 }, {9, 16 }, {10, 20 }, {11, 18 }, {12, 8 }, {13, 12 }, {14, 25 }, {15, 5 }, {16, 11 }, {17, 24 }, {18, 13 }, {19, 22 }, {20, 10 }, {21, 19 }, {22, 15 }, {23, 3 }, {24, 9 }, {25, 2 }};
      break;
    case 3:
      wiring = new int[][] {{0, 8 }, {1, 12 }, {2, 4 }, {3, 19 }, {4, 2 }, {5, 6 }, {6, 5 }, {7, 17 }, {8, 0 }, {9, 24 }, {10, 18 }, {11, 16 }, {12, 1 }, {13, 25 }, {14, 23 }, {15, 22 }, {16, 11 }, {17, 7 }, {18, 10 }, {19, 3 }, {20, 21 }, {21, 20 }, {22, 15 }, {23, 14 }, {24, 9 }, {25, 13 }};
      break;
    case 4:
      wiring = new int[][] {{0, 16 }, {1, 22 }, {2, 4 }, {3, 17 }, {4, 19 }, {5, 25 }, {6, 20 }, {7, 8 }, {8, 14 }, {9, 0 }, {10, 18 }, {11, 3 }, {12, 5 }, {13, 6 }, {14, 7 }, {15, 9 }, {16, 10 }, {17, 15 }, {18, 24 }, {19, 23 }, {20, 2 }, {21, 21 }, {22, 1 }, {23, 13 }, {24, 12 }, {25, 11 }};
      break; 
    }
  }
}
  public void settings() {  fullScreen(); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "--present", "--window-color=#666666", "--hide-stop", "enigmaDraft" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
