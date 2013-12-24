package com.ramis.keepchat;

import java.util.HashMap;
import java.util.Map;

import android.util.SparseArray;

public class VersionResolution {
	
	private String version;
	private boolean legacy;

	private int CLASS_RECEIVEDSNAP = 0;                     				 // ReceivedSnap class name
    private int FUNCTION_RECEIVEDSNAP_GETIMAGEBITMAP = 1;    				 // ReceivedSnap.getImageBitmap() function name
    private int FUNCTION_RECEIVEDSNAP_GETVIDEOURI = 2;      				 // ReceivedSnap.getVideoUri() function name
    private int CLASS_STORY = 3;                             				 // Story class name
    private int FUNCTION_STORY_GETIMAGEBITMAP = 4;          				 // Story.getImageBitmap() function name
    private int FUNCTION_STORY_GETVIDEOURI = 5;             				 // Story.getVideoUri() function name
    private int FUNCTION_RECEIVEDSNAP_MARKVIEWED = 6;       				 // ReceivedSnap.markViewed() function name
    private int CLASS_SNAPVIEW = 7;                        				 // SnapView class name
    private int FUNCTION_SNAPVIEW_SHOWIMAGE = 8;           				 // SnapView.showImage() function name
    private int FUNCTION_SNAPVIEW_SHOWVIDEO = 9;           				 // SnapView.showVideo() function name
    private int FUNCTION_RECEIVEDSNAP_GETSENDER = 10;       				 // ReceivedSnap.getSender() function name
    private int FUNCTION_STORY_GETSENDER = 11;              				 // Story.getSender()
    private int FUNCTION_SNAP_GETTIMESTAMP = 12;            				 // Snap.getTimestamp()
    private int CLASS_SNAP_PREVIEW_FRAGMENT = 13;			  				 // SnapPreviewFragment class name
    private int FUNCTION_SNAPPREVIEWFRAGMENT_PREPARESNAPFORSENDING = 14;	 // SnapPreviewFragment.prepareSnapForSending() function name
    private int VARIABLE_SNAPPREVIEWFRAGMENT_ISVIDEOSNAP = 15; 			 // SnapPreviewFragment.mIsVideoSnap variable name
    private int VARIABLE_SNAPPREVIEWFRAGMENT_SNAPCAPTUREDEVENT = 16;		 // SnapPreviewFragment.SnapCapturedEvent variable name
    private int FUNCTION_SNAPPREVIEWFRAGMENT_VIDEOURI = 17;				 // SnapPreviewFragment.SnapCapturedEvent.getVideoUri() function name
    private int FUNCTION_SNAPPREVIEWFRAGMENT_GETSNAPBITMAP = 18;		 	 // SnapPreviewFragment.getSnapBitmap() function name
    private int VARIABLE_SNAPPREVIEWFRAGMENT_SNAPEDITORVIEW = 19;			 // SnapPreviewFragment.SnapEditorView variable name
    private int CLASS_SNAPUPDATE = 20;									 // SnapUpdate class name
    private int CLASS_STORYVIEWRECORD = 21;								 // StoryViewRecord class name

    Map<String, SparseArray<String>> versionResolution = new HashMap<String, SparseArray<String>>();
   	private SparseArray<String> currentVersionNames = new SparseArray<String>();

   	private String basename = "com.snapchat.android.";
	private SparseArray<String> names_original = new SparseArray<String>();
	private SparseArray<String> names_obfuscated_4021 = new SparseArray<String>();
	private SparseArray<String> names_obfuscated_4022 = new SparseArray<String>();
	private SparseArray<String> names_obfuscated_4101 = new SparseArray<String>();


	public VersionResolution(String version){
		this.version = version;
		legacy = versionCompare(version, "4.0.20") <= 0;
		
		if (legacy){
			this.version = "4.0.20";
		}
		
		versionResolution.put("4.0.20", names_original);
		versionResolution.put("4.0.21 Beta", names_obfuscated_4021);
		versionResolution.put("4.0.22 Beta", names_obfuscated_4022);
		versionResolution.put("4.1.01", names_obfuscated_4101);
		
		setNames();
	}

	public boolean getLegacy(){
		return legacy;
	}
	
	public boolean versionSupported(){
		return !(!versionResolution.containsKey(version) && !legacy);
	}
	
	public SparseArray<String> getNames(){
		return currentVersionNames;
	}
	
	private void setNames() {
		createNames();
		currentVersionNames = versionResolution.get(version);
	}

	private void createNames(){
		//@formatter:off
		names_original.put(CLASS_RECEIVEDSNAP, basename	+ "model.ReceivedSnap");
		names_original.put(CLASS_STORY, basename + "model.Story");
		names_original.put(CLASS_SNAPVIEW, basename + "ui.SnapView");
		names_original.put(CLASS_SNAP_PREVIEW_FRAGMENT, basename + "SnapPreviewFragment");
		names_original.put(CLASS_SNAPUPDATE, basename + "model.server.SnapUpdate");
		names_original.put(CLASS_STORYVIEWRECORD, basename + "model.server.model.StoryViewRecord");
		names_original.put(FUNCTION_RECEIVEDSNAP_GETIMAGEBITMAP,"getImageBitmap");
		names_original.put(FUNCTION_RECEIVEDSNAP_GETVIDEOURI,"getVideoUri");
		names_original.put(FUNCTION_RECEIVEDSNAP_GETSENDER,	"getSender");
		names_original.put(FUNCTION_RECEIVEDSNAP_MARKVIEWED,"markViewed");
		names_original.put(FUNCTION_STORY_GETIMAGEBITMAP,"getImageBitmap");
		names_original.put(FUNCTION_STORY_GETVIDEOURI,"getVideoUri");
		names_original.put(FUNCTION_STORY_GETSENDER, "getSender");
		names_original.put(FUNCTION_SNAPVIEW_SHOWIMAGE, "showImage");
		names_original.put(FUNCTION_SNAPVIEW_SHOWVIDEO, "showVideo");
		names_original.put(FUNCTION_SNAP_GETTIMESTAMP,"getTimestamp");	
		names_original.put(FUNCTION_SNAPPREVIEWFRAGMENT_PREPARESNAPFORSENDING,"prepareSnapForSending");
		names_original.put(VARIABLE_SNAPPREVIEWFRAGMENT_ISVIDEOSNAP, "mIsVideoSnap");
		names_original.put(VARIABLE_SNAPPREVIEWFRAGMENT_SNAPCAPTUREDEVENT,"mSnapCapturedEvent");
		names_original.put(FUNCTION_SNAPPREVIEWFRAGMENT_VIDEOURI,"getVideoUri");
		names_original.put(FUNCTION_SNAPPREVIEWFRAGMENT_GETSNAPBITMAP,"getSnapBitmap");
		names_original.put(VARIABLE_SNAPPREVIEWFRAGMENT_SNAPEDITORVIEW, "N/A");

		names_obfuscated_4021.put(CLASS_RECEIVEDSNAP, basename + "model.ReceivedSnap");
		names_obfuscated_4021.put(CLASS_STORY, basename + "model.Story");
		names_obfuscated_4021.put(CLASS_SNAPVIEW, basename + "ui.SnapView");
		names_obfuscated_4021.put(CLASS_SNAP_PREVIEW_FRAGMENT,basename + "SnapPreviewFragment");
		names_obfuscated_4021.put(CLASS_SNAPUPDATE, basename + "model.server.SnapUpdate");
		names_obfuscated_4021.put(CLASS_STORYVIEWRECORD, basename + "model.server.model.StoryViewRecord");
		names_obfuscated_4021.put(FUNCTION_RECEIVEDSNAP_GETIMAGEBITMAP, "a");
		names_obfuscated_4021.put(FUNCTION_RECEIVEDSNAP_GETVIDEOURI,"z");
		names_obfuscated_4021.put(FUNCTION_RECEIVEDSNAP_GETSENDER,"d");
		names_obfuscated_4021.put(FUNCTION_RECEIVEDSNAP_MARKVIEWED,"i");
		names_obfuscated_4021.put(FUNCTION_STORY_GETIMAGEBITMAP, "a");
		names_obfuscated_4021.put(FUNCTION_STORY_GETVIDEOURI, "z");
		names_obfuscated_4021.put(FUNCTION_STORY_GETSENDER, "Z");
		names_obfuscated_4021.put(FUNCTION_SNAPVIEW_SHOWIMAGE, "l");
		names_obfuscated_4021.put(FUNCTION_SNAPVIEW_SHOWVIDEO, "a");
		names_obfuscated_4021.put(FUNCTION_SNAP_GETTIMESTAMP, "K");
		names_obfuscated_4021.put(FUNCTION_SNAPPREVIEWFRAGMENT_PREPARESNAPFORSENDING, "o");
		names_obfuscated_4021.put(VARIABLE_SNAPPREVIEWFRAGMENT_ISVIDEOSNAP, "p");
		names_obfuscated_4021.put(VARIABLE_SNAPPREVIEWFRAGMENT_SNAPCAPTUREDEVENT, "y");
		names_obfuscated_4021.put(FUNCTION_SNAPPREVIEWFRAGMENT_VIDEOURI, "c");
		names_obfuscated_4021.put(FUNCTION_SNAPPREVIEWFRAGMENT_GETSNAPBITMAP,"getSnapBitmap");
		names_obfuscated_4021.put(VARIABLE_SNAPPREVIEWFRAGMENT_SNAPEDITORVIEW, "c");

		names_obfuscated_4022.put(CLASS_RECEIVEDSNAP, basename + "model.ReceivedSnap");
		names_obfuscated_4022.put(CLASS_STORY, basename + "model.Story");
		names_obfuscated_4022.put(CLASS_SNAPVIEW, basename + "ui.SnapView");
		names_obfuscated_4022.put(CLASS_SNAP_PREVIEW_FRAGMENT,basename + "SnapPreviewFragment");
		names_obfuscated_4022.put(CLASS_SNAPUPDATE, basename + "model.server.SnapUpdate");
		names_obfuscated_4022.put(CLASS_STORYVIEWRECORD, basename + "model.server.model.StoryViewRecord");
		names_obfuscated_4022.put(FUNCTION_RECEIVEDSNAP_GETIMAGEBITMAP, "a");
		names_obfuscated_4022.put(FUNCTION_RECEIVEDSNAP_GETVIDEOURI,"B");
		names_obfuscated_4022.put(FUNCTION_RECEIVEDSNAP_GETSENDER,"e");
		names_obfuscated_4022.put(FUNCTION_RECEIVEDSNAP_MARKVIEWED,"j");
		names_obfuscated_4022.put(FUNCTION_STORY_GETIMAGEBITMAP, "a");
		names_obfuscated_4022.put(FUNCTION_STORY_GETVIDEOURI, "N/A");
		names_obfuscated_4022.put(FUNCTION_STORY_GETSENDER, "ab");
		names_obfuscated_4022.put(FUNCTION_SNAPVIEW_SHOWIMAGE, "l");
		names_obfuscated_4022.put(FUNCTION_SNAPVIEW_SHOWVIDEO, "a");
		names_obfuscated_4022.put(FUNCTION_SNAP_GETTIMESTAMP, "M");
		names_obfuscated_4022.put(FUNCTION_SNAPPREVIEWFRAGMENT_PREPARESNAPFORSENDING, "p");
		names_obfuscated_4022.put(VARIABLE_SNAPPREVIEWFRAGMENT_ISVIDEOSNAP, "o");
		names_obfuscated_4022.put(VARIABLE_SNAPPREVIEWFRAGMENT_SNAPCAPTUREDEVENT, "w");
		names_obfuscated_4022.put(FUNCTION_SNAPPREVIEWFRAGMENT_VIDEOURI, "c");
		names_obfuscated_4022.put(FUNCTION_SNAPPREVIEWFRAGMENT_GETSNAPBITMAP,"getSnapBitmap");
		names_obfuscated_4022.put(VARIABLE_SNAPPREVIEWFRAGMENT_SNAPEDITORVIEW, "c");

		names_obfuscated_4101.put(CLASS_RECEIVEDSNAP, basename + "model.ReceivedSnap");
		names_obfuscated_4101.put(CLASS_STORY, basename + "model.Story");
		names_obfuscated_4101.put(CLASS_SNAPVIEW, basename + "ui.SnapView");
		names_obfuscated_4101.put(CLASS_SNAP_PREVIEW_FRAGMENT,basename + "SnapPreviewFragment");
		names_obfuscated_4101.put(CLASS_SNAPUPDATE, basename + "model.server.SnapUpdate");
		names_obfuscated_4101.put(CLASS_STORYVIEWRECORD, basename + "model.StoryViewRecord");
		names_obfuscated_4101.put(FUNCTION_RECEIVEDSNAP_GETIMAGEBITMAP, "a");
		names_obfuscated_4101.put(FUNCTION_RECEIVEDSNAP_GETVIDEOURI,"C");
		names_obfuscated_4101.put(FUNCTION_RECEIVEDSNAP_GETSENDER,"e");
		names_obfuscated_4101.put(FUNCTION_RECEIVEDSNAP_MARKVIEWED,"j");
		names_obfuscated_4101.put(FUNCTION_STORY_GETIMAGEBITMAP, "a");
		names_obfuscated_4101.put(FUNCTION_STORY_GETVIDEOURI, "N/A");
		names_obfuscated_4101.put(FUNCTION_STORY_GETSENDER, "e");
		names_obfuscated_4101.put(FUNCTION_SNAPVIEW_SHOWIMAGE, "m");
		names_obfuscated_4101.put(FUNCTION_SNAPVIEW_SHOWVIDEO, "a");
		names_obfuscated_4101.put(FUNCTION_SNAP_GETTIMESTAMP, "O");
		names_obfuscated_4101.put(FUNCTION_SNAPPREVIEWFRAGMENT_PREPARESNAPFORSENDING, "q");
		names_obfuscated_4101.put(VARIABLE_SNAPPREVIEWFRAGMENT_ISVIDEOSNAP, "o");
		names_obfuscated_4101.put(VARIABLE_SNAPPREVIEWFRAGMENT_SNAPCAPTUREDEVENT, "w");
		names_obfuscated_4101.put(FUNCTION_SNAPPREVIEWFRAGMENT_VIDEOURI, "c");
		names_obfuscated_4101.put(FUNCTION_SNAPPREVIEWFRAGMENT_GETSNAPBITMAP,"getSnapBitmap");
		names_obfuscated_4101.put(VARIABLE_SNAPPREVIEWFRAGMENT_SNAPEDITORVIEW, "c");
		//@formatter:on
	}
	
	private Integer versionCompare(String str1, String str2) {
		String[] vals1 = str1.replaceAll("[^0-9.]", "").split("\\.");
		String[] vals2 = str2.replaceAll("[^0-9.]", "").split("\\.");
		int i = 0;
		while (i < vals1.length && i < vals2.length
				&& vals1[i].equals(vals2[i])) {
			i++;
		}

		if (i < vals1.length && i < vals2.length) {
			int diff = Integer.valueOf(vals1[i]).compareTo(
					Integer.valueOf(vals2[i]));
			return Integer.signum(diff);
		}

		return Integer.signum(vals1.length - vals2.length);
	}
	
}
