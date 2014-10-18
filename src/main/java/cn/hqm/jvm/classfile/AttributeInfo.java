package cn.hqm.jvm.classfile;

import cn.hqm.jvm.classfile.ConstantInfo.ConstantTag;
import cn.hqm.jvm.classfile.attr.BootstrapMethodsAttribute;
import cn.hqm.jvm.classfile.attr.CodeAttribute;
import cn.hqm.jvm.classfile.attr.ConstantValue;
import cn.hqm.jvm.classfile.attr.ExceptionsAttribute;
import cn.hqm.jvm.classfile.attr.InnerClassesAttribute;
import cn.hqm.jvm.classfile.attr.LineNumberTable;
import cn.hqm.jvm.classfile.attr.LocalVariableTable;
import cn.hqm.jvm.classfile.attr.RuntimeAnnotation;
import cn.hqm.jvm.classfile.attr.RuntimeParameterAnnotation;
import cn.hqm.jvm.classfile.attr.SourceFileAttribute;
import cn.hqm.jvm.classfile.attr.StackMapTable;


/**
 * Java 1.7 JVM Spec 4.7. Attributes
 * 
 * attribute_info {
 *     u2 attribute_name_index;
 *     u4 attribute_length;
 *     u1 info[attribute_length];
 * }
 * 
 * @author linxuan
 * @date 2014-3-16 下午9:30:34
 */
public class AttributeInfo implements Displayable {
    protected final int attribute_name_index;
    protected final byte[] info;


    public AttributeInfo(int attribute_name_index, byte[] info) {
        this.attribute_name_index = attribute_name_index;
        this.info = info;
    }


    public void display(Displayer displayer, byte[] b, int[] offset, ConstantInfo[] pool) {
        //displayer.spanBegin(5);
        displayer.writeAndStepOffset(b, offset, 2, "attribute_name_index:#" + attribute_name_index + // 
                "(" + pool[attribute_name_index].showContent(pool) + ")");
        displayer.writeAndStepOffset(b, offset, 4, "attribute_length:" + info.length);
        //displayer.spanEnd();
        displayInfo(displayer, b, offset, pool);
    }


    protected void displayInfo(Displayer displayer, byte[] b, int[] offset, ConstantInfo[] pool) {
        displayer.writeAndStepOffset(b, offset, info.length, "info:" + showInfo(pool));
    }


    protected String showInfo(ConstantInfo[] pool) {
        return "Unanalysised";
    }


    /**
     * Factory方法
     * @param attribute_name_index
     * @param pool
     * @param info
     * @return 一个新创建的AttributeInfo类或子类对象
     */
    public static AttributeInfo newAttributeInfo(ConstantInfo[] pool, int attribute_name_index, byte[] info) {
        if (pool[attribute_name_index].tag != ConstantTag.CONSTANT_Utf8) {
            throw new IllegalArgumentException(
                "The constant_pool entry at attribute_name_index must be a CONSTANT_Utf8_info structure (§4.4.7) representing the name of the attribute");
        }
        String attribute_name = (String) ((Object[]) pool[attribute_name_index].value)[1];
        if ("ConstantValue".equals(attribute_name)) {
            return new ConstantValue(attribute_name_index, info);
        }
        else if ("Code".equals(attribute_name)) {
            return new CodeAttribute(attribute_name_index, info, pool);
        }
        else if ("StackMapTable".equals(attribute_name)) {
            return new StackMapTable(attribute_name_index, info);
        }
        else if ("Exceptions".equals(attribute_name)) {
            return new ExceptionsAttribute(attribute_name_index, info);
        }
        else if ("InnerClasses".equals(attribute_name)) {
            return new InnerClassesAttribute(attribute_name_index, info);
        }
        else if ("LineNumberTable".equals(attribute_name)) {
            return new LineNumberTable(attribute_name_index, info);
        }
        else if ("LocalVariableTable".equals(attribute_name)) {
            return new LocalVariableTable(attribute_name_index, info);
        }
        else if ("SourceFile".equals(attribute_name)) {
            return new SourceFileAttribute(attribute_name_index, info);
        }
        else if ("RuntimeVisibleAnnotations".equals(attribute_name)) {
            return new RuntimeAnnotation(attribute_name_index, info);
        }
        else if ("RuntimeInvisibleAnnotations".equals(attribute_name)) {
            return new RuntimeAnnotation(attribute_name_index, info);
        }
        else if ("RuntimeVisibleParameterAnnotations".equals(attribute_name)) {
            return new RuntimeParameterAnnotation(attribute_name_index, info);
        }
        else if ("RuntimeInvisibleParameterAnnotations".equals(attribute_name)) {
            return new RuntimeParameterAnnotation(attribute_name_index, info);
        }
        else if ("BootstrapMethods".equals(attribute_name)) {
            return new BootstrapMethodsAttribute(attribute_name_index, info);
        }
        else {
            return new AttributeInfo(attribute_name_index, info);
        }
    }
}
