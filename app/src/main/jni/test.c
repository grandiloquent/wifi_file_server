// qrcode.c
#include <stdio.h>
#include <stdint.h>
#include "qrcode.h"

int main(){
    QRCode  qrcode;
    uint8_t qrCodeBytes[qrcode_getBufferSize(3)];
    qrcode_initText(&qrcode, qrCodeBytes, 3, ECC_LOW, "123");
    for (uint8_t y = 0; y < qrcode.size; y++) {
        for (uint8_t x = 0; x < qrcode.size; x++) {
            if (qrcode_getModule(&qrcode, x, y)) {
               printf("%s","1,");
            } else {
                printf("%s","0,");
            }
        }
    }
}