package org.terasology.model.inventory;

/**
 * Holder for a single item type that may or may not be attached to an Inventory. Handles
 * overflow.
 */
public class Cubbyhole {
	private Item _item;
	private int _count;

	public Cubbyhole() {
		_item = null;
		_count = 0;
	}
	
	public Cubbyhole(Item item, int count) {
		_item = item;
		_count = count;
	}

	public Cubbyhole insert(Item item) {
		return insert(item, 1);
	}

	/**
	 * 
	 * @param item the item to insert
	 * @param count the number of copies to insert
	 * @return a Cubbhole containing any overflow that results from inserting item, or null if there was no
	 *         overflow
	 */
	public Cubbyhole insert(Item item, int count) {
		if (_item == null) {
			simpleInsert(item, count);
			
			return null;
		}
		
		return _item.equals(item) ? updateInsert(item, count) : replaceInsert(item, count);
	}
	
	/**
	 * Removes count copies of Item.
	 * 
	 * @param count number of copies to remove
	 * @return the Item that was completely removed, or null if this Cubbyhole has not been emptied
	 * @throws IllegalArgumentException if removing more items than exist
	 */
	public Item remove(int count) {
		if (count > _count) {
			throw new IllegalArgumentException("Removing too many items results in negative item count: " + count);
		}
		
		_count -= count;
		
		if (isEmpty()) {
			Item result = _item;
			_item = null;
			return result;
		}
		
		return null;
	}
	
	public Cubbyhole clear() {
		if (isEmpty()) {
			return null;
		}
		
		Cubbyhole result = new Cubbyhole(_item, _count);
		_item = null;
		_count = 0;
		
		return result;
	}

	public Item getItem() {
		return _item;
	}

	public int getItemCount() {
		return _count;
	}
	
	public boolean isEmpty() {
		return _count == 0;
	}
	
	public boolean isFull() {
		return _item != null ? _count == _item.getStackSize() : false;
	}
	
	private Cubbyhole replaceInsert(Item item, int count) {
		Cubbyhole result = new Cubbyhole();
		result._item = _item;
		result._count = _count;
		
		_item = item;
		_count = count;

		return result;
	}
	
	private Cubbyhole updateInsert(Item item, int count) {
		int newCount = _count + count;
		
		if (item.getStackSize() >= newCount) {
			_count += count;
			
			return null;				
		} else {
			_count = item.getStackSize();
			int overflow = newCount - item.getStackSize();
			return new Cubbyhole(item, overflow);
		}
	}
	
	private void simpleInsert(Item item, int count) {
		_item = item;
		_count = count;
	}
}
