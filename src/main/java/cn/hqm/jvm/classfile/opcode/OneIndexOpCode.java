package cn.hqm.jvm.classfile.opcode;

import cn.hqm.jvm.classfile.ConstantInfo;
import cn.hqm.jvm.classfile.OpCode;


/**
 * 参数为1个或2个字节，1个或2个字节表示一个常量池序号；（如果是2个字节，编码为无符号int）
 * 
 * @author linxuan
 * @date 2014-3-28 上午8:34:48
 */
public class OneIndexOpCode extends OpCode {

    public OneIndexOpCode(byte opcode, String mnemonic, int paramlen) {
        super(opcode, mnemonic, paramlen);
    }


    @Override
    protected String commentOnParams(byte[] b, int index, int len, ConstantInfo[] pool) {
        int i;
        if (len == 1) {
            i = b[index] & 0xff;
        }
        else if (len == 2) {
            i = ((b[index] & 0xff) << 8) | (b[index + 1] & 0xff);
        }
        else {
            throw new IllegalStateException("Shouldn't happen!");
        }
        return "#" + i + "(" + pool[i].showContent(pool) + ")";
    }

}
