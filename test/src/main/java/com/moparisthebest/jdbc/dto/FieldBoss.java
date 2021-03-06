package com.moparisthebest.jdbc.dto;

import java.util.Date;

/**
 * Created by mopar on 6/10/14.
 */
public class FieldBoss extends FieldPerson implements Boss {
	protected String department;
	protected String first_name;

	public FieldBoss() {
		super();
	}

	public FieldBoss(long personNo, Date birthDate, String firstName, String lastName, String department, String first_name) {
		super(personNo, birthDate, firstName, lastName);
		this.department = department;
		this.first_name = first_name;
	}

	public FieldBoss(Boss boss) {
		super(boss);
		this.department = boss.getDepartment();
		this.first_name = boss.getFirst_name();
	}

	public String getDepartment() {
		return department;
	}

	public String getFirst_name() {
		return first_name;
	}

	public void setDummy(String dummy) {
		// do nothing, this is simply to avoid calling the constructor in some cases
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof FieldBoss)) return false;
		if (!super.equals(o)) return false;

		FieldBoss boss = (FieldBoss) o;

		if (department != null ? !department.equals(boss.department) : boss.department != null) return false;
		if (first_name != null ? !first_name.equals(boss.first_name) : boss.first_name != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + (department != null ? department.hashCode() : 0);
		result = 31 * result + (first_name != null ? first_name.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName()+"{" +
				"department='" + department + '\'' +
				", first_name='" + first_name + '\'' +
				"} " + super.toString();
	}
}
