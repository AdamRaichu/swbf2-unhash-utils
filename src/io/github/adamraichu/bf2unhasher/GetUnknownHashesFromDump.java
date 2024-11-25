package io.github.adamraichu.bf2unhasher;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GetUnknownHashesFromDump {
  private static final String regex = "(?<=(?<=(<((SourceFieldId)|(TargetFieldId)|(Id))))>)0x[0-9A-Fa-f]{8}(?=(</))";
  private static final Pattern pattern = Pattern.compile(regex);

  public static void main(String[] args) {
    List<File> filesToSearch = new ArrayList<File>();
    System.out.println();
    System.out.println("Searching for files...");
    Utils.getAllSubfiles(Config.EBX_DUMP_PATH, filesToSearch);
    System.out.println("Found " + String.valueOf(filesToSearch.size()) + " files to search.");

    deleteOldRun();

    try (BufferedWriter writer = new BufferedWriter(new FileWriter(Config.GENERATED_HASHES, true))) {
      processFiles(filesToSearch, writer);
    } catch (IOException e) {
      e.printStackTrace();
    }

    cleanDuplicates();
  }

  private static void deleteOldRun() {
    // Clear the file first.
    File generatedHashesTxt = new File(Config.GENERATED_HASHES);
    try {
      if (generatedHashesTxt.exists()) {
        generatedHashesTxt.delete();
      }
      generatedHashesTxt.createNewFile();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static void cleanDuplicates() {
    // Clean the file
    try {
      HashSet<String> uniqueHashes = new HashSet<>();
      BufferedReader reader = new BufferedReader(new FileReader(Config.GENERATED_HASHES));
      File tempFile = new File(Config.GENERATED_HASHES + ".tmp");
      BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

      int count = 0;
      String line;
      while ((line = reader.readLine()) != null) {
        if (uniqueHashes.add(line)) {
          writer.write(line);
          writer.newLine();
          count++;
        }
      }

      System.out.println("Found " + String.valueOf(count) + " unique unknown hashes.");

      reader.close();
      writer.close();
      uniqueHashes.clear();
      uniqueHashes = null;

      Files.delete(new File(Config.GENERATED_HASHES).toPath());
      tempFile.renameTo(new File(Config.GENERATED_HASHES));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static ArrayList<String> processFiles(List<File> filesToSearch, BufferedWriter writer) {
    ArrayList<String> hashes = new ArrayList<>();
    int filesToSearchCount = filesToSearch.size();
    for (int i = 0; i < filesToSearchCount; i++) {
      try {
        File file = filesToSearch.get(i);
        String contents = Files.readString(file.toPath());
        System.out.println("Reading " + file.getName() + " (" + (i + 1) + "/" + filesToSearchCount + ")");
        ArrayList<String> result = findUnknownHashes(contents);
        // System.out.println("Found " + String.valueOf(result.size()) + " hashes.");
        for (String hash : result) {
          hashes.add(hash);
          writer.write(hash);
          writer.newLine();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    return hashes;
  }

  /**
   * Take the contents of a file from the EBX dump and search it for hashes.
   * 
   * @param contents The contents of the EBX dump (XML) file as a string.
   * @return All previously unknown hashes.
   *
   * @see #regex
   * 
   * @apiNote This does <b>not</b> include values that already had a matching
   *          hash.
   */
  private static ArrayList<String> findUnknownHashes(String contents) {
    Matcher matcher = pattern.matcher(contents);
    List<MatchResult> results = matcher.results().toList();
    ArrayList<String> output = new ArrayList<>();
    results.forEach(result -> {
      output.add(result.group(0));
    });
    return output;
  }
}
