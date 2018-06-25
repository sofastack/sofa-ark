/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alipay.sofa.ark.container.session.handler;

import com.alipay.sofa.ark.common.util.AssertUtils;
import com.alipay.sofa.ark.common.util.StringUtils;
import com.alipay.sofa.ark.common.util.SimpleByteBuffer;
import com.alipay.sofa.ark.spi.constant.Constants;
import com.alipay.sofa.ark.spi.service.session.TelnetSession;
import com.alipay.sofa.ark.container.session.handler.AbstractTerminalTypeMapping.KEYS;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Handle telnet protocol
 *
 * @author qilong.zql
 * @since 0.4.0
 */
public class TelnetProtocolHandler implements Runnable {

    /**
     * Telnet session
     */
    private final TelnetSession                      telnetSession;

    /**
     * Telnet session inputStream
     */
    private final InputStream                        in;

    /**
     * Telnet session outputStream
     */
    private final OutputStream                       out;

    private byte[]                                   buffer;
    private String                                   telnetCommand;
    private String                                   escCommand;
    private SimpleByteBuffer                         arkCommandBuffer   = new SimpleByteBuffer();

    private String                                   clientTerminalType = AbstractTerminalTypeMapping
                                                                            .getDefaultTerminalType();
    private Map<String, AbstractTerminalTypeMapping> terminalTypeMapping;

    /**
     * Ark Command Handler
     */
    private ArkCommandHandler                        commandHandler     = new ArkCommandHandler();

    /**
     * Telnet NVT
     * http://www.ietf.org/rfc/rfc854.txt
     * https://www.ietf.org/rfc/rfc1091.txt
     */
    private final static byte                        IAC                = (byte) 255;
    private final static byte                        WILL               = (byte) 251;
    private final static byte                        WONT               = (byte) 252;
    private final static byte                        DO                 = (byte) 253;
    private final static byte                        DONT               = (byte) 254;
    private final static byte                        SB                 = (byte) 250;
    private final static byte                        SE                 = (byte) 240;
    private final static byte                        ECHO               = 1;
    private final static byte                        GA                 = 3;
    private final static byte                        NAWS               = 31;
    private final static byte                        TERMINAL_TYPE      = 24;

    /**
     * Special Character Value
     */
    private final static byte                        MIN_VISUAL_BYTE    = 32;
    private final static byte                        MAX_VISUAL_BYTE    = 126;
    private final static byte                        SPACE              = 32;
    private final static byte                        ESC                = 27;
    private final static byte                        BS                 = 8;
    private final static byte                        LF                 = 10;
    private final static byte                        CR                 = 13;

    private boolean                                  isHandlingCommand;
    private boolean                                  isWill;
    private boolean                                  isWont;
    private boolean                                  isDo;
    private boolean                                  isDont;
    private boolean                                  isSb;
    private boolean                                  isEsc;
    private boolean                                  isCr;

    public TelnetProtocolHandler(TelnetSession telnetSession) {
        this.telnetSession = telnetSession;
        this.in = telnetSession.getInput();
        this.out = telnetSession.getOutput();
        buffer = new byte[Constants.BUFFER_CHUNK];
        reset();
        terminalTypeMapping = new HashMap<>();
        terminalTypeMapping.put("ANSI", new AbstractTerminalTypeMapping((byte) 8, (byte) 127) {
        });
        terminalTypeMapping.put("VT100", new AbstractTerminalTypeMapping((byte) 127, (byte) -1) {
        });
        terminalTypeMapping.put("VT200", new AbstractTerminalTypeMapping((byte) 127, (byte) -1) {
        });
        terminalTypeMapping.put("VT320", new AbstractTerminalTypeMapping((byte) 8, (byte) 127) {
        });
        terminalTypeMapping.put("SCO", new AbstractTerminalTypeMapping((byte) -1, (byte) 127) {
        });
        terminalTypeMapping.put("XTERM", new AbstractTerminalTypeMapping((byte) 127, (byte) -1) {
        });
    }

    @Override
    public void run() {
        try {
            int count;
            while ((count = in.read(buffer)) > -1 && telnetSession.isAlive()) {
                for (int i = 0; i < count; ++i) {
                    byteScanner(buffer[i]);
                }
            }
        } catch (IOException ex) {
            // ignore
        } finally {
            telnetSession.close();
        }

    }

    private void byteScanner(byte b) throws IOException {
        b &= 0xFF;
        if (isHandlingCommand) {
            switch (b) {
                case WILL:
                    isWill = true;
                    telnetCommand += (char) WONT;
                    break;
                case WONT:
                    isWont = true;
                    telnetCommand += (char) DONT;
                    break;
                case DO:
                    isDo = true;
                    telnetCommand += (char) WONT;
                    break;
                case DONT:
                    isDont = true;
                    telnetCommand += (char) WONT;
                    break;
                case SB:
                    isSb = true;
                    telnetCommand += (char) SB;
                    break;
                default:
                    handleCommand(b);
                    break;
            }
        } else if (b == IAC) {
            isHandlingCommand = true;
            telnetCommand += (char) IAC;
        } else {
            handleData(b);
        }
    }

    private void handleData(byte b) throws IOException {
        if (isEsc) {
            escCommand += (char) b;
            KEYS key = terminalTypeMapping.get(clientTerminalType).getMatchKeys(escCommand);
            handleEscCommand(key);
        } else if (b == ESC) {
            isEsc = true;
        } else if (b == terminalTypeMapping.get(clientTerminalType).getBackspace()) {
            erase();
            arkCommandBuffer.backSpace();
            render();
            out.flush();
        } else if (b == terminalTypeMapping.get(clientTerminalType).getDEL()) {
            erase();
            arkCommandBuffer.delete();
            render();
            out.flush();
        } else if (MIN_VISUAL_BYTE <= b && b <= MAX_VISUAL_BYTE) {
            if (arkCommandBuffer.getGap() > 0) {
                erase();
                arkCommandBuffer.insert(b);
                render();
                out.flush();
            } else {
                arkCommandBuffer.insert(b);
                out.write(b);
                out.flush();
            }
        } else if ((b == LF && !isCr) || b == CR) {
            out.write(new byte[] { CR, LF });
            out.flush();
            handleCommand();
        }
        isCr = (b == CR);
    }

    private void handleCommand() {
        try {
            echoResponse(commandHandler.handleCommand(getArkCommand()));
            echoPrompt();
        } catch (Throwable throwable) {
            telnetSession.close();
        }
    }

    private void erase() throws IOException {
        for (int i = 0; i < arkCommandBuffer.getPos(); ++i) {
            out.write(BS);
        }
        for (int i = 0; i < arkCommandBuffer.getSize(); ++i) {
            out.write(SPACE);
        }
        for (int i = 0; i < arkCommandBuffer.getSize(); ++i) {
            out.write(BS);
        }
    }

    private void render() throws IOException {
        for (byte b : arkCommandBuffer.getBuffer()) {
            out.write(b);
        }
        for (int i = 0; i < arkCommandBuffer.getGap(); ++i) {
            out.write(BS);
        }
    }

    private void handleEscCommand(KEYS key) throws IOException {
        switch (key) {
            case LEFT:
                if (arkCommandBuffer.goLeft()) {
                    out.write(BS);
                    out.flush();
                }
                reset();
                break;
            case RIGHT:
                byte b = arkCommandBuffer.goRight();
                if (b != -1) {
                    out.write(b);
                    out.flush();
                }
                reset();
                break;
            case DEL:
                erase();
                arkCommandBuffer.delete();
                render();
                out.flush();
                reset();
                break;
            case UNFINISHED:
                break;
            case UNKNOWN:
                reset();
                break;
            default:
                break;
        }
    }

    /**
     * Handle telnet command
     * @param b
     */
    private void handleCommand(byte b) throws IOException {
        if (b == SE) {
            telnetCommand += (char) b;
            handleNegotiation();
            reset();
        } else if (isWill || isDo) {
            if (isWill && b == TERMINAL_TYPE) {
                out.write(new byte[] { IAC, SB, TERMINAL_TYPE, ECHO, IAC, SE });
                out.flush();
            } else if (b != ECHO && b != GA && b != NAWS) {
                telnetCommand += (char) b;
                out.write(telnetCommand.getBytes());
                out.flush();
            }
            reset();
        } else if (isWont || isDont) {
            telnetCommand += (char) b;
            out.write(telnetCommand.getBytes());
            out.flush();
            reset();
        } else if (isSb) {
            telnetCommand += (char) b;
        }
    }

    /**
     * Handle negotiation of terminal type
     */
    private void handleNegotiation() throws IOException {
        if (telnetCommand.contains(new String(new byte[] { TERMINAL_TYPE }))) {
            //  IAC SB TERMINAL_TYPE IS XXX IAC SE
            boolean isSuccess = false;
            clientTerminalType = telnetCommand.substring(4, telnetCommand.length() - 2);
            for (String terminalType : terminalTypeMapping.keySet()) {
                if (clientTerminalType.contains(terminalType)) {
                    isSuccess = true;
                    clientTerminalType = terminalType;
                    echoPrompt();
                    break;
                }
            }
            if (!isSuccess) {
                out.write("TerminalType negotiate failed.".getBytes());
                out.flush();
                telnetSession.close();
            }
        }
    }

    private void reset() {
        isWill = false;
        isDo = false;
        isWont = false;
        isDont = false;
        isSb = false;
        isHandlingCommand = false;
        telnetCommand = StringUtils.EMPTY_STRING;
        isEsc = false;
        escCommand = StringUtils.EMPTY_STRING;
    }

    private String getArkCommand() {
        return new String(arkCommandBuffer.getAndClearBuffer());
    }

    private void echoResponse(String content) throws IOException {
        AssertUtils.assertNotNull(content, "Echo message should not be null");
        content = content.replace("\n", Constants.TELNET_STRING_END);
        if (StringUtils.isEmpty(content)) {
            content = Constants.TELNET_STRING_END;
        } else if (!content.endsWith(Constants.TELNET_STRING_END)) {
            content = content + Constants.TELNET_STRING_END + Constants.TELNET_STRING_END;
        } else if (!content.endsWith(Constants.TELNET_STRING_END
            .concat(Constants.TELNET_STRING_END))) {
            content = content + Constants.TELNET_STRING_END;
        }
        out.write(content.getBytes());
        out.flush();
    }

    private void echoPrompt() throws IOException {
        out.write(Constants.TELNET_SESSION_PROMPT.getBytes());
        out.flush();
    }
}