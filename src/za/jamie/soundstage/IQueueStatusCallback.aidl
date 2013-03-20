package za.jamie.soundstage;

import za.jamie.soundstage.models.Track;

oneway interface IQueueStatusCallback {
	void onQueueChanged(in List<Track> queue);
	void onQueuePositionChanged(int position);
}