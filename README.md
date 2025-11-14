# MIPS Processor Simulator

## 📖 Overview

This project is a functional MIPS processor simulator written in Java. It is designed to demonstrate the fundamental concepts of computer architecture by implementing the core fetch-decode-execute cycle of a CPU.

The simulator loads MIPS machine code from a `.text` file and corresponding data from a `.data` file. It then executes the program, simulating the MIPS register file, memory, and a subset of MARS (MIPS Assembler and Runtime Simulator) syscalls for input and output.

## ✨ Core Features

* **Fetch-Decode-Execute Cycle:** Implements the main loop of a CPU, fetching 32-bit instructions, decoding them, and executing the corresponding operation.
* **MIPS Instruction Set:** Parses and executes a subset of common MIPS instructions, including R-type, I-type, and J-type formats.
* **Simulated Memory:** Features a byte-addressable memory model (`Memory.java`) that handles loading and storing words, as well as reading null-terminated strings for syscalls.
* **Register File:** Manages all 32 general-purpose MIPS registers, including special handling for the `$zero` register to ensure it always remains 0.
* **MARS Syscall Handling:** Supports essential system calls for program I/O and control, including:
    * `print_int` (code 1)
    * `print_string` (code 4)
    * `read_int` (code 5)
    * `exit` (code 10)

## 🛠️ Technology Stack

* **Core Language:** **Java**
* **Architecture:** Object-oriented design (`Simulator`, `Instruction`, `Memory` classes)
* **Core Concepts:** Bitwise operations, file I/O, switch-case state machines, memory simulation.

## 🚀 How to Run
The simulator is built to run from the command line.

### 1. Compile the Code

Navigate to the `src` directory and compile the Java files:

```bash
cd src
javac *.java
```

### 2. Run the Simulator

From within the src directory, run the Simulator class, passing the paths to the text (program) file and the data file as arguments.

`java Simulator <path-to-text-file> <path-to-data-file>`


### Example (Using included files)

An example EvenOrOdd program is included. To run it, execute the following command from the src directory:

`java Simulator EvenOrOdd.text EvenOrOdd.data`


The program will:

* Prompt you to "Enter a number: ".

* Wait for your input.

* Read the integer and print either "Your number is EVEN!" or "Your number is ODD!".

* Exit gracefully.
