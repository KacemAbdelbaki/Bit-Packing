package compression;

import org.openjdk.jol.info.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

public class BitPacking {

    enum CompressionType {
        NO_SPILL, SPILL, OVERFLOW, ALL
    }

    void main(String[] args) {
        System.out.println("======================================= STARTING EXECUTION =======================================");
        long startTime = System.currentTimeMillis();

        if (args.length == 0) {
            System.err.println("Please provide a compression type: NO_SPILL, SPILL, OVERFLOW, or ALL");
            return;
        }

        CompressionType compressionType = null;
        try {
            compressionType = CompressionType.valueOf(args[0].toUpperCase());
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid compression type. Use: NO_SPILL, SPILL, OVERFLOW, or ALL");
            return;
        }

        System.out.println("Selected compression type: " + compressionType);
        System.out.println();

        int x = Integer.MIN_VALUE;
        ArrayList<Integer> decompressedList = new ArrayList<>();
        Random rand = new Random();
        int listSize = 1000;
        for (int i = 0; i < listSize; i++)
            decompressedList.add(rand.nextInt(0, 1000));

        BaseCompression baseCompression = new BaseCompression();
        System.out.println("Input array: " + decompressedList + " | Size: " + decompressedList.size() + " | Memory: " + GraphLayout.parseInstance(decompressedList).totalSize() * 8 + " bits");
        int max = Collections.max(decompressedList, Comparator.comparingInt(i -> Integer.toBinaryString(i).length()));
        String binaryMax = baseCompression.convertToBinary(max);
        int bits = binaryMax.length();
        System.out.println("Max value: " + max + " | Binary: " + binaryMax + " (" + bits + " bits required)");
        ArrayList<String> binaryDecompressedList = baseCompression.convertArrayToBinary(decompressedList, bits);
        System.out.println("Binary representation: " + binaryDecompressedList + " | Size: " + binaryDecompressedList.size() + " | Memory: " + GraphLayout.parseInstance(binaryDecompressedList).totalSize() * 8 + " bits");
        System.out.println();

        if (compressionType == CompressionType.NO_SPILL || compressionType == CompressionType.ALL) {
            if (compressionType == CompressionType.ALL) {
                System.out.println("============================== COMPRESSION TYPE: NO_SPILL ==============================");
            }
            NoSpillCompression noSpillCompression = new NoSpillCompression();
            System.out.println("Compression Method: No Spill (Pack values without crossing integer boundaries)");
            ArrayList<String> binaryCompressedList = noSpillCompression.compress(binaryDecompressedList, bits);
            System.out.println("Compressed binary array: " + binaryCompressedList + " | Size: " + binaryCompressedList.size() + " | Memory: " + GraphLayout.parseInstance(binaryCompressedList).totalSize() * 8 + " bits");

            ArrayList<Integer> integerCompressedArray = baseCompression.convertArrayToInteger(binaryCompressedList);
            System.out.println("Compressed integer array: " + integerCompressedArray + " | Size: " + integerCompressedArray.size() + " | Memory: " + GraphLayout.parseInstance(integerCompressedArray).totalSize() * 8 + " bits");

            System.out.println("Decompression Test:");
            ArrayList<Integer> decompressedNoSpill = noSpillCompression.decompress(binaryCompressedList, decompressedList.size(), bits);
            System.out.println("Decompressed array: " + decompressedNoSpill);
            System.out.println("Decompression match: " + decompressedList.equals(decompressedNoSpill));

            System.out.println("Access Test:");
            int IthNoSpill = rand.nextInt(0, decompressedList.size());
            System.out.println("  Element at index " + IthNoSpill + ":");
            System.out.println("    Original: " + decompressedList.get(IthNoSpill));
            System.out.println("    Retrieved: " + noSpillCompression.get(binaryCompressedList, bits, IthNoSpill));
            System.out.println("    Match: " + (decompressedList.get(IthNoSpill).equals(noSpillCompression.get(binaryCompressedList, bits, IthNoSpill)) ? "YES" : "NO"));
            System.out.println();
        }

        if (compressionType == CompressionType.SPILL || compressionType == CompressionType.ALL) {
            if (compressionType == CompressionType.ALL) {
                System.out.println("============================== COMPRESSION TYPE: SPILL ==============================");
            }
            SpillCompression spillCompression = new SpillCompression();
            System.out.println("Compression Method: Spill (Pack values allowing them to cross integer boundaries)");
            ArrayList<String> binaryCompressedListSpill = spillCompression.compress(binaryDecompressedList);
            System.out.println("Compressed binary array: " + binaryCompressedListSpill + " | Size: " + binaryCompressedListSpill.size() + " | Memory: " + GraphLayout.parseInstance(binaryCompressedListSpill).totalSize() * 8 + " bits");

            ArrayList<Integer> integerCompressedArraySpill = baseCompression.convertArrayToInteger(binaryCompressedListSpill);
            System.out.println("Compressed integer array: " + integerCompressedArraySpill + " | Size: " + integerCompressedArraySpill.size() + " | Memory: " + GraphLayout.parseInstance(integerCompressedArraySpill).totalSize() * 8 + " bits");

            System.out.println("Decompression Test:");
            ArrayList<Integer> decompressedSpill = spillCompression.decompress(binaryCompressedListSpill, decompressedList.size(), bits);
            System.out.println("Decompressed array: " + decompressedSpill);
            System.out.println("Decompression match: " + decompressedList.equals(decompressedSpill));

            System.out.println("Access Test:");
            int IthSpill = rand.nextInt(0, decompressedList.size());
            System.out.println("  Element at index " + IthSpill + ":");
            System.out.println("    Original: " + decompressedList.get(IthSpill));
            System.out.println("    Retrieved: " + spillCompression.get(binaryCompressedListSpill, bits, IthSpill));
            System.out.println("    Match: " + (decompressedList.get(IthSpill).equals(spillCompression.get(binaryCompressedListSpill, bits, IthSpill)) ? "YES" : "NO"));
            System.out.println();
        }

        if (compressionType == CompressionType.OVERFLOW || compressionType == CompressionType.ALL) {
            if (compressionType == CompressionType.ALL) {
                System.out.println("============================== COMPRESSION TYPE: OVERFLOW ==============================");
            }
            OverflowCompression overflowCompression = new OverflowCompression();
            System.out.println("Compression Method: Overflow (Use overflow area for values requiring more bits)");
            int dynamicThreshold = overflowCompression.calculateDynamicThreshold(decompressedList);
            System.out.println("Calculated optimal threshold: " + dynamicThreshold + " bits");

            OverflowCompression.OverflowCompressionResult overflowResult = overflowCompression.compress(decompressedList, dynamicThreshold);
            System.out.println("Main compressed array: " + overflowResult.mainCompressedArray + " | Size: " + overflowResult.mainCompressedArray.size() + " | Memory: " + GraphLayout.parseInstance(overflowResult.mainCompressedArray).totalSize() * 8 + " bits");
            System.out.println("Overflow array: " + overflowResult.overflowArray + " | Size: " + overflowResult.overflowArray.size() + " | Memory: " + GraphLayout.parseInstance(overflowResult.overflowArray).totalSize() * 8 + " bits");
            System.out.println("Max entry width in main array: " + overflowResult.normalBits + " | Overflow index bits: " + overflowResult.overflowBitCount);

            System.out.println("Decompression Test:");
            ArrayList<Integer> decompressedOverflow = overflowCompression.decompress(overflowResult, decompressedList.size());
            System.out.println("Decompressed array: " + decompressedOverflow);
            System.out.println("Decompression match: " + decompressedList.equals(decompressedOverflow));

            System.out.println("Access Test:");
            int IthOverflow = rand.nextInt(0, decompressedList.size());
            System.out.println("  Element at index " + IthOverflow + ":");
            System.out.println("    Original: " + decompressedList.get(IthOverflow));
            int retrievedOverflow = overflowCompression.get(overflowResult, IthOverflow);
            System.out.println("    Retrieved: " + retrievedOverflow);
            System.out.println("    Match: " + (decompressedList.get(IthOverflow).equals(retrievedOverflow) ? "YES" : "NO"));
            System.out.println();
        }

        if (compressionType == CompressionType.ALL) {
            System.out.println("============================== SUMMARY OF ALL METHODS ==============================");
            System.out.println("Comparing all compression methods on the same input array:");
            System.out.println("Input: " + decompressedList);
            System.out.println();

            NoSpillCompression noSpillCompression = new NoSpillCompression();
            SpillCompression spillCompression = new SpillCompression();
            OverflowCompression overflowCompression = new OverflowCompression();

            int IthAll = rand.nextInt(0, decompressedList.size());
            System.out.println("Access Test (Index " + IthAll + "):");
            System.out.println("  Original: " + decompressedList.get(IthAll));

            ArrayList<String> noSpillArray = noSpillCompression.compress(binaryDecompressedList, bits);
            int noSpillResult = noSpillCompression.get(noSpillArray, bits, IthAll);
            System.out.println("  No Spill: " + noSpillResult + " | Match: " + (decompressedList.get(IthAll).equals(noSpillResult) ? "YES" : "NO"));

            ArrayList<String> spillArray = spillCompression.compress(binaryDecompressedList);
            int spillResult = spillCompression.get(spillArray, bits, IthAll);
            System.out.println("  Spill: " + spillResult + " | Match: " + (decompressedList.get(IthAll).equals(spillResult) ? "YES" : "NO"));

            int dynamicThresholdAll = overflowCompression.calculateDynamicThreshold(decompressedList);
            OverflowCompression.OverflowCompressionResult overflowResultAll = overflowCompression.compress(decompressedList, dynamicThresholdAll);
            int overflowResult = overflowCompression.get(overflowResultAll, IthAll);
            System.out.println("  Overflow: " + overflowResult + " | Match: " + (decompressedList.get(IthAll).equals(overflowResult) ? "YES" : "NO"));
            System.out.println();
        }

        System.out.println("======================================= EXECUTION COMPLETE =======================================");
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        System.out.println("Total execution time: " + duration + " ms");
    }
}