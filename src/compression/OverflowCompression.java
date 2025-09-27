package compression;

import java.util.ArrayList;
import java.util.Collections;

public class OverflowCompression extends BaseCompression {
    public static class OverflowCompressionResult {
        public ArrayList<String> mainCompressedArray;
        public ArrayList<String> overflowArray;
        public int normalBits;
        public int overflowBitCount;
        public int thresholdBits;

        public OverflowCompressionResult(ArrayList<String> main, ArrayList<String> overflow, int nBits, int oBits, int tBits) {
            this.mainCompressedArray = main;
            this.overflowArray = overflow;
            this.normalBits = nBits;
            this.overflowBitCount = oBits;
            this.thresholdBits = tBits;
        }
    }

    public OverflowCompressionResult compress(ArrayList<Integer> decompressedList, int thresholdBits) {
        long startTime = System.nanoTime();
        ArrayList<String> normalBinaryList = new ArrayList<>();
        ArrayList<Integer> overflowList = new ArrayList<>();

        for (int i = 0; i < decompressedList.size(); i++) {
            String binary = Integer.toBinaryString(decompressedList.get(i));
            if (binary.length() > thresholdBits) {
                int overflowIndex = overflowList.size();
                overflowList.add(decompressedList.get(i));
                String overflowIndexBinary = Integer.toBinaryString(overflowIndex);
                int overflowIndexBits = overflowList.size() > 0 ? (int) Math.ceil(Math.log(overflowList.size()) / Math.log(2)) : 1;
                if (overflowList.size() <= 1) overflowIndexBits = 1;
                if (overflowIndexBinary.length() < overflowIndexBits)
                    overflowIndexBinary = "0".repeat(overflowIndexBits - overflowIndexBinary.length()) + overflowIndexBinary;
                normalBinaryList.add("1" + overflowIndexBinary);
            } else {
                if (binary.length() < thresholdBits)
                    binary = "0".repeat(thresholdBits - binary.length()) + binary;
                normalBinaryList.add("0" + binary);
            }
        }

        int finalOverflowIndexBits = overflowList.size() > 0 ? (int) Math.ceil(Math.log(overflowList.size()) / Math.log(2)) : 1;
        if (overflowList.size() <= 1) finalOverflowIndexBits = 1;

        for (int i = 0; i < normalBinaryList.size(); i++) {
            String entry = normalBinaryList.get(i);
            if (entry.charAt(0) == '1') {
                String overflowIndexBinary = entry.substring(1);
                if (overflowIndexBinary.length() < finalOverflowIndexBits) {
                    overflowIndexBinary = "0".repeat(finalOverflowIndexBits - overflowIndexBinary.length()) + overflowIndexBinary;
                    normalBinaryList.set(i, "1" + overflowIndexBinary);
                }
            }
        }

        ArrayList<String> compressedMain = new SpillCompression().compress(normalBinaryList);
        int maxEntryWidth = 1 + Math.max(thresholdBits, finalOverflowIndexBits);

        ArrayList<String> overflowStringList = new ArrayList<>();
        for (Integer val : overflowList) {
            overflowStringList.add(Integer.toBinaryString(val));
        }

        long endTime = System.nanoTime();
        long duration = endTime - startTime;
        System.out.println("Execution time (Overflow): " + duration + " nano seconds");

        return new OverflowCompressionResult(compressedMain, overflowStringList, maxEntryWidth, finalOverflowIndexBits, thresholdBits);
    }

    public int get(OverflowCompressionResult result, int i) {
        ArrayList<String> compressedArray = result.mainCompressedArray;
        ArrayList<String> overflowArray = result.overflowArray;
        int thresholdBits = result.thresholdBits;
        int overflowBitCount = result.overflowBitCount;
        int maxEntryWidth = result.normalBits;

        int startFromBitGlobally = i * maxEntryWidth;
        int maxBits = 32;
        int BCi = startFromBitGlobally / maxBits;
        int startFromBitLocally = startFromBitGlobally % maxBits;
        String compressed = compressedArray.get(BCi);
        String binary;

        if (startFromBitLocally + maxEntryWidth <= compressed.length()) {
            binary = compressed.substring(startFromBitLocally, startFromBitLocally + maxEntryWidth);
        } else {
            String firstPart = compressed.substring(startFromBitLocally);
            int neededFromNext = maxEntryWidth - (compressed.length() - startFromBitLocally);
            String secondPart = compressedArray.get(BCi + 1).substring(0, neededFromNext);
            binary = firstPart + secondPart;
        }

        if (binary.charAt(0) == '1') {
            String overflowIndexBinary = binary.substring(1, 1 + overflowBitCount);
            int overflowIndex = convertToInteger(overflowIndexBinary);
            return convertToInteger(overflowArray.get(overflowIndex));
        } else {
            String normalBinary = binary.substring(1, 1 + thresholdBits);
            return convertToInteger(normalBinary);
        }
    }

    public ArrayList<Integer> decompress(OverflowCompressionResult result, int originalSize) {
        ArrayList<Integer> decompressedList = new ArrayList<>();
        for (int i = 0; i < originalSize; i++) {
            decompressedList.add(get(result, i));
        }
        return decompressedList;
    }

    public int calculateDynamicThreshold(ArrayList<Integer> decompressedList) {
        ArrayList<Integer> bitRequirements = new ArrayList<>();
        for (Integer num : decompressedList) {
            String binary = Integer.toBinaryString(num);
            bitRequirements.add(binary.length());
        }

        Collections.sort(bitRequirements);

        int maxBits = bitRequirements.get(bitRequirements.size() - 1);

        if (bitRequirements.get(0).equals(maxBits)) {
            return maxBits - 1;
        }

        int bestThreshold = maxBits - 1;
        long minTotalBits = Long.MAX_VALUE;

        for (int threshold = bitRequirements.get(0); threshold < maxBits; threshold++) {
            int normalCount = 0;
            for (int bits : bitRequirements) {
                if (bits <= threshold) {
                    normalCount++;
                } else {
                    break;
                }
            }

            int overflowCount = decompressedList.size() - normalCount;

            long normalBitsTotal = (long) normalCount * (threshold + 1);
            long overflowBitsTotal = 0;
            for (int i = 0; i < decompressedList.size(); i++) {
                 String binary = Integer.toBinaryString(decompressedList.get(i));
                 if (binary.length() > threshold) {
                     overflowBitsTotal += binary.length();
                 }
            }
            int overflowIndexBits = overflowCount > 0 ? (int) Math.ceil(Math.log(overflowCount) / Math.log(2)) : 0;
            if (overflowCount <= 1 && overflowCount > 0) overflowIndexBits = 1;
            int maxEntryWidth = 1 + Math.max(threshold, overflowIndexBits);
            long totalMainBits = (long) decompressedList.size() * maxEntryWidth;

            long totalBits = totalMainBits + overflowBitsTotal;

            if (totalBits < minTotalBits) {
                minTotalBits = totalBits;
                bestThreshold = threshold;
            }
        }

        return bestThreshold;
    }
}