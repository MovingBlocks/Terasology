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

import org.terasology.model.inventory.Icon;

public final class ModIcons {
	
	private static final String MINIONICONS16 = "miniion:minionicon16";

	public static void loadIcons() {
		Icon.set("minionskull", "miniion:minionicon16", 0, 0);
		Icon.set("minioncommand", "miniion:minionicon16", 0, 1);
		Icon.set("emptycard", "miniion:minionicon16", 0, 2);
		Icon.set("filledcard", "miniion:minionicon16", 0, 3);
		Icon.set("cardbook", "miniion:minionicon16", 0, 4);
		Icon.set("oreominionbook", "miniion:minionicon16", 0, 5);
		Icon.set("zonebook", "miniion:minionicon16", 0, 6);
		Icon.set("zonetool", "miniion:minionicon16", 0, 7);
		
		Icon.set("mulch", MINIONICONS16, 1, 0);
		Icon.set("paper", MINIONICONS16, 1, 1);
		Icon.set("bookcover", MINIONICONS16, 1, 2);
	}
}
