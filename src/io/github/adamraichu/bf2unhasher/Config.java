package io.github.adamraichu.bf2unhasher;

public class Config {
  public static final int LEFTOVER_THREADS = 2;

  public static final String VALID_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_";
  public static final int MAX_LENGTH = 5; // Adjust based on expected string length

  public static final String HASH_LIST_PATH = "lists/hashes.txt";
  public static final String IGNORE_LIST_PATH = "lists/ignore.txt";

  public static final String OUTPUT_FILE = "output.txt";

  public static final String GENERATED_HASHES = "generatedHashes.txt";
  public static final String EBX_DUMP_PATH = "ebx-dump";
}
