package cn.hqm.jvm.classfile;

/**
 * Java 1.7 JVM Spec 4.6. Methods
 * 
 * method_info {
 *     u2             access_flags;
 *     u2             name_index;
 *     u2             descriptor_index;
 *     u2             attributes_count;
 *     attribute_info attributes[attributes_count];
 * }
 * 
 * @author linxuan
 * @date 2014-3-21 上午7:07:44
 */
public class MethodInfo implements Displayable {
    public final int access_flags;
    public final int name_index;
    public final int descriptor_index;
    public final AttributeInfo[] attributes;


    public void display(Displayer displayer, byte[] b, int[] offset, ConstantInfo[] pool) {
        displayer.writeAndStepOffset(b, offset, 2, "access_flags:" + access_flags);
        displayer.writeAndStepOffset(b, offset, 2, "name_index:#" + name_index + //
                "(" + pool[name_index].showContent(pool) + ")");
        displayer.writeAndStepOffset(b, offset, 2, "descriptor_index:#" + descriptor_index + //
                "(" + pool[descriptor_index].showContent(pool) + ")");
        displayer.writeAndStepOffset(b, offset, 2, "attributes_count:" + attributes.length);
        for (int i = 0; i < attributes.length; i++) {
            attributes[i].display(displayer, b, offset, pool);
        }
    }


    public MethodInfo(int access_flags, int name_index, int descriptor_index, AttributeInfo[] attributes) {
        this.access_flags = access_flags;
        this.name_index = name_index;
        this.descriptor_index = descriptor_index;
        this.attributes = attributes;
    }

}
