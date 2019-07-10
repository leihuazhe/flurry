package com.yunji.json;

import com.alibaba.com.caucho.hessian.io.Hessian2Output;
import com.yunji.metadata.tag.DataType;
import com.yunji.metadata.tag.Field;
import com.yunji.metadata.tag.Struct;
import io.netty.buffer.ByteBuf;
import org.apache.dubbo.common.serialize.hessian2.Hessian2ObjectOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Stack;

import static com.yunji.json.JsonUtils.isCollectionKind;
import static com.yunji.json.JsonUtils.isMultiElementKind;
import static com.yunji.json.JsonUtils.isValidMapKeyType;
import static com.yunji.json.util.MetaDataUtil.dataType2Byte;

/**
 * @author Denim.leihz 2019-07-08 8:30 PM
 */
public class JsonReader implements JsonCallback {
    private final Logger logger = LoggerFactory.getLogger(JsonReader.class);

    private final OptimizedMetadata.OptimizedStruct optimizedStruct;
    private final OptimizedMetadata.OptimizedService optimizedService;
    private final ByteBuf requestByteBuf;

//    private final InvocationContext invocationCtx = InvocationContextImpl.Factory.currentInstance();


    /**
     * Dubbo 序列化 encode interface
     */
    private final Hessian2ObjectOutput out;


    private final Hessian2Output mH2o;


    /**
     * 当前处理数据节点
     */
    StackNode current;

    /**
     * incr: startObject/startArray
     * decr: endObject/endArray
     */
    int level = -1;

    /**
     * onStartField的时候, 记录是否找到该Field. 如果没找到,那么需要skip这个field
     * 当 skip 设置为 true 时， skipDepth 初始化为 0。
     * - onStartObject/onStartArray) 则 skipDepth++
     * - onEndObject/onEndArray时，skipDepth--
     * 当 skipDepth 为 0 且进入 onEndField 时， skip 复位为false
     */
    boolean skip = false;
    int skipDepth = 0;

    /**
     * <pre>
     *
     * 按照事件流解析 JSON 时， JsonReader 为维护一个 NodeInfo 的 stack， 基本原则是：
     * - 栈顶的NodeInfo(current) 总是对应当前的 json value 的信息。
     * - 对 JsonObject
     * -- 当 onStartField(String) 时，会将子字段的 NodeInfo 压栈
     * -- 当 onEndField 时， 会 pop 到 上一层NodeInfo 应该是 Struct or MAP 对应的NodeInfo
     * - 对 JsonArray
     * -- 当 onStartFiled(int) 时，会将数组元素对应的 NodeInfo 压栈
     * -- 当 onEndField 时，会pop，恢复到 List/SET 对应的 NodeInfo
     *
     * 也就是说，对JSON中的每一个值( object, array, string, number, boolean, null），都会有一个
     * 对应的 NodeInfo, 这个随着 JSON 的事件流，不断的新建，然后当json值处理完成后，NodeInfo又会销毁。
     *
     * NodeInfo 中包括了如下信息：
     * - DataType 当前 value 对应的元数据类型。
     * - tFieldPos 对应与 { name: null } 这样的 field，当检测到value 是一个null值时，我们需要将
     *     thrift流回滚到 tFieldPos，即消除在流中的field头部信息。这个检测在 onEndField 时进行。
     *     对于LIST/SET 这是不容许子元素有 null 值的。
     * - valuePos 如果 value 时一个MAP/LIST/SET，在 value 的开始部分会是这个集合的长度，
     *     在 onEndObject/onEndArray 时，我们会回到 valuePos，重写集合的长度。
     * - elCount 如果 parent 是MAP/LIST/SET，对非 null 值(onStartObject, onStartArray,onString,
     *     onNumber, onBoolean)都会新增 parent.elCount。然后在parent结束后重置长度。
     * - isNull onNull 会设置当前的 isNull 为true，在onEndField 时，进行 tFieldPos的特殊处理。
     *
     *  主要的处理逻辑：
     *
     * - onStartObject
     *  - 当前栈顶 ：STRUCT or MAP
     *  - 栈操作：无
     *  - 处理：proto.writeStructBegin or proto.writeMapBegin
     *
     * - onEndObject
     *  - 当前栈顶：STRUCT or MAP
     *  - 栈操作：无
     *  - 处理：proto.writeStructEnd or proto.writeMapEnd
     *
     * - onStartArray
     *  - 当前栈顶：LIST/SET
     *  - 栈操作：无
     *  - 处理：proto.writeListBegin or proto.writeSetBegin
     *
     * - onEndArray
     *  - 当前栈顶：
     *  - 栈操作：无
     *  - 处理：proto.writeListEnd or proto.writeSetEnd 并重置长度
     *
     * - onStartField name
     *  - 当前栈顶：STRUCT / MAP
     *  - 栈操作：
     *      - STRUCT：将结构体的fields[name] 压栈
     *      - MAP：将 valueType 压栈
     *  - 处理：
     *      - STRUCT: proto.writeFieldBegin name
     *      - MAP: proto.writeString name or proto.writeInt name.toInt
     *
     *  - onStartField int
     *   - 当前栈顶：LIST/SET
     *   - 栈操作：
     *      - 将 valueType 压栈
     *   - 处理：
     *
     * - onEndField
     *  - 当前栈顶：any
     *  - 栈操作：pop 恢复上一层。
     *  - 处理：
     *   - 当前字段是Map的元素且当前值为 null，则回退到 tFieldPos
     *   - 当前字段为LIST/SET的子元素，不容许当前值为 null
     *   - 当前字段是Struct的字段，则回退到 tFieldPos(null) 或者 writeFieldEnd(not null)
     *   - rewrite array size
     *
     * - onNumber
     *  - 当前栈顶：BYTE/SHORT/INTEGER/LONG/DOUBLE
     *  - 栈操作：无
     *  - 处理
     *      - proto.writeI8, writeI16, ...
     *
     * - onBoolean
     *  - 当前栈顶：BOOLEAN
     *  - 栈操作：无
     *  - 处理
     *      - proto.writeBoolean.
     *
     * - onString
     *  - 当前栈顶：STRING
     *  - 栈操作：无
     *  - 处理
     *      - proto.writeString, ...
     *
     * - onNull
     *  - 当前栈顶：any
     *  - 栈操作：无
     *  - 处理 current.isNull = true
     * </pre>
     */
    Stack<StackNode> history = new Stack<>();

    List<StackNode> nodePool = new ArrayList<>(64);  // keep a minum StackNode Pool

    /**
     * @param optimizedStruct
     * @param optimizedService
     * @param requestByteBuf
     * @param out
     */
    JsonReader(OptimizedMetadata.OptimizedStruct optimizedStruct, OptimizedMetadata.OptimizedService optimizedService, ByteBuf requestByteBuf, Hessian2ObjectOutput out) throws Exception {
        this.optimizedStruct = optimizedStruct;
        this.optimizedService = optimizedService;
        this.requestByteBuf = requestByteBuf;
        this.out = out;
        java.lang.reflect.Field filed = out.getClass().getDeclaredField("mH2o");
        filed.setAccessible(true);
        this.mH2o = (Hessian2Output) filed.get(out);
    }


    @Override
    public void onStartObject() throws IOException {
        level++;

        if (level == 0) return;  // it's the outside { body: ... } object

        if (skip) {
            skipDepth++;
            return;
        }

        assert current.dataType.kind == DataType.KIND.STRUCT || current.dataType.kind == DataType.KIND.MAP;

        StackNode peek = peek();
        if (peek != null && isMultiElementKind(peek.dataType.kind)) {
            peek.incrElementSize();
        }
        switch (current.dataType.kind) {
            case STRUCT:
                Struct struct = current.optimizedStruct.struct;
                if (struct == null) {
                    logger.error("optimizedStruct not found");
                    logAndThrowTException();
                }
                assert struct != null;
                if (struct.namespace != null) {
                    mH2o.writeListBegin(1, "[object");
                    mH2o.writeObjectBegin(struct.namespace + "." + struct.name);
                    mH2o.writeClassFieldLength(struct.fields.size());

                    for (int i = 0; i < struct.fields.size(); i++) {
                        Field field = struct.fields.get(i);
                        mH2o.writeString(field.getName());
                    }
                    mH2o.writeObjectBegin(struct.namespace + "." + struct.name);
                }

//                out.writeStructBegin(new TStruct(struct.name));
                break;
            case MAP:
                assert isValidMapKeyType(current.dataType.keyType.kind);
                writeMapBegin(dataType2Byte(current.dataType.keyType), dataType2Byte(current.dataType.valueType), 0);
                break;
            default:
                logAndThrowTException();
        }

    }

    @Override
    public void onEndObject() throws IOException {
        level--;
        if (level == -1) return; // the outer body

        if (skip) {
            skipDepth--;
            return;
        }

        assert current.dataType.kind == DataType.KIND.STRUCT || current.dataType.kind == DataType.KIND.MAP;

        switch (current.dataType.kind) {
            case STRUCT:
                validateStruct(current);
//                out.writeFieldStop();
//                out.writeStructEnd();
                break;
            case MAP:
//                out.writeMapEnd();
                reWriteByteBuf();
                break;
            default:
                logAndThrowTException();
        }

    }

    /**
     * 由于目前拿不到集合的元素个数, 暂时设置为0个
     *
     * @throws JException
     */
    @Override
    public void onStartArray() throws IOException {
        level++;
        if (skip) {
            skipDepth++;
            return;
        }

        assert isCollectionKind(current.dataType.kind);

        StackNode peek = peek();
        if (peek != null && isMultiElementKind(peek.dataType.kind)) {
            peek.incrElementSize();
        }

        switch (current.dataType.kind) {
            case LIST:
            case SET:
                writeCollectionBegin(dataType2Byte(current.dataType.valueType), 0);
                break;
            default:
                logAndThrowTException();
        }

    }

    @Override
    public void onEndArray() throws IOException {
        level--;
        if (skip) {
            skipDepth--;
            return;
        }

        assert isCollectionKind(current.dataType.kind);

        switch (current.dataType.kind) {
            case LIST:
//                mH2o.writeListEnd();
                //重写长度
                reWriteByteBuf();
                break;
            case SET:
//                mH2o.writeListEnd();
//                out.writeSetEnd();
                reWriteByteBuf();
                break;
            default:
                //do nothing
        }
    }

    @Override
    public void onStartField(String name) throws IOException {
        if (skip) {
            return;
        }
        if (level == 0) { // expect only the "body"
            if ("body".equals(name)) { // body
                DataType initDataType = new DataType();
                initDataType.setKind(DataType.KIND.STRUCT);
                initDataType.qualifiedName = optimizedStruct.struct.name;
                push(initDataType, -1, // not a struct field
                        requestByteBuf.writerIndex(), //
                        optimizedStruct, "body");
            } else { // others, just skip now
                skip = true;
                skipDepth = 0;
            }
        } else { // level > 0, not skip
            if (current.dataType.kind == DataType.KIND.MAP) {
                assert isValidMapKeyType(current.dataType.keyType.kind);

                int tFieldPos = requestByteBuf.writerIndex();
                if (current.dataType.keyType.kind == DataType.KIND.STRING) {
//                    out.writeString(name);
                } else {
                    writeIntField(name, current.dataType.keyType.kind);
                }
                push(current.dataType.valueType,
                        tFieldPos, // so value can't be null
                        requestByteBuf.writerIndex(), // need for List/Map
                        optimizedService.optimizedStructs.get(current.dataType.valueType.qualifiedName),
                        name);
            } else if (current.dataType.kind == DataType.KIND.STRUCT) {

                Field field = current.optimizedStruct.fieldMap.get(name);


                if (field == null) {
                    skip = true;
                    skipDepth = 0;
                    logger.debug("field(" + name + ") not found. just skip");
                    return;
                } else {
                    skip = false;
                }
                /*//写 对象 className 全限定名
                OptimizedMetadata.OptimizedStruct struct = optimizedService.optimizedStructs.get(field.dataType.qualifiedName);
                mH2o.writeObjectBegin(field.dataType.qualifiedName);
                //写对象的 Filed长度
                mH2o.writeClassFieldLength(struct.fieldMap.size());
                //写Filed名称*/


                int tFieldPos = requestByteBuf.writerIndex();
//                out.writeFieldBegin(new TField(field.name, dataType2Byte(field.dataType), (short) field.getTag()));
                push(field.dataType,
                        tFieldPos,
                        requestByteBuf.writerIndex(),
                        optimizedService.optimizedStructs.get(field.dataType.qualifiedName),
                        name);
            } else {
                logAndThrowTException("field " + name + " type " + toString(current.dataType) + " not compatible with json object");
            }
        }
    }

    @Override
    public void onStartField(int index) {
        if (skip) {
            return;
        }
        assert isCollectionKind(current.dataType.kind);

        DataType next = current.dataType.valueType;
        OptimizedMetadata.OptimizedStruct nextStruct = (next.kind == DataType.KIND.STRUCT) ?
                optimizedService.optimizedStructs.get(next.qualifiedName) : null;
        push(current.dataType.valueType,
                -1,
                requestByteBuf.writerIndex(),
                nextStruct,
                null);

    }

    @Override
    public void onEndField() throws IOException {
        if (skip) {
            if (skipDepth == 0) { // reset skipFlag
                skip = false;
            }
            return;
        }

        String fieldName = current.fieldName;

        if (level > 0) { // level = 0 will having no current dataType
            StackNode parent = peek();
            assert (parent != null);

            switch (parent.dataType.kind) {
                case SET:
                case LIST:
                    if (current.isNull) {
                        logAndThrowTException("SET/LIST can't support null value");
                    }
                    break;
                case MAP:
                    if (current.isNull) {
                        // peek().decrElementSize(); onNull not incrElementSize
                        requestByteBuf.writerIndex(current.tFieldPosition);
                    }
                    break;
                case STRUCT:
                    if (current.isNull) {
                        // parent.fields4Struct.remove(fieldName);
                        requestByteBuf.writerIndex(current.tFieldPosition);
                       /* if (invocationCtx.codecProtocol() == CompressedBinary) {
                            ((TCompactProtocol) out).resetLastFieldId();
                        }*/
                    } else {
                        Field field = parent.optimizedStruct.fieldMap.get(fieldName);
                        parent.fields4Struct.set(field.tag - parent.optimizedStruct.tagBase);
//                        out.writeFieldEnd();
                    }
                    break;
            }

            pop();
        }

    }

    @Override
    public void onBoolean(boolean value) throws IOException {
        if (skip) {
            return;
        }

        if (current.dataType.kind != DataType.KIND.BOOLEAN) {
            logAndThrowTException();
        }

        StackNode peek = peek();
        if (peek != null && isMultiElementKind(peek.dataType.kind)) {
            peek.incrElementSize();
        }

//        out.writeBool(value);
    }

    @Override
    public void onNumber(double value) throws IOException {
        DataType.KIND currentType = current.dataType.kind;

        if (skip) {
            return;
        }

        StackNode peek = peek();
        if (peek != null && isMultiElementKind(peek.dataType.kind)) {
            peek.incrElementSize();
        }

        switch (currentType) {
            case SHORT:
//                out.writeI16((short) value);
                break;
            case INTEGER:
            case ENUM:
                out.writeInt((int) value);
                break;
            case LONG:
                mH2o.writeLong((long) value);
                break;
            case DOUBLE:
                mH2o.writeDouble(value);
                break;
            case BIGDECIMAL:
//                out.writeString(String.valueOf(value));
                break;
            case BYTE:
//                out.writeByte((byte) value);
                break;
            default:
                throw new IOException("Field:" + current.fieldName + ", DataType(" + current.dataType.kind
                        + ") for " + current.dataType.qualifiedName + " is not a Number");

        }
    }

    @Override
    public void onNumber(long value) throws IOException {
//        throw new NotImplementedException();
    }

    @Override
    public void onNull() throws IOException {
        if (skip) {
            return;
        }
        current.isNull = true;
    }

    @Override
    public void onString(String value) throws IOException {
        if (skip) {
            return;
        }

        StackNode peek = peek();
        if (peek != null && isMultiElementKind(peek.dataType.kind)) {
            peek.incrElementSize();
        }

        switch (current.dataType.kind) {
            case ENUM:
//                TEnum tEnum = optimizedService.enumMap.get(current.dataType.qualifiedName);
//                Integer tValue = findEnumItemValue(tEnum, value);
//                if (tValue == null) {
//                    logger.error("Enum(" + current.dataType.qualifiedName + ") not found for value:" + value);
//                    logAndThrowTException();
//                }
//                out.writeI32(tValue);
                break;
            case BOOLEAN:
//                out.writeBool(Boolean.parseBoolean(value));
                break;
            case DOUBLE:
//                out.writeDouble(Double.parseDouble(value));
                break;
            case BIGDECIMAL:
//                out.writeString(value);
                break;
            case INTEGER:
//                out.writeI32(Integer.parseInt(value));
                break;
            case LONG:
//                out.writeI64(Long.parseLong(value));
                break;
            case SHORT:
//                out.writeI16(Short.parseShort(value));
                break;
            default:
                if (current.dataType.kind != DataType.KIND.STRING) {
                    throw new IOException("Field:" + current.fieldName + ", Not a real String!");
                }
                mH2o.writeString(value);
        }
    }

    // only used in startField
    private void push(final DataType dataType, final int tFieldPos, final int valuePos, final OptimizedMetadata.OptimizedStruct optimizedStruct, String fieldName) {
        StackNode node;

        if (nodePool.size() > 0) {
            node = nodePool.remove(nodePool.size() - 1);
        } else {
            node = new StackNode();
        }

        node.init(dataType, valuePos, tFieldPos, optimizedStruct, fieldName);
        //if(current != null)
        history.push(node);
        this.current = node;
    }

    // only used in endField
    private StackNode pop() {
        StackNode old = history.pop();
        nodePool.add(old);

        return this.current = (history.size() > 0) ? history.peek() : null;
    }

    private StackNode peek() {
        return history.size() <= 1 ? null : history.get(history.size() - 2);
    }

    private String toString(DataType type) {
        StringBuilder sb = new StringBuilder();
        sb.append(type.kind.toString());
        switch (type.kind) {
            case STRUCT:
                sb.append("(").append(type.qualifiedName).append(")");
                break;
            case LIST:
            case SET:
                sb.append("[").append(toString(type.valueType)).append("]");
                break;
            case MAP:
                sb.append("[").append(toString(type.keyType)).append(",").append(toString(type.valueType)).append("]");
                break;
        }
        return sb.toString();
    }

    private void validateStruct(StackNode current) throws IOException {
        /**
         * 不在该Struct必填字段列表的字段列表
         */
        OptimizedMetadata.OptimizedStruct struct = current.optimizedStruct;
        List<Field> fields = struct.struct.fields;
        for (int i = 0; i < fields.size(); i++) { // iterator need more allocation
            Field field = fields.get(i);
            if (field != null && !field.isOptional() && !current.fields4Struct.get(field.tag - struct.tagBase)) {
                String fieldName = current.fieldName;
                String structName = struct.struct.name;
                IOException ex = new IOException("JsonError, please check:"
                        + structName + "." + fieldName
                        + ", optimizedStruct mandatory fields missing:"
                        + field.name);
                logger.error(ex.getMessage());
                throw ex;
            }
        }
    }

    private void writeIntField(String value, DataType.KIND kind) throws IOException {
        switch (kind) {
            case SHORT:
//                out.writeI16(Short.parseShort(value));
                break;
            case INTEGER:
//                out.writeI32(Integer.parseInt(value));
                break;
            case LONG:
//                out.writeI64(Long.parseLong(value));
                break;
            default:
                logAndThrowTException();
        }
    }

    /**
     * 根据current 节点重写集合元素长度
     */
    private void reWriteByteBuf() throws IOException {
        assert isMultiElementKind(current.dataType.kind);

        //拿到当前node的开始位置以及集合元素大小
        int beginPosition = current.valuePosition;
        int elCount = current.elCount;

        //备份最新的writerIndex
        int currentIndex = requestByteBuf.writerIndex();

        requestByteBuf.writerIndex(beginPosition);

        switch (current.dataType.kind) {
            case MAP:
                reWriteMapBegin(dataType2Byte(current.dataType.keyType), dataType2Byte(current.dataType.valueType), elCount);
                break;
            case SET:
            case LIST:
                reWriteCollectionBegin(dataType2Byte(current.dataType.valueType), elCount);
                break;
            default:
                logger.error("Field:" + current.fieldName + ", won't be here", new Throwable());
        }
/*
        if (current.dataType.kind == DataType.KIND.MAP
                && invocationCtx.codecProtocol() == CompressedBinary
                && elCount == 0) {
            requestByteBuf.writerIndex(beginPosition + 1);*/
//        } else {
//            requestByteBuf.writerIndex(currentIndex);
//        }
    }

    private void writeMapBegin(byte keyType, byte valueType, int defaultSize) throws IOException {
//        out.writeMapBegin(new TMap(keyType, valueType, defaultSize));
    }

    private void reWriteMapBegin(byte keyType, byte valueType, int size) throws IOException {
//        out.writeMapBegin(new TMap(keyType, valueType, size));
    }

    /**
     * TList just the same as TSet
     *
     * @param valueType
     * @param defaultSize
     */
    //todo 其他 List，如 LinkedList等,除了 ArrayList
    private void writeCollectionBegin(byte valueType, int defaultSize) throws IOException {
//        out.writeListBegin(new TList(valueType, defaultSize));
        mH2o.writeListBegin(defaultSize, null);
    }

    private void reWriteCollectionBegin(byte valueType, int size) throws IOException {
       /* switch (invocationCtx.codecProtocol()) {
            case Binary:
                out.writeListBegin(new TList(valueType, size));
                break;
            case CompressedBinary:
            default:
                jsonCompressProtocolCodec.reWriteCollectionBegin(size, requestByteBuf);
                break;
        }*/
    }


    private void logAndThrowTException() throws IOException {
        String fieldName = current == null ? "" : current.fieldName;

        StackNode peek = peek();
        String struct = current == null ? "" : current.optimizedStruct == null ? (peek.optimizedStruct == null ? "" : peek.optimizedStruct.struct.name) : current.optimizedStruct.struct.name;
        IOException ex = new IOException("JsonError, please check:"
                + struct + "." + fieldName);
        logger.error(ex.getMessage(), ex);
        throw ex;
    }

    private void logAndThrowTException(String msg) throws IOException {
        IOException ex = new IOException("JsonError:" + msg);
        logger.error(ex.getMessage(), ex);
        throw ex;
    }

    /**
     * 用于保存当前处理节点的信息, 从之前的 immutable 调整为  mutable，并使用了一个简单的池，这样，StackNode
     * 的数量 = json的深度，而不是长度，降低内存需求
     */
    static class StackNode {

        private DataType dataType;
        /**
         * byteBuf position before this node created, maybe a Struct Field, or a Map field, or an array element
         */
        private int tFieldPosition;

        /**
         * byteBuf position after this node created
         */
        private int valuePosition;

        /**
         * optimizedStruct if dataType.kind==STRUCT
         */
        private OptimizedMetadata.OptimizedStruct optimizedStruct;

        /**
         * the field name
         */
        private String fieldName;

        /**
         * if datatype is optimizedStruct, all fieldMap parsed will be add to this set
         */
        private BitSet fields4Struct = new BitSet(64);

        /**
         * if dataType is a Collection(such as LIST, MAP, SET etc), elCount represents the size of the Collection.
         */
        int elCount = 0;

        boolean isNull = false;

        StackNode() {
        }

        public StackNode init(final DataType dataType, final int valuePosition, int tFieldPosition, final OptimizedMetadata.OptimizedStruct optimizedStruct, String fieldName) {
            this.dataType = dataType;
            this.valuePosition = valuePosition;
            this.tFieldPosition = tFieldPosition;
            this.optimizedStruct = optimizedStruct;
            this.fieldName = fieldName;

            this.fields4Struct.clear();
            this.elCount = 0;
            this.isNull = false;
            return this;
        }

        void incrElementSize() {
            elCount++;
        }

        public DataType getDataType() {
            return dataType;
        }

        public int gettFieldPosition() {
            return tFieldPosition;
        }

        public int getValuePosition() {
            return valuePosition;
        }

        public OptimizedMetadata.OptimizedStruct getOptimizedStruct() {
            return optimizedStruct;
        }

        public String getFieldName() {
            return fieldName;
        }

        public BitSet getFields4Struct() {
            return fields4Struct;
        }

    }
}