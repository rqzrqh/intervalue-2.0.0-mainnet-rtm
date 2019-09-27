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
// Generated from file `sync.ice'
//
// Warning: do not edit this file.
//
// </auto-generated>
//

package one.inve.localfullnode2.sync.rpc.gen;

public final class MerkleTreeizedSyncEventListHelper
{
    public static void write(com.zeroc.Ice.OutputStream ostr, MerkleTreeizedSyncEvent[] v)
    {
        if(v == null)
        {
            ostr.writeSize(0);
        }
        else
        {
            ostr.writeSize(v.length);
            for(int i0 = 0; i0 < v.length; i0++)
            {
                MerkleTreeizedSyncEvent.ice_write(ostr, v[i0]);
            }
        }
    }

    public static MerkleTreeizedSyncEvent[] read(com.zeroc.Ice.InputStream istr)
    {
        final MerkleTreeizedSyncEvent[] v;
        final int len0 = istr.readAndCheckSeqSize(76);
        v = new MerkleTreeizedSyncEvent[len0];
        for(int i0 = 0; i0 < len0; i0++)
        {
            v[i0] = MerkleTreeizedSyncEvent.ice_read(istr);
        }
        return v;
    }

    public static void write(com.zeroc.Ice.OutputStream ostr, int tag, java.util.Optional<MerkleTreeizedSyncEvent[]> v)
    {
        if(v != null && v.isPresent())
        {
            write(ostr, tag, v.get());
        }
    }

    public static void write(com.zeroc.Ice.OutputStream ostr, int tag, MerkleTreeizedSyncEvent[] v)
    {
        if(ostr.writeOptional(tag, com.zeroc.Ice.OptionalFormat.FSize))
        {
            int pos = ostr.startSize();
            MerkleTreeizedSyncEventListHelper.write(ostr, v);
            ostr.endSize(pos);
        }
    }

    public static java.util.Optional<MerkleTreeizedSyncEvent[]> read(com.zeroc.Ice.InputStream istr, int tag)
    {
        if(istr.readOptional(tag, com.zeroc.Ice.OptionalFormat.FSize))
        {
            istr.skip(4);
            MerkleTreeizedSyncEvent[] v;
            v = MerkleTreeizedSyncEventListHelper.read(istr);
            return java.util.Optional.of(v);
        }
        else
        {
            return java.util.Optional.empty();
        }
    }
}
