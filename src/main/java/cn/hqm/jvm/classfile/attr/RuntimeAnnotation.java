package cn.hqm.jvm.classfile.attr;

import cn.hqm.jvm.classfile.AttributeInfo;
import cn.hqm.jvm.classfile.ClassFileParser;
import cn.hqm.jvm.classfile.ConstantInfo;
import cn.hqm.jvm.classfile.Displayer;


/**
 * Java 1.7 JVM Spec 
 *     4.7.16. The RuntimeVisibleAnnotations attribute
 *     4.7.17. The RuntimeInvisibleAnnotations attribute
 * 
 * RuntimeVisibleAnnotations_attribute {
 *     u2         attribute_name_index;
 *     u4         attribute_length;
 *     u2         num_annotations;
 *     annotation annotations[num_annotations];
 * }
 * 
 * RuntimeInvisibleAnnotations_attribute {
 *     u2         attribute_name_index;
 *     u4         attribute_length;
 *     u2         num_annotations;
 *     annotation annotations[num_annotations];
 * }
 * 
 * annotation {
 *     u2 type_index;
 *     u2 num_element_value_pairs;
 *     {   u2            element_name_index;
 *         element_value value;
 *     } element_value_pairs[num_element_value_pairs];
 * }
 * 
 * Used in ClassFile, field_info, or method_info, at most one
 * 
 * @author linxuan
 * @date 2014-3-25 上午8:00:10
 */
public class RuntimeAnnotation extends AttributeInfo {
    public final Annotation[] annotations;


    public RuntimeAnnotation(int attribute_name_index, byte[] info) {
        super(attribute_name_index, info);
        int[] offset = new int[] { 0 };
        int num_annotations = ClassFileParser.getU2AndStepOffset(info, offset);
        annotations = new Annotation[num_annotations];
        for (int i = 0; i < num_annotations; i++) {
            annotations[i] = new Annotation(info, offset);
        }
    }


    @Override
    protected void displayInfo(Displayer displayer, byte[] b, int[] offset, ConstantInfo[] pool) {
        displayer.writeAndStepOffset(b, offset, 2, "num_annotations:" + annotations.length);
        displayer.spanBegin(4);
        for (int i = 0; i < annotations.length; i++) {
            annotations[i].displayInfo(displayer, b, offset, pool);
        }
        displayer.spanEnd();
    }

}
