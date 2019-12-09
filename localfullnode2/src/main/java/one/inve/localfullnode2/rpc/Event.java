// **********************************************************************
//
// Copyright (c) 2003-2018 ZeroC, Inc. All rights reserved.
//
// This copy of Ice is licensed to you under the terms described in the
// ICE_LICENSE file included in this distribution.
//
// **********************************************************************
//
// Ice version 3.7.1
//
// <auto-generated>
//
// Generated from file `Hashnet1.ice'
//
// Warning: do not edit this file.
//
// </auto-generated>
//

package one.inve.localfullnode2.rpc;

public class Event implements java.lang.Cloneable,
                              java.io.Serializable
{
    public int shardId;

    public long selfId;

    public long selfSeq;

    public long otherId;

    public long otherSeq;

    public byte[][] messages;

    public long timeCreatedSecond;

    public int timeCreatedNano;

    public byte[] sign;

    public boolean isFamous;

    public byte[] hash;

    public long generation;

    public long consensusTimestampSecond;

    public int consensusTimestampNano;

    public byte[] otherHash;

    public byte[] parentHash;

    public Event()
    {
    }

    public Event(int shardId, long selfId, long selfSeq, long otherId, long otherSeq, byte[][] messages, long timeCreatedSecond, int timeCreatedNano, byte[] sign, boolean isFamous, byte[] hash, long generation, long consensusTimestampSecond, int consensusTimestampNano, byte[] otherHash, byte[] parentHash)
    {
        this.shardId = shardId;
        this.selfId = selfId;
        this.selfSeq = selfSeq;
        this.otherId = otherId;
        this.otherSeq = otherSeq;
        this.messages = messages;
        this.timeCreatedSecond = timeCreatedSecond;
        this.timeCreatedNano = timeCreatedNano;
        this.sign = sign;
        this.isFamous = isFamous;
        this.hash = hash;
        this.generation = generation;
        this.consensusTimestampSecond = consensusTimestampSecond;
        this.consensusTimestampNano = consensusTimestampNano;
        this.otherHash = otherHash;
        this.parentHash = parentHash;
    }

    public boolean equals(java.lang.Object rhs)
    {
        if(this == rhs)
        {
            return true;
        }
        Event r = null;
        if(rhs instanceof Event)
        {
            r = (Event)rhs;
        }

        if(r != null)
        {
            if(this.shardId != r.shardId)
            {
                return false;
            }
            if(this.selfId != r.selfId)
            {
                return false;
            }
            if(this.selfSeq != r.selfSeq)
            {
                return false;
            }
            if(this.otherId != r.otherId)
            {
                return false;
            }
            if(this.otherSeq != r.otherSeq)
            {
                return false;
            }
            if(!java.util.Arrays.equals(this.messages, r.messages))
            {
                return false;
            }
            if(this.timeCreatedSecond != r.timeCreatedSecond)
            {
                return false;
            }
            if(this.timeCreatedNano != r.timeCreatedNano)
            {
                return false;
            }
            if(!java.util.Arrays.equals(this.sign, r.sign))
            {
                return false;
            }
            if(this.isFamous != r.isFamous)
            {
                return false;
            }
            if(!java.util.Arrays.equals(this.hash, r.hash))
            {
                return false;
            }
            if(this.generation != r.generation)
            {
                return false;
            }
            if(this.consensusTimestampSecond != r.consensusTimestampSecond)
            {
                return false;
            }
            if(this.consensusTimestampNano != r.consensusTimestampNano)
            {
                return false;
            }
            if(!java.util.Arrays.equals(this.otherHash, r.otherHash))
            {
                return false;
            }
            if(!java.util.Arrays.equals(this.parentHash, r.parentHash))
            {
                return false;
            }

            return true;
        }

        return false;
    }

    public int hashCode()
    {
        int h_ = 5381;
        h_ = com.zeroc.IceInternal.HashUtil.hashAdd(h_, "::rpc::Event");
        h_ = com.zeroc.IceInternal.HashUtil.hashAdd(h_, shardId);
        h_ = com.zeroc.IceInternal.HashUtil.hashAdd(h_, selfId);
        h_ = com.zeroc.IceInternal.HashUtil.hashAdd(h_, selfSeq);
        h_ = com.zeroc.IceInternal.HashUtil.hashAdd(h_, otherId);
        h_ = com.zeroc.IceInternal.HashUtil.hashAdd(h_, otherSeq);
        h_ = com.zeroc.IceInternal.HashUtil.hashAdd(h_, messages);
        h_ = com.zeroc.IceInternal.HashUtil.hashAdd(h_, timeCreatedSecond);
        h_ = com.zeroc.IceInternal.HashUtil.hashAdd(h_, timeCreatedNano);
        h_ = com.zeroc.IceInternal.HashUtil.hashAdd(h_, sign);
        h_ = com.zeroc.IceInternal.HashUtil.hashAdd(h_, isFamous);
        h_ = com.zeroc.IceInternal.HashUtil.hashAdd(h_, hash);
        h_ = com.zeroc.IceInternal.HashUtil.hashAdd(h_, generation);
        h_ = com.zeroc.IceInternal.HashUtil.hashAdd(h_, consensusTimestampSecond);
        h_ = com.zeroc.IceInternal.HashUtil.hashAdd(h_, consensusTimestampNano);
        h_ = com.zeroc.IceInternal.HashUtil.hashAdd(h_, otherHash);
        h_ = com.zeroc.IceInternal.HashUtil.hashAdd(h_, parentHash);
        return h_;
    }

    public Event clone()
    {
        Event c = null;
        try
        {
            c = (Event)super.clone();
        }
        catch(CloneNotSupportedException ex)
        {
            assert false; // impossible
        }
        return c;
    }

    public void ice_writeMembers(com.zeroc.Ice.OutputStream ostr)
    {
        ostr.writeInt(this.shardId);
        ostr.writeLong(this.selfId);
        ostr.writeLong(this.selfSeq);
        ostr.writeLong(this.otherId);
        ostr.writeLong(this.otherSeq);
        MessageListHelper.write(ostr, this.messages);
        ostr.writeLong(this.timeCreatedSecond);
        ostr.writeInt(this.timeCreatedNano);
        ostr.writeByteSeq(this.sign);
        ostr.writeBool(this.isFamous);
        ostr.writeByteSeq(this.hash);
        ostr.writeLong(this.generation);
        ostr.writeLong(this.consensusTimestampSecond);
        ostr.writeInt(this.consensusTimestampNano);
        ostr.writeByteSeq(this.otherHash);
        ostr.writeByteSeq(this.parentHash);
    }

    public void ice_readMembers(com.zeroc.Ice.InputStream istr)
    {
        this.shardId = istr.readInt();
        this.selfId = istr.readLong();
        this.selfSeq = istr.readLong();
        this.otherId = istr.readLong();
        this.otherSeq = istr.readLong();
        this.messages = MessageListHelper.read(istr);
        this.timeCreatedSecond = istr.readLong();
        this.timeCreatedNano = istr.readInt();
        this.sign = istr.readByteSeq();
        this.isFamous = istr.readBool();
        this.hash = istr.readByteSeq();
        this.generation = istr.readLong();
        this.consensusTimestampSecond = istr.readLong();
        this.consensusTimestampNano = istr.readInt();
        this.otherHash = istr.readByteSeq();
        this.parentHash = istr.readByteSeq();
    }

    static public void ice_write(com.zeroc.Ice.OutputStream ostr, Event v)
    {
        if(v == null)
        {
            _nullMarshalValue.ice_writeMembers(ostr);
        }
        else
        {
            v.ice_writeMembers(ostr);
        }
    }

    static public Event ice_read(com.zeroc.Ice.InputStream istr)
    {
        Event v = new Event();
        v.ice_readMembers(istr);
        return v;
    }

    static public void ice_write(com.zeroc.Ice.OutputStream ostr, int tag, java.util.Optional<Event> v)
    {
        if(v != null && v.isPresent())
        {
            ice_write(ostr, tag, v.get());
        }
    }

    static public void ice_write(com.zeroc.Ice.OutputStream ostr, int tag, Event v)
    {
        if(ostr.writeOptional(tag, com.zeroc.Ice.OptionalFormat.FSize))
        {
            int pos = ostr.startSize();
            ice_write(ostr, v);
            ostr.endSize(pos);
        }
    }

    static public java.util.Optional<Event> ice_read(com.zeroc.Ice.InputStream istr, int tag)
    {
        if(istr.readOptional(tag, com.zeroc.Ice.OptionalFormat.FSize))
        {
            istr.skip(4);
            return java.util.Optional.of(Event.ice_read(istr));
        }
        else
        {
            return java.util.Optional.empty();
        }
    }

    private static final Event _nullMarshalValue = new Event();

    public static final long serialVersionUID = -204914055L;
}
