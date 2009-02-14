/**
 * JBroFuzz 1.2
 *
 * JBroFuzz - A stateless network protocol fuzzer for web applications.
 * 
 * Copyright (C) 2007, 2008, 2009 subere@uncon.org
 *
 * This file is part of JBroFuzz.
 * 
 * JBroFuzz is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * JBroFuzz is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with JBroFuzz.  If not, see <http://www.gnu.org/licenses/>.
 * Alternatively, write to the Free Software Foundation, Inc., 51 
 * Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * 
 * Verbatim copying and distribution of this entire program file is 
 * permitted in any medium without royalty provided this notice 
 * is preserved. 
 * 
 */
package org.owasp.jbrofuzz.core;

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;

public class Prototype {

	private ArrayList<String> categories;

	private String id;

	private String name;

	private ArrayList<String> payloads;

	private char type;

	public Prototype(char type, String id, String name) {

		this(type, id, name, new ArrayList<String>(), new ArrayList<String>());

	}

	public Prototype(char type, String id, String name,
			ArrayList<String> categories, ArrayList<String> payloads) {

		this.type = type;
		this.id = id;
		this.name = StringUtils.trim(name);
		this.categories = categories;
		this.payloads = payloads;

	}

	public void addCategory(String value) {

		categories.add(value);
		categories.trimToSize();

	}

	public void addPayload(String value) {

		payloads.add(calculatePayload(value));
		payloads.trimToSize();

	}

	private static String calculatePayload(String param) {
	
		String beginning;
		try {
			beginning = param.substring(0, 5);
		} catch (IndexOutOfBoundsException e1) {
			return param;
		}
		if (!beginning.startsWith("f(x)=")) {
			return param;
		}
	
		// Get rid of the first characters
		param = param.substring(5);
		// Chop at x, the variable of f(x)
		final String[] paramArray = param.toString().split(" x ");
	
		// Check to see if you have two elements
		if (paramArray.length != 2) {
			return param;
		}
		// Define the input string
		final String input = paramArray[0];
		// Define the number of times
		int times;
		try {
			times = Integer.parseInt(paramArray[1]);
		} catch (final NumberFormatException e) {
			times = 1;
		}
	
		// Check that times is positive
		if (times <= 0) {
			return param;
		}
	
		final int len = input.length() * times;
	
		final StringBuffer newBuffer = new StringBuffer(len);
		for (int i = 0; i < times; i++) {
			newBuffer.append(input);
		}
		return newBuffer.toString();
	}

	public ArrayList<String> getCategories() {
		return categories;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public ArrayList<String> getPayloads() {
		payloads.trimToSize();
		return payloads;
	}

	public char getType() {
		return type;
	}

	public boolean isAMemberOfCategory(String category) {

		String[] categoriesArray = new String[categories.size()];
		categories.toArray(categoriesArray);

		for (String s : categoriesArray) {
			if (s.equalsIgnoreCase(category)) {
				return true;
			}
		}

		return false;
	}

	public boolean isRecursive() {
		if (type == 'R')
			return true;
		return false;
	}

	public boolean isReplacive() {
		if (type == 'P')
			return true;
		return false;
	}

	public void setCategories(ArrayList<String> categories) {
		// categories.trimToSize();
		this.categories = categories;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPayloads(ArrayList<String> payloads) {
		this.payloads = payloads;
	}

	public void setType(char type) {
		this.type = type;
	}

	public int size() {
		return payloads.size();
	}

}