int doorSwitch = 7;
int plateSwitch = 2;

int prevSwitch = 0;

void setup() {
  pinMode(doorSwitch, INPUT);
  pinMode(plateSwitch, INPUT);
  Serial.begin(9600);
}

void loop() {
  
  digitalWrite(doorSwitch, HIGH);
  digitalWrite(plateSwitch, HIGH);

  if (digitalRead(doorSwitch) == LOW && prevSwitch != doorSwitch) {
      Serial.print(doorSwitch);
      prevSwitch = doorSwitch;
  }

  if (digitalRead(plateSwitch) == LOW && prevSwitch != plateSwitch) {
      Serial.print(plateSwitch);
      prevSwitch = plateSwitch;
  }
}
