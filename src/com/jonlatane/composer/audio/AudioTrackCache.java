package com.jonlatane.composer.audio;

import android.media.AudioTrack;
import android.util.Log;
import android.util.Pair;
import android.util.SparseArray;

import java.util.LinkedList;
import java.util.List;

/**
 * A cache for AudioTrack resources in Android.  Theoretically you should get 32 tracks to do what
 * you will with, minus tracks for what other applications do in the background.  Effectively,
 * 32-note polyphony across whatever you can use to make AudioTracks.
 *
 * Created by jonlatane on 7/19/15.
 */
public class AudioTrackCache {
    private static String TAG = "ToneGenerator";

    /** Maps ({@link #hashCode} of instrument -> note integer value with C4 = 0 -> {@link AudioTrack} */
    private static SparseArray<SparseArray<AudioTrack>> trackData = new SparseArray<SparseArray<AudioTrack>>();
    private static LinkedList<Pair<Integer,Integer>> recentlyUsedTracks = new LinkedList<Pair<Integer,Integer>>();


    /**
     * Get a looping AudioTrack exactly one period long for the given note.  This may be generated,
     * or it may come from the cache. The track requested will become the MRU element.
     *
     * @param n the fundamental frequency
     * @param generator a generator for the track
     * @return the requested AudioTrack
     */
    public static AudioTrack getAudioTrackForNote(int n, AudioTrackGenerator generator) {
        // Find the hashcode of the overtone series
        int generatorHashCode = generator.hashCode();

        // cacheLocation.first tells us the instrument/overtone series (via the hash of the Double[])
        // cacheLocation.second tells us the specific note
        Pair<Integer,Integer> cacheLocation = new Pair<Integer,Integer>(generatorHashCode, n);

        // Update list of recently used notes with this first
        recentlyUsedTracks.remove(cacheLocation);
        recentlyUsedTracks.addFirst(cacheLocation);

        // See if this note is in our cache
        SparseArray<AudioTrack> instrumentNotes = trackData.get(cacheLocation.first);
        if(instrumentNotes == null) {
            instrumentNotes = new SparseArray<AudioTrack>();
            trackData.put(cacheLocation.first, instrumentNotes);
        }
        AudioTrack result = instrumentNotes.get(n);

        // If not, generate it
        if( result == null ) {
            result = generator.getAudioTrackFor(n);
        }

        instrumentNotes.put(n, result);

        return result;
    }

    /**
     * Release all AudioTracks created by this cache
     *
     * @return false unless there was an error
     */
    public static boolean releaseAll() {
        boolean result = releaseOne();
        boolean shouldLoopAgain = result;
        while(shouldLoopAgain == true) {
            shouldLoopAgain = releaseOne();
        }
        return result;
    }

    /**
     * Release all AudioTracks using the given overtone series
     *
     * @param generator a generator with a unique hashCode
     * @return true on success
     */
    public static boolean releaseAll(AudioTrackGenerator generator) {
        SparseArray<AudioTrack> instrumentNotes = trackData.get(generator.hashCode());
        if(instrumentNotes == null)
            return true;
        boolean result = true;
        for(int i=0; i < instrumentNotes.size(); i++) {
            try {
                AudioTrack goodbyeCruelWorld = instrumentNotes.valueAt(i);
                goodbyeCruelWorld.stop();
                goodbyeCruelWorld.flush();
                goodbyeCruelWorld.release();
            } catch( Throwable e ) {
                result = false;
            }
        }
        instrumentNotes.clear();
        return result;
    }

    /**
     * Release the least recently used AudioTrack resource to free up resources
     *
     * @return true if a track was successfully removed
     */
    public static boolean releaseOne() {
        boolean result = true;
        try {
            Pair<Integer, Integer> lruNote = recentlyUsedTracks.removeLast();
            AudioTrack goodbyeCruelWorld = trackData.get(lruNote.first).get(lruNote.second);
            goodbyeCruelWorld.flush();
            goodbyeCruelWorld.release();
            trackData.get(lruNote.first).remove(lruNote.second);
        } catch( Throwable e ) {
            result = false;
        }
        return result;
    }

    public static void normalizeVolumes() {
        float max = AudioTrack.getMaxVolume();
        float min = AudioTrack.getMinVolume();
        float span = max - min;

        //Set<AudioTrack> currentlyPlaying = new HashSet<AudioTrack>();
        SparseArray<List<AudioTrack>> currentlyPlaying = new SparseArray<List<AudioTrack>>();
        //for(Map.Entry<Integer,HashMap<Integer, AudioTrack>> e : trackData.entrySet()) {
        for(int i = 0; i < trackData.size(); i++) {
            SparseArray<AudioTrack> noteTracks = trackData.valueAt(i);
            //for(AudioTrack t : e.getValue().values()) {
            for(int j = 0; j < noteTracks.size(); j++) {
                AudioTrack t = noteTracks.valueAt(j);
                if(t.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
                    int n = trackData.keyAt(i);
                    List<AudioTrack> l = currentlyPlaying.get(n);
                    if(l == null) {
                        l = new LinkedList<AudioTrack>();
                        currentlyPlaying.put(n,l);
                    }
                    l.add(t);
                }
            }
        }

        for(int i = 0; i < currentlyPlaying.size(); i++) {
            int n = currentlyPlaying.keyAt(i);
            List<AudioTrack> l = currentlyPlaying.valueAt(i);
            for(AudioTrack t : l) {
                // Lower notes are amped up so turn them down a bit with this factor
                float arctanCurveFactor = (float) (.05 * Math.atan((float)(n+5)/88.0) + .9);
                // This number between 0 and 1 by which we will increase volume
                float totalNumNotesRedFactor = (float)( 1 / (float)currentlyPlaying.size());
                float adjusted = min
                        + (float)( span * arctanCurveFactor * totalNumNotesRedFactor );
                Log.i(TAG,max + " " + min + "Normalizing volume to " + adjusted);
                t.setStereoVolume(adjusted, adjusted);
            }
        }
    }
}
