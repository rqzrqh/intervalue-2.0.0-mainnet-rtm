package one.inve.localfullnode2.store.rocks;


import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;

public class MsgArray implements Serializable {
    /**
     * 交易信息
     */
	private List<Message> list;
	/**
	 * 交易表索引，代表第几个表 开始查询用0表示
	 */
	private BigInteger tableIndex = BigInteger.ZERO;
	/**
	 * 交易表跳过多少条记录
	 */
	private Long offset;
	/**
	 * 系统表索引，代表第几个表 开始查询用0表示
	 */
	private BigInteger sysTableIndex = BigInteger.ZERO;
	/**
	 * 跳过多少条记录
	 */
	private Long sysOffset;

	public MsgArray() {
	}

	private MsgArray(Builder builder) {
		setList(builder.list);
		setTableIndex(builder.tableIndex);
		setOffset(builder.offset);
		setSysTableIndex(builder.sysTableIndex);
		setSysOffset(builder.sysOffset);
	}

	public List<Message> getList() {
		return list;
	}

	public void setList(List<Message> list) {
		this.list = list;
	}

	public BigInteger getTableIndex() {
		return tableIndex;
	}

	public void setTableIndex(BigInteger tableIndex) {
		this.tableIndex = tableIndex;
	}

	public Long getOffset() {
		return offset;
	}

	public void setOffset(Long offset) {
		this.offset = offset;
	}

	public BigInteger getSysTableIndex() {
		return sysTableIndex;
	}

	public void setSysTableIndex(BigInteger sysTableIndex) {
		this.sysTableIndex = sysTableIndex;
	}

	public Long getSysOffset() {
		return sysOffset;
	}

	public void setSysOffset(Long sysOffset) {
		this.sysOffset = sysOffset;
	}

	public static final class Builder {
		private List<Message> list;
		private BigInteger tableIndex;
		private Long offset;
		private BigInteger sysTableIndex;
		private Long sysOffset;

		public Builder() {
		}

		public Builder list(List<Message> val) {
			list = val;
			return this;
		}

		public Builder tableIndex(BigInteger val) {
			tableIndex = val;
			return this;
		}

		public Builder offset(Long val) {
			offset = val;
			return this;
		}

		public Builder sysTableIndex(BigInteger val) {
			sysTableIndex = val;
			return this;
		}

		public Builder sysOffset(Long val) {
			sysOffset = val;
			return this;
		}

		public MsgArray build() {
			return new MsgArray(this);
		}
	}
}
