package org.sagebionetworks.table.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.sagebionetworks.table.query.model.QuerySpecification;
import org.sagebionetworks.table.query.model.SelectList;
import org.sagebionetworks.table.query.model.SetQuantifier;
import org.sagebionetworks.table.query.model.TableExpression;
import org.sagebionetworks.table.query.util.SqlElementUntils;

public class QuerySpecificationTest {
	
	@Test
	public void testQuerySpecificationToSQL() throws ParseException{
		SetQuantifier setQuantifier = null;
		SelectList selectList = SqlElementUntils.createSelectList("one, two");
		TableExpression tableExpression = SqlElementUntils.createTableExpression("from syn123");
		QuerySpecification element = new QuerySpecification(setQuantifier, selectList, tableExpression);
		assertEquals("SELECT one, two FROM syn123", element.toString());
	}

	@Test
	public void testQuerySpecificationToSQLWithSetQuantifier() throws ParseException{
		SetQuantifier setQuantifier = SetQuantifier.DISTINCT;
		SelectList selectList = SqlElementUntils.createSelectList("one, two");
		TableExpression tableExpression = SqlElementUntils.createTableExpression("from syn123");
		QuerySpecification element = new QuerySpecification(setQuantifier, selectList, tableExpression);
		assertEquals("SELECT DISTINCT one, two FROM syn123", element.toString());
	}
	
	@Test
	public void testIsAggregateNoDistinct() throws ParseException{
		QuerySpecification element = new TableQueryParser("select * from syn123").querySpecification();
		assertFalse(element.isElementAggregate());
	}
	
	@Test
	public void testIsAggregateDistinct() throws ParseException{
		QuerySpecification element = new TableQueryParser("select distinct three, four from syn123").querySpecification();
		assertTrue(element.isElementAggregate());
	}
	
	@Test
	public void testGetTableName() throws ParseException{
		QuerySpecification element = new TableQueryParser("select distinct three, four from syn123").querySpecification();
		assertEquals("syn123", element.getTableName());
	}
	
}
