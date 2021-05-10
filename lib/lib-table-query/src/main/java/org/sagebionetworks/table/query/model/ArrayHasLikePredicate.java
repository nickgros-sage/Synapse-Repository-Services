package org.sagebionetworks.table.query.model;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Custom "HAS_LIKE" predicate for searching patterns in multi-value columns.
 *
 * <HAS_LIKE predicate> ::= <row value constructor> [ NOT ] IN <in predicate value> [ESCAPE <escape character>]
 *
 * Examples:
 * columnName HAS_LIKE ("value1%", "value2", "value3") ESCAPE '_'
 *
 * See  https://sagebionetworks.jira.com/wiki/spaces/PLFM/pages/817168468/Multiple+Value+Annotations
 *
 * Related: {@link ArrayFunctionSpecification}
 *
 * NOTE the implemented {@link HasPredicate} interface is not for the "HAS" keyword, but, instead an interface for any predicate
 */
public class ArrayHasLikePredicate extends ArrayHasPredicate {

	private EscapeCharacter escapeCharacter;

	public ArrayHasLikePredicate(ColumnReference columnReferenceLHS, Boolean not, InPredicateValue inPredicateValue,
			EscapeCharacter escapeCharacter) {
		super(columnReferenceLHS, not, inPredicateValue);
		this.escapeCharacter = escapeCharacter;
	}
	
	public EscapeCharacter getEscapeCharacter() {
		return escapeCharacter;
	}
	
	@Override
	public void toSql(StringBuilder builder, ToSqlParameters parameters) {
		columnReferenceLHS.toSql(builder, parameters);
		builder.append(" ");
		if (this.not != null) {
			builder.append("NOT ");
		}
		builder.append("HAS_LIKE");
		builder.append(" ( ");
		inPredicateValue.toSql(builder, parameters);
		builder.append(" )");
		if (escapeCharacter != null) {
			builder.append(" ESCAPE ");
			escapeCharacter.toSql(builder, parameters);
		}
	}
	
	@Override
	<T extends Element> void addElements(List<T> elements, Class<T> type) {
		super.addElements(elements, type);
		checkElement(elements, type, escapeCharacter);
	}
	
	@Override
	public Iterable<UnsignedLiteral> getRightHandSideValues() {
		Iterable<UnsignedLiteral> valuesIterable = super.getRightHandSideValues();

		if (escapeCharacter != null) {
			// The stream of values in the function
			Stream<UnsignedLiteral> valuesStream = StreamSupport.stream(valuesIterable.spliterator(), /*parallel*/ false);
			
			valuesIterable = Stream.concat(valuesStream, Stream.of(escapeCharacter.getFirstElementOfType(UnsignedLiteral.class)))
					.collect(Collectors.toList());
		}
		
		return valuesIterable;

	}

}
