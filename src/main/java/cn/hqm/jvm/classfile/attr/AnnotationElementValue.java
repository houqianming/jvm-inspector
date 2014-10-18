package cn.hqm.jvm.classfile.attr;

import cn.hqm.jvm.classfile.ClassFileParser;
import cn.hqm.jvm.classfile.ConstantInfo;
import cn.hqm.jvm.classfile.Displayer;


/**
 * Java 1.7 JVM Spec 4.7.16.1. The element_value structure
element_value {
    u1 tag;
    union {
        u2 const_value_index;

        {   u2 type_name_index;
            u2 const_name_index;
        } enum_const_value;

        u2 class_info_index;

        annotation annotation_value;

        {   u2            num_values;
            element_value values[num_values];
        } array_value;
    } value;
}
 * @author linxuan
 * @date 2014-3-25 上午8:13:13
 */
public class AnnotationElementValue {
    public final int tag;
    public final Integer const_value_index;
    public final int[] enum_const_value;
    public final Integer class_info_index;
    public final Annotation annotation_value;
    public final AnnotationElementValue[] element_value;


    public AnnotationElementValue(byte[] b, int[] offset) {
        Integer const_value_index0 = null;
        int[] enum_const_value0 = null;
        Integer class_info_index0 = null;
        Annotation annotation_value0 = null;
        AnnotationElementValue[] element_value0 = null;

        /**
         *    union {
         *        u2 const_value_index;
         *
         *        {   u2 type_name_index;
         *            u2 const_name_index;
         *        } enum_const_value;
         *
         *        u2 class_info_index;
         *
         *        annotation annotation_value;
         *
         *        {   u2            num_values;
         *            element_value values[num_values];
         *        } array_value;
         *    } value;
         */
        this.tag = ClassFileParser.getU1AndStepOffset(b, offset);
        switch (this.tag) {
        case 'B':
        case 'C':
        case 'D':
        case 'F':
        case 'I':
        case 'J':
        case 'S':
        case 'Z':
        case 's':
            const_value_index0 = ClassFileParser.getU2AndStepOffset(b, offset);
            break;
        case 'e':
            int type_name_index = ClassFileParser.getU2AndStepOffset(b, offset);
            int const_name_index = ClassFileParser.getU2AndStepOffset(b, offset);
            enum_const_value0 = new int[] { type_name_index, const_name_index };
            break;
        case 'c':
            class_info_index0 = ClassFileParser.getU2AndStepOffset(b, offset);
            break;
        case '@':
            annotation_value0 = new Annotation(b, offset);
            break;
        case '[':
            int num_values = ClassFileParser.getU2AndStepOffset(b, offset);
            element_value0 = new AnnotationElementValue[num_values];
            for (int i = 0; i < num_values; i++) {
                element_value0[i] = new AnnotationElementValue(b, offset);
            }
            break;
        default:
            throw new IllegalArgumentException("Invalid AnnotationElementValue tag:" + this.tag);
        }

        this.const_value_index = const_value_index0;
        this.enum_const_value = enum_const_value0;
        this.class_info_index = class_info_index0;
        this.annotation_value = annotation_value0;
        this.element_value = element_value0;
    }


    protected void displayInfo(Displayer displayer, byte[] b, int[] offset, ConstantInfo[] pool) {
        displayer.writeAndStepOffset(b, offset, 1, "tag:" + (char) tag);
        switch (this.tag) {
        case 'B':
        case 'C':
        case 'D':
        case 'F':
        case 'I':
        case 'J':
        case 'S':
        case 'Z':
        case 's':
            displayer.writeAndStepOffset(b, offset, 2, showIndex("const_value_index", const_value_index, pool));
            break;
        case 'e':
            displayer.writeAndStepOffset(b, offset, 2, showIndex("type_name_index", enum_const_value[0], pool));
            displayer.writeAndStepOffset(b, offset, 2, showIndex("const_name_index", enum_const_value[1], pool));

            break;
        case 'c':
            displayer.writeAndStepOffset(b, offset, 2, showIndex("class_info_index", class_info_index, pool));

            break;
        case '@':
            annotation_value.displayInfo(displayer, b, offset, pool);
            break;
        case '[':
            displayer.writeAndStepOffset(b, offset, 2, "num_values:" + element_value.length);
            for (int i = 0; i < element_value.length; i++) {
                element_value[i].displayInfo(displayer, b, offset, pool);
            }
            break;
        default:
            throw new IllegalArgumentException("Invalid AnnotationElementValue tag:" + this.tag);
        }
    }


    private static String showIndex(String name, int index, ConstantInfo[] pool) {
        return name + ":#" + index + "(" + pool[index].showContent(pool) + ")";
    }
}
