package io.github.adamraichu.bf2unhasher;

import java.io.File;
import java.util.List;

public class Utils {
  private static final String digits = "0123456789ABCDEF";

  public static String integerToHex(int input) {
    if (input <= 0)
      return "0";
    StringBuilder hex = new StringBuilder();
    while (input > 0) {
      int digit = input % 16;
      hex.insert(0, digits.charAt(digit));
      input = input / 16;
    }
    return hex.toString();
  }

  public static void getAllSubfiles(String directoryName, List<File> files) {
    File directory = new File(directoryName);

    // Get all files from a directory.
    File[] fList = directory.listFiles();
    if (fList != null) {
      for (File file : fList) {
        if (file.isFile()) {
          files.add(file);
        } else if (file.isDirectory()) {
          getAllSubfiles(file.getAbsolutePath(), files);
        }
      }
    }
  }
}
