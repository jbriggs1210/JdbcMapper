package com.moparisthebest.jdbc.codegen;

import com.moparisthebest.jdbc.Cleaner;
import com.moparisthebest.jdbc.dto.FieldPerson;
import com.moparisthebest.jdbc.dto.Person;
import com.moparisthebest.jdbc.util.ResultSetIterable;

import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.*;

/**
 * Created by mopar on 5/24/17.
 */
@JdbcMapper.Mapper(
//		jndiName = "bob",
//        cachePreparedStatements = false,
		sqlParser = PrestoSQLParser.class
)
public interface PrestoPersonDAO extends PersonDAO {


	@JdbcMapper.SQL("UPDATE person SET first_name = {firstName} WHERE last_name = {lastName}")
	int setFirstName(String firstName, String lastName);

	@JdbcMapper.SQL("UPDATE person SET first_name = {firstName} WHERE person_no = {personNo}")
	void setFirstName(String firstName, long personNo) throws SQLException;

	@JdbcMapper.SQL("UPDATE person SET first_name = {firstName} WHERE person_no = {personNo}")
	void setFirstNameBlob(byte[] firstName, long personNo) throws SQLException;

	@JdbcMapper.SQL("UPDATE person SET first_name = {firstName} WHERE person_no = {personNo}")
	void setFirstNameBlob(@JdbcMapper.Blob String firstName, long personNo) throws SQLException;

	@JdbcMapper.SQL("SELECT first_name FROM person WHERE person_no = {personNo}")
	String getFirstName(long personNo) throws SQLException;

	@JdbcMapper.SQL("SELECT first_name, last_name, birth_date FROM person WHERE person_no = {personNo}")
	FieldPerson getPerson(long personNo, Calendar cal) throws SQLException;

	@JdbcMapper.SQL("SELECT first_name, last_name, birth_date FROM person WHERE person_no = {personNo}")
	FieldPerson getPerson(long personNo) throws SQLException;

	@JdbcMapper.SQL("SELECT first_name, last_name FROM person WHERE last_name = {lastName}")
	List<FieldPerson> getPeople(String lastName) throws SQLException;

	@JdbcMapper.SQL("SELECT first_name, last_name FROM person WHERE {last_name not in lastName}")
	List<FieldPerson> getPeople(String[] lastName) throws SQLException;

	@JdbcMapper.SQL("SELECT first_name, last_name FROM person WHERE {last_name in lastName}")
	List<FieldPerson> getPeople(List<String> lastName) throws SQLException;

	@JdbcMapper.SQL("SELECT first_name, last_name FROM person WHERE last_name = {lastName}")
	FieldPerson[] getPeopleArray(String lastName) throws SQLException;

	@JdbcMapper.SQL("SELECT first_name, last_name FROM person WHERE last_name = {lastName}")
	Iterator<FieldPerson> getPeopleIterator(String lastName) throws SQLException;

	@JdbcMapper.SQL("SELECT first_name, last_name FROM person WHERE last_name = {lastName}")
	ListIterator<FieldPerson> getPeopleListIterator(String lastName) throws SQLException;

	@JdbcMapper.SQL("SELECT first_name, last_name, person_no FROM person WHERE last_name = {lastName}")
	Map<String, FieldPerson> getPersonMap(String lastName) throws SQLException;

	@JdbcMapper.SQL("SELECT first_name, last_name, person_no FROM person WHERE last_name = {lastName}")
	Map<String, List<FieldPerson>> getPersonMapList(String lastName) throws SQLException;

	@JdbcMapper.SQL("SELECT first_name FROM person WHERE person_no = {personNo} and last_name = {lastName}")
	String getFirstName(long personNo, String lastName) throws SQLException;

	// various date checks here
	@JdbcMapper.SQL("SELECT birth_date FROM person WHERE person_no = {personNo}")
	Calendar getBirthDateCalendar(long personNo) throws SQLException;
	@JdbcMapper.SQL("SELECT birth_date FROM person WHERE person_no = {personNo}")
	Calendar getBirthDateCalendar(long personNo, Calendar mycal) throws SQLException;

	@JdbcMapper.SQL("SELECT birth_date FROM person WHERE person_no = {personNo}")
	Timestamp getBirthDateTimestamp(long personNo) throws SQLException;
	@JdbcMapper.SQL("SELECT birth_date FROM person WHERE person_no = {personNo}")
	Timestamp getBirthDateTimestamp(long personNo, Calendar mycal) throws SQLException;

	@JdbcMapper.SQL("SELECT birth_date FROM person WHERE person_no = {personNo}")
	Time getBirthDateTime(long personNo) throws SQLException;
	@JdbcMapper.SQL("SELECT birth_date FROM person WHERE person_no = {personNo}")
	Time getBirthDateTime(long personNo, Calendar mycal) throws SQLException;

	@JdbcMapper.SQL("SELECT birth_date FROM person WHERE person_no = {personNo}")
	java.sql.Date getBirthDateSqlDate(long personNo) throws SQLException;
	@JdbcMapper.SQL("SELECT birth_date FROM person WHERE person_no = {personNo}")
	java.sql.Date getBirthDateSqlDate(long personNo, Calendar mycal) throws SQLException;

	@JdbcMapper.SQL("SELECT birth_date FROM person WHERE person_no = {personNo}")
	java.util.Date getBirthDateUtilDate(long personNo) throws SQLException;
	@JdbcMapper.SQL("SELECT birth_date FROM person WHERE person_no = {personNo}")
	java.util.Date getBirthDateUtilDate(long personNo, Calendar mycal) throws SQLException;

	// cleaner checks
	@JdbcMapper.SQL("SELECT first_name, last_name, birth_date FROM person WHERE person_no = {personNo}")
	FieldPerson getPersonCleanFieldPerson(long personNo, Cleaner<FieldPerson> clean) throws SQLException;
	@JdbcMapper.SQL("SELECT first_name, last_name, birth_date FROM person WHERE person_no = {personNo}")
	FieldPerson getPersonCleanPerson(long personNo, Cleaner<Person> clean) throws SQLException;
	@JdbcMapper.SQL("SELECT first_name, last_name, birth_date FROM person WHERE person_no = {personNo}")
	FieldPerson getPersonCleanObject(long personNo, Cleaner<Object> clean) throws SQLException;
	@JdbcMapper.SQL("SELECT first_name, last_name, birth_date FROM person WHERE person_no = {personNo}")
	List<FieldPerson> getPersonCleanPersonList(long personNo, Cleaner<Person> clean) throws SQLException;
	@JdbcMapper.SQL("SELECT first_name, last_name, birth_date FROM person WHERE person_no = {personNo}")
	Map<String, FieldPerson> getPersonCleanPersonMap(long personNo, Cleaner<Person> clean) throws SQLException;
	@JdbcMapper.SQL("SELECT first_name, last_name, birth_date FROM person WHERE person_no = {personNo}")
	Map<String, List<FieldPerson>> getPersonCleanPersonMapList(long personNo, Cleaner<Person> clean) throws SQLException;
	/*
	// this should NOT compile:
	@JdbcMapper.SQL("SELECT first_name, last_name, birth_date FROM person WHERE person_no = {personNo}")
	FieldPerson getPersonCleanNumber(long personNo, Cleaner<Number> clean) throws SQLException;
	*/

	// max row checks
	@JdbcMapper.SQL("SELECT first_name, last_name, birth_date FROM person WHERE person_no = {personNo}")
	Map<String, FieldPerson> getPersonDynamicLimit(long personNo, byte maxRows) throws SQLException;
	@JdbcMapper.SQL("SELECT first_name, last_name, birth_date FROM person WHERE person_no = {personNo}")
	Map<String, FieldPerson> getPersonDynamicLimit(long personNo, short arrayMaxLength) throws SQLException;
	@JdbcMapper.SQL("SELECT first_name, last_name, birth_date FROM person WHERE person_no = {personNo}")
	List<FieldPerson> getPersonDynamicLimit(long personNo, int maxRows) throws SQLException;
	@JdbcMapper.SQL("SELECT first_name, last_name, birth_date FROM person WHERE person_no = {personNo}")
	Map<String, List<FieldPerson>> getPersonDynamicLimit(long personNo, long rowLimit) throws SQLException;


	@JdbcMapper.SQL(value = "SELECT first_name, last_name, birth_date FROM person WHERE person_no = {personNo}", maxRows = Byte.MAX_VALUE)
	List<FieldPerson> getPersonStaticLimitListByte(long personNo) throws SQLException;
	@JdbcMapper.SQL(value = "SELECT first_name, last_name, birth_date FROM person WHERE person_no = {personNo}", maxRows = Short.MAX_VALUE)
	List<FieldPerson> getPersonStaticLimitList(long personNo) throws SQLException;
	@JdbcMapper.SQL(value = "SELECT first_name, last_name, birth_date FROM person WHERE person_no = {personNo}", maxRows = Integer.MAX_VALUE)
	Map<String, FieldPerson> getPersonStaticLimitMap(long personNo) throws SQLException;
	@JdbcMapper.SQL(value = "SELECT first_name, last_name, birth_date FROM person WHERE person_no = {personNo}", maxRows = Long.MAX_VALUE)
	Map<String, List<FieldPerson>> getPersonStaticLimitMapList(long personNo) throws SQLException;


	@JdbcMapper.SQL("SELECT person_no, birth_date, last_name, first_name from person WHERE person_no IN ({personNo1},{personNo2},{personNo3}) ORDER BY person_no")
	List<FieldPerson> getPeopleList(long personNo1, long personNo2, long personNo3) throws SQLException;

	@JdbcMapper.SQL("SELECT person_no, birth_date, last_name, first_name from person WHERE person_no IN ({personNo1},{personNo2},{personNo3}) ORDER BY person_no")
	ResultSetIterable<FieldPerson> getPeopleResultSetIterable(long personNo1, long personNo2, long personNo3) throws SQLException;

	@JdbcMapper.SQL(value = "SELECT person_no, birth_date, last_name, first_name from person WHERE person_no IN ({personNo1},{personNo2},{personNo3}) ORDER BY person_no", cachePreparedStatement = JdbcMapper.OptionalBool.TRUE)
	ResultSetIterable<FieldPerson> getPeopleResultSetIterableCachedPreparedStatement(long personNo1, long personNo2, long personNo3) throws SQLException;

}
