#include <SoftwareSerial.h>
#include <math.h>
#include<Wire.h>

SoftwareSerial BTserial(0, 1); // RX | TX
// Connect the HC-05 TX to Arduino pin 2 RX. 
// Connect the HC-05 RX to Arduino pin 3 TX through a voltage divider.

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
    
    //Serial.print(String(FlexResistance0) + " ohms\t");   
    //Serial.print(String(inputValue) + "\t");
  }
  Serial.print("=======================\n");
  Serial.print("F1: " + String(flex[0]) + " ohms\n");
  Serial.print("F2: " + String(flex[1]) + " ohms\n");
  Serial.print("F3: " + String(flex[2]) + " ohms\n");
  Serial.print("F4: " + String(flex[3]) + " ohms\n");
  Serial.print("F5: " + String(flex[4]) + " ohms\n");

  BTserial.print("=======================\n");
  BTserial.print("F1: " + String(flex[0]) + " ohms\n");
  BTserial.print("F2: " + String(flex[1]) + " ohms\n");
  BTserial.print("F3: " + String(flex[2]) + " ohms\n");
  BTserial.print("F4: " + String(flex[3]) + " ohms\n");
  BTserial.print("F5: " + String(flex[4]) + " ohms\n");
  

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

  Serial.print("AccX = " + String(AccX) + "\n");
  Serial.print("AccY = " + String(AccY) + "\n");
  Serial.print("AccZ = " + String(AccZ) + "\n");
  Serial.print("Temp = " + String(Temp/340.00+36.53) + "\n");
  Serial.print("GyroX = " + String(GyroX) + "\n");
  Serial.print("GyroY = " + String(GyroY) + "\n");
  Serial.print("GyroZ = " + String(GyroZ) + "\n");


  BTserial.print("AccX = " + String(AccX) + "\n");
  BTserial.print("AccY = " + String(AccY) + "\n");
  BTserial.print("AccZ = " + String(AccZ) + "\n");
  BTserial.print("Temp = " + String(Temp/340.00+36.53) + "\n");
  BTserial.print("GyroX = " + String(GyroX) + "\n");
  BTserial.print("GyroY = " + String(GyroY) + "\n");
  BTserial.print("GyroZ = " + String(GyroZ) + "\n");
  
  // FSR
  int fsrReading = analogRead(fsrAnalogPin);

  Serial.print("Force = " + String(fsrReading) + "\n");
  BTserial.print("Force = " + String(fsrReading) + "\n");

  delay(5000);
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
