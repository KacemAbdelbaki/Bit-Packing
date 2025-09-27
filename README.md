# Bit Packing

This project demonstrates various integer compression techniques using bit packing. By storing integers in fewer bits than their standard representation, these algorithms can significantly reduce memory usage for large datasets.

## Overview

The project implements three different compression approaches:

1. **No Spill** - Compresses integers by packing them without crossing 32-bit integer boundaries
2. **Spill** - Compresses integers by allowing them to cross 32-bit integer boundaries
3. **Overflow** - Uses a hybrid approach with a threshold-based mechanism for handling large values

## Features

- Three distinct compression algorithms with different space-time tradeoffs
- Individual element access without decompressing the entire array
- Full compression and decompression functionality
- Memory usage and performance benchmarking
- Dynamic threshold calculation for optimal overflow compression

## Requirements

- Java 11 or higher
- [OpenJDK JOL](https://repo1.maven.org/maven2/org/openjdk/jol/jol-core/0.17) (for memory layout analysis)

## Project Structure

```
src/
├── BitPacking.java                # Main class with driver code
├── compression/                   # Package containing all compression implementations
    ├── BaseCompression.java       # Common utility methods
    ├── NoSpillCompression.java    # No-Spill compression implementation
    ├── SpillCompression.java      # Spill compression implementation 
    └── OverflowCompression.java   # Overflow compression implementation
```

## How to Use

### Compilation

Compile all Java files in the project:

```bash
javac -cp path/to/jol-core.jar src/*.java src/compression/*.java
```

### Running the Program

Run the program with one of the following compression types as an argument:

```bash
java -cp path/to/jol-core.jar:src BitPacking [COMPRESSION_TYPE]
```

Where `[COMPRESSION_TYPE]` can be:

- `NO_SPILL` - Run only the No-Spill compression algorithm
- `SPILL` - Run only the Spill compression algorithm
- `OVERFLOW` - Run only the Overflow compression algorithm
- `ALL` - Run and compare all compression algorithms

### Example

```bash
java -cp path/to/jol-core.jar:src BitPacking ALL
```

## How the Algorithms Work

### No-Spill Compression

This algorithm packs as many integers as possible within a 32-bit boundary. For example, if each integer requires 10 bits, the algorithm would pack 3 integers per 32-bit storage unit (3 × 10 = 30 bits), with 2 bits unused.

Key advantages:
- Fast random access (simple arithmetic calculation)
- No crossing of word boundaries

### Spill Compression

This algorithm concatenates all binary representations of integers and chunks them into 32-bit segments, regardless of integer boundaries. This maximizes bit utilization but complicates random access.

Key advantages:
- Maximum space efficiency
- No wasted bits between integers

### Overflow Compression

This sophisticated algorithm uses a threshold to identify values requiring more bits:
- Regular values use a fixed number of bits
- Values exceeding the threshold are stored in a separate overflow area
- A flag bit indicates whether the value is stored directly or in the overflow area

Key advantages:
- More efficient for datasets with varied integer sizes
- Automatically calculates optimal threshold
- Better compression ratio for datasets with outliers

## Performance Analysis

When run with the `ALL` option, the program will generate comparative performance metrics for all three algorithms, including:

- Compression time
- Memory usage
- Decompression time
- Random access speed

## Customization

You can modify the input data generation in the `BitPacking.java` file to test with different datasets:

```java
// Change the size of the test array
int listSize = 1000;

// Modify the range of random integers
decompressedList.add(rand.nextInt(0, 1000));
```