package cn.hqm.jvm.classfile.attr;

import cn.hqm.jvm.classfile.AttributeInfo;
import cn.hqm.jvm.classfile.ClassFileParser;
import cn.hqm.jvm.classfile.ConstantInfo;
import cn.hqm.jvm.classfile.Displayer;


/**
 * Java 1.7 JVM Spec 4.7.2. The ConstantValue Attribute
 * 
 * LineNumberTable_attribute {
 *     u2 attribute_name_index;
 *     u4 attribute_length;
 *     u2 line_number_table_length;
 *     {   u2 start_pc;
 *         u2 line_number; 
 *     } line_number_table[line_number_table_length];
 * }
 * 
 * @author linxuan
 * @date 2014-3-24 下午12:55:02
 */
public class LineNumberTable extends AttributeInfo {
    public final int[][] line_number_table;


    public LineNumberTable(int attribute_name_index, byte[] info) {
        super(attribute_name_index, info);
        int[] offset = new int[] { 0 };
        int line_number_table_length = ClassFileParser.getU2AndStepOffset(info, offset);
        line_number_table = new int[line_number_table_length][2];
        for (int i = 0; i < line_number_table_length; i++) {
            int start_pc = ClassFileParser.getU2AndStepOffset(info, offset);
            int line_number = ClassFileParser.getU2AndStepOffset(info, offset);
            line_number_table[i] = new int[] { start_pc, line_number };
        }
    }


    @Override
    protected void displayInfo(Displayer displayer, byte[] b, int[] offset, ConstantInfo[] pool) {
        displayer.writeAndStepOffset(b, offset, 2, "line_number_table_length:" + line_number_table.length);
        displayer.spanBegin(4);
        for (int i = 0; i < line_number_table.length; i++) {
            int start_pc = line_number_table[i][0];
            int line_number = line_number_table[i][1];
            displayer.writeAndStepOffset(b, offset, 4, "start_pc:" + start_pc + ", line_number:" + line_number);
        }
        displayer.spanEnd();
    }
}
