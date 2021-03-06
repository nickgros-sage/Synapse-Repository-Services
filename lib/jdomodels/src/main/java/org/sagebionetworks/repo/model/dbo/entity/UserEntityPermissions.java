package org.sagebionetworks.repo.model.dbo.entity;

import java.util.Objects;

import org.sagebionetworks.repo.model.DataType;
import org.sagebionetworks.repo.model.EntityType;

/**
 * A user's permission on a single entity.
 * 
 */
public class UserEntityPermissions {

	private Long entityId;
	private Long benefactorId;
	private EntityType entityType;
	private DataType dataType;
	private boolean doesEntityExist;
	private boolean hasRead;
	private boolean hasDownload;
	private boolean hasCreate;
	private boolean hasDelete;
	private boolean hasChangePermissions;
	private boolean hasChangeSettings;
	private boolean hasModerate;

	public UserEntityPermissions(Long entityId) {
		super();
		this.entityId = entityId;
		this.benefactorId = null;
		this.entityType = null;
		this.dataType = DataType.SENSITIVE_DATA;
		this.doesEntityExist = false;
		this.hasRead = false;
		this.hasDownload = false;
		this.hasCreate = false;
		this.hasDelete = false;
		this.hasChangePermissions = false;
		this.hasChangeSettings = false;
		this.hasModerate = false;
	}

	/**
	 * The ID of the entity.
	 * 
	 * @return the entityId
	 */
	public Long getEntityId() {
		return entityId;
	}

	/**
	 * @param entityId the entityId to set
	 */
	public UserEntityPermissions withEntityId(Long entityId) {
		this.entityId = entityId;
		return this;
	}

	/**
	 * The ID of the entity that has an ACL that controls access to this entity.
	 * 
	 * @return the benefactorId
	 */
	public Long getBenefactorId() {
		return benefactorId;
	}

	/**
	 * The ID of the entity that has an ACL that controls access to this entity.
	 * 
	 * @param benefactorId the benefactorId to set
	 */
	public UserEntityPermissions withBenefactorId(Long benefactorId) {
		this.benefactorId = benefactorId;
		return this;
	}

	/**
	 * The type of the entity.
	 * 
	 * @return the entityType
	 */
	public EntityType getEntityType() {
		return entityType;
	}

	/**
	 * @param entityType the entityType to set
	 */
	public UserEntityPermissions withEntityType(EntityType entityType) {
		this.entityType = entityType;
		return this;
	}

	/**
	 * The data type determines if there are additional restrictions on this entity.
	 */
	public DataType getDataType() {
		return dataType;
	}

	/**
	 * The data type determines if there are additional restrictions on this entity.
	 */
	public UserEntityPermissions withDataType(DataType dataType) {
		this.dataType = dataType;
		return this;
	}

	/**
	 * Does this entity exist?
	 */
	public boolean doesEntityExist() {
		return doesEntityExist;
	}

	/**
	 * Does this entity exist?
	 */
	public UserEntityPermissions withtDoesEntityExist(boolean doesEntityExist) {
		this.doesEntityExist = doesEntityExist;
		return this;
	}

	/**
	 * Does the user have the read permission on this entity?
	 * 
	 * @return the hasRead
	 */
	public boolean hasRead() {
		return hasRead;
	}

	/**
	 * Does the user have the read permission on this entity?
	 * 
	 * @param hasRead the hasRead to set
	 */
	public UserEntityPermissions withHasRead(boolean hasRead) {
		this.hasRead = hasRead;
		return this;
	}

	/**
	 * Does the user have the download permission on this entity?
	 */
	public boolean hasDownload() {
		return hasDownload;
	}

	/**
	 * Does the user have the download permission on this entity?
	 */
	public UserEntityPermissions withHasDownload(boolean hasDownload) {
		this.hasDownload = hasDownload;
		return this;
	}

	/**
	 * Does the user have the create permission on this entity?
	 */
	public boolean hasCreate() {
		return hasCreate;
	}

	/**
	 * Does the user have the create permission on this entity?
	 */
	public UserEntityPermissions withHasCreate(boolean hasCreate) {
		this.hasCreate = hasCreate;
		return this;
	}

	/**
	 * Does the user have the delete permission on this entity?
	 */
	public boolean hasDelete() {
		return hasDelete;
	}

	/**
	 * Does the user have the delete permission on this entity?
	 */
	public UserEntityPermissions withHasDelete(boolean hasDelete) {
		this.hasDelete = hasDelete;
		return this;
	}

	/**
	 * Does the user have the change_permission permission on this entity?
	 */
	public boolean hasChangePermissions() {
		return hasChangePermissions;
	}

	/**
	 * Does the user have the change_permission permission on this entity?
	 */
	public UserEntityPermissions withHasChangePermissions(boolean hasChangePermissions) {
		this.hasChangePermissions = hasChangePermissions;
		return this;
	}

	/**
	 * Does the user have the change_settings permission on this entity?
	 */
	public boolean hasChangeSettings() {
		return hasChangeSettings;
	}

	/**
	 * Does the user have the change_settings permission on this entity?
	 */
	public UserEntityPermissions withHasChangeSettings(boolean hasChangeSettings) {
		this.hasChangeSettings = hasChangeSettings;
		return this;
	}

	/**
	 * Does the user have the moderate permission on this entity?
	 */
	public boolean hasModerate() {
		return hasModerate;
	}

	/**
	 * Does the user have the moderate permission on this entity?
	 */
	public UserEntityPermissions withHasModerate(boolean hasModerate) {
		this.hasModerate = hasModerate;
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(benefactorId, dataType, doesEntityExist, entityId, entityType, hasChangePermissions,
				hasChangeSettings, hasCreate, hasDelete, hasDownload, hasModerate, hasRead);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof UserEntityPermissions)) {
			return false;
		}
		UserEntityPermissions other = (UserEntityPermissions) obj;
		return Objects.equals(benefactorId, other.benefactorId) && dataType == other.dataType
				&& doesEntityExist == other.doesEntityExist && Objects.equals(entityId, other.entityId)
				&& entityType == other.entityType && hasChangePermissions == other.hasChangePermissions
				&& hasChangeSettings == other.hasChangeSettings && hasCreate == other.hasCreate
				&& hasDelete == other.hasDelete && hasDownload == other.hasDownload && hasModerate == other.hasModerate
				&& hasRead == other.hasRead;
	}

	@Override
	public String toString() {
		return "UserEntityPermissions [entityId=" + entityId + ", benefactorId=" + benefactorId + ", entityType="
				+ entityType + ", dataType=" + dataType + ", doesEntityExist=" + doesEntityExist + ", hasRead="
				+ hasRead + ", hasDownload=" + hasDownload + ", hasCreate=" + hasCreate + ", hasDelete=" + hasDelete
				+ ", hasChangePermissions=" + hasChangePermissions + ", hasChangeSettings=" + hasChangeSettings
				+ ", hasModerate=" + hasModerate + "]";
	}

}
