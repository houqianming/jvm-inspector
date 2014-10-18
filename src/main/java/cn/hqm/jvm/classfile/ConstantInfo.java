package cn.hqm.jvm.classfile;

/**
 * Java 1.7 JVM Spec 4.4. The Constant Pool
 *
 * cp_info {
 *     u1 tag;
 *     u1 info[];
 * }
 * 
 * @author linxuan
 * @date 2014-3-21 上午8:59:01
 */
public class ConstantInfo implements Displayable {
    public static enum ConstantTag {
        CONSTANT_Utf8(1),
        CONSTANT_Integer(3),
        CONSTANT_Float(4),
        CONSTANT_Long(5),
        CONSTANT_Double(6),
        CONSTANT_Class(7),
        CONSTANT_String(8),
        CONSTANT_Fieldref(9),
        CONSTANT_Methodref(10),
        CONSTANT_InterfaceMethodref(11),
        CONSTANT_NameAndType(12),
        CONSTANT_MethodHandle(15),
        CONSTANT_MethodType(16),
        CONSTANT_InvokeDynamic(18);

        private int value;


        private ConstantTag(int i) {
            this.value = i;
        }


        public int value() {
            return value;
        }


        public static ConstantTag valueOf(int i) {
            for (ConstantTag tag : values()) {
                if (tag.value == i) {
                    return tag;
                }
            }
            throw new IllegalArgumentException("Invalid constant tag:" + i);
        }
    }

    public final ConstantTag tag;
    //public final int length;
    //public final int[] info;
    public final Object value;


    public ConstantInfo(int tag, Object value) {
        this.tag = ConstantTag.valueOf(tag);
        //this.length = length;
        //this.info = info;
        this.value = value;
    }


    public void display(Displayer displayer, byte[] b, int[] offset, ConstantInfo[] pool) {
        displayer.writeAndStepOffset(b, offset, 1, tag.name());
        switch (tag) {
        case CONSTANT_Utf8:
            /*
            CONSTANT_Utf8_info {
                u1 tag;
                u2 length;
                u1 bytes[length];
            }
            */
            Object[] len_str = (Object[]) value;
            displayer.writeAndStepOffset(b, offset, 2, "length:" + len_str[0]);
            displayer.writeAndStepOffset(b, offset, (Integer) len_str[0], (String) len_str[1]);
            break;
        case CONSTANT_Integer:
        case CONSTANT_Float:
            displayer.writeAndStepOffset(b, offset, 4, showContent(pool));
            break;
        case CONSTANT_Long:
        case CONSTANT_Double:
            displayer.writeAndStepOffset(b, offset, 8, showContent(pool));
            break;
        case CONSTANT_Class:
        case CONSTANT_String:
        case CONSTANT_MethodType:
            /*
            CONSTANT_Class_info {
                u1 tag;
                u2 name_index;
            }
            CONSTANT_String_info {
                u1 tag;
                u2 string_index;
            }
            CONSTANT_MethodType_info {
                u1 tag;
                u2 descriptor_index;
            }
            */
            displayer.writeAndStepOffset(b, offset, 2, showContent(pool));
            break;
        case CONSTANT_Fieldref:
        case CONSTANT_Methodref:
        case CONSTANT_InterfaceMethodref: {
            /*
            CONSTANT_Fieldref_info/CONSTANT_Methodref_info/CONSTANT_InterfaceMethodref {
                u1 tag;
                u2 class_index;
                u2 name_and_type_index;
            }
            */
            int[] index = (int[]) value;
            displayer.writeAndStepOffset(b, offset, 2,
                "class_index:#" + index[0] + "(" + pool[index[0]].showContent(pool) + ")");
            displayer.writeAndStepOffset(b, offset, 2,
                "name_and_type_index:#" + index[1] + "(" + pool[index[1]].showContent(pool) + ")");
            break;
        }
        case CONSTANT_NameAndType: {
            /*
            CONSTANT_NameAndType_info {
                u1 tag;
                u2 name_index;
                u2 descriptor_index;
            }
            */
            int[] index = (int[]) value;
            displayer.writeAndStepOffset(b, offset, 2,
                "name_index:#" + index[0] + "(" + pool[index[0]].showContent(pool) + ")");
            displayer.writeAndStepOffset(b, offset, 2,
                "descriptor_index:#" + index[1] + "(" + pool[index[1]].showContent(pool) + ")");
            break;
        }
        case CONSTANT_MethodHandle: {
            /*
            CONSTANT_MethodHandle_info {
                u1 tag;
                u1 reference_kind;
                u2 reference_index;
            }
            */
            int[] index = (int[]) value;
            displayer.writeAndStepOffset(b, offset, 1, "reference_kind:" + index[0]);
            displayer.writeAndStepOffset(b, offset, 2,
                "reference_index:#" + index[1] + "(" + pool[index[1]].showContent(pool) + ")");
            break;
        }
        case CONSTANT_InvokeDynamic: {
            /*
            CONSTANT_InvokeDynamic_info {
                u1 tag;
                u2 bootstrap_method_attr_index;
                u2 name_and_type_index;
            }
            */
            int[] index = (int[]) value;
            displayer.writeAndStepOffset(b, offset, 2, "bootstrap_method_attr_index:#" + index[0] + "("
                    + "bootstrap_method" + ")");
            displayer.writeAndStepOffset(b, offset, 2,
                "name_and_type_index:#" + index[1] + "(" + pool[index[1]].showContent(pool) + ")");
            break;
        }
        default:
            throw new IllegalStateException("Unconsidered tag:" + tag);
        }
    }


    public String showContent(ConstantInfo[] pool) {
        switch (tag) {
        case CONSTANT_Utf8:
            Object[] len_str = (Object[]) value;
            return (String) len_str[1];
        case CONSTANT_Integer:
        case CONSTANT_Float:
        case CONSTANT_Long:
        case CONSTANT_Double:
            return value.toString();
        case CONSTANT_Class:
        case CONSTANT_String:
        case CONSTANT_MethodType: {
            /*
            CONSTANT_Class_info {
                u1 tag;
                u2 name_index;
            }
            CONSTANT_String_info {
                u1 tag;
                u2 string_index;
            }
            CONSTANT_MethodType_info {
                u1 tag;
                u2 descriptor_index;
            }
            */
            int index = (Integer) value;
            return tag + ":#" + index + "(" + pool[index].showContent(pool) + ")";
        }
        case CONSTANT_Fieldref:
        case CONSTANT_Methodref:
        case CONSTANT_InterfaceMethodref: {
            /*
            CONSTANT_Fieldref_info/CONSTANT_Methodref_info/CONSTANT_InterfaceMethodref {
                u1 tag;
                u2 class_index;
                u2 name_and_type_index;
            }
            */
            int[] index = (int[]) value;
            //displayer.writeAndStepOffset(b, offset, 2, "class_index:#" + index[0]);
            //displayer.writeAndStepOffset(b, offset, 2, "name_and_type_index:#" + index[1]);
            return "class:" + pool[index[0]].showContent(pool) + "," + // 
                    "name_and_type:" + pool[index[1]].showContent(pool);
        }
        case CONSTANT_NameAndType: {
            /*
            CONSTANT_NameAndType_info {
                u1 tag;
                u2 name_index;
                u2 descriptor_index;
            }
            */
            int[] index = (int[]) value;
            //displayer.writeAndStepOffset(b, offset, 2, "name_index:#" + index[0]);
            //displayer.writeAndStepOffset(b, offset, 2, "descriptor_index:#" + index[1]);
            return "name:" + pool[index[0]].showContent(pool) + "," + // 
                    "descriptor:" + pool[index[1]].showContent(pool);
        }
        case CONSTANT_MethodHandle: {
            /*
            CONSTANT_MethodHandle_info {
                u1 tag;
                u1 reference_kind;
                u2 reference_index;
            }
            */
            int[] index = (int[]) value;
            //displayer.writeAndStepOffset(b, offset, 1, "reference_kind:" + index[0]);
            //displayer.writeAndStepOffset(b, offset, 2, "reference_index:#" + index[1]);
            return "reference_kind:" + index[0] + "," + // 
                    "descriptor:" + pool[index[1]].showContent(pool);
        }
        case CONSTANT_InvokeDynamic: {
            /*
            CONSTANT_InvokeDynamic_info {
                u1 tag;
                u2 bootstrap_method_attr_index;
                u2 name_and_type_index;
            }
            */
            int[] index = (int[]) value;
            //displayer.writeAndStepOffset(b, offset, 2, "bootstrap_method_attr_index:#" + index[0]);
            //displayer.writeAndStepOffset(b, offset, 2, "name_and_type_index:#" + index[1]);
            return "bootstrap_method_attr_index:" + index[0] + "," + // 
                    "name_and_type:" + pool[index[1]].showContent(pool);
        }
        default:
            throw new IllegalStateException("Unconsidered tag:" + tag);
        }
    }


    public static String showIndex(String name, int index, ConstantInfo[] pool) {
        return name + ":#" + index + "(" + pool[index].showContent(pool) + ")";
    }
}
