/**
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.  The ASF licenses this file to you under the Apache License, Version
 * 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */

package org.apache.storm.messaging.netty;

import org.apache.storm.shade.io.netty.buffer.ByteBuf;
import org.apache.storm.shade.io.netty.buffer.Unpooled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Send and receive SASL tokens.
 */
public class SaslMessageToken implements INettySerializable {
    public static final short IDENTIFIER = -500;

    /**
     * Class logger
     */
    private static final Logger LOG = LoggerFactory
        .getLogger(SaslMessageToken.class);

    /**
     * Used for client or server's token to send or receive from each other.
     */
    private byte[] token;

    /**
     * Constructor used for reflection only.
     */
    public SaslMessageToken() {
    }

    /**
     * Constructor used to send request.
     *
     * @param token the SASL token, generated by a SaslClient or SaslServer.
     */
    public SaslMessageToken(byte[] token) {
        this.token = token;
    }

    public static SaslMessageToken read(byte[] serial) {
        ByteBuf sm_buffer = Unpooled.wrappedBuffer(serial);
        try {
        short identifier = sm_buffer.readShort();
        int payload_len = sm_buffer.readInt();
        if (identifier != IDENTIFIER) {
            return null;
        }
        byte token[] = new byte[payload_len];
        sm_buffer.readBytes(token, 0, payload_len);
        return new SaslMessageToken(token);
        } finally {
            sm_buffer.release();
        }
    }

    /**
     * Read accessor for SASL token
     *
     * @return saslToken SASL token
     */
    public byte[] getSaslToken() {
        return token;
    }

    /**
     * Write accessor for SASL token
     *
     * @param token SASL token
     */
    public void setSaslToken(byte[] token) {
        this.token = token;
    }

    @Override
    public int encodeLength() {
        return 2 + 4 + token.length;
    }

    /**
     * encode the current SaslToken Message into a ByteBuf.
     * 
     * <p>SaslTokenMessageRequest is encoded as: identifier .... short(2) payload
     * length .... int payload .... byte[]
     */
    @Override
    public void write(ByteBuf dest) {
        int payload_len = 0;
        if (token != null) {
            payload_len = token.length;
        }

        dest.writeShort(IDENTIFIER);
        dest.writeInt(payload_len);

        if (payload_len > 0) {
            dest.writeBytes(token);
        }
    }
}
