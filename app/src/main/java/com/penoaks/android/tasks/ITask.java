/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 * Copyright (c) 2017 Chiori Greene a.k.a. Chiori-chan <me@chiorichan.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 *
 * All Rights Reserved.
 */
package com.penoaks.android.tasks;


/**
 * Represents a task being executed by the scheduler
 */

public interface ITask
{
	
	/**
	 * Returns the taskId for the task.
	 * 
	 * @return Task id number
	 */
	int getTaskId();
	
	/**
	 * Returns the TaskCreator that owns this task.
	 * 
	 * @return The TaskCreator that owns the task
	 */
	TaskRegistrar getOwner();
	
	/**
	 * Returns true if the Task is a sync task.
	 * 
	 * @return true if the task is run by main thread
	 */
	boolean isSync();
}
