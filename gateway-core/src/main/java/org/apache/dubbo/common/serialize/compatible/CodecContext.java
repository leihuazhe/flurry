package org.apache.dubbo.common.serialize.compatible;

import org.apache.dubbo.common.threadlocal.InternalThreadLocal;

/**
 * Codec 上下文
 *
 * @author Denim.leihz 2019-08-20 4:56 PM
 */
public class CodecContext {

    /**
     * use internal thread local to improve performance
     */
    private static final InternalThreadLocal<CodecContext> LOCAL = new InternalThreadLocal<CodecContext>() {
        @Override
        protected CodecContext initialValue() {
            return new CodecContext();
        }
    };

    private CodecContext() {
    }

    private boolean useJsonDecoder;

    /**
     * get context.
     *
     * @return context
     */
    public static CodecContext getContext() {
        return LOCAL.get();
    }

    public static void restoreContext(CodecContext oldContext) {
        LOCAL.set(oldContext);
    }

    /**
     * remove context.
     */
    public static void removeContext() {
        LOCAL.remove();
    }


    public boolean isUseJsonDecoder() {
        return useJsonDecoder;
    }

    public void setUseJsonDecoder(boolean useJsonDecoder) {
        this.useJsonDecoder = useJsonDecoder;
    }
}
