package com.tom.cpl.item;

public interface Inventory {
	int size();
	Stack getInSlot(int i);
	void reset();

	default Stack getStack(int i) {
		if(i >= 0 && i < size())return getInSlot(i);
		else return Stack.EMPTY;
	}

	int getNamedSlotId(NamedSlot slot);
}
