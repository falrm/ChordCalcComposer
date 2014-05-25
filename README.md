ChordCalcComposer
=================

ChordCalcComposer is an attempt at creating a truly touch-oriented interface for interacting with and editing scores.
The base app, ChordCalc, is merely a keyboard which converts input into chord symbols.  This output can be used to
easily generate a lead sheet by non-composers.  This is the only release-ready component of the project and can be found
on the Play Store here: https://play.google.com/store/apps/details?id=com.jonlatane.composer

The scoredisplay package is a system for displaying zoomable, 
optimally-formatted scores, though it is not yet complete.  Some stubs for a better version are in scoredisplay2.

**Contribute**

ChordCalc could use a number of contributions.  The main targets include:

* an improved (possibly native) audio engine, ideally with support for SoundFonts
* a completed score display engine (scoredisplay2)

**IDE support**

ChordCalc was started in AIDE, resumed on the Eclipse Android SDK and has most recently transitioned to Android Studio.
In making these transitions I have attempted to maintain backwards compatibility, so any of these IDEs should work for
making changes and building.
