package cn.hqm.jvm.classfile.attr;

import cn.hqm.jvm.classfile.AttributeInfo;
import cn.hqm.jvm.classfile.ClassFileParser;
import cn.hqm.jvm.classfile.ConstantInfo;
import cn.hqm.jvm.classfile.Displayer;


/**
 * Java 1.7 JVM Spec 4.7.21. The BootstrapMethods attribute
 * 
BootstrapMethods_attribute {
    u2 attribute_name_index;
    u4 attribute_length;
    u2 num_bootstrap_methods;
    {   u2 bootstrap_method_ref;
        u2 num_bootstrap_arguments;
        u2 bootstrap_arguments[num_bootstrap_arguments];
    } bootstrap_methods[num_bootstrap_methods];
}
 *
 * @author linxuan
 * @date 2014-6-3 上午11:55:20
 */
public class BootstrapMethodsAttribute extends AttributeInfo {
    private final BootstrapMethod[] bootstrap_methods;


    public BootstrapMethodsAttribute(int attribute_name_index, byte[] info) {
        super(attribute_name_index, info);
        int[] offset = new int[] { 0 };
        int num_bootstrap_methods = ClassFileParser.getU2AndStepOffset(info, offset);
        bootstrap_methods = new BootstrapMethod[num_bootstrap_methods];
        for (int i = 0; i < num_bootstrap_methods; i++) {
            bootstrap_methods[i] = new BootstrapMethod(info, offset);
        }
    }


    @Override
    protected void displayInfo(Displayer displayer, byte[] b, int[] offset, ConstantInfo[] pool) {
        displayer.writeAndStepOffset(b, offset, 2, "num_bootstrap_methods:" + bootstrap_methods.length);
        displayer.spanBegin(4);
        for (int i = 0; i < bootstrap_methods.length; i++) {
            bootstrap_methods[i].displayInfo(displayer, b, offset, pool);
        }
        displayer.spanEnd();
    }

    private static class BootstrapMethod {
        public final int bootstrap_method_ref;
        public final int[] bootstrap_arguments;


        public BootstrapMethod(byte[] b, int[] offset) {
            this.bootstrap_method_ref = ClassFileParser.getU2AndStepOffset(b, offset);
            int num_bootstrap_arguments = ClassFileParser.getU2AndStepOffset(b, offset);
            this.bootstrap_arguments = new int[num_bootstrap_arguments];
            for (int i = 0; i < num_bootstrap_arguments; i++) {
                this.bootstrap_arguments[i] = ClassFileParser.getU2AndStepOffset(b, offset);
            }
        }


        protected void displayInfo(Displayer displayer, byte[] b, int[] offset, ConstantInfo[] pool) {
            displayer.writeAndStepOffset(b, offset, 2, "bootstrap_method_ref:#" + bootstrap_method_ref + "("
                    + pool[bootstrap_method_ref].showContent(pool) + ")");
            for (int i = 0; i < bootstrap_arguments.length; i++) {
                displayer.writeAndStepOffset(b, offset, 2, "#" + bootstrap_arguments[i] + "("
                        + pool[bootstrap_arguments[i]].showContent(pool) + ")");
            }
        }
    }
}
