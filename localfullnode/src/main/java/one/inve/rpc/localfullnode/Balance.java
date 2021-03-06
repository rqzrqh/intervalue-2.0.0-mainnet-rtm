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
// Generated from file `Hashnet.ice'
//
// Warning: do not edit this file.
//
// </auto-generated>
//

package one.inve.rpc.localfullnode;

public class Balance implements java.lang.Cloneable,
                                java.io.Serializable
{
    public int stable;

    public int pending;

    public Balance()
    {
    }

    public Balance(int stable, int pending)
    {
        this.stable = stable;
        this.pending = pending;
    }

    public boolean equals(java.lang.Object rhs)
    {
        if(this == rhs)
        {
            return true;
        }
        Balance r = null;
        if(rhs instanceof Balance)
        {
            r = (Balance)rhs;
        }

        if(r != null)
        {
            if(this.stable != r.stable)
            {
                return false;
            }
            if(this.pending != r.pending)
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
        h_ = com.zeroc.IceInternal.HashUtil.hashAdd(h_, "::one::inve::rpc::localfullnode::Balance");
        h_ = com.zeroc.IceInternal.HashUtil.hashAdd(h_, stable);
        h_ = com.zeroc.IceInternal.HashUtil.hashAdd(h_, pending);
        return h_;
    }

    public Balance clone()
    {
        Balance c = null;
        try
        {
            c = (Balance)super.clone();
        }
        catch(CloneNotSupportedException ex)
        {
            assert false; // impossible
        }
        return c;
    }

    public void ice_writeMembers(com.zeroc.Ice.OutputStream ostr)
    {
        ostr.writeInt(this.stable);
        ostr.writeInt(this.pending);
    }

    public void ice_readMembers(com.zeroc.Ice.InputStream istr)
    {
        this.stable = istr.readInt();
        this.pending = istr.readInt();
    }

    static public void ice_write(com.zeroc.Ice.OutputStream ostr, Balance v)
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

    static public Balance ice_read(com.zeroc.Ice.InputStream istr)
    {
        Balance v = new Balance();
        v.ice_readMembers(istr);
        return v;
    }

    static public void ice_write(com.zeroc.Ice.OutputStream ostr, int tag, java.util.Optional<Balance> v)
    {
        if(v != null && v.isPresent())
        {
            ice_write(ostr, tag, v.get());
        }
    }

    static public void ice_write(com.zeroc.Ice.OutputStream ostr, int tag, Balance v)
    {
        if(ostr.writeOptional(tag, com.zeroc.Ice.OptionalFormat.VSize))
        {
            ostr.writeSize(8);
            ice_write(ostr, v);
        }
    }

    static public java.util.Optional<Balance> ice_read(com.zeroc.Ice.InputStream istr, int tag)
    {
        if(istr.readOptional(tag, com.zeroc.Ice.OptionalFormat.VSize))
        {
            istr.skipSize();
            return java.util.Optional.of(Balance.ice_read(istr));
        }
        else
        {
            return java.util.Optional.empty();
        }
    }

    private static final Balance _nullMarshalValue = new Balance();

    public static final long serialVersionUID = -4607722546854954320L;
}
