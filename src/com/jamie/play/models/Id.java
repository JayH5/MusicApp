package com.jamie.play.models;

import java.util.Comparator;

public class Id implements IdProvider {

	private final long mId;
	
	public Id(long id) {
		mId = id;
	}
	
	@Override
	public long getId() {
		return mId;
	}
	
	public String toString() {
		return String.valueOf(mId);
	}
	
	public static final Comparator<IdProvider> mComparator = 
			new Comparator<IdProvider>() {

				@Override
				public int compare(IdProvider arg0, IdProvider arg1) {
					if (arg0.getId() < arg1.getId()) {
						return -1;
					} else if (arg0.getId() > arg1.getId()) {
						return 1;
					} else {
						return 0;
					}
				}
		
	};

}
