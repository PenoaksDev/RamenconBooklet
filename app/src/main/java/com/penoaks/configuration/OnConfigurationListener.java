/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 * Copyright (c) 2017 Chiori Greene a.k.a. Chiori-chan <me@chiorichan.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 *
 * All Rights Reserved.
 */
package com.penoaks.configuration;

public interface OnConfigurationListener
{
	/**
	 * Called after a new section is added to tree
	 *
	 * @param section The new child section
	 */
	void onSectionAdd( ConfigurationSection section );

	/**
	 * Called after a section is removed from the tree
	 *
	 * @param parent        The parent of the removed child
	 * @param orphanedChild The orphaned child before it's left for the GC
	 */
	void onSectionRemove( ConfigurationSection parent, ConfigurationSection orphanedChild );

	/**
	 * Called after a section has been altered, i.e., values added, removed, or changed.
	 *
	 * @param parent      The parent section
	 * @param affectedKey The altered key
	 */
	void onSectionChange( ConfigurationSection parent, String affectedKey );
}
