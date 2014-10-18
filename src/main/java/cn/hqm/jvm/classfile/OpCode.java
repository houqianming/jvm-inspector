package cn.hqm.jvm.classfile;

import cn.hqm.jvm.classfile.opcode.LookupSwitch;
import cn.hqm.jvm.classfile.opcode.OneIndexOpCode;
import cn.hqm.jvm.classfile.opcode.TableSwitch;
import cn.hqm.jvm.classfile.opcode.Wide;


/**
 * 
 * @author linxuan
 * @date 2014-3-23 上午10:55:21
 */
public class OpCode {
    public final byte opcode; //字节码取值
    public final String mnemonic; //字符表示，如aload_1
    public final int paramlen; //参数个数


    public OpCode(byte opcode, String mnemonic, int paramlen) {
        this.opcode = opcode;
        this.mnemonic = mnemonic;
        this.paramlen = paramlen;
    }


    public void display(Displayer displayer, byte[] b, int[] offset, ConstantInfo[] pool, int codeIndex) {
        String params = "";
        if (paramlen > 0) {
            int index = offset[0];
            for (int i = 1; i <= paramlen; i++) {
                params = params + " " + (0xff & b[index + i]);
            }
            String comments = commentOnParams(b, index + 1, paramlen, pool);
            if (comments != null) {
                params = params + " //" + comments;
            }
        }
        else if (paramlen < 0) {
            throw new IllegalStateException(mnemonic + " not support yet. paramlen < 0");
        }
        displayer.writeAndStepOffset(b, offset, paramlen + 1, mnemonic + params);
    }


    protected String commentOnParams(byte[] b, int index, int len, ConstantInfo[] pool) {
        return null;
    }

    public static OpCode[] opcodes = new OpCode[256];
    static {
        opcodes[0] = new OpCode((byte) 0x00, "nop", 0);
        opcodes[1] = new OpCode((byte) 0x01, "aconst_null", 0);
        opcodes[2] = new OpCode((byte) 0x02, "iconst_m1", 0);
        opcodes[3] = new OpCode((byte) 0x03, "iconst_0", 0);
        opcodes[4] = new OpCode((byte) 0x04, "iconst_1", 0);
        opcodes[5] = new OpCode((byte) 0x05, "iconst_2", 0);
        opcodes[6] = new OpCode((byte) 0x06, "iconst_3", 0);
        opcodes[7] = new OpCode((byte) 0x07, "iconst_4", 0);
        opcodes[8] = new OpCode((byte) 0x08, "iconst_5", 0);
        opcodes[9] = new OpCode((byte) 0x09, "lconst_0", 0);
        opcodes[10] = new OpCode((byte) 0x0a, "lconst_1", 0);
        opcodes[11] = new OpCode((byte) 0x0b, "fconst_0", 0);
        opcodes[12] = new OpCode((byte) 0x0c, "fconst_1", 0);
        opcodes[13] = new OpCode((byte) 0x0d, "fconst_2", 0);
        opcodes[14] = new OpCode((byte) 0x0e, "dconst_0", 0);
        opcodes[15] = new OpCode((byte) 0x0f, "dconst_1", 0);
        opcodes[16] = new OpCode((byte) 0x10, "bipush", 1);
        opcodes[17] = new OpCode((byte) 0x11, "sipush", 2);
        opcodes[18] = new OneIndexOpCode((byte) 0x12, "ldc", 1);
        opcodes[19] = new OneIndexOpCode((byte) 0x13, "ldc_w", 2);
        opcodes[20] = new OneIndexOpCode((byte) 0x14, "ldc2_w", 2);
        opcodes[21] = new OpCode((byte) 0x15, "iload", 1);
        opcodes[22] = new OpCode((byte) 0x16, "lload", 1);
        opcodes[23] = new OpCode((byte) 0x17, "fload", 1);
        opcodes[24] = new OpCode((byte) 0x18, "dload", 1);
        opcodes[25] = new OpCode((byte) 0x19, "aload", 1);
        opcodes[26] = new OpCode((byte) 0x1a, "iload_0", 0);
        opcodes[27] = new OpCode((byte) 0x1b, "iload_1", 0);
        opcodes[28] = new OpCode((byte) 0x1c, "iload_2", 0);
        opcodes[29] = new OpCode((byte) 0x1d, "iload_3", 0);
        opcodes[30] = new OpCode((byte) 0x1e, "lload_0", 0);
        opcodes[31] = new OpCode((byte) 0x1f, "lload_1", 0);
        opcodes[32] = new OpCode((byte) 0x20, "lload_2", 0);
        opcodes[33] = new OpCode((byte) 0x21, "lload_3", 0);
        opcodes[34] = new OpCode((byte) 0x22, "fload_0", 0);
        opcodes[35] = new OpCode((byte) 0x23, "fload_1", 0);
        opcodes[36] = new OpCode((byte) 0x24, "fload_2", 0);
        opcodes[37] = new OpCode((byte) 0x25, "fload_3", 0);
        opcodes[38] = new OpCode((byte) 0x26, "dload_0", 0);
        opcodes[39] = new OpCode((byte) 0x27, "dload_1", 0);
        opcodes[40] = new OpCode((byte) 0x28, "dload_2", 0);
        opcodes[41] = new OpCode((byte) 0x29, "dload_3", 0);
        opcodes[42] = new OpCode((byte) 0x2a, "aload_0", 0);
        opcodes[43] = new OpCode((byte) 0x2b, "aload_1", 0);
        opcodes[44] = new OpCode((byte) 0x2c, "aload_2", 0);
        opcodes[45] = new OpCode((byte) 0x2d, "aload_3", 0);
        opcodes[46] = new OpCode((byte) 0x2e, "iaload", 0);
        opcodes[47] = new OpCode((byte) 0x2f, "laload", 0);
        opcodes[48] = new OpCode((byte) 0x30, "faload", 0);
        opcodes[49] = new OpCode((byte) 0x31, "daload", 0);
        opcodes[50] = new OpCode((byte) 0x32, "aaload", 0);
        opcodes[51] = new OpCode((byte) 0x33, "baload", 0);
        opcodes[52] = new OpCode((byte) 0x34, "caload", 0);
        opcodes[53] = new OpCode((byte) 0x35, "saload", 0);
        opcodes[54] = new OpCode((byte) 0x36, "istore", 1);
        opcodes[55] = new OpCode((byte) 0x37, "lstore", 1);
        opcodes[56] = new OpCode((byte) 0x38, "fstore", 1);
        opcodes[57] = new OpCode((byte) 0x39, "dstore", 1);
        opcodes[58] = new OpCode((byte) 0x3a, "astore", 1);
        opcodes[59] = new OpCode((byte) 0x3b, "istore_0", 0);
        opcodes[60] = new OpCode((byte) 0x3c, "istore_1", 0);
        opcodes[61] = new OpCode((byte) 0x3d, "istore_2", 0);
        opcodes[62] = new OpCode((byte) 0x3e, "istore_3", 0);
        opcodes[63] = new OpCode((byte) 0x3f, "lstore_0", 0);
        opcodes[64] = new OpCode((byte) 0x40, "lstore_1", 0);
        opcodes[65] = new OpCode((byte) 0x41, "lstore_2", 0);
        opcodes[66] = new OpCode((byte) 0x42, "lstore_3", 0);
        opcodes[67] = new OpCode((byte) 0x43, "fstore_0", 0);
        opcodes[68] = new OpCode((byte) 0x44, "fstore_1", 0);
        opcodes[69] = new OpCode((byte) 0x45, "fstore_2", 0);
        opcodes[70] = new OpCode((byte) 0x46, "fstore_3", 0);
        opcodes[71] = new OpCode((byte) 0x47, "dstore_0", 0);
        opcodes[72] = new OpCode((byte) 0x48, "dstore_1", 0);
        opcodes[73] = new OpCode((byte) 0x49, "dstore_2", 0);
        opcodes[74] = new OpCode((byte) 0x4a, "dstore_3", 0);
        opcodes[75] = new OpCode((byte) 0x4b, "astore_0", 0);
        opcodes[76] = new OpCode((byte) 0x4c, "astore_1", 0);
        opcodes[77] = new OpCode((byte) 0x4d, "astore_2", 0);
        opcodes[78] = new OpCode((byte) 0x4e, "astore_3", 0);
        opcodes[79] = new OpCode((byte) 0x4f, "iastore", 0);
        opcodes[80] = new OpCode((byte) 0x50, "lastore", 0);
        opcodes[81] = new OpCode((byte) 0x51, "fastore", 0);
        opcodes[82] = new OpCode((byte) 0x52, "dastore", 0);
        opcodes[83] = new OpCode((byte) 0x53, "aastore", 0);
        opcodes[84] = new OpCode((byte) 0x54, "bastore", 0);
        opcodes[85] = new OpCode((byte) 0x55, "castore", 0);
        opcodes[86] = new OpCode((byte) 0x56, "sastore", 0);
        opcodes[87] = new OpCode((byte) 0x57, "pop", 0);
        opcodes[88] = new OpCode((byte) 0x58, "pop2", 0);
        opcodes[89] = new OpCode((byte) 0x59, "dup", 0);
        opcodes[90] = new OpCode((byte) 0x5a, "dup_x1", 0);
        opcodes[91] = new OpCode((byte) 0x5b, "dup_x2", 0);
        opcodes[92] = new OpCode((byte) 0x5c, "dup2", 0);
        opcodes[93] = new OpCode((byte) 0x5d, "dup2_x1", 0);
        opcodes[94] = new OpCode((byte) 0x5e, "dup2_x2", 0);
        opcodes[95] = new OpCode((byte) 0x5f, "swap", 0);
        opcodes[96] = new OpCode((byte) 0x60, "iadd", 0);
        opcodes[97] = new OpCode((byte) 0x61, "ladd", 0);
        opcodes[98] = new OpCode((byte) 0x62, "fadd", 0);
        opcodes[99] = new OpCode((byte) 0x63, "dadd", 0);
        opcodes[100] = new OpCode((byte) 0x64, "isub", 0);
        opcodes[101] = new OpCode((byte) 0x65, "lsub", 0);
        opcodes[102] = new OpCode((byte) 0x66, "fsub", 0);
        opcodes[103] = new OpCode((byte) 0x67, "dsub", 0);
        opcodes[104] = new OpCode((byte) 0x68, "imul", 0);
        opcodes[105] = new OpCode((byte) 0x69, "lmul", 0);
        opcodes[106] = new OpCode((byte) 0x6a, "fmul", 0);
        opcodes[107] = new OpCode((byte) 0x6b, "dmul", 0);
        opcodes[108] = new OpCode((byte) 0x6c, "idiv", 0);
        opcodes[109] = new OpCode((byte) 0x6d, "ldiv", 0);
        opcodes[110] = new OpCode((byte) 0x6e, "fdiv", 0);
        opcodes[111] = new OpCode((byte) 0x6f, "ddiv", 0);
        opcodes[112] = new OpCode((byte) 0x70, "irem", 0);
        opcodes[113] = new OpCode((byte) 0x71, "lrem", 0);
        opcodes[114] = new OpCode((byte) 0x72, "frem", 0);
        opcodes[115] = new OpCode((byte) 0x73, "drem", 0);
        opcodes[116] = new OpCode((byte) 0x74, "ineg", 0);
        opcodes[117] = new OpCode((byte) 0x75, "lneg", 0);
        opcodes[118] = new OpCode((byte) 0x76, "fneg", 0);
        opcodes[119] = new OpCode((byte) 0x77, "dneg", 0);
        opcodes[120] = new OpCode((byte) 0x78, "ishl", 0);
        opcodes[121] = new OpCode((byte) 0x79, "lshl", 0);
        opcodes[122] = new OpCode((byte) 0x7a, "ishr", 0);
        opcodes[123] = new OpCode((byte) 0x7b, "lshr", 0);
        opcodes[124] = new OpCode((byte) 0x7c, "iushr", 0);
        opcodes[125] = new OpCode((byte) 0x7d, "lushr", 0);
        opcodes[126] = new OpCode((byte) 0x7e, "iand", 0);
        opcodes[127] = new OpCode((byte) 0x7f, "land", 0);
        opcodes[128] = new OpCode((byte) 0x80, "ior", 0);
        opcodes[129] = new OpCode((byte) 0x81, "lor", 0);
        opcodes[130] = new OpCode((byte) 0x82, "ixor", 0);
        opcodes[131] = new OpCode((byte) 0x83, "lxor", 0);
        opcodes[132] = new OpCode((byte) 0x84, "iinc", 2);
        opcodes[133] = new OpCode((byte) 0x85, "i2l", 0);
        opcodes[134] = new OpCode((byte) 0x86, "i2f", 0);
        opcodes[135] = new OpCode((byte) 0x87, "i2d", 0);
        opcodes[136] = new OpCode((byte) 0x88, "l2i", 0);
        opcodes[137] = new OpCode((byte) 0x89, "l2f", 0);
        opcodes[138] = new OpCode((byte) 0x8a, "l2d", 0);
        opcodes[139] = new OpCode((byte) 0x8b, "f2i", 0);
        opcodes[140] = new OpCode((byte) 0x8c, "f2l", 0);
        opcodes[141] = new OpCode((byte) 0x8d, "f2d", 0);
        opcodes[142] = new OpCode((byte) 0x8e, "d2i", 0);
        opcodes[143] = new OpCode((byte) 0x8f, "d2l", 0);
        opcodes[144] = new OpCode((byte) 0x90, "d2f", 0);
        opcodes[145] = new OpCode((byte) 0x91, "i2b", 0);
        opcodes[146] = new OpCode((byte) 0x92, "i2c", 0);
        opcodes[147] = new OpCode((byte) 0x93, "i2s", 0);
        opcodes[148] = new OpCode((byte) 0x94, "lcmp", 0);
        opcodes[149] = new OpCode((byte) 0x95, "fcmpl", 0);
        opcodes[150] = new OpCode((byte) 0x96, "fcmpg", 0);
        opcodes[151] = new OpCode((byte) 0x97, "dcmpl", 0);
        opcodes[152] = new OpCode((byte) 0x98, "dcmpg", 0);
        opcodes[153] = new OpCode((byte) 0x99, "ifeq", 2);
        opcodes[154] = new OpCode((byte) 0x9a, "ifne", 2);
        opcodes[155] = new OpCode((byte) 0x9b, "iflt", 2);
        opcodes[156] = new OpCode((byte) 0x9c, "ifge", 2);
        opcodes[157] = new OpCode((byte) 0x9d, "ifgt", 2);
        opcodes[158] = new OpCode((byte) 0x9e, "ifle", 2);
        opcodes[159] = new OpCode((byte) 0x9f, "if_icmpeq", 2);
        opcodes[160] = new OpCode((byte) 0xa0, "if_icmpne", 2);
        opcodes[161] = new OpCode((byte) 0xa1, "if_icmplt", 2);
        opcodes[162] = new OpCode((byte) 0xa2, "if_icmpge", 2);
        opcodes[163] = new OpCode((byte) 0xa3, "if_icmpgt", 2);
        opcodes[164] = new OpCode((byte) 0xa4, "if_icmple", 2);
        opcodes[165] = new OpCode((byte) 0xa5, "if_acmpeq", 2);
        opcodes[166] = new OpCode((byte) 0xa6, "if_acmpne", 2);
        opcodes[167] = new OpCode((byte) 0xa7, "goto", 2);
        opcodes[168] = new OpCode((byte) 0xa8, "jsr", 2);
        opcodes[169] = new OpCode((byte) 0xa9, "ret", 1);
        opcodes[170] = new TableSwitch((byte) 0xaa, "tableswitch", -1);
        opcodes[171] = new LookupSwitch((byte) 0xab, "lookupswitch", -1);
        opcodes[172] = new OpCode((byte) 0xac, "ireturn", 0);
        opcodes[173] = new OpCode((byte) 0xad, "lreturn", 0);
        opcodes[174] = new OpCode((byte) 0xae, "freturn", 0);
        opcodes[175] = new OpCode((byte) 0xaf, "dreturn", 0);
        opcodes[176] = new OpCode((byte) 0xb0, "areturn", 0);
        opcodes[177] = new OpCode((byte) 0xb1, "return", 0);
        opcodes[178] = new OpCode((byte) 0xb2, "getstatic", 2);
        opcodes[179] = new OpCode((byte) 0xb3, "putstatic", 2);
        opcodes[180] = new OpCode((byte) 0xb4, "getfield", 2);
        opcodes[181] = new OpCode((byte) 0xb5, "putfield", 2);
        opcodes[182] = new OneIndexOpCode((byte) 0xb6, "invokevirtual", 2);
        opcodes[183] = new OneIndexOpCode((byte) 0xb7, "invokespecial", 2);
        opcodes[184] = new OneIndexOpCode((byte) 0xb8, "invokestatic", 2);
        opcodes[185] = new OpCode((byte) 0xb9, "invokeinterface", 4);
        opcodes[186] = new OpCode((byte) 0xba, "invokedynamic", 4);
        opcodes[187] = new OpCode((byte) 0xbb, "new", 2);
        opcodes[188] = new OpCode((byte) 0xbc, "newarray", 1);
        opcodes[189] = new OpCode((byte) 0xbd, "anewarray", 2);
        opcodes[190] = new OpCode((byte) 0xbe, "arraylength", 0);
        opcodes[191] = new OpCode((byte) 0xbf, "athrow", 0);
        opcodes[192] = new OpCode((byte) 0xc0, "checkcast", 2);
        opcodes[193] = new OpCode((byte) 0xc1, "instanceof", 2);
        opcodes[194] = new OpCode((byte) 0xc2, "monitorenter", 0);
        opcodes[195] = new OpCode((byte) 0xc3, "monitorexit", 0);
        opcodes[196] = new Wide((byte) 0xc4, "wide", -1);
        opcodes[197] = new OpCode((byte) 0xc5, "multianewarray", 3);
        opcodes[198] = new OpCode((byte) 0xc6, "ifnull", 2);
        opcodes[199] = new OpCode((byte) 0xc7, "ifnonnull", 2);
        opcodes[200] = new OpCode((byte) 0xc8, "goto_w", 4);
        opcodes[201] = new OpCode((byte) 0xc9, "jsr_w", 4);
        opcodes[202] = new OpCode((byte) 0xca, "breakpoint", -1); //reserved 
        opcodes[254] = new OpCode((byte) 0xfe, "impdep1", -1); //reserved 
        opcodes[255] = new OpCode((byte) 0xff, "impdep2", -1); //reserved 
    }

}
