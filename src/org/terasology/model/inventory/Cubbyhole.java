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

	public Cubbyhole insert(Item item, int count) {
		if (_item == null) {
			simpleInsert(item, count);
			
			return null;
		}
		
		return _item.equals(item) ? updateInsert(item, count) : replaceInsert(item, count);
	}
	
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
