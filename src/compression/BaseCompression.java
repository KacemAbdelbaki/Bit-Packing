package compression;

import java.util.ArrayList;

public class BaseCompression {
    public String convertToBinary(int i) {
        return i > 0 ? "0".concat(Integer.toBinaryString(i)) : Integer.toBinaryString(i);
    }

    protected Integer convertToInteger(String b) {
        return Integer.parseUnsignedInt(b, 2);
    }

    public ArrayList<String> convertArrayToBinary(ArrayList<Integer> decompressedList, int bits) {
        ArrayList<String> binaryDecompressedList = new ArrayList<String>();
        decompressedList.forEach(i -> {
            String binary = Integer.toBinaryString(i);
            if (binary.length() < bits)
                binary = "0".repeat(bits - binary.length()) + binary;
            else if (binary.length() > bits)
                binary = binary.substring(binary.length() - bits);
            binaryDecompressedList.add(binary);
        });
        return binaryDecompressedList;
    }

    public ArrayList<Integer> convertArrayToInteger(ArrayList<String> binaryCompressedArray) {
        ArrayList<Integer> integerCompressedArray = new ArrayList<>();
        for (String binary : binaryCompressedArray)
            integerCompressedArray.add(convertToInteger(binary));
        return integerCompressedArray;
    }
}