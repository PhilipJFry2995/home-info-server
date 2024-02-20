/**
Required libraries:
- Adafruit_Unified_Sensor
- ArduinoJson
- DHT_sensor_library
**/
#include <Arduino.h>
#include <WiFi.h>
#include <WebServer.h>
#include <ArduinoJson.h>
#include <Wire.h>
#include "DHT.h"

const char *SSID = ""; // TODO set wi-fi access point name
const char *PWD = ""; // TODO set password

#define DHTTYPE DHT22
#define DHTPIN 15

DHT dht(DHTPIN, DHTTYPE);

WebServer server(80);

StaticJsonDocument<250> jsonDocument;
char buffer[250];

float temperature;
float humidity;
 
void setup_routing() {     
  server.on("/climate", getClimate);
          
  server.begin();    
}
 
void create_json(String temperature, String humidity) {
  jsonDocument.clear();
  jsonDocument["temperature"] = temperature;
  jsonDocument["humidity"] = humidity;
  serializeJson(jsonDocument, buffer);
}

void getClimate() {
  Serial.println("Get climate");
  getDHTReadings();
  create_json(String(temperature), String(humidity));
  server.send(200, "application/json", buffer);
}

void getDHTReadings() {
  humidity = dht.readHumidity(); // + 19;
  humidity = roundf(humidity * 10) / 10;
  Serial.print("humidity:");
  Serial.println(humidity);
  temperature = dht.readTemperature(); // + 28.8;
  temperature = roundf(temperature * 10) / 10;
  Serial.print("temperature:");
  Serial.println(temperature);
}

void setup() {     
  Serial.begin(115200);  

  dht.begin(); 

  Serial.print("Connecting to Wi-Fi");
  WiFi.begin(SSID, PWD);
  while (WiFi.status() != WL_CONNECTED) {
    Serial.print(".");
    delay(500);
  }
  Serial.println("");
  Serial.print("Connected! IP Address: ");
  Serial.println(WiFi.localIP());
  setup_routing();   
}    
       
void loop() {    
  server.handleClient();     
}