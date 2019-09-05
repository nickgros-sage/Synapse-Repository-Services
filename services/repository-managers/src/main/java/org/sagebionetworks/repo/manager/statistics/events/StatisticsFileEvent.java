package org.sagebionetworks.repo.manager.statistics.events;

import java.util.Objects;

import org.sagebionetworks.repo.model.file.FileHandleAssociateType;

/**
 * Implementation of a {@link StatisticsEvent} related to file events (E.g. download/upload)
 * 
 * @author Marco
 *
 */
public class StatisticsFileEvent implements StatisticsEvent {

	private StatisticsFileActionType actionType;
	private Long userId;
	private Long timestamp;
	private String fileHandleId;
	private String associationId;
	private FileHandleAssociateType associationType;

	public StatisticsFileEvent(StatisticsFileActionType actionType, Long userId, String fileHandleId, String associationId,
			FileHandleAssociateType associationType) {
		this.timestamp = System.currentTimeMillis();
		this.actionType = actionType;
		this.userId = userId;
		this.fileHandleId = fileHandleId;
		this.associationId = associationId;
		this.associationType = associationType;
	}

	@Override
	public Long getTimestamp() {
		return timestamp;
	}

	@Override
	public Long getUserId() {
		return userId;
	}

	public StatisticsFileActionType getActionType() {
		return actionType;
	}

	public String getFileHandleId() {
		return fileHandleId;
	}

	public String getAssociationId() {
		return associationId;
	}

	public FileHandleAssociateType getAssociationType() {
		return associationType;
	}

	@Override
	public int hashCode() {
		return Objects.hash(actionType, associationId, associationType, fileHandleId, timestamp, userId);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StatisticsFileEvent other = (StatisticsFileEvent) obj;
		return actionType == other.actionType && Objects.equals(associationId, other.associationId)
				&& associationType == other.associationType && Objects.equals(fileHandleId, other.fileHandleId)
				&& Objects.equals(timestamp, other.timestamp) && Objects.equals(userId, other.userId);
	}
	
	

}