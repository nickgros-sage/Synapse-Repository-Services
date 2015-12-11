package org.sagebionetworks.repo.model.dao.discussion;

import java.util.Date;
import java.util.List;

import org.sagebionetworks.reflection.model.PaginatedResults;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadBundle;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadOrder;

public interface DiscussionThreadDAO {

	/**
	 * Create a new discussion thread
	 * 
	 * @param forumId
	 * @param threadId
	 * @param title
	 * @param messageKey
	 * @param userId
	 * @return
	 */
	public DiscussionThreadBundle createThread(String forumId, String threadId,
			String title, String messageKey, long userId);

	/**
	 * Get a discussion thread
	 * 
	 * @param threadId
	 * @return
	 */
	public DiscussionThreadBundle getThread(long threadId);

	/**
	 * Get the number of discussion thread in a given forum
	 * 
	 * @param forumId
	 * @return
	 */
	public long getThreadCount(long forumId);

	/**
	 * Get a paginated list of discussion thread for a forum given forumId,
	 * the order of the discussion thread, limit and offset
	 * 
	 * @param forumId
	 * @param limit
	 * @param offset
	 * @param order
	 * @param ascending
	 * @return
	 */
	public PaginatedResults<DiscussionThreadBundle> getThreads(long forumId,
			Long limit, Long offset, DiscussionThreadOrder order, Boolean ascending);

	/**
	 * Mark a discussion thread as deleted
	 * 
	 * @param threadId
	 */
	public void markThreadAsDeleted(long threadId);

	/**
	 * Update a discussion thread message
	 * 
	 * @param threadId
	 * @param newMessageKey
	 */
	public DiscussionThreadBundle updateMessageKey(long threadId, String newMessageKey);

	/**
	 * Update a discussion thread title
	 * 
	 * @param threadId
	 * @param title
	 */
	public DiscussionThreadBundle updateTitle(long threadId, String title);

	/**
	 * update number of views for the given thread
	 * 
	 * @param threadId
	 * @param numberOfViews
	 */
	public void setNumberOfViews(long threadId, long numberOfViews);

	/**
	 * update number of replies for the given thread
	 * 
	 * @param threadId
	 * @param numberOfReplies
	 */
	public void setNumberOfReplies(long threadId, long numberOfReplies);

	/**
	 * update the last activity of the given thread
	 * 
	 * @param threadId
	 * @param lastActivity
	 */
	public void setLastActivity(long threadId, Date lastActivity);

	/**
	 * update active authors for the given thread
	 * 
	 * @param threadId
	 * @param activeAuthors - the top 5 active authors
	 */
	public void setActiveAuthors(long threadId, List<String> activeAuthors);

	/**
	 * insert ignore a record into THREAD_VIEW table
	 * 
	 * @param threadId
	 * @param userId
	 */
	public void updateThreadView(long threadId, long userId);

	/**
	 * count the number of users who viewed this thread
	 * 
	 * @param threadId
	 */
	public long countThreadView(long threadId);

	/**
	 * Get the etag before attempt to update
	 * 
	 * @param threadId
	 * @return
	 */
	public String getEtagForUpdate(long threadId);
}