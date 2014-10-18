package cn.hqm.jvm.classfile;

/**
 * Java 1.7 JVM Spec 4.1. The ClassFile Structure
 * 
 * ClassFile {
 *     u4             magic;
 *     u2             minor_version;
 *     u2             major_version;
 *     u2             constant_pool_count;
 *     cp_info        constant_pool[constant_pool_count-1];
 *     u2             access_flags;
 *     u2             this_class;
 *     u2             super_class;
 *     u2             interfaces_count;
 *     u2             interfaces[interfaces_count];
 *     u2             fields_count;
 *     field_info     fields[fields_count];
 *     u2             methods_count;
 *     method_info    methods[methods_count];
 *     u2             attributes_count;
 *     attribute_info attributes[attributes_count];
 * }
 * @author linxuan
 * @date 2014-3-21 上午7:24:13
 */
public class ClassFile {
    //public final int magic;
    public final int minor_version; //u2
    public final int major_version; //u2
    public final ConstantInfo[] constant_pool;
    public final int access_flags; //u2
    public final int this_class; //u2
    public final int super_class; //u2
    public final int[] interfaces;
    public final FieldInfo[] fields;
    public final MethodInfo[] methods;
    public final AttributeInfo[] attributes;


    public ClassFile(int minor_version, int major_version, ConstantInfo[] constant_pool, int access_flags,
            int this_class, int super_class, int[] interfaces, FieldInfo[] fields, MethodInfo[] methods,
            AttributeInfo[] attributes) {
        this.minor_version = minor_version;
        this.major_version = major_version;
        this.constant_pool = constant_pool;
        this.access_flags = access_flags;
        this.this_class = this_class;
        this.super_class = super_class;
        this.interfaces = interfaces;
        this.fields = fields;
        this.methods = methods;
        this.attributes = attributes;
    }


    public void display(Displayer displayer, byte[] b) {
        int[] offset = new int[] { 0 };
        displayer.writeAndStepOffset(b, offset, 4, "magic number");
        displayer.spanBegin(3);
        displayer.writeAndStepOffset(b, offset, 2, "minor_version:" + minor_version);
        displayer.writeAndStepOffset(b, offset, 2, "major_version:" + major_version);
        displayer.spanEnd();

        displayer.spanBegin(3);
        displayer.writeAndStepOffset(b, offset, 2, "constant_pool_count:" + constant_pool.length);
        for (int i = 1; i < constant_pool.length; i++) {
            if (constant_pool[i] == null) {
                //long和double的第二个entry；@see cn.hqm.jvm.classfile.ClassFileParser.parseConstantPool()
                continue;
            }
            displayer.spanBegin(2);
            constant_pool[i].display(displayer, b, offset, constant_pool);
            displayer.spanEnd();
        }
        displayer.spanEnd();

        displayer.writeAndStepOffset(b, offset, 2, "access_flags:" + access_flags);
        displayer.writeAndStepOffset(b, offset, 2, "this_class:#" + this_class + // 
                "(" + constant_pool[this_class].showContent(constant_pool) + ")");
        displayer.writeAndStepOffset(b, offset, 2, "super_class:#" + super_class + //
                "(" + constant_pool[super_class].showContent(constant_pool) + ")");

        //interfaces
        displayer.writeAndStepOffset(b, offset, 2, "interfaces_count:" + interfaces.length);
        StringBuilder sb = new StringBuilder("interfaces:");
        for (int i = 0; i < interfaces.length; i++) {
            sb.append("#").append(interfaces[i]).append(",");
        }
        displayer.writeAndStepOffset(b, offset, interfaces.length * 2, sb.toString());

        //fields
        displayer.spanBegin(3);
        displayer.writeAndStepOffset(b, offset, 2, "fields_count:" + fields.length);
        for (int i = 0; i < fields.length; i++) {
            displayer.spanBegin(2);
            fields[i].display(displayer, b, offset, constant_pool);
            displayer.spanEnd();
        }
        displayer.spanEnd();

        //methods
        displayer.spanBegin(3);
        displayer.writeAndStepOffset(b, offset, 2, "methods_count:" + methods.length);
        for (int i = 0; i < methods.length; i++) {
            displayer.spanBegin(2);
            methods[i].display(displayer, b, offset, constant_pool);
            displayer.spanEnd();
        }
        displayer.spanEnd();

        //attributes
        displayer.spanBegin(3);
        displayer.writeAndStepOffset(b, offset, 2, "attributes_count:" + attributes.length);
        for (int i = 0; i < attributes.length; i++) {
            displayer.spanBegin(2);
            attributes[i].display(displayer, b, offset, constant_pool);
            displayer.spanEnd();
        }
        displayer.spanEnd();

        //constant pool
        displayer.divBegin("cp");
        for (int i = 1; i < constant_pool.length; i++) {
            if (constant_pool[i] == null) {
                //long和double的第二个entry；@see cn.hqm.jvm.classfile.ClassFileParser.parseConstantPool()
                continue;
            }
            displayer.divBegin();
            displayer.write("#" + i + " ");
            displayer.write(constant_pool[i].showContent(constant_pool));
            displayer.divEnd();
        }
        displayer.divEnd();

    }
}
