package cn.hqm.jvm.classfile;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;


/**
 * 按Class File Format解析java class文件/byte[]
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
 * 
 * @author linxuan
 * @date 2014-3-16 下午9:39:51
 */
public class ClassFileParser {
    private ConstantInfo[] constantPool;
    private static byte[] magic = new byte[] { (byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE };


    public ClassFile parse(byte[] b, Writer writer) {
        //u4             magic;
        if (b.length < 24) {
            throw new IllegalArgumentException("Too small class file. less than 24");
        }
        if (b[0] != magic[0] || b[1] != magic[1] || b[2] != magic[2] || b[3] != magic[3]) {
            throw new IllegalArgumentException("Wrong magic number");
        }
        //displayer.write(b, 0, 4, "magic number");

        //u2             minor_version;
        //u2             major_version;
        int[] offset = new int[] { 4 };
        int minor_version = getU2AndStepOffset(b, offset); //(b[4] & 0xff) << 8 | b[5];
        int major_version = getU2AndStepOffset(b, offset); //(b[6] & 0xff) << 8 | b[7];
        //displayer.write(b, 4, 2, "minor_version:" + minor_version);
        //displayer.write(b, 6, 2, "major_version:" + major_version);

        //u2             constant_pool_count;
        //cp_info        constant_pool[constant_pool_count-1];
        int constant_pool_count = getU2AndStepOffset(b, offset); //(b[8] & 0xff) << 8 | b[9];
        //displayer.write(b, 8, 2, "constant_pool_count:" + constant_pool_count);
        //int[] cpstart = new int[] { offset[0] };
        constantPool = parseConstantPool(b, offset, constant_pool_count);
        //for (int i = 1; i < constantPool.length; i++) {
        //    constantPool[i].display(displayer, b, cpstart);
        //}

        int access_flags = getU2AndStepOffset(b, offset); //u2 access_flags;
        int this_class = getU2AndStepOffset(b, offset); //u2 this_class;
        int super_class = getU2AndStepOffset(b, offset); //u2 super_class;

        //u2 interfaces_count;
        //u2 interfaces[interfaces_count];
        int interfaces_count = getU2AndStepOffset(b, offset);
        int index = offset[0];
        int[] interfaces = new int[interfaces_count];
        for (int i = 0; i < interfaces_count; i += 2) {
            byte b1 = b[index++];
            byte b2 = b[index++];
            interfaces[i] = ((b1 & 0xff) << 8) | b2;
        }
        offset[0] = index;

        //u2             fields_count;
        //field_info     fields[fields_count];
        int fields_count = getU2AndStepOffset(b, offset);
        FieldInfo[] fields = parseFields(b, offset, fields_count);

        //u2             methods_count;
        //method_info    methods[methods_count];
        int methods_count = getU2AndStepOffset(b, offset);
        MethodInfo[] methods = parseMethods(b, offset, methods_count);

        //u2             attributes_count;
        //attribute_info attributes[attributes_count];
        int attributes_count = getU2AndStepOffset(b, offset);
        AttributeInfo[] attributes = parseAtributes(b, offset, attributes_count, constantPool);
        ClassFile ClassInfo =
                new ClassFile(minor_version, major_version, constantPool, access_flags, this_class, super_class,
                    interfaces, fields, methods, attributes);
        return ClassInfo;
    }


    /**
     * The value of the constant_pool_count item is equal to 
     * the number of entries in the constant_pool table plus one
     * 
     * A constant_pool index is considered valid if it is greater than zero and less than constant_pool_count, 
     * with the exception for constants of type long and double noted in §4.4.5.
     * 
     * 1:CONSTANT_Utf8
     * 3:CONSTANT_Integer
     * 4:CONSTANT_Float
     * 5:CONSTANT_Long
     * 6:CONSTANT_Double
     * 7:CONSTANT_Class
     * 8:CONSTANT_String
     * 9:CONSTANT_Fieldref
     * 10:CONSTANT_Methodref
     * 11:CONSTANT_InterfaceMethodref
     * 12:CONSTANT_NameAndType
     * 15:CONSTANT_MethodHandle
     * 16:CONSTANT_MethodType
     * 18:CONSTANT_InvokeDynamic
     */
    private ConstantInfo[] parseConstantPool(byte[] b, int[] offset, int constant_pool_count) {
        ConstantInfo[] constantPool = new ConstantInfo[constant_pool_count];
        //int index = 11;
        int count = 1;
        while (count < constant_pool_count) {
            int tag = getU1AndStepOffset(b, offset);
            switch (tag) {
            case 1: {//CONSTANT_Utf8
                /*
                CONSTANT_Utf8_info {
                    u1 tag;
                    u2 length;
                    u1 bytes[length];
                }
                */
                int len = getU2AndStepOffset(b, offset);
                String str = getUTFAndStepOffset(b, offset, len);
                constantPool[count++] = new ConstantInfo(tag, new Object[] { len, str });
                break;
            }
            case 3: {
                /*
                CONSTANT_Integer_info {
                    u1 tag;
                    u4 bytes;
                }
                */
                constantPool[count++] = new ConstantInfo(tag, getIntAndStepOffset(b, offset));
                break;
            }
            case 4: {
                /*
                CONSTANT_Float_info {
                    u1 tag;
                    u4 bytes;
                }
                */
                constantPool[count++] = new ConstantInfo(tag, Float.intBitsToFloat(getIntAndStepOffset(b, offset)));
                break;
            }
            //All 8-byte constants take up two entries in the constant_pool table of the class file. 
            //If a CONSTANT_Long_info or CONSTANT_Double_info structure is the item in the constant_pool table at index n, 
            //then the next usable item in the pool is located at index n+2. 
            //The constant_pool index n+1 must be valid but is considered unusable.
            //In retrospect, making 8-byte constants take two constant pool entries was a poor choice.
            case 5: {
                /*
                CONSTANT_Long_info {
                    u1 tag;
                    u4 high_bytes;
                    u4 low_bytes;
                }
                */
                constantPool[count++] = new ConstantInfo(tag, getLongAndStepOffset(b, offset));
                count++; //long占2个constant_pool入口
                break;
            }
            case 6: {
                /*
                CONSTANT_Double_info {
                    u1 tag;
                    u4 high_bytes;
                    u4 low_bytes;
                }
                */
                constantPool[count++] = new ConstantInfo(tag, Double.longBitsToDouble(getLongAndStepOffset(b, offset)));
                count++; //double占2个constant_pool入口
                break;
            }
            case 7: {
                /*
                CONSTANT_Class_info {
                    u1 tag;
                    u2 name_index;
                }
                */
                constantPool[count++] = new ConstantInfo(tag, getU2AndStepOffset(b, offset));
                break;
            }
            case 8: {
                /*
                CONSTANT_String_info {
                    u1 tag;
                    u2 string_index;
                }
                */
                constantPool[count++] = new ConstantInfo(tag, getU2AndStepOffset(b, offset));
                break;
            }
            case 9:
            case 10:
            case 11:
            case 12: {
                /*
                CONSTANT_Fieldref_info/CONSTANT_Methodref_info/CONSTANT_InterfaceMethodref {
                    u1 tag;
                    u2 class_index;
                    u2 name_and_type_index;
                }
                CONSTANT_NameAndType_info {
                    u1 tag;
                    u2 name_index;
                    u2 descriptor_index;
                }
                */
                int index1 = getU2AndStepOffset(b, offset);
                int index2 = getU2AndStepOffset(b, offset);
                constantPool[count++] = new ConstantInfo(tag, new int[] { index1, index2 });
                break;
            }
            case 15: {
                /*
                CONSTANT_MethodHandle_info {
                    u1 tag;
                    u1 reference_kind;
                    u2 reference_index;
                }
                */
                int reference_kind = getU1AndStepOffset(b, offset); // in the range 1 to 9 denotes the kind of this method handle
                int reference_index = getU2AndStepOffset(b, offset);
                constantPool[count++] = new ConstantInfo(tag, new int[] { reference_kind, reference_index });
                break;
            }
            case 16: {
                /*
                CONSTANT_MethodType_info {
                    u1 tag;
                    u2 descriptor_index;
                }
                */
                constantPool[count++] = new ConstantInfo(tag, getU2AndStepOffset(b, offset));
                break;
            }
            case 18: {
                /*
                CONSTANT_InvokeDynamic_info {
                    u1 tag;
                    u2 bootstrap_method_attr_index;
                    u2 name_and_type_index;
                }
                */
                int b_m_attr_index = getU2AndStepOffset(b, offset);
                int name_and_type_index = getU2AndStepOffset(b, offset);
                constantPool[count++] = new ConstantInfo(tag, new int[] { b_m_attr_index, name_and_type_index });
                break;
            }
            default:
                throw new IllegalArgumentException("Invalid constant tag:" + tag);
            }
        }
        return constantPool;
    }


    /**
     * field_info {
     *     u2             access_flags;
     *     u2             name_index;
     *     u2             descriptor_index;
     *     u2             attributes_count;
     *     attribute_info attributes[attributes_count];
     * }
     */
    private FieldInfo[] parseFields(byte[] b, int[] offset, int fields_count) {
        FieldInfo[] fields = new FieldInfo[fields_count];
        for (int count = 0; count < fields_count; count++) {
            int access_flags = getU2AndStepOffset(b, offset);
            int name_index = getU2AndStepOffset(b, offset);
            int descriptor_index = getU2AndStepOffset(b, offset);
            int attributes_count = getU2AndStepOffset(b, offset);
            AttributeInfo[] attributes = parseAtributes(b, offset, attributes_count, constantPool);
            fields[count] = new FieldInfo(access_flags, name_index, descriptor_index, attributes);
        }
        return fields;
    }


    /**
     * method_info {
     *     u2             access_flags;
     *     u2             name_index;
     *     u2             descriptor_index;
     *     u2             attributes_count;
     *     attribute_info attributes[attributes_count];
     * }
     */
    private MethodInfo[] parseMethods(byte[] b, int[] offset, int methods_count) {
        MethodInfo[] methods = new MethodInfo[methods_count];
        for (int count = 0; count < methods_count; count++) {
            int access_flags = getU2AndStepOffset(b, offset);
            int name_index = getU2AndStepOffset(b, offset);
            int descriptor_index = getU2AndStepOffset(b, offset);
            int attributes_count = getU2AndStepOffset(b, offset);
            AttributeInfo[] attributes = parseAtributes(b, offset, attributes_count, constantPool);
            methods[count] = new MethodInfo(access_flags, name_index, descriptor_index, attributes);
        }
        return methods;
    }


    /**
     * attribute_info {
     *     u2 attribute_name_index;
     *     u4 attribute_length;
     *     u1 info[attribute_length];
     * }
     */
    public static AttributeInfo[] parseAtributes(byte[] b, int[] offset, int attributes_count, ConstantInfo[] pool) {
        AttributeInfo[] attributes = new AttributeInfo[attributes_count];
        for (int count = 0; count < attributes_count; count++) {
            int attribute_name_index = getU2AndStepOffset(b, offset);

            int attribute_length = getIntAndStepOffset(b, offset);
            byte[] info = new byte[attribute_length];
            System.arraycopy(b, offset[0], info, 0, attribute_length);
            offset[0] = offset[0] + attribute_length; //step offset

            attributes[count] = AttributeInfo.newAttributeInfo(pool, attribute_name_index, info);
        }
        return attributes;
    }


    /**
     * 返回从offset[0]开始的2字节int并将offset[0]的值加2 
     */
    public static int getU2AndStepOffset(byte[] b, int[] offset) {
        int index = offset[0];
        int b1 = (b[index++] & 0xff) << 8;
        int b2 = (b[index++] & 0xff);
        offset[0] = index;
        return b1 | b2;
    }


    public static int getU1AndStepOffset(byte[] b, int[] offset) {
        int index = offset[0];
        int b1 = (b[index++] & 0xff);
        offset[0] = index;
        return b1;
    }


    /**
     * @return read 4 bytes as a int 
     */
    public static int getInt(byte[] b, int index) {
        int b1 = b[index] & 0xff;
        int b2 = b[index + 1] & 0xff;
        int b3 = b[index + 2] & 0xff;
        int b4 = b[index + 3] & 0xff;
        return (b1 << 24) | (b2 << 16) | (b3 << 8) | b4;
    }


    /**
     * 返回从offset[0]开始的4字节int并将offset[0]的值加4 
     */
    public static int getIntAndStepOffset(byte[] b, int[] offset) {
        int index = offset[0];
        int b1 = b[index++] & 0xff;
        int b2 = b[index++] & 0xff;
        int b3 = b[index++] & 0xff;
        int b4 = b[index++] & 0xff;
        offset[0] = index;
        return (b1 << 24) | (b2 << 16) | (b3 << 8) | b4;
    }


    @SuppressWarnings("unused")
    private static long getLong2(byte[] b, int index) {
        int high = getInt(b, index);
        int low = getInt(b, index + 4);
        return (long) high << 32 + low;
    }


    private static long getLongAndStepOffset(byte[] readBuffer, int[] offset) {
        int index = offset[0];
        long res = (((long) readBuffer[index++] << 56) + //
                ((long) (readBuffer[index++] & 255) << 48) + //
                ((long) (readBuffer[index++] & 255) << 40) + //
                ((long) (readBuffer[index++] & 255) << 32) + //
                ((long) (readBuffer[index++] & 255) << 24) + //
                ((readBuffer[index++] & 255) << 16) + //
                ((readBuffer[index++] & 255) << 8) + //
                ((readBuffer[index++] & 255) << 0));
        offset[0] = index;
        return res;
    }


    private static String getUTFAndStepOffset(byte[] b, int[] offset, int len) {
        try {
            int index = offset[0];
            offset[0] = index + len;
            return new String(b, index, len, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }


    public static void main2(String[] args) {
        byte b = -2;
        System.out.println(b << 8);
        System.out.println((b & 0xff) << 8);
        System.out.println((-2 & 0xff) << 8);
    }


    public static void main(String[] args) throws IOException {
        String dir = "./target/test-classes/cn/hqm/jvm";
        //File file = new File(dir, "TypicalClass1.class");
        //File file = new File(dir, "TypicalClass1$MyException.class");
        //File file = new File(dir, "NotAMember.class");
        //File file = new File(dir, "WideInstructionTest.class");
        //File file = new File("D:/12_code/tae/trunk/tae-php-engine/resin-quercus4036/languagereference.types.scalar_types_php4j.class");
        File file = new File("D:/00_dev_env/download/TraceTransformer.x.class");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
        byte[] b = new byte[1024];
        int len;
        while ((len = in.read(b)) >= 0) {
            out.write(b, 0, len);
        }
        in.close();

        byte[] classBytes = out.toByteArray();
        ClassFileParser parser = new ClassFileParser();
        ClassFile classFile = parser.parse(classBytes, null);
        Displayer displayer = new Displayer();
        classFile.display(displayer, classBytes);
        BufferedWriter bw = new BufferedWriter(new FileWriter(file.getName() + ".html"));
        //bw.write("<style>.s1:hover{border-style:solid}</style>");
        bw.write(displayer.getHtml());
        bw.close();
    }
}
