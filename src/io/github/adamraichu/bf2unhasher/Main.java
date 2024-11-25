package io.github.adamraichu.bf2unhasher;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class Main {
    public static class Result {
        private final AtomicBoolean found;
        private final AtomicReference<String> foundString;
        private final AtomicLong totalTested;

        public Result(AtomicBoolean found, AtomicReference<String> foundString, AtomicLong totalTested) {
            this.found = found;
            this.foundString = foundString;
            this.totalTested = totalTested;
        }

        public AtomicBoolean getFound() {
            return found;
        }

        public AtomicReference<String> getFoundString() {
            return foundString;
        }

        public AtomicLong getTotalTested() {
            return totalTested;
        }
    }

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

        Result result = unhashHash(targetHash, new ArrayList<>());

        if (result.getFound().get()) {
            System.out.println("[INFO]: Found string: " + result.getFoundString().get());
        } else {
            System.out.println("[WARN]: No string found for the given hash.");
        }

        System.out.println("[DEBUG]: Total strings tested: " + result.getTotalTested().get());
    }

    public static Result unhashHash(int hash, ArrayList<String> ignoreList) {
        return unhashHash(hash, ignoreList, "");
    }

    public static Result unhashHash(int hash, ArrayList<String> ignoreList, String guess) {

        int availableProcessors = Runtime.getRuntime().availableProcessors();
        int maxThreads = Math.max(availableProcessors - Config.LEFTOVER_THREADS, 1);

        ExecutorService executorService = Executors.newFixedThreadPool(maxThreads);
        AtomicBoolean found = new AtomicBoolean(false);
        AtomicReference<String> foundString = new AtomicReference<>(null);
        AtomicLong totalTested = new AtomicLong(0); // Total strings tested
        for (int length = 1; length <= Config.MAX_LENGTH; length++) {
            int finalLength = length;
            long lengthTestedCount = 0;
            for (int i = 0; i < Config.VALID_CHARS.length(); i++) {
                String startStr = (!guess.equals("")) ? guess : String.valueOf(Config.VALID_CHARS.charAt(i));
                executorService.submit(() -> {
                    bruteForceWithPrefix(startStr, finalLength - 1, Config.VALID_CHARS, hash, found,
                            foundString, totalTested, ignoreList);
                });
            }
            lengthTestedCount = totalTested.get();
            System.out.printf("[DEBUG]: Length %d: Started testing strings. Total tested so far: %d%n", length,
                    lengthTestedCount);
        }
        executorService.shutdown();
        while (!executorService.isTerminated()) {
        }

        return new Result(found, foundString, totalTested);
    }

    private static void bruteForceWithPrefix(String prefix, int length, String alphabet, int targetHash,
            AtomicBoolean found, AtomicReference<String> foundString, AtomicLong totalTested,
            ArrayList<String> ignoreList) {
        if (found.get()) {
            return;
        }

        if (length == 0) {
            totalTested.incrementAndGet();
            if (fnv1Hash(prefix) == targetHash) {
                System.out.println("[INFO]: Found match " + prefix);
                if (ignoreList.contains(prefix)) {
                    System.out.println("[INFO]: Ignoring because string is on ignore list.");
                    return;
                }
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
            bruteForceWithPrefix(newPrefix, length - 1, alphabet, targetHash, found, foundString, totalTested,
                    ignoreList);
        }
    }
}
