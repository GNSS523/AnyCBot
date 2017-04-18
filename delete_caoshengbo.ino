#define IN1 2
#define IN2 3
#define IN3 4
#define IN4 8
#define ENA 5
#define ENB 6
void forward();
void back();
void turnLeft();
void turnRight();
void orin();
void _stop();
void hold();
void setup()
{ 
  pinMode(IN1,OUTPUT);
  pinMode(IN2,OUTPUT);
  pinMode(IN3,OUTPUT);
  pinMode(IN4,OUTPUT);
  pinMode(ENA,OUTPUT);
  pinMode(ENB,OUTPUT);
    pinMode(9,OUTPUT);
  setspeed();
  _stop();
  Serial.begin(9600);
}
int s=0,m;
int dl=200;
void loop()
{
  if(Serial.available())
  {  
    m= Serial.read();  
    switch(m)
    { 
      case'l':{ turnLeft();break;}
      case'f':{forward();break;}
      case'r':{turnRight();break;}
      case'b':{back();break;}
      case'z':{orin();break;}
    }
    while(Serial.read() >= 0){};
   if (m!='u'||'d'||'x'||'y')
   {
    delay(dl);
   }
  } 
  if(!Serial.available())
  {
     _stop();
  }
}
void forward()
{ 
  digitalWrite(IN1,HIGH);
  digitalWrite(IN2,LOW);
  digitalWrite(IN3,HIGH);
  digitalWrite(IN4,LOW);
}
void back()
{ 
  digitalWrite(IN1,LOW);
  digitalWrite(IN2,HIGH);
  digitalWrite(IN3,LOW);
  digitalWrite(IN4,HIGH);                                                                                                                                
}
void turnLeft()
{
  digitalWrite(IN1,HIGH);
  digitalWrite(IN2,LOW);
  digitalWrite(IN3,LOW);
  digitalWrite(IN4,LOW);
}
void turnRight()
{ 
  digitalWrite(IN1,LOW);
  digitalWrite(IN2,LOW);
  digitalWrite(IN3,HIGH);
  digitalWrite(IN4,LOW);
}
void orin()
{
  digitalWrite(IN1,HIGH);
  digitalWrite(IN2,LOW);
  digitalWrite(IN3,LOW);
  digitalWrite(IN4,HIGH);
  }
void _stop()
{ 
  digitalWrite(IN1,LOW);
  digitalWrite(IN2,LOW);
  digitalWrite(IN3,LOW);
  digitalWrite(IN4,LOW);
}
void setspeed()
{
  analogWrite(ENA,128);
  analogWrite(ENB,128);
  }

