#include <SoftwareSerial.h>
#include <math.h>
#include<Wire.h>

// Bluetooth
SoftwareSerial BTserial(0, 1); // RX | TX

// Flex Sensor
const float VCC = 5.0; // Measured voltage of Ardunio 5V line 4.98 daw
const float R_DIV = 15000.0; //  15k ohms  
const float DIV_4 = 1023.0; //?
const float DIV_2 = 512.0; //?

// Multiplexer
const int selectPins[3] = {11, 12, 13}; // S0~2, S1~3, S2~4
const int zOutput = 5;
const int zInput = A0; // Connect common (Z) to A0 (analog input)

// MPU6050
const int MPU6050_addr=0x68;
int16_t AccX,AccY,AccZ,Temp,GyroX,GyroY,GyroZ;

// FSR
const int fsrAnalogPin = A3; // FSR is connected to analog 3
bool fsrEnabled = true;
int timeDelay = 1000;

//Char used for reading in Serial characters
char inbyte;

void setup() 
{
    // Multiplexer
    for (int i = 0; i < 3; i++)
    {
      pinMode(selectPins[i], OUTPUT);
      digitalWrite(selectPins[i], HIGH);
    }
    pinMode(zInput, INPUT); // Set up Z as an input
    pinMode(fsrAnalogPin, INPUT); // Set up fsr as input

    // Bluetooth
    Wire.begin();
    Wire.beginTransmission(MPU6050_addr);
    Wire.write(0x6B);
    Wire.write(0);
    Wire.endTransmission(true);
    Serial.begin(9600); 
    BTserial.begin(9600); 
}
 
void loop()
{

  if (BTserial.available()){
    inbyte = BTserial.read();
    if (inbyte == '0'){
      fsrEnabled = true;
      timeDelay = 1000;
    }
    else if (inbyte == '1'){
      fsrEnabled = false;
      timeDelay = 1000;
    }
    else if (inbyte == '2'){
      fsrEnabled = false;
      timeDelay = 2000;
    }
    else if (inbyte == '3'){
      fsrEnabled = false;
      timeDelay = 3000;
    }
    else {
      
    }
  }
  
  // Flex Sensor
  float flex[7];
  // Loop through five pins that are used (Y0 - Y4)
  for (byte pin = 0; pin <= 4; pin++)
  {
    if (pin == 0 && pin == 4)
    {
      selectMuxPin(pin); // Select one at a time
      int inputValue = analogRead(A0); // and read Z
      float FlexVoltage = inputValue * VCC / DIV_2;
      float FlexResistance = R_DIV * (VCC / FlexVoltage - 1.0);
      flex[pin] = FlexResistance;
    }
    else
    {
      selectMuxPin(pin); // Select one at a time
      int inputValue = analogRead(A0); // and read Z
      float FlexVoltage = inputValue * VCC / DIV_4;
      float FlexResistance = R_DIV * (VCC / FlexVoltage - 1.0);
      flex[pin] = FlexResistance;
    }
  } 

  // MPU6050
  Wire.beginTransmission(MPU6050_addr);
  Wire.write(0x3B);
  Wire.endTransmission(false);
  Wire.requestFrom(MPU6050_addr,14,true);
  AccX=Wire.read()<<8|Wire.read();
  AccY=Wire.read()<<8|Wire.read();
  AccZ=Wire.read()<<8|Wire.read();
  Temp=Wire.read()<<8|Wire.read();
  GyroX=Wire.read()<<8|Wire.read();
  GyroY=Wire.read()<<8|Wire.read();
  GyroZ=Wire.read()<<8|Wire.read();
  
  // FSR
  int fsr = analogRead(fsrAnalogPin);

  if(fsrEnabled = true){
    if(fsr > 100){ //if fsr is pressed
      // Send all the data 
      Serial.println("#" +
                    String(flex[0]) + "," + 
                    String(flex[1]) + "," + 
                    String(flex[2]) + "," + 
                    String(flex[3]) + "," + 
                    String(flex[4]) + "," +
                    String(GyroX) + "," + 
                    String(GyroY) + "," + 
                    String(GyroZ) + "," + 
                    String(AccX) + "," + 
                    String(AccY) + "," + 
                    String(AccZ) + "~");
                    
      BTserial.println("#" +
                    String(flex[0]) + "," + 
                    String(flex[1]) + "," + 
                    String(flex[2]) + "," + 
                    String(flex[3]) + "," + 
                    String(flex[4]) + "," +
                    String(GyroX) + "," + 
                    String(GyroY) + "," + 
                    String(GyroZ) + "," + 
                    String(AccX) + "," + 
                    String(AccY) + "," + 
                    String(AccZ) + "~");
    }
  }
  else{
    Serial.println("#" +
                    String(flex[0]) + "," + 
                    String(flex[1]) + "," + 
                    String(flex[2]) + "," + 
                    String(flex[3]) + "," + 
                    String(flex[4]) + "," +
                    String(GyroX) + "," + 
                    String(GyroY) + "," + 
                    String(GyroZ) + "," + 
                    String(AccX) + "," + 
                    String(AccY) + "," + 
                    String(AccZ) + "~");
                    
      BTserial.println("#" +
                    String(flex[0]) + "," + 
                    String(flex[1]) + "," + 
                    String(flex[2]) + "," + 
                    String(flex[3]) + "," + 
                    String(flex[4]) + "," +
                    String(GyroX) + "," + 
                    String(GyroY) + "," + 
                    String(GyroZ) + "," + 
                    String(AccX) + "," + 
                    String(AccY) + "," + 
                    String(AccZ) + "~");
  }

  delay(timeDelay);
}


// The selectMuxPin function sets the S0, S1, and S2 pins
// accordingly, given a pin from 0-7.
void selectMuxPin(byte pin)
{
  for (int i = 0; i < 3; i++)
  {
    if (pin & (1 << i))
      digitalWrite(selectPins[i], HIGH);
    else
      digitalWrite(selectPins[i], LOW);
  }
}
