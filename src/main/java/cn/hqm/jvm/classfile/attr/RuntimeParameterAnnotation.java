package cn.hqm.jvm.classfile.attr;

import cn.hqm.jvm.classfile.AttributeInfo;
import cn.hqm.jvm.classfile.ClassFileParser;
import cn.hqm.jvm.classfile.ConstantInfo;
import cn.hqm.jvm.classfile.Displayer;


/**
 * Java 1.7 JVM Spec 
 *     4.7.18. The RuntimeVisibleParameterAnnotations attribute
 *     4.7.19. The RuntimeInvisibleParameterAnnotations attribute
 * 
 * RuntimeVisibleParameterAnnotations_attribute {
 *     u2 attribute_name_index;
 *     u4 attribute_length;
 *     u1 num_parameters;
 *     {   u2         num_annotations;
 *         annotation annotations[num_annotations];
 *     } parameter_annotations[num_parameters];
 * } 

 * RuntimeInvisibleParameterAnnotations_attribute {
 *     u2 attribute_name_index;
 *     u4 attribute_length;
 *     u1 num_parameters;
 *     {   u2         num_annotations;
 *         annotation annotations[num_annotations];
 *     } parameter_annotations[num_parameters];
 * }
 * 
 * Used in parameter, at most one
 * 
 * @author linxuan
 * @date 2014-3-25 下午1:46:34
 */
public class RuntimeParameterAnnotation extends AttributeInfo {
    public final Annotation[][] annotations;


    public RuntimeParameterAnnotation(int attribute_name_index, byte[] info) {
        super(attribute_name_index, info);
        int[] offset = new int[] { 0 };
        int num_parameters = ClassFileParser.getU1AndStepOffset(info, offset);
        annotations = new Annotation[num_parameters][];
        for (int i = 0; i < num_parameters; i++) {
            int num_annotations = ClassFileParser.getU2AndStepOffset(info, offset);
            annotations[i] = new Annotation[num_annotations];
            for (int j = 0; j < num_annotations; j++) {
                annotations[i][j] = new Annotation(info, offset);
            }
        }
    }


    @Override
    protected void displayInfo(Displayer displayer, byte[] b, int[] offset, ConstantInfo[] pool) {
        displayer.writeAndStepOffset(b, offset, 1, "num_parameters:" + annotations.length);
        for (int i = 0; i < annotations.length; i++) {
            displayer.spanBegin(4);
            displayer.writeAndStepOffset(b, offset, 2, "num_annotations:" + annotations[i].length);
            for (int j = 0; j < annotations[i].length; j++) {
                annotations[i][j].displayInfo(displayer, b, offset, pool);
            }
            displayer.spanEnd();
        }
    }

}
