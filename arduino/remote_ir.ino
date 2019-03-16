#include <Arduino.h>
#include <IRremote.h>

//Portta Switcher 2 ports
#define KEY_2_1 0xFFBA45
#define KEY_2_2 0xFF38C7

//Portta Switcher 4 ports
#define KEY_4_1 0x1FE40BF
#define KEY_4_2 0x1FE20DF
#define KEY_4_3 0x1FEA05F
#define KEY_4_4 0x1FE609F

#define KEYS_NUM 6 //damn C

IRsend irsend;

unsigned long keys[] = { KEY_2_1, KEY_2_2, KEY_4_1, KEY_4_2, KEY_4_3, KEY_4_4 };
int keyToSend = 0; // key read from serial to send
String inString = "";

void setup() {
	Serial.begin(115200);
}

void loop() {
	if (Serial.available() > 0) {
		int inChar = Serial.read();
		if (isDigit(inChar)) {
			inString += (char)inChar;
		}
		if (inChar == '\n') {
			keyToSend = inString.toInt() - 1;

			if (keyToSend < 0 || keyToSend >= KEYS_NUM) {
				Serial.print("Invalid key ");
				Serial.println(inString);
			} else {
				sendIR(keys[keyToSend]);
				Serial.print(inString);
				Serial.println(" sent");
			}

			inString = "";
		}
	}
}

void sendIR(unsigned long key) {
	for (int i = 0; i < 3; i++) {
		irsend.sendNEC(key, 32);
		delay(40);
	}
}
