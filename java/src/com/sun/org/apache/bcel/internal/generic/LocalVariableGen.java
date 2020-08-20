/*
 * Copyright (c) 2007, 2020, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sun.org.apache.bcel.internal.generic;

import com.sun.org.apache.bcel.internal.Const;
import com.sun.org.apache.bcel.internal.classfile.LocalVariable;

/**
 * This class represents a local variable within a method. It contains its
 * scope, name and type. The generated LocalVariable object can be obtained with
 * getLocalVariable which needs the instruction list and the constant pool as
 * parameters.
 *
 * @version $Id: LocalVariableGen.java 1749603 2016-06-21 20:50:19Z ggregory $
 * @see LocalVariable
 * @see MethodGen
 */
public class LocalVariableGen implements InstructionTargeter, NamedAndTyped, Cloneable {

    private int index;
    private String name;
    private Type type;
    private InstructionHandle start;
    private InstructionHandle end;

    /**
     * Generate a local variable that with index `index'. Note that double and
     * long variables need two indexs. Index indices have to be provided by the
     * user.
     *
     * @param index index of local variable
     * @param name its name
     * @param type its type
     * @param start from where the instruction is valid (null means from the
     * start)
     * @param end until where the instruction is valid (null means to the end)
     */
    public LocalVariableGen(final int index, final String name, final Type type,
            final InstructionHandle start, final InstructionHandle end) {
        if ((index < 0) || (index > Const.MAX_SHORT)) {
            throw new ClassGenException("Invalid index index: " + index);
        }
        this.name = name;
        this.type = type;
        this.index = index;
        setStart(start);
        setEnd(end);
    }

    /**
     * Get LocalVariable object.
     *
     * This relies on that the instruction list has already been dumped to byte
     * code or or that the `setPositions' methods has been called for the
     * instruction list.
     *
     * Note that for local variables whose scope end at the last instruction of
     * the method's code, the JVM specification is ambiguous: both a
     * start_pc+length ending at the last instruction and start_pc+length ending
     * at first index beyond the end of the code are valid.
     *
     * @param cp constant pool
     */
    public LocalVariable getLocalVariable(final ConstantPoolGen cp) {
        int start_pc = 0;
        int length = 0;
        if ((start != null) && (end != null)) {
            start_pc = start.getPosition();
            length = end.getPosition() - start_pc;
            if (end.getNext() == null) {
                length += end.getInstruction().getLength();
            }
        }
        final int name_index = cp.addUtf8(name);
        final int signature_index = cp.addUtf8(type.getSignature());
        return new LocalVariable(start_pc, length, name_index, signature_index, index, cp
                .getConstantPool());
    }

    public void setIndex(final int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setType(final Type type) {
        this.type = type;
    }

    @Override
    public Type getType() {
        return type;
    }

    public InstructionHandle getStart() {
        return start;
    }

    public InstructionHandle getEnd() {
        return end;
    }

    public void setStart(final InstructionHandle start) { // TODO could be package-protected?
        BranchInstruction.notifyTarget(this.start, start, this);
        this.start = start;
    }

    public void setEnd(final InstructionHandle end) { // TODO could be package-protected?
        BranchInstruction.notifyTarget(this.end, end, this);
        this.end = end;
    }

    /**
     * @param old_ih old target, either start or end
     * @param new_ih new target
     */
    @Override
    public void updateTarget(final InstructionHandle old_ih, final InstructionHandle new_ih) {
        boolean targeted = false;
        if (start == old_ih) {
            targeted = true;
            setStart(new_ih);
        }
        if (end == old_ih) {
            targeted = true;
            setEnd(new_ih);
        }
        if (!targeted) {
            throw new ClassGenException("Not targeting " + old_ih + ", but {" + start + ", " + end
                    + "}");
        }
    }

    /**
     * Clear the references from and to this variable when it's removed.
     */
    void dispose() {
        setStart(null);
        setEnd(null);
    }

    /**
     * @return true, if ih is target of this variable
     */
    @Override
    public boolean containsTarget(final InstructionHandle ih) {
        return (start == ih) || (end == ih);
    }

    @Override
    public int hashCode() {
        // If the user changes the name or type, problems with the targeter hashmap will occur.
        // Note: index cannot be part of hash as it may be changed by the user.
        return name.hashCode() ^ type.hashCode();
    }

    /**
     * We consider to local variables to be equal, if the use the same index and
     * are valid in the same range.
     */
    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof LocalVariableGen)) {
            return false;
        }
        final LocalVariableGen l = (LocalVariableGen) o;
        return (l.index == index) && (l.start == start) && (l.end == end);
    }

    @Override
    public String toString() {
        return "LocalVariableGen(" + name + ", " + type + ", " + start + ", " + end + ")";
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (final CloneNotSupportedException e) {
            throw new Error("Clone Not Supported"); // never happens
        }
    }
}