package cn.hqm.jvm.classfile.attr;

import cn.hqm.jvm.classfile.AttributeInfo;
import cn.hqm.jvm.classfile.ClassFileParser;
import cn.hqm.jvm.classfile.ConstantInfo;
import cn.hqm.jvm.classfile.Displayer;


/**
 * Java 1.7 JVM Spec 4.7.2. The ConstantValue Attribute
 * 
 * InnerClasses_attribute {
 *     u2 attribute_name_index;
 *     u4 attribute_length;
 *     u2 number_of_classes;
 *     {   u2 inner_class_info_index;
 *         u2 outer_class_info_index;
 *         u2 inner_name_index;
 *         u2 inner_class_access_flags;
 *     } classes[number_of_classes];
 * }
 * 
 * @author linxuan
 * @date 2014-3-27 上午7:56:03
 */
public class InnerClassesAttribute extends AttributeInfo {
    public final int[][] classes;


    public InnerClassesAttribute(int attribute_name_index, byte[] info) {
        super(attribute_name_index, info);
        int[] offset = new int[] { 0 };
        int number_of_classes = ClassFileParser.getU2AndStepOffset(info, offset);
        classes = new int[number_of_classes][2];
        for (int i = 0; i < number_of_classes; i++) {
            int inner_class_info_index = ClassFileParser.getU2AndStepOffset(info, offset);
            int outer_class_info_index = ClassFileParser.getU2AndStepOffset(info, offset);
            int inner_name_index = ClassFileParser.getU2AndStepOffset(info, offset);
            int inner_class_access_flags = ClassFileParser.getU2AndStepOffset(info, offset);
            classes[i] = new int[] { inner_class_info_index, outer_class_info_index,// 
                                    inner_name_index, inner_class_access_flags };
        }
    }


    @Override
    protected void displayInfo(Displayer displayer, byte[] b, int[] offset, ConstantInfo[] pool) {
        displayer.writeAndStepOffset(b, offset, 2, "number_of_classes:" + classes.length);
        for (int i = 0; i < classes.length; i++) {
            StringBuilder sb =
                    new StringBuilder().append("inner_class_info_index:#").append(classes[i][0]).append("(")
                        .append(pool[classes[i][0]].showContent(pool)).append(")").append(", outer_class_info_index:#")
                        .append(classes[i][1]);
            if (classes[i][1] != 0) {
                //0表示这个inner_class不是当前类的member class；也即inner_class是下面3种之一：
                //top-level class or interface (JLS §7.6) 
                //local class (JLS §14.3) 
                //anonymous class (JLS §15.9.5)
                sb.append("(").append(pool[classes[i][1]].showContent(pool)).append(")");
            }
            sb.append(", inner_name_index:#").append(classes[i][2]);
            if (classes[i][2] != 0) {
                //If C is anonymous (JLS §15.9.5), the value of the inner_name_index item must be zero.
                sb.append("(").append(pool[classes[i][2]].showContent(pool)).append(")");
            }
            sb.append(", inner_class_access_flags:").append(classes[i][3]);

            displayer.writeAndStepOffset(b, offset, 8, sb.toString());
        }
    }
}
