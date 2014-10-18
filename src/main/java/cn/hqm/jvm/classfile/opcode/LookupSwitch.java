package cn.hqm.jvm.classfile.opcode;

import cn.hqm.jvm.classfile.ClassFileParser;
import cn.hqm.jvm.classfile.ConstantInfo;
import cn.hqm.jvm.classfile.Displayer;
import cn.hqm.jvm.classfile.OpCode;


/**
 * 
 * Format
 *  lookupswitch
 *  <0-3 byte pad>
 *  defaultbyte1
 *  defaultbyte2
 *  defaultbyte3
 *  defaultbyte4
 *  npairs1
 *  npairs2
 *  npairs3
 *  npairs4
 *  match-offset pairs...
 * 
 * @author linxuan
 * @date 2014-3-23 下午5:25:28
 */
public class LookupSwitch extends OpCode {
    public LookupSwitch(byte opcode, String mnemonic, int paramlen) {
        super(opcode, mnemonic, paramlen);
    }


    @Override
    public void display(Displayer displayer, byte[] b, int[] offset, ConstantInfo[] pool, int codeIndex) {
        int index = offset[0];
        if (b[index] != this.opcode) {
            throw new IllegalStateException(b[index] + "not lookupswitch");
        }
        if (index <= codeIndex) {
            throw new IllegalStateException("opcode tableswitch's index <= codeIndex");
        }

        displayer.writeAndStepOffset(b, offset, 1, mnemonic);

        //padding
        int pad = 4 - ((index + 1 - codeIndex) % 4);
        if (pad < 4) {
            displayer.writeAndStepOffset(b, offset, pad, "4 bytes aligin padding");
        }

        int default_pc = ClassFileParser.getInt(b, offset[0]);
        displayer.writeAndStepOffset(b, offset, 4, "default_pc:" + default_pc);

        int pairs_count = ClassFileParser.getInt(b, offset[0]);
        displayer.writeAndStepOffset(b, offset, 4, "pairs_count:" + pairs_count);

        for (int i = 0; i < pairs_count; i++) {
            int casematch = ClassFileParser.getInt(b, offset[0]);
            int jumpoffset = ClassFileParser.getInt(b, offset[0] + 4); //相对于lookupswitch指令所在位置的offset
            displayer.writeAndStepOffset(b, offset, 8, "case " + casematch + ": " + jumpoffset);
        }
    }
}
