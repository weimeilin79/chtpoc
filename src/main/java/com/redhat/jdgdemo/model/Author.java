package com.redhat.jdgdemo.model;

import org.hibernate.search.annotations.Field;

public class Author {
	@Field String name;
	@Field String surname;
	
	public Author(String name, String surname){
		this.name = name;
		this.surname = surname;
	}
	
	public String getName() {
		return name;
	}
	
	public String getSurname() {
		return surname;
	}
	
}