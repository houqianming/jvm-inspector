package cn.hqm.jvm.classfile.attr;

import cn.hqm.jvm.classfile.AttributeInfo;
import cn.hqm.jvm.classfile.ClassFileParser;
import cn.hqm.jvm.classfile.ConstantInfo;
import cn.hqm.jvm.classfile.Displayer;


/**
 * Java 1.7 JVM Spec 4.7.2. The ConstantValue Attribute
 * 
 * LocalVariableTable_attribute {
 *     u2 attribute_name_index;
 *     u4 attribute_length;
 *     u2 local_variable_table_length;
 *     {   u2 start_pc;
 *         u2 length;
 *         u2 name_index;
 *         u2 descriptor_index;
 *         u2 index;
 *     } local_variable_table[local_variable_table_length];
 * }
 * 
 * @author linxuan
 * @date 2014-3-24 下午12:55:02
 */
public class LocalVariableTable extends AttributeInfo {
    public final LocalVariable[] localVariables;


    public LocalVariableTable(int attribute_name_index, byte[] info) {
        super(attribute_name_index, info);
        int[] offset = new int[] { 0 };
        int local_variable_table_length = ClassFileParser.getU2AndStepOffset(info, offset);
        localVariables = new LocalVariable[local_variable_table_length];
        for (int i = 0; i < local_variable_table_length; i++) {
            localVariables[i] = new LocalVariable(info, offset);
        }
    }


    @Override
    protected void displayInfo(Displayer displayer, byte[] b, int[] offset, ConstantInfo[] pool) {
        displayer.writeAndStepOffset(b, offset, 2, "local_variable_table_length:" + localVariables.length);
        displayer.spanBegin(4);
        for (int i = 0; i < localVariables.length; i++) {
            localVariables[i].displayInfo(displayer, b, offset, pool);
        }
        displayer.spanEnd();
    }

    private static class LocalVariable {
        public final int start_pc;
        public final int length;
        public final int name_index;
        public final int descriptor_index;
        public final int index;


        public LocalVariable(byte[] b, int[] offset) {
            this.start_pc = ClassFileParser.getU2AndStepOffset(b, offset);
            this.length = ClassFileParser.getU2AndStepOffset(b, offset);
            this.name_index = ClassFileParser.getU2AndStepOffset(b, offset);
            this.descriptor_index = ClassFileParser.getU2AndStepOffset(b, offset);
            this.index = ClassFileParser.getU2AndStepOffset(b, offset);
        }


        protected void displayInfo(Displayer displayer, byte[] b, int[] offset, ConstantInfo[] pool) {
            displayer.writeAndStepOffset(b, offset, 10, "start_pc:" + start_pc + ",length:" + length + ",name_index:#"
                    + name_index + "(" + pool[name_index].showContent(pool) + ")" + ",descriptor_index:#"
                    + descriptor_index + "(" + pool[descriptor_index].showContent(pool) + ")" + ",index:" + index);
        }
    }
}
