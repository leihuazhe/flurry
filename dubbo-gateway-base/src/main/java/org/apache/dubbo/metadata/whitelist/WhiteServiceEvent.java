package org.apache.dubbo.metadata.whitelist;

import java.util.EventObject;

/**
 * @author Denim.leihz 2019-07-26 10:57 AM
 */
public class WhiteServiceEvent extends EventObject {
    /**
     * Constructs a prototypical Event.
     *
     * @param context The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    public WhiteServiceEvent(ConfigContext context) {
        super(context);
    }

    /**
     * The object on which the Event initially occurred.
     *
     * @return The object on which the Event initially occurred.
     */
    @Override
    public ConfigContext getSource() {
        return (ConfigContext) super.getSource();
    }
}
