package cn.hqm.jvm.classfile.attr;

import cn.hqm.jvm.classfile.AttributeInfo;
import cn.hqm.jvm.classfile.ClassFileParser;
import cn.hqm.jvm.classfile.ConstantInfo;
import cn.hqm.jvm.classfile.Displayer;


/**
 * Java 1.7 JVM Spec 4.7.2. The ConstantValue Attribute
 * 
 * SourceFile_attribute {
 *     u2 attribute_name_index;
 *     u4 attribute_length;
 *     u2 sourcefile_index;
 * }
 * 
 * @author linxuan
 * @date 2014-3-27 上午7:27:07
 */
public class SourceFileAttribute extends AttributeInfo {
    public final int sourcefile_index;


    public SourceFileAttribute(int attribute_name_index, byte[] info) {
        super(attribute_name_index, info);
        int[] offset = new int[] { 0 };
        sourcefile_index = ClassFileParser.getU2AndStepOffset(info, offset);
    }


    @Override
    protected void displayInfo(Displayer displayer, byte[] b, int[] offset, ConstantInfo[] pool) {
        displayer.writeAndStepOffset(b, offset, 2, "sourcefile_index:#" + sourcefile_index + "("
                + pool[sourcefile_index].showContent(pool) + ")");
    }
}
