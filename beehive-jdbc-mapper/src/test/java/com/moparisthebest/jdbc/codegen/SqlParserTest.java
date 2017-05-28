package com.moparisthebest.jdbc.codegen;

import com.moparisthebest.classgen.SQLParser;
import com.moparisthebest.classgen.SimpleSQLParser;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by mopar on 5/30/17.
 */
public class SqlParserTest {

	private final SQLParser factory = new SimpleSQLParser();

	@Test
	public void testSingleSelect() {
		final SQLParser ret = factory.parse("select bob from tom");
		assertTrue(ret.isSelect());
		assertArrayEquals(new String[]{null, "BOB"}, ret.columnNames());
	}

	@Test
	public void testMultiSelect() {
		final String[] expected = new String[]{null, "BOB", "TOM"};
		for (final String sql : new String[]{
				"select bob, tom from tom"
				, "select some_bob bob, some_tom as tom from tom"
				, "select tom.bob, some_tom as tom from tom"
		}) {
			final SQLParser ret = factory.parse(sql);
			assertTrue(ret.isSelect());
			assertArrayEquals(expected, ret.columnNames());
		}
	}

	@Test
	public void testNotSelect() {
		for (final String sql : new String[]{
				"UPDATE bob SET bob = 'bob' WHERE bob_no = 1"
				, "INSERT INTO bob (bob_no, bob) VALUES (1, 'bob')"
				, "MERGE INTO bob bla bla bla"
		}) {
			final SQLParser ret = factory.parse(sql);
			assertFalse(ret.isSelect());
		}
	}

}
