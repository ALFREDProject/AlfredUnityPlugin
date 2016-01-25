using UnityEngine;

public class Alfred {
	private static AndroidJavaClass clazz;

	public static void Init() {
		if (Application.platform == RuntimePlatform.Android) {
			clazz = new AndroidJavaClass("de.tu_darmstadt.kom.alfredplugin.Alfred");
			clazz.CallStatic("init");
		}
	}

	public static int GetNextAction() {
		if (Application.platform == RuntimePlatform.Android) {
			return clazz.CallStatic<int>("getNextAction");
		}
		else {
			return -1;
		}
	}
}
