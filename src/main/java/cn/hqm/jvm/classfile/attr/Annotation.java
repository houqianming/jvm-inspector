package cn.hqm.jvm.classfile.attr;

import cn.hqm.jvm.classfile.ClassFileParser;
import cn.hqm.jvm.classfile.ConstantInfo;
import cn.hqm.jvm.classfile.Displayer;


/**
 * annotation {
 *     u2 type_index;
 *     u2 num_element_value_pairs;
 *     {   u2            element_name_index;
 *         element_value value;
 *     } element_value_pairs[num_element_value_pairs];
 * }
 * 
 * @author linxuan
 * @date 2014-3-25 上午8:10:11
 */
public class Annotation {
    public final int type_index;
    public final ElementValuePair[] element_value_pairs;


    public Annotation(byte[] b, int[] offset) {
        this.type_index = ClassFileParser.getU2AndStepOffset(b, offset);
        int num_element_value_pairs = ClassFileParser.getU2AndStepOffset(b, offset);
        this.element_value_pairs = new ElementValuePair[num_element_value_pairs];
        for (int i = 0; i < num_element_value_pairs; i++) {
            this.element_value_pairs[i] = new ElementValuePair(b, offset);
        }
    }

    private static class ElementValuePair {
        public final int element_name_index;
        public final AnnotationElementValue value;


        public ElementValuePair(byte[] b, int[] offset) {
            this.element_name_index = ClassFileParser.getU2AndStepOffset(b, offset);
            this.value = new AnnotationElementValue(b, offset);
        }


        void displayInfo(Displayer displayer, byte[] b, int[] offset, ConstantInfo[] pool) {
            displayer.writeAndStepOffset(b, offset, 2, "element_name_index:#" + element_name_index + // 
                    "(" + pool[element_name_index].showContent(pool) + ")");
            value.displayInfo(displayer, b, offset, pool);
        }
    }


    protected void displayInfo(Displayer displayer, byte[] b, int[] offset, ConstantInfo[] pool) {
        displayer.writeAndStepOffset(b, offset, 2,
            "type_index:" + type_index + "(" + pool[type_index].showContent(pool) + ")");
        displayer.writeAndStepOffset(b, offset, 2, "num_element_value_pairs:" + element_value_pairs.length);
        for (int i = 0; i < element_value_pairs.length; i++) {
            element_value_pairs[i].displayInfo(displayer, b, offset, pool);
        }
    }
}