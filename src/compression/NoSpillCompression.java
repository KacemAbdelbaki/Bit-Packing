package compression;

import java.util.ArrayList;

public class NoSpillCompression extends BaseCompression {
    public ArrayList<String> compress(ArrayList<String> binaryDecompressedArray, int bits) {
        long startTime = System.nanoTime();
        int maxBits = 32, numberOfValuesPerInt = maxBits / bits;
        ArrayList<String> binaryCompressedArray = new ArrayList<>();
        String binary;
        for (int i = 0; i < binaryDecompressedArray.size(); i += numberOfValuesPerInt) {
            binary = "";
            for (int j = i; j < Math.min(i + numberOfValuesPerInt, binaryDecompressedArray.size()); j++)
                binary = binary.concat(binaryDecompressedArray.get(j));
            binaryCompressedArray.add(binary);
        }
        long endTime = System.nanoTime();
        long duration = endTime - startTime;
        System.out.println("Execution time (No Spill): " + duration + " nano seconds");
        return binaryCompressedArray;
    }

    public int get(ArrayList<String> binaryCompressedArray, int bits, int i) {
        int maxBits = 32, numberOfValuesPerInt = maxBits / bits, BCi = i / numberOfValuesPerInt, startFromBit = (i % numberOfValuesPerInt) * bits;
        String binary = binaryCompressedArray.get(BCi).substring(startFromBit, startFromBit + bits);
        return convertToInteger(binary);
    }

    public ArrayList<Integer> decompress(ArrayList<String> compressedArray, int originalSize, int bits) {
        ArrayList<Integer> decompressedList = new ArrayList<>();
        int maxBits = 32, numberOfValuesPerInt = maxBits / bits;

        for (int i = 0; i < originalSize; i++) {
            int maxBitsTemp = 32, numberOfValuesPerIntTemp = maxBitsTemp / bits, BCi = i / numberOfValuesPerIntTemp, startFromBit = (i % numberOfValuesPerIntTemp) * bits;
            String binary = compressedArray.get(BCi).substring(startFromBit, startFromBit + bits);
            decompressedList.add(convertToInteger(binary));
        }

        return decompressedList;
    }
}