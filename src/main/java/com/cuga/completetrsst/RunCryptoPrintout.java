package com.cuga.completetrsst;

import org.joda.time.LocalTime;

public class RunCryptoPrintout {
  public static void main(String[] args) {
    LocalTime currentTime = new LocalTime();
    System.out.println("The current local time is: " + currentTime);

    ShowCryptoUse greeter = new ShowCryptoUse();
    greeter.showKeys();
  }
}