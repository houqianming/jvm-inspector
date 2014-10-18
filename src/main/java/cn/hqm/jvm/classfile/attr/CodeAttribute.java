package cn.hqm.jvm.classfile.attr;

import cn.hqm.jvm.classfile.AttributeInfo;
import cn.hqm.jvm.classfile.ClassFileParser;
import cn.hqm.jvm.classfile.ConstantInfo;
import cn.hqm.jvm.classfile.Displayer;
import cn.hqm.jvm.classfile.OpCode;


/**
 * Java 1.7 JVM Spec 4.7.3. The Code Attribute
 * 
Code_attribute {
    u2 attribute_name_index;
    u4 attribute_length;
    u2 max_stack;
    u2 max_locals;
    u4 code_length;
    u1 code[code_length];
    u2 exception_table_length;
    {   u2 start_pc;
        u2 end_pc;
        u2 handler_pc;
        u2 catch_type;
    } exception_table[exception_table_length];
    u2 attributes_count;
    attribute_info attributes[attributes_count];
}
 * 
 * @author linxuan
 * @date 2014-3-17 上午9:18:16
 */
public class CodeAttribute extends AttributeInfo {
    public final int max_stack;
    public final int max_locals;
    public final byte[] code;
    public final int[][] exception_tables;
    public final AttributeInfo[] attributes;


    public CodeAttribute(int attribute_name_index, byte[] info, ConstantInfo[] pool) {
        super(attribute_name_index, info);
        int[] offset = new int[] { 0 };
        this.max_stack = ClassFileParser.getU2AndStepOffset(info, offset);
        this.max_locals = ClassFileParser.getU2AndStepOffset(info, offset);
        int code_length = ClassFileParser.getIntAndStepOffset(info, offset);
        this.code = new byte[code_length];
        //从max_stack max_locals code_length共8个字节之后开始copy
        System.arraycopy(info, 8, this.code, 0, code_length);
        offset[0] = offset[0] + code_length;

        //exception_tables
        int exception_table_length = ClassFileParser.getU2AndStepOffset(info, offset);
        this.exception_tables = new int[exception_table_length][4];
        for (int i = 0; i < exception_table_length; i++) {
            int start_pc = ClassFileParser.getU2AndStepOffset(info, offset);
            int end_pc = ClassFileParser.getU2AndStepOffset(info, offset);
            int handler_pc = ClassFileParser.getU2AndStepOffset(info, offset);
            int catch_type = ClassFileParser.getU2AndStepOffset(info, offset);
            exception_tables[i] = new int[] { start_pc, end_pc, handler_pc, catch_type };
        }

        /**
         * The only attributes defined by this specification as appearing in the attributes table 
         * of a Code attribute are the LineNumberTable (§4.7.12), LocalVariableTable (§4.7.13), 
         * LocalVariableTypeTable (§4.7.14), and StackMapTable (§4.7.4) attributes.
         */
        int attributes_count = ClassFileParser.getU2AndStepOffset(info, offset);
        this.attributes = ClassFileParser.parseAtributes(info, offset, attributes_count, pool);
    }


    @Override
    protected void displayInfo(Displayer displayer, byte[] b, int[] offset, ConstantInfo[] pool) {
        //displayer.writeAndStepOffset(b, offset, info.length, "info:" + showInfo(pool));
        displayer.writeAndStepOffset(b, offset, 2, "max_stack:" + max_stack);
        displayer.writeAndStepOffset(b, offset, 2, "max_locals:" + max_locals);

        //code
        displayer.writeAndStepOffset(b, offset, 4, "code_length:" + code.length);
        displayCode(displayer, b, offset, pool);

        //exception_tables
        displayer.writeAndStepOffset(b, offset, 2, "exception_table_length:" + exception_tables.length);
        displayExecptionTables(displayer, b, offset, pool);

        //attributes
        displayer.writeAndStepOffset(b, offset, 2, "attributes_count:" + attributes.length);
        for (int i = 0; i < attributes.length; i++) {
            attributes[i].display(displayer, b, offset, pool);
        }
    }


    private void displayCode(Displayer displayer, byte[] b, int[] offset, ConstantInfo[] pool) {
        //displayer.writeAndStepOffset(b, offset, code.length, "code:" + Arrays.toString(code));
        displayer.spanBegin(4);
        int codeIndex1 = offset[0];
        int codeIndex2 = codeIndex1 + code.length;
        while (offset[0] < codeIndex2) {
            int opcode = b[offset[0]] & 0xff;
            OpCode oc = OpCode.opcodes[opcode];
            oc.display(displayer, b, offset, pool, codeIndex1);
        }
        displayer.spanEnd();
    }


    private void displayExecptionTables(Displayer displayer, byte[] b, int[] offset, ConstantInfo[] pool) {
        displayer.spanBegin(4);
        for (int i = 0; i < exception_tables.length; i++) {
            int[] exception = exception_tables[i];
            displayer.writeAndStepOffset(b, offset, 8, "start_pc:" + exception[0] + ", end_pc:" + exception[1]
                    + ", handler_pc:" + exception[2] + ", catch_type:" + exception[3]);
        }
        displayer.spanEnd();
    }
}
