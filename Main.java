import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class Main {

    public static int fnv1Hash(String input) {
        final int OFFSET_BASIS = 5381;
        final int PRIME = 33;
        int hash = OFFSET_BASIS;

        byte[] data = input.getBytes();

        for (byte b : data) {
            hash = hash * PRIME;
            hash = hash ^ (b & 0xFF);
        }

        return hash;
    }

    public static void main(String[] args) {
        int targetHash = 0x681c4a50; // Example target hash
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        int maxThreads = Math.max(availableProcessors - 2, 1); // To save some cpu power, use all but 2 

        ExecutorService executorService = Executors.newFixedThreadPool(maxThreads);
        AtomicBoolean found = new AtomicBoolean(false);
        AtomicReference<String> foundString = new AtomicReference<>(null);
        AtomicLong totalTested = new AtomicLong(0); // Total strings tested

        final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
      
        final int MAX_LENGTH = 8; // Adjust based on expected string length

        for (int length = 1; length <= MAX_LENGTH; length++) {
            int finalLength = length;
            long lengthTestedCount = 0;
            for (int i = 0; i < ALPHABET.length(); i++) {
                char startChar = ALPHABET.charAt(i);
                executorService.submit(() -> {
                    bruteForceWithPrefix(String.valueOf(startChar), finalLength - 1, ALPHABET, targetHash, found, foundString, totalTested);
                });
            }
            lengthTestedCount = totalTested.get();
            System.out.printf("Length %d: Started testing strings. Total tested so far: %d%n", length, lengthTestedCount);
        }
        executorService.shutdown();
        while (!executorService.isTerminated()) {
        }

        if (found.get()) {
            System.out.println("Found string: " + foundString.get());
        } else {
            System.out.println("No string found for the given hash.");
        }

        System.out.println("Total strings tested: " + totalTested.get());
    }

    private static void bruteForceWithPrefix(String prefix, int length, String alphabet, int targetHash, AtomicBoolean found, AtomicReference<String> foundString, AtomicLong totalTested) {
        if (found.get()) {
            return;
        }

        if (length == 0) {
            totalTested.incrementAndGet(); 
            if (fnv1Hash(prefix) == targetHash) {
                found.set(true);
                foundString.set(prefix);
            }
            return;
        }

        for (int i = 0; i < alphabet.length(); i++) {
            if (found.get()) {
                return;
            }

            String newPrefix = prefix + alphabet.charAt(i);
            bruteForceWithPrefix(newPrefix, length - 1, alphabet, targetHash, found, foundString, totalTested);
        }
    }
}
