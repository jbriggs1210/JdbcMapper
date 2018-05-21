package com.moparisthebest.jdbc;

import java.sql.Connection;
import java.util.*;

import static com.moparisthebest.jdbc.util.InListUtil.toInList;

/**
 * Created by mopar on 4/29/15.
 */
public class BindInList implements InList {

	private static final int defaultMaxSize = Integer.parseInt(System.getProperty("QueryMapper.BindInList.defaultMaxSize", "999"));

	private static final InList instance = new BindInList();

	public static InList instance() {
		return instance;
	}

	private final int maxSize;

	public BindInList(final int maxSize) {
		this.maxSize = maxSize;
	}

	protected BindInList() {
		this(defaultMaxSize);
	}

	public <T> InListObject inList(final Connection conn, final String columnName, final Collection<T> values) {
		return values == null || values.isEmpty() ? InListObject.empty : new BindInListObject(
				toInList(columnName, values, this.maxSize),
				values.toArray()
		);
	}

	class BindInListObject extends InListObject {
		private final Object[] bindObjects;

		public BindInListObject(final String sql, final Object[] bindObjects) {
			super(sql);
			this.bindObjects = bindObjects;
		}

		public Object[] getBindObjects() {
			return bindObjects;
		}
	}
}
