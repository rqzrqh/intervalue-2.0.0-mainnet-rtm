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

package one.inve.localfullnode2.gossip.vo;

import one.inve.localfullnode2.vo.Event;
import one.inve.localfullnode2.vo.EventListHelper;

public class GossipObj implements java.lang.Cloneable, java.io.Serializable {
	public String snapVersion;

	public Event[] events;

	public byte[] snapHash;

	public GossipObj() {
		this.snapVersion = "";
	}

	public GossipObj(String snapVersion, Event[] events, byte[] snapHash) {
		this.snapVersion = snapVersion;
		this.events = events;
		this.snapHash = snapHash;
	}

	public boolean equals(java.lang.Object rhs) {
		if (this == rhs) {
			return true;
		}
		GossipObj r = null;
		if (rhs instanceof GossipObj) {
			r = (GossipObj) rhs;
		}

		if (r != null) {
			if (this.snapVersion != r.snapVersion) {
				if (this.snapVersion == null || r.snapVersion == null || !this.snapVersion.equals(r.snapVersion)) {
					return false;
				}
			}
			if (!java.util.Arrays.equals(this.events, r.events)) {
				return false;
			}
			if (!java.util.Arrays.equals(this.snapHash, r.snapHash)) {
				return false;
			}

			return true;
		}

		return false;
	}

	public int hashCode() {
		int h_ = 5381;
		h_ = com.zeroc.IceInternal.HashUtil.hashAdd(h_, "::one::inve::rpc::localfullnode::GossipObj");
		h_ = com.zeroc.IceInternal.HashUtil.hashAdd(h_, snapVersion);
		h_ = com.zeroc.IceInternal.HashUtil.hashAdd(h_, events);
		h_ = com.zeroc.IceInternal.HashUtil.hashAdd(h_, snapHash);
		return h_;
	}

	public GossipObj clone() {
		GossipObj c = null;
		try {
			c = (GossipObj) super.clone();
		} catch (CloneNotSupportedException ex) {
			assert false; // impossible
		}
		return c;
	}

	public void ice_writeMembers(com.zeroc.Ice.OutputStream ostr) {
		ostr.writeString(this.snapVersion);
		EventListHelper.write(ostr, this.events);
		ostr.writeByteSeq(this.snapHash);
	}

	public void ice_readMembers(com.zeroc.Ice.InputStream istr) {
		this.snapVersion = istr.readString();
		this.events = EventListHelper.read(istr);
		this.snapHash = istr.readByteSeq();
	}

	static public void ice_write(com.zeroc.Ice.OutputStream ostr, GossipObj v) {
		if (v == null) {
			_nullMarshalValue.ice_writeMembers(ostr);
		} else {
			v.ice_writeMembers(ostr);
		}
	}

	static public GossipObj ice_read(com.zeroc.Ice.InputStream istr) {
		GossipObj v = new GossipObj();
		v.ice_readMembers(istr);
		return v;
	}

	static public void ice_write(com.zeroc.Ice.OutputStream ostr, int tag, java.util.Optional<GossipObj> v) {
		if (v != null && v.isPresent()) {
			ice_write(ostr, tag, v.get());
		}
	}

	static public void ice_write(com.zeroc.Ice.OutputStream ostr, int tag, GossipObj v) {
		if (ostr.writeOptional(tag, com.zeroc.Ice.OptionalFormat.FSize)) {
			int pos = ostr.startSize();
			ice_write(ostr, v);
			ostr.endSize(pos);
		}
	}

	static public java.util.Optional<GossipObj> ice_read(com.zeroc.Ice.InputStream istr, int tag) {
		if (istr.readOptional(tag, com.zeroc.Ice.OptionalFormat.FSize)) {
			istr.skip(4);
			return java.util.Optional.of(GossipObj.ice_read(istr));
		} else {
			return java.util.Optional.empty();
		}
	}

	private static final GossipObj _nullMarshalValue = new GossipObj();

	public static final long serialVersionUID = -1634304182004862974L;
}
