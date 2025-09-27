package compression;

import java.util.ArrayList;

public class SpillCompression extends BaseCompression {
    public ArrayList<String> compress(ArrayList<String> binaryDecompressedArray) {
        long startTime = System.nanoTime();
        int maxBits = 32;
        ArrayList<String> binaryCompressedArray = new ArrayList<>();
        String binary = "";
        for (String b : binaryDecompressedArray)
            binary = binary.concat(b);
        while (!binary.isEmpty()) {
            binaryCompressedArray.add(binary.substring(0, Math.min(maxBits, binary.length())));
            binary = binary.substring(Math.min(maxBits, binary.length()));
        }
        long endTime = System.nanoTime();
        long duration = endTime - startTime;
        System.out.println("Execution time (Spill): " + duration + " nano seconds");
        return binaryCompressedArray;
    }

    public int get(ArrayList<String> binaryCompressedArray, int bits, int i) {
        int maxBits = 32, startFromBitGlobally = i * bits, BCi = startFromBitGlobally / maxBits, startFromBitLocally = startFromBitGlobally % maxBits;
        String compressed = binaryCompressedArray.get(BCi);
        if (startFromBitLocally + bits <= compressed.length()) {
            String binary = compressed.substring(startFromBitLocally, startFromBitLocally + bits);
            return convertToInteger(binary);
        } else {
            String firstPart = compressed.substring(startFromBitLocally);
            int neededFromNext = bits - (compressed.length() - startFromBitLocally);
            String secondPart = binaryCompressedArray.get(BCi + 1).substring(0, neededFromNext);
            String binary = firstPart + secondPart;
            return convertToInteger(binary);
        }
    }

    public ArrayList<Integer> decompress(ArrayList<String> compressedArray, int originalSize, int bits) {
        ArrayList<Integer> decompressedList = new ArrayList<>();
        int maxBits = 32;

        for (int i = 0; i < originalSize; i++) {
            int startFromBitGlobally = i * bits, BCi = startFromBitGlobally / maxBits, startFromBitLocally = startFromBitGlobally % maxBits;
            String compressed = compressedArray.get(BCi);
            String binary;

            if (startFromBitLocally + bits <= compressed.length()) {
                binary = compressed.substring(startFromBitLocally, startFromBitLocally + bits);
            } else {
                String firstPart = compressed.substring(startFromBitLocally);
                int neededFromNext = bits - (compressed.length() - startFromBitLocally);
                String secondPart = compressedArray.get(BCi + 1).substring(0, neededFromNext);
                binary = firstPart + secondPart;
            }
            decompressedList.add(convertToInteger(binary));
        }

        return decompressedList;
    }
}