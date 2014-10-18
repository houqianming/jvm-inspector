package cn.hqm.jvm.classfile.opcode;

import cn.hqm.jvm.classfile.ClassFileParser;
import cn.hqm.jvm.classfile.ConstantInfo;
import cn.hqm.jvm.classfile.Displayer;
import cn.hqm.jvm.classfile.OpCode;


/**
 * 
 * @author linxuan
 * @date 2014-3-23 下午5:25:28
 */
public class TableSwitch extends OpCode {
    public TableSwitch(byte opcode, String mnemonic, int paramlen) {
        super(opcode, mnemonic, paramlen);
    }


    @Override
    public void display(Displayer displayer, byte[] b, int[] offset, ConstantInfo[] pool, int codeIndex) {
        int index = offset[0];
        if (b[index] != this.opcode) {
            throw new IllegalStateException(b[index] + "not tableswitch");
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

        int case_low = ClassFileParser.getInt(b, offset[0]);
        displayer.writeAndStepOffset(b, offset, 4, "low_index:" + case_low);

        int case_high = ClassFileParser.getInt(b, offset[0]);
        displayer.writeAndStepOffset(b, offset, 4, "high_index:" + case_high);

        int case_count = case_high - case_low + 1;
        for (int i = 0; i < case_count; i++) {
            int casejump = ClassFileParser.getInt(b, offset[0]);
            displayer.writeAndStepOffset(b, offset, 4, "case " + (case_low + i) + ": " + casejump);
        }
    }
}
