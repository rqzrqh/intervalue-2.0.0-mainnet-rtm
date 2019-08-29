package one.inve.localfullnode2.sync.measure;

import com.alibaba.fastjson.JSON;

import one.inve.localfullnode2.utilities.GenericArray;

/**
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @ClassName: Distribution
 * @Description: Distribution constitutes 1 or a couple of {@code Column},the
 *               former is message distribution
 * @author Francis.Deng
 * @mailbox francis_xiiiv@163.com
 * @date Aug 23, 2019
 *
 */
public class Distribution {

	public static class Column {
		private GenericArray<Range> ranges = new GenericArray<>();

		public GenericArray<Range> getRanges() {
			return ranges;
		}

		public void add(Range r) {
			for (Range range : ranges) {
				boolean b = range.attemptToMerge(r);
				if (b)
					return;
			}

			ranges.append(r);
		}

		public void add(Column column) {
			for (Range range : column.getRanges()) {
				add(range);
			}
		}

		public Range nextRange(int step) {
			Range hasMaxStop = null;
			for (Range range : ranges) {
				if (hasMaxStop == null) {
					hasMaxStop = range;
					continue;
				}

				if (range.getStop() > hasMaxStop.getStop()) {
					hasMaxStop = range;
				}
			}

			return new Range(hasMaxStop.getStop(), hasMaxStop.getStop() + step);
		}
	}

	private Column[] columns;

	public Column[] getColumns() {
		return columns;
	}

	public Distribution(int columnNum) {
		columns = new Column[columnNum];

	}

	public Distribution(Column[] columns) {
		this.columns = columns;
	}

//	public GenericArray<Range> nextRangesInColumns(int step) {
//		GenericArray<Range> ranges = new GenericArray<>();
//
//		for (Column column : columns) {
//			ranges.append(column.nextRange(step));
//		}
//
//		return ranges;
//	}

	public Distribution next(int step) {
		Distribution newDistribution = new Distribution(columns.length);
		Column[] cols = new Column[columns.length];

		for (int index = 0; index < columns.length; index++) {
			Range r = columns[index].nextRange(step);
			Column col = new Column();
			col.add(r);

			cols[index] = col;
		}

		return new Distribution(cols);
	}

	public Range nextRangeInColumn(int columnth, int step) {
		Column column = columns[columnth];
		return column.nextRange(step);
	}

	public void addLabeledRange(int label, Range r) {
		columns[label].add(r);
	}

	// merge another distribution
	public void addDistribution(Distribution dist) {
		for (int index = 0; index < dist.getColumns().length; index++) {
			columns[index].add(dist.getColumns()[index]);
		}
	}

	public String toString() {
		return JSON.toJSONString(this);
	}

	public static Distribution fromString(String text) {
		return JSON.parseObject(text, Distribution.class);
	}

}
