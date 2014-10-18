package cn.hqm.jvm.classfile.attr;

import cn.hqm.jvm.classfile.AttributeInfo;
import cn.hqm.jvm.classfile.ClassFileParser;
import cn.hqm.jvm.classfile.ConstantInfo;
import cn.hqm.jvm.classfile.Displayer;


/**
 * Java 1.7 JVM Spec 4.7.2. The ConstantValue Attribute
 * 
 * Exceptions_attribute {
 *     u2 attribute_name_index;
 *     u4 attribute_length;
 *     u2 number_of_exceptions;
 *     u2 exception_index_table[number_of_exceptions];
 * }
 * 
 * @author linxuan
 * @date 2014-3-17 上午9:18:16
 */
public class ExceptionsAttribute extends AttributeInfo {
    public final int[] exceptions;


    public ExceptionsAttribute(int attribute_name_index, byte[] info) {
        super(attribute_name_index, info);
        int[] offset = new int[] { 0 };
        int number_of_exceptions = ClassFileParser.getU2AndStepOffset(info, offset);
        exceptions = new int[number_of_exceptions];
        for (int i = 0; i < number_of_exceptions; i++) {
            exceptions[i] = ClassFileParser.getU2AndStepOffset(info, offset);
        }
    }


    @Override
    protected void displayInfo(Displayer displayer, byte[] b, int[] offset, ConstantInfo[] pool) {
        displayer.spanBegin(4);
        displayer.writeAndStepOffset(b, offset, 2, "number_of_exceptions:" + exceptions.length);
        StringBuilder throwstrs = new StringBuilder("throws ");
        for (int i = 0; i < exceptions.length; i++) {
            int index = exceptions[i];
            throwstrs.append("#").append(index).append("(").append(pool[index].showContent(pool)).append("), ");
        }
        displayer.writeAndStepOffset(b, offset, exceptions.length * 2, throwstrs.toString());
        displayer.spanEnd();
    }
}
