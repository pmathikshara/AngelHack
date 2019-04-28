#include "mbed.h"
#include "GPS.h"
Serial pc(SERIAL_TX, SERIAL_RX);
GPS ark(PA_9, PA_10);
DigitalOut myled(LED1);
Serial device(PC_10, PC_11);  // tx, rx

int main()
{
    device.printf("AT+CWMODE=1\n\r");
            wait(0.3);
    device.printf("AT+CWJAP=\"L903\",\"laya2907\"\n\r");
            wait(0.3);
    device.printf("AT+CIPMUX=0\n\r");
            wait(0.3);
    device.printf("AT+CIPSTART=\"TCP\",\"184.106.153.149\",80\r\n");
            wait(0.3);
    
    while(1) {
        if( ark.sample() == 1) {
            myled=0;
            float latitude = ark.latitude;
            float longitude = ark.longitude;
            float utc = ark.utc+50000;
            pc.printf("latitude: %0.2f, longitude: %0.2f, utc: %f\r\n",latitude,longitude,utc);
            device.printf("AT+CIPSEND=49\r\n");
            wait(0.3);
            device.printf("GET /update?key=UQECY67419G56BNR&field1=%.2f\r\n",longitude);
            wait(0.3);
            wait(1);
        } else {
            myled=1;
        }
    }
}