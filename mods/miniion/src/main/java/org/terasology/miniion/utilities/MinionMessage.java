/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.miniion.utilities;

import org.terasology.entitySystem.EntityRef;
import org.terasology.miniion.minionenum.MinionMessagePriority;

/**
 * Created with IntelliJ IDEA. User: Overdhose Date: 24/05/12 Time: 22:28 To
 * change this template use File | Settings | File Templates.
 */
public class MinionMessage implements Comparable<MinionMessage> {

	private MinionMessagePriority minionMessagePriority;
	private String messageTitle;
	private String messageDescription;
	private String messageContent;
	private EntityRef Minion;
	private EntityRef Player;
	private int index;

	public MinionMessage(MinionMessagePriority minionmessagetype,
			String messagetitle, String messagedescription,
			String messagecontent, EntityRef minion, EntityRef player) {
		minionMessagePriority = minionmessagetype;
		messageTitle = messagetitle;
		messageDescription = messagedescription;
		messageContent = messagecontent;
		Minion = minion;
		Player = player;
	}

	public MinionMessagePriority getMinionMessagePriority() {
		return minionMessagePriority;
	}

	public String getMessageTitle() {
		return messageTitle;
	}

	public String getMessageDescription() {
		return messageDescription;
	}

	public String getMessageContent() {
		return messageContent;
	}

	public EntityRef getMinion() {
		return Minion;
	}

	public EntityRef getPlayer() {
		return Player;
	}

	@Override
	public int compareTo(MinionMessage o) {
		if (this.getMinionMessagePriority().ordinal() < o
				.getMinionMessagePriority().ordinal()) {
			return -1;
		} else if (this.getMinionMessagePriority().ordinal() > o
				.getMinionMessagePriority().ordinal()) {
			return 1;
		}
		return 0;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}
}
