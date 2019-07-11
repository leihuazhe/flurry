package com.yunji.json.serializer;

/**
 * @author Denim.leihz 2019-07-08 8:43 PM
 */

import com.yunji.json.util.JException;
import org.apache.dubbo.common.serialize.ObjectInput;
import org.apache.dubbo.common.serialize.ObjectOutput;

import java.io.IOException;

/**
 * 通用编解码器接口
 *
 * @author ever
 * @date 2017/7/17
 */
public interface BeanSerializer<T> {

    /**
     * 反序列化方法, 从Thrift协议格式中转换回PoJo
     *
     * @param iproto
     * @return
     * @throws JException
     */
    T read(ObjectInput iproto) throws IOException;

    /**
     * 序列化方法, 把PoJo转换为Thrift协议格式
     *
     * @param bean
     * @param oproto
     * @throws JException
     */
    void write(T bean, ObjectOutput oproto) throws Exception;

    /**
     * PoJo校验方法
     *
     * @param bean
     * @throws JException
     */
    void validate(T bean) throws JException;

    /**
     * 输出对人友好的信息
     *
     * @param bean
     * @return
     */
    String toString(T bean);
}

