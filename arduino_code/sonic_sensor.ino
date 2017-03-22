const int anPin = 0;
long anVolt, mm, inches, wait; 

void setup() { 
  Serial.begin(9600); 
} 

void read_sensor(){ 
  anVolt = analogRead(anPin); 
  mm = anVolt * 5; 
  inches = mm/25.4; 
} 

void print_range(){ 
  Serial.print("S1"); 
  Serial.print("="); 
  Serial.print(" "); 
  Serial.println(inches); 
} 

void loop() { 
  char c;
  if(Serial.available())  
  {  
    c = Serial.read();  
    Serial.print(c);  
  }  
  if(wait == 200000){
  read_sensor(); 
  print_range();
  wait = 0; 
  } 
  wait = wait + 1;
 // Serial.println(wait);
}
