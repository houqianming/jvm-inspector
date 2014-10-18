package cn.hqm.jvm.classfile.opcode;

import cn.hqm.jvm.classfile.ConstantInfo;
import cn.hqm.jvm.classfile.Displayer;
import cn.hqm.jvm.classfile.OpCode;


/**
 * wide: Extend local variable index by additional bytes
 * 
 * Format 1
 *   wide
 *   <opcode>
 *   indexbyte1
 *   indexbyte2
 *   where <opcode> is one of iload, fload, aload, lload, dload, istore, fstore, astore, lstore, dstore, or ret
 * 
 * Format 2
 *   wide
 *   iinc
 *   indexbyte1
 *   indexbyte2
 *   constbyte1
 *   constbyte2
 * @author linxuan
 * @date 2014-3-31 上午11:25:03
 */
public class Wide extends OpCode {
    public Wide(byte opcode, String mnemonic, int paramlen) {
        super(opcode, mnemonic, paramlen);
    }


    @Override
    public void display(Displayer displayer, byte[] b, int[] offset, ConstantInfo[] pool, int codeIndex) {
        int index = offset[0];
        if (b[index] != this.opcode) {
            throw new IllegalStateException(b[index] + "not wide");
        }

        OpCode targetOpcode = OpCode.opcodes[b[index + 1] & 0xff];
        if (targetOpcode.mnemonic != "iinc") {
            int local = ((b[index + 2] & 0xff) << 8) | b[index + 3];
            displayer.writeAndStepOffset(b, offset, 4, mnemonic + " " + targetOpcode.mnemonic + " " + local);
        }
        else {
            int local = ((b[index + 2] & 0xff) << 8) | b[index + 3];
            int incrn = ((b[index + 4] & 0xff) << 8) | b[index + 5];
            displayer.writeAndStepOffset(b, offset, 6, mnemonic + " iinc " + local + " " + incrn);
        }
    }
}
