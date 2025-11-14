import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;


public class Simulator {
    private static final int REGISTER_ZERO = 0;
    private static final int V0 = 2;
    private static final int A0 = 4;

    // Enable debug tracing
    private final boolean debug = false;

    // 32 general-purpose registers
    private int[] regs = new int[32];
    // Program Counter
    private int pc;
    // Memory (text + data)
    private Memory memory;

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java -jar Simulator.jar <textFile> <dataFile>");
            System.exit(1);
        }

        Simulator sim = new Simulator();
        sim.loadTextSegment(args[0]);
        sim.loadDataSegment(args[1]);
        sim.run();
    }

    private Simulator() {
        // Initialize PC to text segment base
        this.pc = Memory.TEXT_BASE;
        this.memory = new Memory();
        // Ensure register zero is always 0
        regs[0] = 0;
    }

    // Load instructions (hex per line) into text segment
    private void loadTextSegment(String path) {
        if (debug) System.out.println("Loading text segment from " + path);
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            int addr = this.pc;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                int instr = (int) Long.parseLong(line, 16);
                if (debug) System.out.printf("[LoadText] Addr=0x%08X Instr=0x%08X%n", addr, instr);
                memory.storeWord(addr, instr);
                addr += 4;
            }
        } catch (IOException e) {
            System.err.println("Error loading text segment: " + e.getMessage());
            System.exit(1);
        }
    }

    // Load data (hex per line) into data segment
    private void loadDataSegment(String path) {
        if (debug) System.out.println("Loading data segment from " + path);
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            int addr = Memory.DATA_BASE;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                int word = (int) Long.parseLong(line, 16);
                // because lil endian
                word = Integer.reverseBytes(word);
                if (debug) System.out.printf("[LoadData] Addr=0x%08X Data=0x%08X%n", addr, word);
                memory.storeWord(addr, word);
                addr += 4;
            }
        } catch (IOException e) {
            System.err.println("Error loading data segment: " + e.getMessage());
            System.exit(1);
        }
    }

    // Main fetch-decode-execute loop
    private void run() {
        if (debug) System.out.println("Starting simulation at PC = 0x" + Integer.toHexString(pc));
        while (true) {
            int instrWord = memory.fetchWord(pc);
            if (instrWord == 0) {
                System.out.println("\n-- program finished running (dropped off bottom) --");
                break;
            }
            if (debug) System.out.printf("[Fetch] PC=0x%08X Instr=0x%08X%n", pc, instrWord);

            Instruction inst = Instruction.decode(instrWord, debug);
            execute(inst);
        }
    }

    // Execute one decoded instruction
    private void execute(Instruction inst) {
        if (debug) System.out.println("[Execute] " + inst);
        switch (inst.getType()) {
            // R-type instructions
            case ADD:
                regs[inst.rd] = regs[inst.rs] + regs[inst.rt];
                break;
            case SUB:
                regs[inst.rd] = regs[inst.rs] - regs[inst.rt];
                break;
            case AND:
                regs[inst.rd] = regs[inst.rs] & regs[inst.rt];
                break;
            case OR:
                regs[inst.rd] = regs[inst.rs] | regs[inst.rt];
                break;
            case SLT:
                regs[inst.rd] = (regs[inst.rs] < regs[inst.rt]) ? 1 : 0;
                break;

            // I-type arithmetic/logical
            case ADDIU:
                regs[inst.rt] = regs[inst.rs] + inst.getSignedImm();
                break;
            case ANDI:
                regs[inst.rt] = regs[inst.rs] & (inst.imm & 0xFFFF);
                break;
            case ORI:
                regs[inst.rt] = regs[inst.rs] | (inst.imm & 0xFFFF);
                break;
            case LUI:
                regs[inst.rt] = inst.imm << 16;
                break;

            // Memory access
            case LW:
                int lwAddr = regs[inst.rs] + inst.getSignedImm();
                regs[inst.rt] = memory.fetchWord(lwAddr);
                break;
            case SW:
                int swAddr = regs[inst.rs] + inst.getSignedImm();
                memory.storeWord(swAddr, regs[inst.rt]);
                break;

            // Branches
            case BEQ:
                if (regs[inst.rs] == regs[inst.rt]) {
                    pc += (inst.getSignedImm() << 2);
                }
                break;
            case BNE:
                if (regs[inst.rs] != regs[inst.rt]) {
                    pc += (inst.getSignedImm() << 2);
                }
                break;

            // Jump
            case J:
                pc = (pc & 0xF0000000) | (inst.getTarget() << 2);
                return; // Skip default PC+4

            // Syscall
            case SYSCALL:
                doSyscall();
                return; // syscall handler advances PC or exits

            default:
                throw new UnsupportedOperationException("Unimplemented: " + inst.getType());
        }

        // Ensure $zero remains zero
        regs[REGISTER_ZERO] = 0;
        // Advance PC by default
        pc += 4;
    }

    // Handle MARS syscalls
    private void doSyscall() {
        int code = regs[V0];
        if (debug) System.out.println("[Syscall] code=" + code);
        switch (code) {
            case 1: // print int
                System.out.print(regs[A0]);
                break;
            case 4: // print string
                String s = memory.readString(regs[A0]);
                System.out.print(s);
                break;
            case 5: // read int
                regs[V0] = readIntegerFromStdin();
                break;
            case 10: // exit
                System.out.println("\n-- program is finished running --");
                System.exit(0);
                break;
            default:
                throw new UnsupportedOperationException("Unknown syscall: " + code);
        }
        // Ensure $zero remains zero
        regs[REGISTER_ZERO] = 0;
        // Advance PC
        pc += 4;
    }

    private int readIntegerFromStdin() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String line = reader.readLine();
            return Integer.parseInt(line.trim());
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error reading integer: " + e.getMessage());
            return 0;
        }
    }
}