package cn.hqm.jvm.asm;

import java.util.Set;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;
import org.objectweb.asm.commons.Method;


/**
 * 操作字节码处的逻辑从housemd的clojure代码翻译而来
 * 
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 * @author linxuan
 */
public class MethodInterceptor extends ClassVisitor {
    private Set<String> methods;
    private String className;


    public MethodInterceptor(ClassWriter cw, String className, Set<String> methods) {
        super(Opcodes.ASM4, cw);
        this.methods = methods;
        this.className = className;
    }


    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (!methods.contains(name)) {
            return super.visitMethod(access, name, desc, signature, exceptions);
        }
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        return new TraceMethodCall(mv, access, className, name, desc);

    }

    private static class TraceMethodCall extends AdviceAdapter {
        private String className;
        private String methodName;
        private Label start = new Label();
        private Label end = new Label();
        private Type advice = Type.getType(MethodAdvice.class);
        private Method enter = Method.getMethod(MethodAdvice.ON_METHOD_BEGIN);
        private Method exit = Method.getMethod(MethodAdvice.ON_METHOD_END);
        private boolean isStaticMethod = (methodAccess & ACC_STATIC) != 0;


        protected TraceMethodCall(MethodVisitor mv, int access, String calssName, String methodName, String desc) {
            super(Opcodes.ASM4, mv, access, methodName, desc);
            this.methodName = methodName;
            this.className = calssName;
        }


        @Override
        public void visitMaxs(int maxStack, int maxLocals) {
            mark(end);
            catchException(start, end, Type.getType(Throwable.class));
            dup();
            invokeStatic(advice, exit);
            throwException();
            super.visitMaxs(maxStack, maxLocals);
        }


        @Override
        protected void onMethodEnter() {
            push(className);
            push(methodName);
            push(methodDesc);
            loadThisOrPushNullIfIsStatic();
            loadArgArray();
            invokeStatic(advice, enter);
            mark(start);
        }


        @Override
        protected void onMethodExit(int opcode) {
            if (opcode != ATHROW) {
                prepareResultBy(opcode);
                invokeStatic(advice, exit);
            }
        }


        private void loadThisOrPushNullIfIsStatic() {
            if (isStaticMethod)
                pushNull();
            else
                loadThis();
        }


        private void prepareResultBy(int opcode) {
            switch (opcode) {
            case RETURN:
                pushNull(); // void
            case ARETURN:
                dup(); // object
            case LRETURN:
            case DRETURN:
                dup2();
                box(Type.getReturnType(methodDesc)); // long or double
            default:
                dup();
                box(Type.getReturnType(methodDesc)); // object or boolean or byte or char or short or int
            }
        }


        //private void pushNull() { push(null.asInstanceOf[Type]) }
        private void pushNull() {
            push((Type) null);
        }
    }
}
