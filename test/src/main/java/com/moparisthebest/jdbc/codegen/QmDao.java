package com.moparisthebest.jdbc.codegen;

import com.moparisthebest.jdbc.dto.*;
import com.moparisthebest.jdbc.util.CaseInsensitiveHashMap;
import com.moparisthebest.jdbc.util.ResultSetIterable;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@JdbcMapper.Mapper(
		cachePreparedStatements = JdbcMapper.OptionalBool.FALSE
		, allowReflection = JdbcMapper.OptionalBool.TRUE
)
public interface QmDao extends JdbcMapper {

	public static final String personRegular = "SELECT person_no, first_name, last_name, birth_date FROM person WHERE person_no = {personNo}";
	public static final String bossRegularAndUnderscore = "SELECT p.person_no, p.first_name AS firstName, p.last_name, p.birth_date, b.department, p.first_name " +
			"FROM person p " +
			"JOIN boss b ON p.person_no = b.person_no " +
			"WHERE p.person_no = {personNo}";
	public static final String bossRegularAndUnderscoreReverse = "SELECT p.person_no, p.first_name, p.last_name, p.birth_date, b.department, p.first_name AS firstName " +
			"FROM person p " +
			"JOIN boss b ON p.person_no = b.person_no " +
			"WHERE p.person_no = {personNo}";
	public static final String bossRegular = "SELECT p.person_no, p.first_name AS firstName, p.last_name, p.birth_date, b.department " +
			"FROM person p " +
			"JOIN boss b ON p.person_no = b.person_no " +
			"WHERE p.person_no = {personNo}";
	public static final String bossUnderscore = "SELECT p.person_no, p.first_name, p.last_name, p.birth_date, b.department " +
			"FROM person p " +
			"JOIN boss b ON p.person_no = b.person_no " +
			"WHERE p.person_no = {personNo}";
	public static final String allNames = "SELECT first_name, last_name FROM person WHERE person_no < 4";
	String selectPersonNo = "SELECT person_no FROM person WHERE person_no = {personNo}";
	String selectMapLongPerson = "SELECT p.person_no, p.first_name AS firstName, p.last_name, p.birth_date, b.department " +
			"FROM person p " +
			"JOIN boss b ON p.person_no = b.person_no " +
			"WHERE p.person_no in (2,3,4)";
	String selectLongLong = "SELECT person_no AS first_no, person_no AS last_no FROM person WHERE person_no < 4";
	String selectLongArray = "SELECT 1, 2, 3 FROM person WHERE person_no = 1";
	String bobTomMap = "SELECT 'bob' as bob, 'tom' as tom FROM person WHERE person_no = 1";
	String selectThreePeople = "SELECT person_no, first_name, last_name, birth_date from person WHERE person_no IN ({personNo1}, {personNo2}, {personNo3}) ORDER BY person_no";

	@JdbcMapper.SQL(personRegular)
	FieldPerson getFieldRegularPerson(long personNo) throws SQLException;

	@JdbcMapper.SQL(personRegular)
	BuilderPerson getBuilderPerson(long personNo) throws SQLException;

	@JdbcMapper.SQL(bossRegularAndUnderscore)
	FieldBoss getFieldRegularAndUnderscore(long personNo) throws SQLException;

	@JdbcMapper.SQL(bossRegularAndUnderscoreReverse)
	FieldBoss getFieldRegularAndUnderscoreReverse(long personNo) throws SQLException;

	@JdbcMapper.SQL(bossRegular)
	FieldBoss getFieldRegular(long personNo) throws SQLException;

	@JdbcMapper.SQL(bossUnderscore)
	FieldBoss getFieldUnderscore(long personNo) throws SQLException;

	@JdbcMapper.SQL(personRegular)
	SetPerson getSetRegularPerson(long personNo) throws SQLException;

	@JdbcMapper.SQL(bossRegularAndUnderscore)
	SetBoss getSetRegularAndUnderscore(long personNo) throws SQLException;

	@JdbcMapper.SQL(bossRegularAndUnderscoreReverse)
	SetBoss getSetRegularAndUnderscoreReverse(long personNo) throws SQLException;

	@JdbcMapper.SQL(bossRegular)
	SetBoss getSetRegular(long personNo) throws SQLException;

	@JdbcMapper.SQL(bossUnderscore)
	SetBoss getSetUnderscore(long personNo) throws SQLException;

	@JdbcMapper.SQL(personRegular)
	ReverseFieldPerson getReverseFieldRegularPerson(long personNo) throws SQLException;

	@JdbcMapper.SQL(bossRegularAndUnderscore)
	ReverseFieldBoss getReverseFieldRegularAndUnderscore(long personNo) throws SQLException;

	@JdbcMapper.SQL(bossRegularAndUnderscoreReverse)
	ReverseFieldBoss getReverseFieldRegularAndUnderscoreReverse(long personNo) throws SQLException;

	@JdbcMapper.SQL(bossRegular)
	ReverseFieldBoss getReverseFieldRegular(long personNo) throws SQLException;

	@JdbcMapper.SQL(bossUnderscore)
	ReverseFieldBoss getReverseFieldUnderscore(long personNo) throws SQLException;

	@JdbcMapper.SQL(personRegular)
	ReverseSetPerson getReverseSetRegularPerson(long personNo) throws SQLException;

	@JdbcMapper.SQL(bossRegularAndUnderscore)
	ReverseSetBoss getReverseSetRegularAndUnderscore(long personNo) throws SQLException;

	@JdbcMapper.SQL(bossRegularAndUnderscoreReverse)
	ReverseSetBoss getReverseSetRegularAndUnderscoreReverse(long personNo) throws SQLException;

	@JdbcMapper.SQL(bossRegular)
	ReverseSetBoss getReverseSetRegular(long personNo) throws SQLException;

	@JdbcMapper.SQL(bossUnderscore)
	ReverseSetBoss getReverseSetUnderscore(long personNo) throws SQLException;

	@SQL(selectPersonNo)
	Long getPersonNo(long personNo) throws SQLException;

	@SQL(selectPersonNo)
	long getPersonNoPrimitive(long personNo) throws SQLException;

	@SQL(selectPersonNo)
	int getPersonNoPrimitiveInt(int personNo) throws SQLException;

	@SQL(selectPersonNo)
	Long[] getPersonNoObjectArray(Long personNo) throws SQLException;

	@SQL(allNames)
	List<Map<String, String>> getAllNames() throws SQLException;

	/*
	@SQL(allNames)
	Map[] getAllNamesArray() throws SQLException; // todo: try Map<String, String>[] fix 'generic array creation' error
	*/

	@SQL(allNames)
	Map<String, String> getAllNameMap() throws SQLException;

	@SQL(selectMapLongPerson)
	Map<Long, FieldBoss> getMapLongPerson() throws SQLException;

	@SQL(selectLongLong)
	Map<Long, Long> getMapLongLong() throws SQLException;

	/*
	@SQL(selectLongArray)
	Long[] getLongObjectArray() throws SQLException;

	@SQL(selectLongArray)
	long[] getLongPrimitiveArray() throws SQLException;
	*/

	@SQL(bobTomMap)
	List<Map<String, String>> getBobTomMap() throws SQLException;

	@SQL(bobTomMap)
	List<CaseInsensitiveHashMap<String, String>> getBobTomMapCaseInsensitive() throws SQLException;

	@SQL(selectThreePeople)
	List<FieldPerson> getThreePeople(long personNo1, long personNo2, long personNo3) throws SQLException;

	@SQL(selectThreePeople)
	List<FieldPerson> getThreePeopleType(long personNo1, long personNo2, long personNo3) throws SQLException;

	@SQL(selectThreePeople)
	ResultSetIterable<FieldPerson> getThreePeopleResultSetIterable(long personNo1, long personNo2, long personNo3) throws SQLException;
}
