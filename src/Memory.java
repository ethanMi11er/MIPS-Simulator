import java.util.HashMap;
import java.util.Map;

class Memory {
    public static final int TEXT_BASE = 0x00400000;
    public static final int DATA_BASE = 0x10010000;

    private final Map<Integer, Byte> mem = new HashMap<>();

    // Store a 32-bit word at address
    public void storeWord(int addr, int word) {
        mem.put(addr,     (byte) ((word >>> 24) & 0xFF));
        mem.put(addr + 1, (byte) ((word >>> 16) & 0xFF));
        mem.put(addr + 2, (byte) ((word >>>  8) & 0xFF));
        mem.put(addr + 3, (byte) ( word        & 0xFF));
    }

    // Fetch a 32-bit word from address
    public int fetchWord(int addr) {
        int b0 = (mem.getOrDefault(addr, (byte)0)     & 0xFF) << 24;
        int b1 = (mem.getOrDefault(addr + 1, (byte)0) & 0xFF) << 16;
        int b2 = (mem.getOrDefault(addr + 2, (byte)0) & 0xFF) << 8;
        int b3 = (mem.getOrDefault(addr + 3, (byte)0) & 0xFF);
        return b0 | b1 | b2 | b3;
    }

    // Read null-terminated string
    public String readString(int addr) {
        StringBuilder sb = new StringBuilder();
        byte b;
        while ((b = mem.getOrDefault(addr, (byte)0)) != 0) {
            sb.append((char) b);
            addr++;
        }
        return sb.toString();
    }
}