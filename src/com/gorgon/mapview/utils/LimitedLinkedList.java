package com.gorgon.mapview.utils;

import java.util.LinkedList;

public class LimitedLinkedList<X> extends LinkedList<X> {
	// A member of serializable class
	private static final long serialVersionUID = 776;
	
	@Override
	public boolean add(X tile) {
		if(this.size() >= Preferences.MAX_REQUESTED_TILES_LIST_SIZE) {
			this.removeFirst();
			Helper.LOGD("LimitedLinkedList removed: " + tile);
		}
		
		return super.add(tile);
	}
}
