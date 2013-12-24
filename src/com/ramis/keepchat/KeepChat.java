//TO DO 
// MOVE FILES WHEN FOLDER CHANGES
// make errors appear all the time
// new option
// new logging to display errors and initial start

package com.ramis.keepchat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findConstructorBestMatch;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.getBooleanField;

public class KeepChat implements IXposedHookLoadPackage {

	private static final String PACKAGE_NAME = KeepChat.class.getPackage()
			.getName();

	private XSharedPreferences prefs;

	private Context context;

	private boolean isStory = false;
	private boolean isSnap = false;
	private boolean isSnapImage = false;
	private boolean isSnapVideo = false;
	private boolean displayDialog = false;

	final int SAVE_AUTO = 0;
	final int SAVE_ASK = 1;
	final int DO_NOT_SAVE = 2;

	//@formatter:off
	final int CLASS_RECEIVEDSNAP = 0;                     				 // ReceivedSnap class name
    final int FUNCTION_RECEIVEDSNAP_GETIMAGEBITMAP = 1;    				 // ReceivedSnap.getImageBitmap() function name
    final int FUNCTION_RECEIVEDSNAP_GETVIDEOURI = 2;      				 // ReceivedSnap.getVideoUri() function name
    final int CLASS_STORY = 3;                             				 // Story class name
    final int FUNCTION_STORY_GETIMAGEBITMAP = 4;          				 // Story.getImageBitmap() function name
    final int FUNCTION_STORY_GETVIDEOURI = 5;             				 // Story.getVideoUri() function name
    final int FUNCTION_RECEIVEDSNAP_MARKVIEWED = 6;       				 // ReceivedSnap.markViewed() function name
    final int CLASS_SNAPVIEW = 7;                        				 // SnapView class name
    final int FUNCTION_SNAPVIEW_SHOWIMAGE = 8;           				 // SnapView.showImage() function name
    final int FUNCTION_SNAPVIEW_SHOWVIDEO = 9;           				 // SnapView.showVideo() function name
    final int FUNCTION_RECEIVEDSNAP_GETSENDER = 10;       				 // ReceivedSnap.getSender() function name
    final int FUNCTION_STORY_GETSENDER = 11;              				 // Story.getSender()
    final int FUNCTION_SNAP_GETTIMESTAMP = 12;            				 // Snap.getTimestamp()
    final int CLASS_SNAP_PREVIEW_FRAGMENT = 13;			  				 // SnapPreviewFragment class name
    final int FUNCTION_SNAPPREVIEWFRAGMENT_PREPARESNAPFORSENDING = 14;	 // SnapPreviewFragment.prepareSnapForSending() function name
    final int VARIABLE_SNAPPREVIEWFRAGMENT_ISVIDEOSNAP = 15; 			 // SnapPreviewFragment.mIsVideoSnap variable name
    final int VARIABLE_SNAPPREVIEWFRAGMENT_SNAPCAPTUREDEVENT = 16;		 // SnapPreviewFragment.SnapCapturedEvent variable name
    final int FUNCTION_SNAPPREVIEWFRAGMENT_VIDEOURI = 17;				 // SnapPreviewFragment.SnapCapturedEvent.getVideoUri() function name
    final int FUNCTION_SNAPPREVIEWFRAGMENT_GETSNAPBITMAP = 18;		 	 // SnapPreviewFragment.getSnapBitmap() function name
    final int VARIABLE_SNAPPREVIEWFRAGMENT_SNAPEDITORVIEW = 19;			 // SnapPreviewFragment.SnapEditorView variable name
    final int CLASS_SNAPUPDATE = 20;									 // SnapUpdate class name
    final int CLASS_STORYVIEWRECORD = 21;								 // StoryViewRecord class name
    //@formatter:on

	private boolean isSaved = false;

	private String mediaPath = "";

	private String toastMessage = "";

	private String savePath;
	private int imagesSnapSavingMode;
	private int videosSnapSavingMode;
	private int imagesStorySavingMode;
	private int videosStorySavingMode;
	private boolean toastMode;
	private int toastLength;
	private boolean saveSentSnaps;
	private boolean debugMode;
	private boolean sortFilesMode;

	// loading package
	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
		if (!lpparam.packageName.equals("com.snapchat.android"))
			return;
		else {

			XposedBridge
					.log("\n------------------- KEEPCHAT STARTED --------------------");
			XposedBridge.log("Snapchat Loaded");

			String versionName;
			try {
				Object activityThread = callStaticMethod(
						findClass("android.app.ActivityThread", null),
						"currentActivityThread");
				Context context = (Context) callMethod(activityThread,
						"getSystemContext");
				PackageInfo piSnapChat = context.getPackageManager()
						.getPackageInfo(lpparam.packageName, 0);
				versionName = piSnapChat.versionName;
				XposedBridge.log("SnapChat Version Name: "
						+ piSnapChat.versionName);
				XposedBridge.log("SnapChat Version Code: "
						+ piSnapChat.versionCode);
				PackageInfo piKeepchat = context.getPackageManager()
						.getPackageInfo(PACKAGE_NAME, 0);
				XposedBridge.log("KeepChat Version Name: "
						+ piKeepchat.versionName);
				XposedBridge.log("KeepChat Version Code: "
						+ piKeepchat.versionCode);
				XposedBridge.log("Android Release: " + Build.VERSION.RELEASE);

			} catch (Exception e) {
				XposedBridge
						.log("Exception while trying to get version info. ("
								+ e.getMessage() + ")");
				return;
			}

			VersionResolution resolution = new VersionResolution(versionName);

			final Boolean legacy = resolution.getLegacy();

			if (!resolution.versionSupported()) {
				XposedBridge.log("We don't currently support version '"
						+ versionName + "', wait for an update");
				XposedBridge
						.log("If you can, pull the apk off your device and submit it to the xda thread");
				findAndHookMethod("com.snapchat.android.LandingPageActivity",
						lpparam.classLoader, "onCreate", Bundle.class,
						new XC_MethodHook() {
							protected void afterHookedMethod(
									MethodHookParam param) throws Throwable {
								Toast.makeText(
										(Context) callMethod(param.thisObject,
												"getApplicationContext"),
										"This version of snapchat is currently not supported.",
										Toast.LENGTH_LONG).show();
							}
						});
				return;
			}

			final SparseArray<String> names = resolution.getNames();

			XposedBridge
					.log("---------------------------------------------------------");

			// get new Preferences
			prefs = new XSharedPreferences(PACKAGE_NAME);
			refreshPreferences();
			printSettings();

			/*
			 * getImageBitmap() hook The ReceivedSnap class has a method to load
			 * a Bitmap in preparation for viewing. This method returns said
			 * bitmap back so the application can display it. We hook this
			 * method to intercept the result and write it to the SD card. The
			 * file path is stored in the mediaPath member for later use in the
			 * showImage() hook.
			 */
			findAndHookMethod(names.get(CLASS_RECEIVEDSNAP),
					lpparam.classLoader,
					names.get(FUNCTION_RECEIVEDSNAP_GETIMAGEBITMAP),
					Context.class, new XC_MethodHook() {
						@Override
						protected void afterHookedMethod(MethodHookParam param)
								throws Throwable {

							refreshPreferences();
							printSettings();
							logging("\n----------------------- KEEPCHAT ------------------------");
							logging("Image Snap opened");
							isSnap = true;
							isStory = false;
							if (imagesSnapSavingMode == DO_NOT_SAVE) {
								logging("Not Saving Image");
								logging("---------------------------------------------------------");
							} else {

								String sender = (String) callMethod(
										param.thisObject,
										names.get(FUNCTION_RECEIVEDSNAP_GETSENDER));
								SimpleDateFormat fnameDateFormat = new SimpleDateFormat(
										"yyyy-MM-dd_HH-mm-ss", Locale
												.getDefault());
								Date timestamp = new Date((Long) callMethod(
										param.thisObject,
										names.get(FUNCTION_SNAP_GETTIMESTAMP)));
								String filename = sender + "_"
										+ (fnameDateFormat.format(timestamp));

								File file = createFile(filename + ".jpg",
										"/RecievedSnaps");
								logging(mediaPath);
								if (file.exists()) {
									logging("Image Snap already Exists");
									toastMessage = "The image already exists.";
									isSaved = true;
								} else {
									isSaved = false;
									Bitmap image = (Bitmap) param.getResult();
									if (saveImage(image, file)) {
										logging("Image Snap has been Saved");
										toastMessage = "The image has been saved.";
									} else {
										logging("Error Saving Image Snap. Error 1.");
										toastMessage = "The image could not be saved. Error 1.";
									}
								}
							}
						}
					});

			/*
			 * getImageBitmap() hook The Story class has a method to load a
			 * Bitmap in preparation for viewing a Image in Story. This method
			 * returns said bitmap back so the application can display it. We
			 * hook this method to intercept the result and write it to the SD
			 * card. The file path is stored in the mediaPath member for later
			 * use in the showImage() hook.
			 */
			findAndHookMethod(names.get(CLASS_STORY), lpparam.classLoader,
					names.get(FUNCTION_STORY_GETIMAGEBITMAP), Context.class,
					new XC_MethodHook() {
						@Override
						protected void afterHookedMethod(MethodHookParam param)
								throws Throwable {

							refreshPreferences();
							printSettings();
							logging("\n----------------------- KEEPCHAT ------------------------");
							logging("Image Story opened");
							isSnap = false;
							isStory = true;
							if (imagesStorySavingMode == DO_NOT_SAVE) {
								logging("Not Saving Image");
								logging("---------------------------------------------------------");
							} else {

								String sender = (String) callMethod(
										param.thisObject,
										names.get(FUNCTION_STORY_GETSENDER));
								SimpleDateFormat fnameDateFormat = new SimpleDateFormat(
										"yyyy-MM-dd_HH-mm-ss", Locale
												.getDefault());
								Date timestamp = new Date((Long) callMethod(
										param.thisObject,
										names.get(FUNCTION_SNAP_GETTIMESTAMP)));
								String filename = sender + "_"
										+ (fnameDateFormat.format(timestamp));

								File file = createFile(filename + ".jpg",
										"/Stories");
								logging(mediaPath);
								if (file.exists()) {
									logging("Image Story already Exists");
									isSaved = true;
									toastMessage = "The image already exists.";
								} else {
									isSaved = false;
									Bitmap image = (Bitmap) param.getResult();
									if (saveImage(image, file)) {
										logging("Image Story has been Saved");
										toastMessage = "The image has been saved.";
									} else {
										logging("Error Saving Image Snap. Error 1.");
										toastMessage = "The image could not be saved. Error 1.";
									}
								}
							}
						}
					});

			/*
			 * getVideoUri() hook The ReceivedSnap class treats videos a little
			 * differently. Videos are not their own object, so they can't be
			 * passed around. The Android system basically provides a VideoView
			 * for viewing videos, which you just provide it the location of the
			 * video and it does the rest.
			 * 
			 * Unsurprisingly, Snapchat makes use of this View. This method in
			 * the ReceivedSnap class gets the URI of the video in preparation
			 * for one of these VideoViews. We hook in, intercept the result (a
			 * String), then copy the bytes from that location to our SD
			 * directory. This results in a bit of a slowdown for the user, but
			 * luckily this takes place before they actually view it.
			 * 
			 * The file path is stored in the mediaPath member for later use in
			 * the showVideo() hook.
			 */
			findAndHookMethod(names.get(CLASS_RECEIVEDSNAP),
					lpparam.classLoader,
					names.get(FUNCTION_RECEIVEDSNAP_GETVIDEOURI),
					new XC_MethodHook() {
						@Override
						protected void afterHookedMethod(MethodHookParam param)
								throws Throwable {

							refreshPreferences();
							printSettings();

							if (param.thisObject.toString().contains("Story")) {
								logging("\n----------------------- KEEPCHAT ------------------------");
								logging("Video Story opened");
								isSnap = false;
								isStory = true;
								if (videosStorySavingMode == DO_NOT_SAVE) {
									logging("Not Saving Video");
									logging("---------------------------------------------------------");
								} else {

									String sender = (String) callMethod(
											param.thisObject,
											names.get(FUNCTION_STORY_GETSENDER));
									SimpleDateFormat fnameDateFormat = new SimpleDateFormat(
											"yyyy-MM-dd_HH-mm-ss", Locale
													.getDefault());
									Date timestamp = new Date(
											(Long) callMethod(
													param.thisObject,
													names.get(FUNCTION_SNAP_GETTIMESTAMP)));
									String filename = sender
											+ "_"
											+ (fnameDateFormat
													.format(timestamp));

									File file = createFile(filename + ".mp4",
											"/Stories");
									logging(mediaPath);
									if (file.exists()) {
										isSaved = true;
										logging("Video Story already Exists");
										toastMessage = "The video already exists";
									} else {
										isSaved = false;
										String videoUri = (String) param
												.getResult();
										if (saveVideo(videoUri, file)) {
											logging("Video Story has been Saved");
											toastMessage = "The video has been saved.";
										} else {
											logging("Error Saving Video Story. Error 2.");
											toastMessage = "The video could not be saved. Error 2.";
										}
									}
								}
							} else {

								logging("\n----------------------- KEEPCHAT ------------------------");
								logging("Video Snap opened");
								isSnap = true;
								isStory = false;

								if (videosSnapSavingMode == DO_NOT_SAVE) {
									logging("Not Saving Video");
									logging("---------------------------------------------------------");
								} else {

									String sender = (String) callMethod(
											param.thisObject,
											names.get(FUNCTION_RECEIVEDSNAP_GETSENDER));
									SimpleDateFormat fnameDateFormat = new SimpleDateFormat(
											"yyyy-MM-dd_HH-mm-ss", Locale
													.getDefault());
									Date timestamp = new Date(
											(Long) callMethod(
													param.thisObject,
													names.get(FUNCTION_SNAP_GETTIMESTAMP)));
									String filename = sender
											+ "_"
											+ (fnameDateFormat
													.format(timestamp));

									File file = createFile(filename + ".mp4",
											"/RecievedSnaps");
									logging(mediaPath);
									if (file.exists()) {
										isSaved = true;
										logging("Video Snap already Exists");
										toastMessage = "The video already exists";
									} else {
										isSaved = false;
										String videoUri = (String) param
												.getResult();
										if (saveVideo(videoUri, file)) {
											logging("Video Snap has been Saved");
											toastMessage = "The video has been saved.";
										} else {
											logging("Error Saving Video Snap. Error 2.");
											toastMessage = "The video could not be saved. Error 2.";
										}
									}
								}
							}
						}

					});

			/*
			 * getVideoUri() hook The Story class because the stories use the
			 * Story class and not the RecievedSnap class to prepare the video.
			 */
			if (resolution.videoStoryLegacy()) {
				findAndHookMethod(names.get(CLASS_STORY), lpparam.classLoader,
						names.get(FUNCTION_STORY_GETVIDEOURI),
						new XC_MethodHook() {
							@Override
							protected void afterHookedMethod(
									MethodHookParam param) throws Throwable {

								refreshPreferences();
								printSettings();
								logging("\n----------------------- KEEPCHAT ------------------------");
								logging("Video Story opened");
								isSnap = false;
								isStory = true;
								if (videosStorySavingMode == DO_NOT_SAVE) {
									logging("Not Saving Video");
									logging("---------------------------------------------------------");
								} else {

									String sender = (String) callMethod(
											param.thisObject,
											names.get(FUNCTION_STORY_GETSENDER));
									SimpleDateFormat fnameDateFormat = new SimpleDateFormat(
											"yyyy-MM-dd_HH-mm-ss", Locale
													.getDefault());
									Date timestamp = new Date(
											(Long) callMethod(
													param.thisObject,
													names.get(FUNCTION_SNAP_GETTIMESTAMP)));
									String filename = sender
											+ "_"
											+ (fnameDateFormat
													.format(timestamp));

									File file = createFile(filename + ".mp4",
											"/Stories");
									logging(mediaPath);
									if (file.exists()) {
										isSaved = true;
										logging("Video Story already Exists");
										toastMessage = "The video already exists";
									} else {
										isSaved = false;
										String videoUri = (String) param
												.getResult();
										if (saveVideo(videoUri, file)) {
											logging("Video Story has been Saved");
											toastMessage = "The video has been saved.";
										} else {
											logging("Error Saving Video Story. Error 2.");
											toastMessage = "The video could not be saved. Error 2.";
										}
									}
								}
							}

						});
			}

			// hook for saving sent snaps
			findAndHookMethod(
					names.get(CLASS_SNAP_PREVIEW_FRAGMENT),
					lpparam.classLoader,
					names.get(FUNCTION_SNAPPREVIEWFRAGMENT_PREPARESNAPFORSENDING),
					new XC_MethodHook() {

						@Override
						protected void afterHookedMethod(MethodHookParam param)
								throws Throwable {

							refreshPreferences();
							if (saveSentSnaps == true) {
								printSettings();
								logging("\n----------------------- KEEPCHAT ------------------------");
								Date cDate = new Date();
								String filename = new SimpleDateFormat(
										"yyyy-MM-dd_HH-mm-ss", Locale
												.getDefault()).format(cDate);

								if (getBooleanField(
										param.thisObject,
										names.get(VARIABLE_SNAPPREVIEWFRAGMENT_ISVIDEOSNAP))) {
									logging("Video Sent Snap");
									File file = createFile(filename + ".mp4",
											"/SentSnaps");
									logging(mediaPath);
									if (file.exists()) {
										logging("Video Sent Snap already Exists");
										toastMessage = "This video already exists.";
									} else {
										Object obj = getObjectField(
												param.thisObject,
												names.get(VARIABLE_SNAPPREVIEWFRAGMENT_SNAPCAPTUREDEVENT));
										String videoUri = ((Uri) callMethod(
												obj,
												names.get(FUNCTION_SNAPPREVIEWFRAGMENT_VIDEOURI)))
												.getPath();

										if (saveVideo(videoUri, file)) {
											logging("Video Sent Snap has been Saved");
											toastMessage = "The video has been saved.";
										} else {
											logging("Error Saving Video Sent Snap. Error 3.");
											toastMessage = "The video could not be saved. Error 3.";
										}
									}
								} else {
									// snap is a image
									logging("Image Sent Snap");
									File file = createFile(filename + ".jpg",
											"/SentSnaps");
									logging(mediaPath);
									if (file.exists()) {
										logging("file already exists");
										toastMessage = "The image already exists";
									} else {
										Bitmap image;
										if (legacy) {
											image = (Bitmap) callMethod(
													param.thisObject,
													names.get(FUNCTION_SNAPPREVIEWFRAGMENT_GETSNAPBITMAP));
										} else {
											Object obj = getObjectField(
													param.thisObject,
													names.get(VARIABLE_SNAPPREVIEWFRAGMENT_SNAPEDITORVIEW));
											image = (Bitmap) callMethod(
													obj,
													names.get(FUNCTION_SNAPPREVIEWFRAGMENT_GETSNAPBITMAP));
										}

										if (saveImage(image, file)) {
											logging("Image Sent Snap has been Saved");
											toastMessage = "The image has been saved.";
										} else {
											logging("Error Saving Image Sent Snap. Error 3.");
											toastMessage = "The image could not be saved. Error 3.";
										}
									}

								}

								runMediaScanAndToast((Context) callMethod(
										param.thisObject, "getActivity"));
							}
						}

					});

			/*
			 * showVideo() and showImage() hooks Because getVideoUri() and
			 * getImageBitmap() do not handily provide a context, nor do their
			 * parent classes (ReceivedSnap), we are unable to get the context
			 * necessary in order to display a notification and call the media
			 * scanner.
			 * 
			 * But these getters are called from the corresponding showVideo()
			 * and showImage() methods of com.snapchat.android.ui.SnapView,
			 * which deliver the needed context. So the work that needs a
			 * context is done here, while the file saving work is done in the
			 * getters. The getters also save the file paths in the mediaPath
			 * member, which we use here.
			 */
			findAndHookMethod(names.get(CLASS_SNAPVIEW), lpparam.classLoader,
					names.get(FUNCTION_SNAPVIEW_SHOWIMAGE),
					new XC_MethodHook() {
						@Override
						protected void afterHookedMethod(MethodHookParam param)
								throws Throwable {
							refreshPreferences();

							isSnapVideo = false;
							isSnapImage = true;

							if (((isSnap == true) && (imagesSnapSavingMode != DO_NOT_SAVE))
									|| ((isStory == true) && (imagesStorySavingMode != DO_NOT_SAVE))) {
								// At this point the context is put in the
								// private
								// member so that the dialog can be
								// initiated from the markViewed() hook
								context = (Context) callMethod(
										param.thisObject, "getContext");
								if (((isSnap == true) && (imagesSnapSavingMode == SAVE_AUTO))
										|| ((isStory == true) && (imagesStorySavingMode == SAVE_AUTO))) {
									runMediaScanAndToast(context);
								} else {
									displayDialog = true;
								}
							}
						}
					});

			findAndHookMethod(names.get(CLASS_SNAPVIEW), lpparam.classLoader,
					names.get(FUNCTION_SNAPVIEW_SHOWVIDEO), Context.class,
					new XC_MethodHook() {
						@Override
						protected void afterHookedMethod(MethodHookParam param)
								throws Throwable {

							isSnapVideo = true;
							isSnapImage = false;
							refreshPreferences();
							if (((isSnap == true) && (videosSnapSavingMode != DO_NOT_SAVE))
									|| ((isStory == true) && (videosStorySavingMode != DO_NOT_SAVE))) {
								// At this point the context is put in the
								// private
								// member so that the dialog can be
								// initiated from the markViewed() hook
								context = (Context) param.args[0];
								if (((isSnap == true) && (videosSnapSavingMode == SAVE_AUTO))
										|| ((isStory == true) && (videosStorySavingMode == SAVE_AUTO))) {
									runMediaScanAndToast(context);
								} else {
									displayDialog = true;
								}
							}
						}
					});

			findAndHookMethod(names.get(CLASS_RECEIVEDSNAP),
					lpparam.classLoader,
					names.get(FUNCTION_RECEIVEDSNAP_MARKVIEWED),
					new XC_MethodHook() {
						@Override
						protected void beforeHookedMethod(MethodHookParam param)
								throws Throwable {
							refreshPreferences();
							// check if its save ask AND that it doesn't exist
							if (displayDialog == true) {
								if (((isSnapImage == true)
										&& (isSaved == false) && (imagesSnapSavingMode == SAVE_ASK))
										|| ((isSnapVideo == true)
												&& (isSaved == false) && (videosSnapSavingMode == SAVE_ASK))) {

									showDialog(context);
									displayDialog = false;
								}
							}

						}
					});

			Constructor<?> constructor;

			if (legacy) {
				constructor = findConstructorBestMatch(
						findClass(names.get(CLASS_SNAPUPDATE),
								lpparam.classLoader), Long.class, Integer.class);

			} else {
				constructor = findConstructorBestMatch(
						findClass(names.get(CLASS_SNAPUPDATE),
								lpparam.classLoader), Long.class,
						Integer.class, Integer.class);
			}

			XposedBridge.hookMethod(constructor, new XC_MethodHook() {
				protected void beforeHookedMethod(MethodHookParam param)
						throws Throwable {

					param.args[1] = 0;

				}
			});

			Constructor<?> constructor2 = findConstructorBestMatch(
					findClass(names.get(CLASS_STORYVIEWRECORD),
							lpparam.classLoader), String.class, Long.class,
					Integer.class);

			XposedBridge.hookMethod(constructor2, new XC_MethodHook() {
				protected void beforeHookedMethod(MethodHookParam param)
						throws Throwable {

					param.args[2] = 0;

				}
			});

			// set snapchat to debug mode so it prints out things in logcat

			// findAndHookMethod("com.snapchat.android.Timber",
			// lpparam.classLoader, "debugMode",
			// new XC_MethodReplacement() {
			// @Override
			// protected Object replaceHookedMethod(
			// MethodHookParam param) throws Throwable {
			// return true;
			// }
			// });

		}

	}

	/*
	 * Tells the media scanner to scan the newly added image or video so that it
	 * shows up in the gallery without a reboot. And shows a Toast message where
	 * the media was saved.
	 * 
	 * @param context Current context
	 * 
	 * @param filePath File to be scanned by the media scanner
	 */
	private void runMediaScanAndToast(Context context) {

		try {
			logging("MediaScanner running ");
			// Run MediaScanner on file, so it shows up in Gallery instantly
			MediaScannerConnection.scanFile(context,
					new String[] { mediaPath }, null,
					new MediaScannerConnection.OnScanCompletedListener() {
						public void onScanCompleted(String path, Uri uri) {
							if (uri != null) {
								logging("MediaScanner ran successfully: "
										+ uri.toString());
							} else {
								logging("Unknown error occurred while trying to run MediaScanner");
							}
							logging("---------------------------------------------------------");
						}
					});
		} catch (Exception e) {
			logging("Error occurred while trying to run MediaScanner");
			e.printStackTrace();
			logging("---------------------------------------------------------");
		}
		// construct the toast notification
		if (toastMode == true) {
			logging("Toast Displayed");
			if (toastLength == 0) {
				Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT)
						.show();
			} else {
				Toast.makeText(context, toastMessage, Toast.LENGTH_LONG).show();
			}
		}

	}

	private void showDialog(final Context dContext) {
		// 1. Instantiate an AlertDialog.Builder with its constructor
		AlertDialog.Builder builder = new AlertDialog.Builder(dContext);

		// 2. Chain together various setter methods to set the dialog
		// characteristics
		final String mediaTypeStr = isSnapImage ? "image" : "video";
		builder.setMessage("Would you like to save the " + mediaTypeStr + "?\n")
				.setTitle("Save " + mediaTypeStr + "?");

		builder.setPositiveButton("Save",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						logging("User choose to save. Keep saved file.");
						runMediaScanAndToast(dContext);
					}
				});

		builder.setNegativeButton("Discard",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						logging("User choose not to save.");
						dialog.cancel();
					}
				});

		builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				if ((new File(mediaPath)).delete())
					logging("File deleted successfully");
				else
					logging("Could not delete file.");
			}
		});
		// 3. Get the AlertDialog from create()
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	// function to saveimage
	private boolean saveImage(Bitmap myImage, File fileToSave) {

		try {
			FileOutputStream out = new FileOutputStream(fileToSave);
			myImage.compress(Bitmap.CompressFormat.JPEG, 90, out);
			out.flush();
			out.close();
		} catch (Exception e) {
			XposedBridge.log(Log.getStackTraceString(e));
			return false;
		}
		return true;
	}

	// function to save video
	private boolean saveVideo(String videoUri, File fileToSave) {

		try {
			FileInputStream in = new FileInputStream(new File(videoUri));
			FileOutputStream out = new FileOutputStream(fileToSave);

			byte[] buf = new byte[1024];
			int len;

			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}

			in.close();
			out.flush();
			out.close();
		} catch (Exception e) {
			XposedBridge.log(Log.getStackTraceString(e));
			return false;
		}
		return true;
	}

	// creates file
	private File createFile(String fileName, String savePathSuffix) {

		File myDir;
		if (sortFilesMode == true) {
			myDir = new File(savePath + savePathSuffix);
		} else {
			myDir = new File(savePath);
		}

		myDir.mkdirs();

		File toReturn = new File(myDir, fileName);

		try {
			mediaPath = toReturn.getCanonicalPath();
		} catch (IOException e) {
			XposedBridge.log(Log.getStackTraceString(e));
		}

		return toReturn;
	}

	// refresh all preferences
	private void refreshPreferences() {

		prefs.reload();
		savePath = prefs.getString("pref_key_save_location", "");
		imagesSnapSavingMode = Integer.parseInt(prefs.getString(
				"pref_key_snaps_images", Integer.toString(SAVE_AUTO)));
		videosSnapSavingMode = Integer.parseInt(prefs.getString(
				"pref_key_snaps_videos", Integer.toString(SAVE_AUTO)));
		imagesStorySavingMode = Integer.parseInt(prefs.getString(
				"pref_key_stories_images", Integer.toString(SAVE_AUTO)));
		videosStorySavingMode = Integer.parseInt(prefs.getString(
				"pref_key_stories_videos", Integer.toString(SAVE_AUTO)));
		toastMode = prefs.getBoolean("pref_key_toasts_checkbox", true);
		saveSentSnaps = prefs.getBoolean("pref_key_save_sent_snaps", false);
		toastLength = Integer
				.parseInt(prefs.getString("pref_key_toasts_duration",
						Integer.toString(Toast.LENGTH_LONG)));
		debugMode = prefs.getBoolean("pref_key_debug_mode", false);
		sortFilesMode = prefs.getBoolean("pref_key_sort_files_mode", true);
		// in case the user doesn't open settings when first installed. need a
		// default save location
		if (savePath == "") {
			String root = Environment.getExternalStorageDirectory().toString();
			savePath = root + "/keepchat";
		}

	}

	private void logging(String message) {
		if (debugMode == true) {
			XposedBridge.log(message);
		}
	}

	private void printSettings() {
		logging("\n------------------- KEEPCHAT SETTINGS -------------------");
		logging("savepath: " + savePath);
		if (imagesSnapSavingMode == SAVE_AUTO) {
			logging("imagesSnapSavingMode: " + "SAVE_AUTO");
		} else if (imagesSnapSavingMode == SAVE_ASK) {
			logging("imagesSnapSavingMode: " + "SAVE_ASK");
		} else if (imagesSnapSavingMode == DO_NOT_SAVE) {
			logging("imagesSnapSavingMode: " + "DO_NOT_SAVE");
		}

		if (videosSnapSavingMode == SAVE_AUTO) {
			logging("videosSnapSavingMode: " + "SAVE_AUTO");
		} else if (videosSnapSavingMode == SAVE_ASK) {
			logging("videosSnapSavingMode: " + "SAVE_ASK");
		} else if (videosSnapSavingMode == DO_NOT_SAVE) {
			logging("videosSnapSavingMode: " + "DO_NOT_SAVE");
		}

		if (imagesStorySavingMode == SAVE_AUTO) {
			logging("imagesStorySavingMode: " + "SAVE_AUTO");
		} else if (imagesStorySavingMode == SAVE_ASK) {
			logging("imagesStorySavingMode: " + "SAVE_ASK");
		} else if (imagesStorySavingMode == DO_NOT_SAVE) {
			logging("imagesStorySavingMode: " + "DO_NOT_SAVE");
		}

		if (videosStorySavingMode == SAVE_AUTO) {
			logging("videosStorySavingMode: " + "SAVE_AUTO");
		} else if (videosStorySavingMode == SAVE_ASK) {
			logging("videosStorySavingMode: " + "SAVE_ASK");
		} else if (videosStorySavingMode == DO_NOT_SAVE) {
			logging("videosStorySavingMode: " + "DO_NOT_SAVE");
		}
		logging("toastMode: " + toastMode);
		logging("saveSentSnaps: " + saveSentSnaps);
		logging("toastLength: " + toastLength);
		logging("sortFilesMode: " + sortFilesMode);
		logging("---------------------------------------------------------");
	}

}
