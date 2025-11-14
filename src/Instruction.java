class Instruction {
    enum Type { ADD, SUB, AND, OR, SLT, ADDIU, ANDI, ORI, LUI, LW, SW, BEQ, BNE, J, SYSCALL }

    Type type;
    int rs, rt, rd, shamt;
    int imm;
    int target;

    // Decode 32-bit word into fields and map to Type
    public static Instruction decode(int word, boolean debug) {
        if (debug) System.out.printf("[Decode] word=0x%08X opcode=%d%n", word, (word >>> 26) & 0x3F);
        Instruction inst = new Instruction();
        int opcode = (word >>> 26) & 0x3F;
        if (opcode == 0) {
            inst.rs    = (word >>> 21) & 0x1F;
            inst.rt    = (word >>> 16) & 0x1F;
            inst.rd    = (word >>> 11) & 0x1F;
            inst.shamt = (word >>>  6) & 0x1F;
            int funct  = word & 0x3F;
            if (debug) System.out.println("[Decode] R-type funct=" + funct);
            switch (funct) {
                case 32: inst.type = Type.ADD; break;
                case 34: inst.type = Type.SUB; break;
                case 36: inst.type = Type.AND; break;
                case 37: inst.type = Type.OR;  break;
                case 42: inst.type = Type.SLT; break;
                case 12: inst.type = Type.SYSCALL; break;
                default: throw new UnsupportedOperationException("Unknown R-type funct: " + funct);
            }
        } else {
            inst.rs     = (word >>> 21) & 0x1F;
            inst.rt     = (word >>> 16) & 0x1F;
            inst.imm    = word & 0xFFFF;
            inst.target = word & 0x03FFFFFF;
            switch (opcode) {
                case 9:  inst.type = Type.ADDIU; break;
                case 12: inst.type = Type.ANDI;  break;
                case 13: inst.type = Type.ORI;   break;
                case 15: inst.type = Type.LUI;   break;
                case 35: inst.type = Type.LW;    break;
                case 43: inst.type = Type.SW;    break;
                case 4:  inst.type = Type.BEQ;   break;
                case 5:  inst.type = Type.BNE;   break;
                case 2:  inst.type = Type.J;     break;
                default: throw new UnsupportedOperationException("Unknown opcode: " + opcode);
            }
        }
        return inst;
    }

    @Override
    public String toString() {
        return String.format("%s rs=%d rt=%d rd=%d imm=0x%04X target=0x%07X", type, rs, rt, rd, imm, target);
    }

    public Type getType() { return type; }
    public int getSignedImm() { return (short) imm; }
    public int getTarget()   { return target; }
}
