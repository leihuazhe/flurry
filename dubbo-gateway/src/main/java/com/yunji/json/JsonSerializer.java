package com.yunji.json;

import com.alibaba.com.caucho.hessian.io.Hessian2Output;
import com.yunji.metadata.tag.Method;
import com.yunji.serialization.BeanSerializer;
import com.yunji.serialization.hessian2.CustomHessian2ObjectOutput;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.apache.dubbo.common.serialize.ObjectOutput;
import org.apache.dubbo.common.serialize.hessian2.Hessian2ObjectOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class JsonSerializer implements BeanSerializer<String> {

    private final Logger logger = LoggerFactory.getLogger(JsonSerializer.class);

    private final static long MAX_JSON_LONG = 1L << 53;
    private final static long MIN_JSON_LONG = -1L << 53;

    private final OptimizedMetadata.OptimizedStruct optimizedStruct;
    private final OptimizedMetadata.OptimizedService optimizedService;
    private final Method method;
    private final String version;
    private ByteBuf requestByteBuf = ByteBufAllocator.DEFAULT.buffer();

    private Hessian2Output mH2o;

    public JsonSerializer(OptimizedMetadata.OptimizedService optimizedService,
                          Method method, String version,
                          OptimizedMetadata.OptimizedStruct optimizedStruct) {
        this.optimizedStruct = optimizedStruct;
        this.optimizedService = optimizedService;
        this.method = method;
        this.version = version;
    }

    /**
     * thrift -> json
     *
     * @param iproto
     * @return
     * @throws JException
     */
    @Override
    public String read(ObjectOutput iproto) throws JException {
//        JsonWriter writer = new JsonWriter();
//        read(iproto, writer);
//        return writer.toString();
        return null;
    }


    /**
     * json -> thrift
     *
     * @throws JException
     */
    @Override
    public void write(String input, ObjectOutput oproto) throws Exception {
        JsonReader jsonReader = null;
        try {
            jsonReader = new JsonReader(optimizedStruct, optimizedService, requestByteBuf, (CustomHessian2ObjectOutput) oproto);
            new JsonParser(input, jsonReader).parseJsValue();
            //write end
//            writeObjectListEnd(oproto);
        } catch (Exception e) {
            if (jsonReader != null && jsonReader.current != null) {
                String errorMsg = "Please check field:" + jsonReader.current.getFieldName();
                logger.error(errorMsg + "\n" + e.getMessage(), e);
                throw new JException(errorMsg);
            }
            throw e;
        }
    }

    @Override
    public void validate(String s) throws JException {

    }

    @Override
    public String toString(String s) {
        return s;
    }

    public void setRequestByteBuf(ByteBuf requestByteBuf) {
        this.requestByteBuf = requestByteBuf;
    }

    private void writeObjectListEnd(ObjectOutput out) throws Exception {
        if (mH2o == null) {
            java.lang.reflect.Field filed = out.getClass().getDeclaredField("mH2o");
            filed.setAccessible(true);
            this.mH2o = (Hessian2Output) filed.get(out);
        }
        mH2o.writeListEnd();
    }

//    private void read(ObjectOutput iproto, JsonCallback writer) throws JException {
//        iproto.readStructBegin();
//        writer.onStartObject();
//
//        while (true) {
//            TField field = iproto.readFieldBegin();
//            if (field.type == TType.STOP) break;
//
//            Field fld = optimizedStruct.get(field.id);
//
//            boolean skip = fld == null;
//
//            if (!skip) {
//                writer.onStartField(fld.name);
//                readField(iproto, fld.dataType, field.type, writer);
//                writer.onEndField();
//            } else { // skip reading
//                TProtocolUtil.skip(iproto, field.type);
//            }
//
//            iproto.readFieldEnd();
//        }
//
//
//        iproto.readStructEnd();
//        writer.onEndObject();
//    }

  /*  private void readField(ObjectOutput iproto, DataType fieldDataType, byte fieldType,
                           JsonCallback writer) throws JException {
        switch (fieldType) {
            case TType.VOID:
                break;
            case TType.BOOL:
                boolean boolValue = iproto.readBool();
                writer.onBoolean(boolValue);
                break;
            case TType.BYTE:
                byte b = iproto.readByte();
                writer.onNumber(b);
                break;
            case TType.DOUBLE:
                double dValue = iproto.readDouble();
                writer.onNumber(dValue);
                break;
            case TType.I16:
                short sValue = iproto.readI16();
                writer.onNumber(sValue);
                break;
            case TType.I32:
                int iValue = iproto.readI32();
                if (fieldDataType != null && fieldDataType.kind == DataType.KIND.ENUM) {
                    String enumLabel = findEnumItemLabel(optimizedService.enumMap.get(fieldDataType.qualifiedName), iValue);
                    writer.onString(enumLabel);
                } else {
                    writer.onNumber(iValue);
                }
                break;
            case TType.I64:
                long lValue = iproto.readI64();
                if (lValue <= MAX_JSON_LONG && lValue >= MIN_JSON_LONG) {
                    writer.onNumber(lValue);
                } else {
                    writer.onString(String.valueOf(lValue));
                }
                break;
            case TType.STRING:
                String strValue = iproto.readString();
                writer.onString(strValue);
                break;
            case TType.STRUCT:
                String subStructName = fieldDataType.qualifiedName;
                OptimizedMetadata.OptimizedStruct subStruct = optimizedService.optimizedStructs.get(subStructName);
                new JsonSerializer(optimizedService, method, version, subStruct).read(iproto, writer);

                break;
            case TType.MAP:
                TMap map = iproto.readMapBegin();
                writer.onStartObject();
                for (int index = 0; index < map.size; index++) {
                    switch (map.keyType) {
                        case TType.STRING:
                            writer.onStartField(iproto.readString());
                            break;
                        case TType.I16:
                            writer.onStartField(String.valueOf(iproto.readI16()));
                            break;
                        case TType.I32:
                            writer.onStartField(String.valueOf(iproto.readI32()));
                            break;
                        case TType.I64:
                            writer.onStartField(String.valueOf(iproto.readI64()));
                            break;
                        default:
                            logger.error("won't be here", new Throwable());
                    }

                    readField(iproto, fieldDataType.valueType, map.valueType, writer);
                    writer.onEndField();
                }
                writer.onEndObject();
                break;
            case TType.SET:
                TSet set = iproto.readSetBegin();
                writer.onStartArray();
                readCollection(set.size, set.elemType, fieldDataType.valueType, fieldDataType.valueType.valueType, iproto, writer);
                writer.onEndArray();

                break;
            case TType.LIST:
                TList list = iproto.readListBegin();
                writer.onStartArray();
                readCollection(list.size, list.elemType, fieldDataType.valueType, fieldDataType.valueType.valueType, iproto, writer);
                writer.onEndArray();
                break;
            default:

        }
    }*/

    /**
     * @param size
     * @param elemType     thrift的数据类型
     * @param metadataType metaData的DataType
     * @param iproto
     * @param writer
     * @throws JException
     */
    /*private void readCollection(int size, byte elemType, DataType metadataType,
                                DataType subMetadataType, TProtocol iproto,
                                JsonCallback writer) throws JException {
        OptimizedMetadata.OptimizedStruct struct = null;
        if (metadataType.kind == DataType.KIND.STRUCT) {
            struct = optimizedService.optimizedStructs.get(metadataType.qualifiedName);
        }
        for (int index = 0; index < size; index++) {
            //没有嵌套结构,也就是原始数据类型, 例如int, boolean,string等
            if (!isComplexKind(metadataType.kind)) {
                readField(iproto, metadataType, elemType, writer);
            } else {
                if (struct != null) {
                    new JsonSerializer(optimizedService, method, version, struct).read(iproto, writer);
                } else if (isCollectionKind(metadataType.kind)) {
                    //处理List<list<>>
                    TList list = iproto.readListBegin();
                    writer.onStartArray();
                    readCollection(list.size, list.elemType, subMetadataType, subMetadataType.valueType, iproto, writer);
                    writer.onEndArray();
                } else if (metadataType.kind == DataType.KIND.MAP) {
                    readField(iproto, metadataType, elemType, writer);
                }
            }
            writer.onEndField();
        }

    }*/
}
