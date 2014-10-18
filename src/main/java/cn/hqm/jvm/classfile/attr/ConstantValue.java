package cn.hqm.jvm.classfile.attr;

import cn.hqm.jvm.classfile.AttributeInfo;
import cn.hqm.jvm.classfile.ConstantInfo;


/**
 * Java 1.7 JVM Spec 4.7.2. The ConstantValue Attribute
 * 
ConstantValue_attribute {
    u2 attribute_name_index;
    u4 attribute_length;
    u2 constantvalue_index;
}
 * 
 * @author linxuan
 * @date 2014-3-17 上午9:18:16
 */
public class ConstantValue extends AttributeInfo {
    public final int constantvalue_index;


    public ConstantValue(int attribute_name_index, byte[] info) {
        super(attribute_name_index, info);
        if (this.info.length != 2) {
            throw new IllegalArgumentException(
                "The attribute_length of a ConstantValue_attribute structure must be 2. actrul:" + info.length);
        }
        this.constantvalue_index = ((info[0] & 0xff) << 8) | (info[1] & 0xff);
    }


    @Override
    protected String showInfo(ConstantInfo[] pool) {
        return "#" + constantvalue_index + "(" + pool[constantvalue_index].showContent(pool) + ")";
    }
}
