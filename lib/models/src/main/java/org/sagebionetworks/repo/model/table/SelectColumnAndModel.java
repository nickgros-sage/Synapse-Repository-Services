package org.sagebionetworks.repo.model.table;

import org.sagebionetworks.util.ValidateArgument;

/**
 * Represents all of the information about a column in a select clause of a query.
 *
 */
public final class SelectColumnAndModel {
	
	String name;
	ColumnType columnType;
	SelectColumn selectColumn;
	ColumnModel columnModel;
	
	/**
	 * Create using only a ColumnModel.
	 * 
	 * @param columnModel
	 */
	public SelectColumnAndModel(ColumnModel columnModel){
		ValidateArgument.required(columnModel, "columnModel");
		this.name = columnModel.getName();
		this.columnType = columnModel.getColumnType();
		this.columnModel = columnModel;
		this.selectColumn = new SelectColumn();
		selectColumn.setName(columnModel.getName());
		selectColumn.setColumnType(columnModel.getColumnType());
		selectColumn.setId(columnModel.getId());
	}
	
	/**
	 * Create using both SelectColumn and ColumnModel.  The selectColumn is required
	 * and the columnModel is optional.
	 * 
	 * @param selectColumn
	 * @param columnModel
	 */
	public SelectColumnAndModel(SelectColumn selectColumn, ColumnModel columnModel){
		ValidateArgument.required(selectColumn, "selectColumn");
		this.name = selectColumn.getName();
		this.columnType = selectColumn.getColumnType();
		this.selectColumn = selectColumn;
		this.columnModel = columnModel;
	}
	
	public String getName() {
		return name;
	}
	public ColumnType getColumnType() {
		return columnType;
	}
	public SelectColumn getSelectColumn() {
		return selectColumn;
	}
	public ColumnModel getColumnModel() {
		return columnModel;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((columnModel == null) ? 0 : columnModel.hashCode());
		result = prime * result
				+ ((columnType == null) ? 0 : columnType.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result
				+ ((selectColumn == null) ? 0 : selectColumn.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SelectColumnAndModel other = (SelectColumnAndModel) obj;
		if (columnModel == null) {
			if (other.columnModel != null)
				return false;
		} else if (!columnModel.equals(other.columnModel))
			return false;
		if (columnType != other.columnType)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (selectColumn == null) {
			if (other.selectColumn != null)
				return false;
		} else if (!selectColumn.equals(other.selectColumn))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "SelectColumnAndModel [name=" + name + ", columnType="
				+ columnType + ", selectColumn=" + selectColumn
				+ ", columnModel=" + columnModel + "]";
	}
	
}
