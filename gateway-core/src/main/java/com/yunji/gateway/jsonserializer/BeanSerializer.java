package com.yunji.gateway.jsonserializer;


import org.apache.dubbo.common.serialize.ObjectInput;
import org.apache.dubbo.common.serialize.ObjectOutput;

import java.io.IOException;


/**
 * 通用编解码器接口
 *
 * @author Denim.leihz 2019-07-08 8:43 PM
 */
public interface BeanSerializer<T> {

    /**
     * 反序列化方法, 从Thrift协议格式中转换回PoJo
     *
     * @param iproto
     * @return
     */
    T read(ObjectInput iproto) throws IOException;

    /**
     * 序列化方法, 把PoJo转换为Thrift协议格式
     *
     * @param bean
     * @param oproto
     */
    void write(T bean, ObjectOutput oproto) throws Exception;

    /**
     * PoJo校验方法
     *
     * @param bean
     */
    void validate(T bean) throws IOException;

    /**
     * 输出对人友好的信息
     *
     * @param bean
     * @return
     */
    String toString(T bean);
}

