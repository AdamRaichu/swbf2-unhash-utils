package io.github.adamraichu.bf2unhasher;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class TestHashesFromFile {
  public static void main(String[] args) {
    ArrayList<String> output = new ArrayList<>();

    try {
      File file = new File(Config.HASH_LIST_PATH);
      Scanner scanner = new Scanner(file);
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
        int hash = (int) Long.parseLong(line.toLowerCase().substring(2), 16);
        Main.Result unhashed = Main.unhashHash(hash, getIgnoreList());
        if (unhashed.getFound().get()) {
          String resultString = unhashed.getFoundString().get();
          System.out.println("[INFO]: Found string: " + resultString);
          output.add(resultString);
        } else {
          System.out.println("[WARN]: No string found for the given hash.");
          output.add("0x" + Utils.integerToHex(hash));
        }
      }
      scanner.close();
    } catch (FileNotFoundException e) {
      System.out.println("[ERROR]: File not found: " + Config.HASH_LIST_PATH);
    }

    // write output to file
    System.out.println("[INFO]: Writing output to file: " + Config.OUTPUT_FILE);
    File outpuFile = new File(Config.OUTPUT_FILE);
    try {
      java.io.PrintWriter writer = new java.io.PrintWriter(outpuFile);
      for (String line : output) {
        writer.println(line);
      }
      writer.close();
    } catch (FileNotFoundException e) {
      System.out.println("[ERROR]: File not found: " + Config.OUTPUT_FILE);
    }
  }

  private static ArrayList<String> getIgnoreList() {
    ArrayList<String> ignoreList = new ArrayList<String>();
    try {
      File file = new File(Config.IGNORE_LIST_PATH);
      Scanner scanner = new Scanner(file);
      while (scanner.hasNextLine()) {
        ignoreList.add(scanner.nextLine());
      }
      scanner.close();
    } catch (FileNotFoundException e) {
      System.out.println("[ERROR]: File not found: " + Config.IGNORE_LIST_PATH);
    }
    return ignoreList;
  }
}
