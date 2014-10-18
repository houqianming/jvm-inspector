package cn.hqm.jvm.classfile.attr;

import cn.hqm.jvm.classfile.AttributeInfo;
import cn.hqm.jvm.classfile.ClassFileParser;
import cn.hqm.jvm.classfile.ConstantInfo;
import cn.hqm.jvm.classfile.Displayer;


/**
 * Java 1.7 JVM Spec 4.7.4. The StackMapTable Attribute
 * 
StackMapTable_attribute {
    u2              attribute_name_index;
    u4              attribute_length;
    u2              number_of_entries;
    stack_map_frame entries[number_of_entries];
}
 * 
 * @author linxuan
 * @date 2014-3-17 上午9:18:16
 */
public class StackMapTable extends AttributeInfo {
    public final StackMapFrame[] entries;


    public StackMapTable(int attribute_name_index, byte[] info) {
        super(attribute_name_index, info);
        int[] offset = new int[] { 0 };
        int number_of_entries = ClassFileParser.getU2AndStepOffset(info, offset);
        entries = new StackMapFrame[number_of_entries];
        for (int i = 0; i < number_of_entries; i++) {
            entries[i] = new StackMapFrame(info, offset);
        }
    }


    @Override
    protected void displayInfo(Displayer displayer, byte[] b, int[] offset, ConstantInfo[] pool) {
        displayer.writeAndStepOffset(b, offset, 2, "number_of_entries:" + entries.length);
        displayer.spanBegin(4);
        for (int i = 0; i < entries.length; i++) {
            entries[i].displayInfo(displayer, b, offset, pool);
        }
        displayer.spanEnd();
    }

    /**
     * 
     * union stack_map_frame {
     *     same_frame;
     *     same_locals_1_stack_item_frame;
     *     same_locals_1_stack_item_frame_extended;
     *     chop_frame;
     *     same_frame_extended;
     *     append_frame;
     *     full_frame;
     * }
     * 
     * 每个frame的结构是full_frame的一个子集。full_frame:
     *
     * full_frame {
     *     u1 frame_type = FULL_FRAME; // 255 
     *     u2 offset_delta;
     *     u2 number_of_locals;
     *     verification_type_info locals[number_of_locals];
     *     u2 number_of_stack_items;
     *     verification_type_info stack[number_of_stack_items];
     * }
     * 
     * @author linxuan
     * @date 2014-3-26 上午8:58:59
     */
    private static class StackMapFrame {
        private final int frame_type;
        private final int offset_delta;
        private final VerificationTypeInfo[] locals;
        private final VerificationTypeInfo[] stack;


        public StackMapFrame(byte[] b, int[] offset) {
            frame_type = ClassFileParser.getU1AndStepOffset(b, offset);
            if (frame_type <= 63) {
                // same_frame {
                //     u1 frame_type = SAME; /* 0-63 */
                // }
                offset_delta = frame_type;
                locals = null; //has same locals as the previous 
                stack = null; //the number of stack items is zero
            }
            else if (frame_type <= 127) {
                // same_locals_1_stack_item_frame {
                //     u1 frame_type = SAME_LOCALS_1_STACK_ITEM; /* 64-127 */
                //     verification_type_info stack[1];
                // }
                offset_delta = frame_type - 64;
                locals = null; //has same locals as the previous 
                stack = new VerificationTypeInfo[] { new VerificationTypeInfo(b, offset) };
            }
            else if (frame_type <= 246) {
                throw new IllegalArgumentException(
                    "stack_map_frame tags in the range [128-246] are reserved for future use");
            }
            else if (frame_type == 247) {
                // same_locals_1_stack_item_frame_extended {
                //     u1 frame_type = SAME_LOCALS_1_STACK_ITEM_EXTENDED; /* 247 */
                //     u2 offset_delta;
                //     verification_type_info stack[1];
                // }
                offset_delta = ClassFileParser.getU2AndStepOffset(b, offset);
                locals = null; //has same locals as the previous 
                stack = new VerificationTypeInfo[] { new VerificationTypeInfo(b, offset) };
            }
            else if (frame_type <= 250) {
                // chop_frame {
                //     u1 frame_type = CHOP; /* 248-250 */
                //     u2 offset_delta;
                //}
                offset_delta = ClassFileParser.getU2AndStepOffset(b, offset);
                locals = null; //** k last locals are absent. k = 251 - frame_type
                stack = null; //the number of stack items is zero
            }
            else if (frame_type == 251) {
                // same_frame_extended {
                //     u1 frame_type = SAME_FRAME_EXTENDED; /* 251 */
                //     u2 offset_delta;
                // }
                offset_delta = ClassFileParser.getU2AndStepOffset(b, offset);
                locals = null; //has same locals as the previous 
                stack = null; //the number of stack items is zero
            }
            else if (frame_type <= 254) {
                // append_frame {
                //     u1 frame_type = APPEND; /* 252-254 */
                //     u2 offset_delta;
                //     verification_type_info locals[frame_type - 251];
                //}
                offset_delta = ClassFileParser.getU2AndStepOffset(b, offset);
                //k additional locals are defined. k = frame_type - 251.
                locals = new VerificationTypeInfo[frame_type - 251];
                for (int i = 0; i < locals.length; i++) {
                    locals[i] = new VerificationTypeInfo(b, offset);
                }
                stack = null; //the number of stack items is zero
            }
            else if (frame_type == 255) {
                //full_frame
                offset_delta = ClassFileParser.getU2AndStepOffset(b, offset);

                int number_of_locals = ClassFileParser.getU2AndStepOffset(b, offset);
                locals = new VerificationTypeInfo[number_of_locals];
                for (int i = 0; i < number_of_locals; i++) {
                    locals[i] = new VerificationTypeInfo(b, offset);
                }

                int number_of_stack_items = ClassFileParser.getU2AndStepOffset(b, offset);
                stack = new VerificationTypeInfo[number_of_stack_items];
                for (int i = 0; i < number_of_stack_items; i++) {
                    stack[i] = new VerificationTypeInfo(b, offset);
                }
            }
            else {
                throw new IllegalStateException("Shouldn't happen");
            }
        }


        protected void displayInfo(Displayer displayer, byte[] b, int[] offset, ConstantInfo[] pool) {
            if (frame_type <= 63) {
                // same_frame {
                //     u1 frame_type = SAME; /* 0-63 */
                // }
                displayer.writeAndStepOffset(b, offset, 1, "SAME: offset_delta:" + offset_delta);
            }
            else if (frame_type <= 127) {
                // same_locals_1_stack_item_frame {
                //     u1 frame_type = SAME_LOCALS_1_STACK_ITEM; /* 64-127 */
                //     verification_type_info stack[1];
                // }
                displayer.writeAndStepOffset(b, offset, 1, "SAME_LOCALS_1_STACK_ITEM: offset_delta:" + offset_delta);
                for (int i = 0; i < stack.length; i++) {
                    stack[i].displayInfo(displayer, b, offset, pool);
                }
            }
            else if (frame_type <= 246) {
                throw new IllegalArgumentException(
                    "stack_map_frame tags in the range [128-246] are reserved for future use");
            }
            else if (frame_type == 247) {
                // same_locals_1_stack_item_frame_extended {
                //     u1 frame_type = SAME_LOCALS_1_STACK_ITEM_EXTENDED; /* 247 */
                //     u2 offset_delta;
                //     verification_type_info stack[1];
                // }
                displayer.writeAndStepOffset(b, offset, 1, "SAME_LOCALS_1_STACK_ITEM_EXTENDED");
                displayer.writeAndStepOffset(b, offset, 2, "offset_delta:" + offset_delta);
                for (int i = 0; i < stack.length; i++) {
                    stack[i].displayInfo(displayer, b, offset, pool);
                }
            }
            else if (frame_type <= 250) {
                // chop_frame {
                //     u1 frame_type = CHOP; /* 248-250 */
                //     u2 offset_delta;
                //}
                displayer.writeAndStepOffset(b, offset, 1, "CHOP: " + (251 - frame_type) + " last locals are absent.");
                displayer.writeAndStepOffset(b, offset, 2, "offset_delta:" + offset_delta);
            }
            else if (frame_type == 251) {
                // same_frame_extended {
                //     u1 frame_type = SAME_FRAME_EXTENDED; /* 251 */
                //     u2 offset_delta;
                // }
                displayer.writeAndStepOffset(b, offset, 1, "SAME_FRAME_EXTENDED");
                displayer.writeAndStepOffset(b, offset, 2, "offset_delta:" + offset_delta);
            }
            else if (frame_type <= 254) {
                // append_frame {
                //     u1 frame_type = APPEND; /* 252-254 */
                //     u2 offset_delta;
                //     verification_type_info locals[frame_type - 251];
                //}
                displayer.writeAndStepOffset(b, offset, 1, "APPEND: " + (frame_type - 251)
                        + "additional locals are defined");
                displayer.writeAndStepOffset(b, offset, 2, "offset_delta:" + offset_delta);
                displayer.spanBegin(4);
                for (int i = 0; i < locals.length; i++) {
                    locals[i].displayInfo(displayer, b, offset, pool);
                }
                displayer.spanEnd();
            }
            else if (frame_type == 255) {
                //full_frame
                displayer.writeAndStepOffset(b, offset, 1, "FULL_FRAME");
                displayer.writeAndStepOffset(b, offset, 2, "offset_delta:" + offset_delta);
                displayer.writeAndStepOffset(b, offset, 2, "number_of_locals:" + locals.length);
                for (int i = 0; i < locals.length; i++) {
                    locals[i].displayInfo(displayer, b, offset, pool);
                }
                displayer.writeAndStepOffset(b, offset, 2, "number_of_stack_items:" + stack.length);
                for (int i = 0; i < stack.length; i++) {
                    stack[i].displayInfo(displayer, b, offset, pool);
                }

            }
            else {
                throw new IllegalStateException("Shouldn't happen");
            }
        }
    }

    /**
     * Each verification_type_info structure specifies the verification type of one or two locations\
     * 
     * union verification_type_info {
     *     Top_variable_info;
     *     Integer_variable_info;
     *     Float_variable_info;
     *     Long_variable_info;
     *     Double_variable_info;
     *     Null_variable_info;
     *     UninitializedThis_variable_info;
     *     Object_variable_info;
     *     Uninitialized_variable_info;
     * }
     * 
     * @author linxuan
     * @date 2014-3-26 上午8:54:57
     */
    private static class VerificationTypeInfo {
        public final int tag;
        public final Integer cpool_index_or_newindex;


        public VerificationTypeInfo(byte[] b, int[] offset) {
            tag = ClassFileParser.getU1AndStepOffset(b, offset);
            Integer extraIndex = null;
            switch (tag) {
            case 0: //Top_variable_info
            case 1: //Integer_variable_info
            case 2: //Float_variable_info
            case 3: //Double_variable_info
            case 4: //Long_variable_info
            case 5: //Null_variable_info
            case 6://UninitializedThis_variable_info
                break;
            case 7://Object_variable_info
                extraIndex = ClassFileParser.getU2AndStepOffset(b, offset);
                break;
            case 8://Uninitialized_variable_info
                   //offset of the new instruction (§new) that created the object being stored in the location
                extraIndex = ClassFileParser.getU2AndStepOffset(b, offset);
                break;
            default:
                throw new IllegalArgumentException("Invalid verification_type_info tag:" + tag);
            }
            cpool_index_or_newindex = extraIndex;
        }


        protected void displayInfo(Displayer displayer, byte[] b, int[] offset, ConstantInfo[] pool) {
            displayer.writeAndStepOffset(b, offset, 1, "tag:" + tag);
            switch (tag) {
            case 0: //Top_variable_info
            case 1: //Integer_variable_info
            case 2: //Float_variable_info
            case 3: //Double_variable_info
            case 4: //Long_variable_info
            case 5: //Null_variable_info
            case 6://UninitializedThis_variable_info
                break;
            case 7://Object_variable_info
                int i = cpool_index_or_newindex;
                displayer.writeAndStepOffset(b, offset, 2, "cpool_index:#" + i + "(" + pool[i].showContent(pool) + ")");
                break;
            case 8://Uninitialized_variable_info
                   //offset of the new instruction (§new) that created the object being stored in the location
                displayer.writeAndStepOffset(b, offset, 2, "code index of new:" + cpool_index_or_newindex);
                break;
            default:
                throw new IllegalArgumentException("Invalid verification_type_info tag:" + tag);
            }
        }
    }
}
